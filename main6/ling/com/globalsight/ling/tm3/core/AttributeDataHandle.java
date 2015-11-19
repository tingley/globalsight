package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class AttributeDataHandle<T extends TM3Data> extends
        AbstractDataHandle<T> {
    private Map<TM3Attribute, Object> inlineAttrs;
    private Map<TM3Attribute, String> customAttrs;
    private Map<String,Object> m_paramMap;
    
    AttributeDataHandle(BaseTm<T> tm,
                        Map<TM3Attribute, Object> inlineAttrs,
                        Map<TM3Attribute, String> customAttrs,
                        Date start, Date end) {
        super(tm, start, end);
        this.inlineAttrs = inlineAttrs;
        this.customAttrs = customAttrs;
    }

	AttributeDataHandle(BaseTm<T> tm, Map<TM3Attribute, Object> inlineAttrs,
			Map<TM3Attribute, String> customAttrs, Map<String, Object> paramMap)
	{
		super(tm);
		this.inlineAttrs = inlineAttrs;
		this.customAttrs = customAttrs;
		this.m_paramMap = paramMap;
	}

    @Override
	public long getCount() throws TM3Exception
	{
		try
		{
			putValueToParamMap();
			return getTm().getStorageInfo().getTuStorage()
					.getTuCountByParamMap(m_paramMap);
		}
		catch (SQLException e)
		{
			throw new TM3Exception(e);
		}
	}

    @Override
    public long getTuvCount() throws TM3Exception {
        try {
            return getTm().getStorageInfo().getTuStorage()
                    .getTuvCountByAttributes(inlineAttrs, customAttrs,
                                             getStart(), getEnd());
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public Iterator<TM3Tu<T>> iterator() throws TM3Exception {
        return new AttributesTuIterator();
    }

    @Override
    public void purgeData() throws TM3Exception {
        // Bailing on this one for now.  GlobalSight never calls it.
        throw new UnsupportedOperationException("Not yet implemented");
    }

	private void putValueToParamMap()
	{
		if (m_paramMap == null || m_paramMap.isEmpty())
		{
			m_paramMap = new HashMap<String, Object>();
			m_paramMap.put("inlineAttrs", inlineAttrs);
			m_paramMap.put("customAttrs", customAttrs);
		}
		else
		{
			Map<TM3Attribute, Object> inlineAttrsMap = (Map<TM3Attribute, Object>) m_paramMap
					.get("inlineAttrs");
			Map<TM3Attribute, String> customAttrsMap = (Map<TM3Attribute, String>) m_paramMap
					.get("customAttrs");
			if ((inlineAttrsMap == null || inlineAttrsMap.isEmpty())
					&& inlineAttrs != null)
			{
				m_paramMap.put("inlineAttrs", inlineAttrs);
			}
			if ((customAttrsMap == null || customAttrsMap.isEmpty())
					&& customAttrs != null)
			{
				m_paramMap.put("customAttrs", customAttrs);
			}
		}
	}
    
    class AttributesTuIterator extends TuIterator {
        @Override
		protected void loadPage()
		{
			try
			{
				putValueToParamMap();
				List<TM3Tu<T>> page = getTm().getStorageInfo().getTuStorage()
						.getTuPageByParamMap(startId, 100, m_paramMap);

				if (page.size() > 0)
				{
					startId = page.get(page.size() - 1).getId();
					currentPage = page.iterator();
				}
			}
			catch (SQLException e)
			{
				throw new TM3Exception(e);
			}
		}
    }
}
