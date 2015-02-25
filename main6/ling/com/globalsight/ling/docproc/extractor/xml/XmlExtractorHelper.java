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
package com.globalsight.ling.docproc.extractor.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

/**
 * Firstly used for entity protection as XML parser cannot recognize entity and
 * character at the same time.
 * 
 * @since GBS-3702
 */
public class XmlExtractorHelper
{
    private final static Logger logger = Logger
            .getLogger(XmlExtractorHelper.class);

    // this is the original thought: to protect all the entities in below list
    public final static List<String> ENTITIES = new ArrayList<String>();
    // a list contains the 252 allowed entities described in HTML 4
    // specifications, as classified at url:
    // http://www.elizabethcastro.com/html/extras/entities.html#punct
    static
    {
        ENTITIES.add("&#38;");
        ENTITIES.add("&#62;");
        ENTITIES.add("&#60;");
        ENTITIES.add("&#34;");
        ENTITIES.add("&#180;");
        ENTITIES.add("&#184;");
        ENTITIES.add("&#710;");
        ENTITIES.add("&#175;");
        ENTITIES.add("&#183;");
        ENTITIES.add("&#732;");
        ENTITIES.add("&#168;");
        ENTITIES.add("&#193;");
        ENTITIES.add("&#225;");
        ENTITIES.add("&#194;");
        ENTITIES.add("&#226;");
        ENTITIES.add("&#198;");
        ENTITIES.add("&#230;");
        ENTITIES.add("&#192;");
        ENTITIES.add("&#224;");
        ENTITIES.add("&#197;");
        ENTITIES.add("&#229;");
        ENTITIES.add("&#195;");
        ENTITIES.add("&#227;");
        ENTITIES.add("&#196;");
        ENTITIES.add("&#228;");
        ENTITIES.add("&#199;");
        ENTITIES.add("&#231;");
        ENTITIES.add("&#201;");
        ENTITIES.add("&#233;");
        ENTITIES.add("&#202;");
        ENTITIES.add("&#234;");
        ENTITIES.add("&#200;");
        ENTITIES.add("&#232;");
        ENTITIES.add("&#208;");
        ENTITIES.add("&#240;");
        ENTITIES.add("&#203;");
        ENTITIES.add("&#235;");
        ENTITIES.add("&#205;");
        ENTITIES.add("&#237;");
        ENTITIES.add("&#206;");
        ENTITIES.add("&#238;");
        ENTITIES.add("&#204;");
        ENTITIES.add("&#236;");
        ENTITIES.add("&#207;");
        ENTITIES.add("&#239;");
        ENTITIES.add("&#209;");
        ENTITIES.add("&#241;");
        ENTITIES.add("&#211;");
        ENTITIES.add("&#243;");
        ENTITIES.add("&#212;");
        ENTITIES.add("&#244;");
        ENTITIES.add("&#338;");
        ENTITIES.add("&#339;");
        ENTITIES.add("&#210;");
        ENTITIES.add("&#242;");
        ENTITIES.add("&#216;");
        ENTITIES.add("&#248;");
        ENTITIES.add("&#213;");
        ENTITIES.add("&#245;");
        ENTITIES.add("&#214;");
        ENTITIES.add("&#246;");
        ENTITIES.add("&#352;");
        ENTITIES.add("&#353;");
        ENTITIES.add("&#223;");
        ENTITIES.add("&#222;");
        ENTITIES.add("&#254;");
        ENTITIES.add("&#218;");
        ENTITIES.add("&#250;");
        ENTITIES.add("&#219;");
        ENTITIES.add("&#251;");
        ENTITIES.add("&#217;");
        ENTITIES.add("&#249;");
        ENTITIES.add("&#220;");
        ENTITIES.add("&#252;");
        ENTITIES.add("&#221;");
        ENTITIES.add("&#253;");
        ENTITIES.add("&#255;");
        ENTITIES.add("&#376;");
        ENTITIES.add("&#162;");
        ENTITIES.add("&#164;");
        ENTITIES.add("&#8364;");
        ENTITIES.add("&#163;");
        ENTITIES.add("&#165;");
        ENTITIES.add("&#166;");
        ENTITIES.add("&#8226;");
        ENTITIES.add("&#169;");
        ENTITIES.add("&#8224;");
        ENTITIES.add("&#8225;");
        ENTITIES.add("&#8260;");
        ENTITIES.add("&#8230;");
        ENTITIES.add("&#161;");
        ENTITIES.add("&#8465;");
        ENTITIES.add("&#191;");
        ENTITIES.add("&#8206;");
        ENTITIES.add("&#8212;");
        ENTITIES.add("&#8211;");
        ENTITIES.add("&#172;");
        ENTITIES.add("&#8254;");
        ENTITIES.add("&#170;");
        ENTITIES.add("&#186;");
        ENTITIES.add("&#182;");
        ENTITIES.add("&#8240;");
        ENTITIES.add("&#8242;");
        ENTITIES.add("&#8243;");
        ENTITIES.add("&#8476;");
        ENTITIES.add("&#174;");
        ENTITIES.add("&#8207;");
        ENTITIES.add("&#167;");
        ENTITIES.add("&#173;");
        ENTITIES.add("&#185;");
        ENTITIES.add("&#8482;");
        ENTITIES.add("&#8472;");
        ENTITIES.add("&#8222;");
        ENTITIES.add("&#171;");
        ENTITIES.add("&#8220;");
        ENTITIES.add("&#8249;");
        ENTITIES.add("&#8216;");
        ENTITIES.add("&#187;");
        ENTITIES.add("&#8221;");
        ENTITIES.add("&#8250;");
        ENTITIES.add("&#8217;");
        ENTITIES.add("&#8218;");
        ENTITIES.add("&#8195;");
        ENTITIES.add("&#8194;");
        ENTITIES.add("&#160;");
        ENTITIES.add("&#8201;");
        ENTITIES.add("&#8205;");
        ENTITIES.add("&#8204;");
        ENTITIES.add("&#176;");
        ENTITIES.add("&#247;");
        ENTITIES.add("&#189;");
        ENTITIES.add("&#188;");
        ENTITIES.add("&#190;");
        ENTITIES.add("&#8805;");
        ENTITIES.add("&#8804;");
        ENTITIES.add("&#8722;");
        ENTITIES.add("&#178;");
        ENTITIES.add("&#179;");
        ENTITIES.add("&#215;");
        ENTITIES.add("&#8501;");
        ENTITIES.add("&#8743;");
        ENTITIES.add("&#8736;");
        ENTITIES.add("&#8776;");
        ENTITIES.add("&#8745;");
        ENTITIES.add("&#8773;");
        ENTITIES.add("&#8746;");
        ENTITIES.add("&#8709;");
        ENTITIES.add("&#8801;");
        ENTITIES.add("&#8707;");
        ENTITIES.add("&#402;");
        ENTITIES.add("&#8704;");
        ENTITIES.add("&#8734;");
        ENTITIES.add("&#8747;");
        ENTITIES.add("&#8712;");
        ENTITIES.add("&#9001;");
        ENTITIES.add("&#8968;");
        ENTITIES.add("&#8970;");
        ENTITIES.add("&#8727;");
        ENTITIES.add("&#181;");
        ENTITIES.add("&#8711;");
        ENTITIES.add("&#8800;");
        ENTITIES.add("&#8715;");
        ENTITIES.add("&#8713;");
        ENTITIES.add("&#8836;");
        ENTITIES.add("&#8853;");
        ENTITIES.add("&#8744;");
        ENTITIES.add("&#8855;");
        ENTITIES.add("&#8706;");
        ENTITIES.add("&#8869;");
        ENTITIES.add("&#177;");
        ENTITIES.add("&#8719;");
        ENTITIES.add("&#8733;");
        ENTITIES.add("&#8730;");
        ENTITIES.add("&#9002;");
        ENTITIES.add("&#8969;");
        ENTITIES.add("&#8971;");
        ENTITIES.add("&#8901;");
        ENTITIES.add("&#8764;");
        ENTITIES.add("&#8834;");
        ENTITIES.add("&#8838;");
        ENTITIES.add("&#8721;");
        ENTITIES.add("&#8835;");
        ENTITIES.add("&#8839;");
        ENTITIES.add("&#8756;");
        ENTITIES.add("&#913;");
        ENTITIES.add("&#945;");
        ENTITIES.add("&#914;");
        ENTITIES.add("&#946;");
        ENTITIES.add("&#935;");
        ENTITIES.add("&#967;");
        ENTITIES.add("&#916;");
        ENTITIES.add("&#948;");
        ENTITIES.add("&#917;");
        ENTITIES.add("&#949;");
        ENTITIES.add("&#919;");
        ENTITIES.add("&#951;");
        ENTITIES.add("&#915;");
        ENTITIES.add("&#947;");
        ENTITIES.add("&#921;");
        ENTITIES.add("&#953;");
        ENTITIES.add("&#922;");
        ENTITIES.add("&#954;");
        ENTITIES.add("&#923;");
        ENTITIES.add("&#955;");
        ENTITIES.add("&#924;");
        ENTITIES.add("&#956;");
        ENTITIES.add("&#925;");
        ENTITIES.add("&#957;");
        ENTITIES.add("&#937;");
        ENTITIES.add("&#969;");
        ENTITIES.add("&#927;");
        ENTITIES.add("&#959;");
        ENTITIES.add("&#934;");
        ENTITIES.add("&#966;");
        ENTITIES.add("&#928;");
        ENTITIES.add("&#960;");
        ENTITIES.add("&#982;");
        ENTITIES.add("&#936;");
        ENTITIES.add("&#968;");
        ENTITIES.add("&#929;");
        ENTITIES.add("&#961;");
        ENTITIES.add("&#931;");
        ENTITIES.add("&#963;");
        ENTITIES.add("&#962;");
        ENTITIES.add("&#932;");
        ENTITIES.add("&#964;");
        ENTITIES.add("&#920;");
        ENTITIES.add("&#952;");
        ENTITIES.add("&#977;");
        ENTITIES.add("&#978;");
        ENTITIES.add("&#933;");
        ENTITIES.add("&#965;");
        ENTITIES.add("&#926;");
        ENTITIES.add("&#958;");
        ENTITIES.add("&#918;");
        ENTITIES.add("&#950;");
        ENTITIES.add("&#8629;");
        ENTITIES.add("&#8595;");
        ENTITIES.add("&#8659;");
        ENTITIES.add("&#8596;");
        ENTITIES.add("&#8660;");
        ENTITIES.add("&#8592;");
        ENTITIES.add("&#8656;");
        ENTITIES.add("&#8594;");
        ENTITIES.add("&#8658;");
        ENTITIES.add("&#8593;");
        ENTITIES.add("&#8657;");
        ENTITIES.add("&#9827;");
        ENTITIES.add("&#9830;");
        ENTITIES.add("&#9829;");
        ENTITIES.add("&#9824;");
        ENTITIES.add("&#9674;");
        // below are extra entities that need to be protected
        ENTITIES.add("&#58;");
        ENTITIES.add("&amp;");
        ENTITIES.add("&gt;");
        ENTITIES.add("&lt;");
        ENTITIES.add("&quot;");
        ENTITIES.add("&apos;");
    }
    // thought 2: "&" is the main cause of affecting xml format, then just
    // protecting "&" will preserve the xml format well
    public final static List<String> NON_ENTITIES = new ArrayList<String>()
    {
        {
            add("&amp;");
            add("&lt;");
            add("&gt;");
            add("&apos;");
            add("&quot;");
        }
    };

