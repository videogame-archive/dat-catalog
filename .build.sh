#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

pushd "$DIR/.build" >/dev/null

IFS=$'\n'
for f in *.sh
do
	echo "Processing $f file..."
	. "$f"
done

popd >/dev/null
