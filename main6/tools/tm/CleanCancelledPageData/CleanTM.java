/*
 * Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;

import oracle.jdbc.driver.OracleResultSet;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

/**
 * <p>This class connects to a System 4 database and removes all
 * garbage data from the TM.
 *
 * <p>Removed is the following: for all pages whose Job was cancelled:
 * - all template parts
 * - all templates
 * - all leverage matches
 * - all task tuvs
 * - all replace image records
 * - all target TUVs in state OUT_OF_DATE
 * - all source TUVs with no target TUV left
 * - all fuzzy index records for the deleted TUVs
 * - all TUs with 0 TUVs left in them
 *
 * <p>I may have missed a few objects that could be deleted also, but
 * I don't care. The main objective of this program is to reduce TUs
 * and TUVs in the TM and slow down leveraging. As long as leveraging
 * performance is improved by less garbage, the objective is met.
 */
public class CleanTM
{
    private static final int EOF = -1;

//    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
//    private static final String CONNECT_THIN_CLIENT = "jdbc:oracle:thin:@";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String CONNECT_THIN_CLIENT = "jdbc:mysql://";
    private static final String PROPERTIES = "db_connection";

    /** Helperclass to connect to Oracle. */
    public static class ConnectData
    {
        public String m_server = "(default)";
        public String m_user = null;
        public String m_password = null;
        public String m_sid = null;
        public int    m_portNumber = 1521;
        public String m_connectString = null;

        public ConnectData(String p_connectString,
            String p_user, String p_password)
        {
            m_connectString = p_connectString;
            m_user = p_user;
            m_password = p_password;
        }

        public ConnectData(String p_server, String p_user, String p_passwd,
            String p_sid, String p_port)
        {
            m_server   = p_server;
            m_user     = p_user;
            m_password = p_passwd;
            m_sid      = p_sid;
            m_portNumber = Integer.parseInt(p_port);
        }

        public String getConnectString()
        {
            if (m_connectString == null)
            {
                m_connectString = CONNECT_THIN_CLIENT + m_server +
                    ":" + m_portNumber + "/" + m_sid;
            }

            return m_connectString;
        }
    }

    /** Helper class to hold page info. */
    public class PageInfo
    {
        public long m_pageId;
        public int m_sourceLocaleId;
        public int m_jobId;
    }

    //
    // Private Members
    //
    private ConnectData m_connectData = null;
    private Connection  m_connection = null;

    private DateFormat m_dateFormat = null;

    private ArrayList m_jobs = null;
    private ArrayList m_pages = null;

    //
    // Main Routine
    //
    public static void main(String args[])
        throws FileNotFoundException, ClassNotFoundException,
               SQLException, IOException
    {
        ConnectData connectData = null;

        // Allow specification of connect data.
        if (args.length == 5)
        {
            String server = args[0];
            String user   = args[1];
            String passwd = args[2];
            String sid    = args[3];
            String port   = args[4];

            connectData = new ConnectData(server, user, passwd, sid, port);
        }
        else if (args.length == 0)
        {
            connectData = getConnectData();
        }

        if (connectData == null)
        {
            System.err.println(
                "Usage: java CleanTM server user password instance port");
            System.err.println(
                "  Deletes data from a System4 TM that does not contribute" +
                " to TM leveraging\n" +
                "  (garbage data).\n" +
                "  Also deletes out-of-date page data.\n");

            System.err.println(
                "  You can interrupt this program at any time and " +
                "it will continue deleting\n" +
                "  any remaining out-of-date data if you run it again.\n");

            System.exit(1);
        }

        CleanTM x = new CleanTM(connectData);

        x.deleteGarbage();
    }

    private static ConnectData getConnectData()
    {
        ConnectData result = null;

        String connectString = null;
        String user = null;
        String password = null;

        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle(PROPERTIES, Locale.US);

            Enumeration keys = res.getKeys();
            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                String tmp = key.toLowerCase();

                if (tmp.equals("connect_string"))
                {
                    connectString = res.getString(key);
                }
                else if (tmp.equals("user_name"))
                {
                    user = res.getString(key);
                }
                else if (tmp.equals("password"))
                {
                    password = res.getString(key);
                }
            }

