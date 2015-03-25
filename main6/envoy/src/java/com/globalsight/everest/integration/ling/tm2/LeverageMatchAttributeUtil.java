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
package com.globalsight.everest.integration.ling.tm2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.ling.tm2.persistence.DbUtil;

public class LeverageMatchAttributeUtil extends SegmentTuTuvCacheManager
		implements TuvQueryConstants
{
	static private final Logger logger = Logger
			.getLogger(LeverageMatchAttributeUtil.class);

    private static final String SAVE_LM_ATTR_SQL = "INSERT INTO "
    		+ LM_ATTR_TABLE_PLACEHOLDER
    		+ "(source_page_id, original_source_tuv_id, sub_id, target_locale_id, order_num, "
    		+ " name, varchar_value, text_value, long_value, date_value) "
    		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    private static final String QUERY_LM_ATTR_SQL = "SELECT "
    		+ "id, source_page_id, original_source_tuv_id, sub_id, target_locale_id, order_num, "
    		+ "name, varchar_value, text_value, long_value, date_value "
    		+ "FROM " + LM_ATTR_TABLE_PLACEHOLDER + " "
    		+ "WHERE source_page_id = ? "
    		+ "AND target_locale_id = ?;";

    /**
	 * Save leverage match attributes into DB store.
	 * 
	 * @param p_connection
	 * @param p_attibutes -- List<LeverageMatchAttribute>
	 * @param p_jobId
	 * @throws Exception
	 */
	public static void saveLeverageMatchAttributes(Connection p_connection,
			List<LeverageMatchAttribute> p_attibutes, long p_jobId)
			throws Exception
	{
        PreparedStatement ps = null;
		String attributeTable = null;

        try
        {
			attributeTable = BigTableUtil.getLMAttributeTableByJobId(p_jobId);
			String sql = SAVE_LM_ATTR_SQL.replace(
					LM_ATTR_TABLE_PLACEHOLDER, attributeTable);
            ps = p_connection.prepareStatement(sql);

            int batchUpdate = 0;
            for (LeverageMatchAttribute attribute : p_attibutes)
            {
                ps.setLong(1, attribute.getSourcePageId());
                ps.setLong(2, attribute.getOriginalSourceTuvId());
                ps.setString(3, attribute.getSubid());
                ps.setLong(4, attribute.getTargetLocaleId());
                ps.setInt(5, attribute.getOrderNum());
                ps.setString(6, attribute.getName());
                ps.setString(7, attribute.getVarcharValue());
                ps.setString(8, attribute.getTextValue());
                ps.setLong(9, attribute.getLongValue());
                if (attribute.getDateValue() != null) {
					ps.setTimestamp(10, new java.sql.Timestamp(attribute
							.getDateValue().getTime()));
				} else {
	            	ps.setTimestamp(10, null);
				}

                ps.addBatch();
                batchUpdate++;

                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    batchUpdate = 0;
                }
            }

            // execute the rest of the added batch
            if (batchUpdate > 0)
            {
                ps.executeBatch();
            }
            p_connection.commit();
        }
        catch (Exception e)
        {
        	logger.error("Error when save leverage match attributes into table '"
					+ attributeTable + "'.", e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
	}


	/**
	 * Query leverage match attributes by source page ID and target locale ID.
	 * 
	 * @param p_sourcePageId
	 * @param p_targetLocaleId
	 * @return -- List<LeverageMatchAttribute>
	 * @throws Exception
	 */
	public static List<LeverageMatchAttribute> getLeverageMatchAttrbutes(
			long p_sourcePageId, long p_targetLocaleId) throws Exception
    {
		List<LeverageMatchAttribute> result = new ArrayList<LeverageMatchAttribute>();

		Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
			String attrTable = BigTableUtil.getLMAttributeTableBySourcePageId(p_sourcePageId);
			String sql = QUERY_LM_ATTR_SQL.replace(
					LM_ATTR_TABLE_PLACEHOLDER, attrTable);
            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            ps.setLong(2, p_targetLocaleId);
            rs = ps.executeQuery();
            while (rs != null && rs.next())
            {
       		    long id = rs.getLong("id");

       		    long srcPageId = rs.getLong("source_page_id");
       		    long oriSrcTuvId = rs.getLong("original_source_tuv_id");
       		    String subId = rs.getString("sub_id");
       		    long trgLocaleId = rs.getLong("target_locale_id");
       		    short orderNum = rs.getShort("order_num");

       		    String name = rs.getString("name");
                String varcharValue = rs.getString("varchar_value");
                String textValue = rs.getString("text_value");
                long longValue = rs.getLong("long_value");
                Date dateValue = rs.getTimestamp("date_value");

				LeverageMatchAttribute attr = new LeverageMatchAttribute(
						srcPageId, oriSrcTuvId, subId, orderNum, trgLocaleId);
                attr.setId(id);
                attr.setName(name);
                attr.setVarcharValue(varcharValue);
                attr.setTextValue(textValue);
                attr.setLongValue(longValue);
                attr.setDateValue(dateValue);

                result.add(attr);
            }
        }
        catch (Exception e)
        {
			logger.error(
					"Error when getLeverageMatchAttrbutes for sourcePageId: "
							+ p_sourcePageId + " and targetLocaleId: "
							+ p_targetLocaleId, e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

	/**
	 * Delete leverage match attribute.
	 * 
	 * @param p_OriginalSourceTuvId
	 * @param p_subId
	 * @param p_targetLocaleId
	 * @param p_orderNum
	 * @param p_jobId
	 * @throws Exception
	 */
	public static void deleteLMAttributes(Long p_OriginalSourceTuvId,
			String p_subId, Long p_targetLocaleId, Long p_orderNum, long p_jobId)
			throws Exception
	{
		Connection connection = null;
		PreparedStatement ps = null;

		try
		{
			connection = DbUtil.getConnection();
			connection.setAutoCommit(false);

			String attrTable = BigTableUtil.getLMAttributeTableByJobId(p_jobId);

			StringBuffer buffer = new StringBuffer();
			buffer.append("DELETE FROM ").append(LM_ATTR_TABLE_PLACEHOLDER);
			buffer.append(" WHERE original_source_tuv_id = ? ");
			buffer.append(" AND sub_id = ? ");
			buffer.append(" AND target_locale_id = ? ");
			buffer.append(" AND order_num = ? ");
			String sql = buffer.toString().replace(LM_ATTR_TABLE_PLACEHOLDER,
					attrTable);
			ps = connection.prepareStatement(sql);
			ps.setLong(1, p_OriginalSourceTuvId);
			ps.setString(2, p_subId);
			ps.setLong(3, p_targetLocaleId);
			ps.setLong(4, p_orderNum);

			ps.executeUpdate();

			connection.commit();
		}
		catch (Exception ex)
		{
			logger.error(
					"Failed to delete leverage matche attributes by originalSourceTuvID : "
							+ p_OriginalSourceTuvId, ex);
			throw ex;
		}
		finally
		{
			DbUtil.silentClose(ps);
			DbUtil.silentReturnConnection(connection);
		}
	}
}
