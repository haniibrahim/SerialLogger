# SerialLogger

 Logs data from RS-232 interface to a GUI, console and optionally to a file
 
## Features

* Cross-platform: Windows (Intel), Mac (Intel), GNU/Linux (Intel/ARM), 32- and 64-bit architectures
* Graphical User Interface (GUI)
* Get serial port names from the OS
* Logs serial data to screen (GUI), console and optionally in a file
* Save every collected line of data immediately to the file avoiding data loss
* Serial port name is editable in the GUI (for pseudo ports as `/dev/pts/[x]`)
* Arbitrary baud rate (if hardware supports it)

## Screenshot
![SerialLogger Screenshot (Windows)](http://blog.hani-ibrahim.de/wp-content/uploads/seriallogger.png "")

SerialLogger on Windows 10

## Requirements

* PC with MS-Windows XP or higher
* PC with GNU/Linux on x86/x86_64 or ARM (32/64-bit))
* Macintosh with Mac OS X 10.5 or higher
* Java 6 JRE on platforms mentioned above (Oracle Java or OpenJDK)

## Dependencies

* [JSerialComm](http://fazecast.github.io/jSerialComm/ "") library
* [SwingLayoutExtensions](http://www.java2s.com/Code/JarDownload/swing/swing-layout.jar.zip "") library
* [AppleJavaExtensions](http://www.java2s.com/Code/Jar/a/applejavaextensions.htm "") library for platforms other than Mac OS X

Build in Netbeans. 

## Status

* ***IMPORTANT: Still not finally released!*** Only preview JAR released yet
* Branch master is always compile- and runnable
* Works without any problems on Windows currently
* GNU/Linux has a small problem, macOS a bigger one, see [Issues](https://github.com/haniibrahim/SerialLogger/issues "")

## To-Do

* Get rid of the Mac OS X and GNU/Linux bug
* Configurable flow control parameters (CTS, RTS/CTS, DSR, DTR/DSR, XOn/XOff)
* Command-line interface
* Time-stamp feature

## Known bugs

See [Issues](https://github.com/haniibrahim/SerialLogger/issues "").

## License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/ "").
