#!/bin/sh
# ldap This shell script takes care of starting and stopping
# ldap servers (slapd and slurpd).

# description: LDAP stands for Lightweight Directory Access Protocol, used
# for implementing the industry standard directory services.

# processname: slapd

# config: /usr/local/openldap/etc/openldap/slapd.conf
# pidfile: /usr/local/openldap/var/run/slapd.pid

# Check that networking is up.
RETVAL=0

# See how we were called.
case "$1" in

start)
# Start daemons.
/usr/local/openldap/libexec/slapd -u openldap 

;;
stop)
# Stop daemons.

kill -9 `cat /usr/local/openldap/var/run/slapd.pid`

;;
restart)

$0 stop
$0 start
RETVAL=$?

;;

*)

echo "Usage: $0 start|stop|restart}"
exit 1
esac
exit $RETVAL
