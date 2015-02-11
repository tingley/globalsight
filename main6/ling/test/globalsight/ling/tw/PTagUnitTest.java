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
package test.globalsight.ling.tw;

import java.util.Hashtable;
/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatString;
import com.globalsight.ling.tw.*;


/**
*/
public class PTagUnitTest extends TestCase
{      

    private PseudoData m_PData = null;
    private TmxPseudo m_Cvt = null;

    private String m_diplomatHTMLIn = null;
    private String m_giantDiplomatHTMLIn = null;
    private String m_diplomatHTMLReversed = null;
    
    private String m_PTagHTMLCompact = null;
    private String m_giantPTagHTMLCompact = null;
    private String m_PTagHTMLCompactReversed = null;
    
    private String m_PTagHTMLVerbose = null;
    private String m_giantPTagHTMLVerbose = null;
    private String m_PTagHTMLVerboseReversed = null;

    /**
    */
    public PTagUnitTest(String p_name)
    {
        super(p_name);
    }

    /**
    */
    public void setUp()
    {
        Exception ex = null;
    
        m_diplomatHTMLIn =
            "<ph type=\"x-space\" x=\"0\"> "
            + "</ph>"
            + "<ph type=\"x-tab\" x=\"1\">\t</ph>"
            + "<ph type=\"lb\" x=\"2\">\n</ph>"
            + "<ph type=\"x-formfeed\" x=\"3\">\f</ph>"
            + "The "
            + "<bpt type=\"bold\" i=\"1\" erasable=\"yes\" >&lt;b&gt;</bpt>"
            + "<bpt type=\"italic\" i=\"2\" erasable=\"yes\" >&lt;I&gt;</bpt>"
            + "ability to "
            + "<ept i=\"1\">&lt;/b&gt;</ept>"
            + "control content "
            + "<ept i=\"2\">&lt;/I&gt;</ept>"
            + "<bpt type=\"link\" i=\"3\" x=\"6\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"7\">[%%111]</sub>&quot;&gt;</bpt>"
            + "overflow"
            + "<ept i=\"3\">&lt;/a&gt;</ept>"
            + ", "
            + "<it type=\"ulined\" x=\"8\" pos=\"begin\" erasable=\"yes\">&lt;U&gt;</it>"
            + "<bpt type=\"link\" i=\"4\" x=\"9\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"10\">[%%222]</sub>&quot;&gt;</bpt>"
            + "clipping"
            + "<ept i=\"4\">&lt;/a&gt;</ept>"
            + ", and "
            + "<bpt type=\"link\" i=\"5\" x=\"11\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"12\">[%%333]</sub>&quot;&gt;</bpt>"
            + "visibility"
            + "<ept i=\"5\">&lt;/a&gt;</ept>"
            + "<it type=\"x-span\" x=\"13\" pos=\"begin\">&lt;SPAN style=&quot;color:blu&quot;&gt;</it>"
            + "<bpt type=\"font\" i=\"6\" x=\"14\">&lt;FONT color=#ffffff&gt;</bpt>"
            + " in the visual formatting model."
            + "<ept i=\"6\">&lt;/FONT&gt;</ept>";
    
        m_PTagHTMLCompact = "[sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
        m_PTagHTMLCompactReversed = "The [b][i]ability to [/b]control content [/i], , and [f14] in the visual formatting model.[/f14][x13][l11]visibility[/l11][l9]clipping[/l9][x8][l6]overflow[/l6][ff3][lb2][t1][sp0]";
        m_giantPTagHTMLCompact ="[sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14][sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";

        
        m_PTagHTMLVerbose = "[space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
        m_PTagHTMLVerboseReversed = "The [bold][italic]ability to [/bold]control content [/italic], , and [font14] in the visual formatting model.[/font14][x13][link11]visibility[/link11][link9]clipping[/link9][x8][link6]overflow[/link6][formfeed3][lineBreak2][tab1][space0]";
        m_giantPTagHTMLVerbose = "[space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14][space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
    
        
        m_diplomatHTMLReversed =
              "The "
            + "<bpt type=\"bold\" i=\"1\" erasable=\"yes\" >&lt;b&gt;</bpt>"
            + "<bpt type=\"italic\" i=\"2\" erasable=\"yes\" >&lt;I&gt;</bpt>"
            + "ability to "
            + "<ept i=\"1\">&lt;/b&gt;</ept>"
            + "control content "
            + "<ept i=\"2\">&lt;/I&gt;</ept>"
            + ", "
    
            + ", and "
    
            + "<bpt type=\"font\" i=\"6\" x=\"14\">&lt;FONT color=#ffffff&gt;</bpt>"
            + " in the visual formatting model."
            + "<ept i=\"6\">&lt;/FONT&gt;</ept>"
    
            + "<it type=\"x-span\" x=\"13\" pos=\"begin\">&lt;SPAN style=&quot;color:blu&quot;&gt;</it>"
                            
            + "<bpt type=\"link\" i=\"5\" x=\"11\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"12\">[%%333]</sub>&quot;&gt;</bpt>"
            + "visibility"
            + "<ept i=\"5\">&lt;/a&gt;</ept>"
            
            + "<bpt type=\"link\" i=\"4\" x=\"9\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"10\">[%%222]</sub>&quot;&gt;</bpt>"
            + "clipping"
            + "<ept i=\"4\">&lt;/a&gt;</ept>"
    
            + "<it type=\"ulined\" x=\"8\" pos=\"begin\" erasable=\"yes\">&lt;U&gt;</it>"
                            
            + "<bpt type=\"link\" i=\"3\" x=\"6\">&lt;a href=&quot;<sub type=\"url\" locType=\"localizable\" x=\"7\">[%%111]</sub>&quot;&gt;</bpt>"
            + "overflow"
            + "<ept i=\"3\">&lt;/a&gt;</ept>"
            
            + "<ph type=\"x-formfeed\" x=\"3\">\f</ph>"
            + "<ph type=\"lb\" x=\"2\">\n</ph>"
            + "<ph type=\"x-tab\" x=\"1\">\t</ph>"
            + "<ph type=\"x-space\" x=\"0\"> </ph>";
            
        // build giant string.
        StringBuffer giantDiplomatHTMLIn = new StringBuffer();
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        giantDiplomatHTMLIn.append(m_diplomatHTMLIn);
        m_giantDiplomatHTMLIn = giantDiplomatHTMLIn.toString();
    
        try
        {
            m_PData = new PseudoData();
            m_Cvt = new TmxPseudo();
        }
        catch(Exception e)
        {
            ex = e;
        }
        assertNull(ex);
    
    }

