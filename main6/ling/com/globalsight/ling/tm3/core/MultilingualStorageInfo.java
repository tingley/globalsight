package com.globalsight.ling.tm3.core;

class MultilingualStorageInfo<T extends TM3Data>  
            extends DedicatedStorageInfo<T> {

    protected MultilingualStorageInfo(BaseTm<T> tm) {
        super(tm, TM3TmType.MULTILINGUAL);
    }
    
    @Override
    FuzzyIndex<T> getFuzzyIndex() {
        return new MultilingualFuzzyIndex<T>(this);
    }
}
