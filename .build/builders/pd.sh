#!/bin/bash
###############################
### PleasureDome Collection ###
###############################

MD_LINK_REG="^.*\[\(.*\)\].*(\(https:.*.zip\)).*$"
PD_LINE_REG="Datfile:.*\(https://.*.zip\)"

echo "*** building PleasureDome collection ***"
mkdir -p "$ROOT/PleasureDome"

## Latest Mame
# todo

## Reference Sets
PD_REF_INDEX="https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame-reference-sets/index.md"
PD_REF_ROOT="$ROOT/PleasureDome/Mame Reference Set"

echo "** fetching mame reference sets **"
mkdir -p "$PD_REF_ROOT"
pushd "$PD_REF_ROOT" >/dev/null

rm -rf "$PD_REF_ROOT"/*

for i in $(curl -s -L "$(PD_REF_INDEX)" | grep "$(PD_LINE_REG)" | sed -n "s/$(MD_LINK_REG)/name=\"\1\";file=\"\2\"/p")
do
	# analyse entry
	eval $i
	fname="$(basename $file)"
	echo "- found \"$name\""
	
	# get file
	encoded=$(echo "$file" | sed 's/ /%20/g')
	curl -s -L "$encoded" -o "$fname"
	
	# unzip content
	unar -q "$fname"
	
	# remove zip
	rm "$fname"
done
popd >/dev/null

## HBMame
# todo

## Fruit Machines
# todo
