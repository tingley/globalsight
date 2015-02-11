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
package com.plug;

import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import com.plug.Version_7_1_6_0.DbServer;
import com.plug.Version_8_5_2.LuceneConstants;
import com.plug.Version_8_5_2.gs.ling.common.DiplomatBasicParser;
import com.plug.Version_8_5_2.gs.ling.common.Text;
import com.plug.Version_8_5_2.gs.ling.lucene.analysis.ngram.NgramAnalyzer;
import com.plug.Version_8_5_2.gs.ling.tm2.FuzzyIndexFormatHandler;
import com.plug.Version_8_5_2.gs.ling.tm2.lucene.GsAnalyzer;
import com.plug.Version_8_5_2.gs.ling.tm2.lucene.GsPerFieldAnalyzer;
import com.plug.Version_8_5_2.gs.ling.tm2.lucene.LuceneUtil;
import com.plug.Version_8_5_2.gs.ling.tm2.lucene.TuvDocument;
import com.plug.Version_8_5_2.gs.ling.tm3.core.Fingerprint;
import com.plug.Version_8_5_2.gs.ling.tm3.core.TM3TmType;
import com.plug.Version_8_5_2.gs.ling.tm3.core.Trigram;
import com.plug.Version_8_5_2.gs.terminology.Definition;
import com.plug.Version_8_5_2.gs.terminology.Definition.Index;
import com.plug.Version_8_5_2.gs.terminology.TermHandler;
import com.plug.Version_8_5_2.gs.terminology.Termbase;
import com.plug.Version_8_5_2.gs.terminology.TermbaseHandler;
import com.plug.Version_8_5_2.gs.tm.ProjectTM;
import com.plug.Version_8_5_2.gs.tm.ProjectTMHandler;
import com.plug.Version_8_5_2.gs.util.AmbFileStoragePathUtils;
import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;
import com.plug.Version_8_5_2.gs.util.GlobalSightLocaleHandler;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.FileUtil;
import com.util.ServerUtil;

