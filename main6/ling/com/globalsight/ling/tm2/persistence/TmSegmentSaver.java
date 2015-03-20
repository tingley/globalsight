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
package com.globalsight.ling.tm2.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.population.SegmentsForSave;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * TmSegmentSaver class is responsible for saving segments to the TM.
 */

public class TmSegmentSaver
{
    static private final Logger logger = Logger
    .getLogger(TmSegmentSaver.class);
    
    private static final boolean TRANSLATABLE = true;
    private static final boolean LOCALIZABLE = false;

    private static final boolean PAGE_TM = true;
    private static final boolean SEGMENT_TM = false;

    private Connection m_connection;

    static private final String INSERT_INTO = "INSERT INTO ";

    static private final String INSERT_PAGE_TM_TU = " (id, tm_id, format, type, source_tm_name) VALUES (?, ?, ?, ?, ?)";

    static private final String INSERT_SEGMENT_TM_TU = " (id, tm_id, format, type, source_locale_id, source_tm_name, from_world_server) VALUES (?, ?, ?, ?, ?, ?, ?)";

    static private final String INSERT_NON_CLOB_TUV = " (id, tu_id, segment_string, segment_clob, "
            + "exact_match_key, locale_id, creation_date, modify_date, "
            + "creation_user, modify_user, updated_by_project, sid) VALUES "
            + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; 

    static private final String UPDATE = "UPDATE ";

    static private final String UPDATE_SET = " SET modify_date = ?, modify_user = ?, "
            + "updated_by_project = ? WHERE id = ?";

    static private final String UPDATE_SOURCE_TM_TU = " SET source_tm_name = ? WHERE id = ?";
    
    static private final ResourceBundle RESUORCE_BUNDLE =
        SystemResourceBundle.getInstance().getResourceBundle(
            ResourceBundleConstants.EXCEPTION_RESOURCE_NAME,
            Locale.getDefault());

    public TmSegmentSaver(Connection p_connection) throws Exception
    {
        m_connection = p_connection;
    }

    /**
     * Save segments to Page TM
     * 
     * @param p_segmentsForSave
     *            repository that holds segments to save
     * @param p_tmId
     *            Tm id in which segments are saved
     */
    public void saveToPageTm(SegmentsForSave p_segmentsForSave, long p_tmId)
            throws Exception
    {
        // create translatable Tus and Tuvs
        createNewTuTuv(p_segmentsForSave, p_tmId, PAGE_TM, TRANSLATABLE);
        // create localizable Tus and Tuvs
        createNewTuTuv(p_segmentsForSave, p_tmId, PAGE_TM, LOCALIZABLE);
        // add translatable Tuvs to existing Tus
        addTuvs(p_segmentsForSave, PAGE_TM, TRANSLATABLE);
        // add localizable Tuvs to existing Tus
        addTuvs(p_segmentsForSave, PAGE_TM, LOCALIZABLE);
    }

    /**
     * Save segments to Segment TM
     * 
     * @param p_segmentsForSave
     *            repository that holds segments to save
     * @param p_tmId
     *            Tm id in which segments are saved
     */
    public void saveToSegmentTm(SegmentsForSave p_segmentsForSave, long p_tmId)
            throws Exception
    {
        // create translatable Tus and Tuvs
        createNewTuTuv(p_segmentsForSave, p_tmId, SEGMENT_TM, TRANSLATABLE);
        // create localizable Tus and Tuvs
        createNewTuTuv(p_segmentsForSave, p_tmId, SEGMENT_TM, LOCALIZABLE);
        // add translatable Tuvs to existing Tus
        addTuvs(p_segmentsForSave, SEGMENT_TM, TRANSLATABLE);
        // add localizable Tuvs to existing Tus
        addTuvs(p_segmentsForSave, SEGMENT_TM, LOCALIZABLE);
        // update translatable Tuv's modifyDate
        updateTuvs(p_segmentsForSave, TRANSLATABLE);
        // update localizable Tuv's modifyDate
        updateTuvs(p_segmentsForSave, LOCALIZABLE);

        // update translatable Tu's source tm name.
        updateTuSourceTmName(p_segmentsForSave, SEGMENT_TM, TRANSLATABLE);
        // update localizable Tu's source tm name.
        updateTuSourceTmName(p_segmentsForSave, SEGMENT_TM, LOCALIZABLE);
    }

