package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

class LocaleDataHandle<T extends TM3Data> extends AbstractDataHandle<T> {
    private TM3Locale locale;
    private int increment = 100; // Load 100 at a time
    
    LocaleDataHandle(BaseTm<T> tm, TM3Locale locale) {
        super(tm);
        this.locale = locale;
    }
    
    LocaleDataHandle(BaseTm<T> tm, TM3Locale locale, 
                     Date start, Date end) {
        super(tm, start, end);
        this.locale = locale;
    }
    
    @Override
    public void purgeData() throws TM3Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCount() throws TM3Exception {
        try {
            return getTm().getStorageInfo().getTuStorage()
                    .getTuCountByLocale(locale, getStart(), getEnd());
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public long getTuvCount() throws TM3Exception {
        try {
            return getTm().getStorageInfo().getTuStorage()
                    .getTuvCountByLocale(locale, getStart(), getEnd());
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
        
    class LocaleTusIterator extends AbstractDataHandle<T>.TuIterator {
        @Override
        protected void loadPage() {
            try {
                List<TM3Tu<T>> page = getTm().getStorageInfo().getTuStorage()
                            .getTuPageByLocale(startId, increment, locale, getStart(), getEnd());
                if (page.size() > 0) {
                    startId = page.get(page.size() - 1).getId();
                    currentPage = page.iterator();
                }
            }
            catch (SQLException e) {
                throw new TM3Exception(e);
            }
        }
    }
}
