/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.docproc.extractor.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junitx.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;

public class TestOfficeContentPostFilterHelper
{
    private ExtractionRules m_rules = null;
    private static List<String> m_embeddedHtmlContents = new ArrayList<String>();

    @BeforeClass
    public static void initClass()
    {
        m_embeddedHtmlContents
                .add("This is <span test=\"hello\" title=biaoti>I'm text in <bb>span <a>text under a</a> text {between a and span}</span> a demo string");
        m_embeddedHtmlContents
                .add("<table border=0 cellpadding=0 cellspacing=0 width=98%> {leo} <tr> <td class=\"scaledefinitions\" align=center></td> <td>put a text here</tr></table><br>");
    }

    @Before
    public void initMethod()
    {
        m_rules = new ExtractionRules();
    }

    @Test
    public void test1And2()
    {
        // case 1 - m_embeddedHtmlContents.get(0)
        combinedCase1();

        // case 2 - m_embeddedHtmlContents.get(1)
        combinedCase2();
    }

    @Test
    public void testHandleInternalText()
    {
        // case 1 - m_embeddedHtmlContents.get(0)
        internalTextPostFilterCase1();

        // case 2 - m_embeddedHtmlContents.get(1)
        internalTextPostFilterCase2();
    }

    @Test
    public void testHandleTagsInContent()
    {
        // case 1 - m_embeddedHtmlContents.get(0)
        contentPostFilterCase1();

        // case 2 - m_embeddedHtmlContents.get(1)
        contentPostFilterCase2();
    }

