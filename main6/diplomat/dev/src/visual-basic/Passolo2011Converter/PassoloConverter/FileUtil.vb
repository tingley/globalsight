Public Class FileUtil
    Public Sub addMsg(ByVal s As String)

        Dim SW As System.IO.StreamWriter
        SW = New System.IO.StreamWriter(Context.CONVERTER_ROOT + "\" + Context.PASSOLO + "\PassoloConverter.log", True)

        s = s.Trim
        If s.Length > 0 Then
            s = Now.ToString + "; " + s
        End If

        SW.WriteLine(s)
        SW.Flush()
        SW.Close()

    End Sub

    Public Sub addStatus(ByVal path As String, ByVal status As Integer, ByVal msg As String)
        Dim SW As System.IO.StreamWriter
        SW = New System.IO.StreamWriter(path + ".status", False)
        SW.WriteLine(status)
        SW.WriteLine(msg)
        SW.Flush()
        SW.Close()
    End Sub

    Public Sub removeFolder(ByVal root As String)
        If Not (Dir(root, FileAttribute.Directory) = "") Then
            Dim flist As System.Collections.ObjectModel.ReadOnlyCollection(Of String)
            flist = Microsoft.VisualBasic.FileIO.FileSystem.GetFiles(root, FileIO.SearchOption.SearchAllSubDirectories, "*.*")
            If flist.Count > 0 Then
                For Each f As String In flist
                    Kill(f)
                Next
            End If

            Dim fl As String()
            fl = System.IO.Directory.GetDirectories(root)
            For Each f As String In fl
                RmDir(f)
            Next

            RmDir(root)
        End If
    End Sub

    Public Function getFileName(ByVal filePath As String)
        Dim index As Integer = Strings.InStrRev(filePath, "\")
        Dim name As String = Strings.Right(filePath, Strings.Len(filePath) - index)
        Return name
    End Function

    Public Function getFileSize(ByVal filePath As String)
        Dim l As Long = FileLen(filePath)
        If l > 1024 Then
            l = l / 1024

            If l > 1024 Then
                l = l / 1024
                Return l.ToString + " MB"
            Else
                Return l.ToString + " KB"
            End If
        Else
            Return l.ToString
        End If
    End Function

    Public Sub createFolder(ByVal filePath As String)
        If Dir(filePath, vbDirectory) = "" Then
            MkDir(filePath)
        End If
    End Sub

    Public Function readFile(ByVal filePath As String)
        Return My.Computer.FileSystem.ReadAllText(filePath)
    End Function
End Class
