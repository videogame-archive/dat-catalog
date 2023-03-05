#!/bin/bash

index_header() {
	echo "type,name,dl_url,feed,crc,size" > .index.csv
}

index_scan() {
	baseurl="https://github.com/open-retrogaming-archive/dat-catalog/raw/main/root"
	root="$1"
	current="$(pwd)"
	relative=${current#"$root"}
	if [ -z "$2" ]; then
		path="$1";
		local currenturl="${baseurl}"
	else
		path="$2";
		local currenturl="${3}/$(rawurlencode $2)"
	fi
	
	if [[ -f "$path" ]]; then
		if [ -f .index.csv ]; then 
			echo -n "FILE,\"$path\",\"$currenturl\",," >> .index.csv
			echo $(cksum "$path" | cut -d ' ' -f 1-2 --output-delimiter=',') >> .index.csv
		fi
	else
		if [ -f .index.csv ]; then
			echo "DIR,\"$path\",,,," >> .index.csv
		fi
		pushd "$path" >/dev/null
		index_header
		for e in $(ls -1); do
			index_scan "$root" "$e" "$currenturl"
		done
		popd >/dev/null
	fi
}