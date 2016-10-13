#!/bin/ksh
######
# This script performs all the steps necessary to unpack/install the components
# for the given System 3 Tar/Gzip file.
######
clear
echo "**********"
echo "*** System3 Interactive Installation ***"
echo "**********"
echo

######
# Define some local variables
######
FIRST_INSTALL=Y
RM="/bin/rm -f"
RMR="/bin/rm -rf"
LOCAL_ROOT=$(pwd)
GLOBALSIGHT_HOME=$LOCAL_ROOT/globalsight
CAP=$GLOBALSIGHT_HOME/system3/cap
CXE=$GLOBALSIGHT_HOME/system3/cxe
CXE_LIB=$CXE/lib
SYSTEM3_ENV_FILE=$GLOBALSIGHT_HOME/system3/system3.env
TOMCAT_HOME=$CXE/jakarta-tomcat
export NLS_LANG=AMERICAN_AMERICA.UTF8

######
# Function to display a prompt and wait for a Yes/No response.
# Accepts 2 arguments:
#     $1 is the string that prompts for a Y/N response.
#     $2 is default value that will be used if user presses enter.
#
# On exit the value of the function is stored in the environment
# variable ANSWER.
######
waitForYesNo() {
	if [ $2 = y ]
	then
		DEFAULT=Y
	elif [ $2 = n ]
	then
		DEFAULT=N
	else
		DEFAULT=$2
	fi

	ANSWER=
	while [ -z "$ANSWER" ]
	do
		echo "$1 [$DEFAULT] \c"
		read -r ANSWER
		if [ -z "$ANSWER" ]
		then
			ANSWER=$DEFAULT
		fi
		TEMP=$(echo $ANSWER | cut -c 1)
		if [ $TEMP = y -o $TEMP = Y ]
		then
			ANSWER=Y
		elif [ $TEMP = n -o $TEMP = N ]
		then
			ANSWER=N
		else
			echo "Invalid response ($ANSWER), please try again"
			ANSWER=
		fi
	done
}

######
# Function to wait for a process to complete or die.
# While waiting, this function displays a sequence of dots
# on the screen.
# Accepts 1 argument:
#     $1 is the id of the process to wait for.
######
displayDotsTilDone() {
	ERR_FILE=_process_monitor_.txt
	PROC_ID=$1
	COUNTER=0
	while ps -p $PROC_ID > /dev/null 2> $ERR_FILE
	do
		if [ $COUNTER -lt 9 ]
		then
			echo ".\c"
		else
			echo ":\c"
			COUNTER=-1
		fi
		COUNTER=$((COUNTER+1))
		sleep 1
		if [ -s $ERR_FILE ]
		then
			kill -9 $PROC_ID
			echo
			cat $ERR_FILE
			exit 1
		fi
	done
	$RM $ERR_FILE
	echo "Done."
}

######
# Function to generalize the unpacking of TAR/GZIP files.
# Accepts 4 arguments:
#     $1 is the name of the file to unpack
#     $2 is a descriptive string to display
#     $3 is a flag: "keep" = keep the original file after expansion; otherwise, delete it
#     $4 is a flag: "Y" means display progress dots; "N" means display output
######
unpackFile() {
	FILE_SIZE=$(gunzip -c $1 | tar tvf - | wc -l | xargs)
	echo
	echo "*** Unpacking "$2" archive ($FILE_SIZE files)"
	if [ $4 = Y ]
	then
		gunzip -c $1 | tar xf - &
		displayDotsTilDone $!
	else
		gunzip -c $1 | tar xvf -
	fi
	if [ "$3" != "keep" ]
	then
		$RM $1
	fi
}

######
# Prompt for removal of old installation if one exists.
######
if [ -d $GLOBALSIGHT_HOME ]
then
	FIRST_INSTALL=N
	echo "**********"
	waitForYesNo "* Do you want to remove the old System3 install?" N
	if [ $ANSWER = Y ]
	then
		FIRST_INSTALL=Y
		DIR_SIZE=$(find $GLOBALSIGHT_HOME -name "*.*" -print | wc -l | xargs;)
		echo
		echo "*** Removing $DIR_SIZE files"
		$RMR $GLOBALSIGHT_HOME &
		displayDotsTilDone $!
	fi
fi

######
# If installation is present, prompt for unpacking of archives.
# Make sure the archive exists first.
######
PRIMARY_FILE="system3.tar.gz"
ANSWER=Y
if [ -d $GLOBALSIGHT_HOME ]
then
	echo
	echo "**********"
	waitForYesNo "* Do you want to unpack the System3 archives?" $FIRST_INSTALL
fi
if [ $ANSWER = Y ]
then
	if [ -f $PRIMARY_FILE ]
	then
		echo
		echo "**********"
		echo "* You can choose to see a progress meter in the form of dots on your terminal,"
		echo "* or you can choose to see the full output of the unzip/tar process."
		waitForYesNo "* Do you prefer the progress meter dots?" Y
		unpackFile $PRIMARY_FILE "System3" "keep" $ANSWER
		cd $CAP
		unpackFile cap.tar.gz "Content Adaptation Platform" "delete" $ANSWER
		cd $LOCAL_ROOT/globalsight/system3/cxe/jakarta-tomcat
		unpackFile jakarta-tomcat.tar.gz "Tomcat" "delete" $ANSWER
	else
		echo "File \"$PRIMARY_FILE\" does not exist; cannot continue"
		exit 1
	fi
