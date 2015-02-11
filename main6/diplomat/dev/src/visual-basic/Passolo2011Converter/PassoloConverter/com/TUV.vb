Public Class TUV
    Dim language As String
    Dim content As String

    Public Function getLanguage()
        Return language
    End Function

    Public Sub setLanguage(ByVal l As String)
        language = l
    End Sub

    Public Function getContent()
        Return content
    End Function

    Public Sub setContent(ByVal c As String)
        content = c
    End Sub
End Class
