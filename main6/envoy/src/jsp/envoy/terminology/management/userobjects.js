/*
 * Copyright (c) 2004 GlobalSight Corporation. All rights reserved.
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

var aObjects = new Array();

function UserObject(type, username, name, value, isDefault)
{
    this.type = type;
    this.username = username;
    this.name = name;
    this.value = value;
    this.isDefault = isDefault;
}

UserObject.prototype.setUserName = function (arg)
{
    if (arg == null)
    {
        this.username = null;
    }
    else
    {
        this.username = new String(arg);
    }
}

UserObject.prototype.setName = function (arg)
{
    if (arg == null)
    {
        this.name = null;
    }
    else
    {
        this.name = new String(arg);
    }
}

UserObject.prototype.setValue = function (arg)
{
    if (arg == null)
    {
        this.value = null;
    }
    else
    {
        this.value = new String(arg);
    }
}

UserObject.prototype.isDefault = function ()
{
    return this.isDefault;
}

UserObject.prototype.isUserObject = function ()
{
    return this.username != null && this.username != "";
}

UserObject.prototype.toString = function ()
{
    return "Termbase Object type=" + this.type +
        (this.isUserObject() ? ", user=" + this.username : " ") +
        ", name=" + this.name + ", value=" + this.value +
        ", isDefault=" + this.isDefault;
}

UserObject.prototype.toXml = function ()
{
    var result =
        "<object>" +
        "<type>" + this.type + "</type>" +
        "<user>" + this.username + "</user>" +
        "<name>" + this.name + "</name>" +
        "<value>" + this.value + "</value>" +
        "<isdefault>" + this.isDefault + "</isdefault>" +
        "</object>";

    return result;
}

