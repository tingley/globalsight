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

#ifndef FPROGS_H
#define FPROGS_H

#ifdef __cplusplus
extern "C" {
#endif

extern ErrorT F_Progress FARGS((IntT percent));
extern VoidT F_Warning FARGS((ConStringT msg));

#ifdef __cplusplus
}
#endif

#endif /* FPROGS_H */
