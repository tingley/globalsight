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

#ifndef FMEMORY_H
#define FMEMORY_H

#ifdef MACINTOSH
typedef IntT HandleRefT;
#define HandleT HandleRefT
#else
typedef struct _masterHandleT *HandleT;
#endif


/* See also TOOBIG in
 * unix/vm/heap/heapks.c and 
 * win/winplat/mem/heapks.cpp
 */
#define TOOBIG 0x7FFF0000
#define NO_DSE 0 /* return NULL if can't allocate */
#define DSE 1	/* call exit handler set by F_SetDSExit if can't allocate */

/*
 * NB this restriction is no longer enforced; we depend on a large flat
 * address space.
 */
#define PCTOOBIG (64L*1024L)

#ifdef __cplusplus
extern "C" {
#endif

extern HandleT F_AllocHandle FARGS((UIntT size, PUCharT flags));
extern ErrorT F_FreeHandle FARGS((HandleT));
extern ErrorT FdeFreeHandle FARGS((HandleT *));
extern AddrT F_LockHandle FARGS((HandleT handle));
extern IntT F_UnlockHandle FARGS((HandleT handle));
extern HandleT F_ReallocHandle FARGS((HandleT handle, UIntT newSize, PUCharT flags));
extern UIntT F_GetHandleSize FARGS((HandleT handle));
extern BoolT F_HandlesAreEqual FARGS((HandleT handle1, HandleT handle2));
extern BoolT F_HandleEqual FARGS((HandleT handle1, HandleT handle2));
extern BoolT F_DuplicateHandle FARGS((HandleT srcHandle, HandleT dstHandle));
extern ErrorT F_ClearHandle FARGS((HandleT handle));
extern PtrT F_Alloc FARGS((UIntT size, PUCharT flags));
extern PtrT F_Calloc FARGS((UIntT n, UIntT size, PUCharT flags));
extern ErrorT FdeFree FARGS((PtrT *ptr));
extern ErrorT F_Free FARGS((PtrT ptr));
extern BoolT F_PtrEqual FARGS((ConPtrT ptr1, ConPtrT ptr2, UIntT size1));
/*
 * F_BytesEqual withdrawn from the library.  This macro retains
 * source-code compatibility.
 */
#define F_BytesEqual(x, y, n)F_PtrEqual((PtrT) (x), (PtrT) (y), n)
extern PtrT F_DuplicatePtr FARGS((ConPtrT srcPtr, UIntT size, PUCharT flags));
extern ErrorT F_CopyPtr FARGS((ConPtrT srcPtr, PtrT dstPtr, UIntT numBytes));
extern ErrorT F_CopyBytes FARGS((const UCharT *srcPtr, UCharT *dstPtr,
	UIntT numBytes));
extern ErrorT F_ClearPtr FARGS((PtrT ptr, UIntT size));
extern PtrT F_Realloc FARGS((PtrT ptr, UIntT newsize, PUCharT flags));

#ifdef __cplusplus
}
#endif

#endif /* FMEMORY_H */
