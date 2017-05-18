/*
 * COPYRIGHT (c) 1986-2005 Adobe Systems Incorporated.
 *
 * This source code contained herein is proprietary and
 * confidential information of Adobe Systems Incorporated
 * and is covered by the U.S. and other copyright and trade
 * secret protection.  Unauthorized adaptation, distribution,
 * use or display is prohibited and may be subject to civil
 * and criminal penalties.  Disclosure to others is prohibited.
 * For the terms and conditions of source code use refer to
 * your Adobe Systems Incorporated Source Code License Agreement.
 */

/*
 * This file is used to map build environment defines to set of symbols
 * that actually control preprocessing of fdk include files.  The
 * environment symbols loosely follow fmarch output and compiler names.
 */

#ifndef FDK_ENV_H
#define FDK_ENV_H

#ifdef WIN_FRAME
#undef WIN_FRAME
#endif

#define WIN_FRAME

#ifndef _INC_WINDOWS
#include <windows.h>
#endif
#ifndef _INC_STDIO
#include <stdio.h>
#endif
#ifndef _INC_STDLIB
#include <stdlib.h>
#endif

#ifdef COMPILER_IS_MSC
#define CDECL _cdecl
#endif

#define ALWAYS_CONSTRAIN_QUICKCOPY
#define _fullprototypes_
#define __PROTO__
#define USE_ASM_METRICMUL
#define USE_ASM_METRICDIV

#if defined(COMPILER_IS_SUPPORTED)
#define NativeIntIs16Bits
#endif /* COMPILER_IS_SUPPORTED */

#if defined(NativeIntIs16Bits)
#define MACHINE_USES_HUGE_MEMORY
#endif

#endif /* FDK_ENV_H */
