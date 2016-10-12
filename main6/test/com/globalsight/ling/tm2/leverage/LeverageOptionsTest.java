package com.globalsight.ling.tm2.leverage;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.ling.tm.LeveragingLocales;

public class LeverageOptionsTest
{
    @Test
    public void testGetTmIdsForLevInProgressTmPurpose()
    {
        // TM profile
        TranslationMemoryProfile tmProfile = new TranslationMemoryProfile();
        tmProfile.setDynLevFromPopulationTm(true);
        long storageTmId = 1000;
        tmProfile.setProjectTmIdForSave(storageTmId);
        tmProfile.setDynLevFromReferenceTm(true);
        
        LeverageProjectTM leverageProjectTM = new LeverageProjectTM();
        leverageProjectTM.setProjectTmId(1001);
        tmProfile.setProjectTMToLeverageFrom(leverageProjectTM);
        
        leverageProjectTM = new LeverageProjectTM();
        leverageProjectTM.setProjectTmId(1002);
        tmProfile.setProjectTMToLeverageFrom(leverageProjectTM);
        
        LeveragingLocales leveragingLocales = new LeveragingLocales();
        LeverageOptions levOptions = new LeverageOptions(tmProfile, leveragingLocales);
        
        Set<Long> results = levOptions.getTmIdsForLevInProgressTmPurpose();
        Collection<Long> expected = new HashSet();
        expected.add(new Long(1000));
        expected.add(new Long(1001));
        expected.add(new Long(1002));
        
        assertTrue(results.containsAll(expected));
    }
}
