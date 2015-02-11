package com.globalsight.ling.tm2.segmenttm;

import static com.globalsight.ling.tm2.population.TmPopulator.LOCALIZABLE;
import static com.globalsight.ling.tm2.population.TmPopulator.TRANSLATABLE;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.TMAttributeManager;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.TmSegmentIndexer;
import com.globalsight.ling.tm2.persistence.SegmentQueryResult;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;
import com.globalsight.ling.tm2.persistence.SegmentTmRetriever;
import com.globalsight.ling.tm2.persistence.TmSegmentSaver;
import com.globalsight.ling.tm2.population.SegmentsForSave;
import com.globalsight.ling.tm2.population.UniqueSegmentRepository;
import com.globalsight.ling.tm2.population.UniqueSegmentRepositoryForCorpus;
import com.globalsight.util.GlobalSightLocale;


/**
 * This class holds Tm2-specific code that used to live in 
 * TmPopulator and is now accessed through Tm2SegmentTmInfo.
 */
public class SegmentTmPopulator {
    private static final Logger c_logger =
        Logger.getLogger(
                SegmentTmPopulator.class);
    
    private Connection m_connection;
    public static final Object LOCK = new Object();
    
    public SegmentTmPopulator(Connection connection) {
        this.m_connection = connection;
    }
    
