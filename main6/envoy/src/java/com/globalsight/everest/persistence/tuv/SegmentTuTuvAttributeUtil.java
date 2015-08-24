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

package com.globalsight.everest.persistence.tuv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * Helper for "translation_tu_tuv_attr_[companyId]" storage.
 *
 * @author york.jin
 * @since 2015-03-06
 * @version 8.6.1
 */
public class SegmentTuTuvAttributeUtil extends SegmentTuTuvCacheManager
		implements TuvQueryConstants
{
	static private final Logger logger = Logger
			.getLogger(SegmentTuTuvAttributeUtil.class);

	private static final String SELECT_COLUMNS = "SELECT "
			+ " attr.id, attr.object_id, attr.object_type, attr.name, "
		    + " attr.varchar_value, attr.text_value, attr.long_value, attr.date_value FROM ";

    private static final String GET_ATTR_BY_TUVID_SQL = SELECT_COLUMNS
    		+ TU_TUV_ATTR_TABLE_PLACEHOLDER + " attr "
    		+ " WHERE attr.object_type = ? "
    		+ " AND attr.name = ? "
    		+ " AND attr.object_id IN (_OBJECT_ID_PLACEHOLDER_) ";

    private static final String SAVE_ATTR_SQL = "INSERT INTO "
            + TU_TUV_ATTR_TABLE_PLACEHOLDER
            + " ("
            + "object_id, object_type, name, varchar_value, text_value, long_value, date_value) "
            + "values (?, ?, ?, ?, ?, ?, ?)";

    private static final String DEL_ATTR_BY_TUVID_SQL = "DELETE FROM "
    		+ TU_TUV_ATTR_TABLE_PLACEHOLDER
    		+ " WHERE object_type = ? "
    		+ " AND name = ? "
    		+ " AND object_id IN (_OBJECT_ID_PLACEHOLDER_) ";

	/**
	 * Return all SID attributes for specified TUV Ids.
	 * 
	 * @param p_tuvIds
	 * @param p_objectType
	 *            -- "TU"|"TUV"
	 * @param p_jobId
	 * @return List<TuTuvAttributeImpl>
	 * @throws Exception
	 */
	public static List<TuTuvAttributeImpl> getSidAttributesByTuvIds(
			List<Long> p_tuvIds, long p_jobId)
			throws Exception
	{
		return queryAttributes(p_tuvIds, TuTuvAttributeImpl.OBJECT_TYPE_TUV,
				TuTuvAttributeImpl.SID, p_jobId);
	}
	
	public static List<TuTuvAttributeImpl> getStateAttributesByTuvIds(
			List<Long> p_tuvIds, long p_jobId)
			throws Exception
	{
		return queryAttributes(p_tuvIds, TuTuvAttributeImpl.OBJECT_TYPE_TUV,
				TuTuvAttributeImpl.STATE, p_jobId);
	}

	/**
	 * Query attributes.
	 * 
	 * @param p_objectIdList
	 * @param p_objectType
	 *            -- TuTuvAttributeImpl.TU|TuTuvAttributeImpl.TUV
	 * @param p_name
	 *            -- for column "name", i.e. TuTuvAttributeImpl.NAME_SID
	 * @param p_jobId
	 * @return
	 * @throws Exception
	 */
	private static List<TuTuvAttributeImpl> queryAttributes(
			List<Long> p_objectIdList, String p_objectType, String p_name,
			long p_jobId) throws Exception
    {
        List<TuTuvAttributeImpl> result = new ArrayList<TuTuvAttributeImpl>();
		if (p_objectIdList == null || p_objectIdList.size() == 0)
			return result;

		String objectIdsStr = null;
		Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
			String attrTable = BigTableUtil
					.getTuTuvAttributeTableByJobId(p_jobId);
			objectIdsStr = getObjectIdsClause(p_objectIdList);
			String sql = GET_ATTR_BY_TUVID_SQL.replace(
					TU_TUV_ATTR_TABLE_PLACEHOLDER, attrTable).replace(
					"_OBJECT_ID_PLACEHOLDER_", objectIdsStr);
            ps = conn.prepareStatement(sql);
            ps.setString(1, p_objectType);
            ps.setString(2, p_name);
            rs = ps.executeQuery();
            while (rs != null && rs.next())
            {
            	TuTuvAttributeImpl attr = new TuTuvAttributeImpl();

       		    long id = rs.getLong("id");
       		    long objectId = rs.getLong("object_id");
       		    String objectType = rs.getString("object_type");
                String name = rs.getString("name");
                String varcharValue = rs.getString("varchar_value");
                String textValue = rs.getString("text_value");
                long longValue = rs.getLong("long_value");
                Date dateValue = rs.getTimestamp("date_value");

                attr.setId(id);
                attr.setObjectId(objectId);
                attr.setObjectType(objectType);
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
            logger.error("Error when getAttributesByTuvIds: " + objectIdsStr, e);
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
	 * Save TU/TUV attributes.
	 * 
	 * @param p_connection
	 * @param p_attibutes
	 *            -- List<TuTuvAttributeImpl>
	 * @param p_jobId
	 * @throws Exception
	 */
	public static void saveTuTuvAttributes(Connection p_connection,
			List<TuTuvAttributeImpl> p_attibutes, long p_jobId)
			throws Exception
	{
        PreparedStatement ps = null;
		String attributeTable = null;

        try
        {
			attributeTable = BigTableUtil
					.getTuTuvAttributeTableByJobId(p_jobId);
			String sql = SAVE_ATTR_SQL.replace(
					TU_TUV_ATTR_TABLE_PLACEHOLDER, attributeTable);
            ps = p_connection.prepareStatement(sql);

            int batchUpdate = 0;
            for (TuTuvAttributeImpl attribute : p_attibutes)
            {
                ps.setLong(1, attribute.getObjectId());
                ps.setString(2, attribute.getObjectType());
                ps.setString(3, attribute.getName());
                ps.setString(4, attribute.getVarcharValue());
                ps.setString(5, attribute.getTextValue());
                ps.setLong(6, attribute.getLongValue());
				if (attribute.getDateValue() != null) {
					ps.setTimestamp(7, new java.sql.Timestamp(attribute
							.getDateValue().getTime()));
				} else {
	            	ps.setTimestamp(7, null);
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
			logger.error("Error when save tu/tuv attributes into table '"
					+ attributeTable + "'.", e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
	}

	/**
	 * For SID attribute, its value is in "value_text" column, and it's a TUV
	 * level attribute.
	 * 
	 * @param p_connection
	 * @param sidAttibutes -- List<TuTuvAttributeImpl>
	 * @param p_jobId
	 * @throws Exception 
	 */
	public static void updateSidAttributes(Connection p_connection,
			List<TuTuvAttributeImpl> sidAttibutes, long p_jobId)
			throws Exception
	{
		updateTuTuvAttributes(p_connection, sidAttibutes,
				TuTuvAttributeImpl.OBJECT_TYPE_TUV,
				TuTuvAttributeImpl.SID, p_jobId);
	}

	/**
	 * Update attributes
	 * 
	 * @param p_connection
	 * @param attibutes
	 *            -- List<TuTuvAttributeImpl>
	 * @param p_objectType
	 *            -- TuTuvAttributeImpl.TU|TuTuvAttributeImpl.TUV
	 * @param p_name
	 *            -- for column "name", i.e. TuTuvAttributeImpl.NAME_SID
	 * @param p_jobId
	 * @throws Exception
	 */
	private static void updateTuTuvAttributes(Connection p_connection,
			List<TuTuvAttributeImpl> attibutes, String p_objectType,
			String p_name, long p_jobId) throws Exception
	{
		PreparedStatement ps = null;
		String attributeTable = null;

		try
		{
			// Delete attributes by object IDs first.
			attributeTable = BigTableUtil
					.getTuTuvAttributeTableByJobId(p_jobId);
			StringBuffer objectIdsBuf = new StringBuffer();
			for (TuTuvAttributeImpl attr : attibutes)
			{
				objectIdsBuf.append(attr.getObjectId()).append(",");
			}
			String objectIdsClause = objectIdsBuf
					.substring(0, objectIdsBuf.length() - 1);

			String sql = DEL_ATTR_BY_TUVID_SQL.replace(
					TU_TUV_ATTR_TABLE_PLACEHOLDER, attributeTable).replace(
					"_OBJECT_ID_PLACEHOLDER_", objectIdsClause);
			ps = p_connection.prepareStatement(sql);
			ps.setString(1, p_objectType);
			ps.setString(2, p_name);
			ps.execute();
			p_connection.commit();

			// Save new values.
			saveTuTuvAttributes(p_connection, attibutes, p_jobId);
		}
		catch (Exception e)
		{
			logger.error("Error when update tu/tuv attributes into table '"
					+ attributeTable + "'.", e);
			throw e;
		}
		finally
		{
			DbUtil.silentClose(ps);
		}
	}
	
	public static void deleteStateAttributes(Connection p_connection,
			List<TuTuvAttributeImpl> sidAttibutes, long p_jobId)
			throws Exception
	{
		deleteTuTuvAttributes(p_connection, sidAttibutes,
				TuTuvAttributeImpl.OBJECT_TYPE_TUV,
				TuTuvAttributeImpl.STATE, p_jobId);
	}
	
	private static void deleteTuTuvAttributes(Connection p_connection,
			List<TuTuvAttributeImpl> attibutes, String p_objectType,
			String p_name, long p_jobId) throws Exception
	{
		PreparedStatement ps = null;
		String attributeTable = null;

		try
		{
			// Delete attributes by object IDs first.
			attributeTable = BigTableUtil
					.getTuTuvAttributeTableByJobId(p_jobId);
			StringBuffer objectIdsBuf = new StringBuffer();
			for (TuTuvAttributeImpl attr : attibutes)
			{
				objectIdsBuf.append(attr.getObjectId()).append(",");
			}
			String objectIdsClause = objectIdsBuf
					.substring(0, objectIdsBuf.length() - 1);

			String sql = DEL_ATTR_BY_TUVID_SQL.replace(
					TU_TUV_ATTR_TABLE_PLACEHOLDER, attributeTable).replace(
					"_OBJECT_ID_PLACEHOLDER_", objectIdsClause);
			ps = p_connection.prepareStatement(sql);
			ps.setString(1, p_objectType);
			ps.setString(2, p_name);
			ps.execute();
			p_connection.commit();
		}
		catch (Exception e)
		{
			logger.error("Error when update tu/tuv attributes into table '"
					+ attributeTable + "'.", e);
			throw e;
		}
		finally
		{
			DbUtil.silentClose(ps);
		}
	}

	private static String getObjectIdsClause(List<Long> objectIdList)
	{
		if (objectIdList == null || objectIdList.size() == 0)
			return "";

		StringBuffer buffer = new StringBuffer();
		for (Long tuvId : objectIdList) {
			buffer.append(tuvId).append(",");
		}
		return buffer.substring(0, buffer.length() - 1);
	}
}
