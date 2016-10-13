package com.globalsight.ling.tm3.core;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

abstract class AbstractDataHandle<T extends TM3Data> implements TM3Handle<T> {
    private BaseTm<T> tm;
    private Date start, end;
    
    AbstractDataHandle(BaseTm<T> tm) {
        this(tm, null, null);
    }
        
    AbstractDataHandle(BaseTm<T> tm, Date start, Date end) {
        this.tm = tm;
        this.start = start;
        this.end = end;
    }
    
    protected BaseTm<T> getTm() {
        return tm;
    }
    
    protected Date getStart() {
        return start;
    }
    
    protected Date getEnd() {
        return end;
    }
    
    @Override
    public void purge() {
        tm.lockForWrite();
        purgeData();
    }
    
    @Override
    public void purgeWithoutLock() {
        purgeData();
    }
    
    protected abstract void purgeData();

	@Override
	public long getAllTuCount() throws TM3Exception
	{
		// NOTE: fake implementation.
		return 0;
	}

	@Override
	public long getTuCountByLocale(Long localeId) throws TM3Exception
	{
		// NOTE: fake implementation.
		return 0;
	}
	
	public long getTuvCountByLocale(List<TM3Locale> localeList) throws TM3Exception
	{
		// NOTE: fake implementation.
		return 0;
	}

	abstract class TuIterator implements Iterator<TM3Tu<T>> {

        protected Iterator<TM3Tu<T>> currentPage;
        protected long startId = 0;
        
        @Override
        public boolean hasNext() {
            if (currentPage != null && currentPage.hasNext()) {
                return true;
            }
            loadPage();
            return (currentPage != null && currentPage.hasNext());
        }

        /**
         * Subclasses must implement this.  This method should
         * either set the currentPage member to an iterator to
         * the next page or results, or else set it to null
         * (indicating completion.)
         */
        protected abstract void loadPage();
        
        @Override
        public TM3Tu<T> next() {
            if (hasNext()) {
                return currentPage.next();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
