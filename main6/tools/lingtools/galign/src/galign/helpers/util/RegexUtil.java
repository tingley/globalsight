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

package galign.helpers.util;

public class RegexUtil
{
    /**
     * Converts a 'simplified' regular expression to a full regular
     * expression.
     * @param pattern The pattern to convert
     * @return The full regular expression
     */
    public static String simplePatternToFullRegularExpression(String pattern)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < pattern.length(); i++)
        {
            char c = pattern.charAt(i);
            switch (c)
            {
                case '*':
                    // To make * mean 'any chars', use this:
                    // buf.append(".*"); break;

                case '.':
                case '[':
                case ']':
                case '\\':
                case '+':
                case '?':
                case '{':
                case '}':
                case '$':
                case '^':
                case '|':
                case '(':
                case ')':
                    buf.append('\\');
                default:
                    buf.append(c);
                    break;
            }
        }
        return buf.toString();
    }
}