    private void updateTuSourceTmName(SegmentsForSave p_segmentsForSave,
            boolean p_pageTm, boolean p_translatable) throws Exception
    {

        // get Tu table name
        String tuTableName = getTuTableName(p_pageTm, p_translatable);

        PreparedStatement tuUpdateStmt = m_connection.prepareStatement(UPDATE
                + tuTableName + UPDATE_SOURCE_TM_TU);

        // addBatch counters
        int batchUpdate = 0;

        Iterator itUpdateTu = p_segmentsForSave.getTusForChangeSourceTm(
                p_translatable).iterator();
        while (itUpdateTu.hasNext())
        {
            BaseTmTu updateTu = (BaseTmTu) itUpdateTu.next();

            // tuvAddBatchForUpdate(tuvUpdateStmt, updateTuv.getTuvIdToUpdate(),
            // updateTuv.getTuv().getModifyDate());
            tuAddBatchForChangeSourceTm(tuUpdateStmt, updateTu);
            batchUpdate++;

            if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
            {
                tuUpdateStmt.executeBatch();
                batchUpdate = 0;
            }
        }

        // execute the rest of the added batch
        if (batchUpdate > 0)
        {
            tuUpdateStmt.executeBatch();
        }

        tuUpdateStmt.close();
    }

    private void createNewTuTuv(SegmentsForSave p_segmentsForSave, long p_tmId,
            boolean p_pageTm, boolean p_translatable) throws Exception
    {
        // get sequece number for Tu table
        long tuSeq = getTuSequence(p_segmentsForSave, p_pageTm, p_translatable);
        // get sequece number for Tuv table
        long tuvSeq = getTuvSequence(p_segmentsForSave, p_pageTm,
                p_translatable);

        // get Tu table name
        String tuTableName = getTuTableName(p_pageTm, p_translatable);
        // get Tuv table name
        String tuvTableName = getTuvTableName(p_pageTm, p_translatable);

        PreparedStatement tuInsert = null;
        PreparedStatement tuvNonClobInsert = null;
        
        try
        {
            if (p_pageTm)
            {
                tuInsert = m_connection.prepareStatement(INSERT_INTO + tuTableName
                        + INSERT_PAGE_TM_TU);
            }
            else
            {
                tuInsert = m_connection.prepareStatement(INSERT_INTO + tuTableName
                        + INSERT_SEGMENT_TM_TU);
            }

            tuvNonClobInsert = m_connection
                    .prepareStatement(INSERT_INTO + tuvTableName
                            + INSERT_NON_CLOB_TUV);

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // addBatch counters
            int batchTus = 0;
            int batchNonClobs = 0;

            Iterator itTu = p_segmentsForSave.getTusForCreate(p_translatable)
                    .iterator();
           
            while (itTu.hasNext())
            {
                SegmentsForSave.CreateTu createTu = (SegmentsForSave.CreateTu) itTu
                        .next();
                if (p_pageTm)
                {
                    tuAddBatchForPageTm(tuInsert, createTu.getTu(), tuSeq, p_tmId);
                }
                else
                {
                    tuAddBatchForSegmentTm(tuInsert, createTu.getTu(), tuSeq,
                            p_tmId, p_segmentsForSave.getSourceLocale());
                }

                createTu.setNewTuId(tuSeq++);
                batchTus++;

                Iterator itTuv = createTu.getAddTuvIterator();
                
                while (itTuv.hasNext())
                {
                    SegmentsForSave.AddTuv addTuv = (SegmentsForSave.AddTuv) itTuv
                            .next();

                    BaseTmTuv tuv = addTuv.getTuv();
                    tuvNonClobAddBatch(tuvNonClobInsert, tuv, tuvSeq, createTu
                            .getNewTuId(), now);
                    batchNonClobs++;

                    addTuv.setNewTuvId(tuvSeq++);
                }

                if (batchNonClobs > DbUtil.BATCH_INSERT_UNIT
                        || batchTus > DbUtil.BATCH_INSERT_UNIT)
                {
                    tuInsert.executeBatch();
                    tuvNonClobInsert.executeBatch();
                    batchTus = batchNonClobs = 0;
                }
            }

            if (batchNonClobs > 0 || batchTus > 0)
            {
                tuInsert.executeBatch();
                tuvNonClobInsert.executeBatch();
            }
        }
        finally
        {
            if (tuInsert != null)
                tuInsert.close();

            if (tuvNonClobInsert != null)
                tuvNonClobInsert.close();
        }
    }
    
