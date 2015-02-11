(*Hi, this is AppleScript to run DesktopIcon*)
(*
Set the DesktopIcon path to make it work well.
*)
set desktopicon_java_home to "/Applications/DesktopIcon.app/Contents/Resources/Java"

(*
Script below is for run DesktopIcon directly.
*)
do shell script "

DESKTOPICON_JAVA_HOME=" & desktopicon_java_home & "

export DESKTOPICON_JAVA_HOME

cd $DESKTOPICON_JAVA_HOME

PATH=/bin:/sbin:/usr/bin:/usr/sbin export PATH
RUBYLIB=.:./resource/ruby/firewatir:./resource/ruby export RUBYLIB

java -jar ${DESKTOPICON_JAVA_HOME}/desktopicon.jar "

(* 
Script below is for dropping file(s) to DesktopIcon's icon
*)

on open dropitems
	
	if the dropitems's length > 1 then
		set file_1 to POSIX path of item 1 in dropitems
	else
		set file_1 to POSIX path of dropitems
	end if
	
	set desktopicon_java_home to "/Applications/DesktopIcon.app/Contents/Resources/Java"
	
	do shell script "

DESKTOPICON_JAVA_HOME=" & desktopicon_java_home & "

export DESKTOPICON_JAVA_HOME

cd $DESKTOPICON_JAVA_HOME

PATH=/bin:/sbin:/usr/bin:/usr/sbin export PATH
RUBYLIB=.:./resource/ruby/firewatir:./resource/ruby export RUBYLIB

java -jar ${DESKTOPICON_JAVA_HOME}/desktopicon.jar " & file_1
end open