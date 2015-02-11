package com.globalsight.ling.tm3.integration;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3SaveMode;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.segmenttm.EventType;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.util.progress.ProgressReporter;

public class Tm3Migrator {

    private Session session;
    private long companyId;
    private ProjectTM oldTm;
    private TM3Tm<GSTuvData> tm3tm; 
    private boolean userInterrupt = false;
        
    public Tm3Migrator(Session session, long companyId, ProjectTM oldTm) {
        this.session = session;
        this.companyId = companyId;
        this.oldTm = oldTm;
    }
    
    public interface TransactionControl {
        void commitAndRestartTransaction();
    }
    
    /**
     * Migrate the TM.  Because big TMs may take a long time
     * to migrate, this code will use multiple transactions to 
     * write out the new TM data.  Because of this, callers must
     * pass a TransactionControl implementation that will commit
     * the current transaction and start a new one.
     * 
     * @param progress ProgressReporter for status updates
     * @param control TransactionControl implementation
     * @return migrated TM, or NULL if the operation failed
     */
    @SuppressWarnings("unchecked")
    public ProjectTM migrate(ProgressReporter progress, 
                             TransactionControl control) {
        // Now make sure we can find a name for the new TM
        String newTmName = getUniqueTmName(session, oldTm.getName());
        if (newTmName == null) {
            progress.setMessageKey("lb_tm_migrate_tm3_failed", "Failed to migrate TM");
            return null;
        }
        
        TM3Manager manager = DefaultManager.create();
        SegmentResultSet segments = oldTm.getSegmentTmInfo()
            .getAllSegments(session, oldTm, null, null);

        // XXX It would be nice to be able to ensure that the shared storage for this
        // company actually existed first.
        tm3tm = manager.createMultilingualSharedTm(session, 
                new GSDataFactory(),
                SegmentTmAttribute.inlineAttributes(),
                companyId); 
        session.flush();

        // Create an event for this import
        // TODO: factor out segment save from TM3SegmentTmInfo
        EventMap events = new EventMap(tm3tm);
        TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
        TM3Attribute formatAttr = TM3Util.getAttr(tm3tm, FORMAT);
        TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
        TM3Attribute translatableAttr = TM3Util.getAttr(tm3tm, TRANSLATABLE);
        TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm, FROM_WORLDSERVER);
        
        // In order to calculate completion percentage, we need to see how
        // big the old TM was
        long totalCount = oldTm.getSegmentTmInfo().getAllSegmentsCount(session, 
                                                oldTm, null, null);
        
        progress.setMessageKey("lb_tm_migrate_tm3_converting", 
                "Got tm, migrating to tm3 id " + tm3tm.getId());
        long oldCount = 0, newCount = 0;
        TM3Saver<GSTuvData> saver = tm3tm.createSaver();

        // Create the project TM that points to the this TM. Do it now so
        // we have the id for the lucene
        ProjectTM tm = new ProjectTM();
        tm.setName(newTmName);
        tm.setDomain(oldTm.getDomain());
        tm.setOrganization(oldTm.getOrganization());
        tm.setDescription(oldTm.getDescription());
        tm.setCreationUser(oldTm.getCreationUser());
        tm.setCreationDate(oldTm.getCreationDate());
        tm.setCompanyId(oldTm.getCompanyId());
        tm.setTm3Id(tm3tm.getId());
        tm.setIsRemoteTm(false);
        session.save(tm);
        session.flush();
        progress.setMessageKey("lb_tm_migrate_tm3_created", 
                "Created Project TM id " + tm.getId());
                
        // just to borrow luceneIndexTus
        Tm3SegmentTmInfo tm3SegmentTmInfo = new Tm3SegmentTmInfo();

        // the lucene index uses this
        CompanyThreadLocal.getInstance().setIdValue(Long.toString(companyId));