    /**
     * Save segments to Segment Tm
     *
     * @param p_segmentsToSave Collection of PageTmTu (job data)
     * @param p_sourceLocale source locale
     * @param p_saveTmId Tm id in which segments are saved
     * @param p_targetLocales Set of target locales of all segments
     * @param p_mode one of the "SYNC" constants, to overwrite
     * existing TUs, merge existing TUs with new TUs, or to discard
     * new TUs if they already exist in the TM.
     *
     * @return mappings of translation_unit_variant id and
     * project_tm_tuv_t id of this page
     */
    public TuvMappingHolder populateSegmentTm(
        Collection<? extends BaseTmTu> p_segmentsToSave, GlobalSightLocale p_sourceLocale,
        Tm tm, Set<GlobalSightLocale> p_targetLocales, int p_mode, boolean p_fromTmImport)
        throws Exception
    {
        long p_saveTmId = tm.getId();
        ProjectTM projectTM = null;
        if (tm instanceof ProjectTM)
        {
            projectTM = (ProjectTM) tm;
        }
        
        // sanity check. No data to save, no further processing
        if(p_segmentsToSave.size() == 0)
        {
            c_logger.debug("No segments to save to segment TM");
            return new TuvMappingHolder();
        }

        // save segments to UniqueSegmentRepositoryForCorpus to remove
        // duplicates and maintain identical segments list
        UniqueSegmentRepositoryForCorpus jobDataToSave
            = new UniqueSegmentRepositoryForCorpus(p_sourceLocale);

        for (BaseTmTu tu : p_segmentsToSave)
        {
            // separate subflows out of the main text and save them as
            // independent segments
            Collection<SegmentTmTu> segmentTus = TmUtil.createSegmentTmTus(
                tu, p_sourceLocale);
            jobDataToSave.addTus(segmentTus);
        }

        SegmentsForSave segmentsForSave = null;
        synchronized (LOCK)
        {
            try
            {
                SegmentTmPersistence stPersistence
                    = new SegmentTmPersistence(m_connection);

                c_logger.debug("Got lock for segment tm");
                // get lock on tables
//                stPersistence.lockSegmentTmTables();
                
                c_logger.debug("Retrieve segment tm begin");
                // Get segments from Segment TM
                UniqueSegmentRepository segmentTmData
                    = getSegmentsFromSegmentTm(jobDataToSave,
                        p_saveTmId, p_sourceLocale, p_targetLocales);
                c_logger.debug("Retrieve segment tm end");
     
                // Walk through Tus to be saved and see if an identical
                // source segment exists in the Segment TM. According to
                // the test result and the sync mode, mark Tus for
                // creation, Tuvs for addition to the database, change the
                // timestamp or mark existing Tus to be removed and put
                // them in SegmentsForSave object.
                segmentsForSave
                    = new SegmentsForSave(p_sourceLocale, p_saveTmId);
                            
                for (Iterator<BaseTmTuv> it = jobDataToSave.sourceTuvIterator(); it.hasNext(); )
                {
                    determineSegmentTmSegmentsToSave(it.next(),
                        segmentTmData, segmentsForSave, p_sourceLocale, p_mode, p_fromTmImport);
                }

                c_logger.debug("Remove tu tuv begin");
                // Remove Tu and Tuvs
                removeTuAndTuv(stPersistence, segmentsForSave, p_saveTmId);
                c_logger.debug("Remove tu tuv end");

                c_logger.debug("Save segments begin");
                // Save segments to Segment TM
                TmSegmentSaver tmSegmentSaver = new TmSegmentSaver(m_connection);
                
                tmSegmentSaver.saveToSegmentTm(segmentsForSave, p_saveTmId);
                c_logger.debug("Save segments end");
                
                // remove and save new properties
                c_logger.debug("Save tu properties begin");
                Collection removedTus = segmentsForSave.getTusForRemove(true);
                Collection createdTus = segmentsForSave.getTusForCreate(true);
                Collection updateTuvs = segmentsForSave.getTuvsForUpdate(true);
                Collection addedTuvs = segmentsForSave.getTuvsForAdd(true);
                Collection noOpTuvs = segmentsForSave.getTuvsForNoOp();

                // for job populator                
                Map<String, String> attValues = TMAttributeManager.getTUAttributesForPopulator(projectTM, m_job);
                
                // for TM importing
                for (Object obj : removedTus)
                {
                    SegmentTmTu tu = (SegmentTmTu) obj;
                    ProjectTmTuTProp.removeTuProps(m_connection, tu.getId());
                }
                
                for (Object obj : createdTus)
                {
                    SegmentsForSave.CreateTu ctu = (SegmentsForSave.CreateTu) obj;
                    long tuId = ctu.getNewTuId();
                    BaseTmTu baseTu = ctu.getTu();
                    if (baseTu instanceof SegmentTmTu)
                    {
                        SegmentTmTu stu = (SegmentTmTu) baseTu;
                        Collection<ProjectTmTuTProp> props = stu.getProps();
                        for (ProjectTmTuTProp prop : props)
                        {
                            ProjectTmTuTProp.saveTuProp(m_connection, prop, tuId);
                        }
                        
                        for (String attName : attValues.keySet())
                        {
                            ProjectTmTuTProp p = new ProjectTmTuTProp();
                            p.setPropType(ProjectTmTuTProp.TYPE_ATT_PREFIX + attName);
                            p.setPropValue(attValues.get(attName));
                            
                            ProjectTmTuTProp.saveTuProp(m_connection, p, tuId);
                        }
                    }
                }
                
                List<Long> handledTus = new ArrayList<Long>();
                for (Object obj : updateTuvs)
                {
                    SegmentsForSave.UpdateTuv tuv = (SegmentsForSave.UpdateTuv) obj;
                    long tuId = tuv.getTuId();
                    Long tuIdLong = new Long(tuId);

                    if (!handledTus.contains(tuIdLong))
                    {
                        BaseTmTu baseTu = tuv.getTuv().getTu();

                        mergeTuProperties(baseTu, handledTus, tuId, tuIdLong, attValues);
                    }
                }
                
                handledTus = new ArrayList<Long>();
                for (Object obj : noOpTuvs)
                {
                    SegmentsForSave.UpdateTuv tuv = (SegmentsForSave.UpdateTuv) obj;
                    long tuId = tuv.getTuId();
                    Long tuIdLong = new Long(tuId);

                    if (!handledTus.contains(tuIdLong))
                    {
                        BaseTmTu baseTu = tuv.getTuv().getTu();

                        mergeTuProperties(baseTu, handledTus, tuId, tuIdLong, attValues);
                    }
                }
                
                handledTus = new ArrayList<Long>();
                for (Object obj : addedTuvs)
                {
                    SegmentsForSave.AddTuv tuv = (SegmentsForSave.AddTuv) obj;
                    long tuId = tuv.getTuIdToAdd();
                    Long tuIdLong = new Long(tuId);

                    if (!handledTus.contains(tuIdLong))
                    {
                        BaseTmTu baseTu = tuv.getTuv().getTu();

                        mergeTuProperties(baseTu, handledTus, tuId, tuIdLong, attValues);
                    }
                }
                
                c_logger.debug("Save tu properties end");

                c_logger.debug("Save index begin");
                // index segments
                TmSegmentIndexer tmSegmentIndexer = new TmSegmentIndexer();
                tmSegmentIndexer.indexSegmentTmSegments(segmentsForSave);
                c_logger.debug("Save index end");

                c_logger.debug("Releasing lock for segment tm");
                // commit the change and unlock tables
                m_connection.commit();            
            }
            catch(Exception e)
            {
                // rollback the changes and unlock tables
                m_connection.rollback();
                throw e;
            }
            catch(Throwable t)
            {
                t.printStackTrace();
                
                // rollback the changes and unlock tables
                m_connection.rollback();
                throw new Exception(t.getMessage());
            }
            finally
            {
//                DbUtil.unlockTables(m_connection);
            }
        }
        
        return makeCorpusMapping(tm, segmentsForSave, jobDataToSave);
    }

