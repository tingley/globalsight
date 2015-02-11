function ShowError(message)
{
    window.showModalDialog('envoy/terminology/viewer/error.jsp', message,
        'center:yes; help:no; resizable:yes; status:no; ' +
        'dialogWidth: 450px; dialogHeight: 300px;');
}
