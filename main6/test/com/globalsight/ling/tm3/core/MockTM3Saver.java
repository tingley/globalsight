package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Fake TM3Saver that doesn't actually save anything, but collects
 * the TUs and will return them via getSavedTus() if save() has been
 * called.
 */
public class MockTM3Saver<T extends TM3Data> extends TM3Saver<T> {

    private boolean saved = false;
    private List<MockTu> tus = new ArrayList<MockTu>();
    
    MockTM3Saver() {
        super();
    }

    @Override
    public MockTu tu(T content, TM3Locale locale, String creationUser, Date creationDate,
            String modifyUser, Date modifyDate, Date lastUsageDate, long jobId, String jobName,
            long previousHash, long nextHash, String sid)
    {
        MockTu tu = new MockTu(content, locale, creationUser, creationDate, modifyUser, modifyDate,
                lastUsageDate, jobId, jobName, previousHash, nextHash, sid);
        tus.add(tu);
        return tu;
    }

    @Override
    public List<TM3Tu<T>> save(TM3SaveMode mode, boolean indexTarget) throws TM3Exception {
        saved = true;
        return Collections.emptyList();
    }

    public List<MockTu> getSavedTus() {
        return saved ? tus : new ArrayList<MockTu>();
    }
    
    public class MockTu extends TM3Saver<T>.Tu
    {
        MockTu(T content, TM3Locale locale, String creationUser, Date creationDate,
                String modifyUser, Date modifyDate, Date lastUsageDate, long jobId, String jobName,
                long previousHash, long nextHash, String sid)
        {
            super(content, locale, creationUser, creationDate, modifyUser, modifyDate,
                    lastUsageDate, jobId, jobName, previousHash, nextHash, sid);
        }

        public Tuv getSourceTuv()
        {
            return srcTuv;
        }

        public List<Tuv> getTargets()
        {
            return targets;
        }
    }
}