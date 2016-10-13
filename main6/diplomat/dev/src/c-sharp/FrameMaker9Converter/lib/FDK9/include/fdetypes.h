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

#ifndef FDETYPES_H
#define FDETYPES_H

#include "f_types.h"

/*
 * Now forbid various Non-FDEisms.  If you really need to get to any of
 * these, you can just undef them.
 */

#ifndef DONT_REDEFINE /* do not redefine */ 

/* Ultrix 4.0 has system macros (e.g. _IOWRT) which expand to include casts to int */
#if !defined(ultrix) && !defined(rs6000)
#define int error ! Non_FDE_token "int" !
#endif
#define char error ! Non_FDE_token "char" !
#define signed error ! Non_FDE_token "signed" !
#if !defined(rs6000)
#define unsigned error ! Non_FDE_token "unsigned" !
#endif
#define long error ! Non_FDE_token "long" !
#define float error ! Non_FDE_token "float" !
#define double error ! Non_FDE_token "double" !

#define free ! error Non_FDE_token "free" !
#define malloc ! error Non_FDE_token "malloc" !
#define calloc ! error Non_FDE_token "calloc" !
#define alloca ! error Non_FDE_token "alloca" !
#ifndef SABER
#define realloc ! error Non_FDE_token "realloc" !
#endif /* SABER */

#define getpid ! error Non_FDE_token "getpid" !
#define getenv ! error Non_FDE_token "getenv" !
#define getuid ! error Non_FDE_token "getuid" !
#define getpwent ! error Non_FDE_token "getpwent" !
#define getpwuid ! error Non_FDE_token "getpwuid" !
#define getpwnam ! error Non_FDE_token "getpwnam" !
#define gethostname ! error Non_FDE_token "gethostname" !

#define strcat ! error Non_FDE_token "strcat" !
#define strncat ! error Non_FDE_token "strncat" !

#define strcmp ! error Non_FDE_token "strcmp" !
#define strncmp ! error Non_FDE_token "strncmp" !
#define strcpy ! error Non_FDE_token "strcpy" !
#define strncpy ! error Non_FDE_token "strncpy" !
#define strlen ! error Non_FDE_token "strlen" !
#define strchr ! error Non_FDE_token "strchr" !
#define strrchr ! error Non_FDE_token "strrchr" !
#define strpbrk ! error Non_FDE_token "strpbrk" !
#define strspn ! error Non_FDE_token "strspn" !
#define strcspn ! error Non_FDE_token "strcspn" !
#define strtok ! error Non_FDE_token "strtok" !
/* #define index ! error Non_FDE_token "index" ! */
#define rindex ! error Non_FDE_token "rindex" !
/* #define entry ! error Non_FDE_token "entry" ! */

#define bcopy ! error Non_FDE_token "bcopy" !
#define bcmp ! error Non_FDE_token "bcmp" !
#define bzero ! error Non_FDE_token "bzero" !
#define ffs ! error Non_FDE_token "ffs" !

#define time_t ! error Non_FDE_token "time_t" !
#define timeval ! error Non_FDE_token "timeval" !

#define rename ! error Non_FDE_token "rename" ! /* Use MoveFile instead!!! */

#endif  /* DONT_REDEFINE */

#ifndef max
#define         max(x,y)        (((x)>(y))?(x):(y))
#endif
#ifndef Max
#define         Max(x,y)        (((x)>(y))?(x):(y))
#endif

#ifndef min
#define         min(x,y)        (((x)<(y))?(x):(y))
#endif
#ifndef Min
#define         Min(x,y)        (((x)<(y))?(x):(y))
#endif

#ifndef abs
#define		abs(x)		((x) >= 0 ? (x) : -(x))
#endif
#ifndef Abs
#define		Abs(x)		((x) >=0 ? (x) : -(x))
#endif

/* UIntT cast makes sure we have m as a 32 bit mask */
#define SET_BITS(d,m,s)		((d) = (d) & (~(UIntT)(m)) | (s) & (m))

#ifdef hpux
#ifdef RLIMIT_CORE
#undef RLIMIT_CORE
#endif
#endif

/*
 * There are some compilers where it's more efficient to use "for(;;)"
 * as the "loop" statement.  We leave it as "while(1)" here because it
 * makes our debugger happier (I think).
 */
#define loop while(1)

/*
 * Count the number of elements in an initialized array.
 * I think this is quite portable across compilers and architectures.
 * -pkl 2/6/91
 */
#define	nelements(x)	(sizeof(x) / sizeof((x)[0]))

/*
 * Storage-free Swapping - works for any simple data types since this
 * is based on bit pattern manipulations.  This is faster than 
 * temp-variable-based swapping since no memory access is needed if
 * x & y are registers.
 * (Published in Graphics Gems, p.436)
 */
#define	SWAP(x,y)	do { (x) ^= (y); (y) ^= (x); (x) ^= (y); } while (0)

#ifdef __cplusplus
extern "C" {
#endif

extern ErrorT F_FdeInit FARGS((VoidT));

#ifdef __cplusplus
}
#endif

#endif /* FDETYPES_H */ 
