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
package com.globalsight.ling.util;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.CRC32;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * <p>
 * A tool class that upgrades GlobalSight V2.x EXACTTM and ITEMS tables to
 * System 3 format.
 * </p>
 *
 * <p>
 * <strong>Notes:</strong>
 * </p>
 * <ul>
 * <li>segments are not segmented again (safe, they are too short)</li>
 * <li>segments are not combined (impossible due to insufficient data)</li>
 * <li>the &lt;it&gt; to skeleton is an optimization of the html extractor that
 * is in the wrong place. It should be a final cleanup after segmenting and word
 * counting.</li>
 * <li>we do mimic the extractor's algorithm and perform isolated tag
 * optimization</li>
 * </ul>
 *
 * <p>
 * Open issues
 * </p>
 * <ul>
 * <li>Target language pairs are not checked and INSERT may fail</li>
 * <li>Target INSERT may fail when segments are too long</li>
 * <li>Upgrading an already upgraded table is not checked</li>
 * <li></li>
 * </ul>
 *
 * <p>
 * Usage: <code>java com.globalsight.ling.util.Migrate
 *  -k jdbc:oracle:thin:@localhost:1521:nils -u nils -p sekret</code>
 * </p>
 */
public class Migrate
{
    /**
     * <p>
     * Local class to store pairs of source and target locales in a hashtable
     * that is indexed by a pair id.
     * </p>
     */
    private class LocalePair
    {
        public String source;
        public String target;

        public LocalePair(String source, String target)
        {
            this.source = source;
            this.target = target;
        }
    }

    //
    // Private Constants
    //

    private final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    //
    // Private Members
    //

    /** Debug flag. */
    private boolean m_trace = false;

    private String m_strConnect;
    private String m_strConnect2;
    private String m_strUser;
    private String m_strUser2;
    private String m_strPasswd;
    private String m_strPasswd2;

    private String m_strOldTable;
    private String m_strNewTable;
    private boolean m_bCreateNewTable = false;

    // these are really translation unit counts
    private int m_iSkippedSegments = 0;
    private int m_iUpgradedSegments = 0;

    private RE m_RERemoveLeadingWhite;
    private RE m_RERemoveTrailingWhite;
    private RE m_RERemoveSubTags;
    private RE m_RERemoveTags;
    private RE m_RERemoveWhite;
    private RE m_RERemoveBrackets;

    // helper objects
    private CRC32 m_crc = new CRC32();
    private XmlEntities m_codec = new XmlEntities();
    private Hashtable m_localePairs = new Hashtable();

    // The connections to the Oracle databases.
    private Connection m_connection;
    private Connection m_connection2;

    //
    // Constructors
    //

