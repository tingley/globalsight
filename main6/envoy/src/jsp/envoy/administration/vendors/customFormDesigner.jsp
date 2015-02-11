<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.util.GlobalSightLocale,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    String title = bundle.getString("lb_vendor") + " - " +
                    bundle.getString("lb_custom_page_designer");
    
    //Labels
    String lbSections = bundle.getString("lb_sections");
    String lbSectionName = bundle.getString("lb_section_name");
    String lbFieldsInSection = bundle.getString("lb_fields_in_section");
    String lbLabel = bundle.getString("lb_label");
    String lbType = bundle.getString("lb_type");
    String lbName = bundle.getString("lb_name");
    String lbAdd = bundle.getString("lb_add");
    String lbRemove = bundle.getString("lb_remove");
    String lbPreview = bundle.getString("lb_preview");
    String lbDone = bundle.getString("lb_done");
    String lbSave = bundle.getString("lb_save");
    String lbCheckbox = bundle.getString("lb_checkbox");
    String lbText = bundle.getString("lb_text");
    String lbRadio = bundle.getString("lb_radio_button");
        
    String sections = (String)sessionMgr.getAttribute("sections");
    String fields = (String)sessionMgr.getAttribute("fields");
    if (fields == null) fields = "";
    String pageTitle = (String)sessionMgr.getAttribute("pageTitle");
    if (pageTitle == null) pageTitle = "";

    //available UI languages
    String[] uiLocales = UserHandlerHelper.getUILocales();
    //User UI locale
    Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = false;
var objectName = "<%= bundle.getString("lb_custom_page_designer") %>";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_vendors_custom_form_designer")%>";

removeArray = new Array();

