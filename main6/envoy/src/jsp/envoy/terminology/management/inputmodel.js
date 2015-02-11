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

function setDefaultButton(id) {
    var markId = "checkmark" + id;
    var checkMark = document.getElementById(markId);
    var isHasSetDefault = document.getElementById("isHasSetDefault").value;
    
    if(checkMark.value == "Y") {
        document.getElementById("idMakeDefault").disabled = true;
        document.getElementById("idUnsetDefault").disabled = false;
    }
    else if(checkMark.value == "N") {
        if(isHasSetDefault == "true") {
            document.getElementById("idMakeDefault").disabled = true;
        }
        else {
            document.getElementById("idMakeDefault").disabled = false;
        }
        document.getElementById("idUnsetDefault").disabled = true;
    }
}

function haveObject(p_objects, p_object)
{
    for (var i = 0; i < p_objects.length; i++)
    {
        var o = p_objects[i];

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
        (p_system ? "*" : userId), "", "", false);

    var o = window.showModalDialog(g_editorUrl, args,
        "dialogHeight:500px; dialogWidth:600px; center:yes; " +
        "resizable:no; status:no;");

    if (o != null && o.value)
    {
       createInputModel(o.type, o.username, o.name, o.value);
    }
}

function createInputModel(type, user, name, value) {

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:createAction,type:type,username:user,name:name,value:value},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     
        	     if (result == 'error')
               {
                   error.message = "Input Model could not be created";
                   showError(error);
               }
               else {
                  window.location.reload(); 
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function modifyInputModel(id, type, user, name, value) {

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:modifyModelAction,id:id, type:type,username:user,name:name,value:value},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     
        	     if (result == 'error')
               {
                   error.message = "Input Model could not be modified";
                   showError(error);
               }
               else {
                  window.location.reload(); 
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function loadToModifyInputModel(id) {

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:loadAction,id:id},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     
        	     if (result == 'error')
               {
                   error.message = "Input Model could not be loaded";
                   showError(error);
               }
               else {
                  var args = new UserObject(
                    returnData.type, returnData.userName, returnData.name, 
                    returnData.value, returnData.isDefault);

                  var o = window.showModalDialog(g_editorUrl, args,
                      "dialogHeight:500px; dialogWidth:600px; center:yes; " +
                      "resizable:no; status:no;");
       
                  if (o != null && o.value)
                  {
                      modifyInputModel(id, o.type, o.username, o.name, o.value);
                  }
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function modifyObject()
{
    var id = findSelectedRadioButton();
    if (!id)
    {
        alert(jsmsg_select);
    }
    else
    {
        loadToModifyInputModel(id);
    }
}

function removeInputModel(id) {

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:removeAction,id:id},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     
        	     if (result == 'error')
               {
                   alert("Input Model could not be removed");
               }
               else if(result == 'isSetDefault') {
                   alert("Default Model could not be removed, please first unset it!");
               }
               else {
                   window.location.reload(); 
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function removeObject()
{
    var id = findSelectedRadioButton();
    if (!id)
    {
        alert(jsmsg_select);
        return false;
    }
    else if (confirm(jsmsg_remove_object_confirm))
    {
        removeInputModel(id);
    }
}

function makeDefaultObject()
{
    // can only make system-wide objects the default
    var id = findSelectedRadioButton1(systemForm);
    if (!id)
    {
        alert(jsmsg_select);
        return false;
    }
    else
    {
        var o = aObjects[id - 1];

        try
        {
            makeDefaultModel(id);
        }
        catch (error)
        {
            error.message = "Input Model could not be set as default";
            showError(error);
        }
    }
}

function makeDefaultModel(id) {

    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:makeDefaultAction,id:id},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     
        	     if (result == 'error')
               {
                   error.message = "Input Model could not be unset";
                   showError(error);
               }
               else {
                  window.location.reload(); 
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function unsetDefaultObject() {
    var id = findSelectedRadioButton1(systemForm);
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:unsetDefaultAction,id:id},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.model;
        	     
        	     if (result == 'error')
               {
                   error.message = "Input Model could not be unset";
                   showError(error);
               }
               else {
                  window.location.reload(); 
               }
           }
       },
       error:function(error)
       {
       }
   });
}

function showError(error)
{
    window.showModalDialog("/globalsight/envoy/terminology/management/error.jsp",
        error,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}

// This should be turned into a MsgBox-style window with different
// icons for warnings, informational messages, errors etc.
function showWarning(message)
{
    window.showModalDialog("/globalsight/envoy/terminology/management/warning.jsp",
        message,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}
