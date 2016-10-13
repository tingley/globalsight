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
package com.globalsight.ling.docproc;


/**
 * This class converts C0 control code (U+0000 - U+001F) except for
 * CR, LF, TAB to Private Use Area characters. The reason for doing
 * this is that XML document doesn't allow C0 code in it. If an
 * imported file has these characters in it, they must be kept in
 * GXML. Since these characters are not allowed, a XML parser return
 * an error when it parses the GXML. So we convert these C0 control
 * codes to some Private Use Area characters.
 *
 * The reason why we choose to use Private Use Area characters instead
 * of marking up C0 codes with some elements is simplicity. Changing
 * GXML schema impacts many parts of our code. Besides, XML 1.1 will
 * be published soon (now Candidate Recommendation in Oct. 2002) which
 * allows C0 characters in NCR form except for U+0000. So let's choose
 * easier route.
 *
 * There is a slight chance that the PUA characters used below are
 * also used in the original document if the client chooses to use
 * these characters for some reason. But I think that's very very
 * rare.
 */
public class CtrlCharConverter
{
    static final char NULL = '\u0000';
    static final char UNIT_SEPARATOR = '\u001f';
    static final char TAB = '\u0009';
    static final char LINE_FEED = '\n';         // U+000a;
    static final char CARRIAGE_RETURN = '\r';   // U+000d;
    
    static final char PUA_START = '\ue000';

    static public char convertToPua(char p_char)
    {
        if(p_char >= NULL && p_char <= UNIT_SEPARATOR
            && p_char != TAB && p_char != LINE_FEED
            && p_char != CARRIAGE_RETURN)
        {
            p_char += PUA_START;
        }
        return p_char;
    }
    

    static public char convertToCtrl(char p_char)
    {
        if(p_char >= NULL + PUA_START && p_char <= UNIT_SEPARATOR + PUA_START
            && p_char != TAB + PUA_START && p_char != LINE_FEED + PUA_START
            && p_char != CARRIAGE_RETURN + PUA_START)
        {
            p_char -= PUA_START;
        }
        return p_char;
    }


    static public String convertToPua(String p_str)
    {
        StringBuffer buf = new StringBuffer(p_str);
        convertToPua(buf);
        return buf.toString();
    }
    

    static public String convertToCtrl(String p_str)
    {
        StringBuffer buf = new StringBuffer(p_str);
        convertToCtrl(buf);
        return buf.toString();
    }
    

    static public void convertToPua(StringBuffer p_buf)
    {
        int len = p_buf.length();
        for(int i = 0; i < len; i++)
        {
            char c = p_buf.charAt(i);
            if(c >= NULL && c <= UNIT_SEPARATOR && c != TAB && c != LINE_FEED
                && c != CARRIAGE_RETURN)
            {
                p_buf.setCharAt(i, (char)(c + PUA_START));
            }
        }
    }
    
            
    static public void convertToCtrl(StringBuffer p_buf)
    {
        int len = p_buf.length();
        for(int i = 0; i < len; i++)
        {
            char c = p_buf.charAt(i);
            if(c >= NULL + PUA_START && c <= UNIT_SEPARATOR + PUA_START
                && c != TAB + PUA_START && c != LINE_FEED + PUA_START
                && c != CARRIAGE_RETURN + PUA_START)
            {
                p_buf.setCharAt(i, (char)(c - PUA_START));
            }
        }
    }

}
