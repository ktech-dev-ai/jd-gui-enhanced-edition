# JD-GUI Enhanced Edition

**JD-GUI Enhanced Edition** is a modified fork of the original JD-GUI, a standalone graphical utility that displays Java sources from CLASS files.

This project is based on the original **JD-GUI** by Emmanuel Dupuy.
- Original Java Decompiler projects home page: [http://java-decompiler.github.io](http://java-decompiler.github.io)
- Original JD-GUI source code: [https://github.com/java-decompiler/jd-gui](https://github.com/java-decompiler/jd-gui)

![](https://raw.githubusercontent.com/java-decompiler/jd-gui/master/src/website/img/jd-gui.png)

## Description
JD-GUI Enhanced Edition builds upon the robust foundation of the original JD-GUI by introducing advanced search capabilities and numerous quality-of-life improvements. You can browse the reconstructed source code with instant access to methods and fields, and now perform highly targeted, complex searches across large codebases with ease.

### Enhancements in this Edition:
- **Advanced Multi-File Search:** Search across `.class` files and resources simultaneously or independently.
- **Explicit Resources Support:** Dedicated toggle to search non-class files (`.xml`, `.properties`, `.json`, etc.).
- **Regex & Exact Match:** Support for complex Regular Expressions and an "Exact Match" mode to eliminate substring false-positives.
- **File Masking & Scoping:** Filter search results using glob patterns (e.g., `*Controller.class`, `*.xml`) with a convenient dropdown of presets.
- **Persistent Search History:** Your past search patterns and file masks are automatically saved and remembered across application restarts.
- **UI & Bug Fixes:** Resolved tree-collapsing and highlighter resetting bugs during search result navigation.

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