    private void addTuvs(SegmentsForSave p_segmentsForSave, boolean p_pageTm,
            boolean p_translatable) throws Exception
    {
        // get sequece number for Tuv table
        long tuvSeq = getTuvSequenceForAdd(p_segmentsForSave, p_pageTm,
                p_translatable);

        // get Tuv table name
        String tuvTableName = getTuvTableName(p_pageTm, p_translatable);

        PreparedStatement tuvNonClobInsert = m_connection
                .prepareStatement(INSERT_INTO + tuvTableName
                        + INSERT_NON_CLOB_TUV);
        // PreparedStatement tuvClobInsert = m_connection.prepareStatement(
        // INSERT_INTO + tuvTableName + INSERT_CLOB_TUV);

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // addBatch counters
        int batchNonClobs = 0;
        int batchClobs = 0;

        Iterator itAddTuv = p_segmentsForSave.getTuvsForAdd(p_translatable)
                .iterator();
        while (itAddTuv.hasNext())
        {
            SegmentsForSave.AddTuv addTuv = (SegmentsForSave.AddTuv) itAddTuv
                    .next();
            BaseTmTuv tuv = addTuv.getTuv();
            tuvNonClobAddBatch(tuvNonClobInsert, tuv, tuvSeq, addTuv
                    .getTuIdToAdd(), now);
            batchNonClobs++;

            addTuv.setNewTuvId(tuvSeq++);
            if (batchNonClobs > DbUtil.BATCH_INSERT_UNIT)
            {
                tuvNonClobInsert.executeBatch();
                batchNonClobs = 0;
            }
        }

        // execute the rest of the added batch
        if (batchNonClobs > 0)
        {
            tuvNonClobInsert.executeBatch();
        }

        tuvNonClobInsert.close();
    }
    
    private void updateTuvs(SegmentsForSave p_segmentsForSave,
            boolean p_translatable) throws Exception
    {
        // get Segment Tm Tuv table name
        String tuvTableName = getTuvTableName(false, p_translatable);

        PreparedStatement tuvUpdateStmt = m_connection.prepareStatement(UPDATE
                + tuvTableName + UPDATE_SET);

        // addBatch counters
        int batchUpdate = 0;

        Iterator itUpdateTuv = p_segmentsForSave.getTuvsForUpdate(
                p_translatable).iterator();
        while (itUpdateTuv.hasNext())
        {
            SegmentsForSave.UpdateTuv updateTuv = (SegmentsForSave.UpdateTuv) itUpdateTuv
                    .next();

            tuvAddBatchForUpdate(tuvUpdateStmt, updateTuv.getTuvIdToUpdate(),
                    updateTuv.getTuv());
            batchUpdate++;

            if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
            {
                tuvUpdateStmt.executeBatch();
                batchUpdate = 0;
            }
        }

        // execute the rest of the added batch
        if (batchUpdate > 0)
        {
            tuvUpdateStmt.executeBatch();
        }

        tuvUpdateStmt.close();
    }

    private long getTuSequence(SegmentsForSave p_segmentsForSave,
            boolean p_pageTm, boolean p_translatable) throws Exception
    {
        String tuSeqName = null;
        if (p_pageTm)
        {
            tuSeqName = p_translatable ? Sequence.PAGE_TM_TU_T_SEQ
                    : Sequence.PAGE_TM_TU_L_SEQ;
        }
        else
        {
            tuSeqName = p_translatable ? Sequence.SEGMENT_TM_TU_T_SEQ
                    : Sequence.SEGMENT_TM_TU_L_SEQ;
        }

        long tuNum = p_segmentsForSave.getCreateTuNum(p_translatable);

        return Sequence.allocateIds(tuSeqName, tuNum);
    }

