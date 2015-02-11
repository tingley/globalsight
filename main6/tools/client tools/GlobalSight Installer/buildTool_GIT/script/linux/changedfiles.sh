#!/bin/bash
cd $1
git diff $2..master --name-only --diff-filter=AM > changedfiles.log