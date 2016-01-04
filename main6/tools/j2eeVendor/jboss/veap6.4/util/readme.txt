This is instruction for importing self-signed certificate to client machine for java applet trust.

Using self-signed certificate or default one, following commands need to be executed manually in client machine for java applet trust.

1.First get globalsight_ori.keystore from globalsight source located at main6\tools\j2eeVendor\jboss\veap6.4\util.

2.Run command:
C:\Program Files\Java\jre1.6.0_07\lib\security>"C:\Program Files\Java\jre1.6.0_07\bin\keytool" -export -alias globalsight -file globalsight_ori.crt -keystore <path-of-globalsight_ori.keystore>
(Note: keystore password is changeit)

3.Run command:
C:\Program Files\Java\jre1.6.0_07\lib\security>"C:\Program Files\Java\jre1.6.0_07\bin\keytool" -importcert -trustcacerts -alias globalsight -keystore cacerts -file <path-of-globalsight_ori.crt> -storepass changeit -noprompt


BTW, attaching a delete command:
keytool -delete -alias globalsight -keystore cacerts -storepass changeit

Both "Base 64 encoding (binary format)" and "DER encoding (printable format)" are supported when importing certificates into java keystore.
The -export/-exportcert command by default outputs a certificate in Base 64 encoding, but will instead output a certificate in the printable encoding format, if the -rfc option is specified.
You can open the printable format files with notepad.exe or any other text editor.