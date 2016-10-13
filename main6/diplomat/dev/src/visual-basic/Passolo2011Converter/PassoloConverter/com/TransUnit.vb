Public Class TransUnit
    Dim id As String
    Dim tuvs As List(Of TUV) = New List(Of TUV)

    Public Function getId()
        Return id
    End Function

    Public Sub setId(ByVal i As String)
        id = i
    End Sub

    Public Function getTuvs()
        Return tuvs
    End Function

    Public Sub addTuv(ByVal tuv As TUV)
        tuvs.Add(tuv)
    End Sub

    Public Function hasTarget()
        Return tuvs.Count > 0
    End Function

    Public Function getTuv(ByVal language As String)

        For Each t As TUV In tuvs

            If t.getLanguage = language Then
                Return t
            End If

        Next

        Return Nothing
    End Function
End Class