    private final static String AND = "&";
    private final static String GS_ENTITY_PROTECT_AND = "[gs-entity-protect-AND-";
    private static Pattern P_LTGT = Pattern.compile("&lt;([^&]*?)&gt;");

    /**
     * Protects entities.
     * 
     * @since GBS-3702
     */
    public static void protectEntities(EFInputData input)
    {
        if (input == null)
        {
            return;
        }
        File f = input.getFile();
        if (f == null)
        {
            return;
        }
        try
        {
            String encoding = FileUtil.guessEncoding(f);
            if (encoding == null)
            {
                encoding = input.getCodeset();
            }
            String content = FileUtil.readFile(f, encoding);

            if (content.contains(AND))
            {
                content = StringUtil.replace(content, AND,
                        GS_ENTITY_PROTECT_AND);

                FileUtil.writeFile(f, content, encoding);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Error occurred while reading xml content and protecting entities.",
                    e);
        }
    }

    /**
     * Protects entities.
     * 
     * @since GBS-3702
     */
    public static String protectEntities(String content)
    {
        if (content.contains(AND))
        {
            content = StringUtil.replace(content, AND, GS_ENTITY_PROTECT_AND);
        }
        return content;
    }

    /**
     * Reverts protected entities (GS_ENTITY_PROTECT_AND) back to original
     * (AND).
     * 
     * @since GBS-3702
     */
    public static String revertEntities(String content)
    {
        if (content.contains(GS_ENTITY_PROTECT_AND))
        {
            content = StringUtil.replace(content, GS_ENTITY_PROTECT_AND, AND);
        }
        return content;
    }

