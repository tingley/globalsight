package com.globalsight.ling.docproc.extractor.xml;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.ling.common.srccomment.*;

import java.util.ArrayList;
import java.util.List;

public class SourceCommentTest
{

    @Test
    public void testGetSrcComment()
    {
        List<SrcCmtXmlComment> scs = new ArrayList<SrcCmtXmlComment>();
        SrcCmtXmlComment sc = new SrcCmtXmlComment("content", false);
        scs.add(sc);
        sc = new SrcCmtXmlComment("content 2011", false);
        scs.add(sc);
        
        String result = SrcCmtXmlComment.getSrcCommentContent(scs, "Here is the content 2011!");
        Assert.assertEquals("content 2011", result);
    }
    
    @Test
    public void testGetSrcComment2()
    {
        List<SrcCmtXmlComment> scs = new ArrayList<SrcCmtXmlComment>();
        SrcCmtXmlComment sc = new SrcCmtXmlComment("_locComment_text=\"([^\"]*)\"", true);
        scs.add(sc);
        
        String result = SrcCmtXmlComment.getSrcCommentContent(scs, "_locComment_text=\"{MaxLen=160}:DescriptionEduced\"");
        Assert.assertEquals("{MaxLen=160}:DescriptionEduced", result);
        
        sc = new SrcCmtXmlComment("_locComment_text=\"[^\"]*\"", true);
        scs.add(sc);
        
        result = SrcCmtXmlComment.getSrcCommentContent(scs, "_locComment_text=\"{MaxLen=160}:DescriptionEduced\"");
        Assert.assertEquals("_locComment_text=\"{MaxLen=160}:DescriptionEduced\"", result);
    }
    
    @Test
    public void testGetSrcCommentValue()
    {
        String segment = "<ph type=\"srcComment\" value=\"{MaxLen=160}:DescriptionEduced\"></ph> Collect, race, and customize new cars.";
        
        String result = SourceComment.getSrcCommentValue(segment);
        Assert.assertEquals("{MaxLen=160}:DescriptionEduced", result);
    }
    
    @Test
    public void testRemoveSrcCommentNode()
    {
        String segment = "<ph type=\"srcComment\" value=\"{MaxLen=160}:DescriptionEduced\"></ph> Collect, race, and customize new cars.";
        
        String result = SourceComment.removeSrcCommentNode(segment);
        Assert.assertEquals(" Collect, race, and customize new cars.", result);
    }
    
    @Test
    public void testGetSrcCommentValue2()
    {
        String segment = "<ph type=\"srcComment\" value=\"\n \"test\"\n\"></ph> Collect, race, and customize new cars.";
        
        String result = SourceComment.getSrcCommentValue(segment);
        Assert.assertEquals("\n \"test\"\n", result);
    }
    
    @Test
    public void testRemoveSrcCommentNode2()
    {
        String segment = "<ph type=\"srcComment\" value=\"\n \"test\"\n\"></ph> Collect, race, and customize new cars.";
        
        String result = SourceComment.removeSrcCommentNode(segment);
        Assert.assertEquals(" Collect, race, and customize new cars.", result);
    }
    
    @Test
    public void testRemoveSrcCommentNode3()
    {
        String segment = "<ph type=\"srcComment\" value=\"\n \"test\" \n\"></ph> Collect, race, and customize new cars.";
        
        String result = SourceComment.removeSrcCommentNode(segment);
        Assert.assertEquals(" Collect, race, and customize new cars.", result);
    }

    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore
                .main("com.globalsight.ling.docproc.extractor.xml.SourceCommentTest");
    }
}
