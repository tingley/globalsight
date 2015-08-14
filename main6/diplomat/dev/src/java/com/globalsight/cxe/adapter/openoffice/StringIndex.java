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

package com.globalsight.cxe.adapter.openoffice;

import java.util.List;

/**
 * Util class to find one content from source content base on start string and end
 * string
 * 
 * @author Wayzou
 * 
 */
public class StringIndex
{
    public String value;
    public int start;
    public int end;

    public String allValue;
    public int allStart;
    public int allEnd;

    public StringIndex(String v, int s, int e)
    {
        value = v;
        end = e;
        start = s;
    }

    public static StringIndex getValueBetween(String src, int s, String start,
            String end)
    {
        int index_s = src.indexOf(start, s);
        if (index_s != -1)
        {
            int index_e = src.indexOf(end, index_s + start.length());

            if (index_e != -1)
            {
                int st = index_s + start.length();
                StringIndex si = new StringIndex(src.substring(st, index_e),
                        st, index_e);
                si.allStart = index_s;
                si.allEnd = index_e + end.length();
                si.allValue = src.substring(index_s, index_e + end.length());

                return si;
            }
        }

        return null;
    }

    public static StringIndex getValueBetween(StringBuffer src, int s,
            String start, String end)
    {
        int index_s = src.indexOf(start, s);
        if (index_s != -1)
        {
            int index_e = src.indexOf(end, index_s + start.length());

            if (index_e != -1)
            {
                int st = index_s + start.length();
                StringIndex si = new StringIndex(src.substring(st, index_e),
                        st, index_e);
                si.allStart = index_s;
                si.allEnd = index_e + end.length();
                si.allValue = src.substring(index_s, index_e + end.length());

                return si;
            }
        }

        return null;
    }

    public static StringIndex getValueBetween(String src, int s,
            List<String> starts, String end)
    {
        int startIndexFind = -1;
        String startTagFind = null;

        if (starts != null)
        {
            for (String start : starts)
            {
                int index_s = src.indexOf(start, s);
                if (index_s > -1)
                {
                    String findStart = start;

                    if (startIndexFind == -1)
                    {
                        startIndexFind = index_s;
                        startTagFind = findStart;
                    }
                    else

                    if (index_s < startIndexFind)
                    {
                        startIndexFind = index_s;
                        startTagFind = findStart;

                    }
                }
            }
        }

        if (startIndexFind != -1)
        {
            int index_s = startIndexFind;
            String findStart = startTagFind;
            int index_e = src.indexOf(end, index_s + findStart.length());

            if (index_e != -1)
            {
                int st = index_s + findStart.length();
                StringIndex si = new StringIndex(src.substring(st, index_e),
                        st, index_e);
                si.allStart = index_s;
                si.allEnd = index_e + end.length();
                si.allValue = src.substring(index_s, index_e + end.length());

                return si;
            }
        }

        return null;
    }

    public static StringIndex getValueBetween(StringBuffer src, int s,
            List<String> starts, String end)
    {
        int startIndexFind = -1;
        String startTagFind = null;

        if (starts != null)
        {
            for (String start : starts)
            {
                int index_s = src.indexOf(start, s);
                if (index_s > -1)
                {
                    String findStart = start;

                    if (startIndexFind == -1)
                    {
                        startIndexFind = index_s;
                        startTagFind = findStart;
                    }
                    else

                    if (index_s < startIndexFind)
                    {
                        startIndexFind = index_s;
                        startTagFind = findStart;

                    }
                }
            }
        }

        if (startIndexFind != -1)
        {
            int index_s = startIndexFind;
            String findStart = startTagFind;
            int index_e = src.indexOf(end, index_s + findStart.length());

            if (index_e != -1)
            {
                int st = index_s + findStart.length();
                StringIndex si = new StringIndex(src.substring(st, index_e),
                        st, index_e);
                si.allStart = index_s;
                si.allEnd = index_e + end.length();
                si.allValue = src.substring(index_s, index_e + end.length());

                return si;
            }
        }

        return null;
    }
}
