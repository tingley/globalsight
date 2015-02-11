package com.globalsight.util.mail;

import java.util.Calendar;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.EmailInformation;

@SuppressWarnings("static-access")
public class MailerHelperTest
{
    static MailerHelper m_instance = new MailerHelper();
    
    public static String gsText;
    public static String expectedText;
    public static String expectedHTML;
    
    private static boolean isDebug = false;
    
    @BeforeClass
    public static void beforeClass()
    {
//        isDebug = true;
        gsText = "The following GlobalSight Activity has been accepted:"
                + "\r\n"
                + "Job: <span class=\"classBold\">6_409160662</span>\r\n"
                + "Locale Pair: <span class=\"classBold\">English (United States) / French (France)</span>\r\n"
                + "Activity: <span class=\"classBold\">Translation1(doTranslation)</span>\r\n"
                + "Priority: 3\r\n"
                + "Assignee: kkTest\r\n"
                + "\r\n"
                + "Please go to <a href=\"http://10.10.216.127:8080/globalsight\" target=\"_blank\">http://10.10.216.127:8080/globalsight</a> to access GlobalSight.\r\n"
                + "\r\n";
        expectedText = "The following GlobalSight Activity has been accepted:"
                + "\r\n"
                + "Job: 6_409160662\r\n"
                + "Locale Pair: English (United States) / French (France)\r\n"
                + "Activity: Translation1(doTranslation)\r\n"
                + "Priority: 3\r\n"
                + "Assignee: kkTest\r\n"
                + "\r\n"
                + "Please go to http://10.10.216.127:8080/globalsight to access GlobalSight.\r\n"
                + "\r\n";
        expectedHTML = "<html><head>"
                + "<meta http-equiv=\"Content-Type\" "
                + "content=\"text/html; charset=UTF-8\">"
                + "<style type=\"text/css\">"
                + "body {font-family: Arial, Helvetica, sans-serif; "
                + "font-size: 10pt; line-height: 15pt;}"
                + ".classBold{font-weight:bold; font-size: 10.5pt;}"
                + "</style>"
                + "</head><body>"
                + "The following GlobalSight Activity has been accepted:"
                + "<br/>"
                + "Job: <span class=\"classBold\">6_409160662</span><br/>"
                + "Locale Pair: <span class=\"classBold\">English (United States) / French (France)</span><br/>"
                + "Activity: <span class=\"classBold\">Translation1(doTranslation)</span><br/>"
                + "Priority: 3<br/>"
                + "Assignee: kkTest<br/>"
                + "<br/>"
                + "Please go to <a href=\"http://10.10.216.127:8080/globalsight\" target=\"_blank\">http://10.10.216.127:8080/globalsight</a> to access GlobalSight.<br/>"
                + "<br/>" + "</body></html>";
    }
    
    @Test
    public void testGetHTMLContext()
    {
        String html = m_instance.getHTMLContext(gsText);
        printMsg(expectedHTML, html);
        Assert.assertEquals(expectedHTML, html);
    }
    
    @Test
    public void testGetTextContext()
    {
        String text = m_instance.getTextContext(gsText);
        printMsg(expectedText, text);
        Assert.assertEquals(expectedText, text);
    }
    
    @Test
    public void testReplaceURL()
    {
        String original = "</div><a href=\"http://www.welocalize.com\" target=\"_blank\">http://www.welocalize.com</a>" +
                "<br><div><a href=\"https://10.10.214.33/globalsight\" target=\"_blank\">http://999</a>";
        String expected = "</div>http://www.welocalize.com<br><div><a href=\"https://10.10.214.33/globalsight\" target=\"_blank\">http://999</a>";
        printMsg(expected, m_instance.replaceURL(original));
        Assert.assertEquals(expected, m_instance.replaceURL(original));
        
        original = "</div><a href=\"http://www.welocalize.com:8080\" target=\"_blank\">http://www.welocalize.com:8080</a>";
        expected = "</div>http://www.welocalize.com:8080";
        printMsg(expected, m_instance.replaceURL(original));
        Assert.assertEquals(expected, m_instance.replaceURL(original));
    }
    
    @Test
    public void testGetSendFrom()
    {
        String expected, actural;
        Company com = new Company("myCompany", "JUnit Test");
        
        com.setEmail("myCompany@domain.com");
        EmailInformation user = new EmailInformation("userId","fullName",
                         "firstName.ln@domain.com", Locale.US.toString(),
                         Calendar.getInstance().getTimeZone());
        
        expected = "fullName <myCompany@domain.com>";
        actural = m_instance.getSendFrom(com, user);
        printMsg(expected, actural);
        Assert.assertEquals(expected, actural);
        
        expected = "myCompany@domain.com";
        actural = m_instance.getSendFrom(com, null);
        printMsg(expected, actural);
        Assert.assertEquals(expected, actural);        
    }
    
    public void printMsg(Object expected, Object actual)
    {
        if (!isDebug)
        {
            return;
        }

        System.out.println("Expected: " + expected);
        System.out.println("Actural : " + actual);
        System.out.println();
    }
}