    /**
     * Encodes &lt;TAG&gt; only out from html post-filter.
     * 
     * @since GBS-3702
     */
    public static String encodeFromHtmlPostFilter(String text)
    {
        if (StringUtil.isEmpty(text))
        {
            return text;
        }

        String encoded = encodeLtGt(text);
        // handle nesting case, for example, &lt;!-- &lt;br&gt; --&gt;
        while (!encoded.equals(text))
        {
            text = encoded;
            encoded = encodeLtGt(text);
        }

        // encode unpaired &lt; or &gt;
        text = StringUtil.replace(text, "&lt;", "&amp;lt;");
        text = StringUtil.replace(text, "&gt;", "&amp;gt;");

        return revertEntities(text);
    }

    /**
     * Encodes <TAG> out from html post-filter.
     * 
     * @since GBS-3702
     */
    public static String encodeTagFromHtmlPostFilter(String text)
    {
        if (StringUtil.isEmpty(text))
        {
            return text;
        }

        Pattern p = Pattern.compile("<([^<>]*?)>");
        Matcher m = p.matcher(text);
        while (m.find())
        {
            StringBuilder output = new StringBuilder();
            output.append(text.substring(0, m.start()));
            output.append("&lt;");
            output.append(m.group(1));
            output.append("&gt;");
            output.append(text.substring(m.end()));

            text = output.toString();
            m = p.matcher(text);
        }

        return text;
    }

