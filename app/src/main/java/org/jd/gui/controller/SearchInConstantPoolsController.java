/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.controller;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.api.model.Type;
import org.jd.gui.model.container.DelegatingFilterContainer;
import org.jd.gui.service.type.TypeFactoryService;
import org.jd.gui.spi.TypeFactory;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.function.TriConsumer;
import org.jd.gui.view.SearchInConstantPoolsView;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SearchInConstantPoolsController implements IndexesChangeListener {
    protected static final int CACHE_MAX_ENTRIES = 5*20*9;

    protected API api;
    protected ScheduledExecutorService executor;

    protected JFrame mainFrame;
    protected SearchInConstantPoolsView searchInConstantPoolsView;
    protected Map<String, Map<String, Collection>> cache;
    protected Set<DelegatingFilterContainer> delegatingFilterContainers = new HashSet<>();
    protected Collection<Future<Indexes>> collectionOfFutureIndexes;
    protected Consumer<URI> openCallback;
    protected long indexesHashCode = 0L;
    protected volatile ScheduledFuture<?> pendingSearch = null;

    @SuppressWarnings("unchecked")
    public SearchInConstantPoolsController(API api, ScheduledExecutorService executor, JFrame mainFrame) {
        this.api = api;
        this.executor = executor;
        this.mainFrame = mainFrame;
        // Create UI
        this.searchInConstantPoolsView = new SearchInConstantPoolsView(
            api, mainFrame,
            new BiConsumer<String, Integer>() {
                @Override public void accept(String pattern, Integer flags) { updateTree(pattern, flags); }
            },
            new TriConsumer<URI, String, Integer>() {
                @Override public void accept(URI uri, String pattern, Integer flags) { onTypeSelected(uri, pattern, flags); }
            }
        );
        // Create result cache
        this.cache = new LinkedHashMap<String, Map<String, Collection>>(CACHE_MAX_ENTRIES*3/2, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Map<String, Collection>> eldest) {
                return size() > CACHE_MAX_ENTRIES;
            }
        };
        // Load history from preferences
        try {
            Map<String, String> prefs = api.getPreferences();
            String recentSearchesStr = prefs.get("SearchInConstantPoolsView.recentSearches");
            if (recentSearchesStr != null && !recentSearchesStr.isEmpty()) {
                searchInConstantPoolsView.populateSearchHistory(Arrays.asList(recentSearchesStr.split("\\|\\|\\|")), "");
            }
            String recentMasksStr = prefs.get("SearchInConstantPoolsView.recentFileMasks");
            if (recentMasksStr != null && !recentMasksStr.isEmpty()) {
                searchInConstantPoolsView.populateFileMaskHistory(Arrays.asList(recentMasksStr.split("\\|\\|\\|")), "*");
            } else {
                searchInConstantPoolsView.populateFileMaskHistory(null, "*");
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @SuppressWarnings("unchecked")
    public void show(Collection<Future<Indexes>> collectionOfFutureIndexes, Consumer<URI> openCallback) {
        // Init attributes
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        this.openCallback = openCallback;

        // Load recent history before showing
        try {
            Map<String, String> prefs = api.getPreferences();
            String recentSearchesStr = prefs.get("SearchInConstantPoolsView.recentSearches");
            if (recentSearchesStr != null && !recentSearchesStr.isEmpty()) {
                searchInConstantPoolsView.populateSearchHistory(
                    Arrays.asList(recentSearchesStr.split("\\|\\|\\|")),
                    searchInConstantPoolsView.getPattern()
                );
            }
            String recentMasksStr = prefs.get("SearchInConstantPoolsView.recentFileMasks");
            if (recentMasksStr != null && !recentMasksStr.isEmpty()) {
                searchInConstantPoolsView.populateFileMaskHistory(
                    Arrays.asList(recentMasksStr.split("\\|\\|\\|")),
                    searchInConstantPoolsView.getFileMask()
                );
            }
        } catch (Exception e) {
            // ignore
        }

        // Refresh view
        long hashCode = collectionOfFutureIndexes.hashCode();
        if (hashCode != indexesHashCode) {
            // List of indexes has changed
            updateTree(searchInConstantPoolsView.getPattern(), searchInConstantPoolsView.getFlags());
            indexesHashCode = hashCode;
        }
        // Show
        searchInConstantPoolsView.show();
    }

    @SuppressWarnings("unchecked")
    protected void updateTree(String pattern, int flags) {
        // Cancel any previous pending search
        if (pendingSearch != null && !pendingSearch.isDone()) {
            pendingSearch.cancel(false);
        }

        // Debounce: wait 200ms after the last keystroke before running the search
        pendingSearch = executor.schedule(() -> {
            // Waiting the end of indexation...
            searchInConstantPoolsView.showWaitCursor();
            delegatingFilterContainers.clear();

            int matchingTypeCount = 0;
            int patternLength = pattern.length();
            String fileMask = searchInConstantPoolsView.getFileMask();

            if (patternLength > 0) {
                try {
                    for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                        if (futureIndexes.isDone()) {
                            Indexes indexes = futureIndexes.get();
                            HashSet<Container.Entry> matchingEntries = new HashSet<>();
                            // Find matched entries
                            filter(indexes, pattern, flags, fileMask, matchingEntries);

                            if (!matchingEntries.isEmpty()) {
                                // Search root container with first matching entry
                                Container.Entry parentEntry = matchingEntries.iterator().next();
                                Container container = null;

                                while (parentEntry.getContainer().getRoot() != null) {
                                    container = parentEntry.getContainer();
                                    parentEntry = container.getRoot().getParent();
                                }

                                // TODO In a future release, display matching strings, types, inner-types, fields and methods, not only matching files
                                matchingEntries = getOuterEntries(matchingEntries);

                                matchingTypeCount += matchingEntries.size();

                                // Create a filtered container
                                delegatingFilterContainers.add(new DelegatingFilterContainer(container, matchingEntries));
                            }
                        }
                    }
                } catch (Exception e) {
                    ExceptionUtil.printStackTrace(e);
                }
            }

            final int count = matchingTypeCount;

            searchInConstantPoolsView.hideWaitCursor();
            searchInConstantPoolsView.updateTree(delegatingFilterContainers, count);
        }, 200, TimeUnit.MILLISECONDS);
    }

    protected HashSet<Container.Entry> getOuterEntries(Set<Container.Entry> matchingEntries) {
        HashMap<Container.Entry, Container.Entry> innerTypeEntryToOuterTypeEntry = new HashMap<>();
        HashSet<Container.Entry> matchingOuterEntriesSet = new HashSet<>();

        for (Container.Entry entry : matchingEntries) {
            TypeFactory typeFactory = TypeFactoryService.getInstance().get(entry);

            if (typeFactory != null) {
                Type type = typeFactory.make(api, entry, null);

                if ((type != null) && (type.getOuterName() != null)) {
                    Container.Entry outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);

                    if (outerTypeEntry == null) {
                        HashMap<String, Container.Entry> typeNameToEntry = new HashMap<>();
                        HashMap<String, String> innerTypeNameToOuterTypeName = new HashMap<>();

                        // Populate "typeNameToEntry" and "innerTypeNameToOuterTypeName"
                        for (Container.Entry e : entry.getParent().getChildren()) {
                            typeFactory = TypeFactoryService.getInstance().get(e);

                            if (typeFactory != null) {
                                type = typeFactory.make(api, e, null);

                                if (type != null) {
                                    typeNameToEntry.put(type.getName(), e);
                                    if (type.getOuterName() != null) {
                                        innerTypeNameToOuterTypeName.put(type.getName(), type.getOuterName());
                                    }
                                }
                            }
                        }

                        // Search outer type entries and populate "innerTypeEntryToOuterTypeEntry"
                        for (Map.Entry<String, String> e : innerTypeNameToOuterTypeName.entrySet()) {
                            Container.Entry innerTypeEntry = typeNameToEntry.get(e.getKey());

                            if (innerTypeEntry != null) {
                                String outerTypeName = e.getValue();

                                for (;;) {
                                    String typeName = innerTypeNameToOuterTypeName.get(outerTypeName);
                                    if (typeName != null) {
                                        outerTypeName = typeName;
                                    } else {
                                        break;
                                    }
                                }

                                outerTypeEntry = typeNameToEntry.get(outerTypeName);

                                if (outerTypeEntry != null) {
                                    innerTypeEntryToOuterTypeEntry.put(innerTypeEntry, outerTypeEntry);
                                }
                            }
                        }

                        // Get outer type entry
                        outerTypeEntry = innerTypeEntryToOuterTypeEntry.get(entry);

                        if (outerTypeEntry == null) {
                            outerTypeEntry = entry;
                        }
                    }

                    matchingOuterEntriesSet.add(outerTypeEntry);
                } else{
                    matchingOuterEntriesSet.add(entry);
                }
            } else {
                matchingOuterEntriesSet.add(entry);
            }
        }

        return matchingOuterEntriesSet;
    }

    protected void filter(Indexes indexes, String pattern, int flags, String fileMask, Set<Container.Entry> matchingEntries) {
        boolean declarations = ((flags & SearchInConstantPoolsView.SEARCH_DECLARATION) != 0);
        boolean references   = ((flags & SearchInConstantPoolsView.SEARCH_REFERENCE) != 0);
        boolean caseInsensitive = ((flags & SearchInConstantPoolsView.SEARCH_CASE_INSENSITIVE) != 0);
        boolean useRegex        = ((flags & SearchInConstantPoolsView.SEARCH_REGEX) != 0);
        boolean exactMatch      = ((flags & SearchInConstantPoolsView.SEARCH_EXACT_MATCH) != 0);

        if ((flags & SearchInConstantPoolsView.SEARCH_TYPE) != 0) {
            if (declarations)
                match(indexes, "typeDeclarations", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
            if (references)
                match(indexes, "typeReferences", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_CONSTRUCTOR) != 0) {
            if (declarations)
                match(indexes, "constructorDeclarations", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
            if (references)
                match(indexes, "constructorReferences", pattern,
                      SearchInConstantPoolsController::matchTypeEntriesWithChar,
                      SearchInConstantPoolsController::matchTypeEntriesWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_METHOD) != 0) {
            if (declarations)
                match(indexes, "methodDeclarations", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
            if (references)
                match(indexes, "methodReferences", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_FIELD) != 0) {
            if (declarations)
                match(indexes, "fieldDeclarations", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
            if (references)
                match(indexes, "fieldReferences", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_STRING) != 0) {
            if (declarations || references)
                match(indexes, "strings", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_MODULE) != 0) {
            if (declarations)
                match(indexes, "javaModuleDeclarations", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
            if (references)
                match(indexes, "javaModuleReferences", pattern,
                      SearchInConstantPoolsController::matchWithChar,
                      SearchInConstantPoolsController::matchWithPattern, matchingEntries,
                      caseInsensitive, useRegex, exactMatch);
        }

        if ((flags & SearchInConstantPoolsView.SEARCH_RESOURCES) != 0) {
            // Search non-class files (xml, properties, json, yaml, txt, etc.) in "strings" and "typeReferences" indices
            HashSet<Container.Entry> nonClassEntries = new HashSet<>();
            match(indexes, "strings", pattern,
                  SearchInConstantPoolsController::matchWithChar,
                  SearchInConstantPoolsController::matchWithPattern, nonClassEntries,
                  caseInsensitive, useRegex, exactMatch);
            match(indexes, "typeReferences", pattern,
                  SearchInConstantPoolsController::matchTypeEntriesWithChar,
                  SearchInConstantPoolsController::matchTypeEntriesWithPattern, nonClassEntries,
                  caseInsensitive, useRegex, exactMatch);

            for (Container.Entry entry : nonClassEntries) {
                String path = entry.getPath().toLowerCase();
                if (!path.endsWith(".class")) {
                    matchingEntries.add(entry);
                }
            }
        }

        // Apply file mask glob filter to all gathered entry paths
        Iterator<Container.Entry> iterator = matchingEntries.iterator();
        while (iterator.hasNext()) {
            Container.Entry entry = iterator.next();
            if (!matchFileMask(entry.getPath(), fileMask)) {
                iterator.remove();
            }
        }
    }

    public static boolean matchFileMask(String path, String mask) {
        if (mask == null || mask.trim().isEmpty() || mask.equals("*") || mask.equals("*.*")) {
            return true;
        }
        String[] parts = mask.split("[;,]");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            String regex = globToRegex(part);
            try {
                if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(path).matches()) {
                    return true;
                }
                // Also match against just the filename
                String filename = path;
                int idx = path.lastIndexOf('/');
                if (idx >= 0) {
                    filename = path.substring(idx + 1);
                }
                if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(filename).matches()) {
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    private static String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else if (c == '?') {
                sb.append(".");
            } else if ("\\.[]{}()+-^$|#".indexOf(c) >= 0) {
                sb.append("\\").append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append("$");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected void match(Indexes indexes, String indexName, String pattern,
                         BiFunction<Character, Map<String, Collection>, Map<String, Collection>> matchWithCharFunction,
                         BiFunction<Pattern, Map<String, Collection>, Map<String, Collection>> matchWithPatternFunction,
                         Set<Container.Entry> matchingEntries, boolean caseInsensitive, boolean useRegex, boolean exactMatch) {
        int patternLength = pattern.length();

        if (patternLength > 0) {
            // Build a cache key that includes mode flags before the pattern (so incremental lookup still works)
            String flagsKey = (caseInsensitive ? "i" : "") + (useRegex ? "r" : "") + (exactMatch ? "e" : "");
            String key = String.valueOf(indexes.hashCode()) + "***" + indexName + "***" + flagsKey + "***" + pattern;
            Map<String, Collection> matchedEntries = cache.get(key);

            if (matchedEntries == null) {
                Map<String, Collection> index = indexes.getIndex(indexName);

                if (index != null) {
                    // For single-char, non-regex, case-sensitive: use fast char filter
                    if (patternLength == 1 && !useRegex && !caseInsensitive && !exactMatch) {
                        matchedEntries = matchWithCharFunction.apply(pattern.charAt(0), index);
                    } else {
                        // Build the compiled pattern, reusing the previous search's result as a narrowing set
                        String lastKey = key.substring(0, key.length() - 1);
                        Map<String, Collection> lastMatchedTypes = cache.get(lastKey);
                        Pattern compiledPattern = createPattern(pattern, caseInsensitive, useRegex, exactMatch);
                        if (lastMatchedTypes != null) {
                            matchedEntries = matchWithPatternFunction.apply(compiledPattern, lastMatchedTypes);
                        } else {
                            matchedEntries = matchWithPatternFunction.apply(compiledPattern, index);
                        }
                    }
                }

                // Cache matchingEntries
                cache.put(key, matchedEntries);
            }

            if (matchedEntries != null) {
                for (Collection<Container.Entry> entries : matchedEntries.values()) {
                    matchingEntries.addAll(entries);
                }
            }
        }
    }

    protected static Map<String, Collection> matchTypeEntriesWithChar(char c, Map<String, Collection> index) {
        if ((c == '*') || (c == '?')) {
            return index;
        } else {
            Map<String, Collection> map = new HashMap<>();

            for (String typeName : index.keySet()) {
                int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
                int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
                int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

                char shortFirst = (lastIndex < typeName.length()) ? typeName.charAt(lastIndex) : '\0';
                char fullFirst = !typeName.isEmpty() ? typeName.charAt(0) : '\0';

                if (shortFirst == c || fullFirst == c) {
                    map.put(typeName, index.get(typeName));
                }
            }

            return map;
        }
    }

    protected static Map<String, Collection> matchTypeEntriesWithPattern(Pattern p, Map<String, Collection> index) {
        Map<String, Collection> map = new HashMap<>();

        for (String typeName : index.keySet()) {
            int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
            int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
            int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);

            String shortName = typeName.substring(lastIndex);
            String dotTypeName = typeName.replace('/', '.');

            if (p.matcher(shortName).find() || p.matcher(typeName).find() || p.matcher(dotTypeName).find()) {
                map.put(typeName, index.get(typeName));
            }
        }

        return map;
    }

    protected static Map<String, Collection> matchWithChar(char c, Map<String, Collection> index) {
        if ((c == '*') || (c == '?')) {
            return index;
        } else {
            Map<String, Collection> map = new HashMap<>();

            for (String key : index.keySet()) {
                if (!key.isEmpty() && (key.charAt(0) == c)) {
                    map.put(key, index.get(key));
                }
            }

            return map;
        }
    }

    protected static Map<String, Collection> matchWithPattern(Pattern p, Map<String, Collection> index) {
        Map<String, Collection> map = new HashMap<>();

        for (String key : index.keySet()) {
            if (p.matcher(key).find()) {
                map.put(key, index.get(key));
            }
        }

        return map;
    }

    /**
     * Create a regular expression from a search pattern.
     *
     * Rules (wildcard mode):
     *  '*'  matches 0 or N characters
     *  '?'  matches 1 character
     *
     * When {@code useRegex} is true, the pattern is used as-is as a Java regex.
     * When {@code caseInsensitive} is true, matching ignores case.
     */
    protected static Pattern createPattern(String pattern, boolean caseInsensitive, boolean useRegex, boolean exactMatch) {
        if (useRegex) {
            try {
                return Pattern.compile(pattern,
                        caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
            } catch (Exception e) {
                // Invalid regex — fall back to treating pattern as a literal string
                return Pattern.compile(Pattern.quote(pattern),
                        caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
            }
        }

        // Wildcard pattern mode
        int patternLength = pattern.length();
        StringBuilder sbPattern = new StringBuilder(patternLength * 2);

        if (exactMatch) {
            sbPattern.append("^");
        }

        for (int i = 0; i < patternLength; i++) {
            char c = pattern.charAt(i);

            if (c == '*') {
                sbPattern.append(".*");
            } else if (c == '?') {
                sbPattern.append('.');
            } else if (c == '.') {
                sbPattern.append("\\.");
            } else {
                sbPattern.append(Pattern.quote(String.valueOf(c)));
            }
        }

        if (exactMatch) {
            sbPattern.append("$");
        }

        return Pattern.compile(sbPattern.toString(),
                caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
    }

    protected static Pattern createPattern(String pattern, boolean caseInsensitive, boolean useRegex) {
        return createPattern(pattern, caseInsensitive, useRegex, false);
    }

    /**
     * Legacy overload retained for backward compatibility (case-sensitive wildcard).
     */
    protected static Pattern createPattern(String pattern) {
        return createPattern(pattern, false, false);
    }

    @SuppressWarnings("unchecked")
    protected void onTypeSelected(URI uri, String pattern, int flags) {
        // Save search history
        try {
            if (pattern != null && !pattern.trim().isEmpty()) {
                String p = pattern.trim();
                Map<String, String> prefs = api.getPreferences();
                
                // Save query
                String currentHistory = prefs.get("SearchInConstantPoolsView.recentSearches");
                List<String> list = new ArrayList<>();
                if (currentHistory != null && !currentHistory.isEmpty()) {
                    list.addAll(Arrays.asList(currentHistory.split("\\|\\|\\|")));
                }
                list.remove(p);
                list.add(0, p);
                while (list.size() > 15) {
                    list.remove(list.size() - 1);
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append("|||");
                    sb.append(list.get(i));
                }
                prefs.put("SearchInConstantPoolsView.recentSearches", sb.toString());
                searchInConstantPoolsView.populateSearchHistory(list, p);

                // Save file mask
                String mask = searchInConstantPoolsView.getFileMask();
                String currentMaskHistory = prefs.get("SearchInConstantPoolsView.recentFileMasks");
                List<String> maskList = new ArrayList<>();
                if (currentMaskHistory != null && !currentMaskHistory.isEmpty()) {
                    maskList.addAll(Arrays.asList(currentMaskHistory.split("\\|\\|\\|")));
                }
                maskList.remove(mask);
                maskList.add(0, mask);
                while (maskList.size() > 10) {
                    maskList.remove(maskList.size() - 1);
                }
                StringBuilder sbMask = new StringBuilder();
                for (int i = 0; i < maskList.size(); i++) {
                    if (i > 0) sbMask.append("|||");
                    sbMask.append(maskList.get(i));
                }
                prefs.put("SearchInConstantPoolsView.recentFileMasks", sbMask.toString());
                searchInConstantPoolsView.populateFileMaskHistory(maskList, mask);
            }
        } catch (Exception e) {
            // ignore
        }

        // Open the single entry uri
        Container.Entry entry = null;

        for (DelegatingFilterContainer container : delegatingFilterContainers) {
            entry = container.getEntry(uri);
            if (entry != null)
                break;
        }

        if (entry != null) {
            StringBuilder sbPattern = new StringBuilder(200 + pattern.length());

            sbPattern.append("highlightPattern=");
            sbPattern.append(pattern);
            sbPattern.append("&highlightFlags=");

            if ((flags & SearchInConstantPoolsView.SEARCH_DECLARATION) != 0)
                sbPattern.append('d');
            if ((flags & SearchInConstantPoolsView.SEARCH_REFERENCE) != 0)
                sbPattern.append('r');
            if ((flags & SearchInConstantPoolsView.SEARCH_TYPE) != 0)
                sbPattern.append('t');
            if ((flags & SearchInConstantPoolsView.SEARCH_CONSTRUCTOR) != 0)
                sbPattern.append('c');
            if ((flags & SearchInConstantPoolsView.SEARCH_METHOD) != 0)
                sbPattern.append('m');
            if ((flags & SearchInConstantPoolsView.SEARCH_FIELD) != 0)
                sbPattern.append('f');
            if ((flags & SearchInConstantPoolsView.SEARCH_STRING) != 0)
                sbPattern.append('s');
            if ((flags & SearchInConstantPoolsView.SEARCH_MODULE) != 0)
                sbPattern.append('M');

            // TODO In a future release, add 'highlightScope' to display search results in correct type and inner-type
            // def type = TypeFactoryService.instance.get(entry)?.make(api, entry, null)
            // if (type) {
            //     sbPattern.append('&highlightScope=')
            //     sbPattern.append(type.name)
            //
            //     def query = sbPattern.toString()
            //     def outerPath = UriUtil.getOuterPath(collectionOfFutureIndexes, entry, type)
            //
            //     openClosure(new URI(entry.uri.scheme, entry.uri.host, outerPath, query, null))
            // } else {
                String query = sbPattern.toString();
                URI u = entry.getUri();

                try {
                    openCallback.accept(new URI(u.getScheme(), u.getHost(), u.getPath(), query, null));
                } catch (URISyntaxException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            // }
        }
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        if (searchInConstantPoolsView.isVisible()) {
            // Update the list of containers
            this.collectionOfFutureIndexes = collectionOfFutureIndexes;
            // And refresh
            updateTree(searchInConstantPoolsView.getPattern(), searchInConstantPoolsView.getFlags());
        }
    }
}
