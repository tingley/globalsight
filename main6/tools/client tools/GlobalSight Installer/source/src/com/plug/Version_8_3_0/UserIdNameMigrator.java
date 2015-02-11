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
package com.plug.Version_8_3_0;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
import com.config.properties.Resource;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;
import com.util.ldap.LdapUtil;

/**
 * Move user id and user name to a mapping table 'USER_ID_USER_NAME' so that
 * rename user can be easily done by editing the user name attribute without
 * need to update the user names in other tables.
 * <p>
 * NOTE: This class is only invoked by upgrade installer during the 8.3 upgrade.
 */
public class UserIdNameMigrator
{
    private static final Logger logger = Logger
            .getLogger(UserIdNameMigrator.class);

    private static DbUtil dbUtil = DbUtilFactory.getDbUtil();

    private static final String SQL_INSERT_USER_ID_USER_NAME = "insert into USER_ID_USER_NAME values(?, ?)";

    private static final String SQL_QUERY_JBPM_DELEGATION = "select ID_, CONFIGURATION_ from JBPM_DELEGATION";

    private static final String SQL_QUERY_USER_ID_USER_NAME_USER_ID_USER_NAME = "select USER_ID from USER_ID_USER_NAME where USER_ID=? and USER_NAME=?";

    private static final String SQL_UPDATE_JBPM_DELEGATION_CONFIGURATION_ = "update JBPM_DELEGATION set CONFIGURATION_=? where ID_=?";

    private final static String WF_TEMPLATE_XML = "GlobalSight/WorkflowTemplateXml";

    private static final String FILE_STORAGE_DIR = "file_storage_dir";

    private static final String DEFAULT_ROLE_NAME = "All qualified users";

    /**
     * Moves user ids and user names to mapping table 'USER_ID_USER_NAME'.
     */
    public static void update()
    {
        Connection conn = null;
        try
        {
            conn = dbUtil.getConnection();
            runUpdateProcess(conn);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            dbUtil.closeConn(conn);
        }
    }

    private static void runUpdateProcess(Connection conn)
    {
        List<User> users = UserUtil.getAllUsers();
        if (users.isEmpty())
        {
            return;
        }
        UI ui = UIFactory.getUI();
        String msg = Resource.get("process.updateUser");

        logger.info("Running update process");
        long start = System.currentTimeMillis();
        try
        {
            int i = 1;
            List<File[]> workflowXmlFiles = collectWorkflowXmlFiles();
            Map<Long, String> delegationXmls = collectJbpmDelegation(conn,
                    SQL_QUERY_JBPM_DELEGATION);
            // "ldap_connection" user should not be in the user list
            for (Iterator it = users.iterator(); it.hasNext();)
            {
                String info = MessageFormat.format(msg, i++, users.size());
                ui.addProgress(0, info);

                User user = (User) it.next();
                logger.info("Updating user " + user.getUserId());
                update(conn, user, workflowXmlFiles, delegationXmls);
                logger.info("Done updating user " + user.getUserId());
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        logger.info("Done moving user ids and user names to 'USER_ID_USER_NAME' table, took "
                + (end - start) + " ms");
    }

    private static void update(Connection conn, User user,
            List<File[]> workflowXmlFiles, Map<Long, String> delegationXmls)
            throws Exception
    {
        String userId = user.getUserId();
        String userName = user.getUserName();
        if (hasUpgraded(conn, userId, userName))
        {
            return;
        }
        updateWorkflowTemplateXml(user, workflowXmlFiles);
        updateJbpmDelegation(conn, user, delegationXmls);
        boolean userNameUpdated = updateUserNameInLdap(userId, userName);
        insertUserIdUserName(conn, userId, userName, userNameUpdated);
    }

    private static boolean hasUpgraded(Connection conn, String userId,
            String userName) throws SQLException
    {
        Object uid = query(conn, SQL_QUERY_USER_ID_USER_NAME_USER_ID_USER_NAME,
                new Object[]
                { userId, userName });
        if (uid != null)
        {
            return true;
        }
        return false;
    }

    private static void updateJbpmDelegation(Connection conn, User user,
            Map<Long, String> delegationXmls) throws SQLException
    {
        logStart("JBPM_DELEGATION", user.getUserId());
        for (long id : delegationXmls.keySet())
        {
            String oriConfiguration = delegationXmls.get(id);
            String newConfiguration = updateDelegationXml(oriConfiguration,
                    user);
            if (!newConfiguration.equals(oriConfiguration))
            {
                updateDelegationXmlInJbpmDelegation(conn, id, newConfiguration);
            }
        }
        logEnd("JBPM_DELEGATION", user.getUserId());
    }

    private static String updateDelegationXml(String delegationXml, User user)
    {
        return updateRoleName(delegationXml, user);
    }

    private static void updateDelegationXmlInJbpmDelegation(Connection conn,
            long id, String delegationXml) throws SQLException
    {
        execOnce(conn, SQL_UPDATE_JBPM_DELEGATION_CONFIGURATION_, new Object[]
        { delegationXml, id });
    }

    private static boolean updateUserNameInLdap(String userId, String userName)
    {
        if (userId.matches("\\d*?") || userId.equals(userName))
        {
            return false;
        }
        logger.info("Updating user " + userId + " username in LDAP");
        UserUtil.modifyUserName(userId);
        logger.info("Done updating user " + userId + " username in LDAP");
        return true;
    }

    private static void updateWorkflowTemplateXml(User user,
            List<File[]> workflowXmlFiles) throws IOException
    {
        logger.info("Updating user " + user.getUserId()
                + " records in workflow template xml");
        for (File[] files : workflowXmlFiles)
        {
            for (File f : files)
            {
                updateWorkflowTemplateXml(f, user);
            }
        }
        logger.info("Done updating user " + user.getUserId()
                + " records in workflow template xml");
    }

    private static void updateWorkflowTemplateXml(File file, User user)
            throws IOException
    {
        String content = FileUtil.readFile(file);
        content = updateRoleName(content, user);
        FileUtil.writeFile(file, content);
    }

    private static String updateRoleName(String content, User user)
    {
        String fullName = user.getFirstName() + " " + user.getLastName();
        Pattern p = Pattern.compile("(<role_name>)([\\s\\S]*?)(</role_name>)");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            String roleName = m.group(2);
            if (!LdapUtil.isStringValid(roleName))
            {
                continue;
            }
            if (DEFAULT_ROLE_NAME.equals(roleName))
            {
                return content;
            }
            StringBuilder sb = new StringBuilder();
            StringTokenizer st = new StringTokenizer(roleName, ",");
            while (st.hasMoreElements())
            {
                String rn = st.nextToken();
                if (rn.equalsIgnoreCase(fullName))
                {
                    sb.append(user.getUserId());
                }
                else
                {
                    sb.append(rn);
                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            String newRoleName = sb.toString();
            String newString = m.group(1) + newRoleName + m.group(3);
            content = content.replace(m.group(), newString);
        }

        return content;
    }

    private static void insertUserIdUserName(Connection conn, String userId,
            String userName, boolean userNameUpdated) throws SQLException
    {
        logStart("USER_ID_USER_NAME", userId);
        execOnce(conn, SQL_INSERT_USER_ID_USER_NAME,
                userNameUpdated ? new Object[]
                { userId, userId } : new Object[]
                { userId, userName });
        logEnd("USER_ID_USER_NAME", userId);
    }

    private static void execOnce(Connection conn, String sql, Object[] params)
            throws SQLException
    {
        execOnce(toPreparedStatement(conn, sql, params));
    }

    private static void execOnce(PreparedStatement ps) throws SQLException
    {
        try
        {
            ps.execute();
        }
        finally
        {
            dbUtil.closeStatement(ps);
        }
    }

    private static Object query(Connection conn, String sql, Object[] params)
            throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = toPreparedStatement(conn, sql, params);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getObject(1);
            }
        }
        finally
        {
            dbUtil.closeResultSet(rs);
            dbUtil.closeStatement(ps);
        }
        return null;
    }

