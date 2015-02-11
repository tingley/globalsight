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
package com.globalsight.ling.common;

public interface RegExMatchInterface 
{

    /**
     * A convenience method returning the length of the entire match.
     * If you want to get the length of a particular subgroup you should
     * use the {@link #group(int)} method to get
     * the string and then access its length() method as follows:
     * <p>
     * <blockquote><pre>
     * int length = -1; // Use -1 to indicate group doesn't exist
     * MatchResult result;
     * String subgroup;
     * 
     * // Initialization of result omitted
     *
     * subgroup = result.group(1);
     * if(subgroup != null)
     *   length = subgroup.length();
     *
     * </pre></blockquote>
     * <p>
     *
     * The length() method serves as a more a more efficient way to do:
     * <p>
     * <blockquote><pre>
     * length = result.group(0).length();
     * </pre></blockquote>
     * <p>
     *
     * @return The length of the match.
     */
    public int length();

    /**
     * @return The number of groups contained in the result.  This number
     *         includes the 0th group.  In other words, the result refers
     *         to the number of parenthesized subgroups plus the entire match
     *         itself.
     */
    public int groups();

    /**
     * Returns the contents of the parenthesized subgroups of a match,
     * counting parentheses from left to right and starting from 1.
     * Group 0 always refers to the entire match.  For example, if the
     * pattern <code> foo(\d+) </code> is used to extract a match
     * from the input <code> abfoo123 </code>, then <code> group(0) </code>
     * will return <code> foo123 </code> and <code> group(1) </code> will return
     * <code> 123 </code>.  <code> group(2) </code> will return
     * <code> null </code> because there is only one subgroup in the original
     * pattern.
     * <p>
     * @param group The pattern subgroup to return.
     * @return A string containing the indicated pattern subgroup.  Group
     *         0 always refers to the entire match.  If a group was never
     *         matched, it returns null.  This is not to be confused with
     *         a group matching the null string, which will return a String
     *         of length 0.
     */
    public String group(int group);

    /**
     * @param group The pattern subgroup.
     * @return The offset into group 0 of the first token in the indicated
     *         pattern subgroup.  If a group was never matched or does
     *         not exist, returns -1.  Be aware that a group that matches
     *         the null string at the end of a match will have an offset
     *         equal to the length of the string, so you shouldn't blindly
     *         use the offset to index an array or String.
     */
    public int begin(int group);

    /**
     * @param group The pattern subgroup.
     * @return Returns one plus the offset into group 0 of the last token in
     *         the indicated pattern subgroup.  If a group was never matched
     *         or does not exist, returns -1.  A group matching the null
     *         string will return its start offset.
     */
    public int end(int group);

    /**
     * Returns an offset marking the beginning of the pattern match
     * relative to the beginning of the input from which the match
     * was extracted.
     * <p>
     * @param group The pattern subgroup.
     * @return The offset of the first token in the indicated
     *         pattern subgroup.  If a group was never matched or does
     *         not exist, returns -1.
     */
    public int beginOffset(int group);

    /**
     * Returns an offset marking the end of the pattern match
     * relative to the beginning of the input from which the match was
     * extracted.
     * <p>
     * @param group The pattern subgroup.
     * @return Returns one plus the offset of the last token in
     *         the indicated pattern subgroup.  If a group was never matched
     *         or does not exist, returns -1.  A group matching the null
     *         string will return its start offset.
     */
    public int endOffset(int group);

    /**
     * Returns the same as group(0).
     *
     * @return A string containing the entire match.
     */
    public String toString();  
}