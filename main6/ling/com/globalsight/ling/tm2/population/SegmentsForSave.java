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
package com.globalsight.ling.tm2.population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;

public class SegmentsForSave
{
    private GlobalSightLocale m_sourceLocale;
    private long m_tmId;

    // List of translatable Tus to be removed
    private ArrayList m_removeTuTr = new ArrayList();
    // List of localizable Tus to be removed
    private ArrayList m_removeTuLo = new ArrayList();

    // List of translatable CreateTu (inner class) to create
    private ArrayList m_createTuTr = new ArrayList();
    // List of localizable CreateTu (inner class) to create
    private ArrayList m_createTuLo = new ArrayList();

    // List of translatable AddTuv (inner class) to add to an existing Tu
    private ArrayList m_addTuvTr = new ArrayList();
    // List of localizable AddTuv (inner class) to add to an existing Tu
    private ArrayList m_addTuvLo = new ArrayList();

    // List of translatable UpdateTuv (inner class) to update ModifyDate
    private ArrayList m_updateTuvTr = new ArrayList();
    // List of localizable UpdateTuv (inner class) to update ModifyDate
    private ArrayList m_updateTuvLo = new ArrayList();

    // List of translatable UpdateTuv (inner class) for recording
    // corresponding tuv id in the Tm (for corpus Tm)
    private ArrayList m_noOpTuvs = new ArrayList();

    private ArrayList m_changeSourceTmTuTr = new ArrayList();

    private ArrayList m_changeSourceTmTuLo = new ArrayList();

    private long m_tuTrNum = 0;
    private long m_tuLoNum = 0;
    private long m_tuvCreateTrNum = 0;
    private long m_tuvCreateLoNum = 0;
    private long m_tuvAddTrNum = 0;
    private long m_tuvAddLoNum = 0;

    public SegmentsForSave(GlobalSightLocale p_sourceLocale, long p_tmId)
    {
        m_sourceLocale = p_sourceLocale;
        m_tmId = p_tmId;
    }

    /**
     * Add a Tu that creates a new entry in the database. All Tuvs this Tu owns
     * are added to the database as well.
     * 
     * @param p_tu
     *            Tu to be added to the database
     */
    public void addTuForCreate(BaseTmTu p_tu)
    {
        CreateTu createTu = new CreateTu(p_tu);

        if (p_tu.isTranslatable())
        {
            m_tuTrNum++;
            m_tuvCreateTrNum += p_tu.getTuvSize();
            m_createTuTr.add(createTu);
        }
        else
        {
            m_tuLoNum++;
            m_tuvCreateLoNum += p_tu.getTuvSize();
            m_createTuLo.add(createTu);
        }
    }

    /**
     * Adds old target TUVs that have different locales.
     * <p>
     * For GBS-2792
     */
    public void addTuForCreate(BaseTmTu p_tuToSave, BaseTmTu p_tuInDb)
    {
        Set<GlobalSightLocale> localesInDb = p_tuInDb.getAllTuvLocales();
        Set<GlobalSightLocale> localesToSave = p_tuToSave.getAllTuvLocales();
        for (GlobalSightLocale localeInDb : localesInDb)
        {
            boolean different = true;
            for (GlobalSightLocale localeToSave : localesToSave)
            {
                if (localeInDb.equals(localeToSave))
                {
                    different = false;
                    break;
                }
            }
            if (different)
            {
                Collection<BaseTmTuv> tuvsInDb = p_tuInDb
                        .getTuvList(localeInDb);
                for (BaseTmTuv tuvInDb : tuvsInDb)
                {
                    p_tuToSave.addTuv(tuvInDb);
                }
            }
        }
        addTuForCreate(p_tuToSave);
    }

    /**
     * Add a Tu just for change its source tm name.
     * 
     * @param p_tu
     *            Tu to be changed its source tm name in database
     */
    public void addTuForChangeSourceTm(BaseTmTu p_tu)
    {

        if (p_tu.isTranslatable())
        {
            m_changeSourceTmTuTr.add(p_tu);
        }
        else
        {
            m_changeSourceTmTuLo.add(p_tu);
        }
    }

