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
 * fi.js: Finnish support for FCKeditor v1.2.2
 *
 * Authors:
 *   Marko Korhonen (marko.korhonen@datafisher.com)
 */

// Toolbar Items and Context Menu

lang["Cut"]                 = "Leikkaa" ;
lang["Copy"]                = "Kopioi" ;
lang["Paste"]               = "Liitä" ;
lang["PasteText"]           = "Liitä tekstinä" ;
lang["PasteWord"]           = "Liitä Wordista" ;
lang["Find"]                = "Etsi" ;
lang["SelectAll"]           = "Valitse kaikki" ;
lang["RemoveFormat"]        = "Poista muotoilu" ;
lang["InsertLink"]          = "Lisää linkki/muokkaa linkkiä" ;
lang["InsertTermLink"]      = "Insert/Edit Term Link" ;
lang["RemoveLink"]          = "Poista linkki" ;
lang["InsertImage"]         = "Lisää kuva/muokkaa kuvaa" ;
lang["InsertEquation"]      = "Insert Equation" ;
lang["InsertTable"]         = "Lisä taulu/muokkaa taulua" ;
lang["InsertLine"]          = "Lisää murtoviiva" ;
lang["InsertSpecialChar"]   = "Lisää erikoismerkki" ;
lang["InsertSmiley"]        = "Lisää hymiö" ;
lang["About"]               = "Editorista" ;

lang["Bold"]                = "Lihavoitu" ;
lang["Italic"]              = "Kursivoitu" ;
lang["Underline"]           = "Alleviivattu" ;
lang["StrikeThrough"]       = "Yliviivattu" ;
lang["Subscript"]           = "Alaindeksi" ;
lang["Superscript"]         = "Yläindeksi" ;
lang["LeftJustify"]         = "Tasaa vasemmat reunat" ;
lang["CenterJustify"]       = "Keskitä" ;
lang["RightJustify"]        = "Tasaa oikeat reunat" ;
lang["BlockJustify"]        = "Tasaa molemmat reunat" ;
lang["DecreaseIndent"]      = "Pienennä sisennystä" ;
lang["IncreaseIndent"]      = "Suurenna sisennystä" ;
lang["Undo"]                = "Kumoa" ;
lang["Redo"]                = "Toista" ;
lang["NumberedList"]        = "Numerointi" ;
lang["BulettedList"]        = "Luettelomerkit" ;

lang["ShowTableBorders"]    = "Näytä taulurajat" ;
lang["ShowDetails"]         = "Näytä muotoilumerkit" ;

lang["FontStyle"]           = "Tyyli" ;
lang["FontFormat"]          = "Formaatti" ;
lang["Font"]                = "Fontti" ;
lang["FontSize"]            = "Koko" ;
lang["TextColor"]           = "Tekstin väri" ;
lang["BGColor"]             = "Taustaväri" ;
lang["Source"]              = "Koodi" ;

// Context Menu

lang["EditLink"]            = "Muokkaa linkkiä" ;
lang["InsertRow"]           = "Lisää rivi" ;
lang["DeleteRows"]          = "Poista rivit" ;
lang["InsertColumn"]        = "Lisää sarake" ;
lang["DeleteColumns"]       = "Poista sarakkeet" ;
lang["InsertCell"]          = "Lisää solu" ;
lang["DeleteCells"]         = "Poista solut" ;
lang["MergeCells"]          = "Yhdistä solut" ;
lang["SplitCell"]           = "Jaa solu" ;
lang["CellProperties"]      = "Solun ominaisuudet" ;
lang["TableProperties"]     = "Taulun ominaisuudet" ;
lang["ImageProperties"]     = "Kuvan ominaisuudet" ;

// Alerts and Messages

lang["ProcessingXHTML"]     = "Ajetaan XHTML:ää. Hetkinen..." ;
lang["Done"]                = "Valmis" ;
lang["PasteWordConfirm"]    = "Teksti, jonka haluat liittää, näyttää olevan kopioidun Wordista. Haluatko puhdistaa sen ennen liittämistä? (Suositeltavaa)" ;
lang["NotCompatiblePaste"]  = "Tämä komento on mahdollista ajaa Internet Explorerin versiolla 5.5 tai uudemmalla. Haluatko liittää tekstin ilman puhdistusta?" ;

