/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003 Frederico Caldeira Knabben
 *
 * Licensed under the terms of the GNU Lesser General Public License
 * (http://www.opensource.org/licenses/lgpl-license.php)
 *
 * For further information go to http://www.fredck.com/FCKeditor/
 * or contact fckeditor@fredck.com.
 *
 * fck_toolbaritems.js: Defines all the available toolbar items.
 *
 * Authors:
 *   Frederico Caldeira Knabben (fckeditor@fredck.com)
 */

// This class holds the available toolbar items definitions
function TBI() {}

// Standard
TBI.prototype.Cut           = new TBButton("Cut"            , lang["Cut"]               , DECMD_CUT) ;
TBI.prototype.Copy          = new TBButton("Copy"           , lang["Copy"]              , DECMD_COPY) ;
TBI.prototype.Paste         = new TBButton("Paste"          , lang["Paste"]             , DECMD_PASTE) ;
TBI.prototype.PasteText     = new TBButton("PasteText"      , lang["PasteText"]         , "pastePlainText()"        , TBCMD_CUSTOM, "checkDecCommand(DECMD_PASTE)") ;
TBI.prototype.PasteWord     = new TBButton("PasteWord"      , lang["PasteWord"]         , "pasteFromWord()"         , TBCMD_CUSTOM, "checkDecCommand(DECMD_PASTE)") ;
TBI.prototype.Find          = new TBButton("Find"           , lang["Find"]              , DECMD_FINDTEXT) ;
TBI.prototype.SelectAll     = new TBButton("SelectAll"      , lang["SelectAll"]         , DECMD_SELECTALL) ;
TBI.prototype.RemoveFormat  = new TBButton("RemoveFormat"   , lang["RemoveFormat"]      , DECMD_REMOVEFORMAT) ;
TBI.prototype.Link          = new TBButton("Link"           , lang["InsertLink"]        , "dialogLink()"            , TBCMD_CUSTOM, "checkDecCommand(DECMD_HYPERLINK)") ;
TBI.prototype.TermLink      = new TBButton("TermLink"       , lang["InsertTermLink"]    , "dialogTermLink()"        , TBCMD_CUSTOM, "checkDecCommand(DECMD_HYPERLINK)") ;
TBI.prototype.RemoveLink    = new TBButton("Unlink"         , lang["RemoveLink"]        , DECMD_UNLINK) ;
TBI.prototype.Image         = new TBButton("Image"          , lang["InsertImage"]       , "dialogImage()"           , TBCMD_CUSTOM) ;
TBI.prototype.Equation      = new TBButton("Equation"       , lang["InsertEquation"]    , "dialogEquation()"        , TBCMD_CUSTOM) ;
TBI.prototype.Table         = new TBButton("Table"          , lang["InsertTable"]       , "dialogTable()"           , TBCMD_CUSTOM) ;
TBI.prototype.Rule          = new TBButton("Rule"           , lang["InsertLine"]        , "InsertHorizontalRule"    , TBCMD_DOC) ;
TBI.prototype.SpecialChar   = new TBButton("SpecialChar"    , lang["InsertSpecialChar"] , "insertSpecialChar()"     , TBCMD_CUSTOM) ;
TBI.prototype.Smiley        = new TBButton("Smiley"         , lang["InsertSmiley"]      , "insertSmiley()"          , TBCMD_CUSTOM) ;
TBI.prototype.About         = new TBButton("About"          , lang["About"]             , "about()"                 , TBCMD_CUSTOM) ;

// Formatting
TBI.prototype.Bold          = new TBButton("Bold"           , lang["Bold"]                  , DECMD_BOLD) ;
TBI.prototype.Italic        = new TBButton("Italic"         , lang["Italic"]                , DECMD_ITALIC) ;
TBI.prototype.Underline     = new TBButton("Underline"      , lang["Underline"]             , DECMD_UNDERLINE) ;
TBI.prototype.StrikeThrough = new TBButton("StrikeThrough"  , lang["StrikeThrough"]         , "strikethrough"       , TBCMD_DOC) ;
TBI.prototype.Subscript     = new TBButton("Subscript"      , lang["Subscript"]             , "subscript"           , TBCMD_DOC) ;
TBI.prototype.Superscript   = new TBButton("Superscript"    , lang["Superscript"]           , "superscript"         , TBCMD_DOC) ;
TBI.prototype.JustifyLeft   = new TBButton("JustifyLeft"    , lang["LeftJustify"]           , DECMD_JUSTIFYLEFT) ;
TBI.prototype.JustifyCenter = new TBButton("JustifyCenter"  , lang["CenterJustify"]         , DECMD_JUSTIFYCENTER) ;
TBI.prototype.JustifyRight  = new TBButton("JustifyRight"   , lang["RightJustify"]          , DECMD_JUSTIFYRIGHT) ;
TBI.prototype.JustifyFull   = new TBButton("JustifyFull"    , lang["BlockJustify"]          , "JustifyFull"         , TBCMD_DOC) ;
TBI.prototype.Outdent       = new TBButton("Outdent"        , lang["DecreaseIndent"]        , DECMD_OUTDENT) ;
TBI.prototype.Indent        = new TBButton("Indent"         , lang["IncreaseIndent"]        , DECMD_INDENT) ;
TBI.prototype.Undo          = new TBButton("Undo"           , lang["Undo"]                  , DECMD_UNDO) ;
TBI.prototype.Redo          = new TBButton("Redo"           , lang["Redo"]                  , DECMD_REDO) ;
TBI.prototype.InsertOrderedList     = new TBButton("InsertOrderedList"  , lang["NumberedList"], "insertList('ol')"  , TBCMD_CUSTOM, "checkDecCommand(DECMD_ORDERLIST)") ;
TBI.prototype.InsertUnorderedList   = new TBButton("InsertUnorderedList", lang["BulettedList"], "insertList('ul')"  , TBCMD_CUSTOM, "checkDecCommand(DECMD_UNORDERLIST)") ;

// Options
TBI.prototype.ShowTableBorders  = new TBButton("ShowTableBorders"   , lang["ShowTableBorders"]  , "showTableBorders()", TBCMD_CUSTOM, "checkShowTableBorders()") ;
TBI.prototype.ShowDetails       = new TBButton("ShowDetails"        , lang["ShowDetails"]       , "showDetails()",      TBCMD_CUSTOM, "checkShowDetails()") ;
TBI.prototype.Zoom              = new TBCombo( "Zoom"               , "doZoom(this)"            , "Zoom", "100%;50%;75%;100%;125%;150%;175%;200%", "100;50;75;100;125;150;175;200") ;
TBI.prototype.SpellCheck        = new TBButton("SpellCheck"         , "SpellCheck"              , "SpellCheck()"            , TBCMD_CUSTOM) ;

// Font
TBI.prototype.FontStyle     = new TBCombo( "FontStyle"      , "doStyle(this)"           , lang["FontStyle"] , config.StyleNames, config.StyleValues, 'CheckStyle("cmbFontStyle")') ;
TBI.prototype.FontFormat    = new TBCombo( "FontFormat"     , "doFormatBlock(this)"     , lang["FontFormat"], config.BlockFormatNames, config.BlockFormatNames, 'CheckFontFormat("cmbFontFormat")') ;
TBI.prototype.Font          = new TBCombo( "Font"           , "doFontName(this)"        , lang["Font"]      , config.ToolbarFontNames, config.ToolbarFontNames, 'CheckFontName("cmbFont")') ;
TBI.prototype.FontSize      = new TBCombo( "FontSize"       , "doFontSize(this)"        , lang["FontSize"]  , ';xx-small;x-small;small;medium;large;x-large;xx-large', ';1;2;3;4;5;6;7', 'CheckFontSize("cmbFontSize")') ;
TBI.prototype.TextColor     = new TBButton("TextColor"      , lang["TextColor"]         , "foreColor()"     , TBCMD_CUSTOM) ;
TBI.prototype.BGColor       = new TBButton("BGColor"        , lang["BGColor"]           , "backColor()"     , TBCMD_CUSTOM) ;
TBI.prototype.EditSource    = new TBCheckBox("EditSource"   , "switchEditMode()"        , lang["Source"]    , "onViewMode") ;

// This is the object that holds the available toolbar items
var oTB_Items = new TBI() ;
