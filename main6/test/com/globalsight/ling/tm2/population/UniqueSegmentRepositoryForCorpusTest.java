package com.globalsight.ling.tm2.population;

import static org.junit.Assert.*;
import org.junit.*;

import com.globalsight.util.GlobalSightLocale;

public class UniqueSegmentRepositoryForCorpusTest {

    @Test
    public void testGetIdenticalSegments() {
        GlobalSightLocale sourceLocale = 
            new GlobalSightLocale("en", "US", true);
        sourceLocale.setId(1);
        UniqueSegmentRepositoryForCorpus usr = 
            new UniqueSegmentRepositoryForCorpus(sourceLocale);
        // Make sure we don't ever return a null list, even if there are no 
        // results (GBS-1875)
        assertNotNull(usr.getIdenticalSegment(null));
        // TODO: more
    }
    
    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("com.globalsight.ling.tm2.population.UniqueSegmentRepositoryForCorpusTest");
    }
}
