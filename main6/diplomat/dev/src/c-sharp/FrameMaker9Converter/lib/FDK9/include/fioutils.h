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

#ifndef FIOUTILS_H
#define FIOUTILS_H

#ifdef  __cplusplus
extern "C" {
#endif

extern VoidT F_SetByteOrder FARGS((ChannelT channel));
extern VoidT F_ResetByteOrder FARGS((ChannelT channel));
extern IntT F_ReadShorts FARGS((UCharT *buffer, IntT numShorts,
	ChannelT channel));
extern IntT F_WriteShorts FARGS((const UCharT *buffer, IntT numShorts,
	ChannelT channel));
extern IntT F_ReadLongs FARGS((UCharT *buffer, IntT numInts, ChannelT channel));
extern IntT F_WriteLongs FARGS((const UCharT *buffer, IntT numInts,
	ChannelT channel));
extern IntT F_ReadBytes FARGS((UCharT *buffer, IntT numBytes,
	ChannelT channel));
extern IntT F_WriteBytes FARGS((const UCharT *buffer, IntT numBytes,
	ChannelT channel));
#if defined(__alpha)
extern IntT F_ReadInts(UCharT *buffer, IntT num, ChannelT channel);
#endif
#ifdef  __cplusplus
}
#endif

#endif  /* FIOUTILS_H */
