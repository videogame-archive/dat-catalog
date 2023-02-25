# Public DAT Catalog

## Objective
This repository contains a snapshot of publicly available DAT files to be mainly used on rom managers.

## Directory 
```
latest : contains the latest snapshot of dats
    |- No-Intro
    |- Pleasuredome
    |- Redump
    |- TOSEC
    |- TOSEC-ISO
    |- TOSEC-PIX
```

Each project directory structure depends on the organization of the project.

Leaf nodes contain the dats, dats are stored in a directory with the dat name without any kind of qualifiers as they will be found in 

For example:
```
latest :
    |- Redump
        |- Sega Dreamcast
            |-Sega - Dreamcast - Datfile (1455) (2023-02-21 15-32-49).dat
```

## Navigation
Each directory contains a index.csv file, this file contains the list of files and directories.

It has two fields:
- Type : can be FILE or DIRECTORY.
- Name : Name of the file or directory.

For Example:
```
Type,Name
DIRECTORY,Acorn Archimedes
DIRECTORY,Apple Macintosh
DIRECTORY,Apple Macintosh - SBI Subchannels
DIRECTORY,Atari Jaguar CD Interactive Multimedia System
DIRECTORY,Bandai Pippin
```