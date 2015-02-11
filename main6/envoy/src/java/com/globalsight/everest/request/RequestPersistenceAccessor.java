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
package com.globalsight.everest.request;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * This class provides all the peristence calls (read, update, insert) for a
 * request and relationships to other objects (ie. SourcePage).
 */
public class RequestPersistenceAccessor
{
    private static Logger c_logger = Logger
            .getLogger(RequestPersistenceAccessor.class);

    /**
     * Finds the particular request in the cache/database.
     */
    public static Request findRequest(long p_requestId)
            throws RequestHandlerException
    {
        try
        {
            return setPriorityForRequestIfNon((Request) HibernateUtil.get(
                    RequestImpl.class, p_requestId));
        }
        catch (Exception e)
        {
            c_logger.error("The query for request " + p_requestId + " failed",
                    e);

            String[] args = new String[1];
            args[0] = Long.toString(p_requestId);

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_REQUEST, args, e);
        }
    }

    /**
     * Finds the request associated with the source page id (in the database and
     * don't refresh the cache).
     */
    public static Request findRequestByPageId(long p_pageId)
            throws RequestHandlerException
    {
        String hql = "from RequestImpl r where r.pageId = :sourcePageId";

        Session session = HibernateUtil.getSession();

        try
        {
            List result = session.createQuery(hql)
                    .setLong("sourcePageId", p_pageId).list();

            if (result == null || result.size() == 0)
            {
                return null;
            }

            return setPriorityForRequestIfNon((Request) result.get(0));
        }
        catch (Exception e)
        {
            c_logger.error("The query for the request associated with page "
                    + p_pageId + " failed", e);

            String[] args = new String[1];
            args[0] = Long.toString(p_pageId);

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_REQUEST_BY_PAGE_ID,
                    args, e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Finds the request associated with the source page id (in the database and
     * don't refresh the cache).
     */
    public static WorkflowRequest findWorkflowRequestById(long p_Id)
            throws RequestHandlerException
    {
        try
        {
            return (WorkflowRequest) HibernateUtil.get(
                    WorkflowRequestImpl.class, p_Id);
        }
        catch (Exception e)
        {
            c_logger.error("The query for the request associated with id "
                    + p_Id + " failed", e);

            String[] args = new String[1];
            args[0] = Long.toString(p_Id);

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_REQUEST, args, e);
        }
    }

    /**
     * Finds all requests that are stuck IMPORTING. These requests are NOT
     * associated with a page yet and are not part of a delayed import. These
     * requests were being imported when the system went down.
     */
    public static Collection findRequestsStillImporting()
            throws RequestHandlerException
    {
        String sql = "select * from REQUEST " + "where PAGE_ID is null "
                + "and ID not in (select id from DELAYED_IMPORT_REQUEST)";

        return setPrioritiesForRequestIfNon(HibernateUtil.searchWithSql(sql,
                null, RequestImpl.class));
    }

    /*
     * Insert a new request into the database.
     */
    public static void insertRequest(Request p_request)
            throws RequestHandlerException
    {
        try
        {
            HibernateUtil.save(p_request);
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Failed to insert the new request "
                            + p_request.getExternalPageId(), e);

            // takes in three arguments - external page id, data
            // source type, data source id
            String[] args = new String[3];
            args[0] = p_request.getExternalPageId();
            args[1] = p_request.getDataSourceType();
            args[2] = Long.toString(p_request.getDataSourceId());

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_PERSIST_REQUEST,
                    args, e);
        }
    }

    /**
     * Inserts a new request into the database.
     */
    public static void insertWordCountRequest(Request p_request)
            throws RequestHandlerException
    {
        try
        {
            HibernateUtil.save(p_request);
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Failed to insert the new request "
                            + p_request.getExternalPageId(), e);

            // takes in three arguments - external page id, data
            // source type, data source id
            String[] args = new String[3];
            args[0] = p_request.getExternalPageId();
            args[1] = p_request.getDataSourceType();
            args[2] = Long.toString(p_request.getDataSourceId());

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_PERSIST_REQUEST,
                    args, e);
        }
    }

    /**
     * An exception occurred when processing the request. Persist the exception
     * information
     */
    public static void setExceptionInRequest(Request p_request,
            GeneralException p_exception) throws RequestHandlerException
    {
        try
        {
            p_request.setException(p_exception);
            HibernateUtil.saveOrUpdate(p_request);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Failed to set an exception in request "
                            + p_request.getId(), pe);

            String[] args = new String[2];
            args[0] = Long.toString(p_request.getId());

            try
            {
                args[1] = p_exception.serialize();
            }
            catch (Exception ex)
            {
                args[1] = "(exception could not be deserialized)";
            }

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_UPDATE_EXCEPTION_IN_REQUEST,
                    args, pe);
        }
    }

    public static long insertWorkflowRequest(WorkflowRequest p_workflowRequest,
            Job p_job, Collection p_workflowTemplates)
            throws RequestHandlerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            p_workflowRequest.setJob(p_job);
            p_job.addWorkflowRequest(p_workflowRequest);

            for (Iterator it = p_workflowTemplates.iterator(); it.hasNext();)
            {
                WorkflowTemplateInfo wfTempInfo = (WorkflowTemplateInfo) it
                        .next();
                p_workflowRequest.addWorkflowTemplate(wfTempInfo);
            }

            session.saveOrUpdate(p_workflowRequest);
            tx.commit();

            return p_workflowRequest.getId();
        }
        catch (Exception e)
        {
            c_logger.error("Failed to insert the new request "
                    + p_workflowRequest.getId(), e);

            // takes in three arguments - external page id, data
            // source type, data source id
            String[] args = new String[3];
            args[0] = new Long(p_workflowRequest.getId()).toString();

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_PERSIST_REQUEST,
                    args, e);
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * An exception occurred when processing the request. Persist the exception
     * information
     */
    public static void setExceptionInWorkflowRequest(
            WorkflowRequest p_workflowRequest, GeneralException p_exception)
            throws RequestHandlerException
    {
        try
        {
            if (p_exception == null)
            {
                p_exception = new GeneralException();
            }
            p_workflowRequest.setException(p_exception);
            p_workflowRequest.setType(WorkflowRequest.WORKFLOW_REQUEST_FAILURE);
            HibernateUtil.saveOrUpdate(p_workflowRequest);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to set an exception in request "
                    + p_workflowRequest.getId(), e);

            String[] args = new String[2];
            args[0] = Long.toString(p_workflowRequest.getId());

            try
            {
                args[1] = p_exception.serialize();
            }
            catch (Exception ex)
            {
                args[1] = "(exception could not be deserialized)";
            }

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_UPDATE_EXCEPTION_IN_REQUEST,
                    args, e);
        }
    }

    /*
     * An exception occurred when processing the request. Persist the exception
     * information
     */
    public static void setExceptionInRequest(long p_requestId,
            GeneralException p_exception) throws RequestHandlerException
    {
        try
        {
            c_logger.error("setExceptionInRequest: ", p_exception);

            Session session = HibernateUtil.getSession();
            Transaction tx = session.beginTransaction();

            Request request = (Request) session.get(Request.class, new Long(
                    p_requestId));
            request.setException(p_exception);

            session.saveOrUpdate(request);

            tx.commit();
            // session.close();
        }
        catch (Exception e)
        {
            c_logger.error("Failed to set an exception in request "
                    + p_requestId, e);

            String[] args = new String[2];
            args[0] = Long.toString(p_requestId);

            try
            {
                args[1] = p_exception.serialize();
            }
            catch (Exception ex)
            {
                args[1] = "(exception could not be deserialized)";
            }

            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_UPDATE_EXCEPTION_IN_REQUEST,
                    args, e);
        }
    }

    private static Request setPriorityForRequestIfNon(Request request)
    {
        if (request == null)
        {
            return request;
        }

        if (request.getPriority() != null)
        {
            return request;
        }

        String eventFlowXml = request.getEventFlowXml();
        if (eventFlowXml != null)
        {
            String priority = null;

            try
            {
                EventFlowXmlParser p = new EventFlowXmlParser();
                p.parse(eventFlowXml);
                priority = EventFlowXmlParser.getSingleElementValue(
                        p.getSingleElement("batchInfo"), "priority");
            }
            catch (Exception e)
            {
                // ignore any exception here just log
                c_logger.error(
                        "The parser to get priority from eventFlowXml failed",
                        e);
            }

            if (priority != null)
            {
                request.setPriority(priority);
            }
            else
            {
                request.setPriority("null");
            }

        }

        return request;
    }

    private static Collection setPrioritiesForRequestIfNon(
            List<RequestImpl> requests)
    {
        if (requests == null || requests.size() == 0)
        {
            return requests;
        }

        for (RequestImpl requestImpl : requests)
        {
            setPriorityForRequestIfNon(requestImpl);
        }

        return requests;
    }
}
