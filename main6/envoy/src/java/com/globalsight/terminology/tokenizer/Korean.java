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

package com.globalsight.terminology.tokenizer;

import java.util.Locale;
import java.util.Collection;
import java.util.HashMap;
import java.lang.StringBuffer;

/**
 */
class Korean
    extends CJK
{
    static final int SBASE = 0xAC00;
    static final int LBASE = 0x1100;
    static final int VBASE = 0x1161;
    static final int TBASE = 0x11A7;
    static final int LCOUNT = 19;
    static final int VCOUNT = 21;
    static final int TCOUNT = 28;
    static final int NCOUNT = VCOUNT * TCOUNT;   // 588
    static final int SCOUNT = LCOUNT * NCOUNT;   // 11172

    private TokenizerParameters m_params = null;

    /** Creates a new instance of Korean */
    public Korean()
    {
    }

    /**
     * Return locale specific tokens.
     * @param p_string: Input string to tokenize.
     */
    public Collection tokenize(String p_string)
    {
        int lfSize = m_params.getLangFeatureSize();
        int i;
        HashMap tokenMap = new HashMap(p_string.length() * 2);

        // add term start and end character   
        String temp = " " + p_string.trim() + " ";
        StringBuffer string = new StringBuffer(temp.length() * 3);

        // convert all Hangul to jamo
        i = 0;
        int orgLength = temp.length();
        while (i < orgLength)
        {
            char c = temp.charAt(i);

            if (isHangulSyllable(c))
            {
                string.append(decomposeHangul(c));
            }
            else
            {
                string.append(c);
            }

            i++;
        }

        // get language features
        int tokenLength = string.length() - (lfSize - 1);
        i = 0;
        while (i < tokenLength)
        {
            String lf = string.substring(i, i + lfSize);
            char c = lf.charAt(0);

            if (isCjkUnified(c)) // Han (Chinese) characters
            {
                // store the single Han character
                String han = String.valueOf(c);
                tokenMap.put(han, TokenPool.getInstance(han));
            }

            // we always generate the full ngram no matter the start
            // character type
            tokenMap.put(lf, TokenPool.getInstance(lf));
            i++;
        }

        return TokenWeighter.weightTokens(
            tokenMap.values(), m_params.getLocale());
    }

    public void setParameters(TokenizerParameters p_params)
    {
        m_params = p_params;
    }

    /*
     * Taken from Unicode decomposition algorithm:
     * http://www.unicode.org/unicode/reports/tr15/index.html#Hangul
     */
    private String decomposeHangul(char p_hungul)
    {
        int sIndex = p_hungul - SBASE;
        if (sIndex < 0 || sIndex >= SCOUNT)
        {
            return String.valueOf(p_hungul);
        }

        StringBuffer result = new StringBuffer();
        int L = LBASE + sIndex / NCOUNT;
        int V = VBASE + (sIndex % NCOUNT) / TCOUNT;
        int T = TBASE + sIndex % TCOUNT;

        result.append((char)L);
        result.append((char)V);

        if (T != TBASE)
        {
            result.append((char)T);
        }

        return result.toString();
    }
}
