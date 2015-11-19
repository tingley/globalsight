package com.globalsight.ling.tm3.core;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class ByIdDataHandle<T extends TM3Data> extends AbstractDataHandle<T> {

    private List<Long> ids;
    
    ByIdDataHandle(BaseTm<T> tm, List<Long> ids) {
        super(tm);
        this.ids = ids;
    }
    
    /**
     * Slow/naive impementation, since this is 
     * never currently used.
     */
    @Override
    public long getCount() throws TM3Exception {
        return getTus().size();
    }

    /**
     * Slow/naive impementation, since this is 
     * never currently used.
     */
    @Override
    public long getTuvCount() throws TM3Exception {
        List<TM3Tu<T>> tus = getTus();
        long count = 0;
        for (TM3Tu<T> tu : tus) {
            count += tu.getAllTuv().size();
        }
        return count;
    }

    @Override
    public Iterator<TM3Tu<T>> iterator() throws TM3Exception {
        return new ByIdTuIterator();
    }

    @Override
    public void purgeData() throws TM3Exception {
        try {
            getStorage().deleteTusById(ids);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    private TuStorage<T> getStorage() {
        return getTm().getStorageInfo().getTuStorage();
    }

    private List<TM3Tu<T>> getTus() throws TM3Exception {
        try {
            return getStorage().getTu(ids, false);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    class ByIdTuIterator extends TuIterator {
        boolean done = false;
        
        @Override
        protected void loadPage() {
            if (!done) {
                currentPage = getTus().iterator();
                done = true;
            }
            else {
                currentPage = null;
            }
        }
    }
}
