package com.globalsight.ling.tm3.core;

class MultilingualSharedTm<T extends TM3Data> extends BaseTm<T> 
                            implements TM3SharedTm<T> {

    private long sharedStorageId;
    
    // Empty constructor for Hibernate
    MultilingualSharedTm() {
    }
    
    MultilingualSharedTm(long sharedStorageId, TM3DataFactory<T> factory) {
        super(factory);
        this.sharedStorageId = sharedStorageId;
    }
    
    @Override
    protected StorageInfo<T> createStorageInfo() {
        return new MultilingualSharedStorageInfo<T>(this);
    }

    @Override
    public TM3TmType getType() {
        return TM3TmType.MULTILINGUAL_SHARED;
    }

    @Override
    public long getSharedStorageId() {
        return sharedStorageId;
    }
    
    private void setSharedStorageId(long sharedStorageId) {
        this.sharedStorageId = sharedStorageId;
    }
}