// Dialogs
lang["DlgBtnOK"]            = "OK" ;
lang["DlgBtnCancel"]        = "Peruuta" ;
lang["DlgBtnClose"]         = "Sulje" ;

// Image Dialog
lang["DlgImgTitleInsert"]   = "Lisää kuva" ;
lang["DlgImgTitleEdit"]     = "Muokkaa kuvaa" ;
lang["DlgImgBtnUpload"]     = "Lähetä kuva serverille" ;
lang["DlgImgURL"]           = "URL" ;
lang["DlgImgUpload"]        = "Lähetä" ;
lang["DlgImgBtnBrowse"]     = "Selaa serveriä" ;
lang["DlgImgAlt"]           = "Vaihtoehtoinen teksti" ;
lang["DlgImgWidth"]         = "Leveys" ;
lang["DlgImgHeight"]        = "Korkeus" ;
lang["DlgImgLockRatio"]     = "Lukitse suhteet" ;
lang["DlgBtnResetSize"]     = "Alkuperäinen koko" ;
lang["DlgImgBorder"]        = "Reunaviiva" ;
lang["DlgImgHSpace"]        = "Vaakatila" ;
lang["DlgImgVSpace"]        = "Pystytila" ;
lang["DlgImgAlign"]         = "Kohdistus" ;
lang["DlgImgAlignLeft"]     = "Vasemmalle" ;
lang["DlgImgAlignAbsBottom"]    = "Kuvan ja rivin alareuna" ;
lang["DlgImgAlignAbsMiddle"]    = "Kuvan ja rivin keskelle" ;
lang["DlgImgAlignBaseline"] = "Rivin alareuna" ;
lang["DlgImgAlignBottom"]   = "Kuvan alareuna" ;
lang["DlgImgAlignMiddle"]   = "Keskelle" ;
lang["DlgImgAlignRight"]    = "Oikealle" ;
lang["DlgImgAlignTextTop"]  = "Ylös (teksti)" ;
lang["DlgImgAlignTop"]      = "Ylös" ;
lang["DlgImgPreview"]       = "Esikatselu" ;
lang["DlgImgMsgWrongExt"]   = "Vain seuraavat tiedostotyypit ovat sallittuja:\n\n" + config.ImageUploadAllowedExtensions + "\n\nToiminto peruutettiin." ;
lang["DlgImgAlertSelect"]   = "Valitse kuva, jonka haluat lisätä." ;      // NEW


// Link Dialog
lang["DlgLnkWindowTitle"]   = "Linkki" ;        // NEW
lang["DlgLnkURL"]           = "URL" ;
lang["DlgLnkUpload"]        = "Kuvan lähetys" ;
lang["DlgLnkTarget"]        = "Kohdesivu" ;
lang["DlgLnkTargetNotSet"]  = "<Ei asetettu>" ;
lang["DlgLnkTargetBlank"]   = "Uuteen ikkunaan (_blank)" ;
lang["DlgLnkTargetParent"]  = "Kehykseen, josta kutsuttiin (_parent)" ;
lang["DlgLnkTargetSelf"]    = "Samaan kehykseen (_self)" ;
lang["DlgLnkTargetTop"]     = "Päällimmäiseksi (_top)" ;
lang["DlgLnkTitle"]         = "Nimi" ;
lang["DlgLnkBtnUpload"]     = "Lähetä serverille" ;
lang["DlgLnkBtnBrowse"]     = "Selaa serveriä" ;
lang["DlgLnkMsgWrongExtA"]  = "Vain seuraavat tiedostotyypit ovat sallittuja:\n\n" + config.LinkUploadAllowedExtensions + "\n\nToiminto Peruutettiin." ;
lang["DlgLnkMsgWrongExtD"]  = "Seuraavat tiedostotyypit eivät ole sallittuja:\n\n" + config.LinkUploadDeniedExtensions + "\n\nToiminto peruutettiin." ;

// Term Link Dialog (Mon Jan 17 23:27:37 2005 CvdL)
lang["DlgTLnkWindowTitle"]   = "Term Link" ;
lang["DlgTLnkTerm"]          = "Link to term" ;
lang["DlgTLnkLang"]          = "language" ;
lang["DlgTLnkBtnBrowse"]     = "Browse Language" ;
lang["DlgTLnkBtnPrevious"]   = "Previous" ;
lang["DlgTLnkBtnNext"]       = "Next" ;

