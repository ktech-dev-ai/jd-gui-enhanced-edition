# Release Checklist for JD-GUI Enhanced Edition

Before publishing a new release to GitHub, ensure the following checklist is completed to maintain GPLv3 compliance and project integrity.

## 1. Documentation Verification
- [ ] `README.md` is up-to-date with current features, build instructions, and correctly identifies the project as "Based on JD-GUI".
- [ ] `CHANGELOG.md` reflects all new enhancements and bug fixes for the current release.
- [ ] `NOTICE.md` accurately lists all third-party libraries and their licenses.
- [ ] `NOTICE.md` contains the original copyright statement (Emmanuel Dupuy).
- [ ] `NOTICE.md` contains the modification copyright statement (NaresH K).
- [ ] `CONTRIBUTING.md` is present.
- [ ] `LICENSE` (GPLv3) is present and completely unmodified from the original repository.

## 2. Codebase Verification
- [ ] Original copyright headers in `.java` source files (e.g., `Copyright (c) 2008-2019 Emmanuel Dupuy`) are preserved and unmodified.
- [ ] Application window title (`Constants.java`) reflects "JD-GUI Enhanced Edition".
- [ ] `About` dialog (`AboutView.java`) reflects the new application title while preserving original attribution and adding modification attribution.

## 3. Build & Testing
- [ ] `./gradlew clean build` completes successfully.
- [ ] `./gradlew createExe proguard` completes successfully.
- [ ] Manual verification of search enhancements (Exact Match, File Mask, Resources toggle, History persistence).
- [ ] Verify that double-clicking search results functions correctly without collapsing the tree.

## 4. GitHub Release Publishing
- [ ] Create a new GitHub Release drafting a clear release note (copying from `CHANGELOG.md`).
- [ ] **Source Release Requirement:** The GPLv3 requires that complete source code is provided. GitHub automatically provides `Source code (zip)` and `Source code (tar.gz)` assets with every release. Ensure these are present.
- [ ] **Binary Distribution Requirement:** Attach the newly built binaries from `build/distributions/` and `build/libs/` to the GitHub Release.
  - `jd-gui-windows-x.y.z.zip` (or `.exe`)
  - `jd-gui-osx-x.y.z.tar`
  - `jd-gui-x.y.z.jar`
  - `jd-gui-x.y.z.deb` (optional)
  - `jd-gui-x.y.z.rpm` (optional)
