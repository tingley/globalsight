package com.globalsight.ling.tm3.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.integration.GSTuvData;

public class TM3ImportHelper extends TM3Event
{
    private static final Logger LOGGER = Logger
            .getLogger(TM3ImportHelper.class);

    public synchronized static TM3Event createTM3Event(TM3Tm<GSTuvData> tm,
            String user, int eventType, String attr, Date modifyDate)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        TM3Event event = null;
        try
        {
            if (modifyDate == null)
            {
                modifyDate = new Date();
            }
            event = new TM3Event((BaseTm<?>) tm, eventType, user, attr, modifyDate);

            String sql = "INSERT INTO TM3_EVENTS (time, userName, tmId, type, arg) VALUES (?, ?, ?, ?, ?)";
            ps = tm.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, new Timestamp(event.getTimestamp().getTime()));
            ps.setString(2, user);
            ps.setLong(3, tm.getId());
            ps.setInt(4, eventType);
            ps.setString(5, attr);
            ps.execute();
            rs = ps.getGeneratedKeys();
            long eventId = 0;
            if (rs.next())
            {
                eventId = rs.getLong(1);
            }
            else
            {
                boolean isAutoCommit = tm.getConnection().getAutoCommit();
                LOGGER.error("debug info :: fail to get eventId. isAutoCommit = "
                        + isAutoCommit);
            }

            event.setId(eventId);
        }
        catch (Exception e)
        {
            LOGGER.error("Cannot create TM3 event correctly", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return event;
    }
}
