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
package com.globalsight.ling.tw;

/**
 * <p>Encoder/Decoder for Pseudo Strings.</p>
 *
 * <p>Escapes/unescapes existing open brackets.</p>
 */
public class PseudoCodec
{
    public PseudoCodec()
    {
        super();
    }

    /**
     * Encoder escapes existing open brackets that might otherwise
     * look like the start of a PTag.
     */
    public String encode(String p_strIn)
    {
        StringBuffer res = new StringBuffer(p_strIn.length());
        int startIdx = 0;
        int stopIdx = 0;

        while ((stopIdx = p_strIn.indexOf(
            PseudoConstants.PSEUDO_OPEN_TAG, startIdx)) != -1)
        {
            res.append(p_strIn.substring(startIdx, stopIdx + 1));
            res.append(PseudoConstants.PSEUDO_OPEN_TAG);

            startIdx = stopIdx + 1;
        }

        res.append(p_strIn.substring(startIdx));

        return res.toString();
    }

    /**
     * Decoder unescapes open brackets.
     */
    public String decode(String p_strIn)
    {
        StringBuffer res = new StringBuffer(p_strIn.length());
        StringBuffer tmp = new StringBuffer(2);
        String escapeSeq;
        int startIdx = 0;
        int stopIdx = 0;

        tmp.append(PseudoConstants.PSEUDO_OPEN_TAG);
        tmp.append(PseudoConstants.PSEUDO_OPEN_TAG);
        escapeSeq = tmp.toString();

        while ((stopIdx = p_strIn.indexOf(escapeSeq, startIdx)) != -1)
        {
            res.append(p_strIn.substring(startIdx, stopIdx));
            res.append("[");

            startIdx = stopIdx + 2;
        }

        res.append(p_strIn.substring(startIdx));

        return res.toString();
    }

    /*
    public static void main(String[] args)
    {
        PseudoCodec codec = new PseudoCodec();

        String in = "this [[is]] a [test] with a bracketed [[[ptag]]].";
        String strEncoded = codec.encode(in);
        String strDecoded = codec.decode(strEncoded);

        System.out.println("Input: " + in);
        System.out.println("encoded: " + strEncoded);
        System.out.println("decoded: " + strDecoded);
        if(in.equals(strDecoded))
            System.out.println("Compare OK");
        else
            System.out.println("compare Failed !!");
    }
    */
}
