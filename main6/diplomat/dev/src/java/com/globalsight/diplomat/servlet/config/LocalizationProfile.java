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

package com.globalsight.diplomat.servlet.config;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

public class LocalizationProfile implements Serializable
{
    private final String NAME = "Name";
    private final String CHARACTER_SET = "CHARACTER_SET";
    private final String SOURCE_LANGUAGE = "Source_Language";
    private final String TARGET_LANGUAGE = "Target_Language";
    private final String LANGUAGE_TM = "TM_Language";
    private final String TM_MATCH_STYLE = "TM_MATCH_STYLE";
    private final String PAGE_TM = "Page_TM";
    private final String JOB_TEMPLATE_TARGET_ID = "Job_Template_Target_ID";
    private final String STAGE = "Stage";
    private final String SEQUENCE = "Seq_uence";
    private final String USER = "User_ID";
    private final String DURATION = "Duration";
    private final String JOB_TEMPLATE_ID = "Job_Template_ID";
    private final String JOB_TEMPLATE = "Job_Template";
    private final String JOB_TEMPLATE_TARGET = "Job_Template_Target";
    private final String JOB_TEMPLATE_ROUTING = "Job_Template_Routing";
    private final String INCREMENT_JOBTEMPLATEID = "Increment_JobTemplateID";
    private final String NO_TARGET_LANGUAGES = "No Target Languages";
    private final String INCREMENT_JOBTEMPLATETARGETID = "Increment_JobTemplateTargetID";
    private final String NO_ROUTING_STAGES = "No Routing Stages";

    private long m_id = 0;
    private String m_name = "";
    private int m_sourceLanguage = 0;
    private int m_characterSet = 0;
    private Vector m_languageList = null;

    // ///////////////////////////////////////////////
    public LocalizationProfile(String p_name, int p_sourceLanguage,
            int p_characterSet)
    {
        this(p_name, p_sourceLanguage, p_characterSet, new Vector());
    }

    // ///////////////////////////////////////////////
    public LocalizationProfile(String p_name, int p_sourceLanguage,
            int p_characterSet, Vector p_languageList)
    {
        m_name = p_name;
        m_sourceLanguage = p_sourceLanguage;
        m_characterSet = p_characterSet;
        m_languageList = p_languageList;

    }

