package com.globalsight.ling.tm3.core;

/**
 * Interface for TMs that utilize shared storage.
 */
public interface TM3SharedTm<T extends TM3Data> extends TM3Tm<T> {

    public long getSharedStorageId();
}
