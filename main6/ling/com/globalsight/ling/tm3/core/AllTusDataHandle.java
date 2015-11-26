package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A handle to the entire TM.
 */
class AllTusDataHandle<T extends TM3Data> extends AbstractDataHandle<T>
{
	private Map<String, Object> m_paramMap;

    AllTusDataHandle(BaseTm<T> tm) {
        super(tm);
    }
    
    AllTusDataHandle(BaseTm<T> tm, Date start, Date end) {
        super(tm, start, end);
    }
    
	AllTusDataHandle(BaseTm<T> tm, Map<String, Object> paramMap)
	{
		super(tm);
		m_paramMap = paramMap;
	}

    @Override
	public long getCount() throws TM3Exception
	{
		try
		{
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
                .getTuvCount(getStart(), getEnd());
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    @Override
    public void purgeData() throws TM3Exception {
        try {
            getTm().getStorageInfo().getTuStorage()
                .deleteTus(getStart(), getEnd());
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    @Override
    public Iterator<TM3Tu<T>> iterator() throws TM3Exception {
        return new AllTusIterator();
    }

    class AllTusIterator extends AbstractDataHandle<T>.TuIterator {
        @Override
        protected void loadPage() {
			try
			{
				// Load 100 at a time
				List<TM3Tu<T>> page = getTm().getStorageInfo().getTuStorage()
						.getTuPageByParamMap(startId, 1000, m_paramMap);
				if (page.size() > 0)
				{
					startId = page.get(page.size() - 1).getId();
					currentPage = page.iterator();
				}
			}
            catch (SQLException e) {
                throw new TM3Exception(e);
            }
        }
    }

    /**
     * GBS-3885:Exporting TM takes long time
     * In the absence of the search conditions, get tu id of total
     * */
	@Override
	public long getAllTuCount() throws TM3Exception
	{
		try
		{
			return getTm().getStorageInfo().getTuStorage().getAllTuCount();
		}
		catch (SQLException e)
		{
			throw new TM3Exception(e);
		}
	}

	public long getTuCountByLocale(Long localeId) throws TM3Exception
	{
		try
		{
			return getTm().getStorageInfo().getTuStorage()
					.getTuCountByLocale(localeId);
		}
		catch (SQLException e)
		{
			throw new TM3Exception(e);
		}
	}
	
	public long getTuvCountByLocale(List<TM3Locale> localeList)
			throws TM3Exception
	{
		try
		{
			return getTm().getStorageInfo().getTuStorage()
					.getTuvCountByLocales(localeList, getStart(), getEnd());
		}
		catch (SQLException e)
		{
			throw new TM3Exception(e);
		}
	}
}
