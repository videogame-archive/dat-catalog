#!/bin/bash
###############################
### PleasureDome Collection ###
###############################

PD_LINE_REG="Datfile.*:.*\(https://.*.zip\)"
MD_LINK_REG="^.*\[\(.*\)\].*(\(https:.*.zip\)).*$"
MD_LINK_SED_REPL="name=\"\1\";file=\"\2\""
PD_DIR="$ROOT/PleasureDome"

white "\n*** Building PleasureDome collection ***\n"
mkdir -p "$PD_DIR"

pd_fetch() {
	bold_yellow "\n** Fetching $1 **\n"
	PD_REF_ROOT="${PD_DIR}/$1"
	PD_REF_INDEX="$2"
	
	mkdir -p "$PD_REF_ROOT"
	pushd "$PD_REF_ROOT" >/dev/null
	
	if [ "$(get_current_date)" -gt "$(get_modified_date)" ]
	then
		files=$(curl -s -L "${PD_REF_INDEX}" | grep "${PD_LINE_REG}" | sed -n "s/${MD_LINK_REG}/${MD_LINK_SED_REPL}/p")
		if [ $? -eq 0 ]
		then
			rm -rf "$PD_REF_ROOT"/*
			for i in $files
			do
				# analyse entry
				eval $i
				fname="$(basename $file)"
				dot_green
				echo -n " \"$name\""
				
				# get file
				encoded=$(echo "$file" | sed 's/ /%20/g')
				curl -s -L "$encoded" -o "$fname"
				
				# unzip content
				entry=$(zipinfo -1 "$fname" | head -n 1)
				if [ "${fname%.*}" = "${entry%.*}" ]; then
					unar -q "$fname"
				else
					ffname="${fname%.*}"
					if [ "${ffname%.*}" = "${entry%.*}" ]; then
						unar -q "$fname"
					else
						unar -q -d "$fname"
					fi
				fi
				
				# remove zip
				rm "$fname"
	
				DONE
			done
			write_modified
		else
			dot_red
			echo -n " Could not fetch $1"
			BAD
		fi
	else
		dot_yellow
		echo -n " Already downloaded $1 today"
		MISS
	fi
	popd >/dev/null
}



## Latest Mame
pd_fetch "MAME" "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame/index.md"

## Reference Sets
pd_fetch "MAME Reference Sets" "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/mame-reference-sets/index.md"

## HBMame
pd_fetch "HBMAME" "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/hbmame/index.md"

## Fruit Machines
pd_fetch "Fruit Machines" "https://raw.githubusercontent.com/pleasuredome/pleasuredome/gh-pages/fruitmachines/index.md"

