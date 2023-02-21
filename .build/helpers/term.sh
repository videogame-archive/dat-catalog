#!/bin/bash

COLUMNS=$(tput cols)

printf '\033%%G'

C_UP() {
	echo -e -n "\e[${1}A"
}

C_DN() {
	echo -e -n "\e[${1}B"
}

C_FW() {
	if [ $1 -lt 0 ]; then
		echo -e -n "\r\e[$(($COLUMNS+$1))C"
	else
		echo -e -n "\e[${1}C"
	fi
}

C_BW() {
	echo -e -n "\e[${1}D"
}

C_NL() {
	echo -e -n "\e[${1}E"
}

C_PL() {
	echo -e -n "\e[${1}F"
}

C_HA() {
	if [ $1 -lt 0 ]; then
		echo -e -n "\e[$(($COLUMNS+$1))G"
	else
		echo -e -n "\e[${1}G"
fi
}

C_POS() {
	if [ -z $2 ]; then
		echo -e -n "\e[${1}H"
	else
		echo -e -n "\e[${1};${2}H"
	fi
}

C_POS_GET() {
	local CURPOS
	read -sdR -p $'\E[6n' CURPOS
	CURPOS=${CURPOS#*[} # Strip decoration characters <ESC>[
	echo "${CURPOS}"    # Return position in "row;col" format
}

ED_END() {
	echo -e -n "\e[0J"
}

ED_BEG() {
	echo -e -n "\e[1J"
}

ED_ALL() {
	echo -e -n "\e[2J"
}

EL_END() {
	echo -e -n "\e[0K"
}

EL_BEG() {
	echo -e -n "\e[1K"
}

EL_ALL() {
	echo -e -n "\e[2K"
}

CR() {
	echo -e -n "\r"
}

LF() {
	echo -e -n "\n"
}

normal() {
	echo -e -n "\e[0m${1}"
}

bold() {
	echo -e -n "\e[1m${1}\e[0m"
}

dim() {
	echo -e -n "\e[2m${1}\e[0m"
}

red() {
	echo -e -n "\e[31m${1}\e[0m"
}

bold_red() {
	echo -e -n "\e[1;31m${1}\e[0m"
}

green() {
	echo -e -n "\e[32m${1}\e[0m"
}

bold_green() {
	echo -e -n "\e[1;32m${1}\e[0m"
}

yellow() {
	echo -e -n "\e[33m${1}\e[0m"
}

bold_yellow() {
	echo -e -n "\e[1;33m${1}\e[0m"
}

blue() {
	echo -e -n "\e[34m${1}\e[0m"
}

bold_blue() {
	echo -e -n "\e[1;34m${1}\e[0m"
}

magenta() {
	echo -e -n "\e[35m${1}\e[0m"
}

bold_magenta() {
	echo -e -n "\e[1;35m${1}\e[0m"
}

cyan() {
	echo -e -n "\e[36m${1}\e[0m"
}

bold_cyan() {
	echo -e -n "\e[1;36m${1}\e[0m"
}

white() {
	echo -e -n "\e[1;97m${1}\e[0m"
}

BAD() {
	C_HA -4; bold_red "BAD\n"
}

OK() {
	C_HA -3; bold_green "OK\n"
}

MISS() {
	C_HA -5; yellow "MISS\n"
}

FOUND() {
	C_HA -6; green "FOUND\n"
}

DONE() {
	C_HA -5; bold_blue "DONE\n"
}

dot_green() {
	green '•'
}

dot_red() {
	red '•'
}

dot_yellow() {
	yellow '•'
}

TAB() {
	declare i
	for ((i=0; i<$1; i++)); do echo -e -n "\t"; done
}

ask_yes_no() {
	read -p "$1 [y/N]? " -n 1 -r
	LF
	if [[ $REPLY =~ ^[Yy]$ ]]; then return 1; else return 0; fi
}