    public Migrate(String connect, String user, String passwd, String connect2,
            String user2, String passwd2, String oldTableName,
            String newTableName, boolean createNewTable, boolean trace)
            throws Exception
    {
        m_strConnect = connect;
        m_strConnect2 = connect2;
        m_strUser = user;
        m_strUser2 = user2;
        m_strPasswd = passwd;
        m_strPasswd2 = passwd2;

        m_strOldTable = oldTableName;
        m_strNewTable = newTableName;
        m_bCreateNewTable = createNewTable;

        m_trace = trace;

        try
        {
            m_RERemoveLeadingWhite = new RE("^[:space:]+", RE.MATCH_NORMAL);
            m_RERemoveTrailingWhite = new RE("[:space:]+$", RE.MATCH_NORMAL);
            m_RERemoveSubTags = new RE(
                    "<sub[^>]+>(.|[:space:])*?</sub[:space:]*>",
                    RE.MATCH_NORMAL);

            m_RERemoveTags = new RE(
                    "<(bpt|ept|it|ph|ut)[^>]+>(.|[:space:])*?</(\\1)[:space:]*>",
                    RE.MATCH_NORMAL);

            m_RERemoveWhite = new RE("[:space:]+|&nbsp;?", RE.MATCH_NORMAL);
            m_RERemoveBrackets = new RE("\\[%%\\d+\\]", RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e)
        {
            System.err.println(e.toString());
            System.exit(1);
        }

        m_connection = getDatabaseConnection(m_strConnect, m_strUser,
                m_strPasswd);
        m_connection2 = getDatabaseConnection(m_strConnect2, m_strUser2,
                m_strPasswd2);

        fetchLanguageIds();
    }

    //
    // Public Methods
    //

    public void done()
    {
        if (m_connection != null)
        {
            try
            {
                m_connection.close();
            }
            catch (Exception ex)
            {
                // oh shut up
            }

            m_connection = null;
        }

        if (m_connection2 != null)
        {
            try
            {
                m_connection2.close();
            }
            catch (Exception ex)
            {
                // oh shut up
            }

            m_connection2 = null;
        }
    }

    public void migrateExactTmTable() throws Exception
    {
        if (m_bCreateNewTable)
        {
            createExactTmV3Table();
        }

        upgradeExactTmTable();

        System.err.println(m_iUpgradedSegments + " segments upgraded, "
                + m_iSkippedSegments + " segments dropped.");
    }

    //
    // Private Methods
    //

    private void createExactTmV3Table() throws Exception
    {
        Statement stmt = null;
        ResultSet rs = null;
        String str_sql;

        try
        {
            // read only, forward only
            stmt = m_connection2.createStatement();

            try
            {
                str_sql = "DROP TABLE " + m_strNewTable;
                rs = stmt.executeQuery(str_sql);
            }
            catch (SQLException ex)
            {
                // ignore
            }
            finally
            {
                if (rs != null)
                {
                    rs.close();
                }
            }

            str_sql = "CREATE TABLE " + m_strNewTable + " ("
                    + " SOURCETEXT VARCHAR(4000) NOT NULL,"
                    + " TRANSTEXT VARCHAR(4000) NOT NULL,"
                    + " PAIRID NUMBER(10,0) NOT NULL,"
                    + " SOURCECRC NUMBER(10,0)" +
                    // ", FOREIGN KEY (PAIRID) REFERENCES \"LANGPAIRS\"(PAIRID)"
                    // +
                    " )";
            rs = stmt.executeQuery(str_sql);
            rs.close();
        }
        catch (SQLException ex)
        {
            System.err.println("Error: can't create table" + m_strNewTable);
            throw ex;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
            if (stmt != null)
            {
                stmt.close();
            }
        }
    }

    private void clearExactTmV3Table() throws Exception
    {
        Statement stmt = null;
        ResultSet rs = null;
        String str_sql;

        try
        {
            stmt = m_connection2.createStatement();

            try
            {
                str_sql = "DELETE FROM TABLE " + m_strNewTable;
                rs = stmt.executeQuery(str_sql);
            }
            catch (SQLException ex)
            {
                // ignore
            }
        }
        catch (SQLException ex)
        {
            System.err.println("Error: can't clear table" + m_strNewTable);
            throw ex;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
            if (stmt != null)
            {
                stmt.close();
            }
        }
    }

    private void upgradeExactTmTable() throws Exception
    {
        String str_pairId, str_crc;
        String str_sourceSegment, str_targetSegment;
        String str_newSourceSegment, str_newTargetSegment;
        String str_sourceLocale, str_targetLocale;

        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String str_sql;

        try
        {
            // TARGET DB
            pstmt = m_connection2.prepareStatement("insert into "
                    + m_strNewTable
                    + " (PAIRID, SOURCECRC, SOURCETEXT, TRANSTEXT)"
                    + " values (?, ?, ?, ?)");

            // SOURCE DB - read only, forward only recordset only
            stmt = m_connection.createStatement();

            // not suported by thin driver and oci is broken on my machine

            // scrollable, isolated, updatable - but not supported by oracle
            // query = m_connection.createStatement(
            // ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // updatable, forward-only - not supported
            // query = m_connection.createStatement(
            // ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

            // updatable, forward-only - not supported
            // query = m_connection.createStatement(
            // ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            str_sql = "select PAIRID, SOURCECRC, SOURCETEXT, TRANSTEXT from "
                    + m_strOldTable;

            rs = stmt.executeQuery(str_sql);

            // for each row in EXACTTM
            while (rs.next())
            {
                // fetch source and target segments, language pair, (crc)
                str_pairId = rs.getString(1);
                str_crc = rs.getString(2);
                str_sourceSegment = rs.getString(3);
                str_targetSegment = rs.getString(4);

                // fetch locales for source and target
                try
                {
                    str_sourceLocale = getSourceLocale(str_pairId);
                    str_targetLocale = getTargetLocale(str_pairId);
                }
                catch (Exception ex)
                {
                    // missing data in lang tables, skip TU
                    System.err.println("Unknown languages for langpair "
                            + str_pairId + ", ignoring translation unit.");
                    continue;
                }

                if (m_trace)
                {
                    System.out.println("Source Segment (" + str_sourceLocale
                            + "): " + str_sourceSegment);
                }

                // extract SOURCETEXT using sourcetext's locale
                str_newSourceSegment = upgradeSegment(str_sourceSegment,
                        str_sourceLocale);

                if (m_trace)
                {
                    System.out.println("New Source segment: "
                            + str_newSourceSegment + "\n");
                }

                if (str_newSourceSegment == null
                        || str_newSourceSegment.length() == 0)
                {
                    // not a valid, reusable segment
                    ++m_iSkippedSegments;
                    continue;
                }

                if (m_trace)
                {
                    System.out.println("Target Segment (" + str_targetLocale
                            + "): " + str_targetSegment);
                }

                // extract TRANSTEXT using transtext's locale
                str_newTargetSegment = upgradeSegment(str_targetSegment,
                        str_targetLocale);

                if (m_trace)
                {
                    System.out.println("New Target segment: "
                            + str_newTargetSegment + "\n");
                }

                if (str_newTargetSegment == null
                        || str_newTargetSegment.length() == 0)
                {
                    // not a valid, reusable segment
                    ++m_iSkippedSegments;
                    continue;
                }

                // compute new source crc on text-only part
                String str_crctext = removeTMXTags(str_newSourceSegment);
                long l_crc = computeCRC(str_crctext);

                if (m_trace)
                {
                    System.out.println("CRC=" + l_crc + " for " + str_crctext
                            + "\n");
                }

                try
                {
                    // set new SOURCETEXT = extracted SOURCETEXT
                    // set new TRANSTEXT = extracted TRANSTEXT
                    pstmt.setString(1, str_pairId);
                    pstmt.setString(2, Long.toString(l_crc));
                    pstmt.setString(3, str_newSourceSegment);
                    pstmt.setString(4, str_newTargetSegment);

                    // update the row
                    pstmt.execute();

                    ++m_iUpgradedSegments;
                }
                catch (SQLException ex)
                {
                    // segment is probably longer than 4000 chars, skip
                    System.err.println("Error: insert into target table "
                            + m_strNewTable + " failed.");

                    ++m_iSkippedSegments;
                }
            }
            // close rs and stmt and pstmt in finally clause
        }
        catch (SQLException ex)
        {
            System.err.println("Error: can't upgrade table " + m_strOldTable);
            throw ex;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
            if (stmt != null)
            {
                stmt.close();
            }
            if (pstmt != null)
            {
                pstmt.close();
            }
        }
    }

    /**
     * <p>
     * Runs the Diplomat HTML Extractor on the segment and returns it as
     * DiplomatXML. Returns null when the segment does not contain valid data.
     * </p>
     *
     * <p>
     * Assumption: all data in the tables are in HTML format.
     * </p>
     *
     * <p>
     * Differences between V2 and V3 segmentation are tried to be handled
     * gracefully.
     * </p>
     */
    private String upgradeSegment(String segment, String locale)
    {
        String str_result = null;

        try
        {
            // TODO
            Locale o_locale = getLocale(locale);

            // Initialize an HTML Extractor
            EFInputData input = new EFInputData();
            input.setUnicodeInput(segment);
            input.setLocale(o_locale);

            Output output = new Output();
            Extractor extractor = new Extractor();
            extractor.init(input, output);
            extractor.loadRules();
            extractor.extract();

            // Copy elements into a vector
            Vector v = new Vector();
            Iterator it = output.documentElementIterator();
            while (it.hasNext())
            {
                v.add(it.next());
            }

            // Remove trailing skeletal isolated tags.
            while (v.size() > 0)
            {
                DocumentElement de = (DocumentElement) v.lastElement();

                if (de.type() == DocumentElement.SKELETON)
                {
                    v.removeElementAt(v.size() - 1);
                    // System.out.println("Removing end skeleton " +
                    // ((SkeletonElement)de).getSkeleton());
                }
                else
                {
                    break;
                }
            }

            // Remove leading skeletal isolated tags.
            // Leading isolated font tags may be followed by a
            // localizable element and more skeleton (but should not
            // be followed by a translatable)
            boolean b_prevWasSkeleton = false;
            while (v.size() > 0)
            {
                DocumentElement de = (DocumentElement) v.firstElement();

                if (de.type() == DocumentElement.SKELETON)
                {
                    v.removeElementAt(0);
                    b_prevWasSkeleton = true;
                    // System.out.println("Removing start skeleton " +
                    // ((SkeletonElement)de).getSkeleton());
                }
                else if (de.type() == DocumentElement.LOCALIZABLE
                        && b_prevWasSkeleton)
                {
                    v.removeElementAt(0);
                    b_prevWasSkeleton = false;
                    // System.out.println("Removing localizable " +
                    // ((LocalizableElement)de).getChunk());
                }
                else
                {
                    break;
                }
            }

            // Bail out if everything has been removed
            if (v.size() == 0)
            {
                return null;
            }

            // Collect the result back into a single string
            StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < v.size(); ++i)
            {
                DocumentElement de = (DocumentElement) v.elementAt(i);
                String chunk;

                // TODO: unencode entities if necessary
                switch (de.type())
                {
                    case DocumentElement.SKELETON:
                        buffer.append(((SkeletonElement) de).getSkeleton());
                        break;
                    case DocumentElement.TRANSLATABLE:
                        buffer.append(((TranslatableElement) de).getChunk());
                        break;
                    case DocumentElement.LOCALIZABLE:
                        buffer.append(((LocalizableElement) de).getChunk());
                        break;
                    default:
                        // skip all others
                        break;
                }
            }

            str_result = buffer.toString();
        }
        catch (ExtractorException ex)
        {
            // ignore this segment
            System.err.println("Warning: ignoring unextractable segment: `"
                    + segment + "'");
            return null;
        }

        return str_result;
    }

