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

#ifndef FCHANNEL_H
#define FCHANNEL_H

#ifdef __cplusplus
extern "C" {
#endif

#ifdef UNIX
/***************************************************************************
  private definition of the ChannelT data struct 
*/ 

struct _masterChannelT {
  BoolT      swap; 
  StringT    platformPath;       
  StreamT    stream;
};

typedef struct _masterChannelT PrivateChannelT;

/***********************************************************************
  Access macros for ChannelT structure. 
  This will hide the implementation details of ChannelT from the user.

***********************************************************************/ 
#define FdeChannelNew() \
	(ChannelT) F_Calloc(1, sizeof(PrivateChannelT), NO_DSE)

#define FdeChannelGetStream(Channel)   ((Channel)->stream)
#define FdeChannelGetPathName(Channel) ((Channel)->platformPath)
#define FdeChannelGetSwap(Channel) ((Channel)->swap)

#define FdeChannelSetPathName(Channel, path) (((Channel)->platformPath) = (path))
#define FdeChannelSetStream(Channel, stream_v) (((Channel)->stream) = (stream_v))
#define FdeChannelSetSwap(Channel, Swap) (((Channel)->swap) = (Swap))

#endif /* UNIX */

/****************************************************************************
  channel functions 
*/
#if defined(FAPI_5_BEHAVIOR) || defined(FAPI_4_BEHAVIOR)
extern ChannelT F_ChannelOpen FARGS((FilePathT *path, StringT type));
#else
extern ChannelT F_ChannelOpen FARGS((FilePathT *path, CStringT type));
#endif
extern ChannelT F_ChannelMakeTmp FARGS((PUCharT size));
extern IntT F_ChannelRead FARGS((PtrT ptr, IntT size, IntT nitems, ChannelT channel));
extern IntT F_ChannelWrite FARGS((ConPtrT ptr, IntT size, IntT nitems,
	ChannelT channel));
extern IntT FdeChannelClose FARGS((ChannelT *channel));
extern IntT F_ChannelClose FARGS((ChannelT channel));
extern IntT FdeChannelCloseTmp FARGS((ChannelT *channel));
extern IntT F_ChannelCloseTmp FARGS((ChannelT channel));
extern IntT F_ChannelSeek FARGS((ChannelT channel, NativeLongT offset,
	PUCharT mode));
extern IntT F_ChannelPeek FARGS((ChannelT channel));
extern NativeLongT F_ChannelTell FARGS((ChannelT channel));
extern NativeLongT F_ChannelSize FARGS((ChannelT channel));
extern ErrorT F_ChannelAppend FARGS((ChannelT srcChannel, ChannelT dstChannel));
extern IntT F_ChannelFlush FARGS((ChannelT channel));
extern IntT F_ChannelEof FARGS((ChannelT channel));


#ifdef __cplusplus
}
#endif

#endif /* FCHANNEL_H */
