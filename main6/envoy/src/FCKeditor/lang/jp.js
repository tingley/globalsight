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
 * jp.js: Japanese support.
 *
 * Authors:
 *   Kato Yuichiro (y.kato@sociohealth.co.jp)
 */

// Toolbar Items and Context Menu

lang["Cut"]                 = "切り取り" ;
lang["Copy"]                = "コピー" ;
lang["Paste"]               = "貼り付け" ;
lang["PasteText"]           = "テキストデータのみ貼り付け" ;
lang["PasteWord"]           = "ワード特有のタグを削除して貼り付け" ;
lang["Find"]                = "検索" ;
lang["SelectAll"]           = "全選択" ;
lang["RemoveFormat"]        = "書式の解除" ;
lang["InsertLink"]          = "ハイパーリンクの挿入・編集" ;
lang["InsertTermLink"]      = "Insert/Edit Term Link" ;
lang["RemoveLink"]          = "ハイパーリンクの削除" ;
lang["InsertImage"]         = "画像の挿入" ;
lang["InsertEquation"]      = "Insert Equation" ;
lang["InsertTable"]         = "表の挿入" ;
lang["InsertLine"]          = "区切り線の挿入" ;
lang["InsertSpecialChar"]   = "特殊キャラクタの挿入" ;
lang["InsertSmiley"]        = "スマイリーの挿入" ;
lang["About"]               = "FCKeditorについて" ;

lang["Bold"]                = "太字" ;
lang["Italic"]              = "斜体" ;
lang["Underline"]           = "下線" ;
lang["StrikeThrough"]       = "打ち消し線" ;
lang["Subscript"]           = "下付き文字" ;
lang["Superscript"]         = "上付き文字" ;
lang["LeftJustify"]         = "左揃え" ;
lang["CenterJustify"]       = "中央揃え" ;
lang["RightJustify"]        = "右揃え" ;
lang["BlockJustify"]        = "両端揃え" ;
lang["DecreaseIndent"]      = "インデント解除" ;
lang["IncreaseIndent"]      = "インデント" ;
lang["Undo"]                = "元に戻す" ;
lang["Redo"]                = "やり直す" ;
lang["NumberedList"]        = "段落番号" ;
lang["BulettedList"]        = "箇条書き" ;

lang["ShowTableBorders"]    = "表の枠線を表示する" ;
lang["ShowDetails"]         = "詳細を表示する" ;

lang["FontStyle"]           = "スタイル" ;
lang["FontFormat"]          = "フォーマット" ;
lang["Font"]                = "フォント" ;
lang["FontSize"]            = "サイズ" ;
lang["TextColor"]           = "フォントの色" ;
lang["BGColor"]             = "背景の色" ;
lang["Source"]              = "ソース" ;

// Context Menu

lang["EditLink"]            = "ハイパーリンクの編集" ;
lang["InsertRow"]           = "行の挿入" ;
lang["DeleteRows"]          = "行の削除" ;
lang["InsertColumn"]        = "列の挿入" ;
lang["DeleteColumns"]       = "列の削除" ;
lang["InsertCell"]          = "セルの挿入" ;
lang["DeleteCells"]         = "セルの削除" ;
lang["MergeCells"]          = "セルの結合" ;
lang["SplitCell"]           = "セルの分割" ;
lang["CellProperties"]      = "セルの属性" ;
lang["TableProperties"]     = "表の属性" ;
lang["ImageProperties"]     = "画像の属性" ;

// Alerts and Messages

lang["ProcessingXHTML"]     = "XHTMLを解析しています。そのままお待ちください..." ;
lang["Done"]                = "完了" ;
lang["PasteWordConfirm"]    = "ワードからテキストを貼り付けようとしているようです。\nワード特有のタグを削除してから貼り付けますか？" ;
lang["NotCompatiblePaste"]  = "この操作はインターネット・エクスプローラー5.5以上で利用できます。\nワード特有のタグを残したまま貼り付けます。よろしいですか？" ;

// Dialogs
lang["DlgBtnOK"]            = "OK" ;
lang["DlgBtnCancel"]        = "キャンセル" ;
lang["DlgBtnClose"]         = "閉じる" ;

// Image Dialog
lang["DlgImgTitleInsert"]   = "画像の挿入" ;
lang["DlgImgTitleEdit"]     = "画像の編集" ;
lang["DlgImgBtnUpload"]     = "選択ファイルを送信" ;
lang["DlgImgURL"]           = "画像ファイルURL" ;
lang["DlgImgUpload"]        = "ローカルファイルの選択" ;
lang["DlgImgBtnBrowse"]     = "送信済み画像" ;
lang["DlgImgAlt"]           = "代替テキスト" ;
lang["DlgImgWidth"]         = "幅" ;
lang["DlgImgHeight"]        = "高さ" ;
lang["DlgImgLockRatio"]     = "縦横比を固定" ;
lang["DlgBtnResetSize"]     = "リセット" ;
lang["DlgImgBorder"]        = "枠線" ;
lang["DlgImgHSpace"]        = "左右アキ" ;
lang["DlgImgVSpace"]        = "上下アキ" ;
lang["DlgImgAlign"]         = "揃え" ;
lang["DlgImgAlignLeft"]     = "左" ;
lang["DlgImgAlignAbsBottom"]    = "下（絶対的）" ;
lang["DlgImgAlignAbsMiddle"]    = "中央（絶対的）" ;
lang["DlgImgAlignBaseline"] = "ベースライン" ;
lang["DlgImgAlignBottom"]   = "下" ;
lang["DlgImgAlignMiddle"]   = "中央" ;
lang["DlgImgAlignRight"]    = "右" ;
lang["DlgImgAlignTextTop"]  = "テキスト" ;
lang["DlgImgAlignTop"]      = "上" ;
lang["DlgImgPreview"]       = "プレビュー" ;
lang["DlgImgMsgWrongExt"]   = "送信を中止しました。\n送信できるファイルは以下の拡張子のファイルに制限されています:\n\n" + config.ImageUploadAllowedExtensions;
lang["DlgImgAlertSelect"]   = "送信する画像を選択してください" ;        // NEW1.2

