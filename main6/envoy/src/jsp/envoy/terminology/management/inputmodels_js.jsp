// -*- mode: javascript -*-
<%@ page
    contentType="text/javascript; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.terminology.IUserdataManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String userId = PageHandler.getUser(session).getUserId();

String jsmsg_select_object_to_modify = bundle.getString("jsmsg_please_select_a_row");
String jsmsg_select_object_to_remove = bundle.getString("jsmsg_please_select_a_row");
String jsmsg_select_object_to_make_default = bundle.getString("jsmsg_please_select_a_row");

%>


function findSelectedRadioButton()
{
    var result = findSelectedRadioButton1(systemForm);

    if (!result)
    {
        result = findSelectedRadioButton1(userForm);
    }

    return result;
}

function findSelectedRadioButton1(form)
{
    var result = null;

    if (form.checkbox)
    {
        if (form.checkbox.length)
        {
            for (var i = 0; i < form.checkbox.length; i++)
            {
                if (form.checkbox[i].checked == true)
                {
                    result = form.checkbox[i].value;
                    break;
                }
            }
        }
        else
        {
            if (form.checkbox.checked == true)
            {
                result = form.checkbox.value;
            }
        }
    }

    return result;
}

function haveObject(p_objects, p_object)
{
    for (var i = 0; i < p_objects.length; i++)
    {
        var o = p_objects[i];

        <%-- This is magic. Calling toLowerCase() on a string created
             in a different JS context (the modal dialog) will fail. --%>
        if (p_object.isUserObject() && o.isUserObject())
        {
            var n1 = new String(o.name);
            var n2 = new String(p_object.name);
            var u1 = new String(o.username);
            var u2 = new String(p_object.username);

            if (u1.toLowerCase() == u2.toLowerCase() &&
                n1.toLowerCase() == n2.toLowerCase())
            {
                return true;
            }
        }
        else if (!p_object.isUserObject() && !o.isUserObject())
        {
            var n1 = new String(o.name);
            var n2 = new String(p_object.name);

            if (n1.toLowerCase() == n2.toLowerCase())
            {
                return true;
            }
        }
    }

    return false;
}

function newObject(p_system)
{
    var args = new UserObject(g_objectType,
        (p_system ? "" : "<%=userId%>"), "", "", false);

    var o = window.showModalDialog(g_editorUrl, args,
        "dialogHeight:500px; dialogWidth:600px; center:yes; " +
        "resizable:no; status:no;");

    while (o != null && o.value)
    {
        if (haveObject(aObjects, o))
        {
            alert("<%=bundle.getString("jsmsg_object_exists") %>");

            o = window.showModalDialog(g_editorUrl, args,
                "dialogHeight:500px; dialogWidth:600px; center:yes; " +
                "resizable:no; status:no;");
        }
        else
        {
            try
            {
                sendUserdataRequest(
                    "<%=WebAppConstants.TERMBASE_ACTION_CREATE_OBJECT%>",
                    o.type, o.username, o.name, o.value);
                window.location.reload(); 
            }
            catch (error)
            {
                error.message = "Input Model could not be created";
                showError(error);
            }

            return;
        }
    }
}

function modifyObject()
{
    var id = findSelectedRadioButton();
    if (!id)
    {
        alert("<%=jsmsg_select_object_to_modify%>");
    }
    else
    {
        var object = aObjects[id - 1];

        if (!object.value)
        {
            try
            {
                var resp = sendUserdataRequest(
                    "<%=WebAppConstants.TERMBASE_ACTION_LOAD_OBJECT%>",
                    object.type, object.username, object.name, "");
                //alert(resp.xml);
                object.setValue(resp.xml);
            }
            catch (error)
            {
                error.message = "Input Model could not be loaded";
                showError(error);
                return;
            }
        }

        var args = new UserObject(
            object.type, object.username, object.name, object.value,
            object.isDefault);

        var o = window.showModalDialog(g_editorUrl, args,
            "dialogHeight:500px; dialogWidth:600px; center:yes; " +
            "resizable:no; status:no;");
       
        // TODO: check for duplicates
        if (o != null && o.value)
        {
            try
            {
                if (o.name == object.name)
                {
                    sendUserdataRequest(
                        "<%=WebAppConstants.TERMBASE_ACTION_MODIFY_OBJECT%>",
                        o.type, o.username, o.name, o.value);
                }
                else
                {
                    sendUserdataRequest(
                        "<%=WebAppConstants.TERMBASE_ACTION_REMOVE_OBJECT%>",
                        object.type, object.username, object.name, "");
                    sendUserdataRequest(
                        "<%=WebAppConstants.TERMBASE_ACTION_CREATE_OBJECT%>",
                        o.type, o.username, o.name, o.value);
                }

                //window.navigate(window.location.href);
                window.location.reload(); 

            }
            catch (error)
            {
                error.message = "Input Model could not be modified";
                showError(error);
            }

            return;
        }
    }
}

function removeObject()
{
    var id = findSelectedRadioButton();
    if (!id)
    {
        alert("<%=jsmsg_select_object_to_remove%>");
        return false;
    }
    else if (confirm("<%=bundle.getString("jsmsg_remove_object_confirm") %>"))
    {
        var o = aObjects[id - 1];

        try
        {
            sendUserdataRequest(
                "<%=WebAppConstants.TERMBASE_ACTION_REMOVE_OBJECT%>",
                o.type, o.username, o.name, "");
            aObjects.splice(id - 1, 1);
        }
        catch (error)
        {
            error.message = "Input Model could not be removed";
            showError(error);
        }

        // delete object on server and reload page.
        window.location.reload(); 
    }
}

function makeDefaultObject()
{
    // can only make system-wide objects the default
    var id = findSelectedRadioButton1(systemForm);
    if (!id)
    {
        alert("<%=jsmsg_select_object_to_make_default%>");
        return false;
    }
    else
    {
        var o = aObjects[id - 1];

        try
        {
            sendUserdataRequest(
                "<%=WebAppConstants.TERMBASE_ACTION_MAKE_DEFAULT_OBJECT%>",
                o.type, o.username, o.name);
        }
        catch (error)
        {
            error.message = "Input Model could not be set as default";
            showError(error);
        }

        // delete object on server and reload page.
        window.location.reload(); 
    }
}

function unsetDefaultObject()
{
    try
    {
        sendUserdataRequest(
            "<%=WebAppConstants.TERMBASE_ACTION_UNSET_DEFAULT_OBJECT%>",
            g_objectType, "", "");
    }
    catch (error)
    {
        error.message = "Input Model could not be unset";
        showError(error);
    }

    // delete object on server and reload page.
    window.location.reload(); 
}

