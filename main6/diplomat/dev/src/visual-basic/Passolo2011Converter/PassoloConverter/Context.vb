Public Class Context
    Public Shared CONVERTER_ROOT As String
    Dim start = False

    Public Shared IM_COMMAND = ".im_command"
    Public Shared EX_COMMAND = ".ex_command"
    Public Shared STATUS = ".status"
    Public Shared PASSOLO = "passolo"
    Public Shared IMPORT = "import"
    Public Shared EXPORT = "export"

    Public Shared STARTED = "Passolo 2011 Converter starting up"
    Public Shared IS_STOP = "Passolo 2011 Converter shutting down"
    Public Shared WITHCH_DIRECTORY = "Creating and starting threads to watch directory "
    Public Shared SET_DIRECTORY = "Please set the directory first."
    Public Shared START_CONVERT = "Processing file "
    Public Shared CAN_NOT_OPEN = "Can not open file "
    Public Shared OLD_VERSION = "The LPU file was created by older version of Passolo."

    Public Shared CONVERSION_STOP = "Passolo 2011 Converter has been shut down"
    Public Shared EXTRACT_FILE = "Extracting "
    Public Shared CONVERSION_FINISHED = "Converted successfully to "
    Public Shared CONVERSION_FAILED = "Conversion failed. Error message: "

    Public Shared START_UPDATE = "Processing "
    Public Shared UPDATE_FINISHED = "Converted successfully to "
    Public Shared UPDATE_FAILED = "Conversion failed. Error message: "
    Public Shared UPDATE_FILE = "Updating file "
    Public Shared NO_XLIFF_FILE = "Can not find any xliff files"

    Public Function getStart()
        Return start
    End Function

    Public Sub setStart(ByVal startValue As Boolean)

        If start = startValue Then
            Return
        End If

        start = startValue

        If start = False Then
            addMsg(IS_STOP)
        Else
            addMsg(STARTED)
        End If

    End Sub

    Public Sub addMsg(ByVal s)

        Dim SW As System.IO.StreamWriter
        SW = New System.IO.StreamWriter(CONVERTER_ROOT + "\passolo2011Converter.log", True)
        SW.WriteLine(s)
        SW.Flush()
        SW.Close()
    End Sub
End Class
