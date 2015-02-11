package com.globalsight.ling.tm3.core;

class MultilingualTm<T extends TM3Data> extends BaseTm<T> {

    // Empty constructor for Hibernate
    MultilingualTm() {
    }
    
    MultilingualTm(TM3DataFactory<T> factory) {
        super(factory);
    }
    
    @Override
    protected StorageInfo<T> createStorageInfo() {
        return new MultilingualStorageInfo<T>(this);
    }

    @Override
    public TM3TmType getType() {
        return TM3TmType.MULTILINGUAL;
    }

}
