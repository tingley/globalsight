/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.persistence.tuv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * TuQueryToTargetTuvMapResultHandler provides functionality to convert a
 * Collection of Tuvs into a Map of Tuvs keyed by TuvId.
 */
public class TuQueryToTargetTuvMapResultHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(TuQueryToTargetTuvMapResultHandler.class.getName());

    /**
     * Convert the given collection of Tus into a collection containing a single
     * Map of target locale Tuvs keyed by source locale TuvId.
     * 
     * @param p_collection
     *            the collection of Tus that is created when the original query
     *            is executed
     * @param p_sourceGlobalSightLocale
     *            source locale.
     * @param p_targetGlobalSightLocale
     *            target locale.
     * @return a collection containing a single Map of target locale Tuvs keyed
     *         by source locale TuvId.
     */
    public static Collection handleResult(Collection p_collection,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
            throws TuvException
    {
        Map map = new HashMap(p_collection.size());
        Vector tuvIds = new Vector(p_collection.size());

        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Tu tu = (Tu) it.next();
            Tuv sourceTuv = tu.getTuv(p_sourceLocale.getId());
            Tuv targetTuv = tu.getTuv(p_targetLocale.getId());

            // Cvdl: try making all target tuvs read-write objects...
            if (targetTuv != null)
            {
                try
                {
                    targetTuv = (TuvImpl) HibernateUtil.get(TuvImpl.class,
                            targetTuv.getId());
                }
                catch (Exception pe)
                {
                    CATEGORY.error("Can't get TUV by ID", pe);
                    throw new TuvException("Can't get TUV by ID", pe);
                }
            }

            if (sourceTuv == null)
            {
                continue;
            }

            map.put(sourceTuv.getIdAsLong(), targetTuv);

            if (targetTuv != null)
            {
                tuvIds.add(targetTuv.getIdAsLong());
            }
        }

        if (tuvIds.isEmpty())
        {
            CATEGORY.warn("handleResult target TuvIds isEmpty "
                    + " p_sourceLocale=" + p_sourceLocale.toString()
                    + " p_targetLocale=" + p_targetLocale.toString()
                    + " p_collection=" + p_collection.toString());

            List returnList = new ArrayList(1);
            returnList.add(new HashMap(0));

            return returnList;
        }

        List result = new ArrayList(1);
        result.add(map);

        return result;
    }
}