var win;
function preview()
{
    win = window.open("/globalsight/envoy/administration/vendors/preview.jsp", "preview", 'height=500,width=700,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    win.focus();
}

//
// Can't have duplicate section names or names with commas
// Return true if dup
//
function checkDupSection(value)
{

    // get list of sections
    sectionTBody = document.getElementById("sectionTableBody");
    // item 0 is table header, item 1 is div, so start with item 2
    sections=sectionTBody.getElementsByTagName("tr");
    for (var i = 2; i < sections.length; i++)
    {
        cols = sections[i].getElementsByTagName("td");
        for (var j = 0; j < cols.length-1; j++)
        {
            // section name
            if (cols[j+1].childNodes.item(0).data == value)
            {
                return true;
            }
        }
    }
    return false;
}


//
// Can't have duplicate field names within a section.
// Return true if dup
//
function checkDupField(tableBody, newField)
{
    rows = tableBody.getElementsByTagName("tr");
    fields = tableBody.getElementsByTagName("tr");
    for (var j = 1; j < fields.length; j++)
    {
        fCols = fields[j].getElementsByTagName("td");
        if (fCols.length < 2) return false;

        if (fCols[1].childNodes.item(0).data == newField)
        {
            return true;
        }
    }
    return false;
}

function submitForm(btnName) {
    if (btnName == "save" || btnName == "done")
    {
        if (isEmptyString(ATrim(designerForm.pageTitle.value)))
        {
            alert("<%=bundle.getString("jsmsg_vendor_pageTitle")%>");
            return;
        }
        if (generateXMLandProperties() == null)
            return;
        setRemoveHiddenField();
        if (btnName == "save")
            designerForm.action = "<%=save.getPageURL()%>" + "&action=save";
        else
            designerForm.action = "<%=done.getPageURL()%>" + "&action=doneCustom";
    }
    else if (btnName == "remove")
    {
        if (confirm("<%=bundle.getString("msg_confirm_form_removal")%>"))
        {
            designerForm.action = "<%=save.getPageURL()%>" + "&action=remove";
        }
        else
        {
            return;
        }
    }
    designerForm.submit();
    if (win) win.close();
}

function setRemoveHiddenField()
{
    removed = "";
    for (var i=0; i < removeArray.length; i++)
    {
        removed += removeArray[i] + ",,";
    }
    designerForm.removedFields.value = removed;
}

function generateXMLandProperties()
{
    var properties = "";

    var xml = "<?xml version=\"1.0\"?>";
    // get page title
    xml += "<customPage>";
    xml += "<title>" + document.designerForm.pageTitle.value + "</title>";

    // get list of sections
    sectionTBody = document.getElementById("sectionTableBody");
    // item 0 is table header, item 1 is div, so start with item 2
    sections=sectionTBody.getElementsByTagName("tr");
    if (sections.length == 2)
    {
        alert("<%=bundle.getString("jsmsg_vendor_no_sections")%>");
        return null;
    }
    for (var i = 2; i < sections.length; i++)
    {
        cols = sections[i].getElementsByTagName("td");
        for (var j = 0; j < cols.length-1; j++)
        {
            // section name
            xml += "<section><name>" + cols[j+1].childNodes.item(0).data + "</name>";
            properties += replaceWhiteSpace(cols[j+1].childNodes.item(0).data) + " = " +
                        cols[j+1].childNodes.item(0).data + "\n";

            // get Field table for that section
            table = document.getElementById("table"+sections[i].id);
            fields = table.getElementsByTagName("tr");
            if (fields.length == 1)
            {
                alert("<%= bundle.getString("lb_section")%> " + 
                        cols[j+1].childNodes.item(0).data +
                        " <%=bundle.getString("jsmsg_vendor_no_data")%>");
                return null;
                
            }
            for (var k = 1; k < fields.length; k++)
            {
                fCols = fields[k].getElementsByTagName("td");
                if (fCols.length == 1)
                {
                    alert("<%= bundle.getString("lb_section")%> " + 
                            cols[j+1].childNodes.item(0).data +
                            " <%=bundle.getString("jsmsg_vendor_no_data")%>");
                    return null;
                }
                xml += "<field><label>" + fCols[1].childNodes.item(0).data + "</label>";
                xml += "<type>" + fCols[2].childNodes.item(0).data  + "</type>";
                xml += "</field>";
                properties += replaceWhiteSpace(cols[j+1].childNodes.item(0).data) + "." +
                              replaceWhiteSpace(fCols[1].childNodes.item(0).data) + 
                              " = " + fCols[1].childNodes.item(0).data + "\n";
            }
            xml += "</section>";
        }
    }
    xml += "</customPage>";
    designerForm.xml.value = xml;
    designerForm.properties.value = properties;
        
    return xml;
}

/*
 * Replace white space with underscores.
 */
function replaceWhiteSpace(key)
{
    var regexp = new RegExp(" ", "gi");
    return key.replace(regexp, "_");
}

var previouslySelected = 0;
var SECTION = 1;
var FIELDS = 2;

function addSection()
{
    var value = ATrim(designerForm.sectionName.value);
    if (isEmptyString(value))
    {
        alert("<%=bundle.getString("jsmsg_vendor_no_section")%>");
        return;
    }
    if (hasSpecialChars(value))
    {
        alert("<%=lbSectionName%>" + "<%=bundle.getString("msg_invalid_entry")%>");
        return;
    }
    if (checkDupSection(value))
    {
        alert("<%=bundle.getString("jsmsg_vendor_dup_section_name")%>");
        return;
    }
    needWarning = true;

    var newTD , newTR, newRadio;

    // Determine background color for new row adding
    // Do this by getting how many row there already are
    sections = document.getElementById("sectionTableBody");
    rows = sections.getElementsByTagName("tr");
    sectionValue = rows.length - 2;
    var sectionLineColor = (rows.length%2 == 0) ? "#FFFFFF" : "#EEEEEE";

    emptySectionTable.style.display = "none";
    newTR = document.createElement("tr");
    newTR.style.color="black";
    newTR.style.fontWeight="normal";
    newTR.style.background= sectionLineColor;
    newTR.className = "standardText";

    newTD = document.createElement("td");
    newTR.id = sectionValue;
    newRadio = document.createElement("<input type='radio' name='radioBtn' value='" +
                 sectionValue + "'checked onclick='javascript:enableFields()'>");
    newTD.appendChild(newRadio);
    newTR.appendChild(newTD);

    newTD = document.createElement("td");
    newTD.id = "section"+sectionValue;
    var newText = document.createTextNode(value);
    newTD.appendChild(newText);
    newTR.appendChild(newTD);
    sectionTableBody.appendChild(newTR);

    document.designerForm.removeSectionBtn.disabled = false;
    for (i = 0; i < document.designerForm.radioBtn.length-1; i++) 
    {
        document.designerForm.radioBtn[i].checked = false;
    }
    document.designerForm.fieldName.value = "";
    document.designerForm.fieldValue.value = "";
    enableFields();
    //document.designerForm.sectionName.value = "";
}

function enableFields()
{
    var selectedRadioBtn = getSelectedRadio(SECTION);
    // set section name in section name textfield
    td = document.getElementById("section"+selectedRadioBtn);
    var sectionName = td.innerHTML;
    document.designerForm.sectionName.value = sectionName;

    // hide the previously shown table field table
    if (previouslySelected != -1)
    {
        var prev = document.getElementById("table" + previouslySelected);
        prev.style.display = "none";
    }
    
    // get currently selected table
    var tBody = document.getElementById("table" + selectedRadioBtn);
    if (tBody == null)
    {
        // create a new fieldTable
        createFieldsTable();
    }
    else
    {
        // show correct fieldTable
        tBody.style.display = "block";
        // clear fieldTable selections
        obj = document.designerForm.radioBtn2;
        if (obj != null)
        {
            if (obj.length)
            {
                for (i = 0; i < obj.length; i++)
                    obj[i].checked = false;
            }
            else
            {
                obj.checked = false;
            }
        }
        document.designerForm.fieldName.value = "";
        document.designerForm.removeFieldBtn.disabled = true;
    }
    document.getElementById("fields").style.display = "block";
    previouslySelected = selectedRadioBtn;
    document.designerForm.removeSectionBtn.disabled = false;
}

function updateFieldInput()
{
    needWarning = true;
    var selectedRadioBtn = getSelectedRadio(FIELDS);
    labelTD = document.getElementById("label"+selectedRadioBtn);
    document.designerForm.fieldName.value = labelTD.childNodes.item(0).data;
    typeTD = document.getElementById("type"+selectedRadioBtn);
    typeValue = typeTD.childNodes.item(0).data;
    for (var i = 0; i < document.designerForm.fieldType.length; i++) 
    {
        if (document.designerForm.fieldType.options[i].value == typeValue)
            document.designerForm.fieldType.options[i].selected = true;
    }
    document.designerForm.removeFieldBtn.disabled = false;

}

function getSelectedRadio(type)
{
    var selectedRadioBtn = null;
    var obj = null;
    if (type == SECTION)
        obj = document.designerForm.radioBtn;
    else if (type == FIELDS)
        obj = document.designerForm.radioBtn2;

    if (obj != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (obj.length)
        {
            for (i = 0; i < obj.length; i++)
            {
                if (obj[i].checked == true)
                {
                    selectedRadioBtn = obj[i].value;
                    break;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (obj.checked == true)
            {
                selectedRadioBtn = obj.value;
            }
        }
    }
    return selectedRadioBtn;
}


function removeSection()
{
    needWarning = true;
    selectedRadioBtn = getSelectedRadio(SECTION);
    if (selectedRadioBtn == null)
    {
        alert("<%=bundle.getString("jsmsg_custom_page_select_section")%>");
        return;
    }
    td = document.getElementById("section"+selectedRadioBtn);
    var sectionName = td.innerHTML;

    // Update the removedArray
    // first need to get all field names so that can use sectionName.fieldName
    var tableBody =  document.getElementById("tbody"+selectedRadioBtn);
    rows = tableBody.getElementsByTagName("tr");
    fields = tableBody.getElementsByTagName("tr");
    for (var i = 1; i < fields.length; i++)
    {
        fCols = fields[i].getElementsByTagName("td");
        if (fCols.length < 2) continue;
        fieldName = fCols[1].childNodes.item(0).data;
        removeArray[removeArray.length] = sectionName + "." + fieldName;
    }

    // Update the UI
    // First remove the fields table that belonged to that section
    tbls = document.getElementById("tables");
    fieldTable = document.getElementById("table" + selectedRadioBtn);
    tbls.removeChild(fieldTable);
    
    document.getElementById("sectionTableBody").removeChild(
                document.getElementById(selectedRadioBtn));
    document.getElementById("fields").style.display = "none";
    document.designerForm.sectionName.value = "";

    // loop through sections in table and recolor the background
    sections = document.getElementById("sectionTableBody");
    rows = sections.getElementsByTagName("tr");
    for (var i=1; i < rows.length; i++)
    {
        color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
        rows[i].style.background = color;
    }
    previouslySelected = -1;
    document.designerForm.removeSectionBtn.disabled = true;
}

function createFieldsTable()
{
    var newTable, newTBody, newTD, newTR, newRadio, newText, newDiv;
    var selectedRadioBtn = getSelectedRadio(SECTION);


    newTable = document.createElement("table");
    newTable.border = 0;
    newTable.width = "75%";
    newTable.setAttribute("cellSpacing", 0);
    newTable.setAttribute("cellPadding", 5);
    newTable.className = "list";
    newTable.id = "table" + selectedRadioBtn;

    newTBody = document.createElement("tBody");
    newTBody.id = "tbody" + selectedRadioBtn;

    newTR = document.createElement("tr");
    newTR.className = "tableHeadingBasic";
    newTD = document.createElement("td");
    newTD.width="2%";
    newText = document.createTextNode(" ");
    newTD.appendChild(newText);
    newTR.appendChild(newTD);

    newTD = document.createElement("td");
    newText = document.createTextNode("Name");
    newTD.appendChild(newText);
    newTR.appendChild(newTD);
    newTD = document.createElement("td");
    newText = document.createTextNode("Type");
    newTD.appendChild(newText);
    newTR.appendChild(newTD);
    newTBody.appendChild(newTR);

    newTR = document.createElement("tr");
    newTR.id = "emptyFieldTable" + selectedRadioBtn;
    newTD = document.createElement("td");
    newTD.setAttribute("colSpan", 3);
    newText = document.createTextNode("The section does not contain any fields yet.");
    newTD.appendChild(newText);
    newTR.appendChild(newTD);
    newTBody.appendChild(newTR);
    newTable.appendChild(newTBody);
    t= document.getElementById("tables");
    t.appendChild(newTable);
}

function removeField()
{
    needWarning = true;
    selectedRadioBtn = getSelectedRadio(FIELDS);
    if (selectedRadioBtn == null)
    {
        alert("<%=bundle.getString("jsmsg_custom_page_select_field")%>");
        return;
    }
    
    // update removeArray
    td = document.getElementById("section"+getSelectedRadio(SECTION));
    var sectionName = td.innerHTML;
    td = document.getElementById("label"+selectedRadioBtn);
    fieldName = td.innerHTML;
    removeArray[removeArray.length] = sectionName + "." + fieldName;
    
    // update table in the UI
    table = document.getElementById("tbody" + getSelectedRadio(SECTION));
    tr = document.getElementById("tr"+selectedRadioBtn);

    table.removeChild(tr);

    // loop through fields in table and recolor the background
    rows = table.getElementsByTagName("tr");
    for (var i=1; i < rows.length; i++)
    {
        color = (i%2 == 0) ? "#EEEEEE" : "#FFFFFF";
        rows[i].style.background = color;
    }
    designerForm.fieldName.value = "";
    document.designerForm.removeFieldBtn.disabled = true;
}

function addField()
{
    needWarning = true;
    var value = ATrim(designerForm.fieldName.value);
    if (isEmptyString(value))
    {
        alert("<%=bundle.getString("jsmsg_vendor_no_field")%>");
        return;
    }
    if (hasSpecialChars(value))
    {
        alert("<%=lbLabel%>" + "<%=bundle.getString("msg_invalid_entry")%>");
        return;
    }
    selectedRadioBtn = getSelectedRadio(SECTION);

    var newTD, newTR, newRadio, newText;

    var tableBody =  document.getElementById("tbody"+selectedRadioBtn);

    if (checkDupField(tableBody, value))
    {
        alert("<%=bundle.getString("jsmsg_vendor_dup_field_name")%>");
        return;
    }

    // Determine background color for new row adding
    // Do this by getting how many row there already are
    rows = tableBody.getElementsByTagName("tr");
    var fieldLineColor = (rows.length%2 == 0) ? "#EEEEEE" : "#FFFFFF";

    var empty = document.getElementById("emptyFieldTable"+selectedRadioBtn);
    if (empty != null)
    {
        tableBody.removeChild(empty);
        fieldLineColor =  "#FFFFFF";
    }
    rows = tableBody.getElementsByTagName("tr");
    id = selectedRadioBtn + "_" + rows.length;

    newTR = document.createElement("tr");
    newTR.style.color="black";
    newTR.style.fontWeight="normal";
    newTR.style.background= fieldLineColor;
    newTR.className = "standardText";
    newTR.id = "tr" + id;

    newTD = document.createElement("td");
    newRadio = document.createElement("<input type='radio' name='radioBtn2' value='" + id + "' onclick='javascript:updateFieldInput()'>");
    newTD.appendChild(newRadio);
    newTR.appendChild(newTD);

    newTD = document.createElement("td");
    newText = document.createTextNode(value);
    newTD.appendChild(newText);
    newTD.id = "label" + id;
    newTR.appendChild(newTD);

    newTD = document.createElement("td");
    obj = document.designerForm.fieldType;
    newText = document.createTextNode(obj.options[obj.selectedIndex].value);
    newTD.appendChild(newText);
    newTD.id = "type" + id;
    newTR.appendChild(newTD);

    tableBody.appendChild(newTR);
    for (i = 0; i < document.designerForm.radioBtn2.length; i++) 
    {
        document.designerForm.radioBtn2[i].checked = false;
    }
    // clear out textfields
    document.getElementById("fieldName").value = "";
    document.getElementById("fieldValue").value = "";
}


</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P></P>

<FORM NAME="designerForm" METHOD="post">
<input type="hidden" name="xml">
<input type="hidden" name="properties">
<input type="hidden" name="removedFields">

<table cellspacing=4>
    <tr>
        <td>
            <%=bundle.getString("lb_locale")%>:
        </td>
        <td>
            <select name="uiLocale">
            <%                   
            if (uiLocales != null)
            {
                for (int i = 0; i < uiLocales.length; i++)
                {
                    Locale locale = PageHandler.getUILocale(uiLocales[i]);
                    String locString = locale.toString();
                    String language = locale.getDisplayLanguage(userUiLocale);
                    
                    out.println("<option value=\"" + locString + "\">" +
                                language + "</option>");
                }
            } 
            %> 
            </select>
        </td>
    </tr>
    <tr>
        <td>
            <%=bundle.getString("lb_page")%>&nbsp;<%=bundle.getString("lb_title")%><span class="asterisk">*</span>:
        </td>
        <td>
            <input type="text" maxlenght="30" name="pageTitle" value="<%=pageTitle%>">
        </td>
    </tr>
</table>
<p>
<fieldset>
    <legend><b>Sections</b></legend>
    <p>
    <%=sections%>
    <table border=0 bordercolor=red>
        <tr>
            <td width="25%">Section Name<font color="red">*</font>:</td>
            <td><input type="text" maxlength="100" name="sectionName">
                <input type="button" value="<%=lbAdd%>" onClick="javascript:addSection()">
                <input type="button" value="<%=lbRemove%>" name="removeSectionBtn" onclick="javascript:removeSection()" >
            </td>
        </tr>
        <tr>
            <td style="padding-left:40px; padding-top:20px; padding-right:40px" colspan=2>
                <fieldset id="fields" style="display:block">
                    <legend><b><%=bundle.getString("lb_fields_in_section")%></b></legend>
                    <div id="tables">
                        <%= fields %>
                    </div>
                      <table>
                        <tr>
                            <td nowrap>
                                <%=bundle.getString("lb_label")%><font color="red">*</font>:
                            </td>
                            <td>
                                <input type="text" maxlength="100" name="fieldName">
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <%=bundle.getString("lb_type")%><font color="red">*</font>:
                            </td>
                            <td>
                                <select name="fieldType">
                                    <option value="Text"><%=lbText%></option>
                                    <option value="Checkbox"><%=lbCheckbox%></option>
                                    <option value="Radio"><%=lbRadio%></option>
                                </select>
                            </td>
                        </tr>
                        <tr id="valueInput" style="display:none">
                            <td nowrap>
                                Database Value<font color="red">*</font>:
                            </td>
                            <td>
                                <input type="text" name="fieldValue">
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <input type="button" value="<%=lbAdd%>" onClick="javascript:addField()">
                                <input type="button" value="<%=lbRemove%>" name="removeFieldBtn" onClick="javascript:removeField()" disabled>
                            </td>
                        </tr>
                    </tableBody>
                    </table>
                </fieldset>
            </td>
        </tr>
        <tr>
        </tr>
    </table>
</fieldset>
<p>
    <input type="button" value="<%=lbRemove%>" name="removeFormBtn" onClick="submitForm('remove')" >
    <input type="button" value="<%=lbPreview%>" onclick="javascript:preview()">
    <input type="button" value="<%=lbSave%>" onclick="submitForm('save')">
    <input type="button" value="<%=lbDone%>" onclick="submitForm('done')">
<script language="JavaScript">
if ("<%=pageTitle%>" == "")
{
    document.getElementById("fields").style.display = "none";
    document.designerForm.removeFormBtn.disabled = true;
    previouslySelected = -1;
}
</script>
</form>
</html>
