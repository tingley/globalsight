# Batch file to create a spell checker dictionary.

BASE=deployment/ambassador.ear/spellchecker.war/WEB-INF

java -Xmx256m -classpath "${BASE}/classes:${BASE}/lib/lucene-1.4.3.jar" \
    spell.CreateDictionary %*
