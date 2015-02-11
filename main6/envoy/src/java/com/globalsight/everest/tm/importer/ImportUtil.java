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

package com.globalsight.everest.tm.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.importer.ImportOptions;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.TmProcessStatus;

/**
 * Utility methods for TMX import.
 */
public class ImportUtil
{
    private static final Logger CATEGORY = Logger.getLogger(ImportUtil.class);

    //
    // Public Constants
    //
    static public final int TMX_LEVEL_TRADOS_FM_SGML = -6;

    static public final int TMX_LEVEL_TRADOS_XPTAG = -5;

    static public final int TMX_LEVEL_TRADOS_IL = -4;

    static public final int TMX_LEVEL_TRADOS_FM = -3;

    static public final int TMX_LEVEL_TRADOS_HTML = -2;

    static public final int TMX_LEVEL_TRADOS_RTF = -1;

    static public final int TMX_LEVEL_1 = 1;

    static public final int TMX_LEVEL_2 = 2;

    static public final int TMX_LEVEL_NATIVE = 10;

    // Cache of locale id and GlobalSightLocale map
    static private Hashtable s_name2locale = new Hashtable();

    static public final String ERROR_FILE_SUFFIX = "-error.html";

    static public final String INFO_FILE_SUFFIX = "-info.txt";

    static public final String LOG_FILE_SUFFIX = "-log.html";

    static public final String TOTAL_COUNT = "totalCount";

    static public final String ERROR_COUNT = "errorCount";

    static private final String SPLIT_LINE = "-------------------------------";

    static private final String TMX_DTD_LINE = "<!DOCTYPE tmx SYSTEM "
            + "\"http://www.lisa.org/tmx/tmx14.dtd\" >";
    static private final String UTF8 = "UTF-8";

    static private final String UTF16LE = "UTF-16LE";

    static private final String UTF16BE = "UTF-16BE";

    static private final String UTF16 = "UTF-16";

    static private String[] encodingCheckFirst = {"UTF-8", "UTF-16", };
    
    static private Pattern pattern_encoding = Pattern.compile("encoding=\"([^\"]*?)\"");
    
    //
    // Constructors
    //

    /** Static class, private constructor */
    private ImportUtil()
    {
    }

    public static ImportUtil createInstance()
    {
        return new ImportUtil();
    }

    /**
     * Accepts a TMX locale string, which can be "EN" or "EN-US", and normalizes
     * it to the Java format "xx_yy". Strings that contain only a language part
     * XX are complemented with a default country YY so a GlobalsightLocale can
     * be retrieved.
     * 
     * This method does not throw an exception (other than index out of bounds
     * or null pointer) but may return a result that is not a recognized
     * GlobalSightLocale, so the caller should be prepared for an exception
     * later.
     */
    static public String normalizeLocale(String p_locale)
    {
        String result = p_locale;
        int len = p_locale.length();

        p_locale = p_locale.toLowerCase();

        // Variations are not allowed.
        if (len > 5)
        {
            p_locale = p_locale.substring(0, 5);
            len = 5;
        }

        // Now len is 5 (default case) or less.
        if (len == 5)
        {
            String lang = p_locale.substring(0, 2);
            String country = p_locale.substring(3, 5);

            result = lang + "_" + country.toUpperCase();
        }
        else if (len < 5)
        {
            String lang = p_locale.substring(0, 2);

            if (lang.equals("ar"))
                result = "ar_SA";
            else if (lang.equals("be"))
                result = "be_BY";
            else if (lang.equals("bg"))
                result = "bg_BG";
            else if (lang.equals("ca"))
                result = "ca_ES";
            else if (lang.equals("cs"))
                result = "cs_CZ";
            else if (lang.equals("da"))
                result = "da_DK";
            else if (lang.equals("de"))
                result = "de_DE";
            else if (lang.equals("el"))
                result = "el_GR";
            else if (lang.equals("en"))
                result = "en_US";
            else if (lang.equals("es"))
                result = "es_ES";
            else if (lang.equals("et"))
                result = "et_EE";
            else if (lang.equals("fi"))
                result = "fi_FI";
            else if (lang.equals("fr"))
                result = "fr_FR";
            else if (lang.equals("he"))
                result = "he_IL";
            else if (lang.equals("hr"))
                result = "hr_HR";
            else if (lang.equals("hu"))
                result = "hu_HU";
            else if (lang.equals("is"))
                result = "is_IS";
            else if (lang.equals("it"))
                result = "it_IT";
            else if (lang.equals("ja"))
                result = "ja_JP";
            else if (lang.equals("ko"))
                result = "ko_KR";
            else if (lang.equals("lt"))
                result = "lt_LT";
            else if (lang.equals("lv"))
                result = "lv_LV";
            else if (lang.equals("mk"))
                result = "mk_MK";
            else if (lang.equals("nl"))
                result = "nl_NL";
            else if (lang.equals("no"))
                result = "no_NO";
            else if (lang.equals("pl"))
                result = "pl_PL";
            else if (lang.equals("pt"))
                result = "pt_PT";
            else if (lang.equals("ro"))
                result = "ro_RO";
            else if (lang.equals("ru"))
                result = "ru_RU";
            else if (lang.equals("sh"))
                result = "sh_YU";
            else if (lang.equals("sk"))
                result = "sk_SK";
            else if (lang.equals("sl"))
                result = "sl_SI";
            else if (lang.equals("sq"))
                result = "sq_AL";
            else if (lang.equals("sr"))
                result = "sr_YU";
            else if (lang.equals("sv"))
                result = "sv_SV";
            else if (lang.equals("th"))
                result = "th_TH";
            else if (lang.equals("tr"))
                result = "tr_TR";
            else if (lang.equals("zh"))
                result = "zh_CN";
        }

        return result;
    }

