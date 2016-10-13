/*
Spell Server Processor Page
(c)2001 XDE,Inc.
Only for use with XDE products, no other use licensed
This copyright header must be shown in top of current file code.
$RCSfile: SpellCheckNoApplet.js,v $
$Revision: 1.1 $
$Date: 2009/04/14 15:42:52 $
------------------------------------------------------------

SpellServerHref:
JSP/Java/Domino Server Servlet Support Use : /globalsight/xdespellchecker/xdeSpellNoApplet
ASP COM Use : SC_process_htmlOnly.asp
ASP COM Spell Proxy Use : sc_proxy.asp
*/


var SpellServerHref="/globalsight/xdespellchecker/xdeSpellNoApplet";
//update to reflect the installation location of the spellWindow
var SpellingWindowURL="/globalsight/xdespellchecker/noapplet/SpellWindow.htm";

var NoHtml=false;
var isMSIE=(navigator.appName=='Microsoft Internet Explorer');
var lBrowserVer=parseInt(navigator.appVersion);
if (isMSIE)
{
    var verStartIdx=navigator.appVersion.indexOf("MSIE ");
    var verEndIdx=navigator.appVersion.indexOf(";",verStartIdx);
    lBrowserVer=parseFloat(navigator.appVersion.substring(verStartIdx+5,verEndIdx));
}
var isNS=navigator.appName=='Netscape';
var isMac=navigator.appVersion.indexOf("Mac") > 0;
var mainwindowloaded=false;
var LastStartPosition=0; //reset between calls to search and replace.
var TotalMispellings=0;

function doSpell(opener,Language,p_ctrl,reloadnow,customDictionaryName,UILanguage)
{
    /* p_ctrl has several special parameters:
       typectrl=[type of control]
       exec=[JavaScript/VBScript method to call on parent window (yours)]
       e.g. save a form when finish has been selected.
    */

    if (parent.status=='Spell Check Started')
    {
        //added 1.31
        uAlert("Spell Check already in progress, please wait.");
        return;
    }

    var spellingWindow;
    var x = (screen.width - 450) / 2;
    var y = (screen.height - 300) / 2;

    x = 100;
    y = 100;
    opener.status='Spell Check Started';
    if (isMSIE&&(parseInt (navigator.appVersion)< 4))
    {
        uAlert("This SpellChecker Only supports IE 4.0+ or NS 4.0+");
        return;
    }
    if (typeof(customDictionaryName) == 'undefined' ||
        customDictionaryName == '')
    {
        customDictionaryName = randomString(3);
    }
    if (typeof(UILanguage) == 'undefined') UILanguage="usenglish";

    spellingWindow=window.open(SpellingWindowURL + "?language=" + Language +
        "&ctrl=" + p_ctrl + "&customDictionaryName=" + customDictionaryName +
        "&UILanguage=" + UILanguage, null,
        "top=" + x + ",left=" + y +",width=450,height=300,toolbar=no,resizable=no");

    if (isNS)
    {
        spellingWindow.window.focus();
    }

    if (!reloadnow)
    {
        return spellingWindow;
    }
}

function CheckWord(WordtoCheck)
{
    var pfd = parent.footerframe.document;
    var pmd = parent.mainframe.document;
    //Move to current word via href #
    //Load array
    //populate suggestions
    currentword = WordtoCheck;
    WordtoCheck = StdreplaceString(0, WordtoCheck, "|XDEapos|", "'", true);

    parent.document.title = 'Looking up suggestions for ' + WordtoCheck;

    if (isMSIE)
    {
        // CKH Added 04/22/2002 avoid mac resource crash,
        // do not remove browser check.
        pfd.close();
        pfd.open();
    }

    pfd.write('<html><body><FORM ENCTYPE="application/x-www-form-urlencoded" ACTION="'+SpellServerHref+'" NAME="SpellForm" target="_self" METHOD="POST">');
    pfd.write('<input type="text" name="texttocheck" value="'+escape(WordtoCheck)+'">');
    var language = pmd.spellcheckform.language.value;
    pfd.write('<input type="text" name="language" value="'+escape(language)+'">');
    var customDictionaryName = pmd.spellcheckform.customDictionaryName.value;
    pfd.write('<input type="text" name="customDictionaryName" value="'+escape(customDictionaryName)+'">');
    pfd.write('<INPUT TYPE="submit" NAME="Submit" VALUE="Submit">');
    pfd.write('</FORM></body></html>');
    pfd.forms[0].texttocheck.value = escape(WordtoCheck);
    pfd.forms[0].submit();

    pmd.spellcheckform.currentword.value = WordtoCheck;

    parent.document.title =
        WordtoCheck + pmd.spellcheckform.misSpelledMessage.value;
}

