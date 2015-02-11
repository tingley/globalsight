/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.tm3.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class BilingualTm<T extends TM3Data> extends BaseTm<T> implements
        TM3BilingualTm<T>
{

    private TM3Locale srcLocale; // transient
    private TM3Locale tgtLocale; // transient
    private long srcLocaleId; // persistent
    private long tgtLocaleId; // persistent

    // Empty constructor for Hibernate
    BilingualTm()
    {
    }

    // Constructor for creating new instances
    BilingualTm(TM3DataFactory<T> factory, TM3Locale srcLocale,
            TM3Locale tgtLocale)
    {
        super(factory);
        setSrcLocale(srcLocale);
        setTgtLocale(tgtLocale);
    }

    @Override
    protected StorageInfo<T> createStorageInfo()
    {
        return new BilingualStorageInfo<T>(this);
    }

    @Override
    public TM3TmType getType()
    {
        return TM3TmType.BILINGUAL;
    }

    @Override
    public TM3Locale getSrcLocale()
    {
        if (srcLocale == null)
        {
            srcLocale = loadLocale(srcLocaleId);
        }
        return srcLocale;
    }

    @Override
    public TM3Locale getTgtLocale()
    {
        if (tgtLocale == null)
        {
            tgtLocale = loadLocale(tgtLocaleId);
        }
        return tgtLocale;
    }

    private TM3Locale loadLocale(long id)
    {
        if (getDataFactory() == null)
        {
            throw new IllegalStateException("No TM3DataFactory set");
        }
        return getDataFactory().getLocaleById(id);
    }

    void setSrcLocale(TM3Locale srcLocale)
    {
        this.srcLocale = srcLocale;
        this.srcLocaleId = srcLocale.getId();
    }

    void setTgtLocale(TM3Locale tgtLocale)
    {
        this.tgtLocale = tgtLocale;
        this.tgtLocaleId = tgtLocale.getId();
    }

    // These are used only by Hibernate
    @SuppressWarnings("unused")
    private long getSrcLocaleId()
    {
        return srcLocaleId;
    }

    @SuppressWarnings("unused")
    private void setSrcLocaleId(long id)
    {
        this.srcLocaleId = id;
    }

    @SuppressWarnings("unused")
    private long getTgtLocaleId()
    {
        return tgtLocaleId;
    }

    @SuppressWarnings("unused")
    private void setTgtLocaleId(long id)
    {
        this.tgtLocaleId = id;
    }

//    @Override
//    public TM3Tu<T> save(TM3Locale srcLocale, T source,
//            Map<TM3Attribute, Object> attributes, TM3Locale tgtLocale,
//            T target, TM3SaveMode mode, TM3Event event)
//    {
//        if (!this.getSrcLocale().equals(srcLocale))
//        {
//            throw new IllegalArgumentException(
//                    "Bilingual TM expected srcLocale=" + this.getSrcLocale()
//                            + ", found=" + srcLocale);
//        }
//        if (!this.getTgtLocale().equals(tgtLocale))
//        {
//            throw new IllegalArgumentException(
//                    "Bilingual TM expected tgtLocale=" + this.getTgtLocale()
//                            + ", found=" + tgtLocale);
//        }
//        return super.save(srcLocale, source, attributes, tgtLocale, target,
//                mode, event);
//    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold)
    {
        return super.findMatches(matchKey, getSrcLocale(),
                Collections.singleton(getTgtLocale()), attributes, matchType,
                lookupTarget, maxResults, threshold);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget)
    {
        return super.findMatches(matchKey, getSrcLocale(),
                Collections.singleton(getTgtLocale()), attributes, matchType,
                lookupTarget);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception
    {
        // A bilingual TM will always return zero results if the requested
        // locale is not the TM's source locale, unless we are doing
        // reverse leverage (and the requested locale is the TM's target
        // locale).
        if (!sourceLocale.equals(getSrcLocale())
                && !(lookupTarget && sourceLocale.equals(getTgtLocale())))
        {
            return new TM3LeverageResults(matchKey, attributes);
        }
        return super.findMatches(matchKey, sourceLocale, targetLocales,
                attributes, matchType, lookupTarget, maxResults, threshold);
    }

//    @Override
//    public TM3Tu<T> save(T source, Map<TM3Attribute, Object> attributes,
//            T target, TM3SaveMode mode, TM3Event event)
//    {
//        return super.save(getSrcLocale(), source, attributes, getTgtLocale(),
//                target, mode, event);
//    }
}
