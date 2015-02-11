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
package com.globalsight.cxe.adapter.database.source;

import com.globalsight.cxe.adapter.database.TaskXml;

import java.util.Vector;

/**
 * Creates TaskXml (PaginatedResultSetXml and EventFlowXml) for each
 * Task contained in a TaskClassifier.
 */
public class TaskXmlBuilder
{
    //
    // PRIVATE MEMBER VARIABLES
    //

    private transient PaginatedResultSetXmlGenerator m_prsXmlGen;
    private transient EventFlowXmlGenerator m_efXmlGen;

    //
    // PUBLIC CONSTRUCTOR
    //

    /**
     * Create a new task xml generator.
     */
    public TaskXmlBuilder()
    {
        m_prsXmlGen = new PaginatedResultSetXmlGenerator();
        m_efXmlGen = new EventFlowXmlGenerator();
    }

    /**
     * Returns a vector of task xml objects, one for each task in the given
     * task classifier.  The task xml objects are created with groupings
     * based on the given number of records per page and pages per batch.
     *
     * @param p_taskClassifier the classified tasks to be processed.
     * @param p_recordsPerPage the number of records per page.
     * @parem p_pagesPerBatch the number of pages per batch.
     *
     * @return a vector containing TaskXml objects.
     */
    public Vector createXmlFor(TaskClassifier p_tc,
        int p_recordsPerPage, int p_pagesPerBatch)
        throws PaginatedResultSetXmlGenerationException, EventFlowXmlGenerationException
    {
        m_prsXmlGen.setRecordsPerPage(p_recordsPerPage);
        m_prsXmlGen.setPagesPerBatch(p_pagesPerBatch);

        Vector xmlList = new Vector();
        Vector taskLists = p_tc.allTaskLists();
        for (int i = 0; i < taskLists.size(); i++)
        {
            TaskList tl = (TaskList)taskLists.elementAt(i);
            xmlList.addAll(createXmlFor(tl.automaticTasks()));
            xmlList.addAll(createXmlFor(tl.manualTasks()));
        }

        return xmlList;
    }

    /**
     * Returns a vector of task xml objects, one for each task in the
     * given vector of tasks.
     */
    private Vector createXmlFor(Vector p_vec)
        throws PaginatedResultSetXmlGenerationException, EventFlowXmlGenerationException
    {
        Vector v = new Vector();
        if (p_vec.size() > 0)
        {
            m_prsXmlGen.setTaskVector(p_vec);

            int pages = m_prsXmlGen.pageCount();
            int ppb = m_prsXmlGen.getPagesPerBatch();
            int batches = (int)(Math.ceil((double)pages / (double)ppb));
            Task t = (Task)p_vec.elementAt(0);
            String eventFlowXml;

            for (int batch = 0; batch < batches; batch++)
            {
                long time = System.currentTimeMillis();
                int startPage = batch * ppb + 1;
                int endPage = Math.min(startPage + ppb - 1, pages);
                for (int pg = startPage ; pg <= endPage ; pg++)
                {
                    TaskXml tx = new TaskXml();
                    tx.setPaginatedResultSetXml(m_prsXmlGen.xml(pg));
                    eventFlowXml = m_efXmlGen.xml(t, time, (pg - 1) % ppb + 1,
                        endPage - startPage + 1);
                    tx.setEventFlowXml(eventFlowXml);
                    v.addElement(tx);
                }
            }
        }

        return v;
    }
}
