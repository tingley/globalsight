package com.globalsight.cxe.adapter.msoffice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OfficeXmlTagHelperTest
{

    /**
     * Test the merge function for PPTX
     */
    @Test
    public void testMergePptxTagsWithEmpty()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"CC3300\"/></a:solidFill></a:rPr><a:t> </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" smtClean=\"0\"/><a:t>are market competitive. </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" smtClean=\"0\"/><a:t> are market competitive. </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    public void testMergePptxTagsWithEmpty2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" dirty=\"0\" smtClean=\"0\"/><a:t>Short-Term Incentive (STI) Objectives </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" dirty=\"0\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"0070C0\"/></a:solidFill></a:rPr><a:t/></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" dirty=\"0\" smtClean=\"0\"/><a:t>Short-Term Incentive (STI) Objectives </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }

    @Test
    public void testMergePptxTagsWithUline()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr kumimoji=\"0\" lang=\"en-US\" sz=\"1000\" b=\"0\" i=\"1\" u=\"sng\" strike=\"noStrike\" cap=\"none\" normalizeH=\"0\" baseline=\"0\" smtClean=\"0\"><a:ln><a:noFill/></a:ln><a:solidFill><a:schemeClr val=\"tx1\"/></a:solidFill><a:effectLst/><a:latin typeface=\"Verdana\" pitchFamily=\"34\" charset=\"0\"/><a:ea typeface=\"ＭＳ Ｐゴシック\" pitchFamily=\"22\" charset=\"-128\"/></a:rPr><a:t>with finance programs</a:t></a:r>";
        String f2 = "<a:r><a:rPr kumimoji=\"0\" lang=\"en-US\" sz=\"1000\" b=\"0\" i=\"1\" u=\"none\" strike=\"noStrike\" cap=\"none\" normalizeH=\"0\" baseline=\"0\" smtClean=\"0\"><a:ln><a:noFill/></a:ln><a:solidFill><a:schemeClr val=\"tx1\"/></a:solidFill><a:effectLst/><a:latin typeface=\"Verdana\" pitchFamily=\"34\" charset=\"0\"/><a:ea typeface=\"ＭＳ Ｐゴシック\" pitchFamily=\"22\" charset=\"-128\"/></a:rPr><a:t>)</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr kumimoji=\"0\" lang=\"en-US\" sz=\"1000\" b=\"0\" i=\"1\" u=\"sng\" strike=\"noStrike\" cap=\"none\" normalizeH=\"0\" baseline=\"0\" smtClean=\"0\"><a:ln><a:noFill/></a:ln><a:solidFill><a:schemeClr val=\"tx1\"/></a:solidFill><a:effectLst/><a:latin typeface=\"Verdana\" pitchFamily=\"34\" charset=\"0\"/><a:ea typeface=\"ＭＳ Ｐゴシック\" pitchFamily=\"22\" charset=\"-128\"/></a:rPr><a:t>with finance programs</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }

    @Test
    public void testMergePptxTagsWithUline2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"1700\" u=\"sng\" dirty=\"0\"><a:solidFill><a:schemeClr val=\"tx2\"><a:lumMod val=\"60000\"/><a:lumOff val=\"40000\"/></a:schemeClr></a:solidFill></a:rPr><a:t>EOC</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1700\" dirty=\"0\"/><a:t> – </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"1700\" u=\"sng\" dirty=\"0\"><a:solidFill><a:schemeClr val=\"tx2\"><a:lumMod val=\"60000\"/><a:lumOff val=\"40000\"/></a:schemeClr></a:solidFill></a:rPr><a:t>EOC</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }

    @Test
    public void testMergePptxTagsWithSize()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"7200\" i=\"1\" dirty=\"0\" smtClean=\"0\"><a:latin typeface=\"Verdana\"/><a:cs typeface=\"Verdana\"/></a:rPr><a:t>Q</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"2400\" i=\"1\" dirty=\"0\" smtClean=\"0\"><a:latin typeface=\"Verdana\"/><a:cs typeface=\"Verdana\"/></a:rPr><a:t>:</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"7200\" i=\"1\" dirty=\"0\" smtClean=\"0\"><a:latin typeface=\"Verdana\"/><a:cs typeface=\"Verdana\"/></a:rPr><a:t>Q</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }

    @Test
    public void testMergePptxTagsWithSize2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" dirty=\"0\" smtClean=\"0\"/><a:t>Step 5: Enter quantities for users licenses and additional storage that requires activation. </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1200\" dirty=\"0\" smtClean=\"0\"/><a:t>(Note: the system automatically defaults the user licenses to </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" dirty=\"0\" smtClean=\"0\"/><a:t>Step 5: Enter quantities for users licenses and additional storage that requires activation. </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }

    @Test
    public void testMergePptxTagsWithSizeAndEmpty()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"4000\" b=\"1\"/><a:t>You have completed this course.</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"4400\" b=\"1\"><a:solidFill><a:schemeClr val=\"tx2\"/></a:solidFill></a:rPr><a:t> </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"4000\" b=\"1\"/><a:t>You have completed this course.</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithUlineAndEmpty()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" u=\"sng\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>Ads Properties</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>          </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" u=\"sng\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>Ads Properties</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithUlineAndEmpty2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>         </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" u=\"sng\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>Campaign Properties</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-AU\" sz=\"2000\" i=\"1\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>         </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithBaseline()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" b=\"0\" dirty=\"0\" smtClean=\"0\"/><a:t>Net Income (Loss) </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" b=\"0\" baseline=\"30000\" dirty=\"0\" smtClean=\"0\"/><a:t>(3)</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" b=\"0\" dirty=\"0\" smtClean=\"0\"/><a:t>Net Income (Loss) </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithColor()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"/><a:t>Retirements* between dates: </a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>No MTI</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"/><a:t>Retirements* between dates: </a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithColor2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"/><a:t>Retirements* between dates: </a:t></a:r>";
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>No MTI</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" b=\"0\" dirty=\"0\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill></a:rPr><a:t>No MTI</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithColor3()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>uk</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>.doubleclick.net </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>uk</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithHlinkClick()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"><a:hlinkClick r:id=\"rId4\"/></a:rPr><a:t>EPDPIPPSubCouncil@JohnDeere.com</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"/><a:t>. </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"><a:hlinkClick r:id=\"rId4\"/></a:rPr><a:t>EPDPIPPSubCouncil@JohnDeere.com</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTagsWithHlinkClick2()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"/><a:t>If you have additional questions, contact </a:t></a:r><a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"><a:hlinkClick r:id=\"rId4\"/></a:rPr><a:t>EPDPIPPSubCouncil@JohnDeere.com</a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" sz=\"1600\" dirty=\"0\" smtClean=\"0\"/><a:t>If you have additional questions, contact </a:t></a:r><a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    @Test
    public void testMergePptxTags()
    {
        int filetype = OfficeXmlHelper.OFFICE_PPTX;
        String f1 = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>uk</a:t></a:r>";
        String f2 = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>.doubleclick.net </a:t></a:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);

        System.out.println(merged);
        String expected = "<a:r><a:rPr lang=\"en-US\" altLang=\"zh-CN\" sz=\"2000\" smtClean=\"0\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ea typeface=\"宋体\" pitchFamily=\"2\" charset=\"-122\"/></a:rPr><a:t>uk</a:t></a:r>";
        Assert.assertEquals(expected, merged);
    }
    
    /**
     * Test the merge function for DOCX
     */
    @Test
    public void testMergeDocxTags()
    {
        int filetype = OfficeXmlHelper.OFFICE_DOCX;
        String f1 = "<w:r w:rsidR=\"00DC53C3\"><w:tab/><w:t>Shipping Costs</w:t></w:r>";
        String f2 = "<w:r w:rsidR=\"00880973\"><w:t xml:space=\"preserve\"> Scenarios</w:t></w:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        String expected = "<w:r w:rsidR=\"00DC53C3\"><w:tab/><w:t xml:space=\"preserve\">Shipping Costs Scenarios</w:t></w:r>";
        Assert.assertEquals(expected, merged);

        f1 = "<w:r w:rsidR=\"00DC53C3\"><w:t>5.3.3</w:t></w:r>";
        f2 = merged;
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r w:rsidR=\"00DC53C3\"><w:t>5.3.3</w:t></w:r>";
        Assert.assertEquals(expected, merged);
    }
    
    /**
     * Test the merge function for DOCX with style
     */
    @Test
    public void testMergeDocxTagsWithStyle()
    {
        int filetype = OfficeXmlHelper.OFFICE_DOCX;
        String f1 = "<w:r><w:t xml:space=\"preserve\">This text should be translated, except for this </w:t></w:r>";
        String f2 = "<w:r w:rsidR=\"00B25FB0\" w:rsidRPr=\"0053397B\"><w:rPr><w:rStyle w:val=\"tw4winInternal\"/></w:rPr><w:t>tw4winInternal</w:t></w:r>";

        String merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        String expected = "<w:r><w:t xml:space=\"preserve\">This text should be translated, except for this </w:t></w:r>";
        Assert.assertEquals(expected, merged);

        f1 = "<w:r w:rsidR=\"00B25FB0\" w:rsidRPr=\"0053397B\"><w:rPr><w:rStyle w:val=\"tw4winInternal\"/></w:rPr><w:t>tw4winInternal</w:t></w:r>";
        f2 = "<w:r><w:t xml:space=\"preserve\"> statement.</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r w:rsidR=\"00B25FB0\" w:rsidRPr=\"0053397B\"><w:rPr><w:rStyle w:val=\"tw4winInternal\"/></w:rPr><w:t>tw4winInternal</w:t></w:r>";
        Assert.assertEquals(expected, merged);
        
        f1 = "<w:r w:rsidRPr=\"00882CE5\"><w:rPr><w:rStyle w:val=\"1Char\"/><w:rFonts w:hint=\"eastAsia\"/></w:rPr><w:t>style1</w:t></w:r>";
        f2 = "<w:r w:rsidRPr=\"00882CE5\"><w:rPr><w:rStyle w:val=\"2Char\"/><w:rFonts w:hint=\"eastAsia\"/></w:rPr><w:t>style2</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r w:rsidRPr=\"00882CE5\"><w:rPr><w:rStyle w:val=\"1Char\"/><w:rFonts w:hint=\"eastAsia\"/></w:rPr><w:t>style1</w:t></w:r>";
        Assert.assertEquals(expected, merged);
        
        f1 = "<w:r w:rsidR=\"001648B2\" w:rsidRPr=\"001648B2\"><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>U</w:t></w:r>";
        f2 = "<w:r w:rsidR=\"001648B2\" w:rsidRPr=\"001648B2\"><w:rPr><w:rFonts w:hint=\"eastAsia\"/><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>p</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r w:rsidR=\"001648B2\" w:rsidRPr=\"001648B2\"><w:rPr><w:rFonts w:hint=\"eastAsia\"/><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>Up</w:t></w:r>";
        Assert.assertEquals(expected, merged);
        
        f1 = "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>up</w:t></w:r>";
        f2 = "<w:r><w:rPr><w:vertAlign w:val=\"subscript\"/></w:rPr><w:t>down</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>up</w:t></w:r>";
        Assert.assertEquals(expected, merged);
        
        f1 = "<w:r w:rsidRPr=\"0024750C\"><w:rPr><w:u w:val=\"single\"/></w:rPr><w:t>U</w:t></w:r>";
        f2 = "<w:r w:rsidRPr=\"0024750C\"><w:rPr><w:rFonts w:hint=\"eastAsia\"/><w:u w:val=\"single\"/></w:rPr><w:t>u</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r w:rsidRPr=\"0024750C\"><w:rPr><w:rFonts w:hint=\"eastAsia\"/><w:u w:val=\"single\"/></w:rPr><w:t>Uu</w:t></w:r>";
        Assert.assertEquals(expected, merged);
        
        f1 = "<w:r><w:t>before </w:t></w:r>";
        f2 = "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>up</w:t></w:r>";
        merged = OfficeXmlTagHelper.getMergedTags(filetype, f1, f2);
        System.out.println(merged);
        expected = "<w:r><w:t>before </w:t></w:r>";
        Assert.assertEquals(expected, merged);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(OfficeXmlTagHelperTest.class);
    }

}
