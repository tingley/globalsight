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
package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.DistributedId;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

/**
 * Class that manages the table space for a given TM. Since different TM types
 * may have different underlying table structures, we must abstract the table
 * access through some intermediate mechanism.
 * 
 * The TM3StorageInfo object is used to both create and access the tables for a
 * given TM.
 */
abstract class StorageInfo<T extends TM3Data>
{
    private TM3TmType type;
    private long id;
    private BaseTm<T> tm;
    private Set<TM3Attribute> inlineAttributes = new HashSet<TM3Attribute>();
    private DistributedId tuIds, tuvIds;

    static final int MAX_ATTR_VALUE_LEN = 256;

    static final String TU_TABLE_NAME = "TM3_TU";
    static final String TUV_TABLE_NAME = "TM3_TUV";
    static final String TUV_EXT_TABLE_NAME = "TM3_TUV_EXT";
    static final String INDEX_TABLE_NAME = "TM3_INDEX";
    static final String ATTR_VAL_TABLE_NAME = "TM3_ATTR_VAL";

    protected StorageInfo(BaseTm<T> tm, TM3TmType type)
    {
        this.id = tm.getId();
        this.type = type;
        this.tm = tm;
        for (TM3Attribute attr : tm.getAttributes())
        {
            if (attr.isInline())
            {
                inlineAttributes.add(attr);
            }
        }
    }

    long getId()
    {
        return id;
    }

    public TM3Manager getManager()
    {
        return tm.getManager();
    }

    BaseTm<T> getTm()
    {
        return tm;
    }

    void setTm(BaseTm<T> tm)
    {
        this.tm = tm;
    }

    protected Set<TM3Attribute> getInlineAttributes()
    {
        return inlineAttributes;
    }

    String getTuTableName()
    {
        return tm.getTuTableName();
    }

    String getTuvTableName()
    {
        return tm.getTuvTableName();
    }

    String getTuvExtTableName()
    {
        return tm.getTuvExtTableName();
    }

    String getFuzzyIndexTableName()
    {
        return tm.getFuzzyIndexTableName();
    }

    String getAttrValTableName()
    {
        return tm.getAttrValTableName();
    }

    public TM3TmType getType()
    {
        return type;
    }

    /**
     * Initialize the tablespace defined by this object. This requires an open
     * JDBC connection.
     * 
     * @param conn
     *            JDBC connection
     * @throws SQLException
     */
    public void create() throws SQLException
    {
        // Initialize table names
        initializeTm();

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            // Create TU and TUV storage
            createTuStorage(conn);
            // Create Fuzzy Index
            createFuzzyIndex(conn);
            // Other tables
            createAttrTable(conn);

            conn.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Remove the tablespace defined by this object. This requires an open JDBC
     * connection.
     * 
     * @param conn
     *            JDBC connection
     * @throws SQLException
     */
    public void destroy() throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            destroyAttrTable(conn);
            destroyFuzzyIndex(conn);
            destroyTuStorage(conn);
            conn.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    protected DistributedId getTuIds()
    {
        if (tuIds == null)
        {
            tuIds = new DistributedId(getTuTableName());
        }
        return tuIds;
    }

    protected DistributedId getTuvIds()
    {
        if (tuvIds == null)
        {
            tuvIds = new DistributedId(getTuvTableName());
        }
        return tuvIds;
    }

    // XXX Should this use its own connection?
    long getTuId(Connection conn) throws SQLException
    {
        return getTuIds().getId(conn);
    }

    long getTuvId(Connection conn) throws SQLException
    {
        return getTuvIds().getId(conn);
    }

    /**
     * Add a set of INNER JOIN clauses to filter a statement by an arbitrary
     * number of attributes.
     * 
     * @param sb
     * @param tuAlias
     *            alias of the TU table to join against
     * @param attrs
     * @return
     */
    StatementBuilder attributeJoinFilter(StatementBuilder sb, String tuIdAlias,
            Map<TM3Attribute, String> attrs)
    {
        int i = 0;
        for (Map.Entry<TM3Attribute, String> attr : attrs.entrySet())
        {
            if (attr.getKey() == null)
            {
                throw new IllegalArgumentException(
                        "null TM3Attribute in attributes map");
            }
            String alias = "attr" + i++;
            sb.append(" INNER JOIN ").append(getAttrValTableName())
                    .append(" AS ").append(alias).append(" ON ")
                    .append(tuIdAlias).append(" = ").append(alias)
                    .append(".tuId AND ").append(alias)
                    .append(".attrId = ? AND ").append(alias)
                    .append(".value = ?")
                    .addValues(attr.getKey().getId(), attr.getValue());
        }
        return sb;
    }

    /**
     * Get the interface to this TM's fuzzy matching index.
     */
    abstract FuzzyIndex<T> getFuzzyIndex();

    /**
     * Get the interface to this TM's TU storage.
     */
    abstract TuStorage<T> getTuStorage();

    /**
     * Initialize the table names for the given TM. This should only be called
     * once, when the TM is first created.
     * 
     * @param tm
     */
    protected abstract void initializeTm();

    protected abstract void createTuStorage(Connection conn)
            throws SQLException;

    protected abstract void destroyTuStorage(Connection conn)
            throws SQLException;

    protected abstract void createFuzzyIndex(Connection conn)
            throws SQLException;

    protected abstract void destroyFuzzyIndex(Connection conn)
            throws SQLException;

    protected abstract void createAttrTable(Connection conn)
            throws SQLException;

    protected abstract void destroyAttrTable(Connection conn)
            throws SQLException;

	protected static String getTableName(long id, String baseName)
    {
        return baseName + "_" + id;
    }
}
