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

}
