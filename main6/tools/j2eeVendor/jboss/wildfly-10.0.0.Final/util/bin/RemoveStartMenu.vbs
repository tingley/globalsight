On Error Resume Next

'Create a WshShell Object
Set oShell = Wscript.CreateObject("Wscript.Shell")
Set fso = Wscript.CreateObject("Scripting.FileSystemObject")

'Get Start Menu Folder Path
progFolder = oShell.specialFolders("AllUsersPrograms")
progFolder = progFolder & "\\GlobalSight"

If (fso.FolderExists(progFolder)) Then
    fso.DeleteFolder(progFolder)
End If
