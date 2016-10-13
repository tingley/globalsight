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

package com.globalsight.tools.tmximport;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm.Indexer;
import com.globalsight.ling.tm.IndexerLocal;

import java.util.List;
import java.util.ArrayList;

/**
 * This class is responsible for indexing the migrated data
 */
public class IndexerTmx
{
    private static Indexer m_indexer = new IndexerLocal();

    public static void index(Tuv p_tuv)
        throws Exception
    {
        List tuvList = new ArrayList();
        tuvList.add(p_tuv);
        m_indexer.index(tuvList);
    }
}