    private void mergeTuProperties(BaseTmTu baseTu, List<Long> handledTus, long tuId,
            Long tuIdLong, Map<String, String> attValue) throws SQLException
    {
        if (baseTu instanceof SegmentTmTu)
        {
            SegmentTmTu stu = (SegmentTmTu) baseTu;
            Collection<ProjectTmTuTProp> props = stu.getProps();

            for (ProjectTmTuTProp prop : props)
            {
                if (prop.getPropType() == null || prop.getPropValue() == null
                        || "".equals(prop.getPropType())
                        || "".equals(prop.getPropValue()))
                {
                    continue;
                }

                // Merge properties which have same name
                if (!ProjectTmTuTProp.doesPropsExistTu(m_connection, tuId,
                        prop.getPropType()))
                {
                    ProjectTmTuTProp.saveTuProp(m_connection, prop, tuId);
                }
                else
                {
                    ProjectTmTuTProp.updateTuProp(m_connection, prop, tuId);
                }
            }

            for (String attName : attValue.keySet())
            {
                String propType = ProjectTmTuTProp.TYPE_ATT_PREFIX + attName;
                String propV = attValue.get(attName);

                // do not merge properties which have same name
                if ("".equals(propV)
                        || ProjectTmTuTProp.doesPropsExistTu(m_connection, tuId,
                                propType))
                {
                    continue;
                }

                ProjectTmTuTProp p = new ProjectTmTuTProp();
                p.setPropType(propType);
                p.setPropValue(propV);

                ProjectTmTuTProp.saveTuProp(m_connection, p, tuId);
            }
            
            handledTus.add(tuIdLong);
        }
    }

    private UniqueSegmentRepository getSegmentsFromSegmentTm(
            UniqueSegmentRepository p_dataToSave, long p_tmId,
            GlobalSightLocale p_sourceLocale, Set p_targetLocales)
            throws Exception
    {
        UniqueSegmentRepository segmentTmData
            = new UniqueSegmentRepository(p_sourceLocale);

        // retrieve segments from Segment TM that satisfy the
        // following criteria:
        // - a source segment that has the same exact match key, the
        //   same type and the same localize type (translatable or
        //   localizable) as the source segment in the data to be saved.
        // - along with the corresponding target segments whose
        //   locales are in the given list of target locales
        SegmentTmRetriever segmentTmRetriever = new SegmentTmRetriever(
            m_connection, p_tmId, p_dataToSave, p_targetLocales);

        SegmentQueryResult result = segmentTmRetriever.query();
        BaseTmTu tu = null;
        while((tu = result.getNextTu()) != null)
        {
            segmentTmData.addTu(tu);
        }

        return segmentTmData;
    }


