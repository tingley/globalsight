#include "fapi.h"
#include "fdetypes.h"
#include "fmemory.h"
#include "futils.h"
#include "fstrings.h"

/// undef these which forbid in fdetypes.h
#undef char
#undef int

#define SVPDF 1
#define SVMIF 2
#define SVFM 3

#define MyString(x) F_StrCopyString((StringT)x)
#define NOTE_NO_ACTIVE_DOC MyString("No active document found.")

VoidT CloseActiveDoc(VoidT);
VoidT SaveAsPdf(VoidT);
VoidT SaveAsFm(VoidT);
VoidT SaveAsMif(VoidT);

StringT EMPTY = MyString("");

VoidT FrameAlert(StringT msg)
{
	F_ApiAlert(msg, FF_ALERT_CONTINUE_NOTE);
}

VoidT F_ApiInitialize(IntT init)
{
	F_ObjHandleT menuBarId, menuId;

	/* Making it unicode enabled. */
	F_FdeInit();
	F_ApiEnableUnicode(True);
  	F_FdeInitFontEncs(MyString("UTF-8"));

	switch(init)
	{
	case FA_Init_First:
		menuBarId = F_ApiGetNamedObject(FV_SessionId, FO_Menu, MyString("!MakerMainMenu"));
		menuId = F_ApiDefineAndAddMenu(menuBarId, MyString("GlobalSightMenu"), MyString("GlobalSight"));
		F_ApiDefineAndAddCommand(SVPDF, menuId, MyString("SvPdfCmd"), MyString("Save As PDF"), EMPTY);
		F_ApiDefineAndAddCommand(SVMIF, menuId, MyString("SvMifCmd"), MyString("Save As MIF"), EMPTY);
		F_ApiDefineAndAddCommand(SVFM, menuId, MyString("SvFmCmd"), MyString("Save As FM"), EMPTY);
		F_ApiNotification(FA_Note_PostOpenDoc, True);
		F_ApiNotification(FA_Note_PostOpenMIF, True);
		F_ApiBailOut();
		break;
	}
}


VoidT F_ApiNotify(IntT notification, F_ObjHandleT docId, StringT sparm, IntT iparm)
{
	switch(notification)
	{
	case FA_Note_PostOpenDoc:
		F_ApiNotification(FA_Note_PostOpenMIF, False);
		SaveAsMif();
		F_ApiNotification(FA_Note_PostOpenMIF, True);
		//CloseActiveDoc();
		break;

	case FA_Note_PostOpenMIF:
		F_ApiNotification(FA_Note_PostOpenDoc, False);
		SaveAsFm();
		SaveAsPdf();
		F_ApiNotification(FA_Note_PostOpenDoc, True);
		//CloseActiveDoc();
		break;
	}
}

StringT GetSvFilename(F_ObjHandleT docId, StringT newext)
{
	IntT len, sublen, extlen;
	StringT filename, sub, nstr, prefix;

	filename = F_ApiGetString(docId, docId, FP_Name);
	nstr = F_StrCopyString(filename);

	len = F_StrLen(nstr);
	sub = F_StrRChr(nstr, '.');
	sublen = F_StrLen(sub);
	extlen = F_StrLen(newext);

	F_StrTrunc(nstr, len - sublen);

	prefix = F_StrNew(len - sublen + 1);
	F_StrCpy(prefix, nstr);

	prefix = (StringT) F_Realloc(prefix, F_StrLen(prefix)+extlen+1, NO_DSE);
	F_StrCat(prefix, newext);

	return prefix;
}

VoidT CloseActiveDoc()
{
	F_ObjHandleT docId;
	StringT name = NULL;

	F_FdeInit();

	docId = F_ApiGetId(FV_SessionId, FV_SessionId, FP_ActiveDoc);

	FrameAlert(MyString("111"));

	if (docId)
	{
		FrameAlert(MyString("222"));
		F_ApiSimpleSave(docId, name, False);
		F_ApiClose(docId, FF_CLOSE_MODIFIED);
	}
}

VoidT SaveAsMif()
{
	F_PropValsT params, *returnParams = NULL;
	F_ObjHandleT saveId, docId;
	IntT i = 0;
	StringT svname;

	F_FdeInit();
	
	/* Gets the active book (bookId) */
	docId = F_ApiGetId(FV_SessionId, FV_SessionId, FP_ActiveDoc);

	if(!docId)
    {
		F_ApiAlert(NOTE_NO_ACTIVE_DOC, FF_ALERT_CONTINUE_NOTE);
		return;
    }

	svname = GetSvFilename(docId, MyString(".mif"));
	//FrameAlert(MyString(svname));
	
	/* Sets save parameters */
	params = F_ApiGetSaveDefaultParams();
	i = F_ApiGetPropIndex(&params, FS_FileType);
	params.val[i].propVal.u.ival = FV_SaveFmtInterchange;
	i = F_ApiGetPropIndex(&params, FS_AlertUserAboutFailure);
    params.val[i].propVal.u.ival = True;
	i = F_ApiGetPropIndex(&params, FS_SaveMode);
	params.val[i].propVal.u.ival = FV_ModeSaveAs;
	i = F_ApiGetPropIndex(&params, FS_SaveAsModeName);
	params.val[i].propVal.u.ival = FV_SaveAsNameProvided;
	i = F_ApiGetPropIndex(&params, FS_AutoBackupOnSave);
	params.val[i].propVal.u.ival = FV_SaveNoAutoBackup;

	/* Saves the book */
	saveId = F_ApiSave(docId, svname, &params, &returnParams);
	F_ApiPrintSaveStatus(returnParams);
	F_ApiDeallocatePropVals(&params);
	F_ApiDeallocatePropVals(returnParams);

	//CloseActiveDoc();
}


