#!jpython

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

str_baseDir = "d:/JavaScript/"

# Read in HTML file and create Input object
input = EFInputData()
input.setCodeset("8859_1")
locale = Locale("en", "US")
input.setLocale(locale)
input.setURL("file:///" + str_baseDir + "testPlacables.html")

# Extraction
output = Output()
extractor = Extractor()
extractor.init(input, output)
extractor.loadRules()
extractor.extract()

# Print segments
print DiplomatWriter.WriteXML(output)