    /**
    * Insert the method's description here.
    * Creation date: (8/16/2000 10:40:43 AM)
    */
    public static Test suite()
    {
        return new TestSuite(PTagUnitTest.class);
    }

    /**
    * Test compact errors
    *
    */
    public void testErrorCheckerCompactHTML()
    {
    
        Exception ex = null;
        String compact = null;
        String verbose = null;
        String diplomatCompactOut = null;
    
        m_PData.setAddables("HTML");
    
        // Tmx 2 Pseudo
        try
        {
            // get verbose
            m_PData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, m_PData);
            verbose = m_PData.getPTagSourceString();
    
            // get compact
            m_PData.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, m_PData);
            compact = m_PData.getPTagSourceString();
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
        assertTrue("E1: Compact compare failed", compact.compareTo(m_PTagHTMLCompact) == 0);
        assertTrue("E2: Verbose compare failed", verbose.compareTo(m_PTagHTMLVerbose) == 0);
    
    
        // ERROR CHECKER TESTS
        //m_ptagCompact = "[sp1][t2][lb3][ff4]The [b][i]ability to [/b]control content [/i][l5]overflow[/l5], [x6][l7]clipping[/l7], and [l8]visibility[/l8][x9][f10] in the visual formatting model.[/f10]";
        //m_ptagVerbose = "[space1][tab2][lineBreak3][formfeed4]The [bold][italic]ability to [/bold]control content [/italic][link5]overflow[/link5], [x6][link7]clipping[/link7], and [link8]visibility[/link8][x9][font10] in the visual formatting model.[/font10]";
    
        PseudoErrorChecker EC = new PseudoErrorChecker();
        m_PData.setLocale("en_US");
    