// Link Dialog
lang["DlgLnkWindowTitle"]   = "リンク" ;        // NEW1.2
lang["DlgLnkURL"]           = "リンク先URL" ;
lang["DlgLnkUpload"]        = "添付ファイル" ;
lang["DlgLnkTarget"]        = "ウィンドウ" ;
lang["DlgLnkTargetNotSet"]  = "<指定なし>" ;
lang["DlgLnkTargetBlank"]   = "別ウィンドウ(_blank)" ;
lang["DlgLnkTargetParent"]  = "親ウィンドウ(_parent)" ;
lang["DlgLnkTargetSelf"]    = "同じウインドウ(_self)" ;
lang["DlgLnkTargetTop"]     = "一番外のウィンドウ(_top)" ;
lang["DlgLnkTitle"]         = "タイトル" ;
lang["DlgLnkBtnUpload"]     = "添付ファイル送信" ;
lang["DlgLnkBtnBrowse"]     = "サーバを参照する" ;
lang["DlgLnkMsgWrongExtA"]  = "送信を中止しました。\n送信できるファイルは以下の拡張子のファイルに制限されています:\n\n" + config.LinkUploadAllowedExtensions;
lang["DlgLnkMsgWrongExtD"]  = "送信を中止しました。\n以下の拡張子のファイルは送信できません:\n\n" + config.LinkUploadDeniedExtensions ;
lang["DlgLnkAlertSelect"]   = "送信するファイルを選択してください" ;        // NEW1.2

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
lang["DlgColorTitle"]       = "色の選択" ;
lang["DlgColorBtnClear"]    = "クリア" ;
lang["DlgColorHighlight"]   = "" ;
lang["DlgColorSelected"]    = "" ;

// Smiley Dialog
lang["DlgSmileyTitle"]      = "スマイリーの挿入" ;

// Special Character Dialog
lang["DlgSpecialCharTitle"] = "特殊文字の挿入" ;

// Table Dialog
lang["DlgTableTitleInsert"] = "表の挿入" ;
lang["DlgTableTitleEdit"]   = "表の編集" ;
lang["DlgTableRows"]        = "行" ;
lang["DlgTableColumns"]     = "列" ;
lang["DlgTableBorder"]      = "枠" ;
lang["DlgTableAlign"]       = "揃え" ;
lang["DlgTableAlignNotSet"] = "<指定なし>" ;
lang["DlgTableAlignLeft"]   = "左" ;
lang["DlgTableAlignCenter"] = "中央" ;
lang["DlgTableAlignRight"]  = "右" ;
lang["DlgTableWidth"]       = "幅" ;
lang["DlgTableWidthPx"]     = "pixcel" ;
lang["DlgTableWidthPc"]     = "％" ;
lang["DlgTableHeight"]      = "高さ" ;
lang["DlgTableCellSpace"]   = "セル間隔" ;
lang["DlgTableCellPad"]     = "セル余白" ;
lang["DlgTableCaption"]     = "説明" ;

// Table Cell Dialog
lang["DlgCellTitle"]        = "セルの属性" ;
lang["DlgCellWidth"]        = "幅" ;
lang["DlgCellWidthPx"]      = "pixel" ;
lang["DlgCellWidthPc"]      = "％" ;
lang["DlgCellHeight"]       = "高さ" ;
lang["DlgCellWordWrap"]     = "改行" ;
lang["DlgCellWordWrapNotSet"]   = "<指定なし>" ;
lang["DlgCellWordWrapYes"]      = "許可" ;
lang["DlgCellWordWrapNo"]       = "禁止" ;
lang["DlgCellHorAlign"]     = "左右揃え" ;
lang["DlgCellHorAlignNotSet"]   = "<指定なし>" ;
lang["DlgCellHorAlignLeft"]     = "左" ;
lang["DlgCellHorAlignCenter"]   = "中央" ;
lang["DlgCellHorAlignRight"]    = "右" ;
lang["DlgCellVerAlign"]     = "上下揃え" ;
lang["DlgCellVerAlignNotSet"]   = "<指定なし>" ;
lang["DlgCellVerAlignTop"]      = "上" ;
lang["DlgCellVerAlignMiddle"]   = "中央" ;
lang["DlgCellVerAlignBottom"]   = "下" ;
lang["DlgCellVerAlignBaseline"] = "ベースライン" ;
lang["DlgCellRowSpan"]      = "行数" ;
lang["DlgCellCollSpan"]     = "列数" ;
lang["DlgCellBackColor"]    = "背景色" ;
lang["DlgCellBorderColor"]  = "枠の色" ;
lang["DlgCellBtnSelect"]    = "選択..." ;

// About Dialog
lang["DlgAboutVersion"]     = "version" ;
lang["DlgAboutLicense"]     = "Licensed under the terms of the GNU Lesser General Public License" ;
lang["DlgAboutInfo"]        = "For further information go to" ;
