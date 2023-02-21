#!/bin/bash

echo "*** building PleasureDome collection ***"
mkdir -p "$DIR/PleasureDome"

echo "* fetching mame reference sets *"
mkdir -p "$DIR/PleasureDome/Mame Reference Set"
pushd "$DIR/PleasureDome/Mame Reference Set" >/dev/null

for i in $(curl -s https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame-reference-sets/index.md | grep "Datfile:.*\(https://.*.zip\)" | sed -n "s/^.*\[\(.*\)\].*(\(https:.*.zip\)).*$/name=\"\1\";file=\"\2\"/p")
do
	eval $i
	echo "$file"
	echo $(echo "$file" | sed 's/ /%20/g')
	curl -L $( echo "$file" | sed 's/ /%20/g' ) -o "$(basename $file)"
	unzip -qqo "$(basename $file)"
	rm "$(basename $file)"
done

popd >/dev/null
