package com.globalsight.everest.page;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;

public class WsPageTemplateExtentionTest
{

    @Test
    public void test()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("...<skeleton>&lt;/source&gt;&lt;target&gt;</skeleton>");
        buffer.append("<translatable blockId=\"2\" tm_score=\"66.00\"");
        buffer.append(" xliffPart=\"target\" ws_word_count=\"4\" tuID=\"0000000446b\">\";");
        String skeletonStr = buffer.toString();

        TuvImpl tuv = new TuvImpl();

        TuImpl tu = new TuImpl();

        tu.addTuv(tuv);
        tuv.setTu(tu);
        tuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
        WsPageTemplateExtention extention = new WsPageTemplateExtention(false);
        String newSkeletion = extention.processSkeleton(skeletonStr, tuv,
                1000);

        String shouldBeStr = replaceString(skeletonStr, PageTemplate.byLocalTM);

        assertTrue(newSkeletion.equals(shouldBeStr));

        tuv.setLastModifiedUser("MS_Translator_MT");
        tu.setSourceContent(Extractor.IWS_REPETITION);
        newSkeletion = extention.processSkeleton(skeletonStr, tuv, 1000);
        shouldBeStr = replaceString(skeletonStr, "no");
        assertTrue(newSkeletion.equals(shouldBeStr));

        tu.setSourceContent(null);
        newSkeletion = extention.processSkeleton(skeletonStr, tuv, 1000);
        shouldBeStr = replaceString(skeletonStr, PageTemplate.byMT);
        assertTrue(newSkeletion.equals(shouldBeStr));

        tuv.setLastModifiedUser("walter");
        newSkeletion = extention.processSkeleton(skeletonStr, tuv, 1000);
        shouldBeStr = replaceString(skeletonStr, PageTemplate.byUser);
        assertTrue(newSkeletion.equals(shouldBeStr));

        tuv.setState(TuvState.NOT_LOCALIZED);
        newSkeletion = extention.processSkeleton(skeletonStr, tuv, 1000);
        shouldBeStr = replaceString(skeletonStr, "no");
        assertTrue(newSkeletion.equals(shouldBeStr));
    }

    private String replaceString(String skeletonStr, String replaceStr)
    {
        String trasStr = "<" + DiplomatNames.Element.TRANSLATABLE;
        skeletonStr = skeletonStr.replace(trasStr, trasStr + " "
                + DiplomatNames.Attribute.ISLOCALIZED + "=\"" + replaceStr
                + "\" ");
        return skeletonStr;
    }

}