VoidT SaveAsPdf()
{
	F_PropValsT params, *returnParams = NULL;
	F_ObjHandleT docId, saveId;
	IntT i = 0;
	StringT svname;

	F_FdeInit();
	docId = F_ApiGetId(FV_SessionId, FV_SessionId, FP_ActiveDoc);

	if(!docId)
    {
		F_ApiAlert(NOTE_NO_ACTIVE_DOC, FF_ALERT_CONTINUE_NOTE);
		return;
    }

	svname = GetSvFilename(docId, MyString(".pdf"));
	//FrameAlert(MyString(svname));

	/* Sets the save parameters so as to save as PDF and allow user to name file.*/
	params = F_ApiGetSaveDefaultParams();
	i = F_ApiGetPropIndex(&params, FS_FileType);
	params.val[i].propVal.u.ival = FV_SaveFmtPdf;
	i = F_ApiGetPropIndex(&params, FS_AlertUserAboutFailure);
    params.val[i].propVal.u.ival = True;
	i = F_ApiGetPropIndex(&params, FS_SaveMode);
	params.val[i].propVal.u.ival = FV_ModeSaveAs;
	i = F_ApiGetPropIndex(&params, FS_SaveAsModeName);
	params.val[i].propVal.u.ival = FV_SaveAsNameProvided;
	i = F_ApiGetPropIndex(&params, FS_AutoBackupOnSave);
	params.val[i].propVal.u.ival = FV_SaveNoAutoBackup;


	/* Saves the book */
	saveId = F_ApiSave(docId, svname, &params, &returnParams);
	F_ApiPrintSaveStatus(returnParams);
	F_ApiDeallocatePropVals(&params);
	F_ApiDeallocatePropVals(returnParams);

	//CloseActiveDoc();
}

VoidT SaveAsFm()
{
	F_PropValsT params, *returnParams = NULL;
	F_ObjHandleT saveId, docId;
	IntT i = 0;
	StringT svname;
	
	F_FdeInit();
	
	/* Gets the active book (bookId) */
	docId = F_ApiGetId(FV_SessionId, FV_SessionId, FP_ActiveDoc);	
	
	if(!docId)
    {
		F_ApiAlert(NOTE_NO_ACTIVE_DOC, FF_ALERT_CONTINUE_NOTE);
		return;
    }

	svname = GetSvFilename(docId, MyString(".fm"));
	//FrameAlert(MyString(svname));

	/* Sets save parameters */
	params = F_ApiGetSaveDefaultParams();
	i = F_ApiGetPropIndex(&params, FS_FileType);
	params.val[i].propVal.u.ival = FV_SaveFmtBinary;
	i = F_ApiGetPropIndex(&params, FS_AlertUserAboutFailure);
    params.val[i].propVal.u.ival = True;
	i = F_ApiGetPropIndex(&params, FS_SaveMode);
	params.val[i].propVal.u.ival = FV_ModeSaveAs;
	i = F_ApiGetPropIndex(&params, FS_SaveAsModeName);
	params.val[i].propVal.u.ival = FV_SaveAsNameProvided;
	i = F_ApiGetPropIndex(&params, FS_AutoBackupOnSave);
	params.val[i].propVal.u.ival = FV_SaveNoAutoBackup;

	/* Saves the book */
	saveId = F_ApiSave(docId, svname, &params, &returnParams);
	F_ApiPrintSaveStatus(returnParams);
	F_ApiDeallocatePropVals(&params);
	F_ApiDeallocatePropVals(returnParams);
}

VoidT F_ApiCommand(IntT command)
{
	switch(command)
	{
		case SVPDF:
			SaveAsPdf();
			//CloseActiveDoc();
			break;
		
		case SVMIF:
			SaveAsMif();
			break;

		case SVFM:
			SaveAsFm();
			break;
		
		default:
			break;
	}
}

int CSHandleCommand(int command, char* svFile)
{
	//SaveFile = F_StrNew(F_StrLen((StringT)svFile)+1);
	//F_StrCpy(SaveFile, (StringT) svFile);

	F_ApiCommand((IntT) command);

	return 0;
}