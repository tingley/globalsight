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
 * ru.js: Russian support.
 *
 * Authors:
 *   Alexander Slesarenko (avsbox@mail.ru)
 */

// Toolbar Items and Context Menu

lang["Save"]                = "Сохранить" ;
lang["Reload"]              = "Обновить" ;
lang["Cut"]                 = "Вырезать" ;
lang["Copy"]                = "Копировать" ;
lang["Paste"]               = "Вставить" ;
lang["PasteText"]           = "Вставить как текст" ;
lang["PasteWord"]           = "Вставить из Word" ;
lang["Find"]                = "Найти" ;
lang["SelectAll"]           = "Выделить все" ;
lang["RemoveFormat"]        = "Удалить формат" ;
lang["InsertLink"]          = "Создать/Редактировать гиперссылку" ;
lang["InsertTermLink"]      = "Insert/Edit Term Link" ;
lang["RemoveLink"]          = "Удалить гиперссылку" ;
lang["InsertImage"]         = "Вставить/Редактировать рисунок" ;
lang["InsertEquation"]      = "Insert Equation" ;
lang["InsertTable"]         = "Вставить/Редактировать таблицу" ;
lang["InsertLine"]          = "Вставить горизонтальную линию" ;
lang["InsertSpecialChar"]   = "Вставить специальный символ" ;
lang["InsertSmiley"]        = "Вставить рожицу" ;
lang["About"]               = "О редакторе FCKeditor" ;

lang["Bold"]                = "Полужирный" ;
lang["Italic"]              = "Курсив" ;
lang["Underline"]           = "Подчеркнутый" ;
lang["StrikeThrough"]       = "Зачеркнутый" ;
lang["Subscript"]           = "Нижний индекс" ;
lang["Superscript"]         = "Верхний индекс" ;
lang["LeftJustify"]         = "По левому краю" ;
lang["CenterJustify"]       = "По центру" ;
lang["RightJustify"]        = "По правому краю" ;
lang["BlockJustify"]        = "По ширине" ;
lang["DecreaseIndent"]      = "Уменьшить отступ" ;
lang["IncreaseIndent"]      = "Увеличить отступ" ;
lang["Undo"]                = "Отменить" ;
lang["Redo"]                = "Вернуть" ;
lang["NumberedList"]        = "Нумерованный список" ;
lang["BulettedList"]        = "Маркированный список" ;

lang["ShowTableBorders"]    = "Границы таблиц" ;
lang["ShowDetails"]         = "Специальные знаки" ;

lang["FontStyle"]           = "Стиль" ;
lang["FontFormat"]          = "Формат" ;
lang["Font"]                = "Шрифт" ;
lang["FontSize"]            = "Размер" ;
lang["TextColor"]           = "Цвет текста" ;
lang["BGColor"]             = "Цвет фона" ;
lang["Source"]              = "Разметка" ;

// Context Menu

lang["EditLink"]            = "Изменить гиперссылку" ;
lang["InsertRow"]           = "Добавить строку" ;
lang["DeleteRows"]          = "Удалить строки" ;
lang["InsertColumn"]        = "Добавить столбец" ;
lang["DeleteColumns"]       = "Удалить столбцы" ;
lang["InsertCell"]          = "Добавить ячейку" ;
lang["DeleteCells"]         = "Удалить ячейки" ;
lang["MergeCells"]          = "Объединить ячейки" ;
lang["SplitCell"]           = "Разбить ячейки" ;
lang["CellProperties"]      = "Свойства ячейки" ;
lang["TableProperties"]     = "Свойства таблицы" ;
lang["ImageProperties"]     = "Свойства рисунка" ;

// Alerts and Messages

lang["ProcessingXHTML"]     = "Обработка XHTML. Пожалуйста подождите..." ;
lang["Done"]                = "Готово" ;
lang["PasteWordConfirm"]    = "Вставляемый текст похож на текст из Word. Выполнить чистку HTML разметки перед вставкой?" ;
lang["NotCompatiblePaste"]  = "Команда доступна для Internet Explorer версии 5.5 или выше. Выполнить вставку без очистки?" ;

// Dialogs
lang["DlgBtnOK"]            = "OK" ;
lang["DlgBtnCancel"]        = "Отмена" ;
lang["DlgBtnClose"]         = "Закрыть" ;

// Image Dialog
lang["DlgImgTitleInsert"]   = "Вставка рисунка" ;
lang["DlgImgTitleEdit"]     = "Свойства рисунка" ;
lang["DlgImgBtnUpload"]     = "Отправить на сервер" ;
lang["DlgImgURL"]           = "Гиперссылка" ;
lang["DlgImgUpload"]        = "Файл рисунка" ;
lang["DlgImgImageName"]     = "Имя рисунка" ;
lang["DlgImgBtnBrowse"]     = "Выбрать..." ;
lang["DlgImgAlt"]           = "Альтернативный текст" ;
lang["DlgImgWidth"]         = "Width" ;
lang["DlgImgHeight"]        = "Height" ;
lang["DlgImgLockRatio"]     = "Сохранять пропорцию" ;
lang["DlgBtnResetSize"]     = "Переустановить размер" ;
lang["DlgImgBorder"]        = "Border" ;
lang["DlgImgHSpace"]        = "HSpace" ;
lang["DlgImgVSpace"]        = "VSpace" ;
lang["DlgImgAlign"]         = "Align" ;
lang["DlgImgAlignLeft"]     = "Left" ;
lang["DlgImgAlignAbsBottom"]    = "Abs Bottom" ;
lang["DlgImgAlignAbsMiddle"]    = "Abs Middle" ;
lang["DlgImgAlignBaseline"] = "Baseline" ;
lang["DlgImgAlignBottom"]   = "Bottom" ;
lang["DlgImgAlignMiddle"]   = "Middle" ;
lang["DlgImgAlignRight"]    = "Right" ;
lang["DlgImgAlignTextTop"]  = "Text Top" ;
lang["DlgImgAlignTop"]      = "Top" ;
lang["DlgImgPreview"]       = "Просмотр" ;
lang["DlgImgMsgWrongExt"]   = "Допустима загрузка файлов следующих типов:\n\n" + config.ImageUploadAllowedExtensions + "\n\nОперация отменена." ;
lang["DlgImgAlertSelect"]   = "Please select an image to upload." ;     // TODO

