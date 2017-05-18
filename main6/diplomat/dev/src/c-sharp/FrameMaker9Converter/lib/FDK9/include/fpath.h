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

#ifndef FPATH_H
#define FPATH_H

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
	FDefaultPath,
	FDIPath,
	FUnixPath,
	FMacPath,
	FDosPath
} PathEnumT;

#ifndef FILEPATH_DEFINED
#define FILEPATH_DEFINED
#define _FilePathT FilePathT
typedef struct _FilePathT FilePathT;
#endif

#ifdef __cplusplus
}
#endif

/*
 * Flags to F_FilePathProperty.
 */
#define FF_FilePathReadable   0x0001
#define FF_FilePathWritable   0x0002
#define FF_FilePathDirectory  0x0100
#define FF_FilePathFile       0x0200
#define FF_FilePathExist      0x0400

#define FF_FilePathStat \
(FF_FilePathDirectory|FF_FilePathFile|FF_FilePathExist)
#define FF_FilePathAccess \
(FF_FilePathReadable|FF_FilePathWritable)

#endif /* FPATH_H */