    /**
     * Decodes tag and protects entities in the attributes and value before
     * going to html post-filter.
     * <p>
     * "&lt;a id=&quot;lightbox-closer-button&quot; &gt;&#8230;&amp;&lt;/a&gt;"
     * will be changed as
     * "<a id=GS_ENTITY_PROTECT_ANDquot;lightbox-closer-buttonGS_ENTITY_PROTECT_ANDquot;>GS_ENTITY_PROTECT_AND#8230;GS_ENTITY_PROTECT_ANDamp;</a>"
     * 
     * @since GBS-3702
     */
    public static String decodeToHtmlPostFilter(String text)
    {
        if (StringUtil.isEmpty(text))
        {
            return text;
        }
        // 1. protect all "&" to GS_ENTITY_PROTECT_AND
        text = protectEntities(text);

        // 2. revert protected entities
        // GS_ENTITY_PROTECT_ANDlt;TAGGS_ENTITY_PROTECT_ANDgt; back to
        // &lt;TAG&gt;
        text = StringUtil.replace(text, GS_ENTITY_PROTECT_AND + "lt;", "&lt;");
        text = StringUtil.replace(text, GS_ENTITY_PROTECT_AND + "gt;", "&gt;");

        // 3. decode tag "&lt;TAG&gt;"
        String decoded = decodeLtGt(text);
        // handle nesting case, for example, &lt;!-- &lt;br&gt; --&gt;
        while (!decoded.equals(text))
        {
            text = decoded;
            decoded = decodeLtGt(text);
        }

        // 4. protect other unpaired &lt; or &gt;
        text = protectEntities(text);

        return text;
    }

    private static String decodeLtGt(String old)
    {
        if (StringUtil.isEmpty(old))
        {
            return old;
        }

        Matcher m = P_LTGT.matcher(old);
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (m.find(start))
        {
            output.append(old.substring(start, m.start()));
            output.append("<");
            output.append(m.group(1));
            output.append(">");

            start = m.end();
        }
        output.append(old.substring(start));

        return output.toString();
    }

    private static String encodeLtGt(String old)
    {
        if (StringUtil.isEmpty(old))
        {
            return old;
        }

        // 1. protect all "&" to GS_ENTITY_PROTECT_AND
        old = protectEntities(old);

        // 2. revert protected entities
        // GS_ENTITY_PROTECT_ANDlt;TAGGS_ENTITY_PROTECT_ANDgt; back to
        // &lt;TAG&gt;
        old = StringUtil.replace(old, GS_ENTITY_PROTECT_AND + "lt;", "&lt;");
        old = StringUtil.replace(old, GS_ENTITY_PROTECT_AND + "gt;", "&gt;");

        // 3. encode "&lt;TAG&gt;"
        Matcher m = P_LTGT.matcher(old);
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (m.find(start))
        {
            output.append(old.substring(start, m.start()));
            output.append("&amp;lt;");
            output.append(m.group(1));
            output.append("&amp;gt;");

            start = m.end();
        }
        output.append(old.substring(start));

        return output.toString();
    }
}
