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

#ifndef FMETRICS_H 
#define FMETRICS_H 

/*
 * Basic geometric types: MetricT, PointT, SizeT, RectT.
 * All these types have 4-byte coordinates.
 */

typedef	MetricT	AngleT;

#define FM_EPSILON	((MetricT)0x10)		/* A small non-zero number */

/* Angular measurements */
#define FM_1		((MetricT)0x10000)
#define FM_360		(360*FM_1)

#ifdef  __cplusplus
extern "C" {
#endif

extern MetricT F_MetricFloat FARGS((PRealT f));
extern PRealT F_MetricToFloat FARGS((MetricT m));
extern MetricT F_MetricMake FARGS((IntT n, IntT d));
extern MetricT F_MetricSquare FARGS((MetricT x));
extern MetricT F_MetricSqrt FARGS((MetricT a));
extern MetricT F_MetricMul FARGS((MetricT x, MetricT y));
extern BoolT F_MetricApproxEqual FARGS((MetricT a, MetricT b));
extern MetricT F_MetricDiv FARGS((MetricT sx, MetricT sy));
extern MetricT F_MetricFractMul FARGS((MetricT x, IntT n, IntT d));
extern VoidT F_MetricNormalizeAngle FARGS((AngleT *anglep));
extern VoidT F_MetricConstrainAngle FARGS((AngleT *anglep, AngleT lbound));

#ifdef  __cplusplus
}
#endif

#endif /* FMETRICS_H  */ 
