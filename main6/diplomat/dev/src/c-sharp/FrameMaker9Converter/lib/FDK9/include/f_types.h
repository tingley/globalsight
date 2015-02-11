/*************************************************************************
*
* ADOBE CONFIDENTIAL
* __________________
*
* Copyright 1986 - 2009 Adobe Systems Incorporated
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

#ifndef F_TYPES_H
#define F_TYPES_H
#define _f_typesh_	/* backward compatible */

/*
 * A platform-dependent FDK header file.
 * It must #define exactly one of UNIX, MACINTOSH, or WIN_FRAME;
 * it may contain other macro definitions as well.
 */
#include "fdk_env.h"

/* ANSI C knows full prototypes, "signed", and "const" */
#if defined(__STDC__) || defined(__cplusplus) || defined(WIN_FRAME) || defined(rs6000)
#define __stdc__ 1
/*
 * define __PROTO__ for compatibility with existing practice;
 * it is not otherwise used
 */
#ifndef __PROTO__
#define __PROTO__
#endif
#ifndef _fullprototypes_
#define _fullprototypes_
#endif
#else
#ifndef COMPILER_DOESNT_KNOW_SIGNED 
#define COMPILER_DOESNT_KNOW_SIGNED 
#endif
#define const
#endif

#ifdef _fullprototypes_
#define FARGS(x)x
#else
#define FARGS(x)()
#endif

#ifdef COMPILER_DOESNT_KNOW_SIGNED
#define SIGNED
#else
#define SIGNED signed
#endif

/*
 * C implementations usually provide one of two schemes for accessing
 * variable-length argument lists.  Standard C uses the header file
 * <stdarg.h>; <varargs.h> is available from pre-Standard implementations,
 * typically on UNIX systems.  The following definition represents our
 * recommendation on which scheme to use: if USE_STDARG is defined, you
 * should include <stdarg.h>; otherwise, include <varargs.h>.  In our
 * environment, only the default C compiler provided with SunOS 4 still
 * requires <varargs.h>.
 *
 * You may have to use #define DONT_REDEFINE (or include f_types.h
 * rather than fdetypes.h) if your C compiler provides a header file
 * (either <stdarg.h> or <varargs.h>) that contains identifiers
 * specifically excluded by fdetypes.h via its
 * ! error non-FDE token ! mechanism.
 */
#if !defined(SUNXMOSSPARC_CC)
#ifndef USE_STDARG
#define USE_STDARG
#endif
#else
#undef USE_STDARG
#endif

typedef SIGNED char ByteT;  		/* Signed 1 byte */
typedef SIGNED char CharT;  		/* Signed 1 byte */

typedef short ShortT;			/* Signed 2 bytes */

/*
 * Define USE_LONG_INTS if there are overloading or other
 * compiler problems in a windows client to do with the
 * IntT or UIntT not being a long. Size is the same.
 */
#if defined(USE_LONG_INTS)
typedef long IntT;				/* Signed 4 bytes */
typedef unsigned long UIntT;	/* Unsigned 4 bytes */
#else
typedef int IntT;				/* Signed 4 bytes */
typedef unsigned int UIntT;		/* Unsigned 4 bytes */
#endif

typedef unsigned char   UByteT;		/* Unsigned 1 byte */
typedef unsigned char   UCharT; 	/* Unsigned 1 byte */
typedef unsigned short  UShortT;	/* Unsigned 2 bytes */
typedef UIntT 			UChar32T;	/* Unsigned 4 bytes */		
typedef UShortT			UChar16T;	/* Unsigned 2 bytes */

/*
 * Use P types to pass values to unprototyped/variadic functions, e.g.,
 *	IntT c;
 *	...
 *	F_Printf(0, "It's %c\n", (PUCharT) c);
 * The cast to PUCharT will force C to be widened/truncated as appropriate
 * for the %c format.
 */
typedef unsigned int	PUCharT;
typedef unsigned int	PUShortT;
typedef SIGNED int	PCharT;
typedef SIGNED int	PShortT;
typedef unsigned int	PUByteT;
typedef int		PByteT;

typedef float	RealT; 		/* Float   4 bytes  	*/

#ifdef MACINTOSH
#pragma options align=mac68k

typedef long double NativeLongDoubleT;	/* 8 byte double on PPC, 10 byte extended on 68K */

#ifndef APIRealT_defined		/* Also defined in fApiCallMaker.h */
#define APIRealT_defined
typedef extended80 APIRealT;	/* 80-bit IEEE extended */
#endif	/* APIRealT_defined */

