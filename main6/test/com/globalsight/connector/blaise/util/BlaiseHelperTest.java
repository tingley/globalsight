package com.globalsight.connector.blaise.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognitran.core.model.i18n.Locale;
import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.cognitran.workflow.client.State;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.SelectOption;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.util.ClassUtil;

public class BlaiseHelperTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public final void testGetEntriesJobName() throws Exception
    {
        long id = 1111;
        String workflowId = "";
        Date workflowStartDate = new Date();
        State state = null;
        boolean group = false;
        String description = "";
        int attachmentsNumber = 1;
        long relatedObjectId = 2223;//
        String relatedObjectClassname = "unknown";
        String sourceType = "unknown";
        int sourceRevision = 10;
        Locale sourceLocale = Locale.en_US;//
        Locale targetLocale = Locale.zh_CN;//
        Date dueDate = new Date();
        boolean checkedOut = false;
        String companyName = "welocalize";
        long workflowObjectId = 1;
        TranslationInboxEntry entry1 = new TranslationInboxEntry(
                id, workflowId, workflowStartDate, state, group, description, attachmentsNumber, 
                relatedObjectId, relatedObjectClassname, sourceType, sourceRevision, sourceLocale,
                targetLocale, dueDate, checkedOut, companyName, workflowObjectId);
        TranslationInboxEntryVo vo1 = new TranslationInboxEntryVo(entry1);

        id = 2222;
        relatedObjectId = 1112;//
        TranslationInboxEntry entry2 = new TranslationInboxEntry(
                id, workflowId, workflowStartDate, state, group, description, attachmentsNumber, 
                relatedObjectId, relatedObjectClassname, sourceType, sourceRevision, sourceLocale,
                targetLocale, dueDate, checkedOut, companyName, workflowObjectId);
        TranslationInboxEntryVo vo2 = new TranslationInboxEntryVo(entry2);

        List<TranslationInboxEntryVo> entries = new ArrayList<TranslationInboxEntryVo>();
        entries.add(vo1);
        entries.add(vo2);

        BlaiseHelper helper = new BlaiseHelper(null);
        String result = (String) ClassUtil.testMethod(helper, "getEntriesJobName", entries);
        String expected = "Blaise IDs (1112 2223) - en_US - zh_CN";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGetTypeByRelatedObjectClassName()
    {
        String relatedObjectClassName = "com.cognitran.publication.model.media.Graphic";
        String type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Graphic", type);

        relatedObjectClassName = "com.cognitran.publication.model.standalone.StandalonePublication";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Standalone", type);

        relatedObjectClassName = "com.cognitran.publication.model.composite.Procedure";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Procedure", type);

        relatedObjectClassName = "com.cognitran.publication.model.controlled.ControlledContent";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Controlled content", type);

        relatedObjectClassName = "com.cognitran.translation.model.TranslatableObjectsDocument";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Translatable object", type);

        relatedObjectClassName = "com.cognitran.translation.model.unkown";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals("Unkown", type);

        relatedObjectClassName = " ";
        type = BlaiseHelper.getTypeByRelatedObjectClassName(relatedObjectClassName);
        Assert.assertEquals(null, type);
    }

    @Test
    public void testHandleSpecialChars()
    {
        BlaiseHelper helper = new BlaiseHelper(null);

        String str = "a\\b/c:d*e?f\"g'h<i>j|";
        String actual = (String) ClassUtil.testMethod(helper, "handleSpecialChars", str);
        Assert.assertEquals("a b c d e f g h i j", actual);

        str = "a  b   c         d     ";
        actual = (String) ClassUtil.testMethod(helper, "handleSpecialChars", str);
        Assert.assertEquals("a b c d", actual);
    }

    @Test
    public void testGetHarlyJobName() throws Exception
    {
        long id = 1111;
        String workflowId = "";
        Date workflowStartDate = new Date();
        State state = null;
        boolean group = false;
        String description = "";
        int attachmentsNumber = 1;
        long relatedObjectId = 2223;//
        String relatedObjectClassname = "unknown";
        String sourceType = "unknown";
        int sourceRevision = 10;
        Locale sourceLocale = Locale.en_US;//
        Locale targetLocale = Locale.zh_CN;//
        Date dueDate = new Date();
        boolean checkedOut = false;
        String companyName = "welocalize";
        long workflowObjectId = 1;
        TranslationInboxEntry entry = new TranslationInboxEntry(
                id, workflowId, workflowStartDate, state, group, description, attachmentsNumber, 
                relatedObjectId, relatedObjectClassname, sourceType, sourceRevision, sourceLocale,
                targetLocale, dueDate, checkedOut, companyName, workflowObjectId);
        TranslationInboxEntryVo vo = new TranslationInboxEntryVo(entry);

        String falconTargetValue = "1609_519_AA";
        String expected = "Harley_1609_519_AA_zh_CN";
        String actual = BlaiseHelper.getHarlyJobName(vo, falconTargetValue);
        Assert.assertEquals(expected, actual);

        falconTargetValue = "0123456789_0123456789_0123456789_0123456789_0123456789A_0123456789";
        expected = "Harley_0123456789_0123456789_0123456789_0123456789_0123456789A_zh_CN";
        actual = BlaiseHelper.getHarlyJobName(vo, falconTargetValue);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFindFalconTargetValue()
    {
        List<JobAttribute> jobAttribtues = new ArrayList<JobAttribute>();

        // Case 1: job attributes is empty
        String actual = BlaiseHelper.findFalconTargetValue(jobAttribtues);
        Assert.assertEquals(null, actual);

        // Case 2: get falcon target value from "text" attribute
        TextCondition textCondition = new TextCondition();
        textCondition.setId(111);
        textCondition.setLength(48);

        AttributeClone textAttr = new AttributeClone();
        textAttr.setCondition(textCondition);
        textAttr.setName("FalconTargetValueHD");
        textAttr.setDisplayName("Falcon Target Value");
        textAttr.setVisible(true);
        textAttr.setEditable(true);
        textAttr.setRequired(false);

        JobAttribute jobTextAttr = new JobAttribute();
        jobTextAttr.setAttribute(textAttr);
        jobTextAttr.setValue("123456_78_AAA");
        jobAttribtues.add(jobTextAttr);

        actual = BlaiseHelper.findFalconTargetValue(jobAttribtues);
        Assert.assertEquals("123456_78_AAA", actual);

        // Case 3: get falcon target value from "choicelist" attribute
        ListCondition listCondition = new ListCondition();
        listCondition.addOption("123456_78_BBB");
        listCondition.addOption("123456_78_CCC");

        AttributeClone listAttr = new AttributeClone();
        listAttr.setCondition(listCondition);
        listAttr.setName("FalconTargetValueHD");
        listAttr.setDisplayName("falcon target value");
        listAttr.setVisible(true);
        listAttr.setEditable(true);
        listAttr.setRequired(false);

        JobAttribute jobListAttr = new JobAttribute();
        jobListAttr.setAttribute(listAttr);
        List<String> listValue = new ArrayList<String>();
        listValue.add("123456_78_BBB");
        listValue.add("123456_78_CCC");
        jobListAttr.setValue(listValue, false);

        jobAttribtues.clear();
        jobAttribtues.add(jobListAttr);
        actual = BlaiseHelper.findFalconTargetValue(jobAttribtues);
        Assert.assertEquals("123456_78_BBB", actual);
    }
}