    // Walk through Tus to be saved and see if an identical source
    // segment exists in the Segment TM. According to the test
    // result, mark Tus for creation, Tuvs for addition to the
    // database or change the timestamp and put them in
    // SegmentsForSave object.   
    private void determineSegmentTmSegmentsToSave(
        BaseTmTuv p_sourceTuvToExamin, UniqueSegmentRepository p_segmentTmData,
        SegmentsForSave p_segmentsForSave, GlobalSightLocale p_sourceLocale,
        int p_syncMode, boolean p_fromTmImport)
    {
        BaseTmTu tuToExamin = p_sourceTuvToExamin.getTu();
        BaseTmTu segmentTmTu
            = p_segmentTmData.getMatchedTu(p_sourceTuvToExamin);

        // if there isn't matched source segment in Segment Tm, the Tu
        // along with its all Tuvs are saved in the TM
        if(segmentTmTu == null)
        {
              Iterator itLocale = tuToExamin.getAllTuvLocales().iterator();
              while(itLocale.hasNext())
              {
                  GlobalSightLocale locale
                      = (GlobalSightLocale)itLocale.next();                 
                  // mark all Tuvs in this locale for add to Segment Tm
                  Iterator itTuv
                      = tuToExamin.getTuvList(locale).iterator();
                  while(itTuv.hasNext())
                  {
                      BaseTmTuv tuvToExamin = (BaseTmTuv)itTuv.next();
                      if (!p_fromTmImport)
                      {
                        if (tuvToExamin.getModifyUser() != null)
                        {
                            // Becasue GlobalSight auto create sourceTuv and
                            // targetTuv after
                            // import a file. Originally we record pm as their
                            // creator, and insert
                            // them into translation_unit_variant table.
                            // Now we take the modify user as creator user and
                            // insert it into
                            // project_tm_tuv_t table as TM data.
                            tuvToExamin.setCreationUser(tuvToExamin
                                    .getModifyUser());
                            tuvToExamin.setCreationDate(tuvToExamin
                                    .getModifyDate());
                            tuvToExamin.setModifyDate(tuvToExamin
                                    .getModifyDate());
                            tuvToExamin.setModifyUser(tuvToExamin
                                    .getModifyUser());
                        }
                      }

                  }
                               
              }
              
            p_segmentsForSave.addTuForCreate(tuToExamin);
        }
        
        else
        {
            switch(p_syncMode)
            {
            case TmCoreManager.SYNC_DISCARD:
                // no-op. Just ignore the new Tu.
                // For special tm import, just change a tu's source tm name in database.
                segmentTmTu.setSourceTmName(tuToExamin.getSourceTmName());
                p_segmentsForSave.addTuForChangeSourceTm(segmentTmTu);
                break;
                
            case TmCoreManager.SYNC_OVERWRITE:
                p_segmentsForSave.addTuForCreate(tuToExamin);
                p_segmentsForSave.addTuForRemove(segmentTmTu);
                break;

            case TmCoreManager.SYNC_MERGE:
            default:
                // matched source segment is found...
                Iterator itLocale = tuToExamin.getAllTuvLocales().iterator();
                while(itLocale.hasNext())
                {
                    GlobalSightLocale locale
                        = (GlobalSightLocale)itLocale.next();

                    Collection segmentTmTargetTuvs
                        = segmentTmTu.getTuvList(locale);

                    if(segmentTmTargetTuvs == null)
                    {
                        // mark all Tuvs in this locale for add to Segment Tm
                        Iterator itTuv
                            = tuToExamin.getTuvList(locale).iterator();
                        while(itTuv.hasNext())
                        {
                            BaseTmTuv tuvToExamin = (BaseTmTuv)itTuv.next();
                            if (!p_fromTmImport)
                            {
                                if (tuvToExamin.getModifyUser() != null)
                                {
                                    // Becasue GlobalSight auto create sourceTuv and targetTuv after
                                    // import a file. Originally we record pm as their creator, and insert
                                    // them into translation_unit_variant table.
                                    // Now we take the modify user as creator user and insert it into 
                                    // project_tm_tuv_t table as TM data.
                                    tuvToExamin.setCreationUser(tuvToExamin.getModifyUser());
                                    tuvToExamin.setCreationDate(tuvToExamin.getModifyDate());
                                    //tuvToExamin.setModifyDate(null);
                                    tuvToExamin.setModifyUser(null);
                                }
                            }
                            p_segmentsForSave.addTuvForAdd(
                                segmentTmTu.getId(), tuvToExamin);
                        }
                    }
                    else
                    {
                        // examine if job data are added to Segment Tm or
                        // changed the last modified date column
                        examineSegmentsForAdd(p_segmentsForSave,
                            tuToExamin.getTuvList(locale),
                            segmentTmTargetTuvs, segmentTmTu, p_fromTmImport);
                    }
                }
            }
        }
    }

