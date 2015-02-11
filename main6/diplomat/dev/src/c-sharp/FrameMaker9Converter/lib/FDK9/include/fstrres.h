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

#ifndef FSTRRES_H
#define FSTRRES_H

#ifdef __cplusplus
extern "C" {
#endif

extern ErrorT F_SrInit FARGS((const UCharT *appname, const UCharT *uilanguage));
extern StringT F_SrCopy FARGS((IntT srindex));
extern ErrorT F_SrGetN FARGS((IntT srindex, UCharT *buf, UIntT n));
extern ErrorT F_SrGetF FARGS((IntT srindex, UCharT *buf, UIntT n,
	const UCharT *argformat, ...));
#ifdef va_arg
/*
 * To use this declaration, you need to #include either <stdarg.h> or
 * <varargs.h>, depending on your C implementation.  See the discussion
 * of USE_STDARG in "f_types.h".
 */
extern ErrorT F_SrGetVF FARGS((IntT srindex, UCharT *buf, UIntT n,
	const UCharT *argformat, va_list argsp));
#endif

#ifdef __cplusplus
}
#endif

#endif /* FSTRRES_H */
