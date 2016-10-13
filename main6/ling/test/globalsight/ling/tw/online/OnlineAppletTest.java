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
package test.globalsight.ling.tw.online;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatString;
import com.globalsight.ling.tw.online.OnlineApplet;
public class OnlineAppletTest extends TestCase
{
    private OnlineApplet m_placeholderUtils = null;
    private String m_diplomat = null;
    private String m_ptagCompact = null;
    private String m_ptagVerbose = null;
    private String m_colorTestDiplomat = null;
    private String m_colorTestPtagCompact = null;
    private String m_colorTestPtagVerbose = null;
    private String m_diplomatWithoutSubs = null;
    private String m_diplomatWithSubs = null;
    private String m_lenTestPtagCompact = null;
    private String m_lenTestPtagVerbose = null;
    public OnlineAppletTest(String p_name)
    {
        super(p_name);
        m_placeholderUtils = new OnlineApplet();
        m_placeholderUtils.init();
    }

    public void setUp()
    {
        m_diplomat =
            "This ia a "
            + "<bpt x=\"1\" type=\"italic\" i=\"1\" erasable=\"yes\">&lt;i&gt;</bpt>"
            + "big"
            + "<ept i=\"1\">&lt;/i&gt;</ept>"
            + "<bpt x=\"2\" type=\"bold\" i=\"2\"erasable=\"yes\">&lt;b&gt;</bpt>"
            + "fat"
            + "<ept i=\"2\">&lt;/b&gt;</ept>"
            + " segment.";
        m_ptagCompact = "This ia a [i]big[/i][b]fat[/b] segment.";
        m_ptagVerbose = "This ia a [italic]big[/italic][bold]fat[/bold] segment.";
        // for color testing with escapes
        m_colorTestDiplomat =
            "This ia a "
            + "<bpt x=\"1\" type=\"italic\" i=\"1\" erasable=\"yes\">&lt;i&gt;</bpt>"
            + "big"
            + "<ept i=\"1\">&lt;/i&gt;</ept>"
            + "<bpt x=\"2\" type=\"bold\" i=\"2\"erasable=\"yes\">&lt;b&gt;</bpt>"
            + "fat"
            + "<ept i=\"2\">&lt;/b&gt;</ept>"
            + " segment. &lt; &gt; &amp; &quot;";
        m_colorTestPtagCompact = "This ia a <font color=\"red\">[i]</font>big<font color=\"red\">[/i]</font><font color=\"red\">[b]</font>fat<font color=\"red\">[/b]</font> segment. &lt; &gt; &amp; &quot;";
        m_colorTestPtagVerbose = "This ia a <font color=\"red\">[italic]</font>big<font color=\"red\">[/italic]</font><font color=\"red\">[bold]</font>fat<font color=\"red\">[/bold]</font> segment. &lt; &gt; &amp; &quot;";
        // for length test
        m_diplomatWithoutSubs  = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"                
                                +"<bpt x=\"2\" i=\"1\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shop<ept i=\"1\">&lt;/B&gt;</ept> · "                           
                                +"<bpt x=\"3\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8265]</sub>&quot;&gt;</bpt>Auctions<ept i=\"2\">&lt;/A&gt;</ept> ·"             
                                +"<bpt x=\"4\" i=\"3\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8266]</sub>&quot;&gt;</bpt>Classifieds<ept i=\"3\">&lt;/A&gt;</ept> ·"
                                +"<bpt x=\"5\" i=\"4\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8267]</sub>&quot;&gt;</bpt><bpt x=\"6\" i=\"5\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shopping<ept i=\"5\">&lt;/B&gt;</ept><ept i=\"4\">&lt;/A&gt;</ept> ·"
                                +"<bpt x=\"7\" i=\"6\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8268]</sub>&quot;&gt;</bpt>Travel<ept i=\"6\">&lt;/A&gt;</ept> · "
                                +"<bpt x=\"8\" i=\"7\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8269]</sub>&quot;&gt;</bpt>Yellow Pgs<ept i=\"7\">&lt;/A&gt;</ept> · "
                                +"<bpt x=\"9\" i=\"8\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8270]</sub>&quot;&gt;</bpt>Maps<ept i=\"8\">&lt;/A&gt;</ept> "
                                +"<ph x=\"10\" type=\"x-nbspace\" erasable=\"yes\">&amp;nbsp;</ph> "
                                +"<bpt x=\"11\" i=\"9\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Media<ept i=\"9\">&lt;/B&gt;</ept> · "
                                +"<bpt x=\"12\" i=\"10\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8271]</sub>&quot;&gt;</bpt>News<ept i=\"10\">&lt;/A&gt;</ept> · "
                                +"<bpt x=\"13\" i=\"11\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8272]</sub>&quot;&gt;</bpt>Sports<ept i=\"11\">&lt;/A&gt;</ept> · "
                                +"<bpt x=\"14\" i=\"12\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8273]</sub>&quot;&gt;</bpt><bpt x=\"15\" i=\"13\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Stock Quotes<ept i=\"13\">&lt;/B&gt;</ept><ept i=\"12\">&lt;/A&gt;</ept> ·"
                                +"<bpt x=\"16\" i=\"14\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8274]</sub>&quot;&gt;</bpt>TV<ept i=\"14\">&lt;/A&gt;</ept> · "
                                +"<bpt x=\"17\" i=\"15\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8275]</sub>&quot;&gt;</bpt>Weather<ept i=\"15\">&lt;/A&gt;</ept>";
        // for length test
        m_diplomatWithSubs = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"
                              +"<bpt x=\"2\" i=\"1\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shop<ept i=\"1\">&lt;/B&gt;</ept> · "
                              +"<bpt x=\"3\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/a2</sub>&quot;&gt;</bpt>Auctions<ept i=\"2\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"4\" i=\"3\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/cf</sub>&quot;&gt;</bpt>Classifieds<ept i=\"3\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"5\" i=\"4\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/sh</sub>&quot;&gt;</bpt><bpt x=\"6\" i=\"5\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shopping<ept i=\"5\">&lt;/B&gt;</ept><ept i=\"4\">&lt;/A&gt;</ept> ·"
                              +"<bpt x=\"7\" i=\"6\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/ta</sub>&quot;&gt;</bpt>Travel<ept i=\"6\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"8\" i=\"7\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/yp</sub>&quot;&gt;</bpt>YellowPgs<ept i=\"7\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"9\" i=\"8\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/mp</sub>&quot;&gt;</bpt>Maps<ept i=\"8\">&lt;/A&gt;</ept> "
                              +"<ph x=\"10\" type=\"x-nbspace\" erasable=\"yes\">&amp;nbsp;</ph> "
                              +"<bpt x=\"11\" i=\"9\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Media<ept i=\"9\">&lt;/B&gt;</ept> · "
                              +"<bpt x=\"12\" i=\"10\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/dn</sub>&quot;&gt;</bpt>News<ept i=\"10\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"13\" i=\"11\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/ys</sub>&quot;&gt;</bpt>Sports<ept i=\"11\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"14\" i=\"12\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/sq</sub>&quot;&gt;</bpt><bpt x=\"15\" i=\"13\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Stock Quotes<ept i=\"13\">&lt;/B&gt;</ept><ept i=\"12\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"16\" i=\"14\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/tg</sub>&quot;&gt;</bpt>TV<ept i=\"14\">&lt;/A&gt;</ept> · "
                              +"<bpt x=\"17\" i=\"15\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/wt</sub>&quot;&gt;</bpt>Weather<ept i=\"15\">&lt;/A&gt;</ept>";
        m_lenTestPtagCompact  = "[x1][b]Shop[/b] · [l3]Auctions[/l3] ·[l4]Classifieds[/l4] ·[l5][b]Shopping[/b][/l5] ·[l7]Travel[/l7] · [l8]Yellow Pgs[/l8] · [l9]Maps[/l9] [nbsp] [b]Media[/b] · [l12]News[/l12] · [l13]Sports[/l13] · [l14][b]Stock Quotes[/b][/l14] ·[l16]TV[/l16] · [l17]Weather[/l17]";
        m_lenTestPtagVerbose  = "[x1][bold]Shop[/bold] · [link3]Auctions[/link3] ·[link4]Classifieds[/link4] ·[link5][bold]Shopping[/bold][/link5] ·[link7]Travel[/link7] · [link8]Yellow Pgs[/link8] · [link9]Maps[/link9] [nbsp] [bold]Media[/bold] · [link12]News[/link12] · [link13]Sports[/link13] · [link14][bold]Stock Quotes[/bold][/link14] ·[link16]TV[/link16] · [link17]Weather[/link17]";
    }
    public static Test suite()
    {
        return new TestSuite(OnlineAppletTest.class);
    }
    public void test1()
    {
        Exception ex = null;
        String compact = null;
        String verbose = null;
        try
        {
            m_placeholderUtils.setInputSegment(m_diplomat, "", "HTML");
            compact = m_placeholderUtils.getCompact();
            verbose = m_placeholderUtils.getVerbose();
        }
        catch(Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(compact.compareTo(m_ptagCompact), 0);
        assertEquals(verbose.compareTo(m_ptagVerbose), 0);
    }
    public void test2()
    {
        Exception ex = null;
        String compact = null;
        String diplomat = null;
        try
        {
            m_placeholderUtils.setInputSegment(m_diplomat, "", "HTML");
            compact = m_placeholderUtils.getCompact();
            diplomat = m_placeholderUtils.getTargetDiplomat(compact);
        }
        catch(Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        try
        {
            DiplomatString srcDiplomat = new DiplomatString(m_diplomat);
            assertTrue("Round-trip compare failed", srcDiplomat.equalsIgnoreCase(diplomat));
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    }
    public void test3()
    {
        Exception ex = null;
        String compact = null;
        String diplomat = null;
        String error = null;
        try
        {
            m_placeholderUtils.setInputSegment(m_diplomat, "", "HTML");
            compact = m_placeholderUtils.getCompact();
            error = m_placeholderUtils.errorCheck(compact);
        }
        catch(DiplomatBasicParserException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNull(error);
    }
    public void testColorPTags()
    {
        Exception ex = null;
        String compact = null;
        String verbose = null;
        try
        {
            m_placeholderUtils.setInputSegment(m_colorTestDiplomat, "", "HTML");
            compact = m_placeholderUtils.makeCompactColoredPtags(m_colorTestDiplomat);
            verbose = m_placeholderUtils.makeVerboseColoredPtags(m_colorTestDiplomat);
        }
        catch(DiplomatBasicParserException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals("Compare compact failed", compact.compareTo(m_colorTestPtagCompact), 0);
        assertEquals("Compare verbose failed", verbose.compareTo(m_colorTestPtagVerbose), 0);
    }
    public void testStringLengthValidation()
    {
        Exception ex = null;
        String simulateTrgCompact = null;
        try
        {
            // simulate normal source being set
            m_placeholderUtils.setInputSegment(m_diplomatWithoutSubs, "", "HTML");
            // simulate neccesary discard operation to set data
            simulateTrgCompact = m_placeholderUtils.getCompact(); // discard
            // simulate new error check	method
            String err = null;
            // test ignore length - length = 0
            assertNull("Ignore legnth failed", (err = m_placeholderUtils.errorCheck(simulateTrgCompact, m_diplomatWithSubs,  0, "", 0, ""))) ;
            // test gxml too long 
            assertNotNull("Gxml legnth did not fail", (err = m_placeholderUtils.errorCheck(simulateTrgCompact, m_diplomatWithSubs,  100, "UTF8", 0, ""))) ;
            // native too long	
            assertNotNull("Native legnth did not fail", (err = m_placeholderUtils.errorCheck(simulateTrgCompact, m_diplomatWithSubs,  0, "", 100, "8859_1"))) ;
            // normal pass	
            assertNull("Did not pass when it should", (err = m_placeholderUtils.errorCheck(simulateTrgCompact, m_diplomatWithSubs,  4000, "UTF8", 4000, "8859_1"))) ;
            // IF YOU DO VERBOSE DO IT AFTER
            //verbose = m_placeholderUtils.getVerbose();
        }
        catch(DiplomatBasicParserException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals("Compare compact failed", (simulateTrgCompact.compareTo(m_lenTestPtagCompact)), 0);
    }
}
