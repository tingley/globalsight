'                          -*- Mode: Visual-Basic -*- 
' 
' runxsl.vbs
' 
' Copyright (C) GlobalSight Corporation 2000 (Cornelis van der Laan)
' 
' Responsible     : Cornelis van der Laan
' Author          : Cornelis van der Laan
' EMail           : nils@globalsight.com
' Created On      : Mon Aug 07 14:24:43 2000
' Last Modified By: Cornelis van der Laan
' Last Modified On: Tue Aug 08 03:03:53 2000
' Update Count    : 48
' Status          : Unknown, use with caution!
' Description     : apply an xsl file to an xml file and output the result
' Note            : requires MSXML2 (July 2000), run "xmlinst" after setup;
'                   see http://msdn.microsoft.com/xml

Dim xmlfile, xslfile, output
Dim xml, xsl, res

Set args = WScript.Arguments
If args.Count <> 3 And args.count <> 2 Then
  WScript.Echo "Usage: runxsl xmlfile xslfile [outputfile]"
  WScript.Quit 1
End If

xmlfile = args(0)
xslfile = args(1)
If args.Count = 3 Then
  output = args(2)
Else
  output = ""
End If 

Set xml = CreateObject("MSXML2.DOMDocument")
Set xsl = CreateObject("MSXML2.DOMDocument")
Set res = CreateObject("MSXML2.DOMDocument")
xml.async = False
xsl.async = False
res.async = False

If Not xml.Load(xmlfile) Then
  WScript.Echo "cannot load xml file " & xmlfile
  WScript.Quit 1  
End If

If Not xsl.Load(xslfile) Then
  WScript.Echo "cannot load xsl file " & xslfile
  WScript.Quit 1
End If

xml.transformNodeToObject xsl, res

If output <> "" Then
  res.save(output)
Else
  WScript.Echo res.xml
End If 

WScript.Quit 0

