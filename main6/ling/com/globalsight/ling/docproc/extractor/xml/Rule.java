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
package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.globalsight.util.SortUtil;

/**
 * <p>
 * Defines an individual rule.
 * </p>
 * 
 * <p>
 * Rules have the following properties:
 * </p>
 * 
 * <ul>
 * <li>translate (boolean): nodes matched by this rule are
 * translatable/localizable or should go to the skeleton.</li>
 * <li>translatable (boolean): nodes matched by this rule are extracted as
 * &lt;translatable&gt; or &lt;localizable&gt; (</li>
 * <li>dataFormat (string): nodes matched by this rule are sent to the extractor
 * for the specified data format (e.g, "html", "css" etc).</li>
 * <li>type (string): nodes matched by this rule have the DiplomatXML type
 * attribute set to this value.</li>
 * <li>inline (boolean): if true, the node will be extracted as &lt;ph&gt;
 * element (if the node is empty), or as &lt;bpt&gt;. If false, the node goes to
 * the skeleton.</li>
 * <li>containedInHtml (boolean): determines whether the tag should be treated
 * as an html tag in surrounding xml</li>
 * <li>movable (boolean): the DiplomatXML attribute for bpt,it,ut,ph tags,
 * specifying whether these tags can be moved around in the editor.</li>
 * <li>erasable (boolean): the DiplomatXML attribute for bpt,it,ut,ph tags,
 * specifying whether these tags can be deleted in the editor.</li>
 * </ul>
 */
public class Rule implements Cloneable, ErrorMessages
{
    private boolean translate = true;
    private boolean translatable = true; // if false, it's "localizable"
    private boolean inline = false;
    private boolean movable = false;
    private boolean erasable = false;
    private boolean containedInHtml = false;
    private boolean internal = false;
    private String dataFormat = null;
    private String type = null;
    private String preserveWhiteSpace = null;
    private String sid = null;
    private Set words = new HashSet();
    private String srcComment = null;
    private boolean isSrcCommentNode = false;

    private int priority = 10;

    public void setTranslate(boolean translate)
    {
        this.translate = translate;
    }

    public void setTranslatable(boolean translatable)
    {
        this.translatable = translatable;
    }

