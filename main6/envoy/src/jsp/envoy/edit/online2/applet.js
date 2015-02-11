// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
//
// Helper functions for GlobalSight Applet communication.
//
// This file uses global objects in main.jsp.

var g_placeholderImage =
    "<img src='/globalsight/images/img.gif' width='30' height='16'>";
var isMF = navigator.userAgent.indexOf("Firefox") != -1;

function InitDisplayHtml(gxml, datatype, g_ptagsVerbose, preserveWhitespace, dataformat)
{
    var result;
	applet.setInputSegment(gxml, "", datatype);
	
	if (g_ptagsVerbose)
	{
	    applet.getVerbose();
        result = applet.makeVerboseColoredPtags(gxml);
	}
	else
	{
	    applet.getCompact();
        result = applet.makeCompactColoredPtags(gxml);
        result = ptagStringToHtml(result);
	}
	
	if (preserveWhitespace)
	{
		result = "<pre style='display: inline'>" + result + "</pre>";
	}
	else
	{
		result = FixLeftSpace(result, dataformat);
	}
	
	return result;
}

function FixLeftSpace(p_s, p_format)
{
	if ("xml" == p_format)
	{
		var sbb = new StringBuffer(p_s);
		var ltrimed = sbb.ltrim();
		if (p_s != ltrimed)
		{
			return "&nbsp;" + ltrimed;
		}
	}

	return p_s;
}

function GetTargetDisplayHtml(gxml, datatype)
{
    var result;

    applet.setInputSegment(gxml, "", datatype);
    applet.getCompact(); // discard
    result = applet.makeCompactColoredPtags(gxml);

    result = ptagStringToHtml(result);

    return result;
}

function GetTargetDisplayHtmlForPreview(gxml, datatype)
{
    var result;

    applet.setInputSegment(gxml, "", datatype);
    applet.getVerbose(); // discard - verbose for images
    result = applet.makeInlineVerboseColoredPtags(gxml);

    result = ptagStringToHtmlForPreview(result);

    return result;
}

function GetTargetDisplayHtmlForTmPreview(gxml, datatype)
{
    var result;

    applet.setInputSegment(gxml, "", datatype);
    applet.getCompact(); // discard
    result = applet.makeCompactColoredPtags(gxml);

    result = ptagStringToHtmlForTmPreview(result);

    return result;
}

function GetTargetDisplayHtmlForTmPreview2(gxml, datatype, g_ptagsVerbose)
{
    var result;

    applet.setInputSegment(gxml, "", datatype);
    
    if (g_ptagsVerbose)
	{
	    applet.getVerbose();
        result = applet.makeVerboseColoredPtags(gxml);
	}
	else
	{
	    applet.getCompact();
        result = applet.makeCompactColoredPtags(gxml);
        result = ptagStringToHtml(result);
	}

    return result;
}

function GetSourceDisplayHtml(gxml, datatype)
{
    var result;

    applet.setInputSegment(gxml, "", datatype);
    applet.getCompact(); // discard
    result = applet.makeCompactColoredPtags(gxml);

    result = ptagStringToHtml(result);

    return result;
}

// This is a transactional type of pattern. Caller must call
// EndGetPTagStrings() when done requesting PTag strings,
// best in a try {} finally {} handler.
function GetPTagString(text, datatype)
{
    var result;

    applet.setInputSegment(text, "", datatype);
    applet.getCompact(); // discard
    result = applet.makeCompactColoredPtags(text);

    return result;
}

// Must reset applet state to source string and its ptags.
function EndGetPTagStrings()
{
    applet.setInputSegment(sourceGxml, "", datatype);
    applet.getCompact(); // discard
}

function ptagStringToHtml(result)
{
    result = result.replace(/<SPAN [^>]*>\[b(old)?\]<\/SPAN>/gi, "<B>");
    result = result.replace(/<SPAN [^>]*>\[\/b(old)?\]<\/SPAN>/gi, "</B>");

    result = result.replace(/<SPAN [^>]*>\[i(talic)?\]<\/SPAN>/gi, "<I>");
    result = result.replace(/<SPAN [^>]*>\[\/i(talic)?\]<\/SPAN>/gi, "</I>");

    result = result.replace(/<SPAN [^>]*>\[u(nderline)?\]<\/SPAN>/gi, "<U>");
    result = result.replace(/<SPAN [^>]*>\[\/u(nderline)?\]<\/SPAN>/gi, "</U>");

    result = result.replace(/<SPAN [^>]*>\[br(eak)?\]<\/SPAN>/gi, "<BR>");

    // For HTML display, this is not strictly necessary since the
    // Unicode chars alone cause IE to display the segment correctly.
    //result = result.replace(/\u202A/g, "<span class='lre'>");
    //result = result.replace(/\u202B/g, "<span class='rle'>");
    //result = result.replace(/\u202C/g, "</span>");

    return result;
}

function ptagStringToHtmlForTmPreview(result)
{
    result = ptagStringToHtml(result);

    result = result.replace(/<SPAN [^>]*>\[nbsp\]<\/SPAN>/gi, "\u00a0");

    // can't copy with only one bracket
    // result = result.replace(/\[\[/g, "[");

    return result;
}