    private void combinedCase1()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(0)));

        Map<String, Object> inlineTagMap = new HashMap<String, Object>();
        inlineTagMap.put("span", null);
        inlineTagMap.put("a", null);
        m_rules.setContentPostFilterInlineTagMap(inlineTagMap);

        Map<String, Object> translatableAttrMap = new HashMap<String, Object>();
        translatableAttrMap.put("test", null);
        translatableAttrMap.put("title", null);
        m_rules.setContentPostFilterTranslableAttrMap(translatableAttrMap);

        List<InternalText> list = new ArrayList<InternalText>();
        InternalText it1 = new InternalText("text under a", false);
        list.add(it1);
        InternalText it2 = new InternalText("\\{[^\\{]*\\}", true);
        list.add(it2);
        m_rules.setInternalTextList(list);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleTagsInContent(tags);
        tags = helper.handleInternalText(tags);

        // index 0
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(0);
        Assert.assertEquals("HtmlObjects.Text equals.", "This is ",
                text1.toString());

        // index 1
        HtmlObjects.Tag tag1 = (HtmlObjects.Tag) tags.get(1);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<span test=\"hello\" title=biaoti>", tag1.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "span",
                tag1.tag);
        HtmlObjects.ExtendedAttributeList attrList = tag1.attributes;
        for (Iterator<?> it = attrList.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "test=\"hello\"", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "title=biaoti", it.next().toString());
        }

        // index 2
        // <bb> is not set to inline tag, so it is included in an
        // HtmlObjects.Text object
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(2);
        Assert.assertEquals("HtmlObjects.Text equals.",
                "I'm text in <bb>span ", text2.toString());

        // index 3
        HtmlObjects.Tag tag2 = (HtmlObjects.Tag) tags.get(3);
        Assert.assertEquals("htmlObjects.Tag string equals.", "<a>",
                tag2.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "a", tag2.tag);

        // index 4
        HtmlObjects.InternalText internalText1 = (HtmlObjects.InternalText) tags
                .get(4);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "text under a", internalText1.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "text under a"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText1.original);

        // index 5
        HtmlObjects.EndTag endTag1 = (HtmlObjects.EndTag) tags.get(5);
        Assert.assertEquals("htmlObjects.EndTag string equals.", "</a>",
                endTag1.toString());
        Assert.assertEquals("htmlObjects.EndTag tag name equals.", "a",
                endTag1.tag);

        // index 6
        HtmlObjects.Text text3 = (HtmlObjects.Text) tags.get(6);
        Assert.assertEquals("HtmlObjects.Text equals.", " text ",
                text3.toString());

        // index 7
        HtmlObjects.InternalText internalText2 = (HtmlObjects.InternalText) tags
                .get(7);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "{between a and span}", internalText2.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START
                        + "{between a and span}"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText2.original);

        // index 8
        HtmlObjects.EndTag endTag2 = (HtmlObjects.EndTag) tags.get(8);
        Assert.assertEquals("htmlObjects.EndTag string equals.", "</span>",
                endTag2.toString());
        Assert.assertEquals("htmlObjects.EndTag tag name equals.", "span",
                endTag2.tag);

        // index 9
        HtmlObjects.Text text4 = (HtmlObjects.Text) tags.get(9);
        Assert.assertEquals("HtmlObjects.Text equals.", " a demo string",
                text4.toString());
    }

    private void combinedCase2()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(1)));

        Map<String, Object> inlineTagMap = new HashMap<String, Object>();
        inlineTagMap.put("table", null);
        inlineTagMap.put("tr", null);
        inlineTagMap.put("td", null);
        inlineTagMap.put("br", null);
        m_rules.setContentPostFilterInlineTagMap(inlineTagMap);

        Map<String, Object> translatableAttrMap = new HashMap<String, Object>();
        translatableAttrMap.put("test", null);
        translatableAttrMap.put("title", null);
        translatableAttrMap.put("border", null);
        translatableAttrMap.put("cellpadding", null);
        translatableAttrMap.put("cellspacing", null);
        translatableAttrMap.put("width", null);
        translatableAttrMap.put("class", null);
        translatableAttrMap.put("align", null);
        m_rules.setContentPostFilterTranslableAttrMap(translatableAttrMap);

        List<InternalText> list = new ArrayList<InternalText>();
        InternalText it1 = new InternalText("=", false);
        list.add(it1);
        InternalText it2 = new InternalText("text under a", false);
        list.add(it2);
        InternalText it3 = new InternalText("\\{[^\\{]*\\}", true);
        list.add(it3);
        m_rules.setInternalTextList(list);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleTagsInContent(tags);
        tags = helper.handleInternalText(tags);

        // index 0
        HtmlObjects.Tag tag1 = (HtmlObjects.Tag) tags.get(0);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<table border=0 cellpadding=0 cellspacing=0 width=98%>",
                tag1.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "table",
                tag1.tag);
        HtmlObjects.ExtendedAttributeList attrList1 = tag1.attributes;
        for (Iterator<?> it = attrList1.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.", "border=0", it
                    .next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "cellpadding=0", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "cellspacing=0", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.", "width=98%",
                    it.next().toString());
        }

        // index 1
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(1);
        Assert.assertEquals("HtmlObjects.Text equals.", " ", text1.toString());

        // index 2
        HtmlObjects.InternalText internalText1 = (HtmlObjects.InternalText) tags
                .get(2);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "{leo}", internalText1.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "{leo}"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText1.original);

        // index 3
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(3);
        Assert.assertEquals("HtmlObjects.Text equals.", " ", text2.toString());

        // index 4
        // merged tag: <tr>
        HtmlObjects.Tag tag2 = (HtmlObjects.Tag) tags.get(4);
        Assert.assertEquals("htmlObjects.Tag string equals.", "<tr> ",
                tag2.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "tr", tag2.tag);

        // index 5
        HtmlObjects.Tag tag3 = (HtmlObjects.Tag) tags.get(5);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<td class=\"scaledefinitions\" align=center>", tag3.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "td", tag3.tag);
        HtmlObjects.ExtendedAttributeList attrList2 = tag3.attributes;
        for (Iterator<?> it = attrList2.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "class=\"scaledefinitions\"", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "align=center", it.next().toString());
        }

        // index 6
        // merged tag: </td> <td>
        HtmlObjects.Tag tag4 = (HtmlObjects.Tag) tags.get(6);
        Assert.assertEquals("htmlObjects.Tag string equals.", "</td> <td>",
                tag4.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "td", tag4.tag);

        // index 7
        HtmlObjects.Text text3 = (HtmlObjects.Text) tags.get(7);
        Assert.assertEquals("HtmlObjects.Text equals.", "put a text here",
                text3.toString());

        // index 8
        // merged tag: </tr></table><br>
        HtmlObjects.Tag tag5 = (HtmlObjects.Tag) tags.get(8);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "</tr></table><br>", tag5.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "tr", tag5.tag);
    }

    private void internalTextPostFilterCase1()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(0)));

        List<InternalText> list = new ArrayList<InternalText>();
        InternalText it1 = new InternalText("text under a", false);
        list.add(it1);
        InternalText it2 = new InternalText("\\{[^\\{]*\\}", true);
        list.add(it2);
        m_rules.setInternalTextList(list);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleInternalText(tags);

        // index 0
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(0);
        Assert.assertEquals(
                "HtmlObjects.Text equals.",
                "This is <span test=\"hello\" title=biaoti>I'm text in <bb>span <a>",
                text1.toString());

        // index 1
        HtmlObjects.InternalText internalText1 = (HtmlObjects.InternalText) tags
                .get(1);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "text under a", internalText1.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "text under a"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText1.original);

        // index 2
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(2);
        Assert.assertEquals("HtmlObjects.Text equals.", "</a> text ",
                text2.toString());

        // index 3
        HtmlObjects.InternalText internalText2 = (HtmlObjects.InternalText) tags
                .get(3);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "{between a and span}", internalText2.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START
                        + "{between a and span}"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText2.original);

        // index 4
        HtmlObjects.Text text3 = (HtmlObjects.Text) tags.get(4);
        Assert.assertEquals("HtmlObjects.Text equals.",
                "</span> a demo string", text3.toString());
    }

    private void internalTextPostFilterCase2()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(1)));

        List<InternalText> list = new ArrayList<InternalText>();
        InternalText it1 = new InternalText("=", false);
        list.add(it1);
        InternalText it2 = new InternalText("text under a", false);
        list.add(it2);
        InternalText it3 = new InternalText("\\{[^\\{]*\\}", true);
        list.add(it3);
        m_rules.setInternalTextList(list);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleInternalText(tags);

        // index 0
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(0);
        Assert.assertEquals("HtmlObjects.Text equals.", "<table border",
                text1.toString());

        // index 1
        HtmlObjects.InternalText internalText1 = (HtmlObjects.InternalText) tags
                .get(1);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText1.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText1.original);

        // index 2
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(2);
        Assert.assertEquals("HtmlObjects.Text equals.", "0 cellpadding",
                text2.toString());

        // index 3
        HtmlObjects.InternalText internalText2 = (HtmlObjects.InternalText) tags
                .get(3);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText2.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText2.original);

        // index 4
        HtmlObjects.Text text3 = (HtmlObjects.Text) tags.get(4);
        Assert.assertEquals("HtmlObjects.Text equals.", "0 cellspacing",
                text3.toString());

        // index 5
        HtmlObjects.InternalText internalText3 = (HtmlObjects.InternalText) tags
                .get(5);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText3.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText3.original);

        // index 6
        HtmlObjects.Text text4 = (HtmlObjects.Text) tags.get(6);
        Assert.assertEquals("HtmlObjects.Text equals.", "0 width",
                text4.toString());

        // index 7
        HtmlObjects.InternalText internalText4 = (HtmlObjects.InternalText) tags
                .get(7);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText4.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText4.original);

        // index 8
        HtmlObjects.Text text5 = (HtmlObjects.Text) tags.get(8);
        Assert.assertEquals("HtmlObjects.Text equals.", "98%> ",
                text5.toString());

        // index 9
        HtmlObjects.InternalText internalText5 = (HtmlObjects.InternalText) tags
                .get(9);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "{leo}", internalText5.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "{leo}"
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText5.original);

        // index 10
        HtmlObjects.Text text6 = (HtmlObjects.Text) tags.get(10);
        Assert.assertEquals("HtmlObjects.Text equals.", " <tr> <td class",
                text6.toString());

        // index 11
        HtmlObjects.InternalText internalText6 = (HtmlObjects.InternalText) tags
                .get(11);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText6.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText6.original);

        // index 12
        HtmlObjects.Text text7 = (HtmlObjects.Text) tags.get(12);
        Assert.assertEquals("HtmlObjects.Text equals.",
                "\"scaledefinitions\" align", text7.toString());

        // index 13
        HtmlObjects.InternalText internalText7 = (HtmlObjects.InternalText) tags
                .get(13);
        Assert.assertEquals("htmlObjects.InternalText internal text equals.",
                "=", internalText7.internalText);
        Assert.assertEquals("htmlObjects.InternalText name equals.",
                InternalTextHelper.GS_INTERNALT_TAG_START + "="
                        + InternalTextHelper.GS_INTERNALT_TAG_END,
                internalText7.original);

        // index 14
        HtmlObjects.Text text8 = (HtmlObjects.Text) tags.get(14);
        Assert.assertEquals("HtmlObjects.Text equals.",
                "center></td> <td>put a text here</tr></table><br>",
                text8.toString());
    }

    private void contentPostFilterCase1()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(0)));

        Map<String, Object> inlineTagMap = new HashMap<String, Object>();
        inlineTagMap.put("span", null);
        inlineTagMap.put("a", null);
        m_rules.setContentPostFilterInlineTagMap(inlineTagMap);

        Map<String, Object> translatableAttrMap = new HashMap<String, Object>();
        translatableAttrMap.put("test", null);
        translatableAttrMap.put("title", null);
        m_rules.setContentPostFilterTranslableAttrMap(translatableAttrMap);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleTagsInContent(tags);

        // index 0
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(0);
        Assert.assertEquals("HtmlObjects.Text equals.", "This is ",
                text1.toString());

        // index 1
        HtmlObjects.Tag tag1 = (HtmlObjects.Tag) tags.get(1);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<span test=\"hello\" title=biaoti>", tag1.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "span",
                tag1.tag);
        HtmlObjects.ExtendedAttributeList attrList = tag1.attributes;
        for (Iterator<?> it = attrList.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "test=\"hello\"", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "title=biaoti", it.next().toString());
        }

        // index 2
        // <bb> is not set to inline tag, so it is included in an
        // HtmlObjects.Text object
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(2);
        Assert.assertEquals("HtmlObjects.Text equals.",
                "I'm text in <bb>span ", text2.toString());

        // index 3
        HtmlObjects.Tag tag2 = (HtmlObjects.Tag) tags.get(3);
        Assert.assertEquals("htmlObjects.Tag string equals.", "<a>",
                tag2.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "a", tag2.tag);

        // index 4
        HtmlObjects.Text text3 = (HtmlObjects.Text) tags.get(4);
        Assert.assertEquals("HtmlObjects.Text equals.", "text under a",
                text3.toString());

        // index 5
        HtmlObjects.EndTag endTag1 = (HtmlObjects.EndTag) tags.get(5);
        Assert.assertEquals("htmlObjects.EndTag string equals.", "</a>",
                endTag1.toString());
        Assert.assertEquals("htmlObjects.EndTag tag name equals.", "a",
                endTag1.tag);

        // index 6
        HtmlObjects.Text text4 = (HtmlObjects.Text) tags.get(6);
        Assert.assertEquals("HtmlObjects.Text equals.",
                " text {between a and span}", text4.toString());

        // index 7
        HtmlObjects.EndTag endTag2 = (HtmlObjects.EndTag) tags.get(7);
        Assert.assertEquals("htmlObjects.EndTag string equals.", "</span>",
                endTag2.toString());
        Assert.assertEquals("htmlObjects.EndTag tag name equals.", "span",
                endTag2.tag);

        // index 8
        HtmlObjects.Text text5 = (HtmlObjects.Text) tags.get(8);
        Assert.assertEquals("HtmlObjects.Text equals.", " a demo string",
                text5.toString());
    }

    private void contentPostFilterCase2()
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>();
        tags.add(new HtmlObjects.Text(m_embeddedHtmlContents.get(1)));

        Map<String, Object> inlineTagMap = new HashMap<String, Object>();
        inlineTagMap.put("table", null);
        inlineTagMap.put("tr", null);
        inlineTagMap.put("td", null);
        inlineTagMap.put("br", null);
        m_rules.setContentPostFilterInlineTagMap(inlineTagMap);

        Map<String, Object> translatableAttrMap = new HashMap<String, Object>();
        translatableAttrMap.put("test", null);
        translatableAttrMap.put("title", null);
        translatableAttrMap.put("border", null);
        translatableAttrMap.put("cellpadding", null);
        translatableAttrMap.put("cellspacing", null);
        translatableAttrMap.put("width", null);
        translatableAttrMap.put("class", null);
        translatableAttrMap.put("align", null);
        m_rules.setContentPostFilterTranslableAttrMap(translatableAttrMap);

        OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                m_rules);
        // call the method to test
        tags = helper.handleTagsInContent(tags);

        // index 0
        HtmlObjects.Tag tag1 = (HtmlObjects.Tag) tags.get(0);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<table border=0 cellpadding=0 cellspacing=0 width=98%>",
                tag1.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "table",
                tag1.tag);
        HtmlObjects.ExtendedAttributeList attrList1 = tag1.attributes;
        for (Iterator<?> it = attrList1.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.", "border=0", it
                    .next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "cellpadding=0", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "cellspacing=0", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.", "width=98%",
                    it.next().toString());
        }

        // index 1
        HtmlObjects.Text text1 = (HtmlObjects.Text) tags.get(1);
        Assert.assertEquals("HtmlObjects.Text equals.", " {leo} ",
                text1.toString());

        // index 2
        // merged tag: <tr>
        HtmlObjects.Tag tag2 = (HtmlObjects.Tag) tags.get(2);
        Assert.assertEquals("htmlObjects.Tag string equals.", "<tr> ",
                tag2.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "tr", tag2.tag);

        // index 3
        HtmlObjects.Tag tag3 = (HtmlObjects.Tag) tags.get(3);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "<td class=\"scaledefinitions\" align=center>", tag3.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "td", tag3.tag);
        HtmlObjects.ExtendedAttributeList attrList2 = tag3.attributes;
        for (Iterator<?> it = attrList2.iterator(); it.hasNext();)
        {
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "class=\"scaledefinitions\"", it.next().toString());
            Assert.assertEquals("HtmlObjects.Attribute equals.",
                    "align=center", it.next().toString());
        }

        // index 4
        // merged tag: </td> <td>
        HtmlObjects.Tag tag4 = (HtmlObjects.Tag) tags.get(4);
        Assert.assertEquals("htmlObjects.Tag string equals.", "</td> <td>",
                tag4.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "td", tag4.tag);

        // index 5
        HtmlObjects.Text text2 = (HtmlObjects.Text) tags.get(5);
        Assert.assertEquals("HtmlObjects.Text equals.", "put a text here",
                text2.toString());

        // index 6
        // merged tag: </tr></table><br>
        HtmlObjects.Tag tag5 = (HtmlObjects.Tag) tags.get(6);
        Assert.assertEquals("htmlObjects.Tag string equals.",
                "</tr></table><br>", tag5.toString());
        Assert.assertEquals("htmlObjects.Tag tag name equals.", "tr", tag5.tag);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore
                .runClasses(TestOfficeContentPostFilterHelper.class);
    }

}
