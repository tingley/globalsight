from java.lang import *
from java.io import *
from java.util import *
from jarray import array
from com.globalsight.ling.common import *
from com.globalsight.ling.docproc import *

# Read in HTML file and create Input object
input = EFInputData()
input.setCodeset("8859_1")
locale = Locale("en", "US")
input.setLocale(locale)
input.setType(EFInputDataConstants.HTML)
fs = FileInputStream("d:/globalsight/prototype/integer.html")
numBytes = fs.available()
bytes = array(range(numBytes), 'b')
fs.read(bytes)
input.setInput(bytes)

# Extraction
output = Output()
extractor = ExtractorWrapper()
extractor.extract(input, output)

# Print segments
it = output.documentElementIterator()
da = DocumentAttribute()
writer = XmlWriter()
while it.hasNext():
    de = it.next()
    de.toDiplomatString(da, writer)
   ##  if (de.type() == DocumentElement.TRANSLATABLE):
   ##  print de.getChunk()
    
print writer.getXml()