    /**
     * Add a Tuv that is added to an existing Tu in the database.
     * 
     * @param p_tuId
     *            Existing Tu id in the database. Which Tm table this id belongs
     *            is known 'a priori'. This class hold only either of Page TM
     *            data or Segment TM data, not both.
     * @param p_tuv
     *            Tuv to be added to the database
     */
    public void addTuvForAdd(long p_tuId, BaseTmTuv p_tuv)
    {
        AddTuv addTuv = new AddTuv(p_tuId, p_tuv);

        if (p_tuv.isTranslatable())
        {
            m_tuvAddTrNum++;
            m_addTuvTr.add(addTuv);
        }
        else
        {
            m_tuvAddLoNum++;
            m_addTuvLo.add(addTuv);
        }
    }

    /**
     * Add a Tuv that updates an existing Tuv's modifyDate
     * 
     * @param p_tuId
     *            Existing Tuv's Tu's id
     * @param p_tuvId
     *            Existing Tuv's id in the database. Which Tm table this id
     *            belongs is known 'a priori'. This class hold only either of
     *            Page TM data or Segment TM data, not both.
     * @param p_tuv
     *            Tuv that has newer modifyDate
     */
    public void addTuvForUpdate(long p_tuId, long p_tuvId, BaseTmTuv p_tuv)
    {
        UpdateTuv updateTuv = new UpdateTuv(p_tuId, p_tuvId, p_tuv);

        if (p_tuv.isTranslatable())
        {
            m_updateTuvTr.add(updateTuv);
        }
        else
        {
            m_updateTuvLo.add(updateTuv);
        }
    }

