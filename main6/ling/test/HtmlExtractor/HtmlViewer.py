#!jpython

import sys
from java.lang import *
from java.io import *
from java.util import *
from jarray import array
from com.globalsight.ling.common  import XmlWriter
from com.globalsight.ling.docproc import EFInputData
from com.globalsight.ling.docproc import ExtractorRegistry
from com.globalsight.ling.docproc import Output
from com.globalsight.ling.docproc import DiplomatAttribute
from com.globalsight.ling.docproc import DiplomatWriter
from com.globalsight.ling.docproc import DiplomatReader
from com.globalsight.ling.docproc.extractor.html import Extractor

str_file = "C:/GS/ling/test/HTMLExtractor/changes.html"
if len(sys.argv) > 1:
    str_file = sys.argv[1]

# Read in HTML file and create Input object
input = EFInputData()
input.setCodeset("8859_1")
locale = Locale("en", "US")
input.setLocale(locale)
input.setURL("file:///" + str_file)

# Extraction
output = Output()
extractor = Extractor()
extractor.init(input, output)
extractor.loadRules()
try:
    extractor.extract()
except:
    a = 1

# Print segments
da = DiplomatAttribute()
print DiplomatWriter.WriteXML(da, output) # before

#  da = DiplomatAttribute()
#  dr = DiplomatReader(DiplomatWriter.WriteXML(da, output));
#  o = dr.getOutput()
#  print DiplomatWriter.WriteXML(da, o)    # after
