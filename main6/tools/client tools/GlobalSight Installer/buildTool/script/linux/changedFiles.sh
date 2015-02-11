#!/bin/bash
svn log -v -r$1 $2 | grep opensourcing