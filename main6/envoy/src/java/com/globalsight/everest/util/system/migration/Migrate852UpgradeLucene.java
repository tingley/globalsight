package com.globalsight.everest.util.system.migration;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.indexer.TmSegmentIndexer;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.Tm2Reindexer;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentResultSet;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.terminology.ITermbaseImpl;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Upgrade Lucene index for [#GBS-3166] GlobalSight Improvement Idea 12: Upgrade
 * Apache Lucene
 * 
 * @author Wayne
 * 
 */
public class Migrate852UpgradeLucene extends MigrateObj
{
    private static String keynameTb = "doGBS3166LuceneUpgradeTbMigration";
    private static String keynameTm2 = "doGBS3166LuceneUpgradeTm2Migration";
    private static String keynameTm3 = "doGBS3166LuceneUpgradeTm3Migration";

    @Override
    public boolean checkIfDoMigration()
    {
        boolean doTb = checkIfDoMigration(keynameTb);
        boolean doTm2 = checkIfDoMigration(keynameTm2);
        boolean doTm3 = checkIfDoMigration(keynameTm3);

        return (doTb | doTm2 | doTm3);
    }

    @Override
    public void doMigration() throws Exception
    {
        migrate();
        /*
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                migrate();
            }
        };

        t.start();
        */
    }
    
    private void migrate()
    {
        Collection<Company> companies = null;

        try
        {
            companies = ServerProxy.getJobHandler().getAllCompanies();

        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when re-index TM ", e);
        }

        List<Long> companyIds = new ArrayList<Long>();
        if (companies != null)
        {
            for (Company c : companies)
            {
                companyIds.add(c.getId());
            }
        }

        try
        {
            // 1 re-index all termbase
            if (checkIfDoMigration(keynameTb))
            {
                CATEGORY.info("Start upgrading Lucene index for termbase");

                for (Long cid : companyIds)
                {
                    String cidS = cid.toString();
                    if (cidS.equals(CompanyWrapper.SUPER_COMPANY_ID))
                    {
                        continue;
                    }

                    CompanyThreadLocal.getInstance().setIdValue(cidS);
                    ArrayList allNames = TermbaseList.getNames();
                    String userid = "superAdmin";
                    if (allNames != null && allNames.size() > 0)
                    {
                        for (int i = 0; i < allNames.size(); i++)
                        {
                            String name = allNames.get(i).toString();
                            Termbase tb = TermbaseList.get(name);
                            SessionInfo session = new SessionInfo(
                                    userid, "guest");

                            ITermbaseImpl itb = new ITermbaseImpl(tb,
                                    session);
                            itb.getIndexer().doIndex();
                        }
                    }

                }

                CATEGORY.info("End upgrading Lucene index for termbase");
                updateMigrationKey(keynameTb);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when re-index termbase", e);
        }

        CompanyThreadLocal.getInstance().setIdValue(CompanyWrapper.SUPER_COMPANY_ID);
        Collection<ProjectTM> allTMs = null;

        try
        {
            allTMs = ServerProxy.getProjectHandler().getAllProjectTMs(
                    true);
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when re-index TM ", e);
        }

        try
        {
            // 2 re-index tm2
            if (checkIfDoMigration(keynameTm2))
            {
                CATEGORY.info("Start upgrading Lucene index for TM2");
                Collection<ProjectTM> tms = new ArrayList<ProjectTM>();

                if (allTMs != null)
                {
                    Iterator it = allTMs.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = (ProjectTM) it.next();
                        if (tm.getTm3Id() != null || tm.getIsRemoteTm())
                        {
                            continue;
                        }
                        tms.add(tm);
                    }
                }

                SystemResourceBundle srb = SystemResourceBundle
                        .getInstance();
                ResourceBundle rb = srb.getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                        Locale.getDefault());

                Reindexer reindexer = LingServerProxy
                        .getTmCoreManager().getReindexer(tms);
                reindexer.setResourceBundle(rb);

                for (ProjectTM tm : tms)
                {
                    CATEGORY.info("  TM2: " + tm.toString());
                    Tm2Reindexer tm2Reindexer = new Tm2Reindexer();
                    boolean indexTarget = TmSegmentIndexer
                            .indexesTargetSegments(tm.getId());
                    tm2Reindexer.reindexTm(tm, reindexer, indexTarget);
                }

                updateMigrationKey(keynameTm2);
                CATEGORY.info("End upgrading Lucene index for TM2");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when re-index TM2", e);
        }

        Connection conn = null;
        try
        {
            // 3 re-index tm3
            if (checkIfDoMigration(keynameTm3))
            {
                CATEGORY.info("Start upgrading Lucene index for TM3");
                Collection<ProjectTM> tms = new ArrayList<ProjectTM>();

                if (allTMs != null)
                {
                    Iterator it = allTMs.iterator();
                    while (it.hasNext())
                    {
                        ProjectTM tm = (ProjectTM) it.next();
                        if (tm.getTm3Id() == null || tm.getIsRemoteTm())
                        {
                            continue;
                        }
                        tms.add(tm);
                    }
                }

                conn = DbUtil.getConnection();
                for (ProjectTM tm : tms)
                {
                    CATEGORY.info("  TM3: " + tm.toString());
                    
                    long tmId = tm.getId();
                    Tm3SegmentTmInfo tm3SegInfo = (Tm3SegmentTmInfo) tm
                            .getSegmentTmInfo();
                    Tm3SegmentResultSet segments = (Tm3SegmentResultSet) tm3SegInfo
                            .getAllSegments(tm, null, null, conn);

                    // index per 1000 tu
                    int count = 0;
                    int max = 1000;
                    Collection<TM3Tu<GSTuvData>> tus = new ArrayList<TM3Tu<GSTuvData>>(
                            max);
                    while (segments.hasNext())
                    {
                        if (count == max)
                        {
                            tm3SegInfo.luceneIndexTus(tmId, tus);
                            count = 0;
                            tus.clear();
                        }

                        TM3Tu<GSTuvData> tu = segments.nextTm3tu();
                        tus.add(tu);
                        count++;
                    }

                    if (tus.size() > 0)
                    {
                        tm3SegInfo.luceneIndexTus(tmId, tus);
                    }
                }

                updateMigrationKey(keynameTm3);
                CATEGORY.info("End upgrading Lucene index for TM3");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when re-index TM2", e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }
}