        while (true) {
            synchronized(this) {
                if (userInterrupt || ! segments.hasNext()) {
                    break;
                }
                SegmentTmTu oldTu = segments.next();
                oldCount++;
                if (oldTu == null) {
                    continue;
                }
                BaseTmTuv oldSrcTuv = oldTu.getSourceTuv();
                TM3Saver<GSTuvData>.Tu tu = saver.tu(new GSTuvData(oldSrcTuv), 
                        oldTu.getSourceLocale(), events.get(oldSrcTuv));
                tu.attr(fromWsAttr, oldTu.isFromWorldServer());
                tu.attr(translatableAttr, oldTu.isTranslatable());
                if (oldTu.getType() != null) {
                    tu.attr(typeAttr, oldTu.getType());
                }
                if (oldTu.getFormat() != null) {
                    tu.attr(formatAttr, oldTu.getFormat());
                }
                if (oldSrcTuv.getSid() != null) {
                    tu.attr(sidAttr, oldSrcTuv.getSid());
                }
                for (BaseTmTuv tuv : oldTu.getTuvs()) {
                    if (tuv.equals(oldSrcTuv)) {
                        continue;
                    }
                    tu.target(new GSTuvData(tuv), tuv.getLocale(), events.get(tuv));
                }
                if (oldCount % 1000 == 0) {
                    if (!userInterrupt) {
                        List<TM3Tu<GSTuvData>> saved = saver.save(TM3SaveMode.MERGE);
                        try {
                            tm3SegmentTmInfo.luceneIndexTus(tm.getId(), saved);
                        } catch (Exception e) {
                            throw new TM3Exception(e);
                        }
                        newCount += saved.size();
                        // Commit this batch and start a new transaction
                        control.commitAndRestartTransaction(); 
                        // Update the percentage
                        progress.setPercentage((int)((100 * newCount) / totalCount));
                    }
                }
            }
        }
        // HACK: Normally we should call segments.finish() here, but we 
        // still own/need the session, so we'll clean it up ourselves.
        synchronized(this) {
            if (!userInterrupt) {
                List<TM3Tu<GSTuvData>> saved = saver.save(TM3SaveMode.MERGE);
                try {
                    tm3SegmentTmInfo.luceneIndexTus(tm.getId(), saved);
                } catch (Exception e) {
                    throw new TM3Exception(e);
                }
                newCount += saved.size();
                progress.setPercentage(99);
                
                progress.setMessageKey("lb_tm_migrate_tm3_created", 
                        "Created tm3 tm " + tm3tm.getId() + " with " + newCount + " tu");
                
                // Now update any TM Profiles that are saving to the old TM so that they 
                // update to the migrated TM.
                /**
                progress.setMessageKey("lb_tm_migrate_tm3_created", 
                        "Updating TM Profiles");
                List<TranslationMemoryProfile> profiles = 
                    session.createCriteria(TranslationMemoryProfile.class)
                        .add(Restrictions.eq("projectTmIdForSave", oldTm.getId()))
                        .list();
                for (TranslationMemoryProfile p : profiles) {
                    p.setProjectTmIdForSave(tm.getId());
                }
                */
                progress.setPercentage(100);
                progress.setMessageKey("lb_done", "Done");
                return tm;
            } else {
                progress.setMessageKey("lb_tm_convert_cancel", "User cancel the conversion");
                return null;
            }
        }
    }
    
    public TM3Tm<GSTuvData> getCurrrentTm3() {
        return this.tm3tm;
    }
    
    public String getUniqueTmName(Session session, String base) {
        for (int i = 1; i < 100; i++) {
            StringBuilder sb = new StringBuilder(base).append(" (migrated");
            if (i > 1) {
                sb.append(" ").append(i);
            }
            String candidate = sb.append(")").toString();
            if (!checkForTmByName(session, candidate)) {
                return candidate;
            }
        }
        // Couldn't find one in 100 tries?  Give up.
        return null;
    }
    
    private boolean checkForTmByName(Session session, String name) {
        ProjectTM tm = (ProjectTM)session.createCriteria(ProjectTM.class)
                .add(Restrictions.eq("name", name))
                .uniqueResult();
        return (tm != null);
    }
    
    public synchronized void cancelConvert() {
        this.userInterrupt = true;
    }
    
    static class EventMap {
        private TM3Tm<GSTuvData> tm;
        private Map<LegacyEventKey, TM3Event> events = 
            new HashMap<LegacyEventKey, TM3Event>();
        EventMap(TM3Tm<GSTuvData> tm) {
            this.tm = tm;
        }
        public TM3Event get(BaseTmTuv tuv) {
            String username = tuv.getModifyUser() != null ? 
                    tuv.getModifyUser() : tuv.getCreationUser();
            Date date = tuv.getModifyDate() != null ? 
                    tuv.getModifyDate() : tuv.getCreationDate();
            LegacyEventKey k = new LegacyEventKey(username, date);
            TM3Event e = events.get(k);
            if (e == null) {
                e = tm.addEvent(EventType.LEGACY_MIGRATE.getValue(), username, null, date);
                events.put(k, e);
            }
            return e;
        }
    }
    
    static class LegacyEventKey {
        String username;
        Date date;
        LegacyEventKey(String username, Date date) {
            this.username = username;
            this.date = date;
        }
        @Override
        public boolean equals(Object o) {
            LegacyEventKey k = (LegacyEventKey)o;
            return username.equals(k.username) && date.equals(k.date);
        }
        @Override
        public int hashCode() {
            return 17 * username.hashCode() + date.hashCode();
        }
    }


}
