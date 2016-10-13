package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LocaleDataHandle<T extends TM3Data> extends AbstractDataHandle<T> {
    private List<TM3Locale> localeList;
    private Set<String> m_jobAttrinbuteSet;
    private int increment = 100; // Load 100 at a time
    private Map<String,Object> m_paramMap;
    
    LocaleDataHandle(BaseTm<T> tm, List<TM3Locale> localeList, 
                     Date start, Date end) {
        super(tm, start, end);
        this.localeList = localeList;
    }
    
    LocaleDataHandle(BaseTm<T> tm, List<TM3Locale> localeList, 
    				Date start, Date end,Set<String> jobAttributeSet) {
		super(tm, start, end);
		this.localeList = localeList;
		m_jobAttrinbuteSet = jobAttributeSet;
	}
    
	LocaleDataHandle(BaseTm<T> tm, List<TM3Locale> localeList,
			Map<String, Object> paramMap)
	{
		super(tm);
		this.localeList = localeList;
		this.m_paramMap = paramMap;
	}

    @Override
    public void purgeData() throws TM3Exception {
        throw new UnsupportedOperationException();
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
                    .getTuvCountByLocales(localeList, getStart(), getEnd());
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public Iterator<TM3Tu<T>> iterator() throws TM3Exception {
        return new LocaleTusIterator();
    }

	private void putValueToParamMap()
	{
		if (m_paramMap == null || m_paramMap.isEmpty())
		{
			m_paramMap = new HashMap<String, Object>();
			m_paramMap.put("language", localeList);
		}
		else
		{
			List<TM3Locale> list = (List<TM3Locale>) m_paramMap.get("language");
			if (list == null && localeList != null)
			{
				m_paramMap.put("language", localeList);
			}
		}
	}
    /**
     * For testing
     **/
    void setIncrement(int increment) {
        this.increment = increment;
    }
        
	class LocaleTusIterator extends AbstractDataHandle<T>.TuIterator
	{
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