    /**
     * Examine if job data should be added to Segment Tm or the last
     * modified date column should be updated in case the same
     * segments are already there.
     *
     * @param p_segmentsForSave SegmentsForSave, records data to be
     *                          added, updated, etc
     * @param p_jobData Collection of job data (BaseTmTuv). They are
     *                  job data in a same Tu and in a same locale.
     * @param p_segmentsForSave Collection of data in Segment Tm
     *                  (BaseTmTuv). They are Tm data in a same Tu and
     *                  in a same locale.
     * @param p_segmentTu Tu all p_segmentData belong to.
     */
    private void examineSegmentsForAdd(
        SegmentsForSave p_segmentsForSave, Collection p_jobData,
        Collection p_segmentData, BaseTmTu p_segmentTu, boolean p_fromTmImport)
    {
        Iterator itJobData = p_jobData.iterator();
        while(itJobData.hasNext())
        {
            BaseTmTuv jobTuv = (BaseTmTuv)itJobData.next();

            boolean foundPeer = false;
            Iterator itSegmentTmTuv = p_segmentData.iterator();
            while(itSegmentTmTuv.hasNext())
            {
                BaseTmTuv segmentTmTuv = (BaseTmTuv)itSegmentTmTuv.next();
                // compare with the same locale target Tuv in Segmen
                // TM and if an identical segment is found, compare
                // the modify time and update the modify time to the
                // latest
                if(segmentTmTuv.equals(jobTuv))
                {
                    foundPeer = true;
                            
                    if(segmentTmTuv.getModifyDate()
                        .before(jobTuv.getModifyDate()))
                    {
                        if (!p_fromTmImport && jobTuv.getModifyUser() != null)
                        {
                            // Becasue GlobalSight auto create sourceTuv and targetTuv after
                            // import a file. Originally we record pm as their creator, and insert
                            // them into translation_unit_variant table.
                            // Now we take the modify user as creator user and insert it into 
                            // project_tm_tuv_t table as TM data.
                            jobTuv.setCreationUser(jobTuv.getModifyUser());
                            jobTuv.setCreationDate(jobTuv.getModifyDate());
                            //jobTuv.setModifyDate(null);
                            jobTuv.setModifyUser(null);
                        }
                        p_segmentsForSave.addTuvForUpdate(
                            p_segmentTu.getId(),
                            segmentTmTuv.getId(), jobTuv);
                    }
                    else
                    {
                        // if the modify time of the segment in the
                        // Segment Tm is more recent, add the job tuv
                        // to SegmentsForSave object as a no-op tuv
                        // for the purpose of building corpus Tm
                        p_segmentsForSave.addTuvForNoOp(
                            p_segmentTu.getId(),
                            segmentTmTuv.getId(), jobTuv);
                    }
                    break;
                }
            }

            // if an identical target segment is not found, add the
            // Tuv to the Tu
            if(!foundPeer)
            {
                if (!p_fromTmImport && jobTuv.getModifyUser() != null)
                {
                    // Becasue GlobalSight auto create sourceTuv and targetTuv after
                    // import a file. Originally we record pm as their creator, and insert
                    // them into translation_unit_variant table.
                    // Now we take the modify user as creator user and insert it into 
                    // project_tm_tuv_t table as TM data.
                    jobTuv.setCreationUser(jobTuv.getModifyUser());
                    jobTuv.setCreationDate(jobTuv.getModifyDate());
                    //jobTuv.setModifyDate(null);
                    jobTuv.setModifyUser(null);
                }
                p_segmentsForSave.addTuvForAdd(p_segmentTu.getId(), jobTuv);
            }
        }
    }
    
    

    private void removeTuAndTuv(SegmentTmPersistence p_stPersistence,
        SegmentsForSave p_segmentsForSave, long p_tmId)
        throws Exception
    {
        // remove translatable Tu and Tuvs
        Collection tus = p_segmentsForSave.getTusForRemove(TRANSLATABLE);
        Collection tuvs
            = p_stPersistence.retrieveTuvsByTuId(tus, TRANSLATABLE);

        // sort the collection of Tuvs by locale
        Map localeMap = sortTuvsByLocale(tuvs);

        // call tuv remove method by locale
        Iterator itLocale = localeMap.keySet().iterator();
        while(itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)itLocale.next();
            p_stPersistence.removeTuvs((Collection)localeMap.get(locale),
                TRANSLATABLE, locale, p_tmId);
        }

        p_stPersistence.removeTus(tus, TRANSLATABLE);
        

        // remove localizable Tu and Tuvs
        tus = p_segmentsForSave.getTusForRemove(LOCALIZABLE);
        tuvs = p_stPersistence.retrieveTuvsByTuId(tus, LOCALIZABLE);

