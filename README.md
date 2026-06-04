# JD-GUI Enhanced Edition

**JD-GUI Enhanced Edition** is a modified fork of the original JD-GUI, a standalone graphical utility that displays Java sources from CLASS files.

This project is based on the original **JD-GUI** by Emmanuel Dupuy.
- Original Java Decompiler projects home page: [http://java-decompiler.github.io](http://java-decompiler.github.io)
- Original JD-GUI source code: [https://github.com/java-decompiler/jd-gui](https://github.com/java-decompiler/jd-gui)

![](https://raw.githubusercontent.com/java-decompiler/jd-gui/master/src/website/img/jd-gui.png)

## Description
JD-GUI Enhanced Edition builds upon the robust foundation of the original JD-GUI by introducing advanced search capabilities and numerous quality-of-life improvements. You can browse the reconstructed source code with instant access to methods and fields, and now perform highly targeted, complex searches across large codebases with ease.

### The Evolution of JD-GUI: Before & After

| Feature Area | Original JD-GUI (v1.6.6) | JD-GUI Enhanced Edition (v2.0.0) |
| :--- | :--- | :--- |
| **Search Capabilities** | Basic, case-insensitive string search within `.class` files. | **Advanced Multi-File Search** across `.class` and **Resource files** (`.xml`, `.properties`, etc.). |
| **Match Precision** | Results cluttered with substring false-positives. | **Exact Match** & **Regular Expressions** to pinpoint exactly what you need. |
| **Scoping & Filtering** | Unable to filter searches by file name or type. | **File Mask Scoping** using powerful glob patterns (e.g., `*Controller.class`). |
| **User Experience** | Loses search history upon application restart. | **Persistent History** automatically remembers search patterns and masks. |
| **Stability** | Search results tree collapses unexpectedly; highlights reset. | **UI Bug Fixes** resolve tree-collapsing and highlighter state bugs. |

## Download
You can download the latest pre-compiled binaries from our official landing page or directly from GitHub Releases:
- 🌐 **[Official Website](https://ktech-dev-ai.github.io/jd-gui-enhanced-edition/)**
- 🪟 **[Windows .exe](https://github.com/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest/download/jd-gui-enhanced-edition.exe)**
- 🍎 **[macOS .tar](https://github.com/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest/download/jd-gui-osx-2.0.0.tar)**
- ☕ **[Universal .jar](https://github.com/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest/download/jd-gui-2.0.0.jar)**
## How to build JD-GUI Enhanced Edition ?
```
> git clone [YOUR_GITHUB_REPO_URL]
> cd jd-gui
> ./gradlew build 
```
generate :
- _"build/libs/jd-gui-x.y.z.jar"_
- _"build/libs/jd-gui-x.y.z-min.jar"_
- _"build/distributions/jd-gui-windows-x.y.z.zip"_
- _"build/distributions/jd-gui-osx-x.y.z.tar"_
- _"build/distributions/jd-gui-x.y.z.deb"_
- _"build/distributions/jd-gui-x.y.z.rpm"_

## How to launch JD-GUI Enhanced Edition ?
- Double-click on _"jd-gui-x.y.z.jar"_
- Double-click on _"jd-gui.exe"_ application from Windows
- Double-click on _"JD-GUI"_ application from Mac OSX
- Execute _"java -jar jd-gui-x.y.z.jar"_ or _"java -classpath jd-gui-x.y.z.jar org.jd.gui.App"_

## How to use JD-GUI ?
- Open a file with menu "File > Open File..."
- Open recent files with menu "File > Recent Files"
- Drag and drop files from your file explorer
- Access the advanced search menu via `Search -> Search...` or `Ctrl+Shift+S`.

## How to extend JD-GUI ?
```
> ./gradlew idea 
```
generate Idea Intellij project
```
> ./gradlew eclipse
```
generate Eclipse project
```
> java -classpath jd-gui-x.y.z.jar;myextension1.jar;myextension2.jar org.jd.gui.App
```
launch JD-GUI with your extensions

## How to uninstall JD-GUI ?
- Java: Delete "jd-gui-x.y.z.jar" and "jd-gui.cfg".
- Mac OSX: Drag and drop "JD-GUI" application into the trash.
- Windows: Delete "jd-gui.exe" and "jd-gui.cfg".

## License
Released under the [GNU GPL v3](LICENSE).
JD-GUI and JD-Core are originally authored by Emmanuel Dupuy.
Modifications and enhancements in this fork are Copyright © 2026 NaresH K.

## Donations
If the original JD-GUI helped you, consider making a donation to the original author:
[![paypal](https://raw.githubusercontent.com/java-decompiler/jd-gui/master/src/website/img/btn_donate_euro.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=C88ZMVZ78RF22) [![paypal](https://raw.githubusercontent.com/java-decompiler/jd-gui/master/src/website/img/btn_donate_usd.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CRMXT4Y4QLQGU)
