/****************
 * THIS FILE IS AN INSTALLED COPY OF
 * .\fm_struct.h
 ****************/

/* DON'T EDIT THIS FILE DIRECTLY! */
#ifndef fm_struct_h
#define fm_struct_h
#ifdef __cplusplus
extern "C" {
#endif
/*<fm_struct.c<*/
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

#include "fm_base.h"
#include "fm_psr.h"
#include "fm_rdr.h"
#include "fm_wtr.h"

/*>fm_struct.c>*/
/* --- */
/*<entevent.c<*/
extern FilePathT *Srw_EntityHandler FARGS((StringT entname,
	StructuredEntityScopeT scope, FilePathT *defaultFp));
/*>entevent.c>*/
/*<rdrevent.c<*/
extern SrwErrorT Sr_EventHandler FARGS((SrEventT *eventp,
	SrConvObjT srObj));
/*>rdrevent.c>*/
/*<wtrevent.c<*/
extern SrwErrorT Sw_EventHandler FARGS((SwEventT *eventp,
	SwConvObjT swObj));
/*>wtrevent.c>*/
#ifdef __cplusplus /* end */
}
#endif
#endif /* fm_struct_h */

/* Installed: .\fm_struct.h */
