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

String lb_field_no_suggestion =
  EditUtil.toJavascript(bundle.getString("lb_field_no_suggestion"));
String lb_field_name_skip_this_column =
  EditUtil.toJavascript(bundle.getString("lb_field_name_skip_this_column"));
%>


function Column(id, name, example, type, subtype)
{
    this.id = id;
    this.name = name;
    this.example = example;
    this.type = type;
    this.subtype = subtype;
}

function Association(name, example, type)
{
    this.name = name;
    this.example = example;
    this.type = type;
}

function Field(name, type, subtype)
{
    this.name = name;
    this.type = type;
    this.subtype = subtype;
}

function FieldType(id, type, name, subtype, description)
{
    this.id = id;
    this.type = type;
    this.name = name;
    this.subtype = subtype;
    this.description = description;
}

// Used in field.jsp to filter fields
function FieldType_skipField()
{
    return this.type == "skip";
}
FieldType.prototype.skipField = FieldType_skipField;

var aFieldTypes = new Array();
var i = 0;

aFieldTypes.push(new FieldType(
    i++, "skip", "<%=lb_field_name_skip_this_column%>", null, ""));

aFieldTypes.push(new FieldType(
    i++, "username", "User Name", null, "The user's name."));

aFieldTypes.push(new FieldType(
    i++, "activityname", "Subject", null, "The activity/event subject."));

aFieldTypes.push(new FieldType(
    i++, "activitytype", "Activity Type", null, "The activity type."));

aFieldTypes.push(new FieldType(
    i++, "startdate", "Start Date", "MM/dd/yyyy,MM/dd/yy,dd/MM/yyyy,dd/MM/yy",
    "The activity's start date."));

aFieldTypes.push(new FieldType(
    i++, "enddate", "End Date", "MM/dd/yyyy,MM/dd/yy,dd/MM/yyyy,dd/MM/yy",
    "The activity's end date."));

function getFieldNameByType(type)
{
    for (var oIndex in aFieldTypes)
    {
        var oType = aFieldTypes[oIndex];

        if (oType.type.toLowerCase() == type.toLowerCase())
        {
            return oType.name;
        }
    }

    return null;
}

