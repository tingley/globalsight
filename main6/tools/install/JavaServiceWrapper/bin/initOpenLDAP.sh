#!/bin/bash

sudo -u openldap kill -9 `cat $1/var/run/slapd.pid`

sudo -u openldap rm -f $1/etc/globalsight/data/[!DB]*

sudo -u openldap $1/sbin/slapadd -l /home/jboss/server/GlobalSight_7.1/install/data/openldap-linux/globalsight.ldif

sudo -u root /etc/init.d/slapd.sh start
