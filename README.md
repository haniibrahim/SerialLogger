# SerialLogger

 Fail-proof and easy to use cross-platform serial datalogger. 
 Logs data from RS-232 interface to a GUI, console and optionally to a file.
 
## Features

* Cross-platform: Windows (Intel), macOS (Intel), GNU/Linux (Intel/ARM), 32- and 64-bit architectures
* Fail-proof data collection. Save every collected line of data immediately to the optional log file avoiding data loss even at power breakdowns
* Graphical User Interface (GUI)
* Get serial port names from the OS
* Logs serial data to screen (GUI), console and optionally in a file
* Serial port name is editable in the GUI (for pseudo ports as `/dev/pts/[x]`)
* Arbitrary baud rate (if hardware supports it)
* Hardware (CTS/RTS) and Software (Xon/Xoff) handshake available

## Screenshot
![SerialLogger Screenshot (Windows)](http://blog.hani-ibrahim.de/wp-content/uploads/seriallogger.png "")

SerialLogger v0.9.0 on Windows 10

## Requirements

* PC with MS-Windows XP or higher
* PC with GNU/Linux on x86/x86_64 or ARM (32/64-bit)
* Macintosh with macOS 10.5 (Tiger) or higher
* Java 6 JRE on platforms mentioned above (Oracle Java or OpenJDK)

## Dependencies

* [jSerialComm](http://fazecast.github.io/jSerialComm/ "") library, version 2.1.0 or higher
* [AppleJavaExtensions](http://www.java2s.com/Code/Jar/a/applejavaextensions.htm "") library for platforms other than macOS

Build in Netbeans. 

## Binary

For JARs and installers go to the [RELEASE section](https://github.com/haniibrahim/SerialLogger/releases).

## Changelog

| Version | Notes |
|-------|--------|
| 0.9.0 | Basic logging functions and serial settings, fail-proof log file feature, full cross-platform functionality|
| 1.0.0 | Hardware (CTS/RTS) and Software (Xon/Xoff) handshake option, append data to exixting file feature |

## Roadmap

* Time-stamp feature (v1.1.0)
* Command line features (v2.0.0)

## Known bugs

Report bugs on [Issues](https://github.com/haniibrahim/SerialLogger/issues "")

## License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/ "").
