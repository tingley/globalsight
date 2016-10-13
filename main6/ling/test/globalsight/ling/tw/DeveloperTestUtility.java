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

import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.common.DiplomatString;

import com.globalsight.ling.tw.*;


import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * This the main test class.
 * For starters this is a quick wrapper for development purposes.
 * Test cases will be added later.
 */
public final class DeveloperTestUtility
{

    public static void main(String args[])
    {
        String strFName = "";
    
        try
        {
            if (args.length == 0)
            {
                System.out.println("Usage: Java PlaceholdersTest [filename]");
                System.exit(0);
            }
            else
            {
                strFName = args[0];
            }
            DeveloperTestUtility DTU = new DeveloperTestUtility();
    
            DTU.doFiles(strFName);
            //DTU.doSimpleStrings();
    
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

        /**
         * Tester method.
         */
        public void doSimpleStrings()
        {
            
    /*
            // html samples
            String HtmlTmx0 = "<bpt type=\"link\" i=\"1\">&lt;a href=&quot;<sub Type=\"href\">[%%id1]</sub>&quot;&gt;</bpt>overflow<ept type=\"link\" i=\"1\">&lt;/a&gt;</ept>, <bpt type=\"link\" i=\"2\">&lt;a href=&quot;<sub type=\"href\">[%%id2]</sub>&quot;&gt;</bpt>clipping<ept type=\"link\" i=\"2\">&lt;/a&gt;</ept>, and <bpt type=\"link\" i=\"3\">&lt;a href=&quot;<sub type=\"href\">[%%id3]</sub>&quot;&gt;</bpt>visibility<ept type=\"link\" i=\"3\">&lt;/a&gt;</ept> in the visual formatting model";
            String HtmlTmx1 = "The ability to control content <bpt type=\"billlink\" i=\"1\">&lt;a href=&quot;<sub Type=\"href\">[%%id1]</sub>&quot;&gt;</bpt>overflow<ept type=\"link\" i=\"1\">&lt;/a&gt;</ept>, <bpt type=\"link\" i=\"2\">&lt;a href=&quot;<sub type=\"href\">[%%id2]</sub>&quot;&gt;</bpt>clipping<ept type=\"link\" i=\"2\">&lt;/a&gt;</ept>, and <bpt type=\"link\" i=\"3\">&lt;a href=&quot;<sub type=\"href\">[%%id3]</sub>&quot;&gt;</bpt>visibility<ept type=\"link\" i=\"3\">&lt;/a&gt;</ept> in the visual formatting model";
            String HtmlTmx2 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>Contents<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept> <bpt type=\"x_span\" i=\"2\">&lt;span style=&quot;color:red&quot;&gt;</bpt>red <bpt type=\"x_span\" i=\"3\">&lt;span style=&quot;color:blue&quot;&gt;</bpt>blue<ept type=\"x_span\" i=\"3\">&lt;/span&gt;</ept> red<ept type=\"x_span\" i=\"2\">&lt;/span&gt;</ept>";
            String HtmlTmx3 = "<bpt type=\"link\" i=\"1\">&lt;A HREF=&quot;<sub type=\"href\" locType=\"localizable\">fonts.html#font-selection</sub>&quot;&gt;</bpt>An extended font selection<ept type=\"link\" i=\"1\">&lt;/A&gt;</ept>mechanism, including intelligent matching, synthesis, and downloadable fonts. Also, the concept of system fonts has been is introduced, and a new property, <bpt type=\"link\" i=\"2\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">fonts.html#propdef-font-size-adjust</sub>&quot; class=&quot;noxref&quot;&gt;</bpt><bpt type=\"x_span\" i=\"3\">&lt;span class=&quot;propinst-font-size-adjust&quot;&gt;</bpt>&apos;font-size-adjust&apos;<ept type=\"x_span\" i=\"3\">&lt;/span&gt;</ept><ept type=\"link\" i=\"2\">&lt;/a&gt;</ept>, has been added.";
            String HtmlTmx4 = "<bpt type=\"link\" i=\"1\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">sample.html</sub>&quot;&gt;</bpt>previous<ept type=\"link\" i=\"1\">&lt;/a&gt;</ept> &amp;nbsp;<bpt type=\"link\" i=\"2\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">notes.html</sub>&quot;&gt;</bpt>next<ept type=\"link\" i=\"2\">&lt;/a&gt;</ept> &amp;nbsp;<bpt type=\"link\" i=\"3\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">cover.html#minitoc</sub>&quot;&gt;</bpt>contents<ept type=\"link\" i=\"3\">&lt;/a&gt;</ept> &amp;nbsp;<bpt type=\"link\" i=\"4\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">propidx.html</sub>&quot;&gt;</bpt>properties<ept type=\"link\" i=\"4\">&lt;/a&gt;</ept> &amp;nbsp;<bpt type=\"link\" i=\"5\">&lt;a href=&quot;<sub type=\"href\" locType=\"localizable\">indexlist.html</sub>&quot;&gt;</bpt>index<ept type=\"link\" i=\"5\">&lt;/a&gt;</ept> &amp;nbsp;";
            String HtmlTmx5 = "[ nothing ( much [ goes ( here ) ] but ) symbols ]";
            String HtmlTmx6 = "(\n                     run-around [ color: blue ]\n             )";
            String HtmlTmx7 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>Contents<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept> <bpt type=\"x-span\" i=\"2\">&lt;span style=&quot;color:red&quot;&gt;</bpt>red <bpt type=\"x-span\" i=\"3\">&lt;span style=&quot;color:blue&quot;&gt;</bpt>blue<ept type=\"x-span\" i=\"3\">&lt;/span&gt;</ept> red<ept type=\"x-span\" i=\"2\">&lt;/span&gt;</ept>";
            String HtmlTmx8 = "80% \n\n\n";
            String HtmlTmx9 = "<bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept><bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept>";
            // test with no type defined
            String HtmlTmx10 = "<bpt type=\"\" i=\"1\">&lt;a href=&quot;<sub Type=\"href\">[%%id1]</sub>&quot;&gt;</bpt>overflow<ept i=\"1\">&lt;/a&gt;</ept>, <bpt i=\"2\">&lt;a href=&quot;<sub type=\"href\">[%%id2]</sub>&quot;&gt;</bpt>clipping<ept type=\"\" i=\"2\">&lt;/a&gt;</ept>, and <bpt type=\"\" i=\"3\">&lt;a href=&quot;<sub type=\"href\">[%%id3]</sub>&quot;&gt;</bpt>visibility<ept type=\"\" i=\"3\">&lt;/a&gt;</ept> in the visual formatting model";
    
            String HtmlTmx11 = "<bpt type=\"font\" i=\"10\">&lt;FONT color=#ffffff&gt;</bpt>text<ept type=\"font\" i=\"10\">&lt;/FONT&gt;</ept>";
            String HtmlTmx12 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept><bpt type=\"bold\" i=\"1\" erasable=\"no\">&lt;strong&gt;</bpt>text2<ept type=\"bold\" i=\"1\" erasable=\"no\">&lt;/strong&gt;</ept><bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept>";
            String HtmlTmx13 = "<bpt type=\"\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept>";
            // test for IT overrides
            String HtmlTmx14 = "<it type=\"x-span\" x=\"1\" pos=\"begin\">&lt;span style=&quot;color:red&quot;&gt;</it>red<it type=\"x-span\" x=\"2\" pos=\"begin\">&lt;SPAN style=&quot;color:blue&quot;&gt;</it>blue";
            // test for unnumbered paired tags with differnt attributes IT overrides
            String HtmlTmx15 = "<it type=\"x-span\" x=\"1\" pos=\"begin\">&lt;span style=&quot;color:red&quot;&gt;</it>red<it type=\"x-span\" x=\"2\" pos=\"begin\">&lt;SPAN style=&quot;color:blue&quot;&gt;</it>blue  <bpt type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept i=\"1\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"1\" erasable=\"no\">&lt;strong&gt;</bpt>text2<ept i=\"1\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept i=\"1\">&lt;/strong&gt;</ept>";
            // also need to test for numbered NON-paired tags with different attributes IT overrides
            String HtmlTmx16 = "<it type=\"x-span\" x=\"1\" pos=\"begin\">&lt;span style=&quot;color:red&quot;&gt;</it>red<it type=\"x-span\" x=\"2\" pos=\"begin\">&lt;SPAN style=&quot;color:blue&quot;&gt;</it>blue  <bpt type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"1\" erasable=\"no\">&lt;strong&gt;</bpt>text2<ept type=\"ulined\" i=\"1\" erasable=\"no\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept>";
            // test dependency on ept attributes.
            String HtmlTmx17 = "<it type=\"x-span\" x=\"1\" pos=\"begin\">&lt;span style=&quot;color:red&quot;&gt;</it>red<it type=\"x-span\" x=\"2\" pos=\"begin\">&lt;SPAN style=&quot;color:blue&quot;&gt;</it>blue  <bpt type=\"ulined\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept i=\"1\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"2\" erasable=\"no\">&lt;strong&gt;</bpt>text2<ept i=\"2\">&lt;/strong&gt;</ept><bpt type=\"ulined\" i=\"3\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept i=\"3\">&lt;/strong&gt;</ept>";
            // Overlapping tags of same type on input - no ept attributes
            String HtmlTmx18 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<bpt type=\"bold\" i=\"2\" erasable=\"yes\">&lt;strong&gt;</bpt><ept i=\"1\">&lt;/strong&gt;</ept>text2<ept i=\"2\">&lt;/strong&gt;</ept><bpt type=\"bold\" i=\"3\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept i=\"3\">&lt;/strong&gt;</ept>";
            // Overlapping tags of same type on input - with  ept attributes
            String HtmlTmx19 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<bpt type=\"bold\" i=\"2\" erasable=\"yes\">&lt;strong&gt;</bpt><bpt type=\"italic\" i=\"4\" erasable=\"yes\">&lt;I&gt;</bpt>italictext<ept type=\"italic\" i=\"4\">&lt;/I&gt;</ept><ept type=\"bold\" i=\"1\">&lt;/strong&gt;</ept>text2<ept type=\"bold\" i=\"2\">&lt;/strong&gt;</ept><bpt type=\"bold\" i=\"3\" erasable=\"yes\">&lt;strong&gt;</bpt>text3<ept type=\"bold\" i=\"3\">&lt;/strong&gt;</ept>";
    
            // javaprop sample
            String JPTmx1 = "<ph type=\"x-space\"> </ph>leadspace<ph type=\"x-tab\">\t</ph>real tab<ph type=\"x-tab\">\t</ph>real tab value 22";
            String JPTmx2 =    "apple, banana, pear, <ph type=\"x-tab\">\t</ph>canteloupe, watermelon, kiwi, mango.";
    */
    
            // === new error checking
            String    source_segment_1  = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"                
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
                                
            String m_diplomatWithSubs = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"
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
                                  
            String map_segment_1 = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"
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
            
            String sourcePtagCopact  = "[x1][b]Shop[/b] · [l3]Auctions[/l3] ·[l4]Classifieds[/l4] ·[l5][b]Shopping[/b][/l5] ·[l7]Travel[/l7] · [l8]Yellow Pgs[/l8] · [l9]Maps[/l9] [nbsp] [b]Media[/b] · [l12]News[/l12] · [l13]Sports[/l13] · [l14][b]Stock Quotes[/b][/l14] ·[l16]TV[/l16] · [l17]Weather[/l17]";
            String sourcePtagVerbose = "[x1][bold]Shop[/bold] · [link3]Auctions[/link3] ·[link4]Classifieds[/link4] ·[link5][bold]Shopping[/bold][/link5] ·[link7]Travel[/link7] · [link8]Yellow Pgs[/link8] · [link9]Maps[/link9] [nbsp] [bold]Media[/bold] · [link12]News[/link12] · [link13]Sports[/link13] · [link14][bold]Stock Quotes[/bold][/link14] ·[link16]TV[/link16] · [link17]Weather[/link17]";
                                       
                                       
            String target_segment_1 = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it>"
                                     +"<bpt x=\"2\" i=\"1\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shop<ept i=\"1\">&lt;/B&gt;</ept> · "
                                     +"<bpt x=\"3\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8265]</sub>&quot;&gt;</bpt>Auctions<ept i=\"2\">&lt;/A&gt;</ept> · "
                                     +"<bpt x=\"4\" i=\"3\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8266]</sub>&quot;&gt;</bpt>Classifieds<ept i=\"3\">&lt;/A&gt;</ept> · "
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
                                     
            //-----------------------------------------------------
        
            String source_segment_2 = "<bpt x=\"1\" i=\"1\" type=\"font\">&lt;FONT face=<sub locType=\"localizable\" type=\"css-font-family\">[%%8305]</sub>&gt;</bpt><bpt x=\"2\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8306]</sub>&quot;&gt;</bpt><bpt x=\"3\" i=\"3\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Yahoo! Shopping<ept i=\"3\" type=\"bold\">&lt;/B&gt;</ept><ept i=\"2\" type=\"link\">&lt;/A&gt;</ept><ept i=\"1\" type=\"font\">&lt;/FONT&gt;</ept><it x=\"4\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it> - Thousands of stores. ";
            String map_segment_2 = "<bpt x=\"1\" i=\"1\" type=\"font\">&lt;FONT face=<sub locType=\"localizable\" type=\"css-font-family\">arial</sub>&gt;</bpt><bpt x=\"2\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/s/1</sub>&quot;&gt;</bpt><bpt x=\"3\" i=\"3\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Yahoo! Shopping<ept i=\"3\" type=\"bold\">&lt;/B&gt;</ept><ept i=\"2\" type=\"link\">&lt;/A&gt;</ept><ept i=\"1\" type=\"font\">&lt;/FONT&gt;</ept><it x=\"4\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it> - Thousands of stores. ";
            String target_segment_2 = "<bpt x=\"1\" i=\"1\" type=\"font\">&lt;FONT face=<sub locType=\"localizable\" type=\"css-font-family\">[%%8305]</sub>&gt;</bpt><bpt x=\"2\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8306]</sub>&quot;&gt;</bpt><bpt x=\"3\" i=\"3\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Yahoo! Shopping<ept i=\"3\" type=\"bold\">&lt;/B&gt;</ept><ept i=\"2\" type=\"link\">&lt;/A&gt;</ept><ept i=\"1\" type=\"font\">&lt;/FONT&gt;</ept><it x=\"4\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it> - Thousands of stores. ";
    
            // -----------------------------------------------------------
            String source_segment_3 = "· <bpt x=\"1\" i=\"1\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8349]</sub>&quot;&gt;</bpt>FAO Schwarz<ept i=\"1\" type=\"link\">&lt;/A&gt;</ept>";
            String map_segment_3 = "· <bpt x=\"1\" i=\"1\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://rd.yahoo.com/M=139842.1054430.2744225.811243/D=yahoo/P=m242sqb4002004/S=2716149:LCB/A=480289/R=0/*http://shop.store.yahoo.com/cgi-bin/clink?faoschwarz3+shopping:dmad/M=139842.1054430.2744225.811243/D=yahoo/P=m242sqb4002004/S=2716149:LCB/A=480289/R=1/974581965+http://us.rmi.yahoo.com/rmi/http://www.fao.com/rmi-framed-url/http://www.fao.com/FAOWeb/Ecomm/Index.cfm%3Faffiliate=6ECCFA7C-75AA-48A8-A1589DE87CFF40D0</sub>&quot;&gt;</bpt>FAO Schwarz<ept i=\"1\" type=\"link\">&lt;/A&gt;</ept>";
            String target_segment_3 = "· <bpt x=\"1\" i=\"1\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">[%%8349]</sub>&quot;&gt;</bpt>FAO Schwarz<ept i=\"1\" type=\"link\">&lt;/A&gt;</ept>";
    
    
            // Overlapping bolds (same type) and some links on input - with out ept attributes
            String HtmlTmx20 = "<bpt type=\"link\" i=\"1\">&lt;a href=&quot;<sub Type=\"href\">[%%id1]</sub>&quot;&gt;</bpt>overflow<bpt type=\"bold\" i=\"2\" erasable=\"yes\">&lt;b&gt;</bpt>text1<bpt type=\"bold\" i=\"3\" erasable=\"yes\">&lt;b&gt;</bpt><bpt type=\"italic\" i=\"4\" erasable=\"yes\">&lt;I&gt;</bpt>italictext<ept i=\"4\">&lt;/I&gt;</ept><ept i=\"3\">&lt;/b&gt;</ept>text2<ept i=\"2\">&lt;/b&gt;</ept><bpt type=\"bold\" i=\"5\" erasable=\"yes\">&lt;b&gt;</bpt>text3<ept i=\"5\">&lt;/b&gt;</ept>here<ept i=\"1\">&lt;/a&gt;</ept>, <bpt type=\"link\" i=\"6\">&lt;a href=&quot;<sub type=\"href\">[%%id2]</sub>&quot;&gt;</bpt>clipping<ept i=\"6\">&lt;/a&gt;</ept>, and <bpt type=\"link\" i=\"7\">&lt;a href=&quot;<sub type=\"href\">[%%id3]</sub>&quot;&gt;</bpt>visibility<ept i=\"7\">&lt;/a&gt;</ept>in the visual formatting model";
            String HtmlTmx21 = "<bpt type=\"bold\" i=\"1\" erasable=\"yes\">&lt;strong&gt;</bpt>text1<ept type=\"bold\" i=\"1\" erasable=\"yes\">&lt;/strong&gt;</ept>";
    
            String gutam1 = "Two &lt;thousand years ago the proudest boast was <bpt i=\"1\" type=\"italic\"\nerasable=\"yes\">&lt;EM&gt;</bpt> civis Romanus sum <ept i=\"1\" type=\"italic\"\nerasable=\"yes\">&lt;/EM&gt;</ept><ph type=\"xe-br\">&lt;BR&gt;</ph> Today in the world of\nfreedom the proudest boast is <bpt i=\"2\" type=\"italic\"\nerasable=\"yes\">&lt;EM&gt;</bpt> Ich bin ein Berliner <ept i=\"2\" type=\"italic\"\nerasable=\"yes\">&lt;/EM&gt;</ept>";
            String gutam2 = "Two thousand years ago the proudest boast was <bpt i=\"1\" type=\"italic\"erasable=\"yes\"><EM></bpt> civis Romanus sum <ept i=\"1\" type=\"italic\"erasable=\"yes\"></EM></ept><ph type=\"xe-br\"><BR></ph> Today in the world offreedom the proudest boast is <bpt i=\"2\" type=\"italic\"erasable=\"yes\"><EM></bpt> Ich bin ein Berliner <ept i=\"2\" type=\"italic\"erasable=\"yes\"></EM></ept>";
    
            //String xbug = "<it type=\"link\" x=\"1\" pos=\"begin\">&lt;A href=&quot;<sub type=\"url-a\" locType=\"localizable\">http://rd.yahoo.com/M=151161.1181133.2789227.31281/D=yahoo/P=m242sqb4002004/S=2716149:NE/A=501931/R=0/*http://adfarm.mediaplex.com/ad/ck/644-754-1039-3</sub>&quot;&gt;</it><it type=\"bold\" x=\"2\" pos=\"begin\" erasable=\"yes\">&lt;B&gt;</it>CyberRebate.com";
            String xbug = "<it type=\"bold\" x=\"2\" pos=\"begin\" erasable=\"yes\">&lt;B&gt;</it>CyberRebate &lt; com ";
            String CER = "<ph type=\"x-copywrite\" erasable=\"yes\"> &amp;copy;</ph> more text ";
    
            
            String getNumberedBolds = "<it x=\"1\" pos=\"begin\" type=\"x-small\">&lt;SMALL&gt;</it><bpt x=\"2\" i=\"1\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Shop<ept i=\"1\" erasable=\"yes\">&lt;/B&gt;</ept> · <bpt x=\"3\" i=\"2\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/a2</sub>&quot;&gt;</bpt><bpt x=\"4\" i=\"3\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Auctions<ept i=\"3\" erasable=\"yes\">&lt;/B&gt;</ept><ept i=\"2\">&lt;/A&gt;</ept> · <bpt x=\"5\" i=\"4\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/cf</sub>&quot;&gt;</bpt>Classifieds<ept i=\"4\">&lt;/A&gt;</ept> · <bpt x=\"6\" i=\"5\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/sh</sub>&quot;&gt;</bpt>Shopping<ept i=\"5\">&lt;/A&gt;</ept> · <bpt x=\"7\" i=\"6\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/ta</sub>&quot;&gt;</bpt>Travel<ept i=\"6\">&lt;/A&gt;</ept> · <bpt x=\"8\" i=\"7\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/yp</sub>&quot;&gt;</bpt>Yellow Pgs<ept i=\"7\">&lt;/A&gt;</ept> · <bpt x=\"9\" i=\"8\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/mp</sub>&quot;&gt;</bpt>Maps<ept i=\"8\">&lt;/A&gt;</ept>   <bpt x=\"10\" i=\"9\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Media<ept i=\"9\" erasable=\"yes\">&lt;/B&gt;</ept> · <bpt x=\"11\" i=\"10\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/dn</sub>&quot;&gt;</bpt>News<ept i=\"10\">&lt;/A&gt;</ept> · <bpt x=\"12\" i=\"11\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/ys</sub>&quot;&gt;</bpt>Sports<ept i=\"11\">&lt;/A&gt;</ept> · <bpt x=\"13\" i=\"12\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/sq</sub>&quot;&gt;</bpt><bpt x=\"14\" i=\"13\" type=\"bold\" erasable=\"yes\">&lt;B&gt;</bpt>Stock Quotes<ept i=\"13\" erasable=\"yes\">&lt;/B&gt;</ept><ept i=\"12\">&lt;/A&gt;</ept> · <bpt x=\"15\" i=\"14\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/tg</sub>&quot;&gt;</bpt>TV<ept i=\"14\">&lt;/A&gt;</ept> · <bpt x=\"16\" i=\"15\" type=\"link\">&lt;A href=&quot;<sub locType=\"localizable\" type=\"url-a\">http://www.yahoo.com/r/wt</sub>&quot;&gt;</bpt>Weather<ept i=\"15\">&lt;/A&gt;</ept>";
    
                
            // select a sample string
            String tmxInputString = source_segment_1;
    
            try
            {
                
                
                // convert TMX to Pseudo
                PseudoData srcPD = new PseudoData();
                TmxPseudo cvt = new TmxPseudo();
    
                srcPD.setMode(PseudoConstants.PSEUDO_COMPACT);
                srcPD.setAddables("HTML");
    
                srcPD = cvt.tmx2Pseudo(tmxInputString, srcPD);
                srcPD.setPTagTargetString(srcPD.getPTagSourceString());
                //srcPD.setPTagTargetString("[link1]ove[old] [/bold]rfl[b] [/]ow text1[bol][italic]italictext[/italic]text2[/bold][bold]text3[/bold]here[/link1], [link2]clipping[/link2], and [link3]visibility[/link3]in the visual formatting model");
                PseudoErrorChecker EC = new PseudoErrorChecker();
                String msg = EC.check(srcPD);
                if( msg != null )
                {
                    System.out.println(msg + "\n\n");
                }
                else
                {
                    System.out.println("No errors from error checker\n\n");
                }
                String RtnTmx = cvt.pseudo2Tmx(srcPD);
                DiplomatString tmpTmx = new DiplomatString( RtnTmx );
    
                if(! tmpTmx.equalsIgnoreCase(tmxInputString) )
                    System.out.println("SRC & TRG COMPARE ERROR !! \n\n");
                else
                    System.out.println("COMPARE OK \n\n");
                    
                System.out.println("Tmx   : " + tmxInputString);
                System.out.println("Psrc: " + srcPD.getPTagSourceString());
                System.out.println("Ptrg: " + srcPD.getPTagTargetString());
                System.out.println("RtnTxm: " + RtnTmx );
    
                System.out.println("Pseudo2TmxMap: ");
                Hashtable Pseudo2TmxMap = srcPD.getPseudo2TmxMap();
                for (Enumeration e = Pseudo2TmxMap.keys() ; e.hasMoreElements() ;)
                {
                    String key = (String)e.nextElement();
                    String val = (String) Pseudo2TmxMap.get(key);
                    System.out.println( key + "-->" + val );
                }
    
                System.out.println("Pseudo2NativeMap: ");
                Hashtable Tmx2NativeMap = srcPD.getPseudo2NativeMap();
                for (Enumeration e = Tmx2NativeMap.keys() ; e.hasMoreElements() ;)
                {
                    String key = (String)e.nextElement();
                    String val = (String) Tmx2NativeMap.get(key);
                    System.out.println( key + "-->" + val );
                }
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
    
    
        }

    /**
     * This is a quick wrapper for development purposes.
     * Test cases will be added later.
     *
     * @param: <|>
     * @return:
     */
    public void doFiles(String p_inPath) throws ExtractorException
    {
        String strDiplomat = "";
        String outPath ="";
    
        String tmp = p_inPath.toLowerCase();
    //C:\Development\PseudoTags\X_ptag.htm
    //C:\Development\PseudoTags\ProblemPages\yahoo_en_US.htm
        // Prep HTML
        // C:\work\ling\test\HtmlExtractor\TestFiles\changes.html
        if (tmp.endsWith(".html")  || tmp.endsWith("htm") )
        {
            int idx = p_inPath.lastIndexOf(".htm");
            outPath = p_inPath.substring(0,idx) + ".txt";
            HTMLExtractorWrapper extractor = new HTMLExtractorWrapper();
            strDiplomat = extractor.convertFile2DiplomatString(p_inPath);
    
            // get rid of first part of the file which trips up the basic parser
            strDiplomat = strDiplomat.substring(strDiplomat.indexOf("/skeleton") - 1);
            System.out.println(strDiplomat);
        }
    
        // Prep JAVAPROP
        // C:\work\ling\test\globalsight\ling\docproc\extractor\javaprop\TestFiles\jp_self_generating.properties
        if (tmp.endsWith(".properties") )
        {
            int idx = p_inPath.lastIndexOf(".properties");
            outPath = p_inPath.substring(0,idx) + ".txt";
            JPExtractorWrapper extractor = new JPExtractorWrapper();
            strDiplomat = extractor.convertFile2DiplomatString(p_inPath);
    
            // get rid of first part of the file which trips up the basic parser
            strDiplomat = strDiplomat.substring(strDiplomat.indexOf("/skeleton") - 1);
            System.out.println(strDiplomat);
        }
    
        // Prep PLAIN TEXT
        // C:\work\ling\test\globalsight\ling\docproc\extractor\plaintext\TestFiles\constitution.txt
        if (tmp.endsWith(".txt") )
        {
            int idx = p_inPath.lastIndexOf(".txt");
            outPath = p_inPath.substring(0,idx) + "Out.txt";
            PTExtractorWrapper extractor = new PTExtractorWrapper();
            strDiplomat = extractor.convertFile2DiplomatString(p_inPath);
    
            // get rid of first part of the file which trips up the basic parser
            strDiplomat = strDiplomat.substring(strDiplomat.indexOf("/skeleton") - 1);
            System.out.println(strDiplomat);
        }
    
    
        // RAW DIPLOMAT Quick and dirty, load a diplomat file from disk
        // C:\Development\PseudoTags\ProblemSegments\xdr-schema.xsl.diplomat
        if (tmp.endsWith(".diplomat") )
        {
            try
            {
                File f = new File(p_inPath);
                StringBuffer sb = new StringBuffer();
                FileReader fr = new FileReader( f );
                int c;
                while( (c = fr.read() ) != -1 )
                {
                    sb.append((char)c);
                }
                strDiplomat = sb.toString();
        
                // get rid of first part of the file which trips up the basic parser
                strDiplomat = strDiplomat.substring(strDiplomat.indexOf("/skeleton") - 1);
                System.out.println(strDiplomat);
            }
            catch (Exception e)
            {
                throw new ExtractorException(-1, e);
            }
            
        }
    
                
        // Parse for translatable and Localizable and feed to converter
        // The handler does the conversion and accumulates the results.
        try
        {
            DiplomatHandler eventHandler = new DiplomatHandler();
            DiplomatBasicParser parser = new DiplomatBasicParser(eventHandler);
            parser.parse(strDiplomat);
    
            //output
            String r = eventHandler.getResult();
            FileWriter fw = new FileWriter(outPath);
            fw.write(r);
            fw.close();
            System.out.println(r);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //System.out.println(e);
        }
    
    }
}