Imports System.Xml
Imports PassoloU
Imports System.Threading

Public Class ExportUtil

    Dim PSL As PassoloApp
    Dim mythread As Thread
    Dim start As Boolean = False
    Dim pUtil As PassoloUtil = New PassoloUtil
    Dim fUtil As FileUtil = New FileUtil

    Public Sub setPassoloApp(ByVal passoloApp As PassoloApp)
        PSL = passoloApp
    End Sub

    Public Function getStart()
        Return start
    End Function

    Public Sub setStart(ByVal startValue As Boolean)
        start = startValue
    End Sub

    Public Function startConvert()

        mythread = New Thread(New ThreadStart(AddressOf convertThread))
        mythread.Start()
        Return True

    End Function

    Private Function getExportRoot()
        Return Context.CONVERTER_ROOT + "\" + Context.PASSOLO + "\" + Context.EXPORT
    End Function

    Private Sub convertThread()
        Dim flist As System.Collections.ObjectModel.ReadOnlyCollection(Of String)
        Dim x As Integer
        Dim i As Integer, tmpstr1 As String

        While start
            flist = Microsoft.VisualBasic.FileIO.FileSystem.GetFiles(getExportRoot(), FileIO.SearchOption.SearchAllSubDirectories, "*" + Context.EX_COMMAND)
            x = flist.Count

            For i = 0 To x - 1

                If start = False Then
                    Return
                End If

                tmpstr1 = flist.Item(i)
                Try
                    updateXliffToLpu(tmpstr1)
                Catch ex As Exception
                    fUtil.addMsg(Context.UPDATE_FAILED + ex.Message)
                    fUtil.addStatus(tmpstr1.Substring(0, tmpstr1.Length - Context.EX_COMMAND.ToString.Length), 1, ex.Message)
                End Try

                Kill(tmpstr1)

            Next

            Thread.Sleep(1000)
        End While

    End Sub

    Private Function getFs(ByVal filePath)

        Dim sId As String
        Dim tText As String
        Dim tLang As String

        Dim fs As List(Of XliffFile)
        fs = New List(Of XliffFile)

        Dim doc As XmlDocument
        Dim files As XmlNodeList
        Dim nodes As XmlNodeList
        Dim node As XmlNode

        Dim root As String = filePath + ".xliffs"

        Dim flist As System.Collections.ObjectModel.ReadOnlyCollection(Of String)
        flist = Microsoft.VisualBasic.FileIO.FileSystem.GetFiles(root, FileIO.SearchOption.SearchAllSubDirectories, "*.xliff")

        For i = 0 To flist.Count - 1
            Dim path As String
            path = flist.Item(i)

            doc = New XmlDocument()
            doc.Load(path)

            files = doc.GetElementsByTagName("file")

            For Each file As XmlElement In files

                Dim f As XliffFile
                f = getF(fs, file.GetAttribute("tool-id"))

                If f Is Nothing Then
                    f = New XliffFile()
                    f.setId(file.GetAttribute("tool-id"))
                End If

                nodes = file.GetElementsByTagName("trans-unit")
                For Each h As XmlElement In nodes

                    Dim tu As TransUnit = New TransUnit()

                    sId = h.GetAttribute("id")
                    node = h.FirstChild()
                    node = node.NextSibling
                    tu.setId(sId)

                    While True

                        If node Is Nothing Then
                            Exit While
                        End If

                        If "alt-trans" = node.Name Then
                            Exit While
                        End If


                        tLang = node.Attributes.GetNamedItem("xml:lang").InnerText
                        tText = node.InnerText

                        Dim tuv As TUV = New TUV
                        tuv.setLanguage(tLang)
                        tuv.setContent(tText)

                        tu.addTuv(tuv)

                        node = node.NextSibling
                    End While

                    If tu.hasTarget Then
                        f.addTu(tu)
                    End If
                Next

                fs.Add(f)
            Next
        Next

        Return fs
    End Function

    Private Function getTu(ByVal tus As List(Of TransUnit), ByVal id As String)
        For Each tu As TransUnit In tus
            If tu.getId = id Then
                Return tu
            End If
        Next

        Return Nothing
    End Function

    Private Function getF(ByVal fs As List(Of XliffFile), ByVal id As String)
        For Each f As XliffFile In fs

            If f.getId = id Then
                Return f
            End If
        Next

        Return Nothing
    End Function

    Private Sub updateXliffToLpu(ByVal path As String)
        Dim filePath As String
        Dim failed = False

        filePath = path.Substring(0, path.Length - Context.IM_COMMAND.ToString.Length)
        fUtil.addMsg(Context.START_UPDATE + filePath + ".xliffs")

        Dim fs As List(Of XliffFile)
        fs = getFs(filePath)

        If fs.Count > 0 Then

            PSL.Projects.Open(filePath)
            Dim prj As PslProject
            prj = PSL.ActiveProject
            Try
                If prj Is Nothing Then
                    fUtil.addMsg(Context.CAN_NOT_OPEN + filePath)
                    fUtil.addStatus(filePath, 1, Context.CAN_NOT_OPEN + filePath)
                    Exit Sub
                End If

                Dim sl As PslSourceLists
                sl = prj.SourceLists

                Dim trn As PslTransList
                Dim srn As PslSourceList

                prj.SuspendSaving()

                For index As Integer = 1 To sl.Count

                    srn = sl.Item(index)

                    Dim f As XliffFile
                    f = getF(fs, srn.ListID)

                    If f Is Nothing Then
                        Continue For
                    End If

                    Dim name As String = fUtil.getFileName(srn.SourceFile)
                    fUtil.addMsg(Context.UPDATE_FILE + name)

                    Dim tus As List(Of TransUnit)
                    tus = f.getTus()

                    For Each tu As TransUnit In tus

                        If start = False Then
                            fUtil.addMsg(Context.CONVERSION_STOP)
                            fUtil.addStatus(filePath, 1, Context.CONVERSION_STOP)
                            Exit Sub
                        End If

                        Dim pstuv As PslSourceString
                        pstuv = srn.String(Val(tu.getId), 3)

                        If pstuv Is Nothing Then
                            Continue For
                        End If

                        For i = 1 To prj.Languages.Count
                            Dim language As PslLanguage
                            language = prj.Languages.Item(i)

                            Dim t As TUV
                            t = tu.getTuv(PSL.GetLangCode(language.LangID, 8))
                            If t Is Nothing Then
                                Continue For
                            End If

                            trn = prj.TransLists.Item(srn, language.LangID)
                            If trn Is Nothing Then
                                Continue For
                            End If

                            Dim pttuv As PslTransString
                            pttuv = trn.String(pstuv.Number, 3)

                            Dim content As String = pUtil.decodeString(t.getContent())

                            If pttuv.Text <> content Then
                                pttuv.Text = content

                                If needChangeState(pttuv) Then
                                    'PslTransString.State
                                    'set to for review
                                    pttuv.State(6) = True
                                End If

                                trn.Save()
                            End If

                        Next
                    Next

                    'For i = 1 To srn.Size

                    '    If start = False Then
                    '        fUtil.addMsg(Context.CONVERSION_STOP)
                    '        fUtil.addStatus(filePath, 1, Context.CONVERSION_STOP)
                    '        Exit Sub
                    '    End If

                    '    Dim tu As TransUnit
                    '    tu = getTu(tus, srn.String(i).Number.ToString)

                    '    If tu Is Nothing Then
                    '        Continue For
                    '    End If

                    '    For j = 1 To prj.TransLists.Count

                    '        If start = False Then
                    '            fUtil.addMsg(Context.CONVERSION_STOP)
                    '            fUtil.addStatus(filePath, 1, Context.CONVERSION_STOP)
                    '            Exit Sub
                    '        End If

                    '        trn = prj.TransLists.Item(j)

                    '        If trn.SourceList.ListID <> srn.ListID Then
                    '            Continue For
                    '        End If

                    '        Dim tuv As PslTransString
                    '        tuv = trn.String(srn.String(i).Number, 3)

                    '        If tuv Is Nothing Then
                    '            Continue For
                    '        End If

                    '        Dim t As TUV
                    '        t = tu.getTuv(PSL.GetLangCode(trn.Language.LangID, 8))

                    '        If t Is Nothing Then
                    '            Continue For
                    '        End If

                    '        Dim pts As PslTransString
                    '        pts = trn.String(i)
                    '        Dim content As String = pUtil.decodeString(t.getContent())

                    '        If pts.Text <> content Then
                    '            pts.Text = content

                    '            If needChangeState(pts) Then
                    '                'PslTransString.State
                    '                'set to for review
                    '                pts.State(6) = True
                    '            End If

                    '            trn.Save()
                    '        End If

                    '    Next

                    'Next

                Next

                prj.ResumeSaving()

                fUtil.addMsg(Context.UPDATE_FINISHED + filePath)
                fUtil.addStatus(filePath, 0, "")
            Catch ex As Exception
                fUtil.addStatus(filePath, 1, ex.Message)
                fUtil.addMsg(Context.UPDATE_FAILED + ex.Message)
            Finally
                If Not (prj Is Nothing) And start = True Then
                    prj.Close()
                End If
            End Try
        Else
            fUtil.addStatus(filePath, 1, Context.NO_XLIFF_FILE)
            fUtil.addMsg(Context.UPDATE_FAILED + Context.NO_XLIFF_FILE)

        End If

    End Sub

    Private Function needChangeState(ByVal pts As PslTransString)
        Dim tState As String
        tState = pUtil.getState(pts)
        If "New" = tState Or "Translated and reviewed" = tState Then
            Return True
        End If

        Return False
    End Function

End Class
