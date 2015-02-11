package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.globalsight.ling.tm3.core.TM3Saver.Tu;

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
    public MockTu tu(T content, TM3Locale locale, TM3Event event) {
        MockTu tu = new MockTu(content, locale, event);
        tus.add(tu);
        return tu;
    }
    
    @Override
    public List<TM3Tu<T>> save(TM3SaveMode mode) throws TM3Exception {
        saved = true;
        return Collections.emptyList();
    }

    public List<MockTu> getSavedTus() {
        return saved ? tus : new ArrayList<MockTu>();
    }
    
    public class MockTu extends TM3Saver<T>.Tu {
        MockTu(T content, TM3Locale locale, TM3Event event) {
            super(content, locale, event);
        }
        
        public Tuv getSourceTuv() {
            return srcTuv;
        }
        
        public List<Tuv> getTargets() {
            return targets;
        }
    }
}