fi

######
# Prompt for execution of the System3 configuration script, which creates
# environment variable entries that we will need for the environment file.
######
echo
echo "**********"
waitForYesNo "* Do you want to modify the System3 configuration?" $FIRST_INSTALL
if [ $ANSWER = Y ]
then
	cd $CAP/bin
	if [ -f as_init ]
	then
		CAP_BIN=$CAP/bin
		CAP_LIB=$CAP/lib
		SITE_PERL=$CAP_LIB/perl5/site_perl/5.005
		PERL_503=$CAP_LIB/perl5/5.00503
		PERL5LIB=$SITE_PERL:$SITE_PERL/sun4-solaris
		PERL5LIB=$PERL5LIB:$PERL_503:$PERL_503/sun4-solaris
		PERL5LIB=$PERL5LIB:$CAP_BIN/as:$CAP_BIN/as/mod:$CAP_BIN/as/GlobalSight/Admin
		(export PERL5LIB; as_init)
	else
		echo "Cannot find the System3 configuration script; aborting"
		exit 1
	fi
fi

######
# Prompt for creation of the System3 environment file if one already exists.
######
ANSWER=Y
if [ -f $SYSTEM3_ENV_FILE ]
then
	echo
	echo "**********"
	waitForYesNo "* Do you want to re-create the System3 environment file?" $FIRST_INSTALL
fi

if [ $ANSWER = Y ]
then
	# Read the contents of the config file generated by as_init.
	# Import the required environment variables.
	cd $CAP/conf
	CAP_CFG_FILE=system3_init.config

	ACTIVE_HOME=$(grep "ACTIVE_HOME" $CAP_CFG_FILE | grep -v "#" | awk '{print $2}' | xargs);
	ACTIVE_CONFIG=$(grep "ACTIVE_CONFIG" $CAP_CFG_FILE | grep -v "#" | awk '{print $2}' | xargs);
	ORACLE_HOME=$(grep "ORACLE_HOME" $CAP_CFG_FILE | grep -v "#" | awk '{print $2}' | xargs);
	ORACLE_SID=$(grep "Oracle_SID" $CAP_CFG_FILE | grep -v "#" | awk '{print $2}' | xargs);


	if [ -z "$ACTIVE_HOME" -o -z "$ORACLE_HOME" ]
	then
		echo "Configuration file \"$CAP_CFG_FILE\" contains invalid entries, aborting."
		exit 1
	fi

	# Write out the contents of the new environment file.
	cd $CXE

	echo
	echo "*** Creating new System3 environment file"
	
	# Delete the old copy, write out the preliminary contents of the new one.
	$RM $SYSTEM3_ENV_FILE
	echo "#!/bin/ksh" > $SYSTEM3_ENV_FILE
	echo "#" >> $SYSTEM3_ENV_FILE
	echo "# ***** AUTO-GENERATED ENVIRONMENT FILE *****" >> $SYSTEM3_ENV_FILE
	echo "#" >> $SYSTEM3_ENV_FILE
	echo "#       Created on `date`" >> $SYSTEM3_ENV_FILE
	echo "#" >> $SYSTEM3_ENV_FILE
	echo "#       Changes made to this file may be overwritten" >> $SYSTEM3_ENV_FILE
	echo "#       during install(s), so please DO NOT modify" >> $SYSTEM3_ENV_FILE
	echo "#" >> $SYSTEM3_ENV_FILE
	echo "ORACLE_HOME=$ORACLE_HOME" >> $SYSTEM3_ENV_FILE
	echo "ORACLE_SID=$ORACLE_SID" >> $SYSTEM3_ENV_FILE
	echo "LD_LIBRARY_PATH=\$ORACLE_HOME/lib" >> $SYSTEM3_ENV_FILE
	echo "GLOBALSIGHT_HOME=$GLOBALSIGHT_HOME" >> $SYSTEM3_ENV_FILE
	echo "SYSTEM3_HOME=\$GLOBALSIGHT_HOME/system3" >> $SYSTEM3_ENV_FILE
	echo "CXE=\$SYSTEM3_HOME/cxe" >> $SYSTEM3_ENV_FILE
	echo "CXE_BIN=\$CXE/bin" >> $SYSTEM3_ENV_FILE
	echo "CXE_LIB=\$CXE/lib" >> $SYSTEM3_ENV_FILE
	echo "CAP=\$SYSTEM3_HOME/cap" >> $SYSTEM3_ENV_FILE
	echo "CAP_BIN=\$CAP/bin" >> $SYSTEM3_ENV_FILE
	echo "CAP_LIB=\$CAP/lib" >> $SYSTEM3_ENV_FILE
	echo "SITE_PERL=\$CAP_LIB/perl5/site_perl/5.005" >> $SYSTEM3_ENV_FILE
	echo "PERL_503=\$CAP_LIB/perl5/5.00503" >> $SYSTEM3_ENV_FILE
	echo "PERL5LIB=\$SITE_PERL:\$SITE_PERL/sun4-solaris" >> $SYSTEM3_ENV_FILE
	echo "PERL5LIB=\$PERL5LIB:\$PERL_503:\$PERL_503/sun4-solaris" >> $SYSTEM3_ENV_FILE
	echo "PERL5LIB=\$PERL5LIB:\$CAP_BIN/as:\$CAP_BIN/as/mod:\$CAP_BIN/as/GlobalSight/Admin" >> $SYSTEM3_ENV_FILE
	echo "TOMCAT_HOME=\$CXE/jakarta-tomcat" >> $SYSTEM3_ENV_FILE
	echo "ACTIVE_HOME=$ACTIVE_HOME" >> $SYSTEM3_ENV_FILE
	echo "ACTIVE_CONFIG=$ACTIVE_CONFIG" >> $SYSTEM3_ENV_FILE
	echo "JAVA_HOME=\$ACTIVE_HOME/java" >> $SYSTEM3_ENV_FILE
	echo "NLS_LANG=$NLS_LANG" >> $SYSTEM3_ENV_FILE
	
	# Build up the list of jar/zip files for inclusion in the class path
	CXE_JARS=
	for d in `ls $CXE_LIB`
	do
		cd $CXE_LIB/$d
		for f in `ls` 
		do
			if ! [ -z "$CXE_JARS" ]
			then
				CXE_JARS=$CXE_JARS:
			fi
			CXE_JARS=$CXE_JARS\$CXE_LIB/$d/$f
		done
		cd $CXE
	done
	echo "CXE_JARS=$CXE_JARS" >> $SYSTEM3_ENV_FILE

	# Create statements for exporting environment variables
	echo "ALTCLASSPATH=\$CXE/patches:\$CXE/classes:\$ACTIVE_HOME/classes:\$CXE_JARS:\$CXE/config:\$CXE/plugin" >> $SYSTEM3_ENV_FILE
	echo "CLASSPATH=\$ALTCLASSPATH" >> $SYSTEM3_ENV_FILE
	echo "PATH=\$JAVA_HOME/bin:\$ACTIVE_HOME/bin:\$ORACLE_HOME/bin:/usr/bin:/usr/proc/bin:/usr/ucb:/etc:/usr/local/bin:/usr/sbin:." >> $SYSTEM3_ENV_FILE
	# Create export statements
	EXPORT_VARS="ACTIVE_HOME ACTIVE_CONFIG JAVA_HOME PERL5LIB NLS_LANG"
	EXPORT_VARS=$EXPORT_VARS" TOMCAT_HOME ALTCLASSPATH CLASSPATH PATH"
	EXPORT_VARS=$EXPORT_VARS" GLOBALSIGHT_HOME SYSTEM3_HOME CAP CAP_BIN CAP_LIB CXE CXE_BIN CXE_LIB"
	EXPORT_VARS=$EXPORT_VARS" ORACLE_HOME ORACLE_SID LD_LIBRARY_PATH"
	echo "export $EXPORT_VARS" >> $SYSTEM3_ENV_FILE

	# This prevents the user's .kshrc from being invoked
	echo "unset ENV" >> $SYSTEM3_ENV_FILE

	# Make the environment file read-only to prevent accidental modification
	chmod 444 $SYSTEM3_ENV_FILE
