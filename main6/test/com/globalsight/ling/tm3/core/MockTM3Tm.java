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

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.tm3.integration.GSTuvData;

/**
 * Mock TM3 TM for testing.
 */
public class MockTM3Tm implements TM3Tm<GSTuvData>
{
    private Map<String, TM3Attribute> attributes = new HashMap<String, TM3Attribute>();
    private MockTM3Saver<GSTuvData> saver = null;

    // Get the last saver created by this TM
    public MockTM3Saver<GSTuvData> getLastSaver()
    {
        return saver;
    }

    //
    // TM3Tm<T> implementation
    //

    @Override
    public TM3Attribute addAttribute(String name)
    {
        TM3Attribute attr = new TM3Attribute();
        attr.setName(name);
        attributes.put(name, attr);
        return attr;
    }

    @Override
    public MockTM3Saver<GSTuvData> createSaver()
    {
        saver = new MockTM3Saver<GSTuvData>();
        return saver;
    }

    @Override
    public TM3LeverageResults<GSTuvData> findMatches(GSTuvData matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3LeverageResults<GSTuvData> findMatches(GSTuvData matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget) throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getAllAttributeValues(TM3Attribute attr)
            throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getAllData(Date start, Date end)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Attribute getAttributeByName(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Set<TM3Attribute> getAttributes()
    {
        return new HashSet<TM3Attribute>(attributes.values());
    }

    @Override
    public TM3Handle<GSTuvData> getDataByAttributes(
            Map<TM3Attribute, Object> attrs, Date start, Date end)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getDataById(List<Long> tuIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3DataFactory<GSTuvData> getDataFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tu<GSTuvData> getTu(long id) throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tuv<GSTuvData> getTuv(long id) throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<TM3Locale> getTuvLocales() throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3TmType getType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tu<GSTuvData> modifyTu(TM3Tu<GSTuvData> tu, boolean indexTarget)
            throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeAttribute(TM3Attribute name)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeDataByLocale(TM3Locale locale)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean doesAttributeExist(String name)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setConnection(Connection connection)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Connection getConnection()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3LeverageResults<GSTuvData> findMatches(GSTuvData matchKey, TM3Locale sourceLocale,
            Set<? extends TM3Locale> targetLocales, Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold, List<Long> tm3TmIds) throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TM3Tu<GSTuvData>> getTu(List<Long> ids) throws TM3Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getAllDataByParamMap(Map<String, Object> paramMap)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getDataByLocales(List<TM3Locale> localeList, Date start, Date end)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recreateFuzzyIndex(List<TM3Tuv<GSTuvData>> tuvs)
    {
        // TODO Auto-generated method stub
        
    }

}
