package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;

class AttributeDataHandle<T extends TM3Data> extends
        AbstractDataHandle<T> {
    private Map<TM3Attribute, Object> inlineAttrs;
    private Map<TM3Attribute, String> customAttrs;
    private Set<String> m_jobAttributeSet;
    private Map<String,String> m_paramMap;
    
    AttributeDataHandle(BaseTm<T> tm,
                        Map<TM3Attribute, Object> inlineAttrs,
                        Map<TM3Attribute, String> customAttrs,
                        Date start, Date end) {
        super(tm, start, end);
        this.inlineAttrs = inlineAttrs;
        this.customAttrs = customAttrs;
    }
    
    AttributeDataHandle(BaseTm<T> tm,
		            Map<TM3Attribute, Object> inlineAttrs,
		            Map<TM3Attribute, String> customAttrs,
		            Date start, Date end, Set<String> jobAttributeSet) {
		super(tm, start, end);
		this.inlineAttrs = inlineAttrs;
		this.customAttrs = customAttrs;
		m_jobAttributeSet = jobAttributeSet;
	}

	AttributeDataHandle(BaseTm<T> tm, Map<TM3Attribute, Object> inlineAttrs,
			Map<TM3Attribute, String> customAttrs, Map<String, String> paramMap)
	{
		super(tm);
		this.inlineAttrs = inlineAttrs;
		this.customAttrs = customAttrs;
		this.m_paramMap = paramMap;
	}

	AttributeDataHandle(BaseTm<T> tm, Map<TM3Attribute, Object> inlineAttrs,
			Map<TM3Attribute, String> customAttrs,
			Map<String, String> paramMap, Set<String> jobAttributeSet)
	{
		super(tm);
		this.inlineAttrs = inlineAttrs;
		this.customAttrs = customAttrs;
		m_jobAttributeSet = jobAttributeSet;
		this.m_paramMap = paramMap;
	}

    @Override
    public long getCount() throws TM3Exception {
        try {
        	if(m_jobAttributeSet == null || m_jobAttributeSet.size() == 0)
        	{
        		return getTm().getStorageInfo().getTuStorage()
                	.getTuCountByAttributes(inlineAttrs, customAttrs,
                                        getStart(), getEnd());
        	}
        	else
        	{
        		return getTm().getStorageInfo().getTuStorage()
                	.getTuCountByAttributes(inlineAttrs, customAttrs,
                          getStart(), getEnd(),m_jobAttributeSet);
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
			if (m_jobAttributeSet == null || m_jobAttributeSet.size() == 0)
			{
				return getTm()
						.getStorageInfo()
						.getTuStorage()
						.getTuCountByAttributesAndParamMap(inlineAttrs,
								customAttrs, m_paramMap);
			}
			else
			{
				return getTm()
						.getStorageInfo()
						.getTuStorage()
						.getTuCountByAttributesAndParamMap(inlineAttrs,
								customAttrs, m_paramMap, m_jobAttributeSet);
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

    class AttributesTuIterator extends TuIterator {
        @Override
		protected void loadPage()
		{
			try
			{
				// Load 100 at a time
				List<TM3Tu<T>> page;

				if (m_jobAttributeSet == null || m_jobAttributeSet.size() == 0)
				{
					page = getTm()
							.getStorageInfo()
							.getTuStorage()
							.getTuPageByAttributesAndParamMap(startId, 100,
									inlineAttrs, customAttrs, m_paramMap);
				}
				else
				{
					page = getTm()
							.getStorageInfo()
							.getTuStorage()
							.getTuPageByAttributesAndParamMap(startId, 100,
									inlineAttrs, customAttrs, m_paramMap,
									m_jobAttributeSet);
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
