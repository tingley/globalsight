Imports PassoloU
Imports System.Threading

Public Class Windows
    Dim iUtil As ImportUtil = New ImportUtil
    Dim eUtil As ExportUtil = New ExportUtil
    Dim fUtil As FileUtil = New FileUtil
    Dim PSL As PassoloApp

    Public Shared findOldVersion = False

    Private Sub Button2_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Button2.Click
        StartConverter()
    End Sub

    Private Sub StartConverter()
        Dim path As String
        path = TextBox1.Text.Trim

        If path = "" Or Dir(path, vbDirectory) = "" Then
            MsgBox(Context.SET_DIRECTORY)
            Return
        End If

        Button2.Enabled = False
        Button3.Enabled = True

        Context.CONVERTER_ROOT = path
        setStart(True)

        fUtil.createFolder(Context.CONVERTER_ROOT + "\" + Context.PASSOLO)
        fUtil.createFolder(Context.CONVERTER_ROOT + "\" + Context.PASSOLO + "\" + Context.IMPORT)
        fUtil.createFolder(Context.CONVERTER_ROOT + "\" + Context.PASSOLO + "\" + Context.EXPORT)

        PSL = CreateObject("PASSOLO.Application")

        Dim mythread As Thread
        mythread = New Thread(New ThreadStart(AddressOf clickDialog))
        mythread.Start()

        fUtil.addMsg(Context.STARTED)
        fUtil.addMsg(Context.WITHCH_DIRECTORY + path + "\" + Context.PASSOLO)
        iUtil.setPassoloApp(PSL)
        iUtil.startConvert()
        eUtil.setPassoloApp(PSL)
        eUtil.startConvert()
    End Sub

    Private Const BM_CLICK = &HF5
    Public Declare Function SendMessage Lib "user32" Alias "SendMessageA" (ByVal hWnd As Integer, ByVal wMsg As Integer, ByVal wParam As Integer, ByVal lParam As Integer) As Integer
    Public Declare Auto Function FindWindow Lib "user32.dll" Alias "FindWindow" (ByVal lpClassName As String, ByVal lpWindowName As String) As Integer
    Declare Function FindWindowEx Lib "user32" Alias "FindWindowExA" (ByVal hWnd1 As Integer, ByVal hWnd2 As Integer, ByVal lpsz1 As String, ByVal lpsz2 As String) As Integer

    Private Sub clickDialog()
        While (iUtil.getStart)

            Dim title As String = "Open Project"
            Dim bName As String = "Cancel"

            Dim hwnd As Integer
            hwnd = FindWindow(vbNullString, title)

            If (hwnd <> 0) Then

                Dim hwnd2 As Integer
                hwnd2 = FindWindowEx(hwnd, 0, vbNullString, bName)

                If (hwnd2 <> 0) Then
                    findOldVersion = True
                    SendMessage(hwnd2, BM_CLICK, 0, 0)
                End If

            End If

            Thread.Sleep(3000)

        End While
    End Sub

    Private Sub setStart(ByVal isStart As Boolean)
        iUtil.setStart(isStart)
        eUtil.setStart(isStart)
    End Sub


    Private Sub Button1_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Button1.Click
        FolderBrowserDialog1.ShowDialog()
        TextBox1.Text = FolderBrowserDialog1.SelectedPath
        My.MySettings.Default.Folder = TextBox1.Text
        My.MySettings.Default.Save()
    End Sub

    Private Sub Button3_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Button3.Click
        If Not (PSL Is Nothing) Then
            PSL.Quit()
        End If
        setStart(False)

        Button3.Enabled = False
        Button2.Enabled = True

        fUtil.addMsg(Context.IS_STOP)
    End Sub

    Private Sub Form1_FormClosed(ByVal sender As System.Object, ByVal e As System.Windows.Forms.FormClosedEventArgs) Handles MyBase.FormClosed
        setStart(False)

        Button3.Enabled = False
        Button2.Enabled = True

    End Sub

    Private Sub Windows_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load

        TextBox1.Text = My.Settings.Folder
        CBAutoStart.Checked = My.Settings.AutoStart

        If My.Settings.AutoStart Then
            Dim t As New Thread(AddressOf AutoStart)
            t.Start()
        End If
    End Sub

    Private Sub CBAutoStart_CheckedChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles CBAutoStart.CheckedChanged
        My.Settings.AutoStart = CBAutoStart.Checked
        My.Settings.Save()
    End Sub

    Public Sub AutoStart()
        CBAutoStart.Checked = My.Settings.AutoStart
        Dim dir As String = My.MySettings.Default.Folder

        If "".Equals(dir) Then
            Return
        End If

        Dim oriText As String = CBAutoStart.Text
        CBAutoStart.Text = "Auto Starting..."
        CBAutoStart.Enabled = False
        StartConverter()
        CBAutoStart.Text = oriText
        CBAutoStart.Enabled = True
    End Sub
End Class
