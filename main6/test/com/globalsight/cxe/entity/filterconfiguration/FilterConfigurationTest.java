package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.*;
import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;

import static com.globalsight.cxe.entity.filterconfiguration.FilterConfiguration.*;

public class FilterConfigurationTest {

    // GBS-1366
    @Test
    public void testMultiLineDescriptions() throws JSONException {
        FilterConfiguration config = new FilterConfigurationForTesting();
        config.setName("test filter");
        config.setKnownFormatId("xml");
        config.setId(1);
        config.setCompanyId(1L);
        config.setFilterTableName("test");

        testDescription(config, "line1\nline2");
        testDescription(config, "line 1\\\nline 2/\r\"foo\" 'bar'");
    }
    
    void testDescription(FilterConfiguration config, String description) 
                throws JSONException {
        config.setFilterDescription(description);
        String s = config.toJSON();
        JSONObject o = new JSONObject(s);
        String d = (String)o.get("filterDescription");
        assertEquals(description, d);
    }
    
    class FilterConfigurationForTesting extends FilterConfiguration {
        @Override
        public ArrayList<Filter> getSpecialFilters() {
            return new ArrayList<Filter>();
        }
    }
}
