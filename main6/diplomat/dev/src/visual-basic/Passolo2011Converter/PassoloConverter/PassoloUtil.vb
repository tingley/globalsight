Imports PassoloU

Public Class PassoloUtil
    Public Function getState(ByVal pts As PslTransString)

        If pts.TransList.ExportFile.Length > 0 Then
            Return "readOnly"
        End If

        If "DialogFont" = pts.Type Then
            Return "readOnly"
        End If


        If (pts.State(PslStates.pslStateReadOnly)) Then
            Return "readOnly"
        ElseIf (pts.State(PslStates.pslStateLocked)) Then
            Return "Locked"
        ElseIf (pts.State(PslStates.pslStateHidden)) Then
            Return "Hidden"
        ElseIf (pts.State(PslStates.pslStateCorrection)) Then
            Return "Translated and reviewed"
        ElseIf (pts.State(PslStates.pslStateTranslated)) Then

            If pts.State(PslStates.pslStateReview) Then
                Return "Translated"
            End If

            Return "Translated and reviewed"
        ElseIf (pts.State(PslStates.pslStateReview)) Then
            Return "Translated"
        End If

        Return "New"
    End Function

    Public Function encodeString(ByVal s As String)
        s = s.Replace("\", "\\")
        's = s.Replace("\n", "\\n")
        s = s.Replace(Chr(13), "\r")
        's = s.Replace(Chr(10), "\n")

        Return s
    End Function

    Public Function decodeString(ByVal s As String)
        s = s.Replace("\r", Chr(13))
        's = s.Replace("\n", Chr(10))

        s = s.Replace("\\", "\")
        's = s.Replace("\\n", "\n")
        Return s
    End Function

    Public Function getSourceState(ByVal pts As PslSourceString)

        If (pts.State(PslStates.pslStateReadOnly)) Then
            Return "readOnly"
        ElseIf (pts.State(PslStates.pslStateLocked)) Then
            Return "Locked"
        ElseIf (pts.State(PslStates.pslStateHidden)) Then
            Return "Hidden"
        ElseIf (pts.State(PslStates.pslStateCorrection)) Then
            Return "Translated and reviewed"
        ElseIf (pts.State(PslStates.pslStateTranslated)) Then

            If pts.State(PslStates.pslStateReview) Then
                Return "Translated"
            End If

            Return "Translated and reviewed"
        ElseIf (pts.State(PslStates.pslStateReview)) Then
            Return "Translated"
        End If

        Return "New"
    End Function

    Public Function isUnextractState(ByVal state As String)
        Return "readOnly" = state Or "Hidden" = state
    End Function

    Public Function needsExtractFuzzyMatch(ByVal pts As PslTransString)
        Dim state As String = getState(pts)
        Return "New" = state
    End Function

End Class