        localeMap = sortTuvsByLocale(tuvs);
        itLocale = localeMap.keySet().iterator();
        while(itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)itLocale.next();
            p_stPersistence.removeTuvs((Collection)localeMap.get(locale),
                LOCALIZABLE, locale, p_tmId);
        }

        p_stPersistence.removeTus(tus, LOCALIZABLE);
    }
    
    private TuvMappingHolder makeCorpusMapping(Tm p_tm,
            SegmentsForSave p_segmentsForSave,
            UniqueSegmentRepositoryForCorpus p_uniqueData)
    {
        if(c_logger.isDebugEnabled())
        {
            c_logger.debug(p_segmentsForSave.toDebugString());
            c_logger.debug(p_uniqueData.toDebugString());
        }
        
        TuvMappingHolder mappingHolder = new TuvMappingHolder();

        // make mappings for tuvs whole tu of which are added in the Tm
        Iterator itCreateTu
            = p_segmentsForSave.getTusForCreate(TRANSLATABLE).iterator();

        while(itCreateTu.hasNext())
        {
            SegmentsForSave.CreateTu createTu
                = (SegmentsForSave.CreateTu)itCreateTu.next();
            Iterator itAddTuv = createTu.getAddTuvIterator();
            while(itAddTuv.hasNext())
            {
                SegmentsForSave.AddTuv addTuv
                    = (SegmentsForSave.AddTuv)itAddTuv.next();
                populateTuvMappingHolder(p_tm.getId(),
                    createTu.getNewTuId(),addTuv.getNewTuvId(),
                    addTuv.getTuv(), p_uniqueData, mappingHolder);
            }
        }

        // make mappings for tuvs that are added in the Tm
        Iterator itAddTuv
            = p_segmentsForSave.getTuvsForAdd(TRANSLATABLE).iterator();
        while(itAddTuv.hasNext())
        {
            SegmentsForSave.AddTuv addTuv
                = (SegmentsForSave.AddTuv)itAddTuv.next();
                
            populateTuvMappingHolder(p_tm.getId(),
                addTuv.getTuIdToAdd(),
                addTuv.getNewTuvId(),
                addTuv.getTuv(), p_uniqueData, mappingHolder);
        }

        // make mappings for tuvs the modify date of which are updated
        // in the Tm
        Iterator itUpdateTuv
            = p_segmentsForSave.getTuvsForUpdate(TRANSLATABLE).iterator();
        while(itUpdateTuv.hasNext())
        {
            SegmentsForSave.UpdateTuv updateTuv
                = (SegmentsForSave.UpdateTuv)itUpdateTuv.next();
                
            populateTuvMappingHolder(p_tm.getId(),
                updateTuv.getTuId(),
                updateTuv.getTuvIdToUpdate(),
                updateTuv.getTuv(), p_uniqueData, mappingHolder);
        }
        
        // make mappings for tuvs identical tuvs of which have been
        // already in the Tm
        itUpdateTuv = p_segmentsForSave.getTuvsForNoOp().iterator();
        while(itUpdateTuv.hasNext())
        {
            SegmentsForSave.UpdateTuv updateTuv
                = (SegmentsForSave.UpdateTuv)itUpdateTuv.next();
                
            populateTuvMappingHolder(p_tm.getId(),
                updateTuv.getTuId(),
                updateTuv.getTuvIdToUpdate(),
                updateTuv.getTuv(), p_uniqueData, mappingHolder);
        }

        return mappingHolder;
    }
        
    

    private Map sortTuvsByLocale(Collection p_tuvs)
    {
        Map localeMap = new HashMap();
        Iterator itTuv = p_tuvs.iterator();
        while(itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv)itTuv.next();
            List tuvList = (List)localeMap.get(tuv.getLocale());
            if(tuvList == null)
            {
                tuvList = new ArrayList();
                localeMap.put(tuv.getLocale(), tuvList);
            }
            tuvList.add(tuv);
        }
        
        return localeMap;
    }

    

    private void populateTuvMappingHolder(long p_tmId,
        long p_projectTmTuId, long p_projectTmTuvId, BaseTmTuv p_tmTuv,
        UniqueSegmentRepositoryForCorpus p_uniqueData,
        TuvMappingHolder p_mappingHolder)
    {
        GlobalSightLocale locale = p_tmTuv.getLocale();

        List tuvList = p_uniqueData.getIdenticalSegment(p_tmTuv);
        if(tuvList != null)
        {
            Iterator itTuv = tuvList.iterator();
            while(itTuv.hasNext())
            {
                BaseTmTuv tuv = (BaseTmTuv)itTuv.next();
                p_mappingHolder.addMapping(
                    locale, tuv.getId(), tuv.getTu().getId(),
                    p_tmId, p_projectTmTuId, p_projectTmTuvId);
            }
        }
    }
    
    private Job m_job;

    public void setJob(Job job)
    {
        m_job = job;
    }

    public Job getJob()
    {
        return m_job;
    }
}
