GlobalSight Source Mirror
-------------------------------------------------------------

This is *not* the official GlobalSight git repository.  The official
git repository is located at [http://125.35.10.59:7990/scm/globalsight/globalsightsource.git](http://125.35.10.59:7990/scm/globalsight/globalsightsource.git).
This repo is mirrored automatically by cron.

GlobalSight
===========

This is live-ish mirror of the official [GlobalSight](http://globalsight.com)
git repository maintained by Welocalize.


How to Build and Run GlobalSight
--------------------------------
* `cd main6/tools/build`
* Run `ant dist`
* Expand the dated archive in dist `main6/tools/build/dist` directory to your install location.
* Create a mysql database
* Run the installer tool (GUI and terminal versions both exist) in the `install` directory of your expanded dist archive.

About Branches
--------------
* `master` represents CVS HEAD with any local changes (eg, this README).

Older release branches are available in CVS, but aren't currently pushed to
git.

Support
-------
The
[globalsight-devel](https://groups.google.com/forum/#!forum/globalsight-dev)
mailing list or the [GlobalSight
forums](http://www.globalsight.com/forums/) are the best bet.

You are also welcome to contact me (chase@spartansoftwareinc.com) with
questions; however, I am not currently actively involved with GlobalSight
development.