    public void setDataFormat(String dataFormat)
    {
        this.dataFormat = dataFormat;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setInline(boolean inline)
    {
        this.inline = inline;
    }

    public void setContainedInHtml(boolean containedInHtml)
    {
        this.containedInHtml = containedInHtml;
    }

    public void setMovable(boolean movable)
    {
        this.movable = movable;
    }

    public void setErasable(boolean erasable)
    {
        this.erasable = erasable;
    }

    public boolean translates()
    {
        return translate;
    }

    public boolean isTranslatable()
    {
        return translatable;
    }

    public String getDataFormat()
    {
        return dataFormat;
    }

    public String getType()
    {
        return type;
    }

    public boolean isContainedInHtml()
    {
        return containedInHtml;
    }

    public boolean isInline()
    {
        return inline;
    }

    public boolean isMovable()
    {
        return movable;
    }

    public boolean isErasable()
    {
        return erasable;
    }

    public Set getWords()
    {
        return words;
    }

    public void setWords(Set words)
    {
        this.words = words;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public boolean isInternal()
    {
        return internal;
    }

    public void setInternal(boolean internal)
    {
        this.internal = internal;
    }

    public String getPreserveWhiteSpace()
    {
        return preserveWhiteSpace;
    }

    public void setPreserveWhiteSpace(String preserveWhiteSpace)
    {
        this.preserveWhiteSpace = preserveWhiteSpace;
    }

    /**
     * <p>
     * Merges two Rule objects.
     * </p>
     * 
     * <p>
     * The purpose of this method is to merge multiple rules matching on a node
     * into a single rule.
     * </p>
     * 
     * <p>
     * A new rule object is created by cloning <em>this</em> rule, the
     * properties of the <em>new</em> object are set to the properties of the
     * <em>other</em> rule, but only if these properties are different from
     * their default values. The <em>new</em> rule object is returned and
     * neither <em>this</em> nor the <em>other</em> rule are modified.
     * </p>
     * 
     * <p>
     * The "translate" attribute will never be merged, its value will always be
     * derived from <code>this</code> rule.
     * </p>
     */
    public Rule merge(Rule that)
    {
        if (priority == that.priority)
            return mergeWithSamePriority(that);

        Rule newObj = null;

        try
        {
            newObj = (Rule) clone();
        }
        catch (CloneNotSupportedException e)
        {
            // Shouldn't reach here
        }

        if (newObj.dataFormat == null
                || (that.dataFormat != null && that.priority < newObj.priority))
        {
            newObj.dataFormat = that.dataFormat;
        }

        if (newObj.type == null
                || (that.type != null && that.priority < newObj.priority))
        {
            newObj.type = that.type;
        }

        if (newObj.sid == null
                || (that.sid != null && that.priority < newObj.priority))
        {
            newObj.sid = that.sid;
        }

        if (newObj.srcComment == null
                || (that.srcComment != null && that.priority < newObj.priority))
        {
            newObj.srcComment = that.srcComment;
        }

        if (that.isSrcCommentNode)
        {
            newObj.isSrcCommentNode = that.isSrcCommentNode;
        }

        if (newObj.preserveWhiteSpace == null
                || (that.preserveWhiteSpace != null && that.priority < newObj.priority))
        {
            newObj.preserveWhiteSpace = that.preserveWhiteSpace;
        }

        if (priority > that.priority)
        {
            newObj.translate = that.translate;
            newObj.translatable = that.translatable;
            newObj.inline = that.inline;
            newObj.containedInHtml = that.containedInHtml;
            newObj.movable = that.movable;
            newObj.erasable = that.erasable;
        }

        Set words = that.getWords();
        if (words != null && words.size() > 0)
        {
            newObj.words.addAll(words);
        }

        if (that.isInternal())
        {
            newObj.setInternal(true);
        }

        return newObj;
    }

    /**
     * <p>
     * Merges two Rule objects.
     * </p>
     * 
     * <p>
     * The purpose of this method is to merge multiple rules matching on a node
     * into a single rule.
     * </p>
     * 
     * <p>
     * A new rule object is created by cloning <em>this</em> rule, the
     * properties of the <em>new</em> object are set to the properties of the
     * <em>other</em> rule, but only if these properties are different from
     * their default values. The <em>new</em> rule object is returned and
     * neither <em>this</em> nor the <em>other</em> rule are modified.
     * </p>
     * 
     * <p>
     * The "translate" attribute will never be merged, its value will always be
     * derived from <code>this</code> rule.
     * </p>
     */
    public Rule mergeWithSamePriority(Rule that)
    {
        Rule newObj = null;

        try
        {
            newObj = (Rule) clone();
        }
        catch (CloneNotSupportedException e)
        {
            // Shouldn't reach here
        }

        // translate attribute will not be merged

        if (that.translatable != true)
        {
            newObj.translatable = that.translatable;
        }

        if (that.dataFormat != null)
        {
            newObj.dataFormat = that.dataFormat;
        }

        if (that.type != null)
        {
            newObj.type = that.type;
        }

        if (that.inline != false)
        {
            newObj.inline = that.inline;
        }

        if (that.containedInHtml != false)
        {
            newObj.containedInHtml = that.containedInHtml;
        }

        if (that.movable != false)
        {
            newObj.movable = that.movable;
        }

        if (that.isSrcCommentNode)
        {
            newObj.isSrcCommentNode = that.isSrcCommentNode;
        }

        if (that.erasable != false)
        {
            newObj.erasable = that.erasable;
        }

        Set words = that.getWords();
        if (words != null && words.size() > 0)
        {
            newObj.words.addAll(words);
        }

        if (that.getSid() != null)
        {
            newObj.setSid(that.getSid());
        }

        if (that.isInternal())
        {
            newObj.setInternal(true);
        }

        if (that.getPreserveWhiteSpace() != null)
        {
            newObj.setPreserveWhiteSpace(that.getPreserveWhiteSpace());
        }

        return newObj;
    }

    public static String getSid(Map ruleMap, Node node)
    {
        Rule rule = null;
        String sid = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
            if (rule != null)
            {
                sid = rule.getSid();
            }
        }

        return sid;
    }

    public static String getSrcComment(Map ruleMap, Node node)
    {
        Rule rule = null;
        String cmt = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
            if (rule != null)
            {
                cmt = rule.getSrcComment();
            }
        }

        return cmt;
    }

    /**
     * Determines whether the specified Node should be extracted (return value
     * <code>true</code>) or should go to the skeleton (return value
     * <code>false</code>). If no rule matches the node, the default return
     * values are <code>true</code> for element nodes, <code>false</code> for
     * attribute nodes, and <code>true</code> for all other nodes.
     */
    public static boolean extracts(Map ruleMap, Node node)
    {
        boolean ret = true;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.translate;
        }
        else
        {
            switch (node.getNodeType())
            {
                case Node.ELEMENT_NODE:
                    ret = true;
                    break;

                case Node.ATTRIBUTE_NODE:
                    ret = false;
                    break;

                default:
                    ret = true;
                    break;
            }
        }

