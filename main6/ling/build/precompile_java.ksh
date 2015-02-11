#!/bin/ksh
##########
# This script is used during the build process to precompile
# special .jj files into .java files.  The resulting files
# are then compiled into requisite class files by the standard
# javac compiler.
#
# The script requires no arguments.
##########

##########
# Some required local variables
##########
MY_DIR=`pwd -P`
PARENT=`(cd .. ; pwd -P)`
LINGLIB="$PARENT/lib"
COM="$PARENT/com"
JAVACCLIB="$LINGLIB/JavaCCPatch.zip:$LINGLIB/JavaCC.zip"
JAVACCMAIN="COM.sun.labs.javacc.Main"
JAVACC="java -classpath $JAVACCLIB $JAVACCMAIN"

##########
# setPathName() - determine the path name in a fully-qualified
# file-specification.
#
# Accepts 1 argument, the fully qualified name.
##########
setPathName() {
	PATH_NAME=""
	if ! [ -z "$1" ]
	then
		PATH_NAME=`expr "$1" : "\(.*\)\/.*"`
	fi
	if [ "$PATH_NAME" = "" ]
	then
		PATH_NAME="."
	fi
}

##########
# setFileName() - determine the file name in a fully-qualified
# file-specification.
#
# Accepts 1 argument, the fully qualified name.
##########
setFileName() {
	FILE_NAME=""
	if ! [ -z "$1" ]
	then
		FILE_NAME=`basename "$1"`
	fi
}

##########
# patch() - patch the given file by using sed to insert a new line.
#
# Accepts 1 argument, the name of the file in the current directory.
##########
patch() {
	file=$1
	sed -e "s|return new ParseException(token, exptokseq, tokenImage);|token.next.beginColumn = jj_input_stream.column; token.next.beginLine = jj_input_stream.line; &|1" < $file > xyz_temp
	mv -f xyz_temp $file
}

##########
# applyPatches() - patch any precompiled java files that require patches
##########
applyPatches() {
	file=UCode_CharStream
	if [ -f "$file".java ]
	then
		cp -f "$file"_patched.txt "$file".java
	fi

	for file in Parser.java AmbassadorDwUpParser.java
	do
		if [ -f $file ]
		then
			patch $file
		fi
	done
}

##########
# precompile() - precompiles a single .jj file.
#
# Accepts 1 argument, the fully qualified name of the
# file to compile.
##########
precompile() {
	if ! [ -z "$1" ]
	then
		setPathName $1
		setFileName $1
		if [ -f $1 -a -d $PATH_NAME ]
		then
			echo "Compiling $FILE_NAME in $PATH_NAME"
			cd $PATH_NAME
			$JAVACC $FILE_NAME > /dev/null 2> /dev/null
			if [ $? -ne 0 ]
			then
				echo Error encountered: check compilation of $FILE_NAME
			else
				applyPatches
			fi
			cd $MY_DIR
		else
			echo "Error: unable to compile $FILE_NAME in $PATH_NAME"
		fi
	fi
}

##########
# Entry point for the main script
##########
for f in `find $COM -name *.jj -print`
do
	precompile $f
done