    private static PreparedStatement toPreparedStatement(Connection conn,
            String sql, Object[] params) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
        {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }

    private static void logStart(String table, String user)
    {
        logger.info("Updating user " + user + " records in table " + table);
    }

    private static void logEnd(String table, String user)
    {
        logger.info("Done updating user " + user + " records in table " + table);
    }

    private static File getFileStorageDir()
    {
        return new File(InstallValues.getIfNull(FILE_STORAGE_DIR, null));
    }

    private static List<File[]> collectWorkflowXmlFiles()
    {
        List<File[]> xmlFiles = new ArrayList<File[]>();

        File fileStorageDir = getFileStorageDir();
        if (fileStorageDir.exists() && fileStorageDir.isDirectory())
        {
            File[] companyNames = fileStorageDir.listFiles();
            for (File company : companyNames)
            {
                if (company.isDirectory())
                {
                    File workflowTemplateDir = new File(company.getPath(),
                            WF_TEMPLATE_XML);
                    if (workflowTemplateDir.exists()
                            && workflowTemplateDir.isDirectory())
                    {
                        xmlFiles.add(workflowTemplateDir
                                .listFiles(new FileFilter()
                                {
                                    public boolean accept(File f)
                                    {
                                        if (f.isFile())
                                        {
                                            if (f.getName().toLowerCase()
                                                    .endsWith(".xml"))
                                            {
                                                return true;
                                            }
                                        }

                                        return false;
                                    }
                                }));
                    }
                }
            }
        }
        return xmlFiles;
    }

    private static Map<Long, String> collectJbpmDelegation(Connection conn,
            String sql) throws SQLException
    {
        Map<Long, String> list = new HashMap<Long, String>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = toPreparedStatement(conn, sql, new Object[]
            {});
            rs = ps.executeQuery();
            while (rs.next())
            {
                long id = rs.getLong(1);
                String delegationXml = rs.getString(2);
                list.put(id, delegationXml);
            }
        }
        finally
        {
            dbUtil.closeResultSet(rs);
            dbUtil.closeStatement(ps);
        }
        return list;
    }
}
