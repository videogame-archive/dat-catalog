#!/bin/bash
###############################
### No-Intro Collection ###
###############################

NI_DIR="$ROOT/No-Intro"
NI_URL="https://datomatic.no-intro.org/"

# tell datomatic to download a specific archive type
NI_PAGE="index.php?page=download&op=daily&s=64"

# tell what to include in the archive
NI_FORM="dat_type=standard&set1=Ok&valentine_day=Request"

NI_COOKIE="/tmp/ni_cookie.txt"
NI_ARCHIVE="/tmp/ni_archive.zip"

white "\n*** Building No-Intro collection ***\n"
mkdir -p "$NI_DIR"

pushd "$NI_DIR" >/dev/null

if [ "$(get_current_date)" -gt "$(get_modified_date)" ]
then
	dot_green
	echo -n " Fetching global archive..."
	location=$(curl -s -i -H "Content-Type: application/x-www-form-urlencoded" -d "${NI_FORM}" -c "${NI_COOKIE}" "${NI_URL}${NI_PAGE}" | grep -oP 'location: \K.*' | tr -d '[:space:]')
	curl -s -S -H "Content-Type: application/x-www-form-urlencoded" -d "lazy_mode=Download" -b "${NI_COOKIE}" -o "${NI_ARCHIVE}" "${NI_URL}${location}"
	rm "${NI_COOKIE}"
	
	if [ -f "${NI_ARCHIVE}" ];
	then
		DONE
		zipinfo -t "${NI_ARCHIVE}" >/dev/null 2>&1
		if [ $? -eq 0 ];
		then
			dot_green
			echo -n " Unzipping global archive..."
			rm -rf ./*
			unzip -q "${NI_ARCHIVE}"
			rm *.txt
			rm "${NI_ARCHIVE}"
			write_modified
			DONE
		else
			dot_red
			echo -n " failed to unzip archive... please look at ${NI_ARCHIVE} content}"
			BAD
		fi
	else
		BAD
	fi
else
	dot_yellow
	echo -n " Already downloaded today"
	MISS
fi

dot_green
echo -n " Rebuilding latest dirs/links..."
for dir in *
do
	pushd "$dir" >/dev/null
	rm -rf "latest"
	for entry in *.dat
	do
		name=
		brand=
		model=
		eval $(echo "$entry" | sed -n "s/^\(.*\) - \([^-]*\) - \(.*\) (.*).dat$/name=\"\1\";brand=\"\2\";model=\"\3\"/p")
		if [ -z "$name" ]; then
			eval $(echo "$entry" | sed -n "s/^\(.*\) - \(.*\) (.*).dat$/brand=\"\1\";model=\"\2\"/p")
		fi
		if [ "$dir" = "$brand" ]; then
			mkdir -p "latest"
			pushd "latest" >/dev/null
			ln -s "../$(basename "$entry")" "${model}.dat"
			popd >/dev/null
		elif [ -z "$name" -o "$dir" = "$name" ]; then
			mkdir -p "latest/${brand}"
			pushd "latest/${brand}" >/dev/null
			ln -s "../../$(basename "$entry")" "${model}.dat"
			popd >/dev/null
		else
			mkdir -p "latest/${name}/${brand}"
			pushd "latest/${name}/${brand}" >/dev/null
			ln -s "../../../$(basename "$entry")" "${model}.dat"
			popd >/dev/null
		fi
	done
	popd >/dev/null
done
DONE

popd >/dev/null
