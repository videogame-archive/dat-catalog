#!/bin/bash
###################
### DAT CATALOG ###
###################

## 0 : global vars
DIR="$(dirname -- "$(readlink -f -- "$0")")"
ROOT="$DIR/root"
BUILD="$DIR/.build"

## 1 : check requirements
pushd "$BUILD/checkers" >/dev/null
IFS=$'\n'
for f in *.sh; do . "$f"; done
popd >/dev/null

## 2 : load helpers
pushd "$BUILD/helpers" >/dev/null
IFS=$'\n'
for f in *.sh; do . "$f"; done
popd >/dev/null

## 3 : build collections 
pushd "$BUILD/builders" >/dev/null
IFS=$'\n'
for f in *.sh; do . "$f"; done
popd >/dev/null

## 4 : build indexes
white "\n*** build indexes ***"
index_scan "$DIR/root"
DONE


