# Public DAT Catalog

## Objective
This repository contains a snapshot of publicly available DAT files to be mainly used on rom managers.

The idea behind this structure is that it can be replicated to keep local collections and rom managers can provide reports and fixes based on dats found on each directory without requiring manual configurations.

## Directory 
```
build : java project used to build the latest root directory.
root/normalized : contains the latest snapshot of dats.
        |- No-Intro
        |- Pleasuredome
        |- Redump
        |- TOSEC
        |- TOSEC-ISO
        |- TOSEC-PIX
```

Each project directory structure depends on the organization of the project.

Leaf nodes contain the dats, dats are stored in a directory with the dat name without any kind of qualifiers as they will be found in collections.

For example:
```
normalized :
    |- Redump
        |- Sega Dreamcast
            |-Sega - Dreamcast - Datfile (1455) (2023-02-21 15-32-49).dat
```

## Navigation
The root folder of each project contains:
- A modified file, this file indicates when it was last updated.
- An index.csv file, this file contains the list of files/directories.

The index.csv file contains the next fields:
- Type : can be FILE or DIRECTORY
- Name : Name of the FILE or directory
- URL : URL to download the FILE
- CRC : CRC of the FILE
- Size : Size of the FILE

For Example:
```
Type,Name,URL,CRC,Size
DIRECTORY,Bandai Playdia Quick Interactive System,,,
FILE,Bandai - Playdia Quick Interactive System - Datfile (38) (2022-07-06 01-08-10).dat,https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/root/normalized/Redump/Bandai Playdia Quick Interactive System/Bandai - Playdia Quick Interactive System - Datfile (38) (2022-07-06 01-08-10).dat,1688677462,30295
DIRECTORY,Sega Lindbergh,,,
FILE,Arcade - Sega - Lindbergh - Datfile (12) (2022-09-27 15-27-52).dat,https://raw.githubusercontent.com/open-retrogaming-archive/dat-catalog/main/root/normalized/Redump/Sega Lindbergh/Arcade - Sega - Lindbergh - Datfile (12) (2022-09-27 15-27-52).dat,2087931147,4505
```