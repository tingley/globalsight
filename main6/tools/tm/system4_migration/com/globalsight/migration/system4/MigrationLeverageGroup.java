/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.migration.system4;

import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class is responsible for creating a LeverageGroup
 */
public class MigrationLeverageGroup
{
    /**
     * Create a LeverageGroup
     * @return LeverageGroup object
     */
    public static LeverageGroup create()
        throws Exception
    {
        LeverageGroupImpl leverageGroup = new LeverageGroupImpl();
        HibernateUtil.save(leverageGroup);
        return leverageGroup;
    }
}
