#!/bin/sh                               -*- Mode: Sh -*- 
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
JAXP=${TOP}/lib/jaxp1.0.1.jar
ORO=${TOP}/lib/oroMatcher1.1.0.jar
JUNIT=${TOP}/junit3.2.jar 

# Output dir
BUILDDIR=${TOP}/../Classes

CLPATH=".${SEP}${SEP}${BUILDDIR}${SEP}${JGL}${SEP}${JAXP}${SEP}${ORO}${SEP}${JUNIT}${SEP}${CLASSPATH}"

# end of configurable stuff

if [ -z "$1" ]; then
    echo "Usage: $0 file.htm|file.html|file.js|file.css" 1>&2
    exit 1;
fi

if [ ! -f "$1" ]; then
    echo "input file \"$1\" not found..." 1>&2
    exit 1;
fi

if [ ! -z "$2" ]; then
    ENCODING="$2"
else
    ENCODING="iso-8859-1"
fi

if echo $1 | grep ".html$"; then 
    FILETYPE="html";
    FILEBASE=`dirname $1`/`basename $1 .html`;
elif echo $1 | grep ".htm$"; then 
    FILETYPE="html";
    FILEBASE=`dirname $1`/`basename $1 .htm`
elif echo $1 | grep ".cfm"; then 
    FILETYPE="html";
    FILEBASE=`dirname $1`/`basename $1 .cfm`
elif echo $1 | grep ".css$"; then 
    FILETYPE="css";
    FILEBASE=`dirname $1`/`basename $1 .css`
elif echo $1 | grep ".js$"; then 
    FILETYPE="js";
    FILEBASE=`dirname $1`/`basename $1 .js`
elif echo $1 | grep ".properties$"; then 
    FILETYPE="jp";
    FILEBASE=`dirname $1`/`basename $1 .properties`
elif echo $1 | grep ".xxml$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname $1`/`basename $1 .xxml`
elif echo $1 | grep ".xsl$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname $1`/`basename $1 .xsl`
elif echo $1 | grep ".xslt$"; then 
    FILETYPE="xml";
    FILEBASE=`dirname $1`/`basename $1 .xslt`
else
    echo "unknown file type \"$1\"" 1>&2
    echo "known file types: .htm .html .css .js .properties .xxml .xsl .xslt" 1>&2
    exit 1;
fi

case ${FILETYPE} in
    "html") java -classpath ${CLPATH} \
	    test.HtmlExtractor.HtmlSegmenter "$1" > "${FILEBASE}.xml" ;;
    "css")  java -classpath ${CLPATH} \
	    test.css.CssExtractor "$1" > "${FILEBASE}.xml" ;;
    "js")   java -classpath ${CLPATH} \
	    test.javascript.JsExtractor "$1" > "${FILEBASE}.xml" ;;
    "jp")   java -classpath ${CLPATH} \
	    test.javaprop.JpExtractor "$1" > "${FILEBASE}.xml" ;;
    "xml")  java -classpath ${CLPATH} \
	    test.xml.XmlExtractor "$1" > "${FILEBASE}.xml" ;;
esac

if [ $? -ne 0 ]; then
    echo "${FILETYPE} extraction of $1 failed!" 1>&2
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
    echo "<?xml version='1.0' encoding='${ENCODING}' ?>" > $F
    echo "<?xml-stylesheet type='text/xsl' href='`pwd`/diplomat.xsl'?>" >> $F
    tail +2 "${FILEBASE}.xml" >> $F
    mv -f $F "${FILEBASE}.xml"
fi

echo "Result written to ${FILEBASE}.xml"
"${IEXPLORE}" "${FILEBASE}.xml" &

exit 0
