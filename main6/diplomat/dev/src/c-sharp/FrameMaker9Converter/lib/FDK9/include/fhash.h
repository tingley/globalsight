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

#ifndef FHASH_H
#define FHASH_H

typedef struct hashtablet HashTableT;
typedef HashTableT* HashT; 

#define KEY_IS_STRING 32767

#ifdef  __cplusplus
extern "C" {
#endif

/* assumes INFO points to sizeof(StrBuffT) bytes (include trailing \0) */
extern IntT F_HashReportOnData FARGS((HashTableT *table, StringT info,
	GenericT datum));
/* NB this aliases NAME! */
/* NB stringifyMe must assume buffer size sizeof(StrBuffT) */
extern HashTableT *F_HashCreate FARGS((ConStringT name, IntT minsize,
	PShortT key_len, GenericT notfound, BoolT (*deadQuery)(GenericT),
	VoidT (*removeNotify)(GenericT), VoidT (*stringifyMe)(PtrT, UCharT *)));
extern ErrorT F_HashSet FARGS((HashTableT *table, PtrT key, GenericT datum));
extern GenericT F_HashGet FARGS((HashTableT *table, ConPtrT key));
extern VoidT F_HashRemove FARGS((HashTableT *table, PtrT key));
extern VoidT F_HashPrefixForget FARGS((HashTableT *table, PtrT key));
extern VoidT F_HashDestroy FARGS((HashTableT *table));
/* visit every member calling PROC(key, datum). */
extern VoidT F_HashEnumerate FARGS((HashTableT *table,
	IntT (*proc)(PtrT, GenericT)));

#ifdef  __cplusplus
}
#endif

#endif /* FHASH_H */
