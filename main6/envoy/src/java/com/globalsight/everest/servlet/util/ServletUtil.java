/**
 * Copyright 2016 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * */
package com.globalsight.everest.servlet.util;

import jodd.util.*;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VincentYan
 *
 */
public class ServletUtil extends jodd.servlet.ServletUtil
{
    public static Logger logger = Logger.getLogger(ServletUtil.class);
    private static List<Pattern> patterns = null;

    public static String get(HttpServletRequest req, String name)
    {
        return get(req, name, "", false);
    }

    public static String get(HttpServletRequest req, String name, String defaultValue)
    {
        return get(req, name, defaultValue, false);
    }

    public static String get(HttpServletRequest req, String name, String defaultValue,
            boolean encoding)
    {
        String result = StringUtil.isBlank(defaultValue) ? "" : defaultValue;
        if (StringUtil.isNotBlank(name))
        {
            String tmp = req.getParameter(name);
            if (StringUtil.isNotBlank(tmp))
            {
                result = encoding ? encodeHtml(tmp) : tmp;
            }
        }

        return result;
    }

    /**
     * Get parameter value from request with specified name
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @return String Parameter value
     */
    public static String getValue(HttpServletRequest req, String name)
    {
        return getValue(req, name, "", false);
    }

    /**
     * Get encoded parameter value from request with specified name
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @return String Encoded parameter value
     */
    public static String encodedValue(HttpServletRequest req, String name)
    {
        return getValue(req, name, "", true);
    }

    /**
     * Get encoded parameter value from request with specified name. If the
     * value is null or empty, default value will be returned
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @param defaultValue
     *            Default value of parameter
     * @return String Encoded parameter value
     */
    public static String encodedValue(HttpServletRequest req, String name, String defaultValue)
    {
        return getValue(req, name, defaultValue, true);
    }

    /**
     * Get parameter value from request with specified name, if value is null or
     * empty, return default value. If value contains special characters like
     * '"",''', '<','>' etc, it will be encoed.
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @param defaultValue
     *            Default value if value is null or empty
     * @return String Parameter value
     */
    public static String getValue(HttpServletRequest req, String name, String defaultValue,
            boolean encode)
    {
        String result = defaultValue;

        if (req == null || StringUtil.isBlank(name))
            return defaultValue;

        name = name.trim();
        result = req.getParameter(name);
        result = stripXss(result, encode);
        if (StringUtil.isBlank(result) || "null".equalsIgnoreCase(result))
            return defaultValue;

        return result;
    }

    /**
     * Get parameter value with specified name, if it is null, empty or not
     * integer value, return default value
     * 
     * @param req
     *            Request
     * @param name
     *            Parameter name
     * @param defaultValue
     *            Default value
     * @return int Parameter value
     */
    public static int getIntValue(HttpServletRequest req, String name, int defaultValue)
    {
        int result = defaultValue;
        String str = getValue(req, name, "-1", false);
        try
        {
            result = Integer.parseInt(str);
        }
        catch (Exception e)
        {
            result = defaultValue;
            logger.error("Cannot parse string " + str + " to integer.", e);
        }

        return result;
    }

    public static String[] getValues(HttpServletRequest req, String name, boolean encode)
    {
        if (StringUtil.isBlank(name))
            return null;
        String[] values = (String[]) value(req, name);
        if (values == null || values.length == 0)
            return null;
        String[] result = new String[values.length];
        for (int i = 0, len = result.length; i < len; i++)
        {
            result[i] = stripXss(values[i], encode);
        }
        return result;
    }

    /**
     * Check if the attributes or parameters in request have invaild characters
     * 
     * @param request
     *            HttpServletRequest
     * @return true -- Not including invaild characters false -- Including
     *         invaild characters
     */
    public static boolean checkAllValues(HttpServletRequest request)
    {
        String name, value = "";
        Object object = null;
        boolean isValid = true;

        // Check attributes of request
        Enumeration enumeration = request.getAttributeNames();
        while (enumeration.hasMoreElements())
        {
            name = (String) enumeration.nextElement();
            object = request.getAttribute(name);
            if (object == null)
                continue;

            if (object instanceof Integer)
            {
                value = ((Integer) object).toString();
            }
            else if (object instanceof String)
            {
                value = (String) object;
            }
            else if (object instanceof String[])
            {
                String[] tmp = (String[]) object;
                for (String string : tmp)
                {
                    value += string + ",";
                }
            }
            else
                continue;
            logger.debug("name == " + name + ", object == " + value);
            if (containXSS(value))
                isValid = false;
        }

        // Check query string of request
        value = decodeUrl(request.getQueryString());
        logger.debug("Query string in URL -- " + value);
        if (containXSS(value))
            isValid = false;

        // Vincent, Comment below codes because if parameter is post data via
        // request, it maybe contains special characters. This may need to be
        // handled in each page according with regular requirement.

        // Check all parameters
        // enumeration = request.getParameterNames();
        // while (enumeration.hasMoreElements())
        // {
        // name = (String) enumeration.nextElement();
        // value = encodedValue(request, name);
        // if (containXSS(value))
        // return false;
        // }

        return isValid;
    }