function RefreshText()
{
    var pmd = parent.mainframe.document;
    if (typeof(pmd.spellcheckform) == 'undefined' ||
        typeof(pmd.spellcheckform.MispelledWords) == 'undefined')
    {
        //check if available yet.
        setTimeout("RefreshText()", 300);
        return;
    }
    LastStartPosition = 0; //Note last start position is a global.
    var str = pmd.spellcheckform.MispelledWords.value;
    var fixedtext = pmd.spellcheckform.fixedtext.value;
    var TempDisplay = fixedtext;
    var oldstart = 0;
    var FirstWord = '';
    TotalMispellings = 0;
    var currentError = -1;
    var arr = str.split("|XDE|");
    for (var i = 0; i < arr.length; i++)
    {
        var tmpval = arr[i];
        if (tmpval.length > 0 && tmpval != '')
        {
            if (TotalMispellings == 0)
            {
                FirstWord = tmpval;
            }
            if (tmpval.indexOf('-ISOK') > -1)
            {
                var tmpstr = tmpval.substr(0, tmpval.length - 5);
                LastStartPosition =
                    TempDisplay.indexOf(tmpstr, oldstart) + tmpstr.length;
            }
            else
            {
                newtmpval = StdreplaceString(0, tmpval, "'", "|XDEapos|", true);
                var ReplaceWith = '';
                if (pmd.spellcheckform.currentword.value == tmpval)
                {
                    ReplaceWith = '<FONT SIZE="+1">' +
                        '<a name=\'word' + TotalMispellings +
                        '\' class=SpellError href="javascript:CheckWord(\'' +
                        newtmpval + '\');">' + tmpval + '</a></FONT>';
                    if (currentError == -1)
                    {
                        currentError = TotalMispellings; //save for focus
                    }
                }
                else
                {
                    ReplaceWith = '<a name=\'word' + TotalMispellings +
                        '\' class=SpellError href="javascript:CheckWord(\'' +
                        newtmpval+ '\');">' + tmpval + '</a>';
                }

                TotalMispellings++;

                // Note last start position is a global.
                oldstart = LastStartPosition;

                TempDisplay = replaceString(LastStartPosition, TempDisplay,
                    tmpval, ReplaceWith);

                fixedone = true;
            }
        }
    }

    if (NoHtml)
    {
        //Don't do if we have an html delimiter, should be user input later
        TempDisplay = replaceString(0, TempDisplay, '\n', '<br>', true);
    }

    if (isNS)
    {
        with (parent.topframe.document)
            {
            TempDisplay2 = '<SPAN STYLE="position: absolute; border: 0px red solid; width: 100%">' + TempDisplay + '</SPAN>';
            open();
            write(TempDisplay2);
            close();
            }
    }
    else
    {
        //if(isNS){
        if (typeof(parent.topframe) == 'undefined' ||
            typeof(parent.topframe.foundSoFar) == 'undefined')
        {
            // alert("parent.topframe is undefined, restarting method - " +
            // "TempDisplay=" + TempDisplay);
            setTimeout("RefreshText()", 300); //not ready yet.
            return;
        }

        parent.topframe.foundSoFar.innerHTML = TempDisplay;

        if (isMSIE && !isMac)
        {
            //assign focus to current word
            for (i = 0; i < parent.topframe.document.links.length; i++)
            {
                if (parent.topframe.document.links[i].name == 'word' + currentError)
                {
                    // alert("Setting focus on link " + i +
                    // ' (word' + currentError + ')');
                    parent.topframe.document.links[i].focus();
                }
            }
        }
    }

    if (TotalMispellings == 0)
    {
        parent.document.title = pmd.spellcheckform.Spellfinished.value;
        pmd.spellcheckform.suggestionlist.options.length = 1;
        pmd.spellcheckform.tchangeto.focus();

        if (uConfirm(pmd.spellcheckform.Spellfinished.value))
        {
            finishSpellCheck();
        }
        else
        {
            CancelSpellCheck();
        }
    }

    //Add finishSpellCheck() function to the end of RefreshText()
    //function to automatically close window
}

