// Change these two URLs.
var g_SpellServerHref ="/globalsight/spellchecker/SpellCheck";
var g_SpellingWindowURL = "/globalsight/spellchecker/jsp/index.html";

var isMSIE = (navigator.appName=='Microsoft Internet Explorer');
var lBrowserVer = parseInt(navigator.appVersion);
if (isMSIE)
{
    var verStartIdx = navigator.appVersion.indexOf("MSIE ");
    var verEndIdx = navigator.appVersion.indexOf(";",verStartIdx);
    lBrowserVer = parseFloat(navigator.appVersion.substring(
        verStartIdx + 5, verEndIdx));
}
var isNS = (navigator.appName == 'Netscape');
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
var isMac = (navigator.appVersion.indexOf("Mac") > 0);
var g_totalMispellings = 0;
var g_spellingWindow = null;

function scSpell(opener, ctrl, locale, uilocale, dict, customDict)
{
    /* p_ctrl has several special parameters:
       typectrl=[type of control]
       exec=[JavaScript/VBScript method to call on parent window (yours)]
       e.g. save a form when finish has been selected.
    */

    if (g_spellingWindow != null && !g_spellingWindow.closed)
    {
        g_spellingWindow.focus();
        return;
    }

    var x = (screen.width - 450) / 2;
    var y = (screen.height - 300) / 2;

    x = 100;
    y = 100;

    g_spellingWindow = window.open(g_SpellingWindowURL +
        "?locale=" + locale + "&uilocale=" + uilocale +
        "&dict=" + dict + "&customDict=" + customDict +
        "&ctrl=" + ctrl, null,
        "top=" + x + ",left=" + y +
        ",width=450,height=300,toolbar=no,resizable=no");

    window.status = 'Spell Check Started';

    if (isNS)
    {
        g_spellingWindow.window.focus();
    }
}

function scCheckWord(WordtoCheck)
{
    var pmd = parent.mainframe.document;
    var pfd = parent.footerframe.document;

    //Move to current word via href #
    //Load array of suggestions
    //Populate suggestions
    currentword = WordtoCheck;
    //WordtoCheck = scStdReplaceString(0, WordtoCheck, "|XDEapos|", "'", true);

    parent.document.title = 'Looking up suggestions for ' + WordtoCheck;

    if (isMSIE)
    {
        // CKH Added 04/22/2002 avoid mac resource crash,
        // do not remove browser check.
        pfd.close();
        pfd.open();
    }

    pfd.write('<html><body><FORM ENCTYPE="application/x-www-form-urlencoded" ACTION="' +
        g_SpellServerHref + '" NAME="SpellForm" target="_self" METHOD="POST">');
    pfd.write('<input type="text" name="wordtosuggest" value="">');
    var locale = pmd.spellcheckform.locale.value;
    pfd.write('<input type="text" name="locale" value="' + locale + '">');
    var uilocale = pmd.spellcheckform.uilocale.value;
    pfd.write('<input type="text" name="uilocale" value="' + uilocale + '">');
    var dict = pmd.spellcheckform.dict.value;
    pfd.write('<input type="text" name="dict" value="' + escape(dict) + '">');
    var customDict = pmd.spellcheckform.customDict.value;
    pfd.write('<input type="text" name="customDict" value="' + escape(customDict) + '">');
    pfd.write('<INPUT TYPE="submit" NAME="Submit" VALUE="Submit">');
    pfd.write('</FORM></body></html>');
    pfd.forms[0].wordtosuggest.value = escape(WordtoCheck);
    pfd.forms[0].submit();

    pmd.spellcheckform.currentword.value = WordtoCheck;

    parent.document.title =
        "\"" + WordtoCheck + "\"" + pmd.spellcheckform.misSpelledMessage.value;
}

