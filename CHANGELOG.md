# Changelog

All notable changes to **JD-GUI Enhanced Edition** will be documented in this file.

## [Unreleased] - 2026

### Added
- **Exact Match Search:** Added an "Exact Match" checkbox to restrict search results to exact identifiers or words, preventing substring false positives.
- **File Mask Filtering:** Added an editable file mask dropdown (e.g., `*.xml`, `*.properties`, `*Controller.class`) to limit the scope of searches to specific file patterns using globs.
- **Explicit Resources Support:** Added a "Resources" checkbox to explicitly toggle searching inside non-class files (`.xml`, `.properties`, etc.) simultaneously or independently from class files.
- **Persistent Search History:** Upgraded the search input and file mask input to editable dropdowns that automatically save and remember recent queries across application restarts.

### Fixed
- **Search Result Navigation Bug:** Fixed a critical bug where double-clicking a search result would cause the search tree to spontaneously clear, collapse, and reset the highlighter to the first file. This was due to spurious document events firing during background UI population.
- **Regex Interaction:** Ensured Regex search mode interacts correctly with the new Exact Match mode, falling back gracefully if invalid regex is supplied.

### Changed
- **Branding:** Renamed the application to "JD-GUI Enhanced Edition" while preserving original attribution to Emmanuel Dupuy and the GPL v3.0 license.
