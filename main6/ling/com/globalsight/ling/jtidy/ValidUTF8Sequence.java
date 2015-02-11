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
package com.globalsight.ling.jtidy;

/**
 * @author Fabrizio Giustina (translation from c)
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class ValidUTF8Sequence
{

    /**
     * low char.
     */
    int lowChar;

    /**
     * high char.
     */
    int highChar;

    /**
     * number of bytes.
     */
    int numBytes;

    /**
     * array of valid bytes.
     */
    char[] validBytes = new char[8];

    /**
     * Instantiates a new ValidUTF8Sequence.
     * @param lowChar low utf8 char
     * @param highChar high utf8 char
     * @param numBytes number of bytes in the sequence
     * @param validBytes valid bytes array
     */
    public ValidUTF8Sequence(int lowChar, int highChar, int numBytes,
        char[] validBytes)
    {
        this.lowChar = lowChar;
        this.highChar = highChar;
        this.numBytes = numBytes;
        this.validBytes = validBytes;
    }
}
