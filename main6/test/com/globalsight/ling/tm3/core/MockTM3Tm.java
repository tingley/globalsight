package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3DataFactory;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3EventLog;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Handle;
import com.globalsight.ling.tm3.core.TM3LeverageResults;
import com.globalsight.ling.tm3.core.TM3Locale;
import com.globalsight.ling.tm3.core.TM3MatchType;
import com.globalsight.ling.tm3.core.TM3SaveMode;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3TmType;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSTuvData;


/**
 * Mock TM3 TM for testing.
 */
public class MockTM3Tm implements TM3Tm<GSTuvData> {

    private long nextEventId = 1;
    private List<TM3Event> events = new ArrayList<TM3Event>();
    private Map<String, TM3Attribute> attributes = 
        new HashMap<String, TM3Attribute>();
    private MockTM3Saver<GSTuvData> saver = null;
    
    //
    // Custom methods to expose the results of all these calls
    //
    
    // Get a list of all events added to this TM
    public List<TM3Event> getEvents() {
        return events;
    }
    
    // Get the last saver created by this TM
    public MockTM3Saver<GSTuvData> getLastSaver() {
        return saver;
    }
    
    //
    // TM3Tm<T> implementation
    //
    
    @Override
    public TM3Attribute addAttribute(String name) {
        TM3Attribute attr = new TM3Attribute();
        attr.setName(name);
        attributes.put(name, attr);
        return attr;
    }

    @Override
    public TM3Event addEvent(int type, String username, String arg) {
        return addEvent(type, username, arg, new Date());
    }

    @Override
    public TM3Event addEvent(int type, String username, String arg, Date date) {
        TM3Event e = new TM3Event();
        e.setId(nextEventId++);
        e.setType(type);
        e.setUsername(username);
        e.setArgument(arg);
        e.setTimestamp(date);
        events.add(e);
        return e;
    }

    @Override
    public MockTM3Saver<GSTuvData> createSaver() {
        saver = new MockTM3Saver<GSTuvData>();
        return saver;
    }

    @Override
    public TM3LeverageResults<GSTuvData> findMatches(GSTuvData matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3LeverageResults<GSTuvData> findMatches(GSTuvData matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget) throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Object> getAllAttributeValues(TM3Attribute attr)
            throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getAllData(Date start, Date end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Attribute getAttributeByName(String name) {
        return attributes.get(name);
    }

    @Override
    public Set<TM3Attribute> getAttributes() {
        return new HashSet<TM3Attribute>(attributes.values());
    }

    @Override
    public TM3Handle<GSTuvData> getDataByAttributes(
            Map<TM3Attribute, Object> attrs, Date start, Date end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getDataById(List<Long> tuIds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Handle<GSTuvData> getDataByLocale(TM3Locale locale, Date start,
            Date end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3DataFactory<GSTuvData> getDataFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Event getEvent(long id) throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3EventLog getEventLog() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tu<GSTuvData> getTu(long id) throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tuv<GSTuvData> getTuv(long id) throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<TM3Locale> getTuvLocales() throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3TmType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tu<GSTuvData> modifyTu(TM3Tu<GSTuvData> tu, TM3Event event)
            throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeAttribute(TM3Attribute name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeDataByLocale(TM3Locale locale) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TM3Tu<GSTuvData> save(TM3Locale srcLocale, GSTuvData source,
            Map<TM3Attribute, Object> attributes, TM3Locale tgtLocale,
            GSTuvData target, TM3SaveMode mode, TM3Event event)
            throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TM3Tu<GSTuvData> save(TM3Locale srcLocale, GSTuvData source,
            Map<TM3Attribute, Object> attributes,
            Map<TM3Locale, GSTuvData> targets, TM3SaveMode mode, TM3Event event)
            throws TM3Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setIndexTarget(boolean indexTarget) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean doesAttributeExist(String name)
    {
        // TODO Auto-generated method stub
        return false;
    }

    
}
