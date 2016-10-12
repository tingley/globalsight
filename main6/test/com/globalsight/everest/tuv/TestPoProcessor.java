package com.globalsight.everest.tuv;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.globalsight.util.GlobalSightLocale;

public class TestPoProcessor
{
    @Test
    public void testGetTargetLanguage()
    {
        GlobalSightLocale p_sourceLocale = new GlobalSightLocale("en", "US", false);
        GlobalSightLocale p_targetLocale = new GlobalSightLocale("fr", "FR", false);
        
        TuImpl p_tu = new TuImpl();
        p_tu.setXliffTargetLanguage("en");
        
        PoProcessor processor = new PoProcessor();
        String targetLan = processor.getTargetLanguage(p_tu, p_sourceLocale, p_targetLocale);
        
        assertTrue(targetLan.equals("fr_fr"));
        
        p_tu.setXliffTargetLanguage("fr");
        targetLan = processor.getTargetLanguage(p_tu, p_sourceLocale, p_targetLocale);
        assertTrue(targetLan.equals("fr_fr"));
        
    }
}
