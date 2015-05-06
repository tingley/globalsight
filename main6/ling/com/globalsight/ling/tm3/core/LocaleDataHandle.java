package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LocaleDataHandle<T extends TM3Data> extends AbstractDataHandle<T> {
    private List<TM3Locale> localeList;
    private Set<String> m_jobAttrinbuteSet;
    private int increment = 100; // Load 100 at a time
    private Map<String,String> m_paramMap;
    
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
			Map<String, String> paramMap)
	{
		super(tm);
		this.localeList = localeList;
		this.m_paramMap = paramMap;
	}

	LocaleDataHandle(BaseTm<T> tm, List<TM3Locale> localeList,
			Map<String, String> paramMap, Set<String> jobAttributeSet)
	{
		super(tm);
		this.localeList = localeList;
		m_jobAttrinbuteSet = jobAttributeSet;
		this.m_paramMap = paramMap;
	}

    @Override
    public void purgeData() throws TM3Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCount() throws TM3Exception {
        try {
        	if(m_jobAttrinbuteSet == null || m_jobAttrinbuteSet.size() == 0)
        	{
        		return getTm().getStorageInfo().getTuStorage()
                .getTuCountByLocales(localeList, getStart(), getEnd());
        	}
        	else
        	{
        		return getTm().getStorageInfo().getTuStorage()
                .getTuCountByLocales(localeList, getStart(), getEnd(),m_jobAttrinbuteSet);
        	}
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

	@Override
	public long getCountByParameter() throws TM3Exception
	{
		try
		{
			if (m_jobAttrinbuteSet == null || m_jobAttrinbuteSet.size() == 0)
			{
				return getTm().getStorageInfo().getTuStorage()
						.getTuCountByLocalesAndParamMap(localeList, m_paramMap);
			}
			else
			{
				return getTm()
						.getStorageInfo()
						.getTuStorage()
						.getTuCountByLocalesAndParamMap(localeList, m_paramMap,
								m_jobAttrinbuteSet);
			}
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
				List<TM3Tu<T>> page;
				if (m_jobAttrinbuteSet == null
						|| m_jobAttrinbuteSet.size() == 0)
				{
					page = getTm()
							.getStorageInfo()
							.getTuStorage()
							.getTuPageByLocalesAndParamMap(startId, increment,
									localeList, m_paramMap);
				}
				else
				{
					page = getTm()
							.getStorageInfo()
							.getTuStorage()
							.getTuPageByLocalesAndParamMap(startId, increment,
									localeList, m_paramMap, m_jobAttrinbuteSet);
				}
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