    private long getTuvSequence(SegmentsForSave p_segmentsForSave,
            boolean p_pageTm, boolean p_translatable) throws Exception
    {
        String tuvSeqName = null;
        if (p_pageTm)
        {
            tuvSeqName = p_translatable ? Sequence.PAGE_TM_TUV_T_SEQ
                    : Sequence.PAGE_TM_TUV_L_SEQ;
        }
        else
        {
            tuvSeqName = p_translatable ? Sequence.SEGMENT_TM_TUV_T_SEQ
                    : Sequence.SEGMENT_TM_TUV_L_SEQ;
        }

        long tuvNum = p_segmentsForSave.getCreateTuvNum(p_translatable);

        // get sequece number for Tuv table
        return Sequence.allocateIds(tuvSeqName, tuvNum);
    }

    private long getTuvSequenceForAdd(SegmentsForSave p_segmentsForSave,
            boolean p_pageTm, boolean p_translatable) throws Exception
    {
        String tuvSeqName = null;
        if (p_pageTm)
        {
            tuvSeqName = p_translatable ? Sequence.PAGE_TM_TUV_T_SEQ
                    : Sequence.PAGE_TM_TUV_L_SEQ;
        }
        else
        {
            tuvSeqName = p_translatable ? Sequence.SEGMENT_TM_TUV_T_SEQ
                    : Sequence.SEGMENT_TM_TUV_L_SEQ;
        }

        long tuvNum = p_segmentsForSave.getAddTuvNum(p_translatable);

        // get sequece number for Tuv table
        return Sequence.allocateIds(tuvSeqName, tuvNum);
    }

    private String getTuTableName(boolean p_pageTm, boolean p_translatable)
    {
        String tuTableName = null;
        if (p_pageTm)
        {
            tuTableName = p_translatable ? PageTmPersistence.PAGE_TM_TU_T
                    : PageTmPersistence.PAGE_TM_TU_L;
        }
        else
        {
            tuTableName = p_translatable ? SegmentTmPersistence.SEGMENT_TM_TU_T
                    : SegmentTmPersistence.SEGMENT_TM_TU_L;
        }
        return tuTableName;
    }

    private String getTuvTableName(boolean p_pageTm, boolean p_translatable)
    {
        String tuvTableName = null;
        if (p_pageTm)
        {
            tuvTableName = p_translatable ? PageTmPersistence.PAGE_TM_TUV_T
                    : PageTmPersistence.PAGE_TM_TUV_L;
        }
        else
        {
            tuvTableName = p_translatable ? SegmentTmPersistence.SEGMENT_TM_TUV_T
                    : SegmentTmPersistence.SEGMENT_TM_TUV_L;
        }
        return tuvTableName;
    }

    private void tuAddBatchForPageTm(PreparedStatement p_tuInsert,
            BaseTmTu p_tu, long p_tuId, long p_tmId) throws Exception
    {
        p_tuInsert.setLong(1, p_tuId);
        p_tuInsert.setLong(2, p_tmId);
        p_tuInsert.setString(3, p_tu.getFormat());
        p_tuInsert.setString(4, p_tu.getType());
        p_tuInsert.setString(5, p_tu.getSourceTmName());
        p_tuInsert.addBatch();
    }

    private void tuAddBatchForSegmentTm(PreparedStatement p_tuInsert,
            BaseTmTu p_tu, long p_tuId, long p_tmId,
            GlobalSightLocale p_sourceLocale) throws Exception
    {
        p_tuInsert.setLong(1, p_tuId);
        p_tuInsert.setLong(2, p_tmId);
        p_tuInsert.setString(3, p_tu.getFormat());
        p_tuInsert.setString(4, p_tu.getType());
        p_tuInsert.setLong(5, p_sourceLocale.getId());
        p_tuInsert.setString(6, p_tu.getSourceTmName());

        if (p_tu instanceof SegmentTmTu)
        {
            p_tuInsert.setString(7, p_tu.isFromWorldServer() ? "y" : "n");
        }
        else
        {
            p_tuInsert.setString(7, "n");
        }

        p_tuInsert.addBatch();
    }