    /**
     * Check if the attributes or parameters in URL have invaild characters
     * Invaild characters contains,
     * 
     * @param url
     *            String Query string in URL
     * @return true -- Not including invaild characters false -- Including
     *         invaild characters
     */
    public static boolean checkUrl(String url)
    {
        if (StringUtil.isBlank(url))
            return true;
        if (containXSS(url) || containXSS(URLDecoder.decode(url)))
            return false;

        return true;
    }

    private static List<Object[]> getXssPatternList()
    {
        List<Object[]> ret = new ArrayList<Object[]>();
        ret.add(new Object[]
        { "<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE });
        ret.add(new Object[]
        { "eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        { "expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        { "(javascript:|vbscript:|view-source:).*", Pattern.CASE_INSENSITIVE });
//        ret.add(new Object[]
//        { "<(\"[^\"]*\"|\'[^\']*\'|[^\'\">]).*>",
//                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        { "(window\\.location|document\\.cookie|alert\\(.*?\\)|window\\.open\\().*",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        ret.add(new Object[]
        {
                "<?\\s*.*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL });
        return ret;
    }

    private static List<Pattern> getPatterns()
    {
        if (patterns == null)
        {
            List<Pattern> list = new ArrayList<Pattern>();
            String regex = null;
            Integer flag = null;
            int arrLength = 0;
            for (Object[] arr : getXssPatternList())
            {
                arrLength = arr.length;
                for (int i = 0; i < arrLength; i++)
                {
                    regex = (String) arr[0];
                    flag = (Integer) arr[1];
                    list.add(Pattern.compile(regex, flag));
                }
            }
            patterns = list;
        }
        return patterns;
    }

    /**
     * Replace invaild characters got from parameter or attribute
     * 
     * @param value
     * @return
     */
    public static String stripXss(String value)
    {
        return stripXss(value, false);
    }

    public static String stripXss(String value, boolean htmlEncoding)
    {
        if (StringUtil.isNotBlank(value))
        {
            String result = value.trim();
            boolean hasXss = false;
            // Change value if it is encoded by URLEncoder like %20%3d....
            result = decodeUrl(result);
            Matcher matcher = null;
            for (Pattern pattern : getPatterns())
            {
                matcher = pattern.matcher(result);
                if (matcher.find())
                {
                    result = matcher.replaceAll("");
                    hasXss = true;
                }
            }

            if (hasXss)
                value = result;

            if (htmlEncoding)
                value = encodeHtml(value);
        }
        else
            value = "";

        return value;

    }

    /**
     * Check if string contains invaild characters
     * 
     * @param str
     *            String string
     * @return true -- Including invaild characters false -- Not including
     *         invaild characters
     */
    public static boolean containXSS(String str)
    {
        if (StringUtil.isBlank(str))
            return false;
        Matcher matcher = null;
        for (Pattern pattern : getPatterns())
        {
            matcher = pattern.matcher(str);
            if (matcher.find())
            {
                return true;
            }
        }
        return false;
    }

    public static String encodeHtml(String str)
    {
        if (StringUtil.isBlank(str))
            return "";
        return HtmlEncoder.text(str);
    }

    public static String decodeHtml(String str)
    {
        if (StringUtil.isBlank(str))
            return "";
        return HtmlDecoder.decode(str);
    }

    public static String encodeUrl(String str)
    {
        if (StringUtil.isBlank(str))
            return "";
        return URLCoder.encodeHttpUrl(str);
    }

    public static String decodeUrl(String str)
    {
        if (StringUtil.isBlank(str))
            return "";
        return URLDecoder.decode(str);
    }
}
