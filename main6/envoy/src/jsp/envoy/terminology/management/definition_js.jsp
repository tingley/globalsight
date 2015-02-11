// -*- mode: javascript -*-
<%@ page
    contentType="text/javascript; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String jsmsg_removing_a_language_may_be_harmful =
  bundle.getString("jsmsg_removing_a_language_may_be_harmful");
String jsmsg_select_language_to_modify =
  bundle.getString("jsmsg_select_language_to_modify");
String jsmsg_no_field_selected = bundle.getString("jsmsg_no_field_selected");
String jsmsg_select_field_to_modify =
  bundle.getString("jsmsg_select_field_to_modify");
String jsmsg_select_field_to_remove =
  bundle.getString("jsmsg_select_field_to_remove");
String jsmsg_yes = bundle.getString("lb_yes");
String jsmsg_no  = bundle.getString("lb_no");
String jsmsg_remove  = bundle.getString("jsmsg_remove");
%>
/*
 * Copyright (c) 2000-2004 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

<%-- // Uses objects in management/objects_js.jsp --%>

var aLanguages = new Array();
var aFields    = new Array();
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

function findSelectedRadioButton(form)
{
    var result = null;

    // If more than one radio button is displayed, the length
    // attribute of the radio button array will be non-zero,
    // so find which one is checked
    if (form.checkbox)
    {
        if (form.checkbox.length)
        {
            for (i = 0; i < form.checkbox.length; i++)
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
            // If only one is displayed, there is no radio button array, so
            // just check if the single radio button is checked
            if (form.checkbox.checked == true)
            {
                result = form.checkbox.value;
            }
        }
    }

    return result;
}

function haveLanguage(p_languages, p_language)
{
    for (var i = 0; i < p_languages.length; i++)
    {
        var lang = p_languages[i];

        <%-- This is magic. Calling toLowerCase() on a string created
             in a different JS context (the modal dialog) will fail. --%>
        var n1 = new String(lang.name);
        var n2 = new String(p_language.name);

        if (n1.toLowerCase() == n2.toLowerCase())
        {
            return true;
        }
    }

    return false;
}

function newLanguage()
{
   var diaHeight,diaWidth;
   if(isFirefox)
   {
   		diaHeight = 190+"px";
   		diaWidth  = 480+"px";
   }
   else
   {
   		diaHeight = 210+"px";
   		diaWidth  = 440+"px";
   }
   
   var oLanguage = window.showModalDialog(
    "/globalsight/envoy/terminology/management/language.jsp", null,
        "dialogHeight:"+diaHeight+"; dialogWidth:"+diaWidth+"; center:yes; " +
            "resizable:no; status:no;");

    if (oLanguage != null)
    {
        if (haveLanguage(aLanguages, oLanguage))
        {
            alert("The language already exists.");
        }
        else
        {
            aLanguages.push(oLanguage);
        }

        showLanguages();
    }
}

function modifyLanguage()
{
    var langId = findSelectedRadioButton();
    if (!langId)
    {
        alert("<%=EditUtil.toJavascript(jsmsg_select_language_to_modify)%>");
    }
    else
    {
        var oLanguage = aLanguages[langId - 1];
        var oModifiedLanguage = window.showModalDialog(
            "/globalsight/envoy/terminology/management/language.jsp", oLanguage,
            "dialogHeight:150px; dialogWidth:350px; center:yes; " +
            "resizable:no; status:no;");

        // TODO: check for duplicates
        if (oModifiedLanguage != null)
        {
            aLanguages[langId - 1] = oModifiedLanguage;
            showLanguages();
        }
    }
}

function removeLanguage()
{
    var langId = findSelectedRadioButton(languagesForm);
    if (!langId)
    {
        alert("Please select a language to remove.");
        return false;
    }
    else
    {
        if (isModify && aLanguages[langId-1].exists)
        {
             var ok = window.confirm(
                "<%=EditUtil.toJavascript(jsmsg_removing_a_language_may_be_harmful)%>");
             if (!ok)
             {
                return;
            }
        }

        aLanguages.splice(langId - 1, 1);
        showLanguages();
    }
}

function showLanguages()
{
   var tbody = idLanguagesBody;

   for (i = tbody.rows.length; i > 0; --i)
   {
      tbody.deleteRow(i-1);
   }

   for (i = 0; i < aLanguages.length; ++i)
   {
      var bg = (i%2 == 0) ? "white" : "#EEEEEE";
      var oLanguage = aLanguages[i];
      var row, cell;
      row = tbody.insertRow(i);
      row.style.background = bg;
      cell = row.insertCell(0);
      cell.innerHTML = "<INPUT TYPE=RADIO NAME=checkbox VALUE=" + (i+1) + ">";
      cell = row.insertCell(1);
      cell.innerHTML = oLanguage.name;
      cell.setAttribute("id", (i + 1).toString(10));
      cell = row.insertCell(2);
      cell.innerHTML = oLanguage.locale;
   }
}

function showFields()
{
   var tbody = idFieldsBody;

   for (i = tbody.rows.length; i > 0; --i)
   {
      tbody.deleteRow(i-1);
   }

   for (i = 0; i < aFields.length; ++i)
   {
      var bg = (i%2 == 0) ? "white" : "#EEEEEE";
      var oField = aFields[i];
      var row, cell;
      row = tbody.insertRow(i);
      row.style.background = bg;
      cell = row.insertCell(0);
      cell.innerHTML = "<INPUT TYPE=RADIO NAME=checkbox VALUE=" + (i+1) + ">";
      cell = row.insertCell(1);
      cell.innerHTML = oField.getDisplayName();
      cell.setAttribute("id", (i + 1).toString(10));
      cell = row.insertCell(2);
      if (oField.isUserDefinedAttribute())
      {
          cell.innerHTML = "attr";
      }
      else if (oField.isUserDefinedText())
      {
          cell.innerHTML = "text";
      }
      else
      {
          cell.innerHTML = oField.getType();
      }
      cell = row.insertCell(3);
//        cell.innerHTML = (oField.system == true) ?
//          "<%=EditUtil.toJavascript(jsmsg_yes)%>" :
//          "<%=EditUtil.toJavascript(jsmsg_no)%>";
      cell.innerHTML = (oField.values ? oField.values : "\u00a0");
   }
}

function newField()
{
    var args = new FieldDialogArgs(aFields, null);
    var result = window.showModalDialog(
        "/globalsight/envoy/terminology/management/field.jsp", args,
        "dialogHeight:320px; dialogWidth:600px; center:yes; " +
        "resizable:no; status:no;");

    if (result != null)
    {
        // need a copy of the object to run scripts
        var oField = new Field(result.name, result.type, result.format,
            result.system, result.values);

        var msg = validateNewField(aFields, oField);

        if (msg)
        {
            alert(msg);
            return;
        }

        // assign new internal types to attribute and text fields
        assignFieldType(oField);

        // alert(oField);

        aFields.push(oField);

        showFields();
    }
}

function modifyField()
{
    var fldId = findSelectedRadioButton(fieldsForm);

    if (!fldId)
    {
        alert("<%=EditUtil.toJavascript(jsmsg_select_field_to_modify)%>");
    }
    else
    {
        var oField = aFields[fldId - 1];
        var args = new FieldDialogArgs(aFields, oField);
        var result = window.showModalDialog(
            "/globalsight/envoy/terminology/management/field.jsp", args,
            "dialogHeight:320px; dialogWidth:600px; center:yes; " +
            "resizable:no; status:no;");

        if (result != null)
        {
            // need a copy of the object to run scripts
            var oNewField = new Field(result.name, result.type, result.format,
                result.system, result.values);

            var msg = validateModifiedField(aFields, fldId - 1, oNewField);

            if (msg)
            {
                alert(msg);
                return;
            }

            // assign new internal types to attribute and text fields
            assignFieldType(oNewField);

            // alert(oNewField);

            aFields[fldId - 1] = oNewField;
            showFields();
        }
    }
}

function removeField()
{
    var fldId = findSelectedRadioButton(fieldsForm);

    if (!fldId)
    {
        alert("<%=EditUtil.toJavascript(jsmsg_select_field_to_remove)%>");
    }
    else
    {
        if (confirm("<%=EditUtil.toJavascript(jsmsg_remove)%>"))
        {
            aFields.splice(fldId - 1, 1);
            showFields();
        }
    }
}

function validateNewField(fields, field)
{
    var result = null;

    // Cannot have duplicate names
    for (var i = 0; i < fields.length; i++)
    {
        var oField = fields[i];

        if (oField.getDisplayName().toLowerCase() == field.getDisplayName().toLowerCase())
        {
            result = "The field `" + field.name + "' already exists.";
            return result;
        }
    }

    // For non-user-defined fields, cannot have duplicate types
    if (!field.isUserDefinedAttribute() && !field.isUserDefinedText())
    {
        for (var i = 0; i < fields.length; i++)
        {
            var oField = fields[i];

            if (oField.getType() == field.getType())
            {
                result = "A field of type " + field.getType() +
                    " is already defined.\n" +
                    "Please use a user-defined attribute or text field.";
                return result;
            }
        }
    }

    return result;
}

function validateModifiedField(fields, index, field)
{
    var result = null;

    // Cannot have duplicate names
    for (var i = 0; i < fields.length; i++)
    {
        if (i == index)
        {
            continue;
        }

        var oField = fields[i];

        if (oField.getDisplayName().toLowerCase() == field.getDisplayName().toLowerCase())
        {
            result = "The field `" + field.getDisplayName() +
                "' already exists.";
            return result;
        }
    }

    // For non-user-defined fields, cannot have duplicate types
    if (!field.isUserDefinedAttribute() && !field.isUserDefinedText())
    {
        for (var i = 0; i < fields.length; i++)
        {
            if (i == index)
            {
                continue;
            }

            var oField = fields[i];

            if (oField.getType() == field.getType())
            {
                result = "A field of type " + field.getType() +
                    " is already defined.\n" +
                    "Please use a user-defined attribute or text field.";
                return result;
            }
        }
    }

    return result;
}

function assignFieldType(field)
{
    var type;

    if (field.isUserDefinedAttribute())
    {
        type = getTypeName(field.getDisplayName());

        field.setType("attr-" + type.toLowerCase());
    }
    else if (field.isUserDefinedText())
    {
        type = getTypeName(field.getDisplayName());

        field.setType("text-" + type.toLowerCase());
    }
}

function getTypeName(name)
{
    var result = name.replace(/\s+/, "_");
    result = result.replace(/(<|>|"|')/, "_"); //"

    return result;
}