        try
        {
            String err ="";
    
            // LEGAL
    
            // Compact - sanity check using compact versions of original source as target
            m_PData.setPTagTargetString(compact);
            assertNull("E2.5: Compact Sanity check failed", (err = EC.check(m_PData) ));
            //System.out.println("E2.5 " + err);
    
            // Compact - mixed case addables [b][/B][u][/U][I][/I] ***
            String CompactLegalMixedCaseAdded = "*** [b][/B][u][/U][I][/I] ***  [sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(CompactLegalMixedCaseAdded);
            assertNull("E3: Compact mixed case addables failed", (err = EC.check(m_PData) ));
            //System.out.println("E3 " + err);
    
            // Verbose - legal to use verbose ADDABLE tags in compact mode [bold][/bold][underline][/underline][italic][/italic] ***
            String VerboselegalAdded = "*** [bold][/bold][underline][/underline][italic][/italic] ***  [sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(VerboselegalAdded);
            assertNull("E4: Legal verbose-addables in compact mode failed", (err = EC.check(m_PData) ));
            //System.out.println("E4 " + err);

            // Compact - legal to delete ADDABLE tags [b][/b][u][/u][i][/i] ***
            String CompactMinusAddables = "[sp0][t1][lb2][ff3]The ability to control content [l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(CompactMinusAddables);
            assertNull("E4: Legal deletion of addables tags failed", (err = EC.check(m_PData) ));
            //System.out.println("E4a " + err);

            // ILLEGAL
    
            // Verbose - cannot error check using verbose version of original source on compact PseudoData
            m_PData.setPTagTargetString(verbose);
            assertNotNull("E5: Verbose Sanity Passed!! - change test case", (err = EC.check(m_PData) ));
            //System.out.println("E5 " + err);
    
            // Compact - User needs to use escapes - ***
            String CompactNeedsEscape = "[sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14] *** EXAMPLE - ILLEGAL UNESCAPED BRACKET AFTER ANY TAGS [ *** ";
            m_PData.setPTagTargetString(CompactNeedsEscape);
            assertNotNull("E6: Ilegal escape test failed", (err = EC.check(m_PData) ));
            //System.out.println("E6 " + err);
    
            // Compact - missing [x8], [/f11] - replaced with ***
            String CompactNonErasableMissing = "[sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], ****[l9]clipping[/l9], and [l11]visibility**** [x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(CompactNonErasableMissing);
            assertNotNull("E7: Missing tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E7 " + err);
    
            // Compact -  illegal tag [hello] added ***
            String CompactIllegalAdded = "*** [hello] *** [sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(CompactIllegalAdded);
            assertNotNull("E8: Illegal tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E8 " + err);
    
            // Compact -  unbalanced  [l5]
            String CompactUnbalanced = "*** [b] [u] *** [sp0][t1][lb2][ff3]The [b][i]ability to [/b]control content [/i][l6]overflow[/l6], [x8][l9]clipping[/l9], and [l11]visibility[/l11][x13][f14] in the visual formatting model.[/f14]";
            m_PData.setPTagTargetString(CompactUnbalanced);
            assertNotNull("E9: Unbalanced test failed", (err = EC.check(m_PData) ));
            //System.out.println("E9 " + err);
    
    
            // see if round trip still works after all the error testing
    
            // Set/convert original compact form to diplomat
            m_PData.setPTagTargetString(compact);
            diplomatCompactOut = m_Cvt.pseudo2Tmx(m_PData);
    
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
    
    
        try
        {
            // round-trip compare
            DiplomatString DIn = new DiplomatString(m_diplomatHTMLIn);
            boolean b = DIn.equalsIgnoreCase(diplomatCompactOut);
            assertTrue("Diplomat compact round-trip failed", b );
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    
    }

    /**
    * Test Giant String - compact errors
    *
    */
    public void testErrorCheckerGiantCompactHTML()
    {
    
        Exception ex = null;
        String compact = null;
        String verbose = null;
        String diplomatCompactOut = null;
    
        m_PData.setAddables("HTML");
    
        // Tmx 2 Pseudo
        try
        {
            // get verbose
            m_PData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(m_giantDiplomatHTMLIn, m_PData);
            verbose = m_PData.getPTagSourceString();
    
            // get compact
            m_PData.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(m_giantDiplomatHTMLIn, m_PData);
            compact = m_PData.getPTagSourceString();
    
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
        assertTrue("E1: Compact compare failed", compact.compareTo(m_giantPTagHTMLCompact) == 0);
        assertTrue("E2: Verbose compare failed", verbose.compareTo(m_giantPTagHTMLVerbose) == 0);
    
    
        // ERROR CHECKER TESTS
        PseudoErrorChecker EC = new PseudoErrorChecker();
    
        try
        {
            String err ="";
    
            // LEGAL
    
            // Compact - sanity check using compact versions of original source as target
            m_PData.setPTagTargetString(compact);
            assertNull("E2.5: Compact Sanity check failed", (err = EC.check(m_PData) ));
            //System.out.println("E2.5 " + err);
    
    
            // ILLEGAL
    
            // Verbose - cannot error check using verbose version of original source on compact PseudoData
            m_PData.setPTagTargetString(verbose);
            assertNotNull("E5: Verbose Sanity Passed!! - change test case", (err = EC.check(m_PData) ));
            //System.out.println("E5 " + err);
    
            // Compact - User needs to use escapes - ***
            String CompactNeedsEscape = m_giantPTagHTMLCompact + "[bla bla.";
            m_PData.setPTagTargetString(CompactNeedsEscape);
            assertNotNull("E6: Ilegal escape test failed", (err = EC.check(m_PData) ));
            //System.out.println("E6 " + err);
    
            // Compact - missing [x9], [/f10] - replaced with ***
            String CompactNonErasableMissing = "";
            m_PData.setPTagTargetString(CompactNonErasableMissing);
            assertNotNull("E7: Missing tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E7 " + err);
    
            // Compact -  illegal tag [hello] added ***
            String CompactIllegalAdded = m_giantPTagHTMLCompact + "*** [hello] ***";
            m_PData.setPTagTargetString(CompactIllegalAdded);
            assertNotNull("E8: Illegal tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E8 " + err);
    
            // Compact -  unbalanced  [l5]
            String CompactUnbalanced = m_giantPTagHTMLCompact + "*** [b] [u] ***";
            m_PData.setPTagTargetString(CompactUnbalanced);
            assertNotNull("E9: Unbalanced test failed", (err = EC.check(m_PData) ));
            //System.out.println("E9 " + err);
    
    
            // see if round trip still works after all the error testing
    
            // Set/convert original compact form to diplomat
            m_PData.setPTagTargetString(compact);
            diplomatCompactOut = m_Cvt.pseudo2Tmx(m_PData);
    
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
    
    
        try
        {
            // round-trip compare
            DiplomatString DIn = new DiplomatString(m_giantDiplomatHTMLIn);
            boolean b = DIn.equalsIgnoreCase(diplomatCompactOut);
            assertTrue("Diplomat compact round-trip failed", b );
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    
    }

    /**
    * Test verbose erros.
    *
    */
    public void testErrorCheckerVerboseHTML()
    {
    
        Exception ex = null;
        String compact = null;
        String verbose = null;
        String diplomatVerboseOut = null;
    
        m_PData.setAddables("HTML");
    
        // Tmx 2 Pseudo
        try
        {
            // get compact
            m_PData.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, m_PData);
            compact = m_PData.getPTagSourceString();
    
            // get verbose
            m_PData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, m_PData);
            verbose = m_PData.getPTagSourceString();
    
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
        assertTrue("E1: Compact compare failed", compact.compareTo(m_PTagHTMLCompact) == 0);
        assertTrue("E2: Verbose compare failed", verbose.compareTo(m_PTagHTMLVerbose) == 0);
    
    
        // ERROR CHECKER TESTS
        //m_ptagCompact = "[sp1][t2][lb3][ff4]The [b][i]ability to [/b]control content [/i][l5]overflow[/l5], [x6][l7]clipping[/l7], and [l8]visibility[/l8][x9][f10] in the visual formatting model.[/f10]";
        //m_ptagVerbose = "[space1][tab2][lineBreak3][formfeed4]The [bold][italic]ability to [/bold]control content [/italic][link5]overflow[/link5], [x6][link7]clipping[/link7], and [link8]visibility[/link8][x9][font10] in the visual formatting model.[/font10]";
    
        PseudoErrorChecker EC = new PseudoErrorChecker();
    
        try
        {
            String err ="";
    
            // LEGAL
    
            // Verbose - sanity check using verbose versions of original source as target
            m_PData.setPTagTargetString(verbose);
            assertNull("E2.1: Verbose Sanity check Failed!!", (err = EC.check(m_PData) ));
            //System.out.println("E2.1 " + err);
    
            // Verbose - mixed case addables [bold][/BoLd][underLINE][/UnderLINe][ITalic][/italic] ***
            String VerboseLegalMixedCaseAdded = "*** [bold][/BoLd][underLINE][/UnderLINe][ITalic][/italic] *** [space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
            m_PData.setPTagTargetString(VerboseLegalMixedCaseAdded);
            assertNull("E3: Verbose mixed case addables failed", (err = EC.check(m_PData) ));
            //System.out.println("E3 " + err);
    
            // Compact - legal compact ADDABLE tags used in verbose mode [b][/b][u][/u][i][/i] ***
            String CompactlegalAdded = "*** [b][/b][u][/u][i][/i] *** [space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
            m_PData.setPTagTargetString(CompactlegalAdded);
            assertNull("E4: Legal Compact-addable tags used in verbose mode failed", (err = EC.check(m_PData) ));
            //System.out.println("E4 " + err);

            // Verbose - legal to delete ADDABLE tags [bold][/bold][underline][/underline][italic][/italic] ***
            String VerboseMinusAddables = "[space0][tab1][lineBreak2][formfeed3]The ability to control content [link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";        
            m_PData.setPTagTargetString(VerboseMinusAddables);
            assertNull("E4: Legal deletion of addables tags failed", (err = EC.check(m_PData) ));
            //System.out.println("E4a " + err);
            
            // ILLEGAL
    
            // Compact - cannot error check using compact version of original source on verbose PseudoData
            m_PData.setPTagTargetString(compact);
            assertNotNull("E5: Compact Sanity check Passed!! -Change test case.", (err = EC.check(m_PData) ));
            //System.out.println("E5 " + err);
    
            // Verbose - User needs to use escapes - ***
            String VerboseNeedsEscape = "[space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]  *** EXAMPLE - ILLEGAL UNESCAPED BRACKET AFTER ANY TAGS [ *** ";
            m_PData.setPTagTargetString(VerboseNeedsEscape);
            assertNotNull("E6: Ilegal escape test failed", (err = EC.check(m_PData) ));
            //System.out.println("E6 " + err);
    
            // Verbose - missing [x8][/font11] - replaced with ***
            String VerboseNonErasableMissing = "[space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], **** [link9]clipping[/link9], and [link11]visibility****[x13][font14] in the visual formatting model.[/font14]";
            m_PData.setPTagTargetString(VerboseNonErasableMissing);
            assertNotNull("E7: Missing tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E7 " + err);
    
            // Verbose -  illegal tag [hello] added ***
            String VerboseIllegalAdded = "*** [hello] *** [space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
            m_PData.setPTagTargetString(VerboseIllegalAdded);
            assertNotNull("E8: Illegal tags test failed", (err = EC.check(m_PData) ));
            //System.out.println("E8 " + err);
    
            // Verbose -  unbalanced  [link5]
            String VerboseUnbalanced = "*** [bold] [underline] *** [space0][tab1][lineBreak2][formfeed3]The [bold][italic]ability to [/bold]control content [/italic][link6]overflow[/link6], [x8][link9]clipping[/link9], and [link11]visibility[/link11][x13][font14] in the visual formatting model.[/font14]";
            m_PData.setPTagTargetString(VerboseUnbalanced);
            assertNotNull("E9: Unbalanced test failed", (err = EC.check(m_PData) ));
            //System.out.println("E9 " + err);
    
    
            // prepare for round trip test after allt he error checking
            // Set/convert verbose form to diplomat.
            m_PData.setPTagTargetString(verbose);
            diplomatVerboseOut = m_Cvt.pseudo2Tmx(m_PData);
    
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
    
        try
        {
            DiplomatString DIn = new DiplomatString(m_diplomatHTMLIn);
            boolean b  = DIn.equalsIgnoreCase(diplomatVerboseOut);
            assertTrue("Diplomat verbose round-trip failed!!", b );
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    
    }

    /**
    * Simple conversion Test of Diplomat to P-Tag (compact and Verbose).
    * Does not use PTag error checker.
    */
    public void testRoundTripWithoutErrorCheckerHTML()
    {
        Exception ex = null;
        String compact = null;
        String verbose = null;
        String diplomatCompactOut = null;
        String diplomatVerboseOut = null;
    
        PseudoData PDataVerbose;
        PseudoData PDataCompact;
            
    
        // Tmx 2 Pseudo
        try
        {
            PDataVerbose = new PseudoData();
            PDataVerbose.setAddables("hTml");
            
            PDataCompact = new PseudoData();
            PDataCompact.setAddables("html");
            
            // get verbose
            PDataVerbose.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, PDataVerbose);
            verbose = PDataVerbose.getPTagSourceString();
    
            // get compact
            PDataCompact.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, PDataCompact);
            compact = PDataCompact.getPTagSourceString();
        
            assertTrue("Compact compare failed", compact.compareTo(m_PTagHTMLCompact) == 0);
            assertTrue("Verbose compare failed", verbose.compareTo(m_PTagHTMLVerbose) == 0);
    
            // Pseudo 2 Tmx
    
            // confirm there is no output when target is NOT set
            diplomatCompactOut = m_Cvt.pseudo2Tmx(PDataCompact);
            assertTrue("E1:Unexpected diplomat output", diplomatCompactOut.length() <= 0 );
    
            // convert compact form back to diplomat
            PDataCompact.setPTagTargetString(compact);
            diplomatCompactOut = m_Cvt.pseudo2Tmx(PDataCompact);
    
            // convert verbose form back to diplomat.
            PDataVerbose.setPTagTargetString(verbose);
            diplomatVerboseOut = m_Cvt.pseudo2Tmx(PDataVerbose);
    
            // round-trip compare compact
            DiplomatString DIn = new DiplomatString(m_diplomatHTMLIn);
            boolean b = DIn.equalsIgnoreCase(diplomatCompactOut);
            assertTrue("Diplomat compact round-trip failed", b );
    
            // round-trip compare verbose
            b = DIn.equalsIgnoreCase(diplomatVerboseOut);
            assertTrue("Diplomat verbose round-trip failed", b );
            
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    
    }

    /**
    * Test movable
    */
    public void testGenericMovableHTML()
    {
        Exception ex = null;
        String compact = null;
        String verbose = null;
        String diplomatCompactOut = null;
        String diplomatCompactReversedOut = null;
        String diplomatVerboseOut = null;
        String diplomatVerboseReversedOut = null;
    
        final PseudoData PDataVerbose;
        final PseudoData PDataCompact;
    
        PseudoErrorChecker EC = new PseudoErrorChecker();
        String err = null;
    
        // Tmx 2 Pseudo
        try
        {
            PDataVerbose = new PseudoData();
            PDataVerbose.setAddables("HTML");
            
            PDataCompact = new PseudoData();
            PDataCompact.setAddables("html");
            
            // =====================
            // First we SETUP SEPARATE STATIC PDATA FOR BOTH COMPACT and VERBOSE.
            // Then we will simulate target manipulations and error check them against these static object.
            // =====================
    
            // setup compact
            PDataCompact.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, PDataCompact);
            compact = PDataCompact.getPTagSourceString();
        
            // setup verbose
            PDataVerbose.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(m_diplomatHTMLIn, PDataVerbose);
            verbose = PDataVerbose.getPTagSourceString();
    
            assertEquals("Compact compare failed", compact.compareTo(m_PTagHTMLCompact), 0);
            assertEquals("Verbose compare failed", verbose.compareTo(m_PTagHTMLVerbose), 0);
    
    
            // SIMULATE A MOVE - and a first pass error check - A SAVE - and RE-INIT =====================
    
            // submit a REVERSED ptag COMPACT and then get back the diplomat
            PDataCompact.setPTagTargetString(m_PTagHTMLCompactReversed);
            assertNull("Initial CompactMove error check failed", (err = EC.check(PDataCompact) ));
            // get reversed diplomat and make a new ptag string based on the reversed diplomat
            diplomatCompactReversedOut = m_Cvt.pseudo2Tmx(PDataCompact);
            PseudoData tmpPData = new PseudoData();
            tmpPData.setAddables("htML");
            tmpPData.setMode(PseudoConstants.PSEUDO_COMPACT);
            m_Cvt.tmx2Pseudo(diplomatCompactReversedOut, tmpPData);
            String newPtagCompactReversed = tmpPData.getPTagSourceString();
            assertEquals("Compact compare 2 failed", m_PTagHTMLCompactReversed.compareTo(newPtagCompactReversed), 0);
            
            // submit REVERSED ptag VERBOSE and get back the verbose diplomat
            PDataVerbose.setPTagTargetString(m_PTagHTMLVerboseReversed);
            assertNull("Initial VerboseMove error check failed", (err = EC.check(PDataVerbose) ));
            // get reversed diplomat and make a new ptag string based on the reversed diplomat
            diplomatVerboseReversedOut = m_Cvt.pseudo2Tmx(PDataVerbose);
            tmpPData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_Cvt.tmx2Pseudo(diplomatVerboseReversedOut, tmpPData);
            String newPtagVerboseReversed = tmpPData.getPTagSourceString();
            assertEquals("Verbose compare failed", m_PTagHTMLVerboseReversed.compareTo(newPtagVerboseReversed), 0);
    
            //**** NOTE: *****
            // In the following string compares, the DiplomatString object normalizes 
            // the i attribute and remove all x values.
            //**** NOTE: *****
    
            // round-trip compare compact
            DiplomatString DIn = new DiplomatString(m_diplomatHTMLReversed);
            boolean b = DIn.equalsIgnoreCase(diplomatCompactReversedOut);
            assertTrue("Diplomat compact round-trip failed", b );
    
            // round-trip compare verbose
            b = DIn.equalsIgnoreCase(diplomatVerboseReversedOut);
            assertTrue("Diplomat verbose round-trip failed", b );
    
                
            // 2nd PASS - ERROR CHECK ==================
            
            // submit a COMPACT ptag string based on the REVERSED diplomat output from above
            PDataCompact.setPTagTargetString(newPtagCompactReversed);
            assertNull("Compact failed", (err = EC.check(m_PData) ));
            diplomatCompactReversedOut = m_Cvt.pseudo2Tmx(PDataCompact);
                
            // submit a VERBOSE ptag string based on the REVERSED diplomat output from above
            PDataVerbose.setPTagTargetString(newPtagVerboseReversed);
            assertNull("Verbose failed", (err = EC.check(m_PData) ));
            diplomatVerboseReversedOut = m_Cvt.pseudo2Tmx(PDataVerbose);
            
            /*	
            assertEquals("Compact compare failed", compact.compareTo(m_PTagHTMLCompact), 0);
            assertEquals("Verbose compare failed", verbose.compareTo(m_PTagHTMLVerbose), 0);
            */
    
            //**** NOTE: *****
            // In the following string compares, the DiplomatString object normalizes 
            // the i attribute and remove all x values.
            //**** NOTE: *****
            
            // round-trip compare compact
            b = DIn.equalsIgnoreCase(diplomatCompactReversedOut);
            assertTrue("Diplomat compact round-trip failed", b );
    
            // round-trip compare verbose
            b = DIn.equalsIgnoreCase(diplomatVerboseReversedOut);
            assertTrue("Diplomat verbose round-trip failed", b );
            
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    }

    /**
    * Miscellaneous tests
    */
    public void testMiscBehaviorHTML()
    {
    
        try
        {
            m_PData = new PseudoData();
            m_PData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_PData.setAddables("html");
    
            // In verbose mode - with no TMX type defined - should defualt to compact.
            String gxmlIn  = "<ph type=\"\" x=\"1\"> </ph><ph type=\"tab\" x=\"2\">	</ph><bpt type=\"\" i=\"1\" x=\"5\">&lt;a href=&quot;<sub Type=\"href\">[%%id1]</sub>&quot;&gt;</bpt>overflow<ept i=\"1\">&lt;/a&gt;</ept>, <bpt i=\"2\" x=\"6\">&lt;a href=&quot;<sub type=\"href\">[%%id2]</sub>&quot;&gt;</bpt>clipping<ept type=\"\" i=\"2\">&lt;/a&gt;</ept>, and <bpt type=\"\" i=\"3\" x=\"7\">&lt;a href=&quot;<sub type=\"href\">[%%id3]</sub>&quot;&gt;</bpt>visibility<ept type=\"\" i=\"3\">&lt;/a&gt;</ept> in the visual formatting model";
            String ptagExpected = "[x1][tab2][g5]overflow[/g5], [g6]clipping[/g6], and [g7]visibility[/g7] in the visual formatting model";
            String gxmlOut = "";
            m_Cvt.tmx2Pseudo(gxmlIn, m_PData);
            String ptagOut = m_PData.getPTagSourceString();
            assertEquals( "Empty TYPE test failed ", ptagOut.compareTo(ptagExpected), 0);
    
    
            // SPECIFIC TESTs FOR "it" =======
            
            String err = null;
    
            // define one "it" erasable and one set to defualts
            gxmlIn = "<it type=\"x-span\" x=\"1\" pos=\"begin\" erasable=\"yes\">&lt;span style=&quot;color:red&quot;&gt;</it>red<it type=\"x-span\" x=\"2\" pos=\"begin\">&lt;SPAN style=&quot;color:blue&quot;&gt;</it>blue";
            ptagExpected = "[x1]red[x2]blue";
            gxmlOut = "";
            m_Cvt.tmx2Pseudo(gxmlIn, m_PData);
            ptagOut = m_PData.getPTagSourceString();
            // Test forced to [x]
            assertEquals( "<it> all forced to [x] - failed ", ptagOut.compareTo(ptagExpected), 0);
            // Test forced to non-erasable
            PseudoErrorChecker EC = new PseudoErrorChecker();
            m_PData.setPTagTargetString("red blue");
            assertNotNull("<it> all always non-erasable - failed", (err = EC.check(m_PData) ));
    
            // In verbose mode - an "it" with type bold/underline/italic should still be compact and numbered.
            gxmlIn = "<it type=\"bold\" x=\"2\" pos=\"begin\" erasable=\"yes\">&lt;B&gt;</it>CyberRebate &lt; com <it type=\"italic\" x=\"3\" pos=\"begin\" erasable=\"yes\">&lt;I&gt;</it><it type=\"underline\" x=\"4\" pos=\"begin\" erasable=\"yes\">&lt;U&gt;</it>";
            ptagExpected = "[x2]CyberRebate < com [x3][x4]";
            gxmlOut = "";
            m_Cvt.tmx2Pseudo(gxmlIn, m_PData);
            ptagOut = m_PData.getPTagSourceString();
            Hashtable map = m_PData.getPseudo2NativeMap();
            Hashtable map2 = m_PData.getPseudo2TmxMap();
            assertEquals( "bold/U/I <it> failed to be numbered [x]", ptagOut.compareTo(ptagExpected), 0);
    
    
            // END SPECIFIC TESTs FOR "it" =======
            
        }
        catch(PseudoParserException e)
        {
            fail(e.toString());
        }
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    }

    /**
    * Miscellaneous tests
    */
    public void testMiscBehaviorXML()
    {
    
        try
        {
            m_PData = new PseudoData();
            m_PData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            m_PData.setAddables("xml");
    
            // Test that global addables are not recognized as addable in XML
            // VERBOSE
            String gxmlIn  = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\" x=\"1\" >&lt;b&gt;</bpt><bpt type=\"italic\" i=\"1\" erasable=\"yes\" x=\"2\" >&lt;i&gt;</bpt><bpt type=\"ulined\" i=\"1\" erasable=\"yes\" x=\"3\" >&lt;u&gt;</bpt>";
            String ptagExpected = "[bold1][italic2][underline3]";
            String gxmlOut = "";
            m_Cvt.tmx2Pseudo(gxmlIn, m_PData);
            String ptagOut = m_PData.getPTagSourceString();
            assertEquals( "Addbales in XML failed ", ptagOut.compareTo(ptagExpected), 0);
            // COMPACT
            m_PData.setMode(PseudoConstants.PSEUDO_COMPACT);
            ptagExpected = "[b1][i2][u3]";
            gxmlOut = "";
            m_Cvt.tmx2Pseudo(gxmlIn, m_PData);
            ptagOut = m_PData.getPTagSourceString();
            assertEquals( "Addbales in XML failed ", ptagOut.compareTo(ptagExpected), 0);
    
        }
        /*catch(PseudoParserException e)
        {
            fail(e.toString());
        }*/
        catch(DiplomatBasicParserException e)
        {
            fail(e.toString());
        }
    }

   /**
    * Test detection of invalid G(XML) characters
    */
    public void testInvalidGXMLCharacters()
    {        
        try
        {
            PseudoErrorChecker EC = new PseudoErrorChecker();
            PseudoData pData = new PseudoData();
            pData.setLocale("en_US");
            String err = null;
           
            // invalid control characters - add to front
            StringBuffer sb = new StringBuffer("This is the front of the segment.");
            for(char ch = '\u0000'; ch <= '\u001f'; ch++)
            { 
                // allowed
                if(ch == '\u0009' || ch == '\r' || ch == '\n' ) continue;
                
                sb.insert(0,ch);
                m_Cvt.tmx2Pseudo(sb.toString(), pData);
                pData.setPTagTargetString(sb.toString());
                System.out.println( EC.check(pData));
                assertNotNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
                sb.deleteCharAt(0);                
            }    
            
            // invalid control characters - add to middle of long segment 
            sb = new StringBuffer("This is the front of the segment and this is the end of the long segment.");
            int pos = 33;
            for(char ch = '\u0000'; ch <= '\u001f'; ch++)
            { 
                // allowed
                if(ch == '\u0009' || ch == '\r' || ch == '\n' ) continue;
                
                sb.insert(pos,ch);
                m_Cvt.tmx2Pseudo(sb.toString(), pData);
                pData.setPTagTargetString(sb.toString());
                System.out.println( EC.check(pData));
                assertNotNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
                sb.deleteCharAt(pos);
            }    

            // invalid control characters - add to middle of short segment 
            sb = new StringBuffer("This is the short segment.");
            pos = 10;
            for(char ch = '\u0000'; ch <= '\u001f'; ch++)
            { 
                // allowed
                if(ch == '\u0009' || ch == '\r' || ch == '\n' ) continue;
                
                sb.insert(pos,ch);
                m_Cvt.tmx2Pseudo(sb.toString(), pData);
                pData.setPTagTargetString(sb.toString());
                System.out.println( EC.check(pData));
                assertNotNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
                sb.deleteCharAt(pos);
            }    

            // invalid control characters - add to end
            sb = new StringBuffer("This is the front of the segment and this is the end of the long segment.");
            for(char ch = '\u0000'; ch <= '\u001f'; ch++)
            { 
                // allowed
                if(ch == '\u0009' || ch == '\r' || ch == '\n' ) continue;

                sb.append(ch);
                m_Cvt.tmx2Pseudo(sb.toString(), pData);
                pData.setPTagTargetString(sb.toString());
                System.out.println( EC.check(pData));
                assertNotNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
                sb.deleteCharAt(sb.length()-1);
            }    

            // Tab / linefeed / carriage return should be allowed
            sb = new StringBuffer();
            sb.insert(0,'\u0009');
            sb.insert(1,'\r');
            sb.insert(2,'\n');
            m_Cvt.tmx2Pseudo(sb.toString(), pData);
            pData.setPTagTargetString(sb.toString());
            //System.out.println( EC.check(pData));
            assertNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
            
            // everything above the control char range should pass - we test a few
            sb = new StringBuffer();
            for(char ch = '\u0020'; ch <= '\u002f'; ch++)
            {
                sb.insert(0,ch);
                m_Cvt.tmx2Pseudo(sb.toString(), pData);
                pData.setPTagTargetString(sb.toString());
                //System.out.println( EC.check(pData));
                assertNull("testInvalidGXMLCharacters Failed: " + err, (err = EC.check(pData) ));
            }    

        }
        catch(Exception e)
        {
            fail(e.toString());
        }
    }
}
