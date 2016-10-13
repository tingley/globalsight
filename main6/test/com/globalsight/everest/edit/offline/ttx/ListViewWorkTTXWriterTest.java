package com.globalsight.everest.edit.offline.ttx;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class ListViewWorkTTXWriterTest
{
    @Test
    public void testHandleTagsInSegment()
    {
        ListViewWorkTTXWriter ttxWriter = new ListViewWorkTTXWriter();

        // Test separate left "["
        String segment = "[g1][[Sample Document[/g1]";
        String actual = (String) ClassUtil.testMethod(ttxWriter,
                "handleTagsInSegment", segment);
        String expected = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:g1\">[g1]</ut>[[Sample Document<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:g1\">[/g1]</ut>";
        Assert.assertEquals(expected, actual);

        // Test separate right "]"
        segment = "[b]Welocalize - About] Us[/b]";
        actual = (String) ClassUtil.testMethod(ttxWriter,
                "handleTagsInSegment", segment);
        expected = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:b\">[b]</ut>Welocalize - About] Us<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:b\">[/b]</ut>";
        Assert.assertEquals(expected, actual);

        // Test "[...]","[[...]]","[[","]]" cases.
        segment = "[i][g2]Welocalize[/g2][/i][x3] was [[founded] in 1997, [[[[and]] is [[[[a privately held, venture backed company]].";
        actual = (String) ClassUtil.testMethod(ttxWriter,
                "handleTagsInSegment", segment);
        expected = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:i\">[i]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:g2\">[g2]</ut>Welocalize<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:g2\">[/g2]</ut><ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:i\">[/i]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:x3\">[x3]</ut> was [[founded] in 1997, [[[[and]] is [[[[a privately held, venture backed company]].";
        Assert.assertEquals(expected, actual);

        // Test more cases
        segment = "[b][[[/b][i]][/i][b][[][/b][i][[[[][/i]";
        actual = (String) ClassUtil.testMethod(ttxWriter,
                "handleTagsInSegment", segment);
        expected = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:b\">[b]</ut>[[<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:b\">[/b]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:i\">[i]</ut>]<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:i\">[/i]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:b\">[b]</ut>[[]<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:b\">[/b]</ut><ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"GS:i\">[i]</ut>[[[[]<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"GS:i\">[/i]</ut>";
        Assert.assertEquals(expected, actual);
    }

}
