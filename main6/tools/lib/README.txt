NOTE: This jar files only contains third party jars.
The third party jars are broken in two (or more) sets.
thirdParty1.jar, and thirdParty2.jar.
This is to get around line-length limitations in one Manifest file.
New jars can be added to thirdParty2.jar, or create a new thirdParty3.jar

If thirdParty3.jar is created, update MANIFEST.MF to include a reference
to it.

The build will create thirdParty1.jar and thirdParty2.jar with the appropriate
manifest files.

The same is true with axisLibs.mf which adds additional Apache Axis libraries
(excluding the main axis.jar) to the classpath. This is done in a separate
axisLibs.mf so that it can be removed easily from the classpath for WebSphere
which bundles in all its own libs.