#pragma options align=reset
#endif /* !MACINTOSH. */

typedef double PRealT;

#ifdef NOVOIDSTAR
typedef char *PtrT;	/* Generic pointer */
typedef const char *ConPtrT;	/* Generic pointer */
#else
typedef void *PtrT;	/* Generic pointer */
typedef const void *ConPtrT;	/* Generic pointer */
#endif

#if defined(MACHINE_USES_HUGE_MEMORY) || defined(MSC32)	/* for win_frame */
typedef UCharT *AddrT;
#else
typedef PtrT AddrT;
#endif

/*
 * Declare StreamT as a FILE * if <stdio.h> has been included,
 * otherwise as PtrT.
 */
#ifdef WIN_FRAME
#ifdef _FILE_DEFINED
typedef FILE *StreamT;
#endif
#else	/* !WIN_FRAME */
/*
 * #ifdef getchar is more portable than #ifdef FILE because some Macintosh
 * environments define FILE as a typedef, not a macro.
 */
#ifdef getchar
typedef FILE *StreamT;
#else
typedef PtrT StreamT;
#endif
#endif	/* WIN_FRAME */

/* Why is a boolean not a UByteT ?!? */
/* Because of things like "BoolT bitset = flags & 1L<<28" */
typedef IntT	BoolT;	/* Boolean 4 bytes    	*/
typedef IntT	PBoolT;

#ifndef True
#define True (BoolT)1
#endif 

#ifndef False
#define False (BoolT)0
#endif

typedef IntT	ErrorT;	/* Procedure status		*/

#define VoidT	void

typedef VoidT	(*ProcedureT)();
typedef IntT	(*FunctionT)();

typedef UCharT *StringT;
typedef IntT	MetricT;

/*
 * "ConStringT" is "pointer to const UCharT": a pointer
 * declared ConStringT can be modified, but the things that
 * it points to cannot.
 * "const StringT" is "const pointer to UCharT", which is a
 * different thing altogether.
 */
typedef const UCharT *ConStringT;

/*
 * A pointer type conformable to C character-string literals.
 */
typedef const char *CStringT;

typedef VoidT *GenericT; /* Big enough to hold any pointer or an IntT */

/*
 * Size (and presumably, alignment constraint) of a pointer to data.
 * These need to be available to the C preprocessor, otherwise we
 * could use sizeof(void *) or sizeof(GenericT) and avoid this ugliness.
 */
#if defined(__alpha) && defined(unix)
#define FDK_PTR_SIZE 8
#else
#define FDK_PTR_SIZE 4
#endif

typedef int NativeIntT;
typedef unsigned int NativeUIntT;
typedef char NativeCharT;
typedef unsigned char NativeUCharT;
typedef long NativeLongT;
typedef unsigned long NativeULongT;
typedef double NativeDoubleT;

#define STRBUFFSIZE 1023
typedef UCharT StrBuffT[STRBUFFSIZE+1];

/* channel */ 
typedef struct _masterChannelT *ChannelT;

/* string list */ 
typedef struct _masterListT *StringListT;

/* dir handle */
typedef struct DirStruct *DirHandleT;

/* define FilePathT */
#include "fpath.h"

#ifndef WIN_FRAME
#  ifndef NULL      /* some stdio's have NULL declared */
#    define NULL 0
#    define Null 0
#  endif
#else
#  if  defined( COMPILER_IS_SUPPORTED )
#    ifdef NULL
#      undef NULL
#    endif /* NULL */
#    define NULL 0L
#  endif /* COMPILER_IS_SUPPORTED */
#  ifndef Null
#    define Null NULL
#  endif /* Null */
#endif

#define FdeSuccess	0

/* Have it available to everyone */
#include "fassert.h"
#include "fmemory.h"

/*
 * Container for header files provided by the environment,
 * rather than by Frame.
 */
#include "f_local.h"

#ifndef FAPI_TYPES_DECLARED
#define FAPI_TYPES_DECLARED
/*
 * Frame API.
 */
typedef UIntT F_ObjHandleT;
typedef IntT StatusT;

typedef struct
    {MetricT    x,y;
    } F_PointT;

typedef VoidT (*NetLib_AuthFunction)(ConStringT url, StringT username, StringT password, IntT *cancelp);

#endif /* FAPI_TYPES_DECLARED */

#endif /* F_TYPES_H */
