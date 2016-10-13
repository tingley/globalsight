#!/bin/sh         -*- Mode: Sh -*- 
# 
# Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
# 
# THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
# GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
# IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
# OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
# AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
# 
# THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
# SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
# UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
# BY LAW.
# 

# start configuration

TEMP=${TEMP:-"c:/temp"}

# Need Internet Explorer 5.x with XML Parser July Preview
IEXPLORE=`which iexplore`
if [ -z "${IEXPLORE}" ]; then
    IEXPLORE="C:/Program Files/Internet Explorer/iexplore"
fi

# Pathseparator: ";" for Windows, ":" for Unix
SEP=";"

# Root directory of GlobalSight sources
TOP=../..

# Required libraries - delete if in system classpath...
JGL=${TOP}/lib/jgl3.1.0.jar
JUNIT=${TOP}/lib/junit3.2.jar
ORO=${TOP}/lib/oroMatcher1.1.0.jar
REGEXP=${TOP}/lib/jakarta-regexp-1.1.jar
XALAN=${TOP}/lib/xalan.1.2.do2.jar
XERCES=${TOP}/lib/xerces.1.2.0.jar

# Output dir
BUILDDIR=${TOP}/../Classes

CLPATH=".${SEP}${SEP}${BUILDDIR}${SEP}${XERCES}${SEP}${XALAN}${JGL}${SEP}${SEP}${ORO}${SEP}${REGEXP}${SEP}${JUNIT}${SEP}${CLASSPATH}"

# end of configurable stuff

OPTIONS=

while [ ! "$1" = "" ]
do
    case $1 in 
    -e) OPTIONS="$OPTIONS $1 $2"; shift; shift; ;;
    -r) OPTIONS="$OPTIONS $1 $2"; shift; shift; ;;
    -l) OPTIONS="$OPTIONS $1 $2"; shift; shift; ;;
    *)  FILE=$1; shift; ;;
    esac
done



if [ -z "$FILE" ]; then
    echo "Usage: $0 [options] file" 1>&2
    echo "Recognized file types: htm,html,dhtml,cfm,js,css,properties,xxml,xsl,xslt" 1>&2
    echo "Options:" 1>&2
    echo "  -r rulesfile:\tset extractor rules " 1>&2
    echo "  -l locale:\tset source file locale" 1>&2
    echo "  -e encoding:\tset source file encoding (IANA style)" 1>&2
    exit 1;
fi

if [ ! -f "$FILE" ]; then
    echo "input file \"$FILE\" does not exist..." 1>&2
    exit 1;
fi

if echo "$FILE" | grep ".dhtml$"; then 
    FILETYPE="html";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .dhtml`;
elif echo "$FILE" | grep ".html$"; then 
    FILETYPE="html";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .html`;
elif echo "$FILE" | grep ".htm$"; then 
    FILETYPE="html";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .htm`
elif echo "$FILE" | grep ".cfm"; then 
    FILETYPE="html";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .cfm`
elif echo "$FILE" | grep ".css$"; then 
    FILETYPE="css";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .css`
elif echo "$FILE" | grep ".js$"; then 
    FILETYPE="js";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .js`
elif echo "$FILE" | grep ".properties$"; then 
    FILETYPE="jp";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .properties`
elif echo "$FILE" | grep ".xxml$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .xxml`
elif echo "$FILE" | grep ".xsl$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .xsl`
elif echo "$FILE" | grep ".xslt$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname "$FILE"`/`basename "$FILE" .xslt`
else
    echo "unknown file type \""$FILE"\"" 1>&2
    echo "known file types: .htm .html .dhtml .cfm .css .js .properties .xxml .xsl .xslt" 1>&2
    exit 1;
fi

# This runs only the extractor without segmentation and wordcount
# java -classpath ${CLPATH} test.HtmlExtractor.HtmlExtractor \
#   `pwd`/"$FILE" ${OPTIONS} > "${FILEBASE}.xml"

# This runs the extractor with segmentation and wordcount
java -classpath ${CLPATH} Diplomat "$FILE" ${OPTIONS} > "${FILEBASE}.xml"

if [ $? -ne 0 ]; then
    echo "${FILETYPE} extraction of $FILE failed!" 1>&2
    exit 1
fi

if grep -i "<it" ${FILEBASE}.xml >/dev/null; 
then
    echo "ISOLATED TAG FOUND in ${FILEBASE}.xml" 1>&2
fi

if [ ! -f diplomat.xsl ]; 
then
    echo "Warning: diplomat.xsl not found in current directory" 1>&2
else
    F="${TEMP}/~diplomat.xml"
    head -1 "${FILEBASE}.xml" > $F
    echo "<?xml-stylesheet type='text/xsl' href='`pwd`/diplomat.xsl'?>" >> $F
    tail +2 "${FILEBASE}.xml" >> $F
    mv -f $F "${FILEBASE}.xml"
fi

echo "Result written to ${FILEBASE}.xml"
"${IEXPLORE}" "`pwd`/${FILEBASE}.xml" &

exit 0
