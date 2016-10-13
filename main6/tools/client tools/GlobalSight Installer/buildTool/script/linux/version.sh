#!/bin/bash
svn info $1 | grep Revision: | cut -c11-15