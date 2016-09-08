package com.globalsight.ling.tm2.population;

import static org.junit.Assert.*;

import org.junit.*;

import com.globalsight.util.GlobalSightLocale;

public class GoldenTmUniqueSegmentRepositoryTest
{

    @Test
    public void testGetIdenticalSegments()
    {
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", true);
        sourceLocale.setId(1);
        GoldenTmUniqueSegmentRepository usr = new GoldenTmUniqueSegmentRepository(sourceLocale);
        // Make sure we don't ever return a null list, even if there are no
        // results (GBS-1875)
        assertNotNull(usr.getIdenticalSegment(null));
        // TODO: more
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore
                .main("com.globalsight.ling.tm2.population.GoldenTmUniqueSegmentRepositoryTest");
    }
}