public class Plug_8_5_2 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_8_5_2.class);
    private File fileStorageDir = null;
    private String m_directory;
    private Analyzer m_analyzer;
    private SimpleFSDirectory m_fsDir;

    // Start/end of segment boundary
    private static final Long BOUNDARY = -1l;
    private List<String> deletedTables = new ArrayList<String>();

    private UI ui = UIFactory.getUI();

    @Override
    public void run()
    {
        File f = new File(ServerUtil.getPath()
                + "/jboss/server/standalone/deployments/globalsight.ear/lib/lucene-1.4.2.jar");
        if (f.exists())
        {
            f.delete();
        }

        reindexLucene();
    }

    private void reindexLucene()
    {
        // init parameters
        fileStorageDir = AmbFileStoragePathUtils.getFileStorageDir();
        if (!fileStorageDir.exists() || !fileStorageDir.isDirectory())
        {
            String msg = "Cannot get right file storage dir : " + fileStorageDir.toString();
            log.error(msg);
            ui.error(msg);
        }

        DbServer dbs = new DbServer();
        HashMap<String, String> companys = new HashMap<String, String>();
        try
        {
            List<Long> companyIds = dbs.getAllCompanyIds();
            List<String> companyNames = dbs.getAllCompanyNames();

            for (int i = 0; i < companyIds.size(); i++)
            {
                companys.put(companyIds.get(i).toString(), companyNames.get(i));
            }
        }
        catch (Exception ex)
        {
            String msg = "Cannot get company information from Database: ";
            log.error(msg, ex);
            ui.error(msg + ex.toString());
        }

        try
        {
            ui.addProgress(0, "Reindex Termbases");
            reindexTermbase(dbs, companys);
        }
        catch (Exception ex)
        {
            String msg = "Cannot reindex Termbase: ";
            log.error(msg, ex);
            ui.error(msg + ex.toString());
        }

        try
        {
            ui.addProgress(0, "Reindex Project TMs");
            reindexTM(dbs, companys);
        }
        catch (Exception ex)
        {
            String msg = "Cannot reindex project TM: ";
            log.error(msg, ex);
            ui.error(msg + ex.toString());
        }
    }

    private void showMsg(String companyName, String targetName, boolean isTM)
    {
        String msg = "Reindex " + (isTM ? "Project TM: " : "Termbase: ") + targetName
                + " , Company: " + companyName;
        ui.addProgress(0, msg);

        log.info(msg);
    }

    private void logDeleteFile(String path)
    {
        log.info("Deleting old index: " + path);
    }

    private void logAlreadyIndex(String name)
    {
        log.info("Do not create index for " + name + " because of it is reindexed.");
    }

    private void reindexTermbase(DbServer dbServer, HashMap<String, String> companys)
            throws Exception
    {
        log.info("Start upgrading Lucene index for termbase");

        TermbaseHandler h = new TermbaseHandler();
        List<Termbase> tbs = dbServer.getDbUtil().query(TermbaseHandler.SQL, h);
        m_analyzer = new NgramAnalyzer(3);

        for (Termbase tb : tbs)
        {
            if (tb.getCOMPANYID().equals(LuceneConstants.SUPER_COMPANY_ID))
            {
                continue;
            }

            String cname = companys.get(tb.getCOMPANYID());
            File termDir = new File(fileStorageDir, cname + "/TB-" + tb.getTB_NAME());
            // check re-indexed
            if (isIndexedBefore(termDir, tb.getTB_NAME()))
            {
                logAlreadyIndex(tb.getTB_NAME());
                continue;
            }

            showMsg(cname, tb.getTB_NAME(), false);

            // 1 delete old term base indexes
            logDeleteFile(termDir.getAbsolutePath());
            deleteFile(termDir.getAbsolutePath());
            // 2 create new empty dir
            termDir.mkdirs();

            Definition dif = new Definition(tb.getTB_DEFINITION());
            List<Index> indexs = dif.getIndexes();

            for (Index index : indexs)
            {
                // 3 write index into ram
                RAMDirectory ramdir = new RAMDirectory();
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, m_analyzer);
                config.setOpenMode(OpenMode.CREATE_OR_APPEND);
                IndexWriter ramIndexWriter = new IndexWriter(ramdir, config);

                if (index != null && "fuzzy".equalsIgnoreCase(index.getType()))
                {
                    String folder = index.getLanguageName() + "-" + index.getLocale() + "-TERM";
                    File indexFolder = new File(termDir, folder);
                    m_directory = indexFolder.getAbsolutePath();
                    m_fsDir = new SimpleFSDirectory(indexFolder);

                    String sql = TermHandler.generateSQL(tb.getTBID(), index.getLanguageName());
                    TermHandler termH = new TermHandler();
                    List<Document> docs = dbServer.getDbUtil().query(sql, termH);
                    for (Document doc : docs)
                    {
                        ramIndexWriter.addDocument(doc);
                        ramIndexWriter.commit();
                    }

                    // 4 write index from ram into disk
                    IndexWriter diskwriter = getIndexWriter(true);
                    diskwriter.commit();
                    if (docs != null && docs.size() > 0)
                    {
                        Directory[] ds = new Directory[] { ramdir };
                        diskwriter.addIndexes(ds);
                        diskwriter.commit();
                    }

                    // 5 close index writer
                    IOUtils.closeWhileHandlingException(ramIndexWriter);
                    IOUtils.closeWhileHandlingException(diskwriter);

                    ramIndexWriter = null;
                    ramdir = null;
                }

            }

            writeTagFile(termDir, tb.getTB_NAME());
        }

        log.info("End upgrading Lucene index for termbase");
    }

    private String tagFileName = "GS_8.5.2_README.txt";

    private boolean isIndexedBefore(File parent, String name)
    {
        if (isEmptyIndex(parent))
        {
            return false;
        }

        File tf = new File(parent, tagFileName);
        if (tf.exists())
        {
            return true;
        }

        File[] fs = parent.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });

        
        GlobalSightLocale locale = new GlobalSightLocale("zh", "CN", false);
        if (fs != null && fs.length > 0)
        {
            for (File f : fs)
            {
                IndexWriter indexWriter = null;
                try
                {
                    FSDirectory directory = FSDirectory.open(f);
                    IndexWriterConfig conf = new IndexWriterConfig(LuceneUtil.VERSION,
                            new GsAnalyzer(locale));
                    conf.setOpenMode(OpenMode.CREATE);

                    indexWriter = new IndexWriter(directory, conf);
                }
                catch (IndexFormatTooOldException ie)
                {
                    // need to re-index
                    return false;
                }
                catch (Exception e)
                {
                    // ignore
                }
                finally
                {
                    if (indexWriter != null)
                    {
                        IOUtils.closeWhileHandlingException(indexWriter);
                    }
                }
            }

            writeTagFile(parent, name);
            return true;
        }
        else
        {
            return false;
        }

    }

    private boolean isEmptyIndex(File parent)
    {
        File enUS = new File(parent, "en_US");
        
        if (!enUS.exists())
        {
            return true;
        }
        
        File[] files = enUS.listFiles(new FileFilter()
        {
            
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isFile();
            }
        });
        
        if (files == null || files.length == 0)
        {
            return true;
        }
        
        for (File file : files)
        {
            if (file.getName().endsWith(".si")
                    || file.getName().endsWith(".cfs")
                    || file.getName().endsWith(".cfe"))
            {
                return false;
            }
        }
        
        return true;
    }

    private void writeTagFile(File parent, String name)
    {
        File tf = new File(parent, tagFileName);

        if (!tf.exists())
        {
            try
            {
                tf.createNewFile();
            }
            catch (IOException e)
            {
                log.error("Cannot create tag file: " + tf, e);
            }

            try
            {
                FileWriter fw = new FileWriter(tf);
                fw.write(name + " re-indexed.");
                fw.close();
            }
            catch (Exception ex)
            {
            }
        }
    }

    private IndexWriter getIndexWriter(boolean p_create) throws IOException
    {
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_44, m_analyzer);
        OpenMode om = p_create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND;
        conf.setOpenMode(om);

        IndexWriter result = null;
        boolean deleteFiles = false;
        try
        {
            result = new IndexWriter(m_fsDir, conf);
        }
        catch (EOFException eofe)
        {
            deleteFiles = true;
        }
        catch (IndexFormatTooOldException ie)
        {
            deleteFiles = true;
        }

        if (deleteFiles)
        {
            deleteFile(m_directory);

            result = new IndexWriter(m_fsDir, conf);
        }

        return result;
    }

    private void deleteFile(String path)
    {
        File indexDir = new File(path);
        if (!indexDir.exists())
        {
            indexDir.mkdirs();
        }

        // delete too old index
        File[] files = indexDir.listFiles();
        if (files != null && files.length > 0)
        {
            for (int i = 0; i < files.length; i++)
            {
                File oneFile = files[i];
                if (oneFile.isFile())
                {
                    deleteOneFile(oneFile);
                }
                else if (oneFile.isDirectory())
                {
                    deleteFile(oneFile.getAbsolutePath());
                    deleteOneFile(oneFile);
                }
            }
        }
    }

    private void deleteOneFile(File oneFile)
    {
        try
        {
            oneFile.delete();
        }
        catch (Exception eee)
        {
            // ignore but log it
            log.error("Cannot delete file : " + oneFile.toString(), eee);
        }
    }

    private void reindexTM(DbServer dbServer, HashMap<String, String> companys) throws Exception
    {
        log.info("Start upgrading Lucene index for project TM");

        ProjectTMHandler h = new ProjectTMHandler();
        List<ProjectTM> tms = dbServer.getDbUtil().query(ProjectTMHandler.SQL, h);
        GlobalSightLocaleHandler gh = new GlobalSightLocaleHandler();
        List<GlobalSightLocale> gslocales = dbServer.getDbUtil().query(
                GlobalSightLocaleHandler.SQL, gh);
        HashMap<String, GlobalSightLocale> locales = new HashMap<String, GlobalSightLocale>();
        for (GlobalSightLocale l : gslocales)
        {
            locales.put(l.getId() + "", l);
        }

        // GsAnalyzer gsa = new GsAnalyzer(p_locale);
        // m_analyzer = ;

        for (ProjectTM tm : tms)
        {
            boolean isTM3 = false;
            if (tm.getTM3_ID() != null && !"-1".equals(tm.getTM3_ID())
                    && !"0".equals(tm.getTM3_ID()))
            {
                isTM3 = true;
            }

            String cname = companys.get(tm.getCOMPANY_ID());
            File tmDir = new File(fileStorageDir, cname + "/GlobalSight/GoldTmIndex/" + tm.getID());

            // check re-indexed
            if (isIndexedBefore(tmDir, tm.getNAME()))
            {
                logAlreadyIndex(tm.getNAME());
                continue;
            }

            long tmId = Long.parseLong(tm.getID());
            showMsg(cname, tm.getNAME(), true);

            // 1 delete old tm indexes
            logDeleteFile(tmDir.getAbsolutePath());
            deleteFile(tmDir.getAbsolutePath());
            // 2 create new empty dir
            tmDir.mkdirs();

            // tm 2
            if (!isTM3)
            {
                // 3 write index
                String selectSQL = "SELECT tu.id tu_id, tu.format format, tu.type type, "
                        + "tuv.id tuv_id, tuv.segment_string segment_string, "
                        + "tuv.segment_clob segment_clob, tuv.locale_id locale_id, "
                        + "tu.source_locale_id source_locale_id "
                        + "from project_tm_tu_t tu, project_tm_tuv_t tuv "
                        + "where tu.id = tuv.tu_id and tu.tm_id = " + tm.getID()
                        + " and tuv.locale_id = tu.SOURCE_LOCALE_ID order by tuv.locale_id";

                queryAndIndexTmdata(dbServer, locales, tmDir, tmId, selectSQL, false, null, null,
                        null, 0);
            }
            // tm 3
            else
            {
                String tm3Id = tm.getTM3_ID();
                String tm3sql = "select id, type, tu_table, tuv_table, fuzzy_table, attr_val_table,"
                        + " srcLocaleId, tgtLocaleId, sharedStorageId from tm3_tm where id = "
                        + tm3Id;

                Connection conn = dbServer.getDbUtil().getConnection();

                ResultSet rs1 = null;
                Statement st1 = null;
                ResultSetMetaData rsmd1 = null;
                String tu_table = null;
                String tuv_table = null;
                String fuzzy_table = null;
                int tm3Type = 0;
                try
                {
                    st1 = conn.createStatement();
                    rs1 = st1.executeQuery(tm3sql);
                    rsmd1 = rs1.getMetaData();
                    int column = rsmd1.getColumnCount();
                    if (rs1.next())
                    {
                        tu_table = rs1.getString("tu_table");
                        tuv_table = rs1.getString("tuv_table");
                        fuzzy_table = rs1.getString("fuzzy_table");
                        tm3Type = rs1.getInt("type");
                    }
                }
                finally
                {
                    dbServer.getDbUtil().closeResultSet(rs1);
                    dbServer.getDbUtil().closeStatement(st1);
                }

                if (tu_table != null && tuv_table != null)
                {
                    // do not upgrade database finger print
                    fuzzy_table = null;
                    // clean fuzzy_table
                    /*
                     * if (fuzzy_table != null &&
                     * !deletedTables.contains(fuzzy_table)) { String deleteSql
                     * = "DELETE FROM " + fuzzy_table; Statement st2 = null; try
                     * { st2 = conn.createStatement(); st2.execute(deleteSql);
                     * deletedTables.add(fuzzy_table); } finally {
                     * dbServer.getDbUtil().closeStatement(st2); } }
                     */

                    // and tuv.localeId = tu.srcLocaleId
                    String selectSQL = "SELECT tu.id tu_id, tu.format format, tu.type type, "
                            + "tuv.id tuv_id, tuv.content segment_string, "
                            + "tuv.localeId locale_id, " + "tu.srcLocaleId source_locale_id "
                            + "from " + tu_table + " tu, " + tuv_table + " tuv "
                            + "where tu.id = tuv.tuId and tu.tmId = " + tm3Id
                            + " and tuv.localeId = tu.srcLocaleId order by tuv.localeId";

                    queryAndIndexTmdata(dbServer, locales, tmDir, tmId, selectSQL, true, tm3Id,
                            tuv_table, fuzzy_table, tm3Type);
                }
            }

            writeTagFile(tmDir, tm.getNAME());

        }
        log.info("End upgrading Lucene index for project TM");
    }

    private void queryAndIndexTmdata(DbServer dbServer, HashMap<String, GlobalSightLocale> locales,
            File tmDir, long tmId, String selectSQL, boolean isTM3, String tm3Id, String tuvTable,
            String fuzzyTable, int tm3Type) throws SQLException, IOException, Exception
    {
        log.info("SQL : " + selectSQL);
        Connection conn = dbServer.getDbUtil().getConnection();
        PreparedStatement preSta = conn.prepareStatement(selectSQL);
        ResultSet rs = preSta.executeQuery();
        IndexWriter diskwriter = null;
        m_analyzer = null;
        GlobalSightLocale lastLocale = null;
        try
        {
            while (rs.next())
            {
                boolean createNew = false;

                long locale_id = rs.getLong("locale_id");
                GlobalSightLocale locale = locales.get(locale_id + "");

                if (m_analyzer == null)
                {
                    createNew = true;
                }
                else if (!locale.equals(lastLocale))
                {
                    createNew = true;
                }

                if (createNew)
                {
                    m_analyzer = new GsAnalyzer(locale);
                    if (isTM3)
                    {
                        m_analyzer = new GsPerFieldAnalyzer(locale);
                    }

                    lastLocale = locale;

                    if (diskwriter != null)
                    {
                        diskwriter.commit();
                        IOUtils.closeWhileHandlingException(m_fsDir);
                        IOUtils.closeWhileHandlingException(diskwriter);
                    }

                    File indexFolder = new File(tmDir, locale.toString());
                    m_directory = indexFolder.getAbsolutePath();
                    m_fsDir = new SimpleFSDirectory(indexFolder);
                    diskwriter = getIndexWriter(true);

                    log.info("Create new IndexWriter for dir: " + m_directory);
                }

                long tuvId = rs.getLong("tuv_id");
                String segment = rs.getString("segment_string");
                // ignore segment_clob ?
                if(segment == null || segment.length() == 0)
                {
                    continue;
                }
                
                segment = LuceneUtil.normalizeTuvData(segment, locale);
                GlobalSightLocale srcLocale = locales.get(rs.getString("source_locale_id"));
                String type = rs.getString("type");
                String format = rs.getString("format");
                long tuId = rs.getLong("tu_id");
                boolean isSource = srcLocale.equals(locale);

                Set<String> targetLocales = null;
                if (isTM3)
                {
                    targetLocales = new HashSet<String>();
                    PreparedStatement psSelectLocales = null;
                    ResultSet rsSelectLocales = null;
                    try
                    {
                        String sql = "select localeId from " + tuvTable + " where tmId=" + tm3Id
                                + " and tuId=" + tuId + " and id<>" + tuvId;
                        psSelectLocales = conn.prepareStatement(sql);
                        rsSelectLocales = psSelectLocales.executeQuery();

                        while (rsSelectLocales.next())
                        {
                            long targetlocale_id = rsSelectLocales.getLong("localeId");
                            GlobalSightLocale targetlocale = locales.get(targetlocale_id + "");
                            targetLocales.add(targetlocale.toString());
                        }
                    }
                    finally
                    {
                        dbServer.getDbUtil().closeStatement(psSelectLocales);
                        dbServer.getDbUtil().closeResultSet(rsSelectLocales);
                    }
                }

                TuvDocument tuvdoc = new TuvDocument(segment, tuvId, tuId, tmId, isSource,
                        targetLocales, m_analyzer);
                Document doc = tuvdoc.getDocument();
                diskwriter.addDocument(doc);

                if (isTM3 && fuzzyTable != null)
                {
                    List<String> tokens = LuceneUtil.createTm3Tokens(segment, locale);
                    List<Long> fps = new ArrayList<Long>();
                    for (String tok : tokens)
                    {
                        fps.add(Fingerprint.fromString(tok));
                    }

                    List<Long> fingerprints = new ArrayList<Long>();
                    fingerprints.add(BOUNDARY);
                    for (Long tok : fps)
                    {
                        fingerprints.add(tok);
                    }
                    fingerprints.add(BOUNDARY);
                    List<Trigram> trigrams = new ArrayList<Trigram>();
                    for (int i = 0; i + 2 < fingerprints.size(); i++)
                    {
                        trigrams.add(new Trigram(fingerprints.get(i), fingerprints.get(i + 1),
                                fingerprints.get(i + 2)));
                    }

                    List<Long> tset = new ArrayList<Long>();
                    for (Trigram t : trigrams)
                    {
                        tset.add(t.getValue());
                    }

                    // add index into database
                    PreparedStatement ps2 = null;
                    try
                    {
                        List<String> keys = new ArrayList<String>();
                        String sql = null;
                        StringBuilder sb = new StringBuilder("INSERT INTO ").append(fuzzyTable);

                        if (tm3Type == TM3TmType.MULTILINGUAL_SHARED.getId())
                        {
                            sb.append(" (fingerprint, tuvId, tuId, localeId, tuvCount, isSource) ")
                                    .append("VALUES (?, ?, ?, ?, ?, ?)");
                            sql = sb.toString();

                            ps2 = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
                            int tuvCount = tset.size();
                            for (Long fp : tset)
                            {
                                String key = fp + "-" + tuvCount + "-" + locale_id + "-"
                                        + (isSource ? "1" : "0") + "-" + tuvId;

                                if (keys.contains(key))
                                {
                                    continue;
                                }

                                ps2.setObject(1, fp);
                                ps2.setObject(2, tuvId);
                                ps2.setObject(3, tuId);
                                ps2.setObject(4, locale_id);
                                ps2.setObject(5, tuvCount);
                                ps2.setObject(6, isSource);

                                ps2.addBatch();
                                keys.add(key);
                            }
                        }
                        else if (tm3Type == TM3TmType.BILINGUAL.getId())
                        {
                            sb.append(" (fingerprint, tuvId, tuId, tuvCount, isSource) ").append(
                                    "VALUES (?, ?, ?, ?, ?)");
                            sql = sb.toString();

                            ps2 = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
                            int tuvCount = tset.size();
                            for (Long fp : tset)
                            {
                                String key = fp + "-" + tuvCount + "-" + (isSource ? "1" : "0")
                                        + "-" + tuvId;

                                if (keys.contains(key))
                                {
                                    continue;
                                }

                                ps2.setObject(1, fp);
                                ps2.setObject(2, tuvId);
                                ps2.setObject(3, tuId);
                                ps2.setObject(4, tuvCount);
                                ps2.setObject(5, isSource);

                                ps2.addBatch();
                                keys.add(key);
                            }
                        }

                        if (ps2 != null)
                        {
                            ps2.executeBatch();
                        }
                    }
                    finally
                    {
                        dbServer.getDbUtil().closeStatement(ps2);
                    }
                }
            }
        }
        finally
        {
            if (diskwriter != null)
            {
                diskwriter.commit();
            }
            IOUtils.closeWhileHandlingException(m_fsDir);
            IOUtils.closeWhileHandlingException(diskwriter);

            dbServer.getDbUtil().closeConn(conn);
            dbServer.getDbUtil().closeStatement(preSta);
            dbServer.getDbUtil().closeResultSet(rs);
        }
    }
}
