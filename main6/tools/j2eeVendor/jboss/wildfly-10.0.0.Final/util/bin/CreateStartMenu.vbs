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

fso.CreateFolder(progFolder)

'Create start link
Set oLink = oShell.CreateShortcut(progFolder & "\\Start GlobalSight.lnk")
oLink.TargetPath = "net"
oLink.Arguments = "start ""GlobalSight Service"""
oLink.Save

'Create stop link
Set oLink = oShell.CreateShortcut(progFolder & "\\Stop GlobalSight.lnk")
oLink.TargetPath = "net"
oLink.Arguments = "stop ""GlobalSight Service"""
oLink.Save

'Create restart link
Set oLink = oShell.CreateShortcut(progFolder & "\\Restart GlobalSight.lnk")
oLink.TargetPath = fso.getFile(Wscript.scriptfullname).ParentFolder & "\\RestartGS.bat"
'oLink.Arguments = "stop ""GlobalSight Service"" && net start ""GlobalSight Service"""
oLink.Save

'Clean up the WshShortcut Object
Set oShellLink = Nothing
