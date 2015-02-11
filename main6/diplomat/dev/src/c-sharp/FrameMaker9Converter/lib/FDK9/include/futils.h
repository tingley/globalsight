/*************************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
* Copyright 1986 - 2005 Adobe Systems Incorporated
* All Rights Reserved.
*
* NOTICE:  All information contained herein is, and remains the property
* of Adobe Systems Incorporated and its suppliers, if any. The intellectual
* and technical concepts contained herein are proprietary to Adobe Systems
* Incorporated and its suppliers and may be covered by U.S. and Foreign
* Patents, patents in process, and are protected by trade secret or
* copyright law. Dissemination of this information or reproduction of this
* material is strictly forbidden unless prior written permission is
* obtained from Adobe Systems Incorporated.
**************************************************************************/

#ifndef FUTILS_H
#define FUTILS_H

#include "f_types.h"	/* DirHandleT */
#include "fpath.h"	/* FilePathT; FF_* flags to F_FilePathProperty */
#include "fencode.h"

#ifdef __cplusplus
extern "C" {
#endif

extern FilePathT *F_UnixToFilePath FARGS((ConStringT unixName,
	FilePathT *anchorPathp));
extern ErrorT FdeFreeFilePath FARGS((FilePathT **pathpp));
extern ErrorT F_FilePathFree FARGS((FilePathT *pathpp));
extern FilePathT *F_GetFilePath FARGS((ChannelT channel));
extern FilePathT *F_MacToFilePath FARGS((ConStringT name,
	FilePathT *anchor));
extern FilePathT *F_DosToFilePath FARGS((ConStringT name,
	FilePathT *anchor));
extern FilePathT *F_DIToFilePath FARGS((ConStringT diName,
	FilePathT *anchorPathp));
extern ErrorT F_FilePathToDI FARGS((FilePathT *pathp,
	FilePathT *anchorPathp, StringT *diNamep, IntT *diNameSizep));
extern BoolT F_DirectoryExists FARGS((FilePathT *path));
extern BoolT F_FileExists FARGS((FilePathT *path));
extern BoolT F_DirectoryIsWriteable FARGS((FilePathT *path));
extern BoolT F_FileIsWriteable FARGS((FilePathT *path));
extern BoolT F_DirectoryIsReadable FARGS((FilePathT *path));
extern BoolT F_FileIsReadable FARGS((FilePathT *path));
extern IntT F_FilePathProperty FARGS((FilePathT *filepath,
	IntT properties));
extern ErrorT F_RenameFile FARGS((FilePathT *filepath, FilePathT *newpath));
extern ErrorT F_DeleteFile FARGS((FilePathT *filepath));
extern DirHandleT F_FilePathOpenDir FARGS((FilePathT *filepath,
	IntT *statusp));
extern FilePathT *F_FilePathGetNext FARGS((DirHandleT handle, IntT *statusp));
extern VoidT F_FilePathCloseDir FARGS((DirHandleT handle));
extern ErrorT F_ResetDirHandle FARGS((DirHandleT handle));
extern FilePathT *F_FilePathCopy FARGS((FilePathT *filepath));
extern ErrorT F_MakeDir FARGS((FilePathT *filepath));
extern FilePathT *F_FilePathParent FARGS((FilePathT *filepath,
	IntT *statusp));
extern StringT F_FilePathBaseName FARGS((FilePathT *filepath));
extern FilePathT *F_PathNameToFilePath FARGS((ConStringT pathname,
	FilePathT *anchor, IntT platform));
extern StringT F_FilePathToPathName FARGS((FilePathT *filepathp,
	IntT platform));
extern PathEnumT F_PathNameType FARGS((StringT input));
extern ErrorT F_FilePathEnumerate FARGS((FilePathT *filepath,
	IntT (*fn)(FilePathT *, GenericT), GenericT data, IntT direction));
extern VoidT F_RelativizeFileName FARGS((StringT ref, StringT anchor));

extern IntT F_Printf FARGS((ChannelT chan, const NativeCharT *format, ...));
extern IntT F_Sprintf FARGS((UCharT *buf, const NativeCharT *format, ...));
extern IntT F_Scanf FARGS((ChannelT chan, const NativeCharT *format, ...));
extern IntT F_Sscanf FARGS((UCharT *buf, const NativeCharT *format, ...));
extern IntT F_UTFPrintf FARGS((FTextEncodingT toEnc, FTextEncodingT fromEnc,
			ChannelT chan, const NativeCharT *format, ...));

#ifdef MACINTOSH
extern FilePathT *F_NewMacFilePath FARGS((ConStringT name, ShortT vRefNum,
	IntT dirId));
extern ErrorT F_GetMacFilePathInfo FARGS((FilePathT *pathp,
	StringT *namep, ShortT *vRefNump, IntT *dirIDp));
extern ErrorT F_FilePathUpdate FARGS((FilePathT *pathp));
extern ErrorT F_GetMacFileTypeAndCreator(FilePathT *pathp, UIntT *fileTypep, UIntT *fileCreatorp);
extern ErrorT F_SetMacFileTypeAndCreator(FilePathT *pathp, UIntT fileType, UIntT fileCreator);
#endif

#ifdef __cplusplus
}
#endif

#endif /* FUTILS_H */
