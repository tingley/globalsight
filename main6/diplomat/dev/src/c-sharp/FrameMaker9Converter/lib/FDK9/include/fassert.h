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

#ifndef FASSERT_H
#define FASSERT_H

#ifdef __cplusplus
extern "C" {
#endif

typedef VoidT (*F_AssertProcT) FARGS((VoidT));
typedef VoidT (*F_ExitProcT) FARGS((VoidT));

extern F_AssertProcT F_SetAssert FARGS((F_AssertProcT myHandler));
extern F_ExitProcT F_SetDSExit FARGS((F_ExitProcT myHandler));
extern VoidT FdeFail FARGS((VoidT));
extern VoidT F_Exit FARGS((IntT));
extern BoolT FdeSetStrictEnforcement FARGS((BoolT));

#define F_Assert(p)\
do { if (!(p)) FdeFail(); } while (0)

#define F_EnforcementAssert(p)\
do { if (!(p)) FdeEnforcement(); } while (0)

#ifdef __cplusplus
}
#endif

#endif /* FASSERT_H */