    // ///////////////////////////////////////////////
    // retrieve the localization profile from the database
    public LocalizationProfile(long p_id)
    {
        Connection connection = null;
        m_id = p_id;
        m_languageList = new Vector();
        Statement query = null;

        // retrieve data from the job template table
        try
        {
            connection = ConnectionPool.getConnection();
            String sql = "SELECT * FROM " + JOB_TEMPLATE + " WHERE "
                    + JOB_TEMPLATE_ID + " = " + m_id;

            query = connection.createStatement();
            ResultSet result = query.executeQuery(sql);
            result.next();
            m_name = result.getString(NAME);
            m_sourceLanguage = result.getInt(SOURCE_LANGUAGE);
            m_characterSet = result.getInt(CHARACTER_SET);
            query.close();

            // retrieve data from job template target table

            sql = "SELECT * FROM " + JOB_TEMPLATE_TARGET + " WHERE "
                    + JOB_TEMPLATE_ID + " = " + m_id;
            query = connection.createStatement();
            result = query.executeQuery(sql);
            while (result.next())
            {
                long targetID = result.getLong(JOB_TEMPLATE_TARGET_ID);
                int targetLanguage = result.getInt(TARGET_LANGUAGE);
                int languageTM = result.getInt(LANGUAGE_TM);
                boolean pageTM = (result.getInt(PAGE_TM) > 0) ? true : false;
                int tm_matchStyle = result.getInt(TM_MATCH_STYLE);
                int characterSet = result.getInt(CHARACTER_SET);

                LocalizationProfileTarget target = new LocalizationProfileTarget(
                        targetID, targetLanguage, languageTM, pageTM,
                        tm_matchStyle, characterSet);

                m_languageList.add(target);
            }
            query.close();

            // retrieve data from job template routing
            for (int i = 0; i < m_languageList.size(); ++i)
            {
                LocalizationProfileTarget target = (LocalizationProfileTarget) m_languageList
                        .elementAt(i);
                long targetID = target.getID();

                sql = "SELECT * FROM " + JOB_TEMPLATE_ROUTING + " WHERE "
                        + JOB_TEMPLATE_TARGET_ID + " = " + targetID;

                query = connection.createStatement();
                ResultSet results = query.executeQuery(sql);
                while (results.next())
                {
                    int stage = results.getInt(STAGE);
                    int sequence = results.getInt(SEQUENCE);
                    long user = results.getLong(USER);
                    int duration = results.getInt(DURATION);

                    LocalizationProfileRouting routing = new LocalizationProfileRouting(
                            stage, sequence, user, duration);
                    target.addRoutingEntry(routing);
                }
                query.close();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    // ///////////////////////////////////////////////
    public long getID()
    {
        return m_id;
    }

    public String getName()
    {
        return m_name;
    }

    public int getSourceLanguage()
    {
        return m_sourceLanguage;
    }

    public int getCharacterSet()
    {
        return m_characterSet;
    }

    public Vector getLanguageList()
    {
        return m_languageList;
    }

    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setSourceLanguage(int p_sourceLanguage)
    {
        m_sourceLanguage = p_sourceLanguage;
    }

    public void setCharacterSet(int p_characterSet)
    {
        m_characterSet = p_characterSet;
    }

    public void setLanguageList(Vector p_languageList)
    {
        m_languageList = p_languageList;
    }

    // ///////////////////////////////////////////////
    public int getLanguageListSize()
    {
        return m_languageList.size();
    }

    // ///////////////////////////////////////////////
    public void addLanguageEntry(LocalizationProfileTarget target)
    {
        m_languageList.addElement(target);
    }

    // ///////////////////////////////////////////////
    public boolean deleteLanguageEntry(LocalizationProfileTarget target)
    {
        return m_languageList.removeElement((Object) target);
    }

    // ///////////////////////////////////////////////
    public void deleteEntry()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            _deleteEntry(connection);
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    private void _deleteEntry(Connection connection) throws SQLException
    {
        if (m_id > 0)
        {
            connection.setAutoCommit(false);
            String sql = "DELETE FROM " + JOB_TEMPLATE + " WHERE "
                    + JOB_TEMPLATE_ID + " = " + m_id;
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
    }

    // ///////////////////////////////////////////////
    public void update() throws NoSuchFieldException
    {
        Connection connection = null;
        Statement query;
        String sql;
        ResultSet result;
        ResultSet results;

        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            theLogger.println(Logger.DEBUG_D, "Deleting entry.");

            // delete the previous localization profile if it exists
            _deleteEntry(connection);

            // retrieve a new id
            if (0 == m_id)
            {
                sql = "SELECT " + INCREMENT_JOBTEMPLATEID
                        + ".NEXTVAL FROM DUAL";
                query = connection.createStatement();
                result = query.executeQuery(sql);
                result.next();
                m_id = result.getLong(1);
                theLogger.println(Logger.DEBUG_D, "Got new id:" + m_id);
                query.close();
            }

            // create a new row in Job_Template
            sql = "INSERT INTO " + JOB_TEMPLATE + " VALUES ( " + m_id + ","
                    + Utility.quote(m_name) + "," + m_sourceLanguage + ","
                    + m_characterSet + ")";

            connection.createStatement().executeUpdate(sql);
            theLogger.println(Logger.DEBUG_D, "inserted into job_template.");

            // check for entries in languageList
            if (m_languageList.size() == 0)
            {
                theLogger
                        .println(Logger.WARNING,
                                "No target languages were chosen for the L10N Profile.");
                throw new NoSuchFieldException(NO_TARGET_LANGUAGES);
            }

            // create rows in job template target
            for (int i = 0; i < m_languageList.size(); ++i)
            {
                LocalizationProfileTarget target = (LocalizationProfileTarget) m_languageList
                        .elementAt(i);
                // retrieve a new target id
                sql = "SELECT " + INCREMENT_JOBTEMPLATETARGETID
                        + ".NEXTVAL FROM DUAL";

                query = connection.createStatement();
                result = query.executeQuery(sql);
                result.next();
                target.setID(result.getLong(1));
                query.close();

                sql = "INSERT INTO " + JOB_TEMPLATE_TARGET + " VALUES (" + m_id
                        + "," + target.getID() + "," + target.getLanguage()
                        + "," + target.getLanguageTM() + ","
                        + (target.getPageTM() ? 1 : 0) + ","
                        + target.getTM_MatchStyle() + ","
                        + target.getCharacterSet() + ")";

                connection.createStatement().executeUpdate(sql);
                theLogger.println(Logger.DEBUG_D,
                        "inserted into job_template_target.");

                // create rows in job template routing
                Vector routing = target.getRoutingList();

                // check for entries in languageList
                if (routing.size() == 0)
                {
                    theLogger
                            .println(Logger.WARNING,
                                    "No workflow stages were chosen in the L10N Profile.");
                    throw new NoSuchFieldException(NO_ROUTING_STAGES);
                }

                for (int j = 0; j < routing.size(); ++j)
                {
                    LocalizationProfileRouting route = (LocalizationProfileRouting) routing
                            .elementAt(j);
                    sql = "INSERT INTO " + JOB_TEMPLATE_ROUTING + " VALUES ( "
                            + target.getID() + "," + route.getStage() + ","
                            + route.getSequence() + "," + route.getUser() + ","
                            + route.getDuration() + ")";
                    connection.createStatement().executeUpdate(sql);
                    theLogger.println(Logger.DEBUG_D,
                            "inserted into job_template_routing");
                }
            }

            // commit the update!
            connection.commit();
            connection.setAutoCommit(true);
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", e);
            // attempt a roll-back
            try
            {
                if (connection != null)
                    connection.rollback();
            }
            catch (SQLException sqle2)
            {
                theLogger.printStackTrace(Logger.ERROR, "Could not rollback: ",
                        sqle2);
            }
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveLocalizationProfile()
    {
        ResultSet results = null;
        Statement query = null;
        Connection connection = null;
        Vector profiles = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            query = connection.createStatement();
            String sql = "SELECT JOB_TEMPLATE_ID FROM JOB_TEMPLATE ORDER BY JOB_TEMPLATE_ID";
            results = query.executeQuery(sql);
            while (results.next())
            {
                long id = results.getLong(1);
                profiles.add(new LocalizationProfile(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "LocalizationProfile", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
        return profiles;
    }

    private Logger theLogger = Logger.getLogger();
}
