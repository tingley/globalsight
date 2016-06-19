# $1: the service file path
# $2 the service name
test -s /etc/init.d/$2 && rm /etc/init.d/$2
chmod a+x $1
update-rc.d $2 remove
cp $1 /etc/init.d/$2
update-rc.d $2 defaults 99