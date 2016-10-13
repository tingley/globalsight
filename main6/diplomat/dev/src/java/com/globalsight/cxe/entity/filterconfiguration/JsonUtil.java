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

package com.globalsight.cxe.entity.filterconfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class JsonUtil
{
    static String map2Json(Map<String, Object> map)
    {
        if (map.isEmpty())
            return "{}";
        StringBuilder sb = new StringBuilder(map.size() << 4);
        sb.append('{');
        Set<String> keys = map.keySet();
        for (String key : keys)
        {
            Object value = map.get(key);
            sb.append('\"');
            sb.append(key);
            sb.append('\"');
            sb.append(':');
            sb.append(toJson(value));
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, '}');
        return sb.toString();
    }

    public static String toJson(Object o)
    {
        if (o == null)
            return "null";
        if (o instanceof String)
            return string2Json((String) o);
        if (o instanceof Boolean)
            return boolean2Json((Boolean) o);
        if (o instanceof Number)
            return number2Json((Number) o);
        if (o instanceof Map)
            return map2Json((Map<String, Object>) o);
        if (o instanceof Object[])
            return array2Json((Object[]) o);
        if (o instanceof Collection)
            return array2Json((Collection) o);
        
        throw new RuntimeException("Unsupported type: "
                + o.getClass().getName());
    }
    
    public static String toObjectJson(Object o)
    {
        return "(" + toJson(o) + ")";
    }

    static String string2Json(String s)
    {
        StringBuilder sb = new StringBuilder(s.length() + 20);
        sb.append('\"');
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
            case '\"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                sb.append(c);
            }
        }
        sb.append('\"');
        return sb.toString();
    }

    static String number2Json(Number number)
    {
        return number.toString();
    }

    static String boolean2Json(Boolean bool)
    {
        return bool.toString();
    }

    static String array2Json(Object[] array)
    {
        if (array.length == 0)
            return "[]";
        StringBuilder sb = new StringBuilder(array.length << 4);
        sb.append('[');
        for (Object o : array)
        {
            sb.append(toJson(o));
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }
    
    static String array2Json(Collection objs)
    {
        if (objs.size() == 0)
            return "[]";
        StringBuilder sb = new StringBuilder(objs.size() << 4);
        sb.append('[');
        for (Object o : objs)
        {
            sb.append(toJson(o));
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    public static void main(String[] args)
    {
//        HtmlFilter filter = new HtmlFilter();
//        filter.getInternalTags();
//        System.out.println(filter.toJSON(0));
//        Map<String, String> m = new HashMap<String, String>();
//        m.put("a", "a");
//        m.put("b", "b");
//        System.out.println(JsonUtil.map2Json(m));
        try
        {
            System.out.println(HtmlInternalTag.string2tag("<font color = \"blue\"> "));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ;
    }
}
