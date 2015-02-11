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

package com.globalsight.everest.page.pageimport;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Handles the persistence of an ExtractedFile.
 * This is just on importing (inserting) and not on modification.
 */
public class ExtractedFileImportPersistenceHandler
{
    private static Logger s_importLogger =
        Logger.getLogger(
            ExtractedFileImportPersistenceHandler.class);

    static public final String LG_SEQUENCENAME = "LEVERAGE_GROUP_SEQ";
    static public final String TEMPLATE_SEQUENCENAME = "PAGE_TEMPLATE_SEQ";
    static public final String TEMPLATEPART_SEQUENCENAME = "TEMPLATE_PART_SEQ";
    static public final String TU_SEQUENCENAME = "TU_SEQ";
    static public final String TUV_SEQUENCENAME = "TUV_SEQ";

    public ExtractedFileImportPersistenceHandler()
    {
    }

    public void persistObjects(SourcePage p_sourcePage)
        throws PageException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_sourcePage);          
        }
        catch (Exception ex)
        {
            s_importLogger.error("sequence allocation error", ex);
            throw new PageException(ex);
        }
    }
}