    private void tuvNonClobAddBatch(PreparedStatement p_tuvNonClobInsert,
            BaseTmTuv p_tuv, long p_tuvId, long p_tuId, Timestamp p_now)
            throws Exception
    {
        p_tuvNonClobInsert.setLong(1, p_tuvId);
        p_tuvNonClobInsert.setLong(2, p_tuId);
        if (EditUtil.getUTF8Len(p_tuv.getSegment()) <= PersistentObject.CLOB_THRESHOLD) {
            p_tuvNonClobInsert.setString(3, p_tuv.getSegment());
            p_tuvNonClobInsert.setString(4, null);
        } else { 
            p_tuvNonClobInsert.setString(3, null);
            p_tuvNonClobInsert.setString(4, p_tuv.getSegment());
        }
        p_tuvNonClobInsert.setLong(5, p_tuv.getExactMatchKey());
        p_tuvNonClobInsert.setLong(6, p_tuv.getLocale().getId());
        p_tuvNonClobInsert.setTimestamp(7, getCreationDate(p_tuv, p_now));
        p_tuvNonClobInsert.setTimestamp(8, getModifyDate(p_tuv, p_now));
        p_tuvNonClobInsert.setString(9, p_tuv.getCreationUser());
        p_tuvNonClobInsert.setString(10, p_tuv.getModifyUser());
        p_tuvNonClobInsert.setString(11, p_tuv.getUpdatedProject());
        String sid = p_tuv.getSid();
        if (sid != null && sid.length() > 254)
        {
        	sid = sid.substring(0, 254);
        }
        p_tuvNonClobInsert.setString(12, sid);
        p_tuvNonClobInsert.addBatch();
    }

    private void tuvAddBatchForUpdate(PreparedStatement p_tuvUpdateStmt,
            long p_tuvId, BaseTmTuv p_tuv) throws Exception
    {
        p_tuvUpdateStmt.setTimestamp(1, p_tuv.getModifyDate());
        p_tuvUpdateStmt.setString(2, getModifyUser(p_tuv));
        p_tuvUpdateStmt.setString(3, p_tuv.getUpdatedProject());
        p_tuvUpdateStmt.setLong(4, p_tuvId);
        p_tuvUpdateStmt.addBatch();
    }

    private void tuAddBatchForChangeSourceTm(PreparedStatement p_tuUpdateStmt,
            BaseTmTu p_tu) throws Exception
    {
        p_tuUpdateStmt.setString(1, p_tu.getSourceTmName());
        p_tuUpdateStmt.setLong(2, p_tu.getId());
        p_tuUpdateStmt.addBatch();
    }

    private Timestamp getCreationDate(BaseTmTuv p_tuv, Timestamp p_now)
    {
        Timestamp creationDate = p_tuv.getCreationDate();
        Timestamp modifyDate = p_tuv.getModifyDate();

        if (creationDate == null)
        {
            if (modifyDate == null)
            {
                creationDate = p_now;
            }
            else
            {
                creationDate = modifyDate;
            }
        }

        return creationDate;
    }

    private Timestamp getModifyDate(BaseTmTuv p_tuv, Timestamp p_now)
    {
        Timestamp creationDate = p_tuv.getCreationDate();
        Timestamp modifyDate = p_tuv.getModifyDate();

        if (creationDate == null)
        {
            if (modifyDate == null)
            {
                modifyDate = p_now;
            }
        }
        else
        {
            if (modifyDate == null)
            {
                modifyDate = creationDate;
            }
        }

        return modifyDate;
    }

    private String getModifyUser(BaseTmTuv p_tuv)
    {
        // BaseTmTuv came from tm search-replace function, the modify user
        // would be the real modify user. BaseTmTuv came from job export, the
        // modify user would be creator, as we recorded user who have updated
        // this tuv as its creator during tm pupulation.
        return p_tuv.getModifyUser() == null ? p_tuv.getCreationUser() : p_tuv
                .getModifyUser();
    }
}
