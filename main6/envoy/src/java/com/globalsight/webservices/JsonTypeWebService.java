/**
 * 
 */
package com.globalsight.webservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.terminology.util.SqlUtil;

/**
 * @author Mark
 * @since 2013-07-15
 * @version 8.5.1
 */
public abstract class JsonTypeWebService extends AbstractWebService
{
    private static final Logger logger = Logger
            .getLogger(JsonTypeWebService.class);
    public static String ERROR_JOB_NAME = "You cannot have \\, /, :, ;, *, ?, |, \", &lt;, &gt;, % or &amp; in the Job Name.";

    private static String capLoginUrl = null;

    // jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/
    private static String webServerDocRoot = null;

    static public final String DEFAULT_TYPE = "text";

    // new version used to check, since 8.2.1
    // need to be changed according to the release version each time
    private static String VERSION_NEW = "(3.1,8.5)";

    /**
     * used by checkIfInstalled() to remember whether the web service is
     * installed
     */
    // Whether the web service is installed
    private static boolean isWebServiceInstalled = false;

    /**
     * Check if the installation key for the WebService is correct
     * 
     * @return boolean Return true if the installation key is correct, otherwise
     *         return false.
     */
    public static boolean isInstalled()
    {
        isWebServiceInstalled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.WEBSVC_INSTALL_KEY);

        return isWebServiceInstalled;
    }

    static
    {
        try
        {
            isInstalled();
            SystemConfiguration config = SystemConfiguration.getInstance();
            capLoginUrl = config.getStringParameter("cap.login.url");

            webServerDocRoot = config
                    .getStringParameter(SystemConfigParamNames.WEB_SERVER_DOC_ROOT);
            if (!(webServerDocRoot.endsWith("/") || webServerDocRoot
                    .endsWith("\\")))
            {
                webServerDocRoot = webServerDocRoot + "/";
            }
        }
        catch (Exception ne)
        {
            logger.error("Failed to find environment value " + ne);
        }
    }

    /**
     * Logs into the WebService. Returns an access token and company name
     * 
     * The format of returning string is 'p_accessToken+_+CompanyName'.
     * 
     * @param p_username
     *            Username used to log in
     * 
     * @param p_password
     *            Password used to log in
     * 
     * @return java.lang.String Access token and company name which user works
     *         for
     * 
     * @exception WebServiceException
     */
    public String login(String p_username, String p_password)
            throws WebServiceException
    {
        String p_accessToken = this.doLogin(p_username, p_password);
        String separator = "+_+";
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(p_username);
            return p_accessToken + separator + user.getCompanyName();
        }
        catch (Exception e)
        {
            String errorMsg = makeErrorJson("login", e.getMessage());
            throw new WebServiceException(errorMsg);
        }
    }

    protected void checkAccess(String p_accessToken, String p_webMethodName)
            throws WebServiceException
    {
        try
        {
            super.checkAccess(p_accessToken, p_webMethodName);
        }
        catch (Exception e)
        {
            String message = "The p_accessToken parameter is invalid : '"
                    + p_accessToken + "'.";
            String returning = makeErrorJson(p_webMethodName, message);
            throw new WebServiceException(returning);
        }
    }

    protected void releaseDBResource(ResultSet results,
            PreparedStatement query, Connection connection)
    {
        SqlUtil.silentClose(results);
        SqlUtil.silentClose(query);
        SqlUtil.fireConnection(connection);
    }

    protected String makeErrorJson(String p_method, String p_message)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put(p_method, p_message);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * Says Hello. Trivial web method.
     * 
     * @return String Message to welcome
     * 
     * @exception WebServiceException
     */
    public String helloWorld() throws WebServiceException
    {
        checkIfInstalled();
        return "Hello from the Welocalize GlobalSight Web service.";
    }

    @Override
    protected void checkIfInstalled() throws WebServiceException
    {
        if (!isWebServiceInstalled)
            throw new WebServiceException("Web services is not installed.");
    }

    protected String resultSetToJsonArray(ResultSet rs)
            throws SQLException,
            JSONException
    {
        JSONArray array = new JSONArray();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next())
        {
            JSONObject jsonObj = new JSONObject();

            for (int i = 1; i <= columnCount; i++)
            {
                String columnName = metaData.getColumnLabel(i);
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.put(jsonObj);
        }

        return array.toString();
    }

    protected String resultSetToJson(String columnName, ResultSet results)
            throws Exception
    {
        return resultSetToJson(columnName, columnName, results);

    }

    protected String resultSetToJson(String alias, String columnName,
            ResultSet results)
            throws Exception
    {
        if (null == alias)
        {
            alias = columnName;
        }
        JSONObject jsonObj = new JSONObject();
        StringBuffer item = new StringBuffer();

        while (results.next())
        {
            item.append(results.getString(columnName)
                    + (results.isLast() ? "" : ","));
        }
        jsonObj.put(alias, item.toString());
        return jsonObj.toString();
    }
}
