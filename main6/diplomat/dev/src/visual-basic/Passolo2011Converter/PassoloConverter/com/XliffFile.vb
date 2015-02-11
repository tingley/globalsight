Public Class XliffFile
    Dim id As String
    Dim tus As List(Of TransUnit) = New List(Of TransUnit)

    Public Function getId()
        Return id
    End Function

    Public Sub setId(ByVal i As String)
        id = i
    End Sub

    Public Function getTus()
        Return tus
    End Function

    Public Sub addTu(ByVal tu As TransUnit)

        For Each t As TransUnit In tus

            If t.getId = tu.getId Then
                For Each t2 As TUV In tu.getTuvs
                    t.addTuv(t2)
                Next

                Exit Sub
            End If
        Next

        tus.Add(tu)
    End Sub

End Class
