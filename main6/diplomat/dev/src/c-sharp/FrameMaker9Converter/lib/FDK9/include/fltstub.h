#ifndef WMFLTSTB_H
#define WMFLTSTB_H
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

#ifdef __cplusplus
extern "C" {
#endif		
fde ErrorT CDECL F_FdeRun(ChannelT, ChannelT);
fde StringListT CDECL F_FilterGetSourceNames();
fde StringListT CDECL F_FilterGetDestNames();
fde StringT CDECL F_FilterGetDescription();
fde NativeIntT CDECL F_FilterGetVersionMajor();
fde NativeIntT CDECL F_FilterGetVersionMinor();
#ifdef __cplusplus
}
#endif


#endif
