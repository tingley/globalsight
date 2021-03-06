GlobalSight Source Mirror
-------------------------------------------------------------

This is *not* the official GlobalSight git repository.  The official
git repository is located at [http://124.133.49.189:7990/scm/globalsight/globalsightsource.git](http://124.133.49.189:7990/scm/globalsight/globalsightsource.git).
This repo is a mirror that is updated automatically.

**Note**: At this time there have been no commits to the official GlobalSight repository since September 2019, and I assume the project to be dead.

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
* `master` represents the git master branch with any local changes (eg, this README).

Older release branches (back to the 8.6.1 release) are available in git, but
aren't currently pushed to this mirror.  Releases prior to 8.6.1 aren't
available in git, nor is revision history prior to the migration from CVS to
git (which happened in early 2015).

Support
-------
The [GlobalSight forums](http://www.globalsight.com/forums/) are the best bet.

You are also welcome to contact me (tingley@gmail.com) with
questions; however, I am not currently actively involved with GlobalSight
development.
