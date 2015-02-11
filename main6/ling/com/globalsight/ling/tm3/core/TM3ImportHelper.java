package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.integration.GSTuvData;

public class TM3ImportHelper extends TM3Event
{
    private static final Logger LOGGER = Logger
            .getLogger(TM3ImportHelper.class);

    public static TM3Event createTM3Event(TM3Tm<GSTuvData> tm, String user,
            int eventType, String attr, Date modifyDate)
    {
        TM3Event event = null;
        try
        {
            if (modifyDate == null) {
                modifyDate = new Date();
            }
            event = new TM3Event((BaseTm<?>) tm, eventType, user, attr, modifyDate);

            Statement stmt = tm.getConnection().createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO TM3_EVENTS (time, userName, tmId, type, arg) VALUES ('");
            sql.append(new Timestamp(event.getTimestamp().getTime()));
            sql.append("','").append(user).append("',").append(tm.getId())
                    .append(",").append(eventType);
            sql.append(",'").append(attr).append("')");
            stmt.execute(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            long eventId = rs.getLong(1);

            event.setId(eventId);
        }
        catch (Exception e)
        {
            LOGGER.error("Cannot create TM3 event correctly", e);
        }

        return event;
    }
    
    /**
     * Validate if TM3 is the first time to import into TM
     * 
     * @param tm TM3 tm
     * @return boolean true will be return if it's the first time to import TM3
     */
    public static boolean isFirstImporting(Tm tm)
    {
        if (tm.getTm3Id() == null)
            return false;

        long tm3Id = tm.getTm3Id();
        String tableName = "TM3_TU_SHARED_" + tm.getCompanyId();

        Connection connection = null;
        try
        {
            connection = DbUtil.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "
                    + tableName + " WHERE tmid=" + tm3Id);
            if (rs.next())
            {
                if (rs.getInt(1) > 0)
                    return true;
            }
            return false;
        }
        catch (Exception e)
        {
            LOGGER.error(
                    "Cannot check if this is the first time to run TM3 importing.",
                    e);
            return false;
        }
        finally
        {
            DbUtil.silentReturnConnection(connection);
        }
    }
}