            result = new ConnectData(connectString, user, password);
        }
        catch (Throwable t)
        {
        }

        return result;
    }

    //
    // Constructor
    //

    public CleanTM(ConnectData p_connectData)
    {
        m_connectData = p_connectData;

        m_dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
    }

    //
    // Public Methods
    //

    public void trace(String p_message)
    {
        System.out.println(m_dateFormat.format(new java.util.Date()) +
            "; " + p_message);
    }

    /**
     * Deletes garbage TUVs, TUs and other page data from the TM.
     */
    public void deleteGarbage()
        throws FileNotFoundException, ClassNotFoundException,
               SQLException, IOException
    {
        long tuCount, tuvCount;

        m_connection = connect();
        trace("Connected to " + m_connectData.m_server);

        tuCount = getTuCount();
        tuvCount = getTuvCount();
        trace("Total TUs: " + tuCount + " - Total TUVs: " + tuvCount);

        m_pages = readCancelledPages();
        trace("Found " + m_pages.size() + " cancelled pages");

        for (int i = 0; i < m_pages.size(); i++)
        {
            PageInfo info = (PageInfo)m_pages.get(i);

            deletePageGarbage(info);
        }

        trace("Done deleting...");

        tuCount = getTuCount();
        tuvCount = getTuvCount();
        trace("Total TUs: " + tuCount + " - Total TUVs: " + tuvCount);

        trace("Done.");
    }

    public void deletePageGarbage(PageInfo p_info)
        throws SQLException
    {
        long count;

        // DEPENDENT DATA

        trace("");

        ArrayList templateIds = readTemplates(p_info);
        ArrayList targetPageIds = readTargetPageIds(p_info);
        ArrayList allTuvIds = readAllTuvIds(p_info);

        trace("Analyzed data of page " + p_info.m_pageId + ": " +
            allTuvIds.size() + " tuvs");

        count = deleteTemplateParts(templateIds);
        if (count > 0)
        {
            trace("Deleted " + count + " template parts " +
                "of page " + p_info.m_pageId);
        }

        count = deleteTemplates(p_info);
        if (count > 0)
        {
            trace("Deleted " + count + " templates of page " +
                p_info.m_pageId);
        }

        count = deleteLeverageMatches(p_info);
        if (count > 0)
        {
            trace("Deleted " + count + " out-of-date leverage matches " +
                "of page " + p_info.m_pageId);
        }

        count = deleteTaskTuvs(allTuvIds);
        if (count > 0)
        {
            trace("Deleted " + count + " out-of-date task tuvs " +
                "of page " + p_info.m_pageId);
        }

        count = deleteReplacedImages(targetPageIds);
        if (count > 0)
        {
            trace("Deleted " + count + " out-of-date replaced image " +
                "records of page " + p_info.m_pageId);
        }

        // FUZZY INDEX (target)

        ArrayList deletableTargetTuvIds = readDeletableTargetTuvIds(p_info);

        count = deleteFuzzyIndex(deletableTargetTuvIds);
        if (count > 0)
        {
            trace("Deleted " + count + " out-of-date fuzzy index records " +
                "(target) of page " + p_info.m_pageId);
        }

        // TARGET TUVS

        count = deleteGarbageTargetTuvs(p_info);
        trace("Deleted " + count + " out-of-date target TUVs of page " +
            p_info.m_pageId);


        // FUZZY INDEX (source)

        ArrayList deletableSourceTuvIds = readDeletableSourceTuvIds(p_info);

        count = deleteFuzzyIndex(deletableSourceTuvIds);
        if (count > 0)
        {
            trace("Deleted " + count + " out-of-date fuzzy index records " +
                "(source) of page" + p_info.m_pageId);
        }

        // SOURCE TUVS

        count = deleteGarbageSourceTuvs(p_info);
        trace("Deleted " + count + " out-of-date source TUVs of page " +
            p_info.m_pageId);

        // TUS

        count = deleteGarbageTus(p_info);
        trace("Deleted " + count + " out-of-date TUs of page " +
            p_info.m_pageId);
    }

    public ArrayList readCancelledPages()
        throws SQLException, IOException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT s.id, l.source_locale_id, j.id " +
            "FROM job j, request r, source_page s, l10n_profile l " +
            "WHERE j.state = 'CANCELLED' " +
            "  AND j.id = r.job_id " +
            "  AND r.page_id = s.id " +
            "  AND r.l10n_profile_id = l.id");

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            PageInfo info = new PageInfo();

            info.m_pageId = rs.getLong(1);
            info.m_sourceLocaleId = rs.getInt(2);
            info.m_jobId = rs.getInt(3);

            result.add(info);
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Reads template_part ids of inaccessible pages.
     */
    public ArrayList readTargetPageIds(PageInfo p_info)
        throws SQLException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT t.id FROM target_page t " +
            "WHERE t.source_page_id = " + p_info.m_pageId);

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            long id = rs.getLong(1);

            result.add(new Long(id));
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Reads all TUV ids that are in the source and target pages.
     */
    public ArrayList readAllTuvIds(PageInfo p_info)
        throws SQLException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT tuv.id " +
            "FROM source_page s, source_page_leverage_group lg, " +
            "     translation_unit tu, translation_unit_variant tuv " +
            "WHERE s.id = lg.sp_id " +
            "  AND lg.lg_id = tu.leverage_group_id " +
            "  AND tu.id = tuv.tu_id " +
            "  AND s.id = " + p_info.m_pageId);

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            long id = rs.getLong(1);

            result.add(new Long(id));
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Reads all deletable target TUV ids. Deletable target TUVs are
     * all those in a state that cannot be leveraged.
     */
    public ArrayList readDeletableTargetTuvIds(PageInfo p_info)
        throws SQLException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT tuv.id " +
            "FROM source_page s, source_page_leverage_group lg, " +
            "     translation_unit tu, translation_unit_variant tuv " +
            "WHERE s.id = lg.sp_id AND lg.lg_id = tu.leverage_group_id " +
            "  AND tu.id = tuv.tu_id " +
            "  AND tuv.state in ( " +
            "     'OUT_OF_DATE', " +
            "     'LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED', " +
            "     'EXACT_MATCH_LOCALIZED', " +
            "     'NOT_LOCALIZED') " +
            "  AND tuv.locale_id != " + p_info.m_sourceLocaleId +
            "  AND s.id = " + p_info.m_pageId);

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            long id = rs.getLong(1);

            result.add(new Long(id));
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Reads all deletable source TUV ids (after target TUVs have been
     * deleted). Deletable source TUVs are those that have no target
     * TUV left anymore that could be leveraged, i.e. they are alone
     * in their TU.
     */
    public ArrayList readDeletableSourceTuvIds(PageInfo p_info)
        throws SQLException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT tuv.id " +
            "FROM source_page s, source_page_leverage_group lg, " +
            "     translation_unit tu, translation_unit_variant tuv " +
            "WHERE s.id = lg.sp_id AND lg.lg_id = tu.leverage_group_id " +
            "  AND tu.id = tuv.tu_id " +
            "  AND 1 = ( " +
            "    SELECT count(*) FROM translation_unit_variant tuv2 " +
            "    WHERE tuv2.tu_id = tu.id " +
            "  ) " +
            "  AND s.id = " + p_info.m_pageId);

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            long id = rs.getLong(1);

            result.add(new Long(id));
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Reads template_part ids of inaccessible pages.
     */
    public ArrayList readTemplates(PageInfo p_info)
        throws SQLException
    {
        int count = 0;
        ResultSet rs = null;
        PreparedStatement stmt;

        ArrayList result = new ArrayList();

        stmt = m_connection.prepareStatement(
            "SELECT id FROM template " +
            "WHERE source_page_id = " + p_info.m_pageId);

        rs = stmt.executeQuery();

        while (rs.next())
        {
            ++count;

            long id = rs.getLong(1);

            result.add(new Long(id));
        }

        rs.close();
        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Deletes template_part objects of inaccessible pages.
     */
    public int deleteTemplates(PageInfo p_info)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        stmt = m_connection.prepareStatement(
            "DELETE FROM template " +
            "WHERE source_page_id = " + p_info.m_pageId);

        result = stmt.executeUpdate();

        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Deletes template_part objects of inaccessible pages.
     */
    public int deleteTemplateParts(ArrayList p_ids)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        ArrayList clone = new ArrayList(p_ids);
        ArrayList temp = new ArrayList();

        while (clone.size() > 0)
        {
            temp.clear();

            int count = 100;
            while (count > 0 && clone.size() > 0)
            {
                temp.add(clone.remove(0));
                --count;
            }

            stmt = m_connection.prepareStatement(
                "DELETE from template_part " +
                "WHERE template_id IN (" + printIds(temp) + ")");

            result = stmt.executeUpdate();

            stmt.close();

            m_connection.commit();
        }

        return result;
    }

    /**
     * Deletes leverage_match objects for pages. Since pages cannot be
     * opened in the editor, or be worked on in any way, this is ok
     * and needed for removing TUVs and TUs (foreign key constraints).
     */
    public int deleteLeverageMatches(PageInfo p_info)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        stmt = m_connection.prepareStatement(
            "DELETE FROM leverage_match " +
            "WHERE source_page_id = " + p_info.m_pageId);

        result = stmt.executeUpdate();

        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Deletes any records of uploaded images for inaccessible pages.
     *
     * @param p_ids list of target page ids (as Long)
     */
    public int deleteReplacedImages(ArrayList p_ids)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        if (p_ids.size() > 0)
        {
            stmt = m_connection.prepareStatement(
                "DELETE from image_replace_file_map " +
                "WHERE target_page_id IN (" + printIds(p_ids) + ")");

            result = stmt.executeUpdate();

            stmt.close();

            m_connection.commit();
        }

        return result;
    }

    /**
     * Deletes all task tuvs for the given TUV ids. Since pages cannot be
     * opened in the editor, or be worked on in any way, this is ok, and
     * needed to remove TUVs and TUs.
     *
     * @param p_ids list of TUV ids (as Long)
     */
    public int deleteTaskTuvs(ArrayList p_ids)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        ArrayList clone = new ArrayList(p_ids);
        ArrayList temp = new ArrayList();

        while (clone.size() > 0)
        {
            temp.clear();

            int count = 100;
            while (count > 0 && clone.size() > 0)
            {
                temp.add(clone.remove(0));
                --count;
            }

            stmt = m_connection.prepareStatement(
                "DELETE from task_tuv " +
                "WHERE current_tuv_id in (" + printIds(temp) + ")");

            result += stmt.executeUpdate();

            stmt.close();

            m_connection.commit();
        }

        return result;
    }

    /**
     * Deletes all fuzzy index records for the given TUV ids.
     *
     * @param p_ids list of TUV ids (as Long)
     */
    public int deleteFuzzyIndex(ArrayList p_ids)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        ArrayList clone = new ArrayList(p_ids);
        ArrayList temp = new ArrayList();

        while (clone.size() > 0)
        {
            temp.clear();

            int count = 100;
            while (count > 0 && clone.size() > 0)
            {
                temp.add(clone.remove(0));
                --count;
            }

            stmt = m_connection.prepareStatement(
                "DELETE FROM fuzzy_index " +
                "WHERE tuv_id IN (" + printIds(temp) + ")");

            result += stmt.executeUpdate();

            stmt.close();

            m_connection.commit();
        }

        return result;
    }

    /**
     * Deletes all target TUVs that are in an un-leverageable state.
     *
     * @return the number of deleted TUVs
     */
    public int deleteGarbageTargetTuvs(PageInfo p_info)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        stmt = m_connection.prepareStatement(
            "DELETE FROM translation_unit_variant " +
            "WHERE id IN ( " +
            "  SELECT tuv.id " +
            "  FROM source_page s, source_page_leverage_group lg, " +
            "       translation_unit tu, translation_unit_variant tuv " +
            "  WHERE s.id = lg.sp_id AND lg.lg_id = tu.leverage_group_id " +
            "    AND tu.id = tuv.tu_id " +
            "    AND tuv.state IN ( " +
            "       'OUT_OF_DATE', " +
            "       'LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED', " +
            "       'EXACT_MATCH_LOCALIZED', " +
            "       'NOT_LOCALIZED') " +
            "    AND tuv.locale_id != " + p_info.m_sourceLocaleId +
            "    AND s.id = " + p_info.m_pageId +
            ")");

        result = stmt.executeUpdate();

        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Deletes all source TUVs that are alone in their TU, i.e. that
     * have no leverageable target TUV anymore.
     *
     * @return the number of deleted TUVs
     */
    public int deleteGarbageSourceTuvs(PageInfo p_info)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        stmt = m_connection.prepareStatement(
            "DELETE from translation_unit_variant " +
            "WHERE id IN ( " +
            "  SELECT tuv.id " +
            "  FROM source_page s, source_page_leverage_group lg, " +
            "       translation_unit tu, translation_unit_variant tuv " +
            "  WHERE s.id = lg.sp_id and lg.lg_id = tu.leverage_group_id " +
            "    AND tu.id = tuv.tu_id " +
            "    AND 1 = ( " +
            "      SELECT count(*) FROM translation_unit_variant tuv2 " +
            "      where tuv2.tu_id = tu.id " +
            "    ) " +
            "    AND s.id = " + p_info.m_pageId +
            ")");

        result = stmt.executeUpdate();

        stmt.close();

        m_connection.commit();

        return result;
    }


    /**
     * Deletes TUVs that have no target TUVs left that could be
     * leveraged (these TUs will still have a source TUV in state
     * NOT_LOCALIZED).
     */
    public int deleteGarbageTus(PageInfo p_info)
        throws SQLException
    {
        int result = 0;
        PreparedStatement stmt;

        stmt = m_connection.prepareStatement(
            "DELETE FROM translation_unit " +
            "WHERE id IN ( " +
            "  SELECT tu.id " +
            "  FROM source_page s, source_page_leverage_group lg, " +
            "       translation_unit tu " +
            "  WHERE s.id = lg.sp_id and lg.lg_id = tu.leverage_group_id " +
            "    AND NOT EXISTS ( " +
            "      SELECT * FROM translation_unit_variant tuv2 " +
            "      where tuv2.tu_id = tu.id " +
            "    ) " +
            "   AND s.id = " + p_info.m_pageId +
            ")");

        result = stmt.executeUpdate();

        stmt.close();

        m_connection.commit();

        return result;
    }

    /**
     * Retrieves the overall count of TUVs.
     */
    public long getTuvCount()
        throws SQLException
    {
        long result = 0;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = m_connection.prepareStatement(
            "select count(*) from translation_unit_variant");

        rs = stmt.executeQuery();

        if (rs.next())
        {
            result = rs.getLong(1);
        }

        rs.close();
        stmt.close();

        return result;
    }

    /**
     * Retrieves the overall count of TUs.
     */
    public long getTuCount()
        throws SQLException
    {
        long result = 0;
        PreparedStatement stmt;
        ResultSet rs;

        stmt = m_connection.prepareStatement(
            "SELECT count(*) FROM translation_unit");

        rs = stmt.executeQuery();

        if (rs.next())
        {
            result = rs.getLong(1);
        }

        rs.close();
        stmt.close();

        return result;
    }

    public Connection connect()
        throws SQLException, ClassNotFoundException
    {
        Connection result = null;

        try
        {
            // We need to load the thin client jdbc driver.
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            // e.printStackTrace();
            throw e;
        }

        result = DriverManager.getConnection (
            m_connectData.getConnectString(),
            m_connectData.m_user, m_connectData.m_password);

        result.setAutoCommit(false);

        return result;
    }

    static private String readStringFromClob(CLOB p_clob)
        throws IOException, SQLException
    {
        String result = "";

        if (p_clob != null)
        {
            Reader r = p_clob.getCharacterStream();
            StringBuffer sb = new StringBuffer();
            int charsRead = 0;
            char[] buffer = new char[p_clob.getChunkSize()];
            while ((charsRead = r.read(buffer)) != EOF)
            {
                sb.append(buffer, 0, charsRead);
            }
            r.close();
            result = sb.toString();
        }

        return result;
    }

    static private String printIds(ArrayList p_ids)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < p_ids.size(); i++)
        {
            Long id = (Long)p_ids.get(i);

            result.append(id);

            if (i < p_ids.size() - 1)
            {
                result.append(",");
            }
        }

        return result.toString();
    }
}

// jar -cmf Manifest CleanTM.jar CleanTM*.class

// Local Variables:
// compile-command: "javac CleanTM.java"
// End:
