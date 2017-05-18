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
 * es.js: Spanish support.
 *
 * Authors:
 *   Gabriel Schillaci (gabriel@rapisitio.net)
 */

// Toolbar Items and Context Menu

lang["Cut"]                 = "Cortar" ;
lang["Copy"]                = "Copiar" ;
lang["Paste"]               = "Pegar" ;
lang["PasteText"]           = "Pegar como texto plano" ;
lang["PasteWord"]           = "Pegar desde Word" ;
lang["Find"]                = "Buscar" ;
lang["SelectAll"]           = "Seleccionar todo" ;
lang["RemoveFormat"]        = "Eliminar Formato" ;
lang["InsertLink"]          = "Insertar/Editar Vínculo" ;
lang["InsertTermLink"]      = "Insert/Edit Term Link" ;
lang["RemoveLink"]          = "Eliminar Vínculo" ;
lang["InsertImage"]         = "Insertar/Editar Imagen" ;
lang["InsertEquation"]      = "Insert Equation" ;
lang["InsertTable"]         = "Insertar/Editar Tabla" ;
lang["InsertLine"]          = "Insertar Línea Horizontal" ;
lang["InsertSpecialChar"]   = "Insertar Caracter Especial" ;
lang["InsertSmiley"]        = "Insertar Iconos" ;
lang["About"]               = "Acerca de FCKeditor" ;

lang["Bold"]                = "Negrita" ;
lang["Italic"]              = "Itálica" ;
lang["Underline"]           = "Subrayado" ;
lang["StrikeThrough"]       = "Tachado" ;
lang["Subscript"]           = "Superíndice" ;
lang["Superscript"]         = "Subíndice" ;
lang["LeftJustify"]         = "Alineado a Derecha" ;
lang["CenterJustify"]       = "Centrado" ;
lang["RightJustify"]        = "Alineado a Izquierda" ;
lang["BlockJustify"]        = "Justificado" ;
lang["DecreaseIndent"]      = "Disminuir Sangría" ;
lang["IncreaseIndent"]      = "Aumentar Sangría" ;
lang["Undo"]                = "Deshacer" ;
lang["Redo"]                = "Rehacer" ;
lang["NumberedList"]        = "Numeración" ;
lang["BulettedList"]        = "Viñetas" ;

lang["ShowTableBorders"]    = "Mostrar Bordes de Tablas" ;
lang["ShowDetails"]         = "Mostrar saltos de Párrafo" ;

lang["FontStyle"]           = "Estilo" ;
lang["FontFormat"]          = "Formato" ;
lang["Font"]                = "Fuente" ;
lang["FontSize"]            = "Tamaño" ;
lang["TextColor"]           = "Color de Texto" ;
lang["BGColor"]             = "Color de Fondo" ;
lang["Source"]              = "Fuente HTML" ;

// Context Menu

lang["EditLink"]            = "Editar Vínculo" ;
lang["InsertRow"]           = "Insertar Fila" ;
lang["DeleteRows"]          = "Eliminar Filas" ;
lang["InsertColumn"]        = "Insertar Columnas" ;
lang["DeleteColumns"]       = "Eliminar Columnas" ;
lang["InsertCell"]          = "Insertar Celdas" ;
lang["DeleteCells"]         = "Eliminar Celdas" ;
lang["MergeCells"]          = "Unir Celdas" ;
lang["SplitCell"]           = "Dividir Celda" ;
lang["CellProperties"]      = "Propiedades de Celda" ;
lang["TableProperties"]     = "Propiedades de Tabla" ;
lang["ImageProperties"]     = "Propiedades de Imagen" ;

// Alerts and Messages

lang["ProcessingXHTML"]     = "Procesando XHTML. Por favor aguarde..." ;
lang["Done"]                = "Hecho" ;
lang["PasteWordConfirm"]    = "El texto que desea pegar parece provenir de Word. Desea depurarlo antes de copiarlo?" ;
lang["NotCompatiblePaste"]  = "Este comando sólo está disponible para Internet Explorer 5.5 o superior. Desea pegar sin depurar?" ;

// Dialogs
lang["DlgBtnOK"]            = "OK" ;
lang["DlgBtnCancel"]        = "Cancelar" ;
lang["DlgBtnClose"]         = "Cerrar" ;

// Image Dialog
lang["DlgImgTitleInsert"]   = "Insertar Imagen" ;
lang["DlgImgTitleEdit"]     = "Editar Imagen" ;
lang["DlgImgBtnUpload"]     = "Enviar al Servidor" ;
lang["DlgImgURL"]           = "URL" ;
lang["DlgImgUpload"]        = "Cargar" ;
lang["DlgImgBtnBrowse"]     = "Ver Repositorio" ;
lang["DlgImgAlt"]           = "Texto Alternativo" ;
lang["DlgImgWidth"]         = "Anchura" ;
lang["DlgImgHeight"]        = "Altura" ;
lang["DlgImgLockRatio"]     = "Proporcional" ;
lang["DlgBtnResetSize"]     = "Tamaño Original" ;
lang["DlgImgBorder"]        = "Bordes" ;
lang["DlgImgHSpace"]        = "Esp.Horiz." ;
lang["DlgImgVSpace"]        = "Esp.Vert." ;
lang["DlgImgAlign"]         = "Alineación" ;
lang["DlgImgAlignLeft"]     = "Left" ;
lang["DlgImgAlignAbsBottom"]    = "Abs Bottom" ;
lang["DlgImgAlignAbsMiddle"]    = "Abs Middle" ;
lang["DlgImgAlignBaseline"] = "Baseline" ;
lang["DlgImgAlignBottom"]   = "Bottom" ;
lang["DlgImgAlignMiddle"]   = "Middle" ;
lang["DlgImgAlignRight"]    = "Right" ;
lang["DlgImgAlignTextTop"]  = "Text Top" ;
lang["DlgImgAlignTop"]      = "Top" ;
lang["DlgImgPreview"]       = "Vista Previa" ;
lang["DlgImgMsgWrongExt"]   = "Sólo se aceptan los siguientes tipos de archivo:\n\n" + config.ImageUploadAllowedExtensions + "\n\nOperación cancelada." ;
lang["DlgImgAlertSelect"]   = "Por favor seleccione una imagen a cargar." ;     // NEW


// Link Dialog
lang["DlgLnkWindowTitle"]   = "Vínculo" ;      // NEW
lang["DlgLnkURL"]           = "URL" ;
lang["DlgLnkUpload"]        = "Cargar" ;
lang["DlgLnkTarget"]        = "Target" ;
lang["DlgLnkTargetNotSet"]  = "<sin especificar>" ;
lang["DlgLnkTargetBlank"]   = "Nueva Ventana (_blank)" ;
lang["DlgLnkTargetParent"]  = "Ventana Padre (_parent)" ;
lang["DlgLnkTargetSelf"]    = "Misma Ventana (_self)" ;
lang["DlgLnkTargetTop"]     = "Ventana Principal (_top)" ;
lang["DlgLnkTitle"]         = "Título" ;
lang["DlgLnkBtnUpload"]     = "Enviar al Servidor" ;
lang["DlgLnkBtnBrowse"]     = "Ver Repositorio" ;
lang["DlgLnkMsgWrongExtA"]  = "Sólo se aceptan los siguientes tipos de archivo:\n\n" + config.LinkUploadAllowedExtensions + "\n\nOperación cancelada." ;
lang["DlgLnkMsgWrongExtD"]  = "Los siguientes tipos de archivo no son aceptados:\n\n" + config.LinkUploadDeniedExtensions + "\n\nOperación cancelada." ;

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
lang["DlgColorTitle"]       = "Seleccionar Color" ;
lang["DlgColorBtnClear"]    = "Borrar" ;
lang["DlgColorHighlight"]   = "Resaltar" ;
lang["DlgColorSelected"]    = "Selecionado" ;

// Smiley Dialog
lang["DlgSmileyTitle"]      = "Insertar un Icono" ;

// Special Character Dialog
lang["DlgSpecialCharTitle"] = "Insertar Caracter Especial" ;

// Table Dialog
lang["DlgTableTitleInsert"] = "Insertar Tabla" ;
lang["DlgTableTitleEdit"]   = "Editar Tabla" ;
lang["DlgTableRows"]        = "Filas" ;
lang["DlgTableColumns"]     = "Columnas" ;
lang["DlgTableBorder"]      = "Borde" ;
lang["DlgTableAlign"]       = "Alineación" ;
lang["DlgTableAlignNotSet"] = "<omitido>" ;
lang["DlgTableAlignLeft"]   = "Izquierda" ;
lang["DlgTableAlignCenter"] = "Centrado" ;
lang["DlgTableAlignRight"]  = "Derecha" ;
lang["DlgTableWidth"]       = "Anchura" ;
lang["DlgTableWidthPx"]     = "pixeles" ;
lang["DlgTableWidthPc"]     = "porcentaje" ;
lang["DlgTableHeight"]      = "Altura" ;
lang["DlgTableCellSpace"]   = "Esp. e/celdas" ;
lang["DlgTableCellPad"]     = "Esp. interior" ;
lang["DlgTableCaption"]     = "Título" ;

// Table Cell Dialog
lang["DlgCellTitle"]        = "Propiedades de Celda" ;
lang["DlgCellWidth"]        = "Anchura" ;
lang["DlgCellWidthPx"]      = "pixeles" ;
lang["DlgCellWidthPc"]      = "porcentaje" ;
lang["DlgCellHeight"]       = "Altura" ;
lang["DlgCellWordWrap"]     = "Cortar Línea" ;
lang["DlgCellWordWrapNotSet"]   = "<omitido>" ;
lang["DlgCellWordWrapYes"]      = "Sí" ;
lang["DlgCellWordWrapNo"]       = "No" ;
lang["DlgCellHorAlign"]     = "Alineación Horizontal" ;
lang["DlgCellHorAlignNotSet"]   = "<omitido>" ;
lang["DlgCellHorAlignLeft"]     = "Izquierda" ;
lang["DlgCellHorAlignCenter"]   = "Centrado" ;
lang["DlgCellHorAlignRight"]    = "Derecha" ;
lang["DlgCellVerAlign"]     = "Alineación Vertical" ;
lang["DlgCellVerAlignNotSet"]   = "<omitido>" ;
lang["DlgCellVerAlignTop"]      = "Top" ;
lang["DlgCellVerAlignMiddle"]   = "Middle" ;
lang["DlgCellVerAlignBottom"]   = "Bottom" ;
lang["DlgCellVerAlignBaseline"] = "Baseline" ;
lang["DlgCellRowSpan"]      = "Abarcar Filas" ;
lang["DlgCellCollSpan"]     = "Abarcar Columnas" ;
lang["DlgCellBackColor"]    = "Color de Fondo" ;
lang["DlgCellBorderColor"]  = "Color de borde" ;
lang["DlgCellBtnSelect"]    = "Seleccione..." ;

// About Dialog
lang["DlgAboutVersion"]     = "versión" ;
lang["DlgAboutLicense"]     = "Licenciado bajo los términos de la GNU Lesser General Public License" ;
lang["DlgAboutInfo"]        = "Para mayor información vaya a" ;
