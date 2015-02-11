/*
 * Copyright (c) 2002 GlobalSight Corporation. All rights reserved.
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

// API functions that open the Termbase Viewer window.

function checkId(id)
{
    if (id < 0)
    {
        alert("The Termbase does not exist.\n" +
            "It has probably been removed from the system.");
        return false;
    }

    return true;
}

function getWindowName(id)
{
    return "tbviewer";
}

function ShowTermbase(id)
{
    if (!checkId(id)) return;

    var windowName = getWindowName(id);
    var url = "/globalsight/ControlServlet?activityName=termviewer" +
        "&TERMBASEID=" + id;
    window.open(url, windowName,
        "menubar=no,location=no,resizable=yes,toolbar=no,height=500,width=600");
}

function ShowTermbaseConcept(id, conceptid)
{
    if (!checkId(id)) return;

    var windowName = getWindowName(id);
    var url = "/globalsight/ControlServlet?activityName=termviewer" +
        "&TERMBASEID=" + id + "&CONCEPTID=" + conceptid;
    window.open(url, windowName,
        "menubar=no,location=no,resizable=yes,toolbar=no,height=500,width=600");
}

function ShowTermbaseConceptTerm(id, conceptid, termid)
{
    if (!checkId(id)) return;

    var windowName = getWindowName(id);
    var url = "/globalsight/ControlServlet?activityName=termviewer" +
        "&TERMBASEID=" + id + "&CONCEPTID=" + conceptid + "&TERMID=" + termid;
    window.open(url, windowName,
        "menubar=no,location=no,resizable=yes,toolbar=no,height=500,width=600");
}