    /**
     * Connects to an Oracle database and returns a Java Connection object.
     */
    private Connection getDatabaseConnection(String url, String user,
            String passwd) throws Exception
    {
        Connection o_connection = null;

        if (m_trace)
        {
            System.out.println("Connecting to " + m_strConnect + " as "
                    + m_strUser);
        }

        try
        {
            // We need to preload the thin client jdbc driver
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException ex)
        {
            System.err.println("Error: please add the Oracle JDBC library "
                    + "(O816Classes12.zip) to your CLASSPATH.");
            throw ex;
        }

        o_connection = DriverManager.getConnection(url, user, passwd);

        if (m_trace)
        {
            // Create Oracle DatabaseMetaData object
            DatabaseMetaData meta = o_connection.getMetaData();
            System.out.println("Connected.  JDBC driver is "
                    + meta.getDriverName() + " " + meta.getDriverVersion()
                    + ".");
        }

        return o_connection;
    }

    /**
     * <p>
     * Reads the language pairs from the database's LANGPAIRS and LANGS tables
     * and stores them in an internal hash table.
     * </p>
     */
    private void fetchLanguageIds() throws Exception
    {
        String str_pairId, str_sourceLocale, str_targetLocale;
        String str_sql = "select PAIRID, s.NAME, t.NAME from LANGPAIRS, LANGS s, LANGS t "
                + "where LANGPAIRS.SOURCELANG=s.LANG_ID "
                + "  and LANGPAIRS.TARGETLANG=t.LANG_ID";

        try
        {
            Statement query = m_connection.createStatement();
            ResultSet rs = query.executeQuery(str_sql);

            while (rs.next())
            {
                str_pairId = rs.getString(1);
                str_sourceLocale = rs.getString(2);
                str_targetLocale = rs.getString(3);

                m_localePairs.put(str_pairId, new LocalePair(str_sourceLocale,
                        str_targetLocale));
            }

            rs.close();
        }
        catch (SQLException ex)
        {
            System.err.println("Error: can't read language ids from database.");
            throw ex;
        }
    }