        return ret;
    }

    /**
     * Determines whether the specified Node should be extracted as translatable
     * or localizable. If the return value is <code>true</code>, the node is
     * translatable. If the return value is <code>false</code>, the node is
     * localizable. If there is no rule matching the node, the default return
     * value is <code>true</code>.
     */
    public static boolean isTranslatable(Map ruleMap, Node node)
    {
        boolean ret = true;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.translatable;
        }
        else
        {
            ret = true;
        }

        return ret;
    }

    /**
     * Determines whether the specified Node is contained in HTML If there is no
     * rule matching the node, the default return value is <code>false</code>.
     */
    public static boolean isContainedInHtml(Map ruleMap, Node node)
    {
        boolean ret = true;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.containedInHtml;
        }
        else
        {
            ret = false;
        }

        return ret;
    }

    /**
     * Determines whether the specified Node is extracted as inline. If there is
     * no rule matching the node, the default return value is <code>false</code>
     * .
     */
    public static boolean isInline(Map ruleMap, Node node)
    {
        boolean ret = true;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.inline;
        }
        else
        {
            ret = false;
        }

        return ret;
    }

    public static boolean isInternal(Map ruleMap, Node node)
    {
        boolean ret = false;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.isInternal();
        }

        return ret;
    }

    public static boolean isPreserveWhiteSpace(Map ruleMap, Node node,
            boolean defaultValue)
    {
        boolean ret = defaultValue;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null && rule.getPreserveWhiteSpace() != null)
        {
            ret = "true".equalsIgnoreCase(rule.getPreserveWhiteSpace());
        }

        return ret;
    }

    /**
     * Determines whether the specified Node is extracted as movable. If no rule
     * matches the node, the default return value is <code>false</code>.
     */
    public static boolean isMovable(Map ruleMap, Node node)
    {
        boolean ret = true;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.movable;
        }
        else
        {
            ret = false;
        }

        return ret;
    }

    /**
     * Determines whether the specified Node is extracted as erasable. If no
     * rule matches the node, the default return value is <code>false</code>.
     */
    public static boolean isErasable(Map ruleMap, Node node)
    {
        boolean ret = true;

        Rule rule = null;
        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.erasable;
        }
        else
        {
            ret = false;
        }
        return ret;
    }

    public static boolean isSrcCommentNode(Map ruleMap, Node node)
    {
        boolean ret = false;

        Rule rule = null;
        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.isSrcCommentNode;
        }
        else
        {
            ret = false;
        }
        return ret;
    }

    /**
     * Returns the data format (or datatype in DiplomatXML) as which a node is
     * extracted. If no rule matches the node, <code>null</code> is returned.
     */
    public static String getDataFormat(Map ruleMap, Node node)
    {
        String ret = null;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.dataFormat;
        }

        return ret;
    }

    /**
     * Returns the DiplomatXML type as which a node is extracted. If no rule
     * matches the node, <code>null</code> is returned.
     */
    public static String getType(Map ruleMap, Node node)
    {
        String ret = null;
        Rule rule = null;

        if (ruleMap != null)
        {
            rule = (Rule) ruleMap.get(node);
        }

        if (rule != null)
        {
            ret = rule.type;
        }

        return ret;
    }

    /**
     * Gets words that not need count and translate for this node.
     * 
     * @param ruleMap
     * @param node
     * @return
     */
    public static Set getWords(Map ruleMap, Node node)
    {
        if (ruleMap == null || node == null)
        {
            return null;
        }

        Set words = new HashSet();
        List nodes = new ArrayList();

        // Gets this node and parent nodes.
        nodes.add(node);
        Node parentNode = node.getParentNode();
        while (parentNode != null)
        {
            nodes.add(parentNode);
            parentNode = parentNode.getParentNode();
        }

        // Gets all words in this node and parent nodes.
        for (int i = 0; i < nodes.size(); i++)
        {
            Node nodeI = (Node) nodes.get(i);
            Rule rule = (Rule) ruleMap.get(nodeI);
            if (rule != null)
            {
                Set ruleWords = rule.getWords();
                if (ruleWords != null && ruleWords.size() > 0)
                {
                    words.addAll(ruleWords);
                }
            }
        }

        // Removes the words that included in other words.
        // For example, if there are two word: "this is" and "is",
        // "is" will be removed.
        List wordsList = new ArrayList();
        wordsList.addAll(words);
        SortUtil.sort(wordsList, getLengthComparator());
        for (int i = 0; i < wordsList.size() - 1; i++)
        {
            String words1 = (String) wordsList.get(i);
            boolean isIncluded = false;

            for (int j = i + 1; j < wordsList.size(); j++)
            {
                String words2 = (String) wordsList.get(j);
                if (words2.indexOf(words1) > -1)
                {
                    isIncluded = true;
                    break;
                }
            }

            if (isIncluded)
            {
                wordsList.remove(i);
                i--;
            }
        }

        words = new HashSet();
        words.addAll(wordsList);

        return words;
    }

    /**
     * Gets a comparator that sort by string length.
     * 
     * @return
     */
    private static Comparator getLengthComparator()
    {
        return new Comparator()
        {

            public int compare(Object arg0, Object arg1)
            {
                return arg0.toString().length() - arg1.toString().length();
            }
        };
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public String getSrcComment()
    {
        return srcComment;
    }

    public void setSrcComment(String srcComment)
    {
        this.srcComment = srcComment;
    }

    public boolean isSrcCommentNode()
    {
        return isSrcCommentNode;
    }

    public void setIsSrcCommentNode(boolean isSrcCommentNode)
    {
        this.isSrcCommentNode = isSrcCommentNode;
    }
}