    /**
     * A cache from printed locale representation as found in TMX files to
     * GlobalSightLocale.
     * 
     * @param p_locale
     *            : a locale string in form "xx_yy"; use normalizeLocale() to
     *            normalize any other string formats like "xx-yy" and "xx".
     */
    static public GlobalSightLocale getLocaleByName(String p_locale)
            throws Exception
    {
        GlobalSightLocale result = (GlobalSightLocale) s_name2locale
                .get(p_locale);

        if (result == null)
        {
            LocaleManager mgr = ServerProxy.getLocaleManager();

            result = mgr.getLocaleByString(p_locale);

            s_name2locale.put(p_locale, result);
        }

        return result;
    }

    /**
     * Returns the XML representation like Element.asXML() but without the
     * top-level tag.
     */
    static public String getInnerXml(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node) content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(encodeXmlEntities(node.getText()));
            }
            else
            {
                // Element nodes write their text nodes correctly.
                result.append(node.asXML());
            }
        }

        return result.toString();
    }

    // This is a private method because it does encode only those
    // entities that must be encoded in in text nodes, not those in
    // attribute notes (quot and apos). For the general case, use
    // com.globalsight.util.edit.EditUtil#encodeXmlEntities()
    static private String encodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer res = new StringBuffer(s.length());

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
                case '<':
                    res.append("&lt;");
                    break;
                case '>':
                    res.append("&gt;");
                    break;
                case '&':
                    res.append("&amp;");
                    break;
                default:
                    res.append(c);
                    break;
            }
        }

        return res.toString();
    }

    static public int getTmxLevel(ImportOptions p_options)
    {
        com.globalsight.everest.tm.importer.ImportOptions options = (com.globalsight.everest.tm.importer.ImportOptions) p_options;

        String type = options.getFileType();

        if (type.equalsIgnoreCase(options.TYPE_XML))
        {
            return TMX_LEVEL_NATIVE;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TMX1))
        {
            return TMX_LEVEL_1;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TMX2))
        {
            return TMX_LEVEL_2;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_RTF))
        {
            return TMX_LEVEL_TRADOS_RTF;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_HTML))
        {
            return TMX_LEVEL_TRADOS_HTML;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_FM))
        {
            return TMX_LEVEL_TRADOS_FM;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_FM_SGML))
        {
            return TMX_LEVEL_TRADOS_FM_SGML;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_IL))
        {
            return TMX_LEVEL_TRADOS_IL;
        }
        else if (type.equalsIgnoreCase(options.TYPE_TTMX_XPTAG))
        {
            return TMX_LEVEL_TRADOS_XPTAG;
        }

        return TMX_LEVEL_2;
    }

    /**
     * Saves a TM file with sample validation.
     * 
     * For some TM files, it vary easy to happen encoding error or xml role
     * error and can't be import correct. This method try to do some sample
     * validations for each tu. If a tu will be give up if inducing a error.
     * 
     * @param fileName
     * @throws Exception
     */
    public void saveTmFileWithValidation(File file, File newFile,
            TmProcessStatus status) throws Exception
    {
        String encoding = "UTF-8";
        String outEncoding = "UTF-8";
        String logEncoding = "Unicode";
        String strLine = System.getProperty("line.separator");

        int errorCount = 0;
        int totalCount = 0;
        long lineCounter = 0;

        String s = null;

        try
        {
            if (file.exists())
            {
                CATEGORY.info("Validating TM file: "
                        + newFile.getAbsolutePath());

                Date startTime = new Date();

                File errorFile = getErrorFile(newFile);
                File infoFile = getInfoFile(newFile);
                File logFile = getLogFile(newFile);

                encoding = getEncodingOfXml(file);
                // GBS-2932 : UTF-8 by default
                if (encoding == null)
                {
                    encoding = "UTF-8";
                }

                // Initialize IO.
                FileInputStream fIn = new FileInputStream(file);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        fIn, encoding));
                FileOutputStream fOut = new FileOutputStream(newFile);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        fOut, outEncoding));
                FileOutputStream fError = new FileOutputStream(errorFile);
                OutputStreamWriter error = new OutputStreamWriter(fError,
                        logEncoding);
                FileOutputStream fInfo = new FileOutputStream(infoFile);
                BufferedWriter info = new BufferedWriter(
                        new OutputStreamWriter(fInfo, logEncoding));
                FileOutputStream fLog = new FileOutputStream(logFile);
                OutputStreamWriter log = new OutputStreamWriter(fLog,
                        logEncoding);

                writeHead(error);
                writeHead(log);

                StringBuilder sb = new StringBuilder();

                // It must be <?xml ...
                s = in.readLine();
                s = changeXmlEncodingDec(s, outEncoding);
                
                status.addSize(s.getBytes(encoding).length);
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("The content of in.readLine for encoding is "
                            + s);
                }
                sb.append(s);
                sb.append(strLine);

                status.addSize(s.getBytes(encoding).length);

                // If the second line is define dtd
                s = in.readLine();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("The content of in.readLine for doctype is "
                            + s);
                }
                if (s != null && s.indexOf("<!DOCTYPE") > -1)
                {
                    status.addSize(s.getBytes(encoding).length);

                    sb.append(s);
                    sb.append(strLine);
                    s = in.readLine();
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("The content of in.readLine is " + s);
                    }
                }
                else if (newFile.getName().endsWith("tmx"))
                {
                    // Don't define the dtd, add it.
                    sb.append(TMX_DTD_LINE);
                    sb.append(strLine);
                }
                boolean isRemoved = false;
                int count = 0;
                SAXReader reader = new SAXReader();
                while (s != null)
                {
                    if (status.isCanceled())
                    {
                        CATEGORY.info("Cancelled validating");
                        break;
                    }
                    status.addSize(s.getBytes(encoding).length);

                    if (isHeaderStart(s) && isTradosFontTableStart(s)
                            && isHeaderEnd(s))
                    {
                        int headerEndTag = s.indexOf(">");
                        sb.append(s.subSequence(0, headerEndTag + 1));
                        int endHeaderTag = s.indexOf("</header>");
                        sb.append(s.substring(endHeaderTag));
                        sb.append(endHeaderTag);
                        sb.append(strLine);
                    }
                    if (isRemoved)
                    {
                        if (isTradosFontTableEnd(s))
                        {
                            isRemoved = false;
                        }
                        s = in.readLine();
                        continue;
                    }
                    if (isTuStartTag(s))
                    {
                        /* The begin of the tu */
                        // Saves information recoded.
                        if (sb.length() > 0)
                        {
                            out.write(sb.toString());
                            out.flush();
                        }

                        sb = resetStringBuilder(sb);
                        sb.append(s);
                        sb.append(strLine);

                        totalCount++;
                    }

                    // Validate for the tu.
                    else if (isTuEndTag(s))
                    {
                        /* The end of the tu */
                        sb.append(s);
                        sb.append(strLine);
                        String content = sb.toString();

                        try
                        {
                            /* verify the content */
                            reader.read(new StringReader(content));

                            // Saves the tu if no exception happen.
                            out.write(content);
                            out.flush();
                        }
                        catch (Exception e)
                        {
                            // Give up the tu if any exception happened.
                            error.write(content);

                            log.write(strLine);
                            log.write(SPLIT_LINE);
                            log.write(Integer.toString(++errorCount));
                            log.write(SPLIT_LINE);
                            log.write(strLine);

                            log.write(content);
                            log.write(strLine);
                            log.write(e.getMessage());
                            log.write(strLine);

                        }

                        sb = resetStringBuilder(sb);
                    }
                    else if (isTradosFontTableStart(s))
                    {
                        count++;
                        isRemoved = true;
                    }
                    else if (count > 0 && isTradosFontTableEnd(s))
                    {
                        isRemoved = false;
                    }
                    else if ((count > 0) && isHeaderEnd(s))
                    {
                        sb.append("</header>");
                        sb.append(strLine);
                    }
                    else
                    {
                        // Records informations which not included in tu, first
                        // line
                        // etc.
                        sb.append(s);
                        sb.append(strLine);
                    }

                    s = in.readLine();
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("The content of in.readLine is " + s);
                    }
                    lineCounter++;
                }

                // Records informations which not included in tu and not saved
                // to
                // file. Usually it is "</body> </tmx>".
                if (sb.length() > 0)
                {
                    out.write(sb.toString());
                    out.flush();
                }

                in.close();

                out.close();

                CATEGORY.info("Done validating");

                log.write(SPLIT_LINE + SPLIT_LINE + strLine + strLine);
                log.write("Error: " + errorCount + strLine);
                log.write("Total: " + totalCount + strLine);

                // Gets the cost time.
                Date endTime = new Date();
                long costTime = endTime.getTime() - startTime.getTime();
                long h = costTime / (1000 * 60 * 60);
                costTime = costTime % (1000 * 60 * 60);
                long m = costTime / (1000 * 60);
                costTime = costTime % (1000 * 60);
                long se = costTime / 1000;
                StringBuffer time = new StringBuffer("Cost time: ");
                time.append(h).append(" h ").append(m).append(" m ").append(se)
                        .append(" s ");

                // Recodes some sample informations.
                String msg = "Error: " + errorCount + strLine;
                info.write(msg);
                msg = "Total: " + totalCount + strLine;
                info.write(msg);
                info.write(time.toString());

                writeFoot(error);
                writeFoot(log);

                error.flush();
                error.close();
                info.flush();
                info.close();
                log.flush();
                log.close();

                if (lineCounter > 10000)
                {
                    CATEGORY.debug("forces jvm to perform gc when the line count reaches 10000. line count: "
                            + lineCounter);
                    System.gc();
                }
            }
        }
        catch (IOException ie)
        {
            CATEGORY.error("IO Exception occured when save the tm file.");
            CATEGORY.error("The content of current line is " + s);
            CATEGORY.error("The stacktrace of the exception is ", ie);
            throw ie;
        }
        catch (Exception e)
        {
            CATEGORY.error("error occured when save the tm file.");
            CATEGORY.error("The content of current line is " + s);
            CATEGORY.error("The stacktrace of the exception is ", e);
            throw e;
        }

        status.setErrorTus(Integer.toString(errorCount));
        status.setTotalTus(Integer.toString(totalCount));
    }

    private String changeXmlEncodingDec(String s, String newEncoding)
    {
        if (s.indexOf("<?xml ") > -1)
        {
            // If the file has assigned the encoding, return the
            // assigned recoding.
            Matcher matcher = pattern_encoding.matcher(s);
            if (matcher.find())
            {
                String foundEncoding = matcher.group(1);
                String foundAll = matcher.group();
                if (!foundEncoding.equalsIgnoreCase(newEncoding))
                {
                    String newAll = foundAll.replace(foundEncoding, newEncoding);
                    s = s.replace(foundAll, newAll);
                }
            }
        }
        return s;
    }

    /**
     * Saves a TM file with sample validation.
     * 
     * For some TM files, it vary easy to happen encoding error or xml role
     * error and can't be import correct. This method try to do some sample
     * validations for each tu. If a tu will be give up if inducing a error.
     * 
     * This is used for no UI display requirement.
     * 
     * @param fileName
     * @throws Exception
     */
    public void saveTmFileWithValidation(File file, File newFile)
            throws Exception
    {
        String encoding = "UTF-8";
        String outEncoding = "UTF-8";
        String logEncoding = "Unicode";
        String strLine = System.getProperty("line.separator");

        int errorCount = 0;
        int totalCount = 0;
        long lineCounter = 0;

        String s = null;

        try
        {
            if (file.exists())
            {
                CATEGORY.info("Validating TM file: "
                        + newFile.getAbsolutePath());

                Date startTime = new Date();

                File errorFile = getErrorFile(newFile);
                File infoFile = getInfoFile(newFile);
                File logFile = getLogFile(newFile);

                encoding = getEncodingOfXml(file);
                // GBS-2932 : UTF-8 by default
                if (encoding == null)
                {
                    encoding = "UTF-8";
                }

                // Initialize IO.
                FileInputStream fIn = new FileInputStream(file);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        fIn, encoding));
                FileOutputStream fOut = new FileOutputStream(newFile);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        fOut, outEncoding));
                FileOutputStream fError = new FileOutputStream(errorFile);
                OutputStreamWriter error = new OutputStreamWriter(fError,
                        logEncoding);
                FileOutputStream fInfo = new FileOutputStream(infoFile);
                BufferedWriter info = new BufferedWriter(
                        new OutputStreamWriter(fInfo, logEncoding));
                FileOutputStream fLog = new FileOutputStream(logFile);
                OutputStreamWriter log = new OutputStreamWriter(fLog,
                        logEncoding);

                writeHead(error);
                writeHead(log);

                StringBuilder sb = new StringBuilder();

                // It must be <?xml ...
                s = in.readLine();
                s = changeXmlEncodingDec(s, outEncoding);
                
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("The content of in.readLine for encoding is "
                            + s);
                }
                sb.append(s);
                sb.append(strLine);

                // If the second line is define dtd
                s = in.readLine();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("The content of in.readLine for doctype is "
                            + s);
                }
                if (s != null && s.indexOf("<!DOCTYPE") > -1)
                {
                    sb.append(s);
                    sb.append(strLine);
                    s = in.readLine();
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("The content of in.readLine is " + s);
                    }
                }
                else if (newFile.getName().endsWith("tmx"))
                {
                    // Don't define the dtd, add it.
                    sb.append(TMX_DTD_LINE);
                    sb.append(strLine);
                }
                boolean isRemoved = false;
                int count = 0;
                SAXReader reader = new SAXReader();
                while (s != null)
                {
                    if (isHeaderStart(s) && isTradosFontTableStart(s)
                            && isHeaderEnd(s))
                    {
                        int headerEndTag = s.indexOf(">");
                        sb.append(s.subSequence(0, headerEndTag + 1));
                        int endHeaderTag = s.indexOf("</header>");
                        sb.append(s.substring(endHeaderTag));
                        sb.append(endHeaderTag);
                        sb.append(strLine);
                    }
                    if (isRemoved)
                    {
                        if (isTradosFontTableEnd(s))
                        {
                            isRemoved = false;
                        }
                        s = in.readLine();
                        continue;
                    }
                    if (isTuStartTag(s))
                    {
                        /* The begin of the tu */
                        // Saves information recoded.
                        if (sb.length() > 0)
                        {
                            out.write(sb.toString());
                            out.flush();
                        }

                        sb = resetStringBuilder(sb);
                        sb.append(s);
                        sb.append(strLine);

                        totalCount++;
                    }

                    // Validate for the tu.
                    else if (isTuEndTag(s))
                    {
                        /* The end of the tu */
                        sb.append(s);
                        sb.append(strLine);
                        String content = sb.toString();

                        try
                        {
                            /* verify the content */
                            reader.read(new StringReader(content));

                            // Saves the tu if no exception happen.
                            out.write(content);
                            out.flush();
                        }
                        catch (Exception e)
                        {
                            // Give up the tu if any exception happened.
                            error.write(content);

                            log.write(strLine);
                            log.write(SPLIT_LINE);
                            log.write(Integer.toString(++errorCount));
                            log.write(SPLIT_LINE);
                            log.write(strLine);

                            log.write(content);
                            log.write(strLine);
                            log.write(e.getMessage());
                            log.write(strLine);

                        }

                        sb = resetStringBuilder(sb);
                    }
                    else if (isTradosFontTableStart(s))
                    {
                        count++;
                        isRemoved = true;
                    }
                    else if (count > 0 && isTradosFontTableEnd(s))
                    {
                        isRemoved = false;
                    }
                    else if ((count > 0) && isHeaderEnd(s))
                    {
                        sb.append("</header>");
                        sb.append(strLine);
                    }
                    else
                    {
                        // Records informations which not included in tu, first
                        // line
                        // etc.
                        sb.append(s);
                        sb.append(strLine);
                    }

                    s = in.readLine();
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("The content of in.readLine is " + s);
                    }
                    lineCounter++;
                }

                // Records informations which not included in tu and not saved
                // to
                // file. Usually it is "</body> </tmx>".
                if (sb.length() > 0)
                {
                    out.write(sb.toString());
                    out.flush();
                }

                in.close();

                out.close();

                CATEGORY.info("Done validating");

                log.write(SPLIT_LINE + SPLIT_LINE + strLine + strLine);
                log.write("Error: " + errorCount + strLine);
                log.write("Total: " + totalCount + strLine);

                // Gets the cost time.
                Date endTime = new Date();
                long costTime = endTime.getTime() - startTime.getTime();
                long h = costTime / (1000 * 60 * 60);
                costTime = costTime % (1000 * 60 * 60);
                long m = costTime / (1000 * 60);
                costTime = costTime % (1000 * 60);
                long se = costTime / 1000;
                StringBuffer time = new StringBuffer("Cost time: ");
                time.append(h).append(" h ").append(m).append(" m ").append(se)
                        .append(" s ");

                // Recodes some sample informations.
                String msg = "Error: " + errorCount + strLine;
                info.write(msg);
                msg = "Total: " + totalCount + strLine;
                info.write(msg);
                info.write(time.toString());

                writeFoot(error);
                writeFoot(log);

                error.flush();
                error.close();
                info.flush();
                info.close();
                log.flush();
                log.close();

                if (lineCounter > 10000)
                {
                    CATEGORY.debug("suggests jvm to perform gc when the line count reaches 10000. line count: "
                            + lineCounter);
                    System.gc();
                }
            }
        }
        catch (IOException ie)
        {
            CATEGORY.error("IO Exception occured when save the tm file.");
            CATEGORY.error("The content of current line is " + s);
            CATEGORY.error("The stacktrace of the exception is ", ie);
            throw ie;
        }
        catch (Exception e)
        {
            CATEGORY.error("error occured when save the tm file.");
            CATEGORY.error("The content of current line is " + s);
            CATEGORY.error("The stacktrace of the exception is ", e);
            throw e;
        }
    }

    private boolean isHeaderStart(String line)
    {
        return line.indexOf("<header") > -1;
    }

    private boolean isHeaderEnd(String line)
    {
        return line.indexOf("</header>") > -1;
    }

    private boolean isTradosFontTableEnd(String line)
    {
        return line.indexOf("</prop>") > -1;
    }

    private boolean isTradosFontTableStart(String line)
    {
        return (line.indexOf("<prop ") > -1 && (line
                .indexOf("\"RTFFontTable\"") > -1 || line
                .indexOf("\"RTFStyleSheet\"") > -1));
    }

    /**
     * Gets a file that record all error tus.
     * 
     * @param file
     * @return
     */
    public static File getErrorFile(File file)
    {
        String fileName = file.getAbsolutePath();
        File errorFile = new File(fileName + ERROR_FILE_SUFFIX);
        return errorFile;
    }

    /**
     * Gets a log file to record information.
     * 
     * @param file
     * @return
     */
    public static File getLogFile(File file)
    {
        String fileName = file.getAbsolutePath();
        File logFile = new File(fileName + LOG_FILE_SUFFIX);
        return logFile;
    }

    /**
     * Gets a file that record the total information of the tm file.
     * 
     * @param file
     * @return
     */
    public static File getInfoFile(File file)
    {
        String fileName = file.getAbsolutePath();
        File infoFile = new File(fileName + INFO_FILE_SUFFIX);
        return infoFile;
    }

    /**
     * Try to guess the file encoding.
     * <p>
     * 
     * Only guees encodings of "UTF-8", "UTF-16" or "UTF-16BE".
     * 
     * @param file
     *            The file needed to guess the encoding.
     * @return The encoding, may be null.
     * @throws IOException
     */
    public static String guessEncodingByBom(File file) throws IOException
    {
        byte[] b = readFile(file, 3);
        String guess = null;

        if (b[0] == (byte) 0xef && b[1] == (byte) 0xbb && b[2] == (byte) 0xbf)
            guess = UTF8;
        else if (b[0] == (byte) 0xff && b[1] == (byte) 0xfe)
            guess = UTF16LE;
        else if (b[0] == (byte) 0xfe && b[1] == (byte) 0xff)
            guess = UTF16BE;

        return guess;
    }

    /**
     * Reads some bytes from the file.
     * 
     * @param file
     * @param size
     * @return
     * @throws IOException
     */
    private static byte[] readFile(File file, int size) throws IOException
    {
        byte[] b = new byte[size];
        FileInputStream fin = null;

        try
        {
            fin = new FileInputStream(file);
            fin.read(b, 0, size);
        }
        finally
        {
            if (fin != null)
            {
                fin.close();
            }
        }

        return b;
    }

    /**
     * Changes all those files which prepared to import into GlobalSight to
     * "UTF-8";
     * 
     * @throws IOException
     */
    public static void modifyFilesToUTF8(HashSet fileList)
    {
        try
        {
            Iterator<String> fileIterator = fileList.iterator();
            while (fileIterator.hasNext())
            {
                String file = fileIterator.next();
                File preFile = new File(AmbFileStoragePathUtils.getCxeDocDir(),
                        file);
                if (preFile.getAbsolutePath().endsWith(".xml"))
                {
                    String originalEncode = guessEncodingByBom(preFile);
                    if (originalEncode == null)
                    {
                        originalEncode = FileUtil.getEncodingOfXml(preFile);
                    }

                    if (!"UTF-8".equalsIgnoreCase(originalEncode))
                    {
                        saveFileAsUTF8(preFile.getAbsolutePath(),
                                originalEncode);
                    }
                }
            }
        }
        catch (IOException e)
        {
            CATEGORY.error("Can not modify the files to UTF-8", e);
        }
    }

    public static void saveFileAsUTF8(String absolutePath, String originalEncode)
    {
        if (originalEncode == null)
        {
            originalEncode = "UTF-8";
        }
        StringBuilder newStr = new StringBuilder();
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    absolutePath), originalEncode));
            String str = new String();
            str = in.readLine();

            newStr.append(str);
            newStr.append("\r\n");
            while ((str = in.readLine()) != null)
            {
                newStr.append(str);
                newStr.append("\r\n");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not save file:" + absolutePath + " as UTF-8");
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }
        writeFile(absolutePath, newStr.toString());
    }

    public static void main(String[] args) throws IOException
    {
        // File file = new File("D:/source patch/DITA dtd
        // files/sectional_review/2sectional_review_orig.xml");
        File file = new File("D:/a.txt");
        String encode = ImportUtil.guessEncodingByBom(file);
        if (encode == null)
        {
            encode = "utf-8";
        }
        // String s = URLDecoder.decode(new BufferedReader(new
        // InputStreamReader(new FileInputStream(file))).readLine(),encode);
        // System.out.println(s);
        // System.out.println(Integer.toHexString(new Integer(-2).byteValue()));
        saveFileAsUTF8(file.getAbsolutePath(), encode);
    }

    private static void writeFile(String absolutePath, String newStr)
    {
        Writer out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(absolutePath), "UTF-8"));
            out.write(newStr);
        }
        catch (IOException ex)
        {
            CATEGORY.error("Can not write the file:" + absolutePath);
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Try to find the encoding of a xml file.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static String getEncodingOfXml(File file) throws IOException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Got encoding... ");
        }

        // http://tracker.welocalize.com/browse/GBS-2932 : Note that utf-16
        // should have a Byte order mark at the start of the file according to
        // xml standard. utf-8 may or may not have a BOM. we should prioritise
        // the BOM over the xml encoding info in the file - default to utf-8 if
        // non.

        // 1 get encoding by file BOM
        String guessedEncoding = guessEncodingByBom(file);
        if (guessedEncoding != null)
        {
            return guessedEncoding;
        }

        // 2 get encoding by XML file info
        String foundEncoding = getXmlEncodingByDeclaration(file);
        return foundEncoding;
    }

    public static String getXmlEncodingByDeclaration(File file)
            throws IOException, UnsupportedEncodingException
    {
        byte[] bs = readFile(file, 160);

        
        String foundEncoding = null;
        for (String enc : encodingCheckFirst)
        {
            foundEncoding = checkEncoding(bs, enc, pattern_encoding, file);

            if (foundEncoding != null)
            {
                break;
            }
        }
        
        if (foundEncoding == null)
        {
            String encoding = "utf-8";
            Map chars = Charset.availableCharsets();
            Set keys = chars.keySet();
            Iterator iterator = keys.iterator();
            
            while (iterator.hasNext())
            {
                encoding = (String) iterator.next();
                foundEncoding = checkEncoding(bs, encoding, pattern_encoding, file);

                if (foundEncoding != null)
                {
                    break;
                }
            }
        }

        return foundEncoding;
    }

    private static String checkEncoding(byte[] bs, String encoding,
            Pattern pattern, File file) throws UnsupportedEncodingException,
            IOException
    {
        String s = new String(bs, encoding);
        String foundEncoding = null;

        // If "<?xml " can be recognized.
        if (s.indexOf("<?xml ") > -1)
        {
            // If the file has assigned the encoding, return the
            // assigned recoding.
            Matcher matcher = pattern.matcher(s);
            if (matcher.find())
            {
                foundEncoding = matcher.group(1);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Read content: " + s);
            }
        }
        return foundEncoding;
    }

    public static void writeHead(OutputStreamWriter writer) throws IOException
    {
        StringBuffer head = new StringBuffer();
        String strLine = System.getProperty("line.separator");

        head.append("<html>").append(strLine);
        head.append("<head>").append(strLine);
        head.append("<META content=\"text/html; charset=unicode\">").append(
                strLine);
        head.append("</head>").append(strLine);
        head.append("<body>").append(strLine);
        head.append("<TEXTAREA warp=\"off\" STYLE=\"overflow:visible; width:100%;\" READONLY=\"true\">");
        head.append(strLine);
        writer.write(head.toString());
    }

    public static void writeFoot(OutputStreamWriter writer) throws IOException
    {
        StringBuffer head = new StringBuffer();
        String strLine = System.getProperty("line.separator");
        head.append("</TEXTAREA>").append(strLine);
        head.append("</body>").append(strLine);
        head.append("</html>").append(strLine);

        writer.write(head.toString());
    }

    /**
     * Reset the content of the <code>StringBuilder</code>
     * 
     * @param sb
     *            {@code StringBuilder}
     * @return
     */
    private StringBuilder resetStringBuilder(StringBuilder sb)
    {
        sb.delete(0, sb.length());
        return sb;
    }

    private boolean isTuStartTag(String line)
    {
        return (line.indexOf("<tu ") > -1 || (line.indexOf("<tu>") > -1));
    }

    private boolean isTuEndTag(String line)
    {
        return line.indexOf("</tu>") > -1;
    }
}
