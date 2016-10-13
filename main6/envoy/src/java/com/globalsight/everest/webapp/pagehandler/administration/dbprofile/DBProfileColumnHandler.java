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
package com.globalsight.everest.webapp.pagehandler.administration.dbprofile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.collections.HashtableValueOrderWalker;
import com.globalsight.util.collections.HashtableValueOrderWalkerFactory;

/**
 * DBProfileColumnHandler, A page handler to produce the entry page for
 * DataSources management.
 */
public class DBProfileColumnHandler extends PageHandler
{
    private static final int NEW = 1;
    private static final int MODIFY = 2;
    private static final int REMOVE = 4;
    private static final int TRANSPOSE = 7;

    private HashtableValueOrderWalker m_mode_pairs;
    private HashtableValueOrderWalker m_knownFormatTypePairs;
    private Vector m_DBColumns = null;
    private String m_ModDBColumns = "False";

    /**
     * Invokes this PageHandler
     * <p>
     * 
     * @param p_thePageDescriptor
     *            the page descriptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        if (sessionMgr.getAttribute("DBColumns") == null)
            m_DBColumns = new Vector();

        // put user input into session
        if (p_theRequest.getParameter("DBProfileName") != null)
            sessionMgr.setAttribute("DBProfileName",
                    (String) p_theRequest.getParameter("DBProfileName"));
        if (p_theRequest.getParameter("DBProfileDescription") != null)
            sessionMgr.setAttribute("DBProfileDescription",
                    p_theRequest.getParameter("DBProfileDescription"));
        if (p_theRequest.getParameter("locProfile") != null)
            sessionMgr.setAttribute("locProfile",
                    p_theRequest.getParameter("locProfile"));

        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
                p_theResponse, p_context);
    }

    /**
     * Used to communicate with Applet, specifically for grid
     * <p>
     * 
     * @param p_isDoGet
     *            - Determines whether the request is a get or post.
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param the
     *            original request sent from the browser
     * @param the
     *            original response object
     * @param context
     *            the Servlet context
     * @return A vector of serializable objects to be passed to applet.
     */
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
            WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        if (p_isDoGet)
        {
            return getData(p_session, sessionMgr);
        }
        else
            return saveData(p_session, p_theRequest, sessionMgr);
    }

    // get the data to be dispalyed on the grid
    private Vector getData(HttpSession p_session, SessionManager p_sessionMgr)
            throws EnvoyServletException
    {
        ResourceBundle bundle = getBundle(p_session);
        String[] labels =
        { bundle.getString("lb_new"), bundle.getString("lb_edit"),
                bundle.getString("lb_remove"), bundle.getString("lb_previous"),
                bundle.getString("lb_cancel"), bundle.getString("lb_next"),
                bundle.getString("lb_move_up"),
                bundle.getString("lb_move_down") };
        String[] header =
        { bundle.getString("lb_name"), bundle.getString("lb_label"),
                bundle.getString("lb_table"), bundle.getString("lb_format"),
                bundle.getString("lb_mode") };

        // use method in FileProfileMainHandler
        m_knownFormatTypePairs = HashtableValueOrderWalkerFactory
                .createHashtableValueOrderWalker(getKnownFormatTypes());

        m_mode_pairs = getModePairs(bundle);
        DatabaseProfileImpl dbprofile = (DatabaseProfileImpl) p_sessionMgr
                .getAttribute("ModDBProfile");
        long profile_id;
        if (dbprofile != null)
        {
            profile_id = dbprofile.getId();
            String test = (String) p_sessionMgr.getAttribute("ModDBColumns");
            if (test != null)
                m_ModDBColumns = test;
            else
                m_ModDBColumns = "False";
        }
        else
        {
            profile_id = -1;
        }
        List<Object[]> gridData = getDBColumns(profile_id);
        String[] dialogLabels =
        {
                bundle.getString("lb_name") + "* "
                        + bundle.getString("lb_colon"),
                bundle.getString("lb_label") + "* "
                        + bundle.getString("lb_colon"),
                bundle.getString("lb_table") + "* "
                        + bundle.getString("lb_colon"),
                bundle.getString("lb_mode") + "* "
                        + bundle.getString("lb_colon"),
                bundle.getString("lb_format") + "* "
                        + bundle.getString("lb_colon"),
                bundle.getString("lb_xml_rules") + bundle.getString("lb_colon"),
                bundle.getString("lb_choose"),
                bundle.getString("lb_columns"),
                bundle.getString("msg_remove_column"),
                bundle.getString("lb_globalsight")
                        + bundle.getString("lb_colon") + " "
                        + bundle.getString("lb_new_column"),
                bundle.getString("msg_new_column"),
                bundle.getString("lb_globalsight")
                        + bundle.getString("lb_colon") + " "
                        + bundle.getString("lb_edit_column"),
                bundle.getString("msg_edit_column") };
        String[] dialogButtons =
        { bundle.getString("applet_save"), bundle.getString("applet_saveb"),
                bundle.getString("applet_savex"),
                bundle.getString("applet_cancel"),
                bundle.getString("applet_cancelb"),
                bundle.getString("applet_ok"), bundle.getString("applet_okb"),
                bundle.getString("applet_okx") };
        String[] imageNames =
        { bundle.getString("applet_new"), bundle.getString("applet_newb"),
                bundle.getString("applet_modify"),
                bundle.getString("applet_modifyb"),
                bundle.getString("applet_modifyx"),
                bundle.getString("applet_remove"),
                bundle.getString("applet_removeb"),
                bundle.getString("applet_removex"),
                bundle.getString("applet_previous"),
                bundle.getString("applet_previousb"),
                bundle.getString("applet_cancel"),
                bundle.getString("applet_cancelb"),
                bundle.getString("applet_next"),
                bundle.getString("applet_nextb"),
                bundle.getString("applet_nextx") };

        // notify session of additional state
        p_sessionMgr.setAttribute("DBColumns", m_DBColumns);
        p_sessionMgr.setAttribute("ModePairs", m_mode_pairs);

        Vector objs = new Vector();
        objs.addElement(labels);
        objs.addElement(header);
        objs.addElement(gridData);
        objs.addElement(dialogLabels);
        objs.addElement(dialogButtons);
        objs.addElement(imageNames);
        objs.addElement(m_mode_pairs);
        objs.addElement(m_knownFormatTypePairs);
        // use method in FileProfileMainHandler
        objs.addElement(HashtableValueOrderWalkerFactory
                .createHashtableValueOrderWalker(getXmlRules(p_session)));
        objs.addElement(getI18NContents(bundle));

        return objs;
    }

    private Hashtable<String, String> getI18NContents(ResourceBundle bundle)
    {
        Hashtable<String, String> i18nContents = new Hashtable<String, String>();

        i18nContents.put("lb_cancel",
                bundle.getString("applet.resources.lb_cancel"));
        i18nContents.put("lb_ok", bundle.getString("applet.resources.lb_ok"));

        return i18nContents;
    }

    // create a grid data object
    private List<Object[]> getDBColumns(long profileId)
            throws EnvoyServletException
    {
        Vector dbcolumns = null;

        // if there is no database profile, there are no columns from database
        // to display
        if ((profileId == -1) || (m_ModDBColumns.equals("True")))
        {
            if (m_DBColumns != null)
                dbcolumns = m_DBColumns;
        }
        else
        {
            // Call server
            try
            {
                dbcolumns = vectorizedCollection(ServerProxy
                        .getDatabaseColumnPersistenceManager()
                        .getDatabaseColumns(profileId));
                m_DBColumns = dbcolumns;
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.EX_GENERAL, e);
            }
        }

        int rowSize = dbcolumns.size();
        List<Object[]> columns = new ArrayList<Object[]>(rowSize);
        Integer integer;
        for (int i = 0; i < rowSize; i++)
        {
            DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl) dbcolumns
                    .elementAt(i);
            Object[] column = new Object[5];
            column[0] = dbcolumn;
            column[1] = dbcolumn.getLabel();
            column[2] = dbcolumn.getTableName();
            integer = new Integer((int) dbcolumn.getFormatType());
            column[3] = m_knownFormatTypePairs.get(integer);
            integer = new Integer(dbcolumn.getContentMode());
            column[4] = m_mode_pairs.get(integer);

            columns.add(column);
        }
        return columns;
    }

    // create hashtable of ContentMode names and ids
    HashtableValueOrderWalker getModePairs(ResourceBundle bundle)
    {
        Hashtable mode_pair = new Hashtable();
        mode_pair.put(new Integer(1), bundle.getString("lb_translatable"));
        mode_pair.put(new Integer(2), bundle.getString("lb_contextual"));
        mode_pair.put(new Integer(3), bundle.getString("lb_invisible"));
        return HashtableValueOrderWalkerFactory
                .createHashtableValueOrderWalker(mode_pair);
    }

    // process data and save info to session object to pass on to other pages
    // when necessary
    private Vector saveData(HttpSession p_session,
            HttpServletRequest p_theRequest, SessionManager p_sessionMgr)
            throws EnvoyServletException, IOException
    {
        try
        {
            ObjectInputStream inputFromApplet = new ObjectInputStream(
                    p_theRequest.getInputStream());
            Vector data = (Vector) inputFromApplet.readObject();
            if (data != null)
            {
                DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl) data
                        .elementAt(1);
                // HttpSession session = p_theRequest.getSession();
                switch (((Integer) data.elementAt(0)).intValue())
                {
                    case NEW:
                        // notify session of Column data chosen
                        m_DBColumns.addElement(dbcolumn);
                        long size = m_DBColumns.size();
                        dbcolumn.setColumnNumber(size);
                        p_sessionMgr.setAttribute("DBColumns", m_DBColumns);
                        return data;// break;
                    case MODIFY:
                        // update modified column in m_DBColumns
                        int index = (int) dbcolumn.getColumnNumber() - 1;
                        m_DBColumns.set(index, dbcolumn);
                        p_sessionMgr.setAttribute("DBColumns", m_DBColumns);
                        m_ModDBColumns = "True";
                        p_sessionMgr.setAttribute("ModDBColumns",
                                m_ModDBColumns);
                        return data;// break;
                    case TRANSPOSE:
                        // the incoming element always has the lowest index
                        // ColumnNumber's run 1,...,N and Vector indices 0,...,
                        // (N-1)
                        int itmp = (int) dbcolumn.getColumnNumber();
                        DatabaseColumnImpl dbcolumn1 = (DatabaseColumnImpl) m_DBColumns
                                .set(itmp - 1, dbcolumn);
                        dbcolumn1
                                .setColumnNumber(dbcolumn1.getColumnNumber() + 1);
                        int itmp1 = (int) dbcolumn1.getColumnNumber();
                        m_DBColumns.set(itmp, dbcolumn1);
                        p_sessionMgr.setAttribute("DBColumns", m_DBColumns);
                        return data;
                    case REMOVE:
                        // remove column in m_DBColumns
                        int index1 = (int) dbcolumn.getColumnNumber() - 1;
                        m_DBColumns.remove(index1);
                        // decrement the column numbers of all columns with
                        // higher column numbers
                        DatabaseColumnImpl dbcolumn2;
                        for (int i = index1; i < m_DBColumns.size(); i++)
                        {
                            dbcolumn2 = (DatabaseColumnImpl) m_DBColumns
                                    .elementAt(i);
                            dbcolumn2.setColumnNumber(dbcolumn2
                                    .getColumnNumber() - 1);
                        }
                        p_sessionMgr.setAttribute("DBColumns", m_DBColumns);
                        break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ex);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, re);
        }
        return null;
    }

    // create hashtable of KnownFormatType names and ids
    private Hashtable getKnownFormatTypes() throws EnvoyServletException
    {
        // Call server
        Vector knownFormatTypes = null;

        try
        {
            knownFormatTypes = vectorizedCollection(ServerProxy
                    .getFileProfilePersistenceManager()
                    .getAllKnownFormatTypes());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }

        int rowSize = knownFormatTypes.size();
        Hashtable result = new Hashtable();

        for (int i = 0; i < rowSize; i++)
        {
            KnownFormatTypeImpl knownformattype = (KnownFormatTypeImpl) knownFormatTypes
                    .elementAt(i);

            result.put(new Integer((int) knownformattype.getId()),
                    knownformattype.getName());
        }

        return result;
    }

    // create hashtable of Xml rule names and ids
    private Hashtable getXmlRules(HttpSession p_session)
            throws EnvoyServletException
    {
        // Call server
        Vector xmlrulefiles = null;

        try
        {
            xmlrulefiles = vectorizedCollection(ServerProxy
                    .getXmlRuleFilePersistenceManager().getAllXmlRuleFiles());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }

        int rowSize = xmlrulefiles.size();
        ResourceBundle bundle = getBundle(p_session);
        Hashtable result = new Hashtable();
        // add the "none choice
        result.put(new Integer(-1), bundle.getString("lb_none"));

        for (int i = 0; i < rowSize; i++)
        {
            XmlRuleFileImpl xmlrulefile = (XmlRuleFileImpl) xmlrulefiles
                    .elementAt(i);

            result.put(new Integer((int) xmlrulefile.getId()),
                    xmlrulefile.getName());
        }

        return result;
    }

}
