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
package com.globalsight.everest.comment;


import java.util.List;

/**
 * This class represents an issue which is like a comment however it has
 * a status and priority assigned to it.  It is a piece an action item
 * that is assigned to someone or a workflow of people to work on and
 * address.  
 */
public interface Issue extends Comment
{
    // priority
    final static String PRI_URGENT = "urgent";
    final static String PRI_HIGH = "high";
    final static String PRI_MEDIUM = "medium";
    final static String PRI_LOW = "low";

    // status are hard-coded for now
    final static String STATUS_QUERY = "query";
    final static String STATUS_OPEN = "open";
    final static String STATUS_CLOSED = "closed";
    final static String STATUS_REJECTED = "rejected";
    
    // category
    final static String CATEGORY_TYPE01 = "Type01";
    final static String CATEGORY_TYPE02 = "Type02";
    final static String CATEGORY_TYPE03 = "Type03";
    final static String CATEGORY_TYPE04 = "Type04";
    final static String CATEGORY_TYPE05 = "Type05";
    final static String CATEGORY_TYPE06 = "Type06";
    final static String CATEGORY_TYPE07 = "Type07";
    final static String CATEGORY_TYPE08 = "Type08";
    final static String CATEGORY_TYPE09 = "Type09";
    final static String CATEGORY_TYPE10 = "Type10";
    
    final static String CATEGORY_CONFILICTS = "Conflicts with glossary or style guide";
    final static String CATEGORY_FORMATTING = "Formatting error";
    final static String CATEGORY_MISTRSLATED = "Mistranslated";
    final static String CATEGORY_OMISSION = "Omission of text";
    final static String CATEGORY_SPELLING = "Spelling, grammar, or punctuation error";

    final static String CATEGORY_MISTRANSLATION = "mistranslation";
    final static String CATEGORY_ACCURACY = "accuracy";
    final static String CATEGORY_TERMINOLOGY = "terminology";
    final static String CATEGORY_LANGUAGE = "language";
    final static String CATEGORY_STYLE = "style";
    final static String CATEGORY_COUNTRY = "country";

    // types of objects an issue can be associated
    // with
    final static int TYPE_SEGMENT = 4;

    /**
     * 
     * Gets the category of the issue.
     */
    String getCategory();
    
    /**
     * Sets the category of the issue.
     */
    void setCategory(String p_category);
    
    /**
     * Get the priority of the issue.
     */
    String getPriority();

    /**
     * Set the priority of the issue.
     */
    void setPriority(String p_priority);
    
    /**
     * Get the title.
     */  
    String getTitle();

    /**
     * Set the title of the issue.
     */
    void setTitle(String p_title);

    /**
     * Get the status of the issue.
     */
    String getStatus();

    /**
     * Set the status of the issue.
     */
    void setStatus(String p_status);

    /**
     * Returns the type of object that this issue is associated with.
     * The types are listed above with prefix TYPE_
     */
    int getLevelObjectType();

    /**
     * Returns the id of the object that this issue is associated with.
     * So currently it is the unique id for the segment/TUV.
     */
    long getLevelObjectId();
  
    /**
     * Returns a list of comments that were added to this issue.
     * The list is of IssueHistory objects that include the user
     * that added the comment, the date and the comment text.
     *
     * They will be returned in descending order by reportedBy date.
     */
    List getHistory();

    /**
     * A logical key used for the editor to know where the issue belongs
     * and also is used for quick queries for groups of issues that belong
     * to the same higher level object.
     * String stores key information separated by "_" to specify
     * what higher level group it is part of
     *
     * ie.  PageId_TuId_TuvId_SubId   (if work object type = SEGMENT)
     *      JobId_WfId_TaskId (if work object type = TASK)
     */
    String getLogicalKey();

    /**
     * Set the key to identify this particular issue.
     */
    void setLogicalKey(String p_key);
    
    public boolean isShare();

    public void setShare(boolean share);

    public boolean isOverwrite();

    public void setOverwrite(boolean overwrite);
}

