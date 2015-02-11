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

#ifndef FSTRLIST_H
#define FSTRLIST_H 

#ifdef  __cplusplus
extern "C" {
#endif

extern StringListT F_StrListNew FARGS((UIntT numStrings, UIntT quantum));
extern VoidT FdeFreeStrList FARGS((StringListT *list));
extern VoidT F_StrListFree FARGS((StringListT list));
extern ErrorT F_StrListAppend FARGS((StringListT list, ConStringT string));
extern ErrorT F_StrListInsert FARGS((StringListT list, ConStringT string, IntT position));
extern ErrorT F_StrListSetString FARGS((StringListT list, ConStringT string, IntT position));
extern VoidT F_StrListRemove FARGS((StringListT list, IntT position));
extern IntT F_StrListIndex FARGS((StringListT list, ConStringT string));
extern IntT F_StrListIIndex FARGS((StringListT list, ConStringT string));
extern ErrorT F_StrListCat FARGS((StringListT toList, StringListT appendList));
extern IntT F_StrListLen FARGS((StringListT list));
extern StringT *F_StrListStrings FARGS((StringListT list));
extern IntT F_StrListCopy FARGS((StringListT strlist, IntT n, StringT strbuf, UIntT size));
extern StringT F_StrListGet FARGS((StringListT strlist, IntT n));
extern VoidT F_StrListSort FARGS((StringListT strlist,
	NativeIntT (*fn)(const ConStringT *, const ConStringT *)));
extern StringListT F_StrListCopyList FARGS((StringListT strlist));

#ifdef  __cplusplus
}
#endif

#define F_StrListFirst(list) F_StrListGet(list, 0)
#define F_StrListLast(list) F_StrListGet(list, F_StrListLen(list)-1)

#endif /* FSTRLIST_H */ 
