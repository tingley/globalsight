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
package com.globalsight.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Loads the emojis from a JSON database.
 */
public class EmojiLoader
{
    /**
     * Loads a JSONArray of emojis from an InputStream, parses it and returns
     * the associated list of Emojis.
     *
     * @param stream
     *            the stream of the JSONArray
     *
     * @return the list of Emojis
     * @throws IOException
     *             if an error occurs while reading the stream or parsing the
     *             JSONArray
     * @throws JSONException
     */
    public static List<Emoji> loadEmojis(InputStream stream)
            throws IOException, JSONException
    {
        JSONArray emojisJSON = new JSONArray(inputStreamToString(stream));
        List<Emoji> emojis = new ArrayList<Emoji>(emojisJSON.length());
        for (int i = 0; i < emojisJSON.length(); i++)
        {
            Emoji emoji = buildEmojiFromJSON(emojisJSON.getJSONObject(i));
            if (emoji != null)
            {
                emojis.add(emoji);
            }
        }
        return emojis;
    }

    private static String inputStreamToString(InputStream stream)
    {
        Scanner s = new Scanner(stream, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    protected static Emoji buildEmojiFromJSON(JSONObject json)
            throws UnsupportedEncodingException, JSONException
    {
        if (!json.has("emoji"))
        {
            return null;
        }

        byte[] bytes = json.getString("emoji").getBytes("UTF-8");
        String description = null;
        if (json.has("description"))
        {
            description = json.getString("description");
        }
        List<String> aliases = jsonArrayToStringList(json
                .getJSONArray("aliases"));
        List<String> tags = jsonArrayToStringList(json.getJSONArray("tags"));
        int html = getHtmlCodeFromBytes(bytes);
        return new Emoji(description, aliases, tags, html, bytes);
    }

    private static int getHtmlCodeFromBytes(byte[] bytes)
            throws UnsupportedEncodingException
    {
        String unicode = new String(bytes, "UTF-8");
        return Character.codePointAt(unicode, 0);
    }

    private static List<String> jsonArrayToStringList(JSONArray array)
            throws JSONException
    {
        List<String> strings = new ArrayList<String>(array.length());
        for (int i = 0; i < array.length(); i++)
        {
            strings.add(array.getString(i));
        }
        return strings;
    }
}