    /**
     * Add all target Tuvs in a given Tu that are added to an existing Tu in the
     * database. This method should be used for Page Tm addition. If there is an
     * identical source segment in a Page Tm, all target segments are added to
     * the Tu. Previously existed target segments are deleted before adding the
     * new ones.
     * 
     * @param p_tuId
     *            Existing Tu id in the database. Which Tm table this id belongs
     *            is known 'a priori'. This class hold only either of Page TM
     *            data or Segment TM data, not both.
     * @param p_tu
     *            Tu whose target Tuvs are added to the database
     */
    public void addAllTargetTuvsForAdd(long p_tuId, BaseTmTu p_tu)
    {
        Iterator itLocale = p_tu.getAllTuvLocales().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();
            // skip the source locale segment
            if (locale.equals(m_sourceLocale))
            {
                continue;
            }

            Iterator itTuv = p_tu.getTuvList(locale).iterator();
            while (itTuv.hasNext())
            {
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();

                AddTuv addTuv = new AddTuv(p_tuId, tuv);

                if (tuv.isTranslatable())
                {
                    m_tuvAddTrNum++;
                    m_addTuvTr.add(addTuv);
                }
                else
                {
                    m_tuvAddLoNum++;
                    m_addTuvLo.add(addTuv);
                }
            }
        }
    }

    /**
     * Add a Tu that is removed from the database. All Tuvs this Tu owns are
     * also removed.
     * 
     * @param p_tu
     *            Tu to be removed from the database
     */
    public void addTuForRemove(BaseTmTu p_tu)
    {
        if (p_tu.isTranslatable())
        {
            m_removeTuTr.add(p_tu);
        }
        else
        {
            m_removeTuLo.add(p_tu);
        }
    }

    /**
     * Add a tuv of which an identical segment is already in the Tm. This method
     * should be used only for the Segment Tm for building corpus Tm.
     * 
     * @param p_tuId
     *            Existing Tuv's Tu's id
     * @param p_tuvId
     *            Existing Tuv's id in the Tm.
     * @param p_tuv
     *            Tuv from translation_unit_variant table.
     */
    public void addTuvForNoOp(long p_tuId, long p_tuvId, BaseTmTuv p_tuv)
    {
        if (p_tuv.isTranslatable())
        {
            m_noOpTuvs.add(new UpdateTuv(p_tuId, p_tuvId, p_tuv));
        }
    }

    public long getCreateTuNum(boolean p_translatable)
    {
        return p_translatable ? m_tuTrNum : m_tuLoNum;
    }

    public long getCreateTuvNum(boolean p_translatable)
    {
        return p_translatable ? m_tuvCreateTrNum : m_tuvCreateLoNum;
    }

    public long getAddTuvNum(boolean p_translatable)
    {
        return p_translatable ? m_tuvAddTrNum : m_tuvAddLoNum;
    }

    // returns a list of CreateTu (inner class) to create
    public Collection getTusForCreate(boolean p_translatable)
    {
        return p_translatable ? m_createTuTr : m_createTuLo;
    }

    // returns a list of BaseTmTu to change its source tm name
    public Collection getTusForChangeSourceTm(boolean p_translatable)
    {
        return p_translatable ? m_changeSourceTmTuTr : m_changeSourceTmTuLo;
    }

    // returns a list of AddTuvs (inner class) to add
    public Collection getTuvsForAdd(boolean p_translatable)
    {
        return p_translatable ? m_addTuvTr : m_addTuvLo;
    }

    // returns a list of UpdateTuvs (inner class) for update
    public Collection getTuvsForUpdate(boolean p_translatable)
    {
        return p_translatable ? m_updateTuvTr : m_updateTuvLo;
    }

    // returns a list of BaseTmTus to be removed from the database
    public Collection getTusForRemove(boolean p_translatable)
    {
        return p_translatable ? m_removeTuTr : m_removeTuLo;
    }

    // returns a list of UpdateTuvs (inner class) for non operational tuvs
    public Collection getTuvsForNoOp()
    {
        return m_noOpTuvs;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public long getTmId()
    {
        return m_tmId;
    }

    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        Iterator it;

        sb.append("SegmentsForSave  {\n");
        sb.append("  Source locale: ").append(m_sourceLocale.toString())
                .append("\n");
        sb.append("  Tm id: ").append(m_tmId).append("\n");

        if (m_removeTuTr.size() > 0)
        {
            sb.append("  m_removeTuTr  {\n");
            it = m_removeTuTr.iterator();
            while (it.hasNext())
            {
                BaseTmTu tu = (BaseTmTu) it.next();
                sb.append(tu.toDebugString(true));
            }
            sb.append("  }\n");
        }

        if (m_removeTuLo.size() > 0)
        {
            sb.append("  m_removeTuLo  {\n");
            it = m_removeTuLo.iterator();
            while (it.hasNext())
            {
                BaseTmTu tu = (BaseTmTu) it.next();
                sb.append(tu.toDebugString(true));
            }
            sb.append("  }\n");
        }

        if (m_createTuTr.size() > 0)
        {
            sb.append("  m_createTuTr  {\n");
            it = m_createTuTr.iterator();
            while (it.hasNext())
            {
                CreateTu createTu = (CreateTu) it.next();
                sb.append(createTu.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_createTuLo.size() > 0)
        {
            sb.append("  m_createTuLo  {\n");
            it = m_createTuLo.iterator();
            while (it.hasNext())
            {
                CreateTu createTu = (CreateTu) it.next();
                sb.append(createTu.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_addTuvTr.size() > 0)
        {
            sb.append("  m_addTuvTr  {\n");
            it = m_addTuvTr.iterator();
            while (it.hasNext())
            {
                AddTuv addTuv = (AddTuv) it.next();
                sb.append(addTuv.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_addTuvLo.size() > 0)
        {
            sb.append("  m_addTuvLo  {\n");
            it = m_addTuvLo.iterator();
            while (it.hasNext())
            {
                AddTuv addTuv = (AddTuv) it.next();
                sb.append(addTuv.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_updateTuvTr.size() > 0)
        {
            sb.append("  m_updateTuvTr  {\n");
            it = m_updateTuvTr.iterator();
            while (it.hasNext())
            {
                UpdateTuv updateTuv = (UpdateTuv) it.next();
                sb.append(updateTuv.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_updateTuvLo.size() > 0)
        {
            sb.append("  m_updateTuvLo  {\n");
            it = m_updateTuvLo.iterator();
            while (it.hasNext())
            {
                UpdateTuv updateTuv = (UpdateTuv) it.next();
                sb.append(updateTuv.toDebugString());
            }
            sb.append("  }\n");
        }

        if (m_noOpTuvs.size() > 0)
        {
            sb.append("  m_noOpTuvs  {\n");
            it = m_noOpTuvs.iterator();
            while (it.hasNext())
            {
                UpdateTuv updateTuv = (UpdateTuv) it.next();
                sb.append(updateTuv.toDebugString());
            }
            sb.append("  }\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    // Utility class to associate a Tuv and a Tu id to add
    public static class AddTuv
    {
        // Tu id to add
        private long m_tuId = 0;
        private BaseTmTuv m_tuv = null;
        private long m_newTuvId = 0; // newly assigned tuv id in the TM

        public AddTuv(long p_tuId, BaseTmTuv p_tuv)
        {
            m_tuId = p_tuId;
            m_tuv = p_tuv;
        }

        public long getTuIdToAdd()
        {
            return m_tuId;
        }

        public void setTuIdToAdd(long p_tuId)
        {
            m_tuId = p_tuId;
        }

        public BaseTmTuv getTuv()
        {
            return m_tuv;
        }

        public long getNewTuvId()
        {
            return m_newTuvId;
        }

        public void setNewTuvId(long p_newTuvId)
        {
            m_newTuvId = p_newTuvId;
        }

        public String toDebugString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("    AddTuv  {\n");
            sb.append("      Tm Tu id: ").append(m_tuId).append("\n");
            sb.append("      Tm Tuv id: ").append(m_newTuvId).append("\n");
            sb.append("      ").append(m_tuv.toDebugString());
            sb.append("    }\n");
            return sb.toString();
        }
    }

    // Utility class to associate a Tuv and a Tuv id to update ModifyDate
    public class UpdateTuv
    {
        private long m_tuId = 0;
        // Tuv id to update
        private long m_tuvId = 0;
        private BaseTmTuv m_tuv = null;

        private UpdateTuv(long p_tuId, long p_tuvId, BaseTmTuv p_tuv)
        {
            m_tuId = p_tuId;
            m_tuvId = p_tuvId;
            m_tuv = p_tuv;
        }

        public long getTuId()
        {
            return m_tuId;
        }

        public long getTuvIdToUpdate()
        {
            return m_tuvId;
        }

        public BaseTmTuv getTuv()
        {
            return m_tuv;
        }

        public String toDebugString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("    UpdateTuv  {\n");
            sb.append("      Tm Tuv id: ").append(m_tuvId).append("\n");
            sb.append("      ").append(m_tuv.toDebugString());
            sb.append("    }\n");
            return sb.toString();
        }
    }

    // Utility class to hold Tu and its Tuvs for creation
    public class CreateTu
    {
        private BaseTmTu m_tu = null;
        private long m_newTuId = 0; // new Tu id assigned to m_tu
        private List m_tuvs = new ArrayList(); // list of AddTuv

        private CreateTu(BaseTmTu p_tu)
        {
            m_tu = p_tu;

            Iterator itLocale = m_tu.getAllTuvLocales().iterator();
            while (itLocale.hasNext())
            {
                GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();

                Iterator itTuv = m_tu.getTuvList(locale).iterator();
                while (itTuv.hasNext())
                {
                    BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
                    m_tuvs.add(new AddTuv(0, tuv));
                }
            }
        }

        public BaseTmTu getTu()
        {
            return m_tu;
        }

        public long getNewTuId()
        {
            return m_newTuId;
        }

        public void setNewTuId(long p_newTuId)
        {
            m_newTuId = p_newTuId;

            Iterator it = m_tuvs.iterator();
            while (it.hasNext())
            {
                AddTuv addTuv = (AddTuv) it.next();
                addTuv.setTuIdToAdd(p_newTuId);
            }
        }

        public Iterator getAddTuvIterator()
        {
            return m_tuvs.iterator();
        }

        public String toDebugString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("    CreateTuv  {\n");
            sb.append("      Tm Tu id: ").append(m_newTuId).append("\n");
            sb.append("      ").append(m_tu.toDebugString(false));
            Iterator it = m_tuvs.iterator();
            while (it.hasNext())
            {
                AddTuv addTuv = (AddTuv) it.next();
                sb.append(addTuv.toDebugString());
            }
            sb.append("    }\n");
            return sb.toString();
        }
    }

}