function scRefreshText()
{
    var pmd = parent.mainframe.document;

    if (typeof(pmd.spellcheckform) == 'undefined' ||
        typeof(pmd.spellcheckform.MispelledWords) == 'undefined' ||
        typeof(parent.topframe) == 'undefined' ||
        typeof(parent.topframe.document.getElementById("foundSoFar")) == 'undefined')
    {
        // UI not ready yet
        setTimeout("scRefreshText()", 100);
        return;
    }

    var locale = pmd.spellcheckform.locale.value;
    var misspellings = pmd.spellcheckform.MispelledWords.value;
    var fixedtext = pmd.spellcheckform.fixedtext.value;
    var tempDisplay = fixedtext;
    var oldstart = 0;
    var FirstWord = '';

    g_totalMispellings = 0;
    // alert("misspellings=" + misspellings);

    var currentError = -1;
    var arr = misspellings.split("|XDE|");
    for (var i = 0; i < arr.length; i++)
    {
        var misspelling = arr[i];

        if (misspelling != '')
        {
            if (g_totalMispellings == 0)
            {
                FirstWord = misspelling;
            }

            if (misspelling.indexOf('-ISOK') > -1)
            {
                var tmpstr = misspelling.substr(0, misspelling.length - 5);
                oldstart = tempDisplay.indexOf(tmpstr, oldstart) + tmpstr.length;
            }
            else
            {
                var tmpstr = misspelling.replace(/'/g, "\\'"); //"
                // =scStdReplaceString(0, misspelling, "'", "|XDEapos|", true);

                var ReplaceWith;

                if (pmd.spellcheckform.currentword.value == misspelling)
                {
                    ReplaceWith = '<a name=word' + g_totalMispellings +
                        ' class=ce href="javascript:scCheckWord(\'' +
                        tmpstr + '\')">' + misspelling + '</a>';

                    if (currentError == -1)
                    {
                        //save for focus
                        currentError = g_totalMispellings;
                    }
                }
                else
                {
                    ReplaceWith = '<a name=word' + g_totalMispellings +
                        ' class=e href="javascript:scCheckWord(\'' +
                        tmpstr + '\')">' + misspelling + '</a>';
                }

                g_totalMispellings++;

                tempDisplay = scReplaceString(oldstart, tempDisplay,
                    misspelling, ReplaceWith, true);

                oldstart = oldstart + ReplaceWith.length;
            }
        }
    }

    if (false)
    {
        tempDisplay = scReplaceString(0, tempDisplay, '\n', '<br>', true);
    }

    if(isFirefox)
	{
		if (isRtlLocale(locale))
        {
            parent.topframe.document.getElementById("foundSoFar").getAttribute("dir") = "rtl";
        }
        parent.topframe.document.getElementById("foundSoFar").innerHTML = tempDisplay;

        // assign focus to current word
        if (true)
        {
            var links = parent.topframe.document.links;
            var currentWord = 'word' + currentError;

            for (i = 0; i < links.length; i++)
            {
                var link = links[i];

                if (link.name == currentWord)
                {
                    link.focus();
                    break;
                }
            }
        }
	}
    else if (isNS)
    {
        with (parent.topframe.document)
            {
            var tempDisplay2 =
                '<SPAN STYLE="position: absolute; border: 0px red solid; ' +
                'width: 100%" dir="' + (isRtlLocale(locale) ? "rtl" : "ltr") +
                '">' + tempDisplay + '</SPAN>';
            open();
            write(tempDisplay2);
            close();
            }
    }
    else
    {
        // alert(tempDisplay);

        if (isRtlLocale(locale))
        {
            parent.topframe.foundSoFar.dir = "rtl";
        }
        parent.topframe.foundSoFar.innerHTML = tempDisplay;

        // assign focus to current word
        if (isMSIE && !isMac)
        {
            var links = parent.topframe.document.links;
            var currentWord = 'word' + currentError;

            for (i = 0; i < links.length; i++)
            {
                var link = links[i];

                if (link.name == currentWord)
                {
                    link.focus();
                    break;
                }
            }
        }
    }

    if (g_totalMispellings == 0)
    {
        parent.document.title = pmd.spellcheckform.Spellfinished.value;
        pmd.spellcheckform.suggestionlist.options.length = 1;
        pmd.spellcheckform.tchangeto.focus();

        if (uConfirm(pmd.spellcheckform.Spellfinished.value))
        {
            scFinishSpellCheck();
        }
        else
        {
            scCancelSpellCheck();
        }
    }

    // Add scFinishSpellCheck() function to the end of scRefreshText()
    // function to automatically close window.
}

function scSelectWord()
{
    var sdf = self.document.spellcheckform;
    var listindex = sdf.suggestionlist.selectedIndex;
    sdf.tchangeto.value = sdf.suggestionlist.options[listindex].text;
}

function scEnableSpellCheck()
{
    if (parent.opener)
    {
        parent.opener.status = '';
    }
}

function scCancelSpellCheck()
{
    parent.opener.status = "Spell Check Complete";
    parent.close();
}

function scFinishSpellCheck()
{
    var pdlString = parent.document.location.toString();
    //return text to calling form control.
    var control = scGetQueryParm(pdlString, "ctrl");
    var controlType = scGetQueryParm(pdlString, "typectrl");
    var executeWhenComplete = scGetQueryParm(pdlString, "exec");
    var OriginalControl = eval('parent.opener.document.' + control) ||
        parent.opener.document.getElementById(control);

    scSetControlText(OriginalControl, self.document.spellcheckform.fixedtext.value,
        controlType);

    if (executeWhenComplete != "")
    {
        //code to execute when finish selected.
        eval("parent.window.opener." + executeWhenComplete);
    }

    parent.opener.status = "Spell Check Complete";
    parent.close();
}

function scCheckNextWord(LastWord)
{
    //locate where we are in the buffer, skip to the next word...
    var pmd = parent.mainframe.document;

    var str = self.document.spellcheckform.MispelledWords.value;
    pmd.spellcheckform.suggestionlist.options.length = 0;
    pmd.spellcheckform.tchangeto.value = '';
    var arr = str.split("|XDE|");
    var nextword = "";
    var tryit = false;
    for (var i = 0; i < arr.length; i++)
    {
        var misspelling = arr[i];
        var stripped = scStdReplaceString(0, misspelling, '-ISOK', '', true);

        if (misspelling.length > 0 && stripped == LastWord)
        {
            tryit = true;
        }

        if (tryit)
        {
            if (i+1 < arr.length)
            {
                if (arr[i+1] != '' && arr[i+1].indexOf('-ISOK') == -1)
                {
                    scCheckWord(arr[i+1]);
                    return;
                }
            }
            else
            {
                //no more
                break;
            }
        }
    }

    scRefreshText();
}

function scChangeWord()
{
    var sdf = self.document.spellcheckform;

    var wordtoChange = sdf.currentword.value;
    var ReplaceWith = sdf.tchangeto.value;
    var fixedtext = sdf.fixedtext.value;
    var tempDisplay = fixedtext;
    var str = sdf.MispelledWords.value;
    var oldstart = 0;
    var foundWordAt = 0;

    var arr = str.split("|XDE|");
    for (var i = 0; i < arr.length; i++)
    {
        var misspelling = arr[i];
        if (misspelling != '')
        {
            if (misspelling.indexOf('-ISOK') > -1)
            {
                var tmpstr = misspelling.substr(0, misspelling.length-5);
                if (tmpstr == wordtoChange)
                {
                    foundWordAt =
                        tempDisplay.indexOf(tmpstr, oldstart) + tmpstr.length;
                    oldstart = foundWordAt;
                }
            }
        }
    }

    // Rebuild the mis-spelled words buffer, only do once otherwise
    // second mispelling of same word will be ignored.
    var misspelledWordsLeft = scStdReplaceString(0, str, wordtoChange + '|XDE|',
        ReplaceWith + '-ISOK|XDE|', false);
    sdf.MispelledWords.value = misspelledWordsLeft;

    tempDisplay = scReplaceString(
        foundWordAt, tempDisplay, wordtoChange, ReplaceWith);
    sdf.fixedtext.value = tempDisplay;

    scCheckNextWord(ReplaceWith);
}

function scChangeAll()
{
    var sdf = self.document.spellcheckform;

    var wordtoChange = sdf.currentword.value;
    var ReplaceWith = sdf.tchangeto.value;
    var fixedtext = sdf.fixedtext.value;
    var tempDisplay = fixedtext;
    var str = sdf.MispelledWords.value;
    sdf.MispelledWords.value = '';
    var arr = str.split("|XDE|");

    //Rebuild the mis-spelled words buffer.
    var misspelledWordsLeft = scStdReplaceString(0, str, wordtoChange + '|XDE|',
        wordtoChange + '-ISOK|XDE|', true);
    sdf.MispelledWords.value = misspelledWordsLeft;

    tempDisplay = scReplaceString(0, tempDisplay, wordtoChange, ReplaceWith, true);
    sdf.fixedtext.value = tempDisplay;

    scCheckNextWord(wordtoChange);
}

function scIgnore()
{
    var sdf = self.document.spellcheckform;

    var wordtoChange = sdf.currentword.value;
    var str = sdf.MispelledWords.value;

    var misspelledWordsLeft = scStdReplaceString(0, str, wordtoChange + '|XDE|',
        wordtoChange + '-ISOK|XDE|', false);
    sdf.MispelledWords.value = misspelledWordsLeft;

    scCheckNextWord(wordtoChange);
}

function scIgnoreAll()
{
    var sdf = self.document.spellcheckform;

    var wordtoChange = sdf.currentword.value;
    var str = sdf.MispelledWords.value;

    var misspelledWordsLeft = scStdReplaceString(0, str, wordtoChange + '|XDE|',
        wordtoChange + '-ISOK|XDE|', true);
    sdf.MispelledWords.value = misspelledWordsLeft;

    scCheckNextWord(wordtoChange);
}

function scAddWord()
{
    var sdf = self.document.spellcheckform;

    var pmd = parent.mainframe.document;
    var pfd = parent.footerframe.document;

    var wordtoChange = sdf.currentword.value;
    var str = sdf.MispelledWords.value;

    var misspelledWordsLeft = scStdReplaceString(0, str, wordtoChange + '|XDE|',
        wordtoChange + '-ISOK|XDE|', true);
    sdf.MispelledWords.value = misspelledWordsLeft;

    pfd.write('<FORM ENCTYPE="application/x-www-form-urlencoded" ACTION="' +
        g_SpellServerHref + '" NAME="SpellForm" target="_self" METHOD="POST">');
    pfd.write('<TEXTAREA NAME="wordtoadd" ROWS="10" COLS="80"></TEXTAREA>');
    pfd.write('<TEXTAREA NAME="customDict" ROWS="10" COLS="80"></TEXTAREA>');
    pfd.write('<INPUT TYPE="submit" NAME="Submit" VALUE="Submit">');
    pfd.write('</FORM>');

    pfd.SpellForm.wordtoadd.value = escape(wordtoChange);
    pfd.SpellForm.customDict.value =
        escape(pmd.spellcheckform.customDict.value);

    pfd.forms[0].submit();

    var startindex = 0;

    //Notify the user as to what is happening.  Do not remove this
    //alert, it also provides time for the request to be processed.

    uAlert('Adding "' + wordtoChange + '" to custom dictionary.');

    scCheckNextWord(wordtoChange);
}

//
// General Functions
//

var scNonAlphaChars =
  ' ,.;"\'`´~!@#$%^&*()_-=+{}[]\\|:/<>?1234567890\u00ab\u00bb\u2018\u2019\u201A\u201B\u201C\u201D\u201E\u201F\u2039\u203A\u275B\u275C\u275D\u275E\u301D\u301E\u301F\uFF02';

function scReplaceString(StartPos, SearchString, FindText, ReplaceText, doall)
{
    var intag = false;
    var pos, endpos, fSubstring, sSubstring, prevChar, lastChar;

    pos = SearchString.indexOf(FindText, StartPos);
    if (pos == -1)
    {
        return SearchString;
    }

    for (var i = 0; i < SearchString.length; i++)
    {
        if (i > pos)
        {
            pos = SearchString.indexOf(FindText, i);
        }
        if (pos == -1)
        {
            return SearchString;
        }

        var ch = SearchString.charAt(i);

        if (ch == '<')
        {
            intag = true;
            continue;
        }
        else if (ch == '>' && intag/* == true*/)
        {
            intag = false;
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (pos == i)
        {
            endpos = pos + FindText.length;

            if (pos == 0)
            {
                prevChar = ' ';
            }
            else
            {
                prevChar = SearchString.charAt(pos - 1);
            }

            if (endpos < SearchString.length)
            {
                lastChar = SearchString.charAt(endpos);
            }
            else
            {
                lastChar = ' ';
            }

            // Check if we're currently in the middle of another word.
            if (scNonAlphaChars.indexOf(prevChar) > -1 &&
                scNonAlphaChars.indexOf(lastChar) > -1)
            {
                // There is a whole word.
                fSubstring = SearchString.substring(0, pos);
                sSubstring = SearchString.substring(endpos);
                SearchString = fSubstring + ReplaceText + sSubstring;

                if (!doall)
                {
                    break;
                }

                // Move forward to skip this whole word.
                i = pos = fSubstring.length + ReplaceText.length;
            }
            else
            {
                // Move forward to skip this partial word.
                i = pos = endpos;
            }
        }
    }

    return SearchString;
}

function scStdReplaceString(StartPos, SearchString, FindText, ReplaceText, doall)
{
    var pos, fSubstring, sSubstring, intag = false;

    pos = SearchString.indexOf(FindText, StartPos);
    if (pos == -1)
    {
        return SearchString;
    }

    for (var i = 0; i < SearchString.length; i++)
    {
        if (i > pos)
        {
            pos = SearchString.indexOf(FindText, i);
        }

        if (pos ==- 1)
        {
            return SearchString;
        }

        var ch = SearchString.charAt(i);

        if (ch == '<')
        {
            intag = true;
        }
        else if (ch == '>' && intag == true)
        {
            intag = false;
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (pos == i)
        {
            StartPos = pos + ReplaceText.length;
            fSubstring = SearchString.substring(0, pos);
            sSubstring = SearchString.substring(
                pos + FindText.length, SearchString.length);
            SearchString = fSubstring + ReplaceText + sSubstring;

            if (!doall)
            {
                return SearchString;
            }
            else
            {
                pos = SearchString.indexOf(FindText, StartPos);
            }
        }
    }

    return SearchString;
}

function scGetQueryParm(p_url, p_parmToGet)
{
    var nextPos, startPos = p_url.indexOf(p_parmToGet);
    var result = '';

    if (startPos > -1)
    {
        nextPos = p_url.indexOf('&', startPos);

        if (nextPos == -1)
        {
            nextPos = p_url.length;
        }

        result = p_url.substring(startPos + p_parmToGet.length + 1, nextPos);
    }

    return result;
}

function scStripPtags(s)
{
    var intag = false;
    var result = "";
    var addchar = "";

    for (var i = 0; i < s.length; i++)
    {
        var ch = s.charAt(i);

        if (ch == '[')
        {
            if (intag)
            {
                result += "[";
                intag = false;
            }
            else
            {
                intag = true;
            }

            continue;
        }

        if (ch == ']' && intag)
        {
            intag = false;
            addchar = " ";
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (result.length > 0)
        {
            result += addchar;
        }

        result += ch;

        addchar = "";
    }

    return result;
}

function scStripHTML(s)
{
    var intag = false;
    var result = "";
    var addchar = "";

    s = scStdReplaceString(0, s, "&nbsp;", " ", true);
    s = scStdReplaceString(0, s, "\u00a0", " ", true);

    for (var i = 0; i < s.length; i++)
    {
        var ch = s.charAt(i);

        if (ch == '<')
        {
            intag = true;
        }

        if (ch == '>' && intag)
        {
            intag = false;
            addchar = " ";
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (result.length > 0)
        {
            result += addchar;
        }

        result += ch;

        addchar = "";
    }

    return result;
}

function scCancelAllSpellCheck()
{
    if (parent.opener.xdearr)
    {
        parent.opener.xdearr.length = 0;
    }

    scCancelSpellCheck();
}

function scGetControlText(OriginalControl, controlType)
{
    if (controlType == 'xdeedit')
    {
        //handle html editor
        if (!isNS)
        {
            return OriginalControl.document.body.innerHTML;
        }
        else
        {
            return OriginalControl.frameWindow.document.body.innerHTML;
        }
    }
    // Tue Mar 09 01:04:28 2004 CvdL: added.
    else if (controlType == 'richedit')
    {
        return OriginalControl.getHTML();
    }
    else if (controlType == 'useActiveEdit')
    {
        return OriginalControl.DOM.body.innerHTML;
    }
    else if (controlType == 'useOWA2000')
    {
        return OriginalControl.frameWindow.document.body.innerHTML;
    }
    else if (controlType == 'divtag')
    {
        return OriginalControl.innerHTML;
    }
    else
    {
        return OriginalControl.value;
    }
}

function scSetControlText(OriginalControl, text, controlType)
{
    if (controlType == 'xdeedit')
    {
        //handle html editor
        if (!isNS)
        {
            OriginalControl.document.body.innerHTML = text;
        }
        else
        {
            OriginalControl.value = text;
            OriginalControl.frameWindow.document.body.innerHTML = text;
        }
    }
    // Tue Mar 09 01:04:28 2004 CvdL: added.
    else if (controlType == 'richedit')
    {
        OriginalControl.setHTML(text);
    }
    else if (controlType == 'useActiveEdit')
    {
        OriginalControl.DOM.body.innerHTML=text;
        OriginalControl.Refresh();
    }
    else if (controlType == 'useOWA2000')
    {
        OriginalControl.frameWindow.document.body.innerHTML=text;
        OriginalControl.Refresh();
    }
    else if (controlType == 'divtag')
    {
        OriginalControl.innerHTML = text;
    }
    else
    {
        OriginalControl.value = text;
    }
}

function isRtlLocale(locale)
{
    if (locale.indexOf("ar") == 0 ||
        locale.indexOf("he") == 0 ||
        locale.indexOf("fa") == 0 ||
        locale.indexOf("ur") == 0)
    {
        return true;
    }

    return false;
}

function uAlert(msg)
{
    alert(msg);
}

function uConfirm(msg)
{
    return confirm(msg);
}