// Link Dialog
lang["DlgLnkWindowTitle"]   = "Link" ;      // NEW
lang["DlgLinkTitleInsert"]  = "Создание гиперссылки" ;
lang["DlgLinkTitleEdit"]    = "Свойства гиперссылки" ;
lang["DlgLnkURL"]           = "URL" ;
lang["DlgLnkUpload"]        = "Файл" ;
lang["DlgLnkTarget"]        = "Target" ;
lang["DlgLnkTargetNotSet"]  = "<Не задан>" ;
lang["DlgLnkTargetBlank"]   = "Новое окно (_blank)" ;
lang["DlgLnkTargetParent"]  = "Родительское (_parent)" ;
lang["DlgLnkTargetSelf"]    = "Текущее (_self)" ;
lang["DlgLnkTargetTop"]     = "Самое верхнее (_top)" ;
lang["DlgLnkTitle"]         = "Название" ;
lang["DlgLnkBtnUpload"]     = "Отправить на сервер" ;
lang["DlgLnkBtnBrowse"]     = "Выбрать..." ;
lang["DlgLnkMsgWrongExtA"]  = "Допустима загрузка файлов следующих типов:\n\n" + config.LinkUploadAllowedExtensions + "\n\nОперация отменена." ;
lang["DlgLnkMsgWrongExtD"]  = "Загрузка файлов следующих типов недопустима:\n\n" + config.LinkUploadDeniedExtensions + "\n\nОперация отменена." ;

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
lang["DlgColorTitle"]       = "Выбор цвета" ;
lang["DlgColorBtnClear"]    = "Очистить" ;
lang["DlgColorHighlight"]   = "Пример" ;
lang["DlgColorSelected"]    = "Выбранный" ;

// Smiley Dialog
lang["DlgSmileyTitle"]      = "Вставка смайлов" ;

// Special Character Dialog
lang["DlgSpecialCharTitle"] = "Специальные символы" ;

// Table Dialog
lang["DlgTableTitleInsert"] = "Вставка таблицы" ;
lang["DlgTableTitleEdit"]   = "Свойства таблицы" ;
lang["DlgTableRows"]        = "Строки" ;
lang["DlgTableColumns"]     = "Столбцы" ;
lang["DlgTableBorder"]      = "Размер границы" ;
lang["DlgTableAlign"]       = "Выравнивание" ;
lang["DlgTableAlignNotSet"] = "<Не задано>" ;
lang["DlgTableAlignLeft"]   = "Влево" ;
lang["DlgTableAlignCenter"] = "По центру" ;
lang["DlgTableAlignRight"]  = "Вправо" ;
lang["DlgTableWidth"]       = "Ширина" ;
lang["DlgTableWidthPx"]     = "пикселы" ;
lang["DlgTableWidthPc"]     = "проценты" ;
lang["DlgTableHeight"]      = "Высота" ;
lang["DlgTableCellSpace"]   = "Между ячейками" ;
lang["DlgTableCellPad"]     = "Поля ячеек" ;
lang["DlgTableCaption"]     = "Заголовок" ;

// Table Cell Dialog
lang["DlgCellTitle"]        = "Свойства ячейки" ;
lang["DlgCellWidth"]        = "Ширина" ;
lang["DlgCellWidthPx"]      = "пикселы" ;
lang["DlgCellWidthPc"]      = "проценты" ;
lang["DlgCellHeight"]       = "Высота" ;
lang["DlgCellWordWrap"]     = "Перенос слов" ;
lang["DlgCellWordWrapNotSet"]   = "<Не задан>" ;
lang["DlgCellWordWrapYes"]      = "Да" ;
lang["DlgCellWordWrapNo"]       = "Нет" ;
lang["DlgCellHorAlign"]     = "Гориз. выравнивание" ;
lang["DlgCellHorAlignNotSet"]   = "<Не задано>" ;
lang["DlgCellHorAlignLeft"]     = "Влево" ;
lang["DlgCellHorAlignCenter"]   = "По центру" ;
lang["DlgCellHorAlignRight"]    = "Вправо" ;
lang["DlgCellVerAlign"]     = "Верт. выравнивание" ;
lang["DlgCellVerAlignNotSet"]   = "<Не задано>" ;
lang["DlgCellVerAlignTop"]      = "Вверх" ;
lang["DlgCellVerAlignMiddle"]   = "По центру" ;
lang["DlgCellVerAlignBottom"]   = "Вниз" ;
lang["DlgCellVerAlignBaseline"] = "Baseline" ;
lang["DlgCellRowSpan"]      = "Между строками" ;
lang["DlgCellCollSpan"]     = "Между столбцами" ;
lang["DlgCellBackColor"]    = "Цвет фона" ;
lang["DlgCellBorderColor"]  = "Цвет границы" ;
lang["DlgCellBtnSelect"]    = "Выбрать..." ;

// About Dialog
lang["DlgAboutVersion"]     = "version" ;
lang["DlgAboutLicense"]     = "Licensed under the terms of the GNU Lesser General Public License" ;
lang["DlgAboutInfo"]        = "For further information go to" ;
