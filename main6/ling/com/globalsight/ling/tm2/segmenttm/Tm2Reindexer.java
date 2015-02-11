package com.globalsight.ling.tm2.segmenttm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.indexer.TmSegmentIndexer;
import com.globalsight.ling.tm2.lucene.LuceneReindexer;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

public class Tm2Reindexer {

    private static final String GET_SEGMENTS = "SELECT tu.id tu_id, tu.format format, tu.type type, "
            + "tuv.id tuv_id, tuv.segment_string segment_string, "
            + "tuv.segment_clob segment_clob, tuv.locale_id locale_id, "
            + "tu.source_locale_id source_locale_id "
            + "from project_tm_tu_t tu, project_tm_tuv_t tuv "
            + "where tu.id = tuv.tu_id and tu.tm_id = ? ";

    private static final String SOURCE_ONLY = "and tuv.locale_id = tu.SOURCE_LOCALE_ID ";

    private static final String SORT_BY_LOCALE = "order by tuv.locale_id";

    private Session m_session;
    
    public Tm2Reindexer(Session session) {
        m_session = session;
    }

    public boolean reindexTm(Tm tm, Reindexer reindexer) throws Exception {
        Connection conn = m_session.connection();
        LuceneReindexer luceneReindexer = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            boolean success = true;
            String query = GET_SEGMENTS;
            if (!TmSegmentIndexer.indexesTargetSegments())
            {
                query += SOURCE_ONLY;
            }
            query += SORT_BY_LOCALE;
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, tm.getId());
            rs = stmt.executeQuery();
            long curr_locale_id = 0;
            while (rs.next())
            {
                long locale_id = rs.getLong("locale_id");
                if (curr_locale_id != locale_id)
                {
                    curr_locale_id = locale_id;
                    GlobalSightLocale locale = getLocale(locale_id);
                    if (luceneReindexer != null)
                    {
                        luceneReindexer.close();
                    }
                    luceneReindexer = new LuceneReindexer(tm.getId(), locale);
                }
                SegmentTmTuv tuv = getSegment(rs, luceneReindexer
                        .getLocale(), tm.getId());
                luceneReindexer.index(tuv);
                reindexer.incrementCounter(1);
                synchronized (reindexer)
                {
                    if (reindexer.getInterrupted())
                    {
                        success = false;
                        break;
                    }
                }
            }
            return success;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            if (luceneReindexer != null)
            {
                try
                {
                    luceneReindexer.close();
                }
                catch (Exception ignore)
                {
                }
            }
        }
    }
        
    private SegmentTmTuv getSegment(ResultSet p_rs, GlobalSightLocale p_locale,
            long p_tmId) throws Exception
    {
        SegmentTmTuv tuv = null;

        String segment = p_rs.getString("segment_string");
        if (segment == null)
        {
            segment = DbUtil.readClob(p_rs, "segment_clob");
        }

        tuv = new SegmentTmTuv(p_rs.getLong("tuv_id"), segment, p_locale);

        SegmentTmTu tu = new SegmentTmTu(p_rs.getLong("tu_id"), p_tmId, p_rs
                .getString("format"), p_rs.getString("type"), true,
                getLocale(p_rs.getLong("source_locale_id")));

        tu.addTuv(tuv);

        return tuv;
    }

    private GlobalSightLocale getLocale(long p_localeId) throws Exception
    {
        return (GlobalSightLocale)m_session.get(GlobalSightLocale.class, p_localeId);
    }

}
