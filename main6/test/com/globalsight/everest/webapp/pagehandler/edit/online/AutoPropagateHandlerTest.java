package com.globalsight.everest.webapp.pagehandler.edit.online;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GlobalSightLocale;

public class AutoPropagateHandlerTest
{
    private AutoPropagateHandler handlerObj = new AutoPropagateHandler();
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetRepGroups()
    {
        Collection<TuImpl> tus = new ArrayList<TuImpl>();
        
        TuImpl tu1 = new TuImpl();
        tu1.setId(1);
        tu1.setRepeated(true);
        
        TuImpl tu2 = new TuImpl();
        tu2.setId(2);
        tu2.setRepetitionOfId(1);
        
        TuImpl tu3 = new TuImpl();
        tu3.setId(3);
        tu3.setRepetitionOfId(1);
        
        TuImpl tu4 = new TuImpl();
        tu4.setId(4);
        
        tus.add(tu1);
        tus.add(tu2);
        tus.add(tu3);
        tus.add(tu4);
        
        HashMap<Long, List<TuImpl>> repGroupsMap = new HashMap<Long, List<TuImpl>>();
        repGroupsMap = 
                (HashMap<Long, List<TuImpl>>) ClassUtil.testMethod(handlerObj,
                        "getRepGroups", tus);
        assertTrue(repGroupsMap.size()==1);
        List<TuImpl> tuValues = repGroupsMap.get(new Long(1));
        assertTrue(tuValues.size()==3);
    }
    
    @Test
    public void testAdjustOriginalSegmentAttributeValues()
    {
        TuvImpl applyingTuv = new TuvImpl();
        StringBuffer applyingGxml = new StringBuffer();
        applyingGxml.append("<segment segmentId=\"1\" wordcount=\"10\">Choose Delete from the Action menu <ph type=\"ph\" id=\"1\" x=\"1\">&lt;ph id=&quot;77&quot; x=&quot;&amp;lt;ConditionHelp&gt;&quot;&gt;{77}&lt;/ph&gt;</ph>");
        applyingGxml.append("<ph type=\"ph\" id=\"2\" x=\"2\">&lt;ph id=&quot;78&quot; x=&quot;&amp;lt;inlineGraphic&gt;&quot;&gt;{78}&lt;/ph&gt;</ph>");
        applyingGxml.append("<ph type=\"ph\" id=\"3\" x=\"3\">&lt;ph id=&quot;79&quot; x=&quot;&amp;lt;Graphic href=&amp;quot;Art/S220_Actionmenu.png&amp;quot; alt=&amp;quot;&quot;&gt;{79}&lt;/ph&gt;</ph>Image of Action menu-translated.<ph type=\"ph\" id=\"4\" x=\"4\">&lt;ph id=&quot;80&quot; x=&quot;&amp;quot;&gt;&quot;&gt;{80}&lt;/ph&gt;</ph>");
        applyingGxml.append("</segment>");
        applyingTuv.setGxml(applyingGxml.toString());
        GlobalSightLocale targetLocale = new GlobalSightLocale("de", "DE", false);
        applyingTuv.setGlobalSightLocale(targetLocale);

        TuvImpl sourceTuv = new TuvImpl();
        StringBuffer sourceGxml = new StringBuffer();
        sourceGxml.append("<segment segmentId=\"1\" wordcount=\"10\">Choose Delete from the Action menu <ph type=\"ph\" id=\"5\" x=\"5\">&lt;ph id=&quot;195&quot; x=&quot;&amp;lt;ConditionHelp&gt;&quot;&gt;{195}&lt;/ph&gt;</ph>");
        sourceGxml.append("<ph type=\"ph\" id=\"6\" x=\"6\">&lt;ph id=&quot;196&quot; x=&quot;&amp;lt;inlineGraphic&gt;&quot;&gt;{196}&lt;/ph&gt;</ph>");
        sourceGxml.append("<ph type=\"ph\" id=\"7\" x=\"7\">&lt;ph id=&quot;197&quot; x=&quot;&amp;lt;Graphic href=&amp;quot;Art/S220_Actionmenu.png&amp;quot; alt=&amp;quot;&quot;&gt;{197}&lt;/ph&gt;</ph>Image of Action menu.<ph type=\"ph\" id=\"8\" x=\"8\">&lt;ph id=&quot;198&quot; x=&quot;&amp;quot;&gt;&quot;&gt;{198}&lt;/ph&gt;</ph>");
        sourceGxml.append("</segment>");
        sourceTuv.setGxml(sourceGxml.toString());
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", true);
        sourceTuv.setGlobalSightLocale(sourceLocale);
        
        String actual = (String) ClassUtil.testMethod(handlerObj,
                "adjustOriginalSegmentAttributeValues", applyingTuv, sourceTuv);
    
        StringBuffer expected = new StringBuffer();
        expected.append("<segment segmentId=\"1\" wordcount=\"10\">Choose Delete from the Action menu <ph type=\"ph\" id=\"1\" x=\"1\">&lt;ph id=&quot;195&quot; x=&quot;&amp;lt;ConditionHelp&amp;gt;&quot;&gt;{195}&lt;/ph&gt;</ph>");
        expected.append("<ph type=\"ph\" id=\"2\" x=\"2\">&lt;ph id=&quot;196&quot; x=&quot;&amp;lt;inlineGraphic&amp;gt;&quot;&gt;{196}&lt;/ph&gt;</ph>");
        expected.append("<ph type=\"ph\" id=\"3\" x=\"3\">&lt;ph id=&quot;197&quot; x=&quot;&amp;lt;Graphic href=&amp;quot;Art/S220_Actionmenu.png&amp;quot; alt=&amp;quot;&quot;&gt;{197}&lt;/ph&gt;</ph>Image of Action menu-translated.<ph type=\"ph\" id=\"4\" x=\"4\">&lt;ph id=&quot;198&quot; x=&quot;&amp;quot;&amp;gt;&quot;&gt;{198}&lt;/ph&gt;</ph>");
        expected.append("</segment>");
        
        assertEquals(actual, expected.toString());
    }


}
