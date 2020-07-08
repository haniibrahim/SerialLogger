# Notes for SerialLogger

## Handshake (Flow Control)
For flow control users can choose from
- Software flow control via *XON/XOFF*
- Hardware flow control via *RTS/CTS*
- No flow control via *none*

## Timestamp function
SerialLogger offers since v1.1.0 a timestamp function which add a timestamp in front of every data line. The user can choose from a lot of predefined formats. Furthermore the user can choose from a couple delimiter signs which separate the timestamp components and the data from each other. This makes it easier to import the data to post-processing applications.

### Delimiter signs
The delimiter sign is added between timestamp components (if necessary) and at the end of the timestamp just before the data
- blank (*space character*)
- comma (,)
- semicolon (;)

### Timestamp formats
- *ISO 8601:* 
  Format: `2018-09-06T13:45:42+02:00` => 1:45:42pm at September 6th, 2018 CEST (Central European Summer Time), To parse ISO 8601-format in Microsoft Excel or LibreOffice Calc refer [ISO 8601 Parsing in Excel and Calc](http://blog.hani-ibrahim.de/iso-8601-parsing-in-excel-and-calc.html "")
- *Date|Time|Timezone:*
  Format: `06.09.2016,13:45:42,+02:00` => 1:45:42pm at September 6th, 2018 CEST, comma is the delimiter
- *Date|Time:*
  Format: `06.09.2016,13:45:42` => 1:45:42pm at September 6th, 2018, comma is the delimiter
- *Time:*
   Format: `13:45:42` => 1:45:42pm
- *Mod. Julian Day:* The Modified Julian Day (MJD) is an abbreviated version of the old Julian Day (JD) dating method 
  which has been in use for centuries by astronomers, geophysicists, chronologers, and others who needed to have an 
  unambiguous dating system based on continuing day counts. MJD counts days from November 17, 1858 onwards and starts, 
  in comparison to the JD, at midnight. Time is displayed as a fraction of a day. It is easy to calculate time differences.
  Format: `58393.431814675925` => 10:21:48am at October 2nd, 2018 CEST
- *Year|Day of year|Time:* The Day of the year is the counted day from Jan. 1st = 1 onwards till 365 or 366 resp. This 
  Format: `2018 275 10:21:48` => 10:21:48am at October 2nd, 2018, [blank] is the delimiter
- *Year|month|day|hours|minutes|seconds:* This timestamp offers all standard date and time components in
  sepratate fields for easier post-processing handling
  Format: `2018 10 2 10 21 48` => 10:21:48am at October 2nd, 2018, [blank] is the delimiter

## Settings storage
*WINDOWS:* Settings are stored in the regitry (regedit.exe) in the key:
```
Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\de\haniibrahim\seriallogger
```

*UNIX, GNU/Linux:* Settings are stored in user's home directory in the file:
```
~/.java/.userPrefs/de/haniibrahim/seriallogger/prefs.xml
```

*macOS:* Settings are stored in user's library directory in the file:
```
~/Library/Preferences/de.haniibrahim.seriallogger.plist
```

## Bugs
Please report questions, enhancements and bugs to [https://github.com/haniibrahim/SerialLogger/issues](https://github.com/haniibrahim/SerialLogger/issues).