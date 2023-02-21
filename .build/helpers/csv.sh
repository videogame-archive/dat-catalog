#!/bin/bash

index_header() {
	echo "type,name,dl_url,feed,crc,size" > .index.csv
}

index_scan() {
	if [[ -f "$1" ]]; then
		echo -n "FILE,\"$1\",,," >> .index.csv
		echo $(cksum "$1" | cut -d ' ' -f 1-2 --output-delimiter=',') >> .index.csv
	else
		echo "DIR,\"$1\",,,," >> .index.csv
		pushd "$1" >/dev/null
		index_header
		for e in $(ls -1); do
			index_scan "$e"
		done
		popd >/dev/null
	fi
}