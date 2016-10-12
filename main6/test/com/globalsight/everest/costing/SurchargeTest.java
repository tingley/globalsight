package com.globalsight.everest.costing;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Surcharge name should be unique, no mater type(FlatSurcharge/PercentageSurcharge).
 */
public class SurchargeTest
{
    Surcharge fSurCharge;
    Surcharge pSurCharge;
    int fHashCode = -1000690027;
    int pHashCode = -2027272813;

    @Before
    public void before()
    {
        fSurCharge = new FlatSurcharge();
        fSurCharge.setName("Float SurCharge");

        pSurCharge = new PercentageSurcharge();
        pSurCharge.setName("Percentage SurCharge");
    }

    /**
     * Test adds Surcharge data into Set Collection. 
     * Adds equals() and hashCode() to Surcharge, for adding surcharges to Set Collection.
     */
    @Test
    public void addSurchargeIntoSet()
    {
        Set<Surcharge> set = new HashSet<Surcharge>();
        set.add(fSurCharge);
        set.add(pSurCharge);
        assertEquals(2, set.size());

        FlatSurcharge f = new FlatSurcharge(null, 0.1f);
        set.add(f);
        assertEquals(3, set.size());

        f.setName(fSurCharge.getName());
        set.add(f);
        assertEquals(3, set.size());
    }

    @Test
    public void testHashCode()
    {
        assertEquals(fHashCode, fSurCharge.hashCode());
        ((FlatSurcharge) fSurCharge).setAmount(0.1f, null);
        assertEquals(fHashCode, fSurCharge.hashCode());

        assertEquals(pHashCode, pSurCharge.hashCode());
        ((PercentageSurcharge) pSurCharge).setPercentage(0.1f);
        assertEquals(pHashCode, pSurCharge.hashCode());
    }

    @Test
    public void testEquals()
    {
        FlatSurcharge f = new FlatSurcharge(null, 0.1f);
        assertEquals(false, fSurCharge.equals(f));
        f.setName(fSurCharge.getName());
        assertEquals(true, fSurCharge.equals(f));
        f.setName(fSurCharge.getName() + "AAA");
        assertEquals(false, fSurCharge.equals(f));
    }
}
