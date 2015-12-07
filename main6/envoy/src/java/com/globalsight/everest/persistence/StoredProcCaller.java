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
package com.globalsight.everest.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class StoredProcCaller implements TuvQueryConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(StoredProcCaller.class);

    // maximum element number ARRAY can carry is 4095, not 4096.
    private static int MAX_ELEM = 4095;

    private static final String s_STRING_TERMINATOR = "";

    public static final String LGEM_SP = "lev_match.find_lgem_match";

    public static final String TARGET_TERM = "tmlev_match.find_target_terms";

    private static final String CREATE_TEMPORARY_TABLE_LEV_MATCH = " create temporary table tmp_lev_match ( "
            + "                  orig_src_id int, "
            + "                  match_tgt_id int )";

    private static final String DROP_TABLE_LEV_MATCH = "DROP TABLE IF EXISTS tmp_lev_match";

    /**
     * Procedure of "lev_match.find_lgem_match"
     */
    private static String FIND_LGEM_MATCH_SQL = " SELECT lev.orig_src_id as orig_src_id, src.id as match_src_id, "
            + "        1 as fuzzy_score, src.segment_clob as src_segment_clob, "
            + "        src.segment_string as src_segment_string, "
            + "        trg.id as match_tgt_id, trg.locale_id as locale_id, "
            + "        src.tu_id as tu_id, trg.segment_clob as segment_clob, "
            + "        trg.segment_string as segment_string, trg.state as state, "
            + "        tu.data_type as `format`, tu.tu_type as `type`, "
            + "        tu.localize_type as localize_type, trg.timestamp as `timestamp` "
            + " FROM   "
            + TUV_TABLE_PLACEHOLDER
            + " src, "
            + "        "
            + TUV_TABLE_PLACEHOLDER
            + " trg, "
            + "        "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "        tmp_lev_match lev "
            + " WHERE     lev.match_tgt_id = trg.id "
            + "       AND src.locale_id = :src_loc_id "
            + "       AND trg.tu_id = src.tu_id "
            + "       AND tu.id = trg.tu_id "
            + "       AND tu.id = src.tu_id "
            + " ORDER BY lev.orig_src_id ";

    private static String SELECT_SQL = " SELECT MIN(trg.id) FROM "
            + TUV_TABLE_PLACEHOLDER + " src, "
    		+ TUV_TABLE_PLACEHOLDER + " trg, "
            + TU_TABLE_PLACEHOLDER + " tu "
            + " WHERE tu.leverage_group_id IN (:lev_grp_ids)"
            + " AND tu.localize_type = :src_type "
            + " AND src.locale_id = :src_loc_id "
            + " AND src.exact_match_key = :src_crc "
            + " AND trg.locale_id IN (:loc_list) "
            + " AND trg.state in ('LOCALIZED', 'UNVERIFIED_EXACT_MATCH') "
            + " AND src.tu_id = tu.id "
            + " AND trg.tu_id = tu.id "
            + " AND src.tu_id = trg.tu_id "
            + " AND src.id = :src_tuv_id ";

    private static String INSERT_SQL = "INSERT INTO tmp_lev_match(orig_src_id, match_tgt_id) values (?, ?)";

    /**
     * Re-implement procedure "lev_match.find_lgem_match" by Java code.
     * 
     * @param p_connection
     * @param p_numberArgs
     * @param p_stringArgs
     * @return
     * @throws PersistenceException
     */
    public static ResultSet findReimportMatches(Connection p_connection,
            Vector p_numberArgs, Vector p_stringArgs, long p_jobId)
            throws PersistenceException
    {
        Collection vectors = splitParamForReimport(p_numberArgs);
        Iterator m_paramIt = vectors.iterator();

        while (m_paramIt.hasNext())
        {
            long time_PERFORMANCE = 0;

            if (CATEGORY.isDebugEnabled())
            {
                time_PERFORMANCE = System.currentTimeMillis();
            }

            ResultSet result = excuteProcFindLgemMatch(p_connection,
                    (Vector) vectors.iterator().next(), p_stringArgs, p_jobId);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Performance:: TM leveraging query for "
                        + LGEM_SP + " time="
                        + (System.currentTimeMillis() - time_PERFORMANCE));
            }

            return result;
        }

        return null;
    }

    private static ResultSet excuteProcFindLgemMatch(Connection p_connection,
            Vector numberParams, Vector stringParams, long p_jobId)
            throws PersistenceException
    {
        Statement statement = null;
        ResultSet resultSet = null;
        int src_tuv_id = 0;
        int src_loc_id;
        int src_crc;
        char src_type;
        List loc_list;
        List lev_grp_ids;
        int num_array_ptr;
        int i_ptr;

        if (numberParams == null || stringParams == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (numberParams.size() < 8
                || getIntAt(numberParams, numberParams.size() - 1) != -1)
        {
            throw new PersistenceException(
                    "Invalid input array for source tuv's.");
        }

        try
        {
            statement = p_connection.createStatement();
            startProcLevMatch(statement);

            src_loc_id = getIntAt(numberParams, 0);

            lev_grp_ids = new ArrayList();
            for (num_array_ptr = 1, i_ptr = 0; getIntAt(numberParams,
                    num_array_ptr) != -1; i_ptr++, num_array_ptr++)
            {
                lev_grp_ids.add((Integer) numberParams.get(num_array_ptr));
            }

            num_array_ptr++;
            loc_list = new ArrayList();
            for (i_ptr = 0; getIntAt(numberParams, num_array_ptr) != -1; i_ptr++, num_array_ptr++)
            {
                loc_list.add((Integer) numberParams.get(num_array_ptr));
            }

            num_array_ptr++;

            PreparedStatement insertStatement = p_connection
                    .prepareStatement(INSERT_SQL);
            ResultSet values = null;

            String tuTableName = BigTableUtil
                    .getTuTableJobDataInByJobId(p_jobId);
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInByJobId(p_jobId);
            while (num_array_ptr < numberParams.size() - 1)
            {
                src_tuv_id = getIntAt(numberParams, num_array_ptr);
                if (getIntAt(numberParams, num_array_ptr + 1) == 1)
                {
                    src_type = 'T';
                }
                else
                {
                    src_type = 'L';
                }
                src_crc = getIntAt(numberParams, num_array_ptr + 2);
                if (getIntAt(numberParams, num_array_ptr + 3) != -1)
                {
                    throw new PersistenceException(
                            "Invalid input array for source tuv's.");
                }
                num_array_ptr = num_array_ptr + 4;

                String selectSql = SELECT_SQL.replaceAll(TU_TABLE_PLACEHOLDER,
                        tuTableName);
                selectSql = selectSql.replaceAll(TUV_TABLE_PLACEHOLDER,
                        tuvTableName);
                selectSql = selectSql.replaceAll(":src_tuv_id",
                        String.valueOf(src_tuv_id));
                selectSql = selectSql.replaceAll(":lev_grp_ids",
                        convertCollectionToSql(lev_grp_ids, null));
                selectSql = selectSql.replaceAll(":src_type",
                        String.valueOf(src_type));
                selectSql = selectSql.replaceAll(":src_loc_id",
                        String.valueOf(src_loc_id));
                selectSql = selectSql.replaceAll(":src_crc",
                        String.valueOf(src_crc));
                selectSql = selectSql.replaceAll(":loc_list",
                        convertCollectionToSql(loc_list, null));

                values = statement.executeQuery(selectSql);

                while (values.next())
                {
                    insertStatement.setLong(1, src_tuv_id);
                    insertStatement.setLong(2, values.getLong(1));

                    insertStatement.execute();
                }
            }

            if (values != null)
            {
                values.close();
            }

            if (insertStatement != null)
            {
                insertStatement.close();
            }

            String query = FIND_LGEM_MATCH_SQL.replaceAll(TU_TABLE_PLACEHOLDER,
                    tuTableName);
            query = query.replaceAll(TUV_TABLE_PLACEHOLDER, tuvTableName);
            query = query.replaceAll(":src_loc_id", String.valueOf(src_loc_id));
            resultSet = statement.executeQuery(query);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Excute procedure " + LGEM_SP
                    + " failed.", e);
        }

        return resultSet;
    }

    /**
     * Re-implement procedure "tmlev_match.find_target_terms" by Java code.
     * 
     * @param p_connection
     * @param p_numberArgs
     * @param p_stringArgs
     * @return
     * @throws PersistenceException
     */
    public static List findTargetTerms(Connection p_connection,
            Vector p_numberArgs, Vector p_stringArgs)
            throws PersistenceException
    {
        Collection vectors = splitParamForTermSelect(p_numberArgs);
        Iterator m_paramIt = vectors.iterator();

        while (m_paramIt.hasNext())
        {
            long time_PERFORMANCE = 0;

            if (CATEGORY.isDebugEnabled())
            {
                time_PERFORMANCE = System.currentTimeMillis();
            }

            List result = excuteProcFindTargetTerm(p_connection,
                    (Vector) vectors.iterator().next(), p_stringArgs);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Performance:: Term leveraging query spent "
                        + " time="
                        + (System.currentTimeMillis() - time_PERFORMANCE));
            }

            return result;
        }

        return null;
    }

    private static List excuteProcFindTargetTerm(Connection connection,
            Vector numberParams, Vector stringParams)
    {
        long tbid;
        List cid_list;
        List tgt_lang_list;
        int char_array_ptr;
        int i_ptr;

        if (numberParams == null || stringParams == null)
        {
            throw new PersistenceException(
                    "One or more of the input arrays is null.");
        }

        if (numberParams.isEmpty()
                || stringParams.isEmpty()
                || !s_STRING_TERMINATOR.equals((String) stringParams
                        .get(stringParams.size() - 1)))
        {
            throw new PersistenceException(
                    "Invalid input number and/or string array.");
        }

        try
        {
            tbid = Long.parseLong((String) stringParams.get(0));

            tgt_lang_list = new ArrayList();
            for (i_ptr = 0, char_array_ptr = 0; !s_STRING_TERMINATOR
                    .equals(stringParams.get(char_array_ptr)); i_ptr++, char_array_ptr++)
            {
                tgt_lang_list.add((String) stringParams.get(char_array_ptr));
            }

            if (tgt_lang_list.isEmpty())
            {
                throw new PersistenceException(
                        "Invalid input: target language name list is empty.");
            }

            cid_list = new ArrayList(numberParams);

            if (!cid_list.isEmpty())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("select tt from TbTerm tt where tt.language in (");
                sb.append(StoredProcCaller.convertCollectionToSql(
                        tgt_lang_list, "string"));
                sb.append(") and tt.tbid = ");
                sb.append(tbid);
                sb.append(" and tt.tbConcept.id in (");
                sb.append(StoredProcCaller.convertCollectionToSql(cid_list,
                        "integer"));
                sb.append(")");

                List list = HibernateUtil.search(sb.toString());
                return list;
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException("Excute procedure " + TARGET_TERM
                    + " failed.", e);
        }

        return null;
    }

    /**
     * When querying target terms, the number array contains only cids.
     * 
     * This method splits the number array into lists each having less than 4096
     * elements. [data part 1] [data part 2] [data part 3]
     */
    private static Vector splitParamForTermSelect(Vector p_param)
    {
        Vector result = new Vector();
        int size = p_param.size();

        // Small list, use as is.
        if (size <= MAX_ELEM)
        {
            result.add(p_param);
            return result;
        }

        // Long list, split into 4095 element chunks.
        for (int start = 0; start < size; start += MAX_ELEM)
        {
            Vector part = new Vector();

            for (int i = start, max = (start + Math.min(size - start, MAX_ELEM)); i < max; i++)
            {
                part.add(p_param.get(i));
            }

            result.add(part);
        }

        return result;
    }

    /**
     * Prepare for procedure FindLgemMatch. Create temporary table
     * tmp_lev_match.
     * 
     * @param connection
     * @param statement
     * @throws SQLException
     */
    private static void startProcLevMatch(Statement statement)
            throws SQLException
    {
        statement.execute(DROP_TABLE_LEV_MATCH);
        statement.execute(CREATE_TEMPORARY_TABLE_LEV_MATCH);
    }

    /**
     * Get number as simple int type which is at the index position in vector.
     * 
     * @param vector
     * @param index
     * @return
     */
    private static int getIntAt(Vector vector, int index)
    {
        return ((Integer) vector.get(index)).intValue();
    }

    /**
     * Convert list to string for using in sql as IN clause.
     * 
     * @param collection
     * @param type
     * @return
     */
    public static String convertCollectionToSql(Collection collection,
            String type)
    {
        if (collection.isEmpty())
        {
            // Empty string in sql statement.
            return "null";
        }

        Iterator iterator = new HashSet(collection).iterator();

        StringBuffer sql = new StringBuffer(getObject(iterator, type));
        while (iterator.hasNext())
        {
            sql.append(", ");
            sql.append(getObject(iterator));
        }

        return sql.toString();
    }

    private static String getObject(Iterator iterator)
    {
        return "'" + iterator.next() + "'";
    }

    private static String getObject(Iterator iterator, String type)
    {
        if (type == null)
        {
            return getObject(iterator);
        }

        if (type.equals("string"))
        {
            return "'" + iterator.next() + "'";
        }
        else if (type.equals("integer"))
        {
            return String.valueOf(iterator.next());
        }

        return String.valueOf(iterator.next());
    }

    private static Collection splitParamForReimport(Vector p_param)
    {
        int size = p_param.size();
        int idx = 0;

        // source locale id and leverage group id
        while (!isPreambleMarker(p_param.get(idx)))
        {
            idx++;
        }

        idx++;

        // target locale ids
        while (!isPreambleMarker(p_param.get(idx)))
        {
            idx++;
        }

        List suffixList = p_param.subList(0, ++idx);

        Collection list = splitParam(p_param.subList(idx, size), MAX_ELEM - idx);

        for (Iterator it = list.iterator(); it.hasNext();)
        {
            ((List) it.next()).addAll(0, suffixList);
        }

        return list;
    }

    /**
     * Splits the data part into sublists of size < 4096. Split points are
     * marked by the number -1 or the empty string.
     */
    private static Vector splitParam(List p_param, int arrayMax)
    {
        Vector result = new Vector();
        int size = p_param.size();
        int top = 0;
        int end = arrayMax;

        while (end < size)
        {
            // -1 for number, "" for strings
            while (!isSplitPoint(p_param.get(end - 1)))
            {
                end--;
            }

            result.add(new Vector(p_param.subList(top, end)));
            top = end;
            end = top + arrayMax;
        }

        result.add(new Vector(p_param.subList(top, size)));

        return result;
    }

    /** Marks split points in data with -1 or "" */
    private static boolean isSplitPoint(Object p_o)
    {
        boolean result = true;

        if (p_o instanceof Number)
        {
            result = ((Number) p_o).longValue() == -1;
        }
        else if (p_o instanceof String)
        {
            result = ((String) p_o).equals("");
        }

        return result;
    }

    /** Marks the preamble with -1 or "-1". */
    private static boolean isPreambleMarker(Object p_o)
    {
        boolean result = true;

        if (p_o instanceof Number)
        {
            result = (((Number) p_o).longValue() == -1);
        }
        else if (p_o instanceof String)
        {
            result = ((String) p_o).equals("-1");
        }

        return result;
    }

}
