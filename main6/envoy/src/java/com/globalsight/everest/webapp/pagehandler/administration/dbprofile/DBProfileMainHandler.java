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

// Envoy packages
import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl;
import com.globalsight.cxe.entity.previewurl.PreviewUrlImpl;
import com.globalsight.everest.util.comparator.DBProfileComparator;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.dbdispatch.DBDispatchMainHandler;
import com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileMainHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.collections.HashtableValueOrderWalker;
import com.globalsight.util.collections.HashtableValueOrderWalkerFactory;

//Sun
import java.io.IOException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * DBProfileMainHandler, A page handler to produce the entry page(index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class DBProfileMainHandler extends PageHandler
{
    public static String PROFILE_KEY = "profile";
    public static String PROFILE_LIST = "profiles";


    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            p_request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);

        try
        {
            // create or update database profile
            DatabaseProfileImpl dbprofile =
                createModifyDatabaseProfile(p_request, sessionMgr);

            // create or update database columns
            if (dbprofile != null)
                createDatabaseColumns(p_request, dbprofile, sessionMgr);

            clearSessionExceptTableInfo(session, PROFILE_KEY);
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Get list of all db profiles.
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Collection dbprofiles =
            ServerProxy.getDatabaseProfilePersistenceManager().getAllDatabaseProfiles();

        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        SessionManager sessionMgr = (SessionManager)
            p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        setTableNavigation(p_request, p_session, new ArrayList(dbprofiles),
                       new DBProfileComparator(uiLocale),
                       10,
                       PROFILE_LIST, PROFILE_KEY);
        checkPreReqData(p_request, p_session);
    }

    /**
     * Before being able to create a Profile, certain objects must exist.
     * Check that here.  Also set them in the session if they do exist because
     * they will be needed for new & edit so why bother refetching.
     */
    private void checkPreReqData(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException
    {

        SessionManager sessionMgr = (SessionManager)p_session.getAttribute(
                WebAppConstants.SESSION_MANAGER);

        DBDispatchMainHandler dbdh = new DBDispatchMainHandler();

        // gather connection pairs 
        HashtableValueOrderWalker dbconnection_pairs =
            HashtableValueOrderWalkerFactory.createHashtableValueOrderWalker(
            dbdh.getDBConnectionPairs());
        sessionMgr.setAttribute("DBConnectionPairs", dbconnection_pairs);

        // gather preview url name id pairs for user to choose
        HashtableValueOrderWalker previewurl_pairs = getPreviewUrlPairs(p_session);
        sessionMgr.setAttribute("PreviewUrlPairs", previewurl_pairs);

        // gather localization profiles name id pairs for user to choose
        // use method in FileProfileMainHandler
        HashtableValueOrderWalker l10nProfile_pairs =
            HashtableValueOrderWalkerFactory.createHashtableValueOrderWalker(
            getL10nProfiles());
        sessionMgr.setAttribute("L10nProfilePairs",l10nProfile_pairs);

        if (dbconnection_pairs == null || dbconnection_pairs.size() < 1
            || previewurl_pairs == null || previewurl_pairs.size() < 1
            || l10nProfile_pairs == null || l10nProfile_pairs.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if (dbconnection_pairs == null || dbconnection_pairs.size() < 1 )
            {
                message.append(bundle.getString("lb_db_connections"));
                addcomma = true;
            }
            if (previewurl_pairs == null || previewurl_pairs.size() < 1)
            {
                if (addcomma) message.append(", ");
                message.append(bundle.getString("lb_db_preview_rules"));
                addcomma = true;
            }
            if (l10nProfile_pairs == null || l10nProfile_pairs.size() < 1)
            {
                if (addcomma) message.append(", ");
                message.append(bundle.getString("lb_loc_profiles"));
                addcomma = true;
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    /**
     *  obtain preview url name id pairs
     */
    private HashtableValueOrderWalker getPreviewUrlPairs(HttpSession p_session)
        throws EnvoyServletException
    {
        Vector previewurls = null;
        ResourceBundle bundle = getBundle(p_session);

        try
        {
            previewurls = vectorizedCollection(ServerProxy.getPreviewUrlPersistenceManager().getAllPreviewUrls());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }

        int rowSize = previewurls.size();
        Hashtable previewurl_pair = new Hashtable();
        // add the "none choice
        previewurl_pair.put(new Integer((int)-1), bundle.getString("lb_none"));
        for (int i=0; i < rowSize; i++)
        {
            PreviewUrlImpl previewurl = (PreviewUrlImpl)previewurls.elementAt(i);
            previewurl_pair.put(new Integer((int)previewurl.getId()), previewurl.getName()
);
        }

        return HashtableValueOrderWalkerFactory.createHashtableValueOrderWalker(previewurl_pair);
    }

    /**
     * create hashtable of L10nProfile names and ids
     */
    private Hashtable getL10nProfiles()
        throws EnvoyServletException
    {
        //Call server
        Hashtable l10nprofiles = null;

        try
        {
            l10nprofiles = ServerProxy.getProjectHandler().
                getAllL10nProfileNames();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }

        return l10nprofiles;
    }


    private DatabaseProfileImpl createModifyDatabaseProfile(
        HttpServletRequest p_theRequest,
        SessionManager p_sessionMgr)
        throws EnvoyServletException, IOException
    {
        Integer Itmp;

        // check for fields requiring a value
        String name;
        if (p_sessionMgr.getAttribute("DBProfileName") == null)
            return null;
        else
            name = (String)p_sessionMgr.getAttribute("DBProfileName");

        long l10nProfileId;
        if (p_sessionMgr.getAttribute("locProfile") == null)
            return null;
        else
        {
            Itmp = new Integer((String)p_sessionMgr.getAttribute("locProfile"));
            l10nProfileId = Itmp.longValue();
        }

        long checkOutConnectProfileId;
        if (p_sessionMgr.getAttribute("acquisitionConn") == null)
            return null;
        else
        {
            Itmp = new Integer((String)p_sessionMgr.getAttribute("acquisitionConn"));
            checkOutConnectProfileId = Itmp.longValue();
        }

        long checkInConnectProfileId;
        if (p_theRequest.getParameter("finalConn") == null)
            return null;
        else
        {
            Itmp = new Integer((String)p_theRequest.getParameter("finalConn"));
            checkInConnectProfileId = Itmp.longValue();
        }

        // load the fields that do not need a value
        String description = (String)p_sessionMgr.getAttribute("DBProfileDescription");

        String checkOutSql = (String)p_sessionMgr.getAttribute("acquisitionSQL");

        String checkInInsertSql = (String)p_theRequest.getParameter("finalInsertSQL");
        String checkInUpdateSql = (String)p_theRequest.getParameter("finalUpdateSQL");

        // handle various preview url selection cases
        Itmp = new Integer((String)p_sessionMgr.getAttribute("previewURL"));
        long previewConnectProfileId;
        long previewUrlId;
        String previewInsertSql;
        String previewUpdateSql;
        if (Itmp.longValue() == -1)  // no preview connection <=> "choose ..." selection
        {
            previewUrlId = 0;
            previewConnectProfileId = 0;
            previewInsertSql = "";
            previewUpdateSql = "";
        }
        else
        {
            previewUrlId = Itmp.longValue();
            Itmp = new Integer((String)p_sessionMgr.getAttribute("previewConn"));
            previewConnectProfileId = Itmp.longValue();
            previewInsertSql = (String)p_sessionMgr.getAttribute("previewInsertSQL");
            previewUpdateSql = (String)p_sessionMgr.getAttribute("previewUpdateSQL");
        }

        // only UTF-8 is permitted for database codeset
        String codeSet = "UTF-8";

        // create DatabaseProfileImpl
        DatabaseProfileImpl dbprofile;

        // create database profile in database
        try
        {
            if (p_sessionMgr.getAttribute("ModDBProfile") != null)
            {
                dbprofile = (DatabaseProfileImpl)p_sessionMgr.getAttribute("ModDBProfile");

                // modify DatabaseProfileImpl
                dbprofile.setName(name);
                dbprofile.setDescription(description);
                dbprofile.setCheckOutConnectionProfileId(checkOutConnectProfileId);
                dbprofile.setCheckOutSql(checkOutSql);
                dbprofile.setCheckInConnectionProfileId(checkInConnectProfileId);
                dbprofile.setCheckInInsertSql(checkInInsertSql);
                dbprofile.setCheckInUpdateSql(checkInUpdateSql);
                dbprofile.setPreviewConnectionProfileId(previewConnectProfileId);
                dbprofile.setPreviewInsertSql(previewInsertSql);
                dbprofile.setPreviewUpdateSql(previewUpdateSql);
                dbprofile.setPreviewUrlId(previewUrlId);
                dbprofile.setL10nProfileId(l10nProfileId);
                dbprofile.setCodeSet(codeSet);
                ServerProxy.getDatabaseProfilePersistenceManager().updateDatabaseProfile(dbprofile);
            }
            else
            {
                // create DatabaseProfileImpl
                dbprofile = new DatabaseProfileImpl(name,
                    description,
                    checkOutConnectProfileId,
                    checkOutSql,
                    checkInConnectProfileId,
                    checkInInsertSql,
                    checkInUpdateSql,
                    previewConnectProfileId,
                    previewInsertSql,
                    previewUpdateSql,
                    previewUrlId,
                    l10nProfileId,
                    codeSet);

                ServerProxy.getDatabaseProfilePersistenceManager().createDatabaseProfile(dbprofile);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }

        return dbprofile;
    }

    private void createDatabaseColumns(HttpServletRequest p_theRequest,
                                       DatabaseProfileImpl p_dbprofile,
                                       SessionManager p_sessionMgr)
        throws EnvoyServletException, IOException
    {
        Vector columns;

    // check for any DBColumns to persist
        if (p_sessionMgr.getAttribute("DBColumns") == null)
            return;
        else
            columns = (Vector)p_sessionMgr.getAttribute("DBColumns");

        // create database profile in database
        try
        {
            // retrieve DatabaseColumnImpl's
            int size = columns.size();
            long dbprofileId = p_dbprofile.getId();
            Vector dbcolumns =
            vectorizedCollection(ServerProxy
                                 .getDatabaseColumnPersistenceManager()
                                 .getDatabaseColumns(dbprofileId));
            int dbsize = dbcolumns.size();
            Vector ids = new Vector();
            Hashtable originalColumns = new Hashtable();
            for (int j=0; j<dbsize; j++)
            {
                DatabaseColumnImpl dbc = (DatabaseColumnImpl)dbcolumns.get(j);
                originalColumns.put(dbc.getIdAsLong(), dbc);
            }

            for (int i = 0; i < size; i++)
            {
                DatabaseColumnImpl dbcolumn = (DatabaseColumnImpl)columns.elementAt(i);
                dbcolumn.setDatabaseProfileId(dbprofileId);
                if (dbcolumn.getId() > -1)
                {
                    ServerProxy.getDatabaseColumnPersistenceManager().updateDatabaseColumn(dbcolumn);
                }
                else
                    ServerProxy.getDatabaseColumnPersistenceManager().createDatabaseColumn(dbcolumn);


                Long id = dbcolumn.getIdAsLong();
                if (originalColumns.containsKey(id))
                    originalColumns.remove(id);
            }
            //now remove the additional columns
            Object[] keys = originalColumns.keySet().toArray();
            for (int h=0; h<keys.length; h++)
            {
                DatabaseColumnImpl column =
                    (DatabaseColumnImpl)originalColumns.get(keys[h]);
                ServerProxy.getDatabaseColumnPersistenceManager()
                .deleteDatabaseColumn(column);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }
}

