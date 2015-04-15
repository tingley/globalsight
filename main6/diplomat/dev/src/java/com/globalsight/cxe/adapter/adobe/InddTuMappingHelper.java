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
package com.globalsight.cxe.adapter.adobe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a COTI project
 * 
 */
public class InddTuMappingHelper
{
    private static final Logger logger = Logger.getLogger(InddTuMappingHelper.class);

    public static InddTuMapping getMappingByTu(long jobId, long tuId)
    {
        String hql = "from InddTuMapping d where d.jobId = " + jobId + " and d.tuId = " + tuId;

        List<InddTuMapping> ms = (List<InddTuMapping>) HibernateUtil.search(hql);
        return (ms == null || ms.size() == 0) ? null : ms.get(0);
    }

    public static List<InddTuMapping> getMappingByPage(long jobId, long srcPageId, int pageNum)
    {
        String hql = "from InddTuMapping d where d.jobId = " + jobId + " and d.srcPageId = "
                + srcPageId + " and d.pageNum = " + pageNum;

        List<InddTuMapping> ms = (List<InddTuMapping>) HibernateUtil.search(hql);

        return ms;
    }

    public static InddTuMapping saveMapping(long jobId, long srcPageId, long tuId, long companyId,
            int pageNum) throws Exception
    {
        InddTuMapping m = new InddTuMapping();
        m.setCompanyId(companyId);
        m.setJobId(jobId);
        m.setPageNum(pageNum);
        m.setSrcPageId(srcPageId);
        m.setTuId(tuId);

        HibernateUtil.save(m);
        return m;
    }

    public static void processOutput(Output output, boolean isIdml, boolean isIndd)
    {
        DocumentElement de = null;
        int lastPageNum = 0;
        Pattern p = null;

        if (isIdml)
        {
            p = Pattern
                    .compile(
                            "&lt;story name=&quot;Stories/[^&]+\\.xml&quot; pageNum=&quot;([\\d]+)&quot;&gt;",
                            Pattern.MULTILINE);
        }
        
        if (isIndd)
        {
            p = Pattern.compile(" pageNumber=&quot;([\\d]+)&quot;", Pattern.MULTILINE);
        }

        for (Iterator it = output.documentElementIterator(); it.hasNext();)
        {
            de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                    TranslatableElement element = (TranslatableElement) de;

                    element.setInddPageNum(lastPageNum);
                    break;

                case DocumentElement.SKELETON:
                    SkeletonElement ske = (SkeletonElement) de;
                    String content = ske.getSkeleton();

                    Matcher m = p.matcher(content);
                    String lastM = null;
                    while (m.find())
                    {
                        lastM = m.group(1);
                    }

                    if (lastM != null)
                    {
                        lastPageNum = Integer.parseInt(lastM);
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
