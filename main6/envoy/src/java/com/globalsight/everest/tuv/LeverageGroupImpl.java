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

package com.globalsight.everest.tuv;

//
// globalsight imports
//
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * LeverageGroupImpl implements the LeverageGroup interface. It represents a
 * group of translation units that are to be leveraged together. The group are
 * related in a way that a leverage hit within the group is of higher quality
 * than a leverage hit outside the group.
 */
public final class LeverageGroupImpl extends PersistentObject implements
        LeverageGroup, Serializable
{
    private static final long serialVersionUID = 4602235168566799135L;
    private List<Tu> m_tus = new ArrayList<Tu>(100);
    private List m_sourcepage = new ArrayList();

    public LeverageGroupImpl()
    {
    }

    /**
     * Get LeverageGroup unique identifier.
     *
     * @return unique identifier.
     */
    @Override
    public long getLeverageGroupId()
    {
        return getId();
    }

    /**
     * Add a Tu to the LeverageGoup. This should only be used when create job
     * for maintaining "belong to" relationship.
     *
     * @param p_tu
     *            -- Tu to add.
     */
    @Override
    public void addTu(Tu p_tu)
    {
        m_tus.add(p_tu);
        p_tu.setLeverageGroupId(this.getId());
        p_tu.setLeverageGroup(this);
    }

    /**
     * Get a collection of Tus for this leverage group.
     *
     * @return A collection of Tus.
     */
    @Override
    public Collection<Tu> getTus()
    {
        boolean loadFromDb = true;
        return getTus(loadFromDb);
    }

    @Override
    public Collection<Tu> getTus(boolean loadFromDb)
    {
        if ((m_tus == null || m_tus.size() == 0) && loadFromDb)
        {
            Connection connection = null;
            try
            {
                connection = DbUtil.getConnection();
                m_tus = new ArrayList<Tu>(
                        SegmentTuUtil.getTusByLeverageGroupId(connection,
                                getId()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                DbUtil.silentReturnConnection(connection);
            }
        }

        return m_tus;
    }

    /**
     * Return a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString()
    {
        return super.toString() + " m_tus.size()="
                + (m_tus != null ? Integer.toString(m_tus.size()) : "null");
    }

    /*
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a LeverageGroup is
     * serialized - the associated tus may not be available (because they
     * haven't been queried yet). Overwriting the method forces the query to
     * happen so when it is serialized all pieces of the object are serialized
     * and availble to the client.
     */
    protected void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException
    {
        // touch tus - since they are set up to only
        // populate when needed
        m_tus.size();

        // call the default writeObject
        out.defaultWriteObject();
    }

    // For Hibernate
//    public void setTusSet(List p_tus)
//    {
//        this.m_tus = p_tus;
//    }

//    public List getTusSet()
//    {
//        return m_tus;
//    }

    public void setSourcePageSet(List p_tus)
    {
        this.m_sourcepage = p_tus;
    }

    public List getSourcePageSet()
    {
        return m_sourcepage;
    }
}