    /**
     * Returns the source locale from a translation unit's pairid.
     */
    private String getSourceLocale(String pairId) throws Exception
    {
        LocalePair o_pair = (LocalePair) m_localePairs.get(pairId);
        return o_pair.source;
    }

    /**
     * Returns the target locale from a translation unit's pairid.
     */
    private String getTargetLocale(String pairId) throws Exception
    {
        LocalePair o_pair = (LocalePair) m_localePairs.get(pairId);
        return o_pair.target;
    }

    /**
     * <p>
     * Mirrors Perl's CRC computation routine (32-bit only); not implemented
     * yet.
     * </p>
     */
    private long computeCRC(String string)
    {
        m_crc.reset();

        try
        {
            // CRC is computed by perl on UTF-8 strings
            m_crc.update(string.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
            System.err.println("Error: unknown encoding UTF-8 ????");
            return 0;
        }

        return m_crc.getValue();
    }

    /**
     * <p>
     * Removes all TMX tags and TMX content from a string and performs the same
     * substitutions the Perl code in TMUtil.pm does before computing a CRC
     * value.
     * </p>
     */
    private String removeTMXTags(String segment)
    {
        String str_result = segment;

        // chop off leading and trailing white space (^\s+ and \s+$)
        str_result = m_RERemoveLeadingWhite.subst(str_result, "",
                RE.REPLACE_ALL);
        str_result = m_RERemoveTrailingWhite.subst(str_result, "",
                RE.REPLACE_ALL);

        // Extract text parts by removing sub tags...
        str_result = m_RERemoveSubTags.subst(str_result, "", RE.REPLACE_ALL);

        // ... and all other TMX tags
        str_result = m_RERemoveTags.subst(str_result, "", RE.REPLACE_ALL);

        // entities are escaped, must decode (must not decode twice)
        str_result = m_codec.decodeStringBasic(str_result);

        // Remove multiple whitespace \s+ and "&nbsp;?"
        str_result = m_RERemoveWhite.subst(str_result, "", RE.REPLACE_ALL);

        // Delete "[%%\d+] (should not be there, but anyway)
        str_result = m_RERemoveBrackets.subst(str_result, "", RE.REPLACE_ALL);

        return str_result;
    }

    /**
     * Builds a locale object from a locale string
     */
    private Locale getLocale(String locale)
    {
        Locale res = null;

        if (locale.length() == 2) // must be language only
        {
            res = new Locale(locale, "");
        }
        else if (locale.length() == 5) // language plus country
        {
            res = new Locale(locale.substring(0, 2), locale.substring(3, 5));
        }
        else
        {
            // handle variants - use default locale for now
            res = Locale.US;
        }

        return res;
    }

    //
    // Main Routine
    //

    public static void main(String argv[])
    {
        String str_connect = null;
        String str_connect2 = null;
        String str_user = null;
        String str_user2 = null;
        String str_passwd = null;
        String str_passwd2 = null;
        String str_oldTable = "EXACTTM_2x";
        String str_newTable = "EXACTTM";
        boolean b_createTable = false;
        boolean b_trace = false;

        Migrate migrater = null;

        try
        {
            Arguments getopt = new Arguments();
            int c;

            getopt.setUsage(new String[]
            {
                    "Usage: java com.globalsight.ling.util.Migrate [-d] -j connect -u user -p passwd [-k connect] [-v user] [-q passwd] [-f fromtable] [-t totable]",
                    "",
                    "Upgrades an GlobalSight V2.x EXACTTM table to System 3 format.",
                    "",
                    "The source and target tables are assumed to exist, as well as properly",
                    "initialized LANG and LANGPAIRS tables in the target database.",
                    "",
                    "  -h: show this help.",
                    "  -j connect: the connect string for the SOURCE database",
                    "  -k connect: the connect string for the TARGET database",
                    "              (default: same as -j).",
                    "              Connect strings follow Oracle's thin JDBC driver syntax,",
                    "              i.e. jdbc:oracle:thin:@<HOSTNAME>:<PORT>:<INSTANCE>.",
                    "  -u user:    the user as which to connect to the SOURCE database.",
                    "  -v user:    the user as which to connect to the TARGET database.",
                    "              (default: same as -u).",
                    "  -p passwd:  the password for connecting to the source database.",
                    "  -q passwd:  the password for connecting to the target database",
                    "              (default: same as -p).",
                    "  -f from:    the old table to upgrade  (default: EXACTTM_2X).",
                    "  -t to:      the new table to populate (default: EXACTTM).",
                    // "  -c: create the new table.", // for debugging only
                    "  -d: enables debug trace to stdout." });

            getopt.parseArgumentTokens(argv, new char[]
            { 'f', 'j', 'k', 'p', 'q', 't', 'u', 'v' });

            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'c':
                    case 'C':
                        b_createTable = true;
                        break;
                    case 'd':
                    case 'D':
                        b_trace = true;
                        break;
                    case 'f':
                    case 'F':
                        str_oldTable = getopt.getStringParameter();
                        break;
                    case 'j':
                    case 'J':
                        str_connect = getopt.getStringParameter();
                        break;
                    case 'k':
                    case 'K':
                        str_connect2 = getopt.getStringParameter();
                        break;
                    case 'p':
                    case 'P':
                        str_passwd = getopt.getStringParameter();
                        break;
                    case 'q':
                    case 'Q':
                        str_passwd2 = getopt.getStringParameter();
                        break;
                    case 't':
                    case 'T':
                        str_newTable = getopt.getStringParameter();
                        break;
                    case 'u':
                    case 'U':
                        str_user = getopt.getStringParameter();
                        break;
                    case 'v':
                    case 'V':
                        str_user2 = getopt.getStringParameter();
                        break;
                    case 'h':
                    case 'H':
                    case '?':
                    default:
                        getopt.printUsage();
                        System.exit(1);
                        break;
                }
            }

            // check required args
            if (str_connect == null || str_user == null || str_passwd == null
                    || str_oldTable == null || str_newTable == null)
            {
                getopt.printUsage();
                System.exit(1);
            }

            // fulfil default value promise
            if (str_connect2 == null)
                str_connect2 = str_connect;
            if (str_user2 == null)
                str_user2 = str_user;
            if (str_passwd2 == null)
                str_passwd2 = str_passwd;

            // sanity check (driving the author insane)
            if (str_connect.equalsIgnoreCase(str_connect2)
                    && str_user.equalsIgnoreCase(str_user2)
                    && str_passwd.equalsIgnoreCase(str_passwd2))
            {
                // same instance, cannot have same table name
                if (str_oldTable.equalsIgnoreCase(str_newTable))
                {
                    System.err
                            .println("Old and new table must have different names");
                    System.exit(1);
                }
            }

            migrater = new Migrate(str_connect, str_user, str_passwd,
                    str_connect2, str_user2, str_passwd2, str_oldTable,
                    str_newTable, b_createTable, b_trace);

            migrater.migrateExactTmTable();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        finally
        {
            if (migrater != null)
            {
                migrater.done();
            }
        }

        System.exit(0);
    }
}