fi

. $SYSTEM3_ENV_FILE

######
# Make sure the User's logs & plugin directories exists
######
if ! [ -d $CXE/logs ]
then
	mkdir $CXE/logs
fi

if ! [ -d $CXE/plugin ]
then
	mkdir $CXE/plugin
fi

if ! [ -d $CXE/patches ]
then
	mkdir $CXE/patches
fi

######
# Add some required symbolic links for Tomcat
######
TOMCAT_SERVER=$CXE/jakarta-tomcat/conf/server.xml
$RM $TOMCAT_SERVER
ln -fs $CXE/config/server.xml $TOMCAT_SERVER
$RM $TOMCAT_HOME/web-docs
ln -fs $CXE/web-docs $TOMCAT_HOME/web-docs

######
# Add execute permissions on shell files
######
chmod 544 $CXE/active/*.ksh

######
# Prompt for execution of the CXE configuration script
######
echo
echo "**********"
waitForYesNo "* Do you want to modify the CXE configuration?" $FIRST_INSTALL
if [ $ANSWER = Y ]
then
	cd $CXE/active
	if [ -f install_cxe.ksh ]
	then
		./install_cxe.ksh
		echo "*** NOTE: $GLOBALSIGHT_HOME/system3/bin/system3.server"
		echo "***       has been modified. You may need to update the contents of"
		echo "***       /etc/init.d, /etc/rc3.d, /etc/rc0.d as appropriate."
	else
		echo "Cannot find the CXE configuration script; aborting"
		exit 1
	fi
fi

echo
echo "**********"
echo "*** Installation complete ***"
echo "***"
echo "*** Please logout and re-login to ensure that"
echo "*** your environment is properly set up."
echo "**********"
