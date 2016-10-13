package com.globalsight.util.edit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.util.gxml.GxmlElement;

public class SegmentUtil2Test
{
    /**
     * Test "getGxmlElement(...)" and "getAttValuesByName(...)".
     */
    @Test
    public void testGetAttValuesByName()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<segment>Choose Delete from the Action menu <ph id=\"77\" x=\"&lt;ConditionHelp>\">{77}</ph>");
        sb.append("<ph id=\"78\" x=\"&lt;inlineGraphic>\">{78}</ph>");
        sb.append("<ph id=\"79\" x=\"&lt;Graphic href=&quot;Art/S220_Actionmenu.png&quot; alt=&quot;\">{79}</ph>Image of Action menu.<ph id=\"80\" x=\"&quot;>\">{80}</ph>");
        sb.append("</segment>");
        GxmlElement element = SegmentUtil2.getGxmlElement(sb.toString());

        List<String> actual = new ArrayList<String>();
        actual = SegmentUtil2.getAttValuesByName(element, "id");
        
        List<String> expected = new ArrayList<String>();
        expected.add("77");
        expected.add("78");
        expected.add("79");
        expected.add("80");
        
        assertTrue(actual.size()==4);
        assertTrue(expected.toString().equals(actual.toString()));
    }

    @Test
    public void testExtractSegment()
    {
        DiplomatAPI api = new DiplomatAPI();
        StringBuffer segment = new StringBuffer();
        segment.append("Choose Delete from the Action menu <ph id=\"195\" x=\"&lt;ConditionHelp&gt;\">{195}</ph>");
        segment.append("<ph id=\"196\" x=\"&lt;inlineGraphic&gt;\">{196}</ph>");
        segment.append("<ph id=\"197\" x=\"&lt;Graphic href=&quot;Art/S220_Actionmenu.png&quot; alt=&quot;\">{197}</ph>Image of Action menu-translated----.<ph id=\"198\" x=\"&quot;&gt;\">{198}</ph>");
        String dataType = "xlf";
        Locale sourceLocale = new Locale("en", "US");

        SegmentNode result = SegmentUtil2.extractSegment(api,
                segment.toString(), dataType, sourceLocale);
        String actual = result.getSegment();

        StringBuffer expected = new StringBuffer();
        expected.append("Choose Delete from the Action menu <ph type=\"ph\" id=\"1\" x=\"1\">&lt;ph id=\"195\" x=\"&amp;lt;ConditionHelp&amp;gt;\"&gt;{195}&lt;/ph&gt;</ph>");
        expected.append("<ph type=\"ph\" id=\"2\" x=\"2\">&lt;ph id=\"196\" x=\"&amp;lt;inlineGraphic&amp;gt;\"&gt;{196}&lt;/ph&gt;</ph>");
        expected.append("<ph type=\"ph\" id=\"3\" x=\"3\">&lt;ph id=\"197\" x=\"&amp;lt;Graphic href=&amp;quot;Art/S220_Actionmenu.png&amp;quot; alt=&amp;quot;\"&gt;{197}&lt;/ph&gt;</ph>Image of Action menu-translated----.<ph type=\"ph\" id=\"4\" x=\"4\">&lt;ph id=\"198\" x=\"&amp;quot;&amp;gt;\"&gt;{198}&lt;/ph&gt;</ph>");
        
        assertEquals(expected.toString(), actual);
    }

}
