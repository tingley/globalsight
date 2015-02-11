Imports System.Xml
Imports System.Threading
Imports PassoloU

Public Class ImportUtil

    Dim PSL As PassoloApp
    Dim mythread As Thread
    Dim start As Boolean = False

    Dim isOldVersion = False

    Dim pUtil As PassoloUtil = New PassoloUtil
    Dim fUtil As FileUtil = New FileUtil

    Dim path As String
    'Dim textWriter As XmlTextWriter
    Dim textWriters As Hashtable = New Hashtable
    Dim lancnt As Integer
    Dim languages As HashSet(Of PslLanguage) = New HashSet(Of PslLanguage)

    Dim languageTables As Hashtable = New Hashtable
    Dim existLocales As List(Of Short) = New List(Of Short)

    Public Sub setPassoloApp(ByVal passoloApp As PassoloApp)
        PSL = passoloApp
    End Sub

    Public Function getStart()
        Return start
    End Function

    Private Function getLanguage(ByVal languageId As Short)
        Dim languageString As String
        languageString = languageTables.Item(languageId)
        If languageString Is Nothing Then
            languageString = PSL.GetLangCode(languageId, 8)
            languageTables.Add(languageId, languageString)
        End If

        Return languageString
    End Function

    Public Sub setStart(ByVal startValue As Boolean)

        If start = startValue Then
            Return
        End If

        start = startValue

    End Sub

    Public Function startConvert()

        mythread = New Thread(New ThreadStart(AddressOf convertThread))
        mythread.Start()
        Return True

    End Function

    Private Function getImportRoot()
        Return Context.CONVERTER_ROOT + "\" + Context.PASSOLO + "\" + Context.IMPORT
    End Function

    Private Sub convertThread()
        Dim flist As System.Collections.ObjectModel.ReadOnlyCollection(Of String)
        Dim x As Integer

        While start
            flist = Microsoft.VisualBasic.FileIO.FileSystem.GetFiles(getImportRoot(), FileIO.SearchOption.SearchAllSubDirectories, "*" + Context.IM_COMMAND)
            x = flist.Count

            For i = 0 To x - 1

                If start = False Then
                    Return
                End If

                path = flist.Item(i)

                Try
                    convertLpu2Xliff()
                Catch ex As Exception
                    fUtil.addMsg(Context.CONVERSION_FAILED + ex.Message)
                    fUtil.addStatus(path.Substring(0, path.Length - Context.IM_COMMAND.ToString.Length), 1, ex.Message)
                End Try

                Kill(path)

            Next

            Thread.Sleep(1000)
        End While

    End Sub

    Private Sub writeHead()
        For Each textWriter As XmlTextWriter In textWriters.Values
            textWriter.WriteStartDocument()
            textWriter.WriteStartElement("xliff")
            textWriter.WriteAttributeString("version", "", "1.0")
        Next
    End Sub

    Private Sub writeFoot()
        For Each textWriter As XmlTextWriter In textWriters.Values
            textWriter.WriteEndElement()
        Next
    End Sub

    Private Sub writeFileHead(ByVal srn As PslSourceList)
        For Each textWriter As XmlTextWriter In textWriters.Values
            textWriter.WriteStartElement("file")
            textWriter.WriteAttributeString("original", srn.SourceFile)
            textWriter.WriteAttributeString("source-language", getLanguage(srn.LangID))
            textWriter.WriteAttributeString("tool-id", srn.ListID.ToString)
            textWriter.WriteStartElement("body")
        Next
    End Sub

    Private Sub writeFileFoot()
        For Each textWriter As XmlTextWriter In textWriters.Values
            textWriter.WriteEndElement()
            textWriter.WriteEndElement()
        Next
    End Sub

    Private Function getTarget(ByVal sTuv As PslSourceString, ByVal trn As PslTransList)

        Dim tuv As PslTransString
        tuv = trn.String(sTuv.Number, 3)

        If Not (tuv Is Nothing) Then
            Return tuv
        End If

        Return Nothing
    End Function

    Private Sub writeTargetTuv(ByVal tuv As PslTransString, ByVal textWriter As XmlTextWriter)

        Dim state As String = pUtil.getState(tuv)

        textWriter.WriteStartElement("target")
        textWriter.WriteAttributeString("xml:lang", "", getLanguage(tuv.TransList.Language.LangID))
        textWriter.WriteAttributeString("state", "", state)
        textWriter.WriteString(pUtil.encodeString(tuv.Text))
        textWriter.WriteEndElement()

    End Sub

    Private Sub writeMatches(ByVal trn As PslTransList, ByVal sContent As String, ByVal tContent As String, ByVal textWriter As XmlTextWriter)
        Dim pts As PslTranslations
        pts = trn.TranslateText(sContent, 75)

        For j2 = 1 To pts.Count

            If start = False Then
                handleStopped()
                Exit Sub
            End If

            Dim pt As PslTranslation
            pt = pts.Item(j2)

            If pt.SourceString Is Nothing Or "" = pt.SourceString Or pt.TransString = tContent Then
                Continue For
            End If

            textWriter.WriteStartElement("alt-trans")
            textWriter.WriteAttributeString("match-quality", pt.Match)

            textWriter.WriteStartElement("source")
            textWriter.WriteString((pt.SourceString))
            textWriter.WriteEndElement()

            textWriter.WriteStartElement("target")
            textWriter.WriteAttributeString("xml:lang", "", getLanguage(trn.Language.LangID))
            textWriter.WriteString((pt.TransString))
            textWriter.WriteEndElement()

            textWriter.WriteEndElement()
        Next
    End Sub

    Private Sub WriteSourceTuv(ByVal sTuv As PslSourceString, ByVal textWriter As XmlTextWriter)
        Dim sContent As String
        sContent = sTuv.Text

        textWriter.WriteStartElement("source")
        textWriter.WriteAttributeString("state", "", pUtil.getSourceState(sTuv))
        textWriter.WriteString(pUtil.encodeString(sContent))
        textWriter.WriteEndElement()

    End Sub

    Private Sub handleStopped()
        fUtil.addMsg(Context.CONVERSION_STOP)
        fUtil.addStatus(path, 1, Context.CONVERSION_STOP)
    End Sub

    Private Sub handlePslSourceList(ByVal srn As PslSourceList, ByVal prj As PslProject)

        writeFileHead(srn)

        Dim usedlanguages As HashSet(Of PslLanguage) = New HashSet(Of PslLanguage)
        Dim srn2trn As Hashtable = New Hashtable

        For Each language As PslLanguage In languages

            Dim trn As PslTransList
            trn = prj.TransLists.Item(srn, language.LangID)

            If Not (trn Is Nothing Or trn.IsDeleted) Then

                usedlanguages.Add(language)
                srn2trn.Add(srn.ListID.ToString + "_" + language.LangID.ToString, trn)
                existLocales.Add(language.LangID)

            End If
        Next

        If existLocales.Count > 0 Then
            Dim sTuv As PslSourceString
            For i = 1 To srn.Size

                sTuv = srn.String(i)

                If pUtil.isUnextractState(pUtil.getSourceState(sTuv)) Then
                    Continue For
                End If

                If sTuv.Text.Length = 0 Then
                    Continue For
                End If


                For Each language As PslLanguage In usedlanguages

                    If start = False Then
                        handleStopped()
                        Exit Sub
                    End If

                    Dim trn As PslTransList
                    trn = srn2trn.Item(srn.ListID.ToString + "_" + language.LangID.ToString)
                    Dim tuv As PslTransString = getTarget(srn.String(i), trn)

                    If tuv Is Nothing Then
                        Continue For
                    End If

                    If pUtil.isUnextractState(pUtil.getState(tuv)) Then
                        Continue For
                    End If

                    Dim textWriter As XmlTextWriter
                    textWriter = textWriters.Item(language.LangID)

                    Dim resname As String
                    resname = sTuv.IDName

                    If resname.Length = 0 Then
                        resname = sTuv.ID
                    End If

                    textWriter.WriteStartElement("trans-unit")
                    textWriter.WriteAttributeString("resname", "", resname)
                    textWriter.WriteAttributeString("id", "", sTuv.Number.ToString)

                    WriteSourceTuv(sTuv, textWriter)
                    writeTargetTuv(tuv, textWriter)

                    If pUtil.needsExtractFuzzyMatch(tuv) Then
                        writeMatches(tuv.TransList, sTuv.Text, tuv.Text, textWriter)
                    End If

                    textWriter.WriteEndElement()
                Next
            Next
        End If

        writeFileFoot()
    End Sub



    Private Function getImCommandLanguage()

        Dim content As String
        content = fUtil.readFile(path)
        Dim ls As String() = Strings.Split(content, "|")
        Dim result As HashSet(Of String) = New HashSet(Of String)

        For Each l As String In ls

            If l.Trim.Length > 0 Then
                result.Add(l.Trim)
            End If

        Next
        Return result

    End Function

    Private Function isEmptyFile(ByVal path As String)

        If FileLen(path) < 1024 Then
            Dim content As String = fUtil.readFile(path)
            If content.IndexOf("<body />") > 0 Then
                Return True
            End If
        End If

        Return False

    End Function


    Private Sub convertLpu2Xliff()
        Dim filePath As String
        Dim failed = False

        textWriters.Clear()

        filePath = path.Substring(0, path.Length - Context.IM_COMMAND.ToString.Length)
        fUtil.addMsg(Context.START_CONVERT + filePath)

        Dim imCommandLanguage As HashSet(Of String)
        imCommandLanguage = getImCommandLanguage()

        Dim root As String = filePath + ".xliffs"
        fUtil.removeFolder(root)
        MkDir(root)

        PSL.Projects.Open(filePath)
        Dim prj As PslProject
        prj = PSL.ActiveProject

        Try
            If prj Is Nothing Then

                If Windows.findOldVersion Then

                    fUtil.addMsg(Context.OLD_VERSION)
                    fUtil.addStatus(filePath, 1, Context.OLD_VERSION)
                    Windows.findOldVersion = False

                Else

                    fUtil.addMsg(Context.CAN_NOT_OPEN + filePath)
                    fUtil.addStatus(filePath, 1, Context.CAN_NOT_OPEN + filePath)

                End If

                Exit Sub

            End If

            lancnt = prj.Languages.Count

            languages.Clear()
            For i = 1 To lancnt
                Dim language As PslLanguage
                language = prj.Languages.Item(i)
                Dim languageString As String = getLanguage(language.LangID)

                If imCommandLanguage.Count = 0 Or imCommandLanguage.Contains(languageString) Then
                    languages.Add(language)
                    MkDir(root + "/" + languageString)
                End If

            Next

            Dim sl As PslSourceLists
            sl = prj.SourceLists

            Dim fileNames As HashSet(Of String) = New HashSet(Of String)

            For i = 1 To sl.Count

                existLocales.Clear()

                Dim name As String = fUtil.getFileName(sl.Item(i).SourceFile)
                Dim sufferName As String = ""

                Try
                    fUtil.addMsg(Context.EXTRACT_FILE + name)

                    Dim index As Integer = 1
                    While fileNames.Contains(name + sufferName)
                        sufferName = "(" + index.ToString + ")"
                        index = index + 1
                    End While

                    fileNames.Add(name + sufferName)

                    For Each language As PslLanguage In languages
                        Dim fileName As String
                        fileName = root + "/" + getLanguage(language.LangID) + "/" + name + sufferName + ".xliff"
                        Dim writer As XmlTextWriter = New XmlTextWriter(fileName, System.Text.Encoding.UTF8)
                        writer.Formatting = Formatting.Indented
                        textWriters.Add(language.LangID, writer)
                    Next

                    writeHead()
                    handlePslSourceList(sl.Item(i), prj)
                    writeFoot()

                Finally

                    For Each textWriter As XmlTextWriter In textWriters.Values
                        textWriter.Close()
                    Next

                    textWriters.Clear()
                End Try

                For Each language As PslLanguage In languages
                    Dim fileName As String

                    If (existLocales.Contains(language.LangID)) Then
                        Continue For
                    End If

                    fileName = root + "/" + getLanguage(language.LangID) + "/" + name + sufferName + ".xliff"

                    If isEmptyFile(fileName) = True Then
                        Kill(fileName)
                        fileNames.Remove(name + sufferName)
                    End If
                Next
            Next

            fUtil.addStatus(filePath, 0, "")
            fUtil.addMsg(Context.CONVERSION_FINISHED + root)
        Catch ex As Exception
            If start = False Then
                fUtil.addStatus(filePath, 1, Context.CONVERSION_STOP)
            Else
                fUtil.addStatus(filePath, 1, ex.Message)
                fUtil.addMsg(Context.CONVERSION_FAILED + ex.Message)
            End If

        Finally

            If Not (prj Is Nothing) And start = True Then
                prj.Close()
            End If

        End Try

    End Sub
End Class
