# SerialLogger

 Fail-proof and easy to use cross-platform serial datalogger. 
 Logs data from RS-232 interface to a GUI, console and optionally to a file.

## Features

* Cross-platform: 
  * Windows XP and later (32/64-bit x86, ARM and ARM64)
  * Mac OS X Tiger (10.4) and later (32/64-bit x86 and Apple Silicon ARM64)
  * All Linux distributions (32/64-bit x86, ARM, and PowerPC)
  * Solaris 10 and later (32/64-bit x86 and SPARC)
  * FreeBSD (32/64-bit x86 and ARM64)
  * OpenBSD (32/64-bit x86)
  * ARM/x86 Mobile Linux derivatives (e.g. RaspberryPi, Beaglebone, etc.)
* Run and compile on Java 8 or higher ( Java ≥ 11 recommended)
* Logs serial data to screen (GUI), console and optionally in a file
* **Fail-proof data collection.** Save every collected line of data immediately to the optional log file avoiding data loss even at power breakdowns or computer/application crash.
* Warning of unintentional overwriting or deleting of unlogged buffer
* Impossible to overwrite log file from application
* Graphical User Interface (GUI)
* Get serial port names from the OS
* Serial port name is editable in the GUI (for pseudo ports as `/dev/pts/[x]`)
* Arbitrary baud rate (if hardware supports it)
* Hardware (CTS/RTS) and Software (Xon/Xoff) handshake available
* Timestamp function (ISO 8601, MJD, ...)
* Apple Silicon ARM support

## Screenshot

![](http://blog.hani-ibrahim.de/wp-content/uploads/SerialLogger-1.1.0.png "Screenshot")

SerialLogger v1.1.0 on Windows 10

## Requirements

for the binaries provided in [RELEASE](https://github.com/haniibrahim/SerialLogger/releases/):

* PC with MS-Windows 7 or higher on x86/x86_64
* PC with GNU/Linux on x86/x86_64 or ARM (32/64-bit)
* Macintosh with macOS 10.11 (El Captain) or higher, x86_64 or ARM64
* Java ≥ 11, if not included in the installer (Oracle Java or OpenJDK)

## Dependencies

* [jSerialComm](http://fazecast.github.io/jSerialComm/ "") library, version 2.1.0 or higher (included in binary distributions)

Build in Netbeans with `ant`.

## Binaries

For JARs and platform related installers go to the [RELEASE](https://github.com/haniibrahim/SerialLogger/releases) section.

## Wiki

For some important additional information, visit the [Wiki](https://github.com/haniibrahim/SerialLogger/wiki).

## Changelog

| Version     | Notes                                                                                                                                                                                          |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0.9.0-alpha | Basic functions, works just in Windows                                                                                                                                                         |
| 0.9.0       | Basic logging functions and serial settings, fail-proof log file feature, full cross-platform functionality (Windows, macOS, GNU/Linux)                                                        |
| 1.0.0-beta  | Hardware (CTS/RTS) and Software (Xon/Xoff) flowcontrol option, append data to existing file feature, invalid path bug fixed, warning of overwriting unsaved/unlogged buffer at new connections |
| 1.0.0       | Check & warn for unsaved/unlogged puffer at app closing, empty commport bug fixed, flowcontrol bug fixed, filedialog bug fixed on macOS & Windows, code clean-up                               |
| 1.1.0-alpha | Timestamp feature (see Notes.md for details)                                                                                                                                                   |
| 1.1.0-beta  | ISO 8601 timezone bug fixed. All other timezone strings now ISO 8601 compatible. Delimiter combobox bug fixed                                                                                  |
| 1.1.0       | Timestamp function, Look and Feel function                                                                                                                                                     |
| 1.2.0       | Save buffer feature, Timezone-DST bug fixed, latest jSerialComm library v 2.3.0                                                                                                                |
| 1.2.0b      | latest jSerialComm library v 2.6.2, Apple M1 ARM support                                                                                                                                       |
| 1.2.1       | Upgrade for newer platform and Java versions incl. better HDPI-support                                                                                                                         |
| 1.2.2       | Multi-screen position bug fixed, Update-URL in InfoDialog                                                                                                                                      |
| 1.2.3       | Close port bug fixed                                                                                                                                                                           |

## Known bugs

Report bugs on [Issues](https://github.com/haniibrahim/SerialLogger/issues "")

## License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/ "").
