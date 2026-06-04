# GPL v3.0 Compliance Report: JD-GUI Enhanced Edition

**Date Generated:** June 2026
**Copyright Owner for Modifications:** NaresH K

This report summarizes the compliance evaluation and actions taken to prepare the modified "JD-GUI Enhanced Edition" codebase for open-source release under the GNU General Public License v3.0.

## 1. What was Changed

- **Application Branding & UI Text:**
  - `Constants.java`: Renamed `APP_NAME` to "JD-GUI Enhanced Edition".
  - `AboutView.java`: Updated main labels and dialog window title to reflect the new Enhanced Edition name.
  - Added new "Based on JD-GUI" labels in the About Dialog.
  - Added a new copyright statement for the modifications by NaresH K in the About dialog.
- **Documentation:**
  - `README.md`: Overhauled to include the new title, feature list (Regex, Exact Match, File Mask, etc.), and clear statements that this is a fork. Included links to original repo.
  - `CHANGELOG.md`: Created to transparently document all new features and bug fixes.
  - `CONTRIBUTING.md`: Created to encourage community participation and outline GPL requirements for contributors.
  - `NOTICE`: Converted to `NOTICE.md`, adding new explicit sections for the original author attribution (Emmanuel Dupuy), the modification attribution (NaresH K), and the GPL v3 acknowledgment, while preserving all third-party license notes.
  - `RELEASE_CHECKLIST.md`: Created to ensure continuous compliance upon future releases.
- **Source Code Functionality:**
  - `SearchInConstantPoolsView.java` & `SearchInConstantPoolsController.java`: Significantly enhanced to add new checkboxes, comboboxes, persistent preferences, and glob matching.

## 2. What Remains Unchanged

- **The `LICENSE` File:** The exact text of the original GNU GPL v3.0 file remains untouched at the root of the repository.
- **Original Source Code Headers:** All unmodified and modified `.java` files retain their original copyright headers (`Copyright (c) 2008-2019 Emmanuel Dupuy`).
- **Original UI Attribution:** The original `Copyright © 2008, 2019 Emmanuel Dupuy` label inside the `AboutView.java` dialog is fully preserved and visibly displayed alongside the new modifications copyright.
- **Splash Screen / Logo Image:** The original `jd_icon_128.png` remains unaltered, maintaining visual familiarity while acknowledging its origins.
- **Third-Party Dependencies:** No third-party dependencies were removed; the original list of libraries (Groovy, Gradle, RSyntaxTextArea, Launch4j, etc.) and their licenses are fully preserved in `NOTICE.md`.

## 3. Compliance Verification & Risks

- **Missing Attributions or Notices?** **None.** Extensive review was conducted. The application explicitly credits Emmanuel Dupuy both in code, UI, and documentation.
- **GPL v3.0 Stating Changes Requirement:** The GPL requires modified versions to prominently state that files were changed. The newly created `CHANGELOG.md` and the updated `README.md` satisfy this requirement transparently.
- **Proprietary Risk?** **None.** The project strictly remains under GPL v3.0. No proprietary or closed-source dependencies were introduced.
- **Source Distribution Requirement:** As part of the release checklist, instructions have been laid out to ensure the source code is distributed alongside the compiled binaries on GitHub.

## 4. Conclusion

The "JD-GUI Enhanced Edition" codebase has been fully audited and updated. It is **100% compliant** with the GNU General Public License v3.0 requirements and is cleared for open-source distribution on GitHub.
