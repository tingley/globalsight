#set install path of GlobalSight DesktopIcon
DESKTOPICON_JAVA_HOME=/Applications/DesktopIcon.app/Contents/Resources/Java
export DESKTOPICON_JAVA_HOME

#change current dictory to DESKTOPICON_JAVA_HOME
cd $DESKTOPICON_JAVA_HOME

PATH=/bin:/sbin:/usr/bin:/usr/sbin export PATH
RUBYLIB=.:./resource/ruby/firewatir:./resource/ruby export RUBYLIB

echo "Running GlobalSight Desktop Icon"
echo "in directory"
pwd
echo " "
echo "RUBYLIB=$RUBYLIB"

java -jar ${DESKTOPICON_JAVA_HOME}/desktopicon.jar $1 $2 $3 $4 $5