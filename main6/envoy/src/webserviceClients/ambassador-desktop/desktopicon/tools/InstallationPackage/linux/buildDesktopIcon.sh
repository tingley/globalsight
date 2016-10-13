rm -R src
rm -R jar
unzip DesktopIcon.zip -d src
sed "s/%version%/$1/g" control.template > root/DEBIAN/control
cp src/desktopicon.jar .
unzip desktopicon.jar -d jar
rm -R root/usr/share/doc/globalsight/DesktopIcon
mkdir -p root/usr/share/doc/globalsight/DesktopIcon
mkdir -p root/usr/share/globalsight/DesktopIcon
cp src/*.pdf root/usr/share/doc/globalsight/DesktopIcon
cp -r src/* root/usr/share/globalsight/DesktopIcon
cp -r jar/com root/usr/share/globalsight/DesktopIcon
cp -r jar/org root/usr/share/globalsight/DesktopIcon
cp -r jar/log4j.xml root/usr/share/globalsight/DesktopIcon
chmod -R 777 root
chmod -R 755 root/DEBIAN
cd root 
dpkg -b . ../desktopicon_$1.deb
cd ..
rm desktopicon.jar
rm -R src
rm -R jar
alien -r desktopicon_$1.deb