function SelectWord()
{
    var sdFrm = self.document.spellcheckform;
    var listindex = sdFrm.suggestionlist.selectedIndex;
    sdFrm.tchangeto.value = sdFrm.suggestionlist.options[listindex].text;
}

function CancelSpellCheck()
{
    parent.opener.status="Spell Check Completed";//added 1.31
    parent.close();
}

function finishSpellCheck()
{
    var pdlString = parent.document.location.toString();
    //return text to calling form control.
    var textobjectcheck = getQueryParm(pdlString, "ctrl");
    var controlType = getQueryParm(pdlString, "typectrl");
    var executeWhenComplete = getQueryParm(pdlString, "exec");
    var OriginalControl = eval('parent.opener.document.' + textobjectcheck) ||
        parent.opener.document.getElementById(textobjectcheck);
    setControlText(OriginalControl, self.document.spellcheckform.fixedtext.value,
        controlType);
    if (executeWhenComplete != "")
    {
        //code to execute when finish selected.
        eval("parent.window.opener." + executeWhenComplete);
    }

    parent.opener.status = "Spell Check Completed";//added 1.31

    parent.close();
}

function ServerSpellCompleted()
{
    parent.document.title = 'Please correct highlighted text.';
}

function CheckNextWord(LastWord)
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
        var tmpval = arr[i];
        var stripped = StdreplaceString(0, tmpval, '-ISOK', '', true);
        if (tmpval.length > 0 && stripped == LastWord)
        {
            tryit = true;
        }
        if (tryit)
        {
            if (i+1 < arr.length)
            {
                if (arr[i+1] != '' && arr[i+1].indexOf('-ISOK') == -1)
                {
                    CheckWord(arr[i+1]);
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

    RefreshText();
}

function ChangeWord()
{
    var sdf = self.document.spellcheckform;
    var WordtoChange = sdf.currentword.value;
    var tmpchangeto = sdf.tchangeto.value;
    var ReplaceWith = tmpchangeto;
    var fixedtext = sdf.fixedtext.value;
    var TempDisplay = fixedtext;
    var str = sdf.MispelledWords.value;
    var oldstart = 0;
    var foundWordAt = 0;
    sdf.MispelledWords.value = '';

    var arr = str.split("|XDE|");
    for (var i = 0; i < arr.length; i++)
    {
        var tmpval = arr[i];
        if (tmpval.length > 0 && tmpval != '')
        {
            if (tmpval.indexOf('-ISOK') > -1)
            {
                var tmpstr = tmpval.substr(0, tmpval.length-5);
                if (tmpstr == WordtoChange)
                {
                    foundWordAt =
                        TempDisplay.indexOf(tmpstr, oldstart) + tmpstr.length;
                    oldstart = foundWordAt;
                }
            }
        }
    }

    var MisspelledWordsLeft = '';
    var AddRemainingMispelledWords = false; //use later for change all

    //Rebuild the mis-spelled words buffer, only do once otherwise
    //second mispelling of same word will be ignored.
    MisspelledWordsLeft = StdreplaceString(0, str, WordtoChange + '|XDE|',
        ReplaceWith + '-ISOK|XDE|', false);
    self.document.spellcheckform.MispelledWords.value = MisspelledWordsLeft;
    TempDisplay = replaceString(
        foundWordAt, TempDisplay, WordtoChange, ReplaceWith);
    self.document.spellcheckform.fixedtext.value = TempDisplay;
    CheckNextWord(ReplaceWith);
}

function ChangeAll()
{
    var WordtoChange = self.document.spellcheckform.currentword.value;
    var tmpchangeto = self.document.spellcheckform.tchangeto.value;
    var ReplaceWith = tmpchangeto;
    var fixedtext = self.document.spellcheckform.fixedtext.value;
    var TempDisplay = fixedtext;
    var str = self.document.spellcheckform.MispelledWords.value;
    self.document.spellcheckform.MispelledWords.value = '';
    var arr = str.split("|XDE|");
    var MisspelledWordsLeft = '';
    var AddRemainingMispelledWords = false; //use later for change all
    //Rebuild the mis-spelled words buffer.
    MisspelledWordsLeft = StdreplaceString(0, str, WordtoChange + '|XDE|',
        WordtoChange + '-ISOK|XDE|', true);
    self.document.spellcheckform.MispelledWords.value = MisspelledWordsLeft;
    TempDisplay= replaceString(0, TempDisplay, WordtoChange, ReplaceWith, true);
    self.document.spellcheckform.fixedtext.value = TempDisplay;
    CheckNextWord(WordtoChange);
}

function Ignore()
{
    var WordtoChange = self.document.spellcheckform.currentword.value;
    var str = self.document.spellcheckform.MispelledWords.value;
    var MisspelledWordsLeft = '';
    MisspelledWordsLeft = StdreplaceString(0, str, WordtoChange + '|XDE|',
        WordtoChange + '-ISOK|XDE|', false);
    self.document.spellcheckform.MispelledWords.value = MisspelledWordsLeft;
    CheckNextWord(WordtoChange);
}

function IgnoreAll()
{
    var WordtoChange = self.document.spellcheckform.currentword.value;
    var str = self.document.spellcheckform.MispelledWords.value;
    var MisspelledWordsLeft = '';
    MisspelledWordsLeft = StdreplaceString(0, str, WordtoChange + '|XDE|',
        WordtoChange + '-ISOK|XDE|', true);
    self.document.spellcheckform.MispelledWords.value = MisspelledWordsLeft;
    CheckNextWord(WordtoChange);
}

function AddWord()
{
    var pmd = parent.mainframe.document;
    var pfd = parent.footerframe.document;
    var WordtoChange = self.document.spellcheckform.currentword.value;
    var str = self.document.spellcheckform.MispelledWords.value;
    var MisspelledWordsLeft = '';
    MisspelledWordsLeft = StdreplaceString(0, str, WordtoChange + '|XDE|',
        WordtoChange + '-ISOK|XDE|', true);
    self.document.spellcheckform.MispelledWords.value = MisspelledWordsLeft;

    parent.document.title = 'Adding ' + WordtoChange;

    pfd.write('<FORM ENCTYPE="application/x-www-form-urlencoded" ACTION="'+SpellServerHref+'" NAME="SpellForm" target="_self" METHOD="POST">');
    pfd.write('<TEXTAREA NAME="xdewordtoadd" ROWS="10" COLS="80"></TEXTAREA>');
    pfd.write('<TEXTAREA NAME="customDictionaryName" ROWS="10" COLS="80"></TEXTAREA>');
    pfd.write('<INPUT TYPE="submit" NAME="Submit" VALUE="Submit">');
    pfd.write('</FORM>');
    pfd.SpellForm.xdewordtoadd.value=escape(WordtoChange);
    pfd.SpellForm.customDictionaryName.value=escape(pmd.spellcheckform.customDictionaryName.value);

    pfd.forms[0].submit();

    var startindex=0;
    //Notify the user as to what is happening.  Do not remove this
    //alert, it also provides time for the request to be processed.

    uAlert('Adding "' + WordtoChange + '" to custom dictionary');

    CheckNextWord(WordtoChange);
}

//General Functions
function replaceString(StartPos, SearchString, FindText, ReplaceWithText, doall)
{
    var changeall = false, intag = false;
    var TempMatched, fSubstring, sSubstring;
    // var AlphaCharacters = 'abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ°µ¿¡¬√ƒ≈∆«»… ÀÃÕŒœ–—“”‘’÷◊ÿŸ⁄€‹›ﬁﬂ‡·‚„‰ÂÊÁËÈÍÎÏÌÓÔÒÚÛÙıˆ¯˘˙˚¸˝˛ˇ\\';
    var NonAlphaCharacters =
        ' `~!@#$%^&*()_-=+{}[]\\|:;"\'\u00ab\u00bb,./<>?1234567890';

    if (doall == true)
    {
        changeall = true;
    }

    TempMatched = SearchString.indexOf(FindText, StartPos);
    if (TempMatched == -1)
    {
        return SearchString;
    }

    for (var i = 0; i < SearchString.length; i++)
    {
        if (i > TempMatched)
        {
            TempMatched = SearchString.indexOf(FindText, i);
        }
        if (TempMatched == -1)
        {
            return SearchString;
        }

        StartPos = TempMatched + FindText.length;

        var tmpval = SearchString.charAt(i);

        if (tmpval == '<')
        {
            intag = true;
        }

        if (tmpval == '>' && intag == true)
        {
            intag = false;
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (TempMatched == i)
        {
            if (TempMatched == 0)
            {
                PreviousChar = ' ';
            }
            else
            {
                PreviousChar = SearchString.charAt(TempMatched - 1);
            }

            if (StartPos < SearchString.length)
            {
                LastChar = SearchString.charAt(StartPos);
            }
            else
            {
                LastChar = ' ';
            }

            //Check if we're currently in the middle of another word.
            if (NonAlphaCharacters.indexOf(PreviousChar) > -1 &
                NonAlphaCharacters.indexOf(LastChar) > -1)
            {
                //Casual we have a whole word.
                fSubstring = SearchString.substring(0, TempMatched);
                sSubstring = SearchString.substring(TempMatched + FindText.length,
                    SearchString.length);
                SearchString = fSubstring + ReplaceWithText + sSubstring;
                //move forward to skip this partial word.
                LastStartPosition = fSubstring.length + ReplaceWithText.length;
                if (!changeall)
                {
                    break;
                }
            }
            else
            {
                //move forward to skip this partial word.
                LastStartPosition = StartPos;
            }
        }
    }

    return SearchString; //Return changed text
}

function StdreplaceString(StartPos, SearchString, FindText, ReplaceWithText, changeall)
{
    //be sure to reset LastStartPosition between functions.
    var TempMatched = 0, fSubstring, sSubstring, intag = false;

    TempMatched = SearchString.indexOf(FindText, StartPos);
    if (TempMatched == -1)
    {
        return SearchString;
    }

    for (var i = 0; i < SearchString.length; i++)
    {
        if (i > TempMatched)
        {
            TempMatched = SearchString.indexOf(FindText, i);
        }

        if (TempMatched ==- 1)
        {
            return SearchString;
        }

        var tmpval = SearchString.charAt(i);

        if (tmpval == '<')
        {
            intag = true;
        }

        if (tmpval == '>' && intag == true)
        {
            intag = false;
            continue;
        }

        if (intag)
        {
            continue;
        }

        if (TempMatched == i)
        {
            StartPos = TempMatched + ReplaceWithText.length;
            fSubstring = SearchString.substring(0, TempMatched);
            sSubstring = SearchString.substring(
                TempMatched + FindText.length, SearchString.length);
            SearchString = fSubstring + ReplaceWithText + sSubstring;

            if (!changeall)
            {
                return SearchString; //Return changed text  break;
            }
            else
            {
                TempMatched = SearchString.indexOf(FindText, StartPos);
            }
        }
    }

    return SearchString; //Return changed text
}

function getQueryParm(pURL,ParmtoGet)
{
    var StartPos = pURL.indexOf(ParmtoGet);
    var ParmValue = '';
    if (StartPos >- 1)
    {
        var lookfor = '&';
        var NextPos = pURL.indexOf(lookfor, StartPos);
        if (NextPos == -1)
        {
            NextPos = pURL.length;
        }
        ParmValue = pURL.substring(StartPos + ParmtoGet.length + 1, NextPos);
    }

    return ParmValue;
}

function StripPtags(s)
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

function StripHTML(s)
{
    var intag = false;
    var result = "";
    var addchar = "";

    s = StdreplaceString(0, s, "&nbsp;", " ", true);
    s = StdreplaceString(0, s, "\u00a0", " ", true);

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

function CancelAllSpellCheck()
{
    //added by sothebys.com, used with multiText Box
    if (parent.opener.xdearr)
    {
        parent.opener.xdearr.length = 0;
    }

    CancelSpellCheck();
}

function isJSDefined(pobject)
{
    var t = typeof pobject;
    if (t == "string")
    {
        return false;
    }

    return t != "undefined" && (t != "object" || String(pobject) != "undefined")
}

function getControlText(OriginalControl, controlType)
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
    else if(controlType == 'richedit')
    {
        return OriginalControl.getHTML();
    }
    else if(controlType == 'useActiveEdit')
    {
        return OriginalControl.DOM.body.innerHTML;
    }
    else if(controlType == 'useOWA2000')
    {
        return OriginalControl.frameWindow.document.body.innerHTML;
    }
    else if(controlType == 'divtag')
    {
        return OriginalControl.innerHTML;
    }
    else
    {
        return OriginalControl.value;
    }
}

function setControlText(OriginalControl,text,controlType)
{
    if (controlType == 'xdeedit')
    {
        //handle html editor
        if(!isNS)
            OriginalControl.document.body.innerHTML=text;
        else
        {
            OriginalControl.value=text;
            OriginalControl.frameWindow.document.body.innerHTML=text;
        }
    }
    // Tue Mar 09 01:04:28 2004 CvdL: added.
    else if(controlType == 'richedit')
    {
        OriginalControl.setHTML(text);
    }
    else if(controlType == 'useActiveEdit')
    {
        OriginalControl.DOM.body.innerHTML=text;
        OriginalControl.Refresh();
    }
    else if(controlType == 'useOWA2000')
    {
        OriginalControl.frameWindow.document.body.innerHTML=text;
        OriginalControl.Refresh();
    }
    else if(controlType == 'divtag')
    {
        OriginalControl.innerHTML=text;
    }
    else
    {
        OriginalControl.value=text;
    }
}

function uAlert(msg)
{
    alert(msg);
    return 1;
}

function uConfirm(msg)
{
    return confirm(msg);
}

var xdecurrentItem;
var xdecurrentSpellWindow;
var xdearr, xdegLanguage;
var xdeisBusy;
var xdeFieldsChecked;
var xdeOpener;

function doSpellMultiple(opener1, Language, p_ctrl)
{
    xdeisBusy = false;
    xdegLanguage = Language;
    xdearr = p_ctrl.split(",");
    xdecurrentItem = 0;
    xdeOpener = opener1;
    xde_checkWindow(); // start the funtion
}

function xde_checkWindow()
{
   // something to do
    if (!xdeisBusy&xdecurrentItem < xdearr.length)
    {
        var tmpval = xdearr[xdecurrentItem];
        xdeisBusy = true;
        var OriginalControl2 = eval('document.' + tmpval);
        if (OriginalControl2.value != '')
        {
            xdecurrentSpellWindow =
                doSpell(xdeOpener, xdegLanguage, tmpval, false);
            xdeFieldsChecked++;
        }
    }

    if (typeof(xdecurrentSpellWindow) == "object")
    {
        if (xdecurrentSpellWindow.closed)
        {
            xdeisBusy=false;
            xdecurrentItem++;
        }
    }

   setTimeout('xde_checkWindow()', 300);
}

function enableSpellCheck()
{
    if (parent.opener)
    {
        parent.opener.status = '';
    }
}

function randomString(argLength)
{
    var charPool = "abcdefghijklmnopqrstuvwxyz123456789";

    var i = 0;
    var retVal = " ";
    while (i <= argLength)
    {
        rand = parseFloat(Math.random()) * parseInt(charPool.length);
        retVal += charPool.charAt(rand);
        i++;
    }

    retVal = retVal.substring(1, argLength + 1);

    return retVal;
}