function ptagStringToHtmlForPreview(result)
{
    result = ptagStringToHtml(result);

    result = result.replace(/<SPAN [^>]*>\[nbsp\]<\/SPAN>/gi, "\u00a0");
    result = result.replace(/<SPAN [^>]*>\[img.*?\]<\/SPAN>/gi,
        g_placeholderImage);
    result = result.replace(/<SPAN [^>]*>\[.*?\]<\/SPAN>/gi, "");

    result = result.replace(/\[\[/g, "[");

    return result;
}

function fnReplaceTagForMF(str){
	var result,origArr,origArr2,replArr;
	
	origArr = ["font-weight: bold;","font-style: italic;","text-decoration: underline;"];
	replArr  = ['[b]','[/b]','[i]','[/i]','[u]','[/u]'];
	
	result = str;
	result = result.replace(/<span style=\"font-weight: bold;\">(.*?)<\/span>/gi, "[b]$1[/b]");
	result = result.replace(/<span style=\"font-style: italic;\">(.*?)<\/span>/gi, "[i]$1[/i]");
	result = result.replace(/<span style=\"text-decoration: underline;\">(.*?)<\/span>/gi, "[u]$1[/u]");	
	while(result.indexOf("<span style=")!=-1)
	{
		var startPos 		= result.indexOf("<span style=");
		var endPos   		= result.indexOf("</span>");
		var fullStr  		= result.substring(startPos,endPos+7);
		var contstartPos 	= fullStr.indexOf(";\">");
		var content  		= fullStr.substring(contstartPos+3,endPos);
		var styleStartPos	= fullStr.indexOf("style=");
		var styleStr 		= fullStr.substring(styleStartPos,contstartPos+1);
		var replaceStr1="",replaceStr2="";
		for(var j=0;j<origArr.length;j++)
		{
			if(styleStr.indexOf(origArr[j])!=-1)
			{
				replaceStr1 = replaceStr1 + replArr[2*j];
				replaceStr2 = replArr[2*j+1] + replaceStr2;
			}
		}
		var replaceStr = replaceStr1.trim()+content+replaceStr2.trim();
		result = result.replace(fullStr,replaceStr.trim());
		
	}
	return result;
}

function DisplayHtmlToPTagString(html)
{
    var result = html;

    result = result.replace(/<B>/gi, "[b]");
    result = result.replace(/<\/B>/gi, "[/b]");
    result = result.replace(/<STRONG>/gi, "[b]");
    result = result.replace(/<\/STRONG>/gi, "[/b]");

    result = result.replace(/<I>/gi, "[i]");
    result = result.replace(/<\/I>/gi, "[/i]");
    result = result.replace(/<EM>/gi, "[i]");
    result = result.replace(/<\/EM>/gi, "[/i]");

    result = result.replace(/<U>/gi, "[u]");
    result = result.replace(/<\/U>/gi, "[/u]");
    
    if(isMF)		//Mozilla Firefox 
    {
    	result = fnReplaceTagForMF(result);
    }
    
    var isSuccessfactorsToolLF = result.indexOf("\\<BR>");
    //When xml processed by successfactors
    //<br> no need remove
    if(isSuccessfactorsToolLF == -1)
    {
        result = result.replace(/<BR>/gi, "[br]");
    }else
    {
         result = result.replace(/<BR>/gi, "\n");
    }

    // 6.3: Only accept [nbsp] as ptag. In IE, nbsp gets inserted too often
    // to be useful. See RemoveNbsp().
    //result = result.replace(/&nbsp;/gi, "[nbsp]");
    //result = result.replace(/\u00a0/gi, "[nbsp]");

    result = result.replace(/<span class=ptag[^>]*>(.*?)<\/span>/gi, "$1");

    // LRE and RLE formatting.
    //result = result.replace(/<span class=lre[^>]*>(.*?)<\/span>/gi,
    //  "\u202A$1\u202C");
    //result = result.replace(/<span class=rle[^>]*>(.*?)<\/span>/gi,
    //  "\u202B$1\u202C");

    // Remove all other HTML elements.
    //When xml processed by successfactors
    //<br> no need remove
    if(isSuccessfactorsToolLF == -1)
    {
       result = result.replace(/<[^>]*>/g, "");
    }

    result = result.replace(/&lt;/g, "<");
    result = result.replace(/&gt;/g, ">");
    result = result.replace(/&amp;/g, "&");

    return result;
}

function RemovePtags(text)
{
    var result = text;

    result =  result.replace(/\[.*?\]/g, "");

    return result;
}

function RemoveNbsp(text)
{
    text = text.replace(/(&nbsp;|\u00a0)/g, ' ');
    text = text.replace(/\\s+/g, ' ');

    return text;
}

var g_whitespace = " \t\n\r\u00a0";
function AdjustWhitespace(p_old, p_new)
{
    var result = p_new;

    if (p_new.length > 0)
    {
        var chOld = p_old.charAt(p_old.length - 1);
        var chNew = p_new.charAt(p_new.length - 1);

        if (g_whitespace.indexOf(chOld) != -1 &&
            g_whitespace.indexOf(chNew) == -1)
        {
            result += " ";
        }
        else if (g_whitespace.indexOf(chOld) == -1 &&
            g_whitespace.indexOf(chNew) != -1)
        {
            do
            {
                result = result.substring(0, result.length - 1);
                chNew = result.charAt(result.length - 1);
            }
            while (g_whitespace.indexOf(chNew) != -1);
        }
    }

    return result;
}