// Equation Editor Dialog (Thu Mar 10 15:07:35 2005 CvdL)
lang["DlgEqWindowTitle"]     = "Equation Editor";
lang["DlgEqEnterEquation"]   = "Enter equation:";
lang["DlgEqSyntaxHelp"]      = "Syntax Help";
lang["DlgEqPreview"]         = "Preview:";

// Color Dialog
lang["DlgColorTitle"]       = "Valitse väri" ;
lang["DlgColorBtnClear"]    = "Tyhjennä" ;
lang["DlgColorHighlight"]   = "Kohdistettu" ;
lang["DlgColorSelected"]    = "Valittu" ;

// Smiley Dialog
lang["DlgSmileyTitle"]      = "Lisää hymiö" ;

// Special Character Dialog
lang["DlgSpecialCharTitle"] = "Lisää erikoismerkki" ;

// Table Dialog
lang["DlgTableTitleInsert"] = "Lisää taulu" ;
lang["DlgTableTitleEdit"]   = "Muokkaa taulua" ;
lang["DlgTableRows"]        = "Rivit" ;
lang["DlgTableColumns"]     = "Sarakkeet" ;
lang["DlgTableBorder"]      = "Rajan paksuus" ;
lang["DlgTableAlign"]       = "Kohdistus" ;
lang["DlgTableAlignNotSet"] = "<Ei asetettu>" ;
lang["DlgTableAlignLeft"]   = "Vasemmalle" ;
lang["DlgTableAlignCenter"] = "Keskelle" ;
lang["DlgTableAlignRight"]  = "Oikealle" ;
lang["DlgTableWidth"]       = "Leveys" ;
lang["DlgTableWidthPx"]     = "pikseliä" ;
lang["DlgTableWidthPc"]     = "prosenttia" ;
lang["DlgTableHeight"]      = "Korkeus" ;
lang["DlgTableCellSpace"]   = "Solujen etäisyys toisistaan" ;
lang["DlgTableCellPad"]     = "Solun sisäinen marginaali" ;
lang["DlgTableCaption"]     = "Otsikko" ;

// Table Cell Dialog
lang["DlgCellTitle"]        = "Solun ominaisuudet" ;
lang["DlgCellWidth"]        = "Leveys" ;
lang["DlgCellWidthPx"]      = "pikseliä" ;
lang["DlgCellWidthPc"]      = "prosenttia" ;
lang["DlgCellHeight"]       = "Korkeus" ;
lang["DlgCellWordWrap"]     = "Sanankierrätys" ;
lang["DlgCellWordWrapNotSet"]   = "<Ei asetettu>" ;
lang["DlgCellWordWrapYes"]      = "Kyllä" ;
lang["DlgCellWordWrapNo"]       = "Ei" ;
lang["DlgCellHorAlign"]     = "Vaakakohdistus" ;
lang["DlgCellHorAlignNotSet"]   = "<Ei asetettu>" ;
lang["DlgCellHorAlignLeft"]     = "Vasemmalle" ;
lang["DlgCellHorAlignCenter"]   = "Keskelle" ;
lang["DlgCellHorAlignRight"]    = "Oikealle" ;
lang["DlgCellVerAlign"]     = "Pystykohdistus" ;
lang["DlgCellVerAlignNotSet"]   = "<Ei asetettu>" ;
lang["DlgCellVerAlignTop"]      = "Ylös" ;
lang["DlgCellVerAlignMiddle"]   = "Keskelle" ;
lang["DlgCellVerAlignBottom"]   = "Kuvan alareuna" ;
lang["DlgCellVerAlignBaseline"] = "Rivin alareuna" ;
lang["DlgCellRowSpan"]      = "Solun rivikorkeus" ;
lang["DlgCellCollSpan"]     = "Solun sarakeleveys" ;
lang["DlgCellBackColor"]    = "Tausta väri" ;
lang["DlgCellBorderColor"]  = "Rajan väri" ;
lang["DlgCellBtnSelect"]    = "Valitse..." ;

// About Dialog
lang["DlgAboutVersion"]     = "versio" ;
lang["DlgAboutLicense"]     = "GNU Lesser General Public License -lisenssin allaoleva ohjelmistokirjasto" ;
lang["DlgAboutInfo"]        = "Lisätietoa editorista saat seuraavasta osoitteesta:" ;
