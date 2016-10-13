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

package com.globalsight.terminology;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.terminology.Definition;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import com.globalsight.util.UTC;

import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.util.NodeComparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class EntryUtils
{
    /**
     * Sets the entry's concept ID.
     */
    static public void setConceptId(Entry p_entry, long p_cid)
        throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();
        Element concept = root.element("concept");

        if (concept == null)
        {
            // create new <concept> element and add to tree
            root.addElement("concept").addText(String.valueOf(p_cid));
        }
        else
        {
            concept.setText(String.valueOf(p_cid));
        }

        // let entry know its dom is dirty
        p_entry.setDom(dom);
    }

    /**
     * Gets the entry's concept ID.
     * @return 0 if the entry has no ID yet, else a positive number.
     */
    static public long getConceptId(Entry p_entry)
        throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();
        Element concept = root.element("concept");

        if (concept == null || concept.getText().length() == 0)
        {
            return 0;
        }
        else
        {
            return Long.parseLong(concept.getText());
        }
    }
    
    /**
     * Get TBX files entry id
     * @param entry
     * @return
     * @throws TermbaseException
     */
    public static String getTbxTermEntryId(Entry entry)
			throws TermbaseException {
    	Document dom = entry.getDom();
    	Element root = dom.getRootElement();
    	
    	if (root.attribute("id") == null
				|| root.attribute("id").getText().length() == 0) {
    		return null;
    	} else {
    		return root.attribute("id").getText();
    	}
    }
    
    /**
     * Get the language name of a certain language
     * @param langLocale
     * @return language name
     */
    public static String getLanguageName(String langLocale) {
    	Locale en = new Locale("en");
    	Locale language = new Locale(langLocale);
    	String displayName = language.getDisplayName(en);
    	if (displayName.equalsIgnoreCase(langLocale)) {
    	    return langLocale;
    	} else {
    	    return displayName;
    	}
    }

    /**
     * Inserts <transacGrp><transac type="origination">user</transac>
     * <date></date></transacGrp> into an entry.  This is for
     * Termbase.addEntry() where the creation time is NOW.
     *
     * For imported entries that may have their own timestamp, use a
     * batch import function.
     */
    static public void setCreationTimeStamp(Entry p_entry,
        SessionInfo p_session)
        throws TermbaseException
    {
        String timestamp = UTC.valueOf(p_session.getTimestamp());
        String username = p_session.getUserName();

        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();

        Element transac = (Element)root.selectSingleNode(
            "/conceptGrp/transacGrp/transac[@type='origination']");
        Element conceptGrp, transacGrp, date;

        if (transac != null)
        {
            // Timestamp sneaked in, overwrite.
            transac.setText(username);

            transacGrp = transac.getParent();
            date = transacGrp.element("date");
            if (date == null)
            {
                transacGrp.addElement("date").addText(timestamp);
            }
            else
            {
                date.setText(timestamp);
            }
        }
        else
        {
            // No origination timestamp, create one.
            transacGrp = root.addElement("transacGrp");
            transac = transacGrp.addElement("transac").
                addAttribute("type", "origination").
                addText(username);
            date = transacGrp.addElement("date").addText(timestamp);
        }

        // Man this was a cinch. Me love dom4j.

        // let entry know its dom is dirty
        p_entry.setDom(dom);
    }


    /**
     * Inserts <transacGrp><transac type="modification">user</transac>
     * <date></date></transacGrp> into an entry.  This is for
     * Termbase.updateEntry() where the creation time is NOW.
     */
    static public void setModificationTimeStamp(Entry p_entry,
        SessionInfo p_session)
        throws TermbaseException
    {
        String timestamp = UTC.valueOf(p_session.getTimestamp());
        String username = p_session.getUserName();

        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();

        Element transac = (Element)root.selectSingleNode(
            "/conceptGrp/transacGrp/transac[@type='modification']");
        Element conceptGrp, transacGrp, date;

        if (transac != null)
        {
            // Timestamp exists, overwrite.
            transac.setText(username);

            transacGrp = transac.getParent();
            date = transacGrp.element("date");
            if (date == null)
            {
                transacGrp.addElement("date").addText(timestamp);
            }
            else
            {
                date.setText(timestamp);
            }
        }
        else
        {
            // No modification timestamp, create one.
            transacGrp = root.addElement("transacGrp");
            transac = transacGrp.addElement("transac").
                addAttribute("type", "modification").
                addText(username);
            transacGrp.addElement("date").addText(timestamp);
        }

        // let entry know its dom is dirty
        p_entry.setDom(dom);
    }

    /** Returns the list of <languageGrp> nodes in the entry. */
    static public List getLanguageGrps(Entry p_entry)
        throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();

        return root.selectNodes("//languageGrp");
    }

    /** Returns the list of <term> nodes in the entry. */
    static public List getTerms(Node p_node)
        throws TermbaseException
    {
        Entry entry = new Entry(p_node.asXML());
        return getTerms(entry);
    }

    /** Returns the list of <term> nodes in the entry. */
    static public List getTerms(Entry p_entry)
        throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();

        return root.selectNodes("//term");
    }
    
    static public String getPreferredTbxTerm(Entry p_entry, String p_language,
            String fileType) throws TermbaseException
    {
        if (fileType != null
                && fileType.equalsIgnoreCase(WebAppConstants.TERMBASE_TBX))
        {
            return getPreferredTbxTerm(p_entry, p_language);

        }
        else
        {
            return getPreferredTerm(p_entry, p_language);
        }
    }
    
    /**
     * Finds the preferred term in the given language. The preferred
     * term is the one that has a usage='preferred' field. If no term
     * is qualified as preferred, the first term is returned.
     *
     * @return preferred term as string if found, else null.
     */
    static public String getPreferredTbxTerm(Entry p_entry, String p_language)
        throws TermbaseException
    {
    	Document dom = p_entry.getDom();
        Element root = dom.getRootElement();
        String result = null;
        
        List langSets = root.elements("langSet");
        if (langSets.size() == 0)
        {
            return null;
        }
        
        for (int i = 0; i < langSets.size(); i++)
        {
        	Element langSet = (Element) langSets.get(i);
        	if (EntryUtils.getLanguageName(langSet.attribute("lang").getText()).equals(p_language))
        	{
        		if (langSet.elements("ntig").size() != 0) {
        			result = langSet.element("ntig").element("termGrp").element("term").getText();
        		}
        		if (langSet.elements("tig").size() != 0) {
        			result = langSet.element("tig").element("term").getText();
        		}
        		
        		break;
        	}
        }
		return result;
    }

    /**
     * Finds the preferred term in the given language. The preferred
     * term is the one that has a usage='preferred' field. If no term
     * is qualified as preferred, the first term is returned.
     *
     * @return preferred term as string if found, else null.
     */
    static public String getPreferredTerm(Entry p_entry, String p_language)
        throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();

        List termGrps = root.selectNodes("//languageGrp[./language/@name='" +
            p_language + "']/termGrp");

        if (termGrps.size() == 0)
        {
            return null;
        }

        for (int i = 0, max = termGrps.size(); i < max; i++)
        {
            Element termGrp = (Element)termGrps.get(i);
            Node usage = termGrp.selectSingleNode(
                "descripGrp/descrip[@type='usage']");

            if (usage != null && "preferred".equals(usage.getText()))
            {
                return termGrp.element("term").getText();
            }
        }

        return ((Element)termGrps.get(0)).element("term").getText();
    }

    /**
     * Returns the inner text like Element.getText() but for all
     * embedded text nodes.
     */
    static public String getInnerText(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(node.getText());
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                result.append(getInnerText((Element)node));
            }
        }

        return result.toString();
    }

    /**
     * Returns the XML representation like Element.asXML() but without
     * the top-level tag.
     */
    static public String getInnerXml(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(EditUtil.encodeXmlEntities(node.getText()));
            }
            else
            {
                // Element nodes write their text nodes correctly.
                result.append(node.asXML());
            }
        }

        return result.toString();
    }

    /**
     * Returns the HTML representation of an element's text. This is
     * like getInnerXml() but doesn't encode apostrophes.
     */
    static public String getInnerHtml(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(EditUtil.encodeHtmlEntities(node.getText()));
            }
            else
            {
                // Element nodes write their text nodes correctly.
                result.append(node.asXML());
            }
        }

        return result.toString();
    }

    // This is a private method because it encodes only those entities
    // that must be encoded in text nodes, not those in attribute
    // notes (quot and apos).  For the general case, use
    // com.globalsight.util.edit.EditUtil#encodeXmlEntities()
    static private String encodeXmlEntities(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }

        StringBuffer res = new StringBuffer(s.length());

        for (int i = 0, max = s.length(); i < max; i++)
        {
            char c = s.charAt(i);

            switch (c)
            {
            case '<':  res.append("&lt;");   break;
            case '>':  res.append("&gt;");   break;
            case '&':  res.append("&amp;");  break;
            default:   res.append(c);        break;
            }
        }

        return res.toString();
    }
    
    /**
     * <p>Checks that an TBX entry is consistent with the termbase
     * definition (at least one term per language, languages defined
     * in termbase, required fields present, etc).</p>
     *
     * <p>For now, check that we have at least one term. General entry
     * structure validation should have been done by the XML Parser
     * that parsed the entry into DOM - using the EntryStructure
     * schema. We also remove empty fields and groups.</p>
     *
     * TO BE COMPLETED
     */
    public static void normalizeTbxEntry(Entry p_entry, Definition p_definition)
    	throws TermbaseException {
    	try {
    		boolean ok = false;
    		// Remove empty fields and then check if there's at least
            // one term in the entry.
    		pruneEntry(p_entry);
    		
    		Document dom = p_entry.getDom();
            Element root = dom.getRootElement();
            
            //termEntry-level
            for (Iterator it = root.elementIterator(); it.hasNext();) {
            	Element tnode = (Element)it.next();
            	
            	if (tnode.getName().equals("langSet")) {
            		//langSet-level
            		for (Iterator it2 = tnode.elementIterator(); it2.hasNext(); ) {
            			Element lnode = (Element)it2.next();
            			
            			if (lnode.getName().equals("ntig")) {
            				//ntig-level
            				for (Iterator it3 = lnode.elementIterator(); it3.hasNext(); ) {
            					Element ntignode = (Element)it3.next();
            					
            					if (ntignode.getName().equals("termGrp")) {
            						//term-node
            						for (Iterator it4 = ntignode.elementIterator(); it4.hasNext(); ) {
            							Element termnode = (Element)it4.next();
            							
            							if (termnode.getName().equals("term")) {
            								String text = termnode.getText();
            								if (text != null && text.length() > 0)
                                            {
                                                ok = true;
                                                break;
                                            }
            							}
            						}
            					}
            					if (ok) break;
            				}
            				if (!ok) invalidEntry("no terms defined");
            			}
            			if (lnode.getName().equals("tig")){
            				//tig-level
            				for (Iterator it3 = lnode.elementIterator(); it3.hasNext(); ) {
            					Element tignode = (Element)it3.next();
            					
            					if (tignode.getName().equals("term")) {
            						String text = tignode.getText();
            						if (text != null && text.length() > 0)
                                    {
                                        ok = true;
                                        break;
                                    }
            					}
            				}
            			}
            			if (ok) break;
            		}
            	}
            	if (ok) break;
            }
            if (!ok)
            {
                invalidEntry("no languages defined");
            }
    	} catch (TermbaseException e) {
            throw e;
        } catch (Exception e) {
            invalidEntry(e.getMessage());
        }
    }

    /**
     * <p>Checks that an entry is consistent with the termbase
     * definition (at least one term per language, languages defined
     * in termbase, required fields present, etc).</p>
     *
     * <p>For now, check that we have at least one term. General entry
     * structure validation should have been done by the XML Parser
     * that parsed the entry into DOM - using the EntryStructure
     * schema. We also remove empty fields and groups.</p>
     *
     * TO BE COMPLETED
     */
    static public void normalizeEntry(Entry p_entry, Definition p_definition)
        throws TermbaseException
    {
        try
        {
            boolean ok = false;

            // Remove empty fields and then check if there's at least
            // one term in the entry.
            pruneEntry(p_entry);

            Document dom = p_entry.getDom();
            Element root = dom.getRootElement();

            // CONCEPT-LEVEL
            for (Iterator it = root.elementIterator(); it.hasNext(); )
            {
                Element cnode = (Element)it.next();

                if (cnode.getName().equals("languageGrp"))
                {
                    // LANGUAGE-LEVEL
                    for (Iterator it2 = cnode.elementIterator();
                         it2.hasNext(); )
                    {
                        Element lnode = (Element)it2.next();

                        if (lnode.getName().equals("termGrp"))
                        {
                            // TERM-LEVEL
                            for (Iterator it3 = lnode.elementIterator();
                                 it3.hasNext(); )
                            {
                                Element tnode = (Element)it3.next();

                                if (tnode.getName().equals("term"))
                                {
                                    String text = tnode.getText();
                                    if (text != null && text.length() > 0)
                                    {
                                        ok = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (ok) break;
                    }

                    if (!ok)
                    {
                        invalidEntry("no terms defined");
                    }
                }

                if (ok) break;
            }

            if (!ok)
            {
                invalidEntry("no languages defined");
            }
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            invalidEntry(e.getMessage());
        }
    }

    static public void pruneEntry(Entry p_entry)
        throws TermbaseException
    {
        try
        {
            Document dom = p_entry.getDom();
            Element root = dom.getRootElement();

            boolean dirty = removeInsignificantWhitespace(root);
            dirty |= pruneEmptyFields(root);

            if (dirty)
            {
                // let entry know its dom is dirty
                p_entry.setDom(dom);
            }
        }
        catch (Exception e)
        {
            invalidEntry(e.getMessage());
        }
    }

    /**
     * <p>Removes insignificant whitespace between elements in groups.
     * Whitespace inside non-Grps, i.e. the data elements is
     * significant and is preserved.</p>
     *
     * This method is needed for comparing nodes.
     */
    static private boolean removeInsignificantWhitespace(Element p_node)
    {
        boolean dirty = false;

        boolean isGrp = p_node.getName().endsWith("Grp");

        for (Iterator it = p_node.content().iterator(); it.hasNext(); )
        {
            Node temp = (Node)it.next();

            if (temp.getNodeType() != Node.ELEMENT_NODE)
            {
                if (isGrp)
                {
                    it.remove();
                    dirty = true;
                }

                continue;
            }

            Element node = (Element)temp;

            // Depth-first recursion.
            dirty |= removeInsignificantWhitespace(node);
        }

        return dirty;
    }

    /**
     * <p>Recursively prunes empty fields and groups from the given entry.
     * The entry is destructively modified.</p>
     *
     * <p>A depth-first traversal first removes empty leaf nodes, and
     * then groups that are empty or not fully filled.</p>
     *
     * <p>Example: a <descripGrp> must contain at least one <descrip>
     * child.  A <languageGrp> must contain at least one <language>
     * and one <termGrp> child (2 children minimum).</p>
     *
     * <p>As of 6.2, non-relevant whitespace nodes are also removed.</p>
     * <p>As of 6.3, admissible empty HTML tags are not pruned: IMG, HR, BR.</p>
     */
    static private boolean pruneEmptyFields(Element p_node)
    {
        boolean dirty = false;

        if (!p_node.hasContent())
        {
            return dirty;
        }

        // Cannot iterate child elements with node.elementIterator()
        // because that doesn't implement the remove() method.
        for (Iterator it = p_node.content().iterator(); it.hasNext(); )
        {
            Node temp = (Node)it.next();

            // Only work on child elements.
            if (temp.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }

            Element node = (Element)temp;

            // Depth-first recursion.
            dirty |= pruneEmptyFields(node);

            // Sat Jan 15 02:17:38 2005 CvdL Need to allow empty HTML tags.
            String name = node.getName().toLowerCase();
            if (name.equals("language") || name.equals("img") ||
                name.equals("hr") || name.equals("br"))
            {
                continue;
            }

            // Leaf nodes
            if (node.isTextOnly())
            {
                String value = node.getText();
                if (value == null || value.trim().length() == 0)
                {
                    // prune empty leaf nodes
                    it.remove();
                    dirty = true;
                }
            }
            else
            {
                // Group nodes
                int childCount = node.elements().size();
                if (childCount == 0 ||
                    (node.getName().equals("languageGrp") && childCount < 2))
                {
                    // prune empty groups
                    it.remove();
                    dirty = true;
                }
            }
        }

        return dirty;
    }
    
    public static Entry mergeEntries(Entry p_one, Entry p_two, String fileType)
            throws TermbaseException
    {
        if (fileType != null
                && fileType.equalsIgnoreCase(WebAppConstants.TERMBASE_TBX))
        {
            return mergeTbxEntries(p_one, p_two);

        }
        else
        {
            return mergeEntries(p_one, p_two);
        }
    }
    
    /**
     * <p>Merges the information in entry2 into entry1.</p>
     *
     * @return the modified version of entry1.
     */
    public static Entry mergeTbxEntries(Entry p_one, Entry p_two)
    	throws TermbaseException
    {
    	Entry result = new Entry();
    	
    	pruneEntry(p_one);
        pruneEntry(p_two);
        
        Document dom1 = p_one.getDom();
        Element root1 = dom1.getRootElement();

        Document dom2 = p_two.getDom();
        Element root2 = dom2.getRootElement();
        
        NodeComparator comp = new NodeComparator();
        mergeTbxInnerGroups(root1, root2, comp);

        // let entry 1 know its dom is dirty
        result.setDom(root1.getDocument());

        return result;
    }

    /**
     * <p>Merges the information in entry2 into entry1.</p>
     *
     * @return the modified version of entry1.
     */
    static public Entry mergeEntries(Entry p_one, Entry p_two)
        throws TermbaseException
    {
        // Remove empty fields and normalize whitespace.
        pruneEntry(p_one);
        pruneEntry(p_two);

        Document dom1 = p_one.getDom();
        Element root1 = dom1.getRootElement();

        Document dom2 = p_two.getDom();
        Element root2 = dom2.getRootElement();

        NodeComparator comp = new NodeComparator();
        mergeInnerGroups(root1, root2, comp);

        // let entry 1 know its dom is dirty
        p_one.setDom(dom1);

        return p_one;
    }
    
    private static void mergeTbxInnerGroups(Element p_one, Element p_two,
            NodeComparator p_comp)
    {
    	for (Iterator it = p_two.elementIterator(); it.hasNext(); )
    	{
    		Element node = (Element)it.next();
            it.remove();
            node.detach();
            
            String name = node.getName();
            
            if (name.equals("langSet")) 
            {
            	mergeLangSet(p_one, p_two, node, p_comp);
            }
    	}
    }
    
    /**
     * Used to merge Tag ntig or tig of TBX files. 
     * TBX files may contain several same languages in a single termentry,
     * when merging two new files, all the same languages should totally considered.
     * @param sameLangSetsInATermentry
     * @param p_one
     * @param p_two
     * @param p_comp
     */
    private static void mergeTbxNtigsOrTigs(List sameLangSetsInATermentry,
			Element p_one, Element p_two, NodeComparator p_comp)
    {
    	for (Iterator it = p_two.elementIterator(); it.hasNext(); )
    	{
    		Element node = (Element)it.next();
            it.remove();
            node.detach();
            
            String name = node.getName();
            
            if (name.equals("ntig")) {
            	String newTerm = node.element("termGrp").element("term").getText();
            	List<String> oldTerms = new ArrayList<String>();
            	
            	for (int i = 0; i < sameLangSetsInATermentry.size(); i++) {
            		Element tmp = (Element) sameLangSetsInATermentry.get(i);
            		
            		List<Element> ntigs = tmp.elements("ntig");
            		for (int j = 0; j < ntigs.size(); j++) {
            			try {
            				String oldTerm = ntigs.get(j).element("termGrp").element("term").getText();
            				oldTerms.add(oldTerm);
            			} catch (Exception e) {
            				//langSet or termGrp is empty
            			}
            		}
            	}
            	if (!oldTerms.contains(newTerm)) 
        		{
        			p_one.add(node);
        		}
            }
            if (name.equals("tig")) {
            	String newTerm = node.element("term").getText();
            	List<String> oldTerms = new ArrayList<String>();
        		
            	for (int i = 0; i < sameLangSetsInATermentry.size(); i++) {
            		Element tmp = (Element) sameLangSetsInATermentry.get(i);
            		
            		List<Element> tigs = p_one.elements("tig");
            		for (int j = 0; j < tigs.size(); j++) {
            			try {
            				String oldTerm = tigs.get(j).element("term").getText();
                			oldTerms.add(oldTerm);
            			} catch (Exception e) {
            				//langSet or termGrp is empty
            			}
            		}
            	}
            	if (!oldTerms.contains(newTerm)) 
        		{
        			p_one.add(node);
        		}
            }
    	}
    }

	static private void mergeInnerGroups(Element p_one, Element p_two,
        NodeComparator p_comp)
    {
        for (Iterator it = p_two.content().iterator(); it.hasNext(); )
        {
            Element node = (Element)it.next();
            it.remove();
            node.detach();

            String name = node.getName();

            if (name.equals("transacGrp"))
            {
                continue;
            }
            else if (name.equals("noteGrp"))
            {
                mergeNoteGrp(p_one, p_two, node, p_comp);
            }
            else if (name.equals("sourceGrp"))
            {
                mergeSourceGrp(p_one, p_two, node, p_comp);
            }
            else if (name.equals("descripGrp"))
            {
                mergeDescripGrp(p_one, p_two, node, p_comp);
            }
            else if (name.equals("languageGrp"))
            {
                mergeLanguageGrp(p_one, p_two, node, p_comp);
            }
            else if (name.equals("termGrp"))
            {
                mergeTermGrp(p_one, p_two, node, p_comp);
            }
        }
    }

    static private void mergeNoteGrp(Element p_one, Element p_two,
        Element p_noteGrp, NodeComparator p_comp)
    {
        Element p_note = p_noteGrp.element("note");

        if (p_note == null || !p_note.hasContent())
        {
            return;
        }

        // Find all noteGrps in 1.
        List matches = p_one.selectNodes("noteGrp");

        if (matches == null || matches.size() == 0)
        {
            // No notes exist, add the new one to the end.
            p_one.add(p_noteGrp);

            return;
        }

        // Check if one of the matches is the same note.
        for (int i = 0, max = matches.size(); i < max; i++)
        {
            Element noteGrp = (Element)matches.get(i);
            Element note = noteGrp.element("note");

            if (fieldEquals(note, p_note, p_comp))
            {
                // could be a case/formatting-insensitive match
                note.detach();
                noteGrp.content().add(0, p_note);

                return;
            }
        }

        // Note does not exist, add it to the end.
        p_one.add(p_noteGrp);
    }

    static private void mergeSourceGrp(Element p_one, Element p_two,
        Element p_sourceGrp, NodeComparator p_comp)
    {
        Element p_source = p_sourceGrp.element("source");

        if (p_source == null || !p_source.hasContent())
        {
            return;
        }

        // Find all sourceGrps in 1.
        List matches = p_one.selectNodes("sourceGrp");

        if (matches == null || matches.size() == 0)
        {
            // No sources exist, add the new one.
            p_one.add(p_sourceGrp);

            return;
        }

        // Check if one of the matches is the same source.
        for (int i = 0, max = matches.size(); i < max; i++)
        {
            Element sourceGrp = (Element)matches.get(i);
            Element source = sourceGrp.element("source");

            if (fieldEquals(source, p_source, p_comp))
            {
                // could be a case/formatting-insensitive match
                source.detach();
                sourceGrp.content().add(0, p_source);

                mergeInnerGroups(sourceGrp, p_sourceGrp, p_comp);

                return;
            }
        }

        // Source does not exist, add it.
        p_one.add(p_sourceGrp);
    }

    static private void mergeDescripGrp(Element p_one, Element p_two,
        Element p_descripGrp, NodeComparator p_comp)
    {
        Element p_descrip = p_descripGrp.element("descrip");

        if (p_descrip == null || !p_descrip.hasContent())
        {
            return;
        }

        String type = p_descrip.attributeValue("type");

        // some descrips can occur only once per entry.
        boolean occursOnce = fieldOccursOnce(type);

        // Find nodes that match the descrip.
        List matches = p_one.selectNodes(
            "descripGrp[descrip/@type='" + type + "']");

        if (matches == null || matches.size() == 0)
        {
            // descripGrp does not exist in entry 1, add.
            int index = findDescripInsertionPoint(p_one, type);
            p_one.content().add(index, p_descripGrp);

            return;
        }

        // Check if one of the matches contains the same descrip
        for (int i = 0, max = matches.size(); i < max; i++)
        {
            Element descripGrp = (Element)matches.get(i);
            Element descrip = descripGrp.element("descrip");

            if (fieldEquals(descrip, p_descrip, p_comp))
            {
                // could be a case/formatting-insensitive match
                descrip.detach();
                descripGrp.content().add(0, p_descrip);

                mergeInnerGroups(descripGrp, p_descripGrp, p_comp);

                return;
            }
        }

        // DescripGrp was not found, add new descripGrp to old.
        if (occursOnce)
        {
            // DescripGrp can occur only once, merge by overwriting.
            // (Single descrips also contain just text, no HTML.)
            Element descGrp = (Element)matches.get(0);
            Element desc = descGrp.element("descrip");

            desc.setText(getInnerText(p_descrip));

            mergeInnerGroups(descGrp, p_descripGrp, p_comp);
        }
        else
        {
            // Multiple descrips may exist (for e.g., definition),
            // so add the new one after the last.
            Element last = (Element)matches.get(matches.size() - 1);

            p_one.content().add(p_one.indexOf(last) + 1, p_descripGrp);
        }
    }
    
	private static void mergeLangSet(Element p_one, Element p_two,
			Element node, NodeComparator p_comp) 
	{
		String languageName = node.attribute("lang").getText();
		
		List langSets = p_one.elements("langSet");
		List<Element> langSetsWithEqualLanguageName = new ArrayList<Element>();
		for (int i = 0; i < langSets.size(); i++) 
		{
			Element tmp = (Element) langSets.get(i);
			if (tmp.attribute("lang").getText().equals(languageName)) {
				langSetsWithEqualLanguageName.add(tmp);
			}
		}
		if (langSetsWithEqualLanguageName.size() == 0) {
			p_one.add(node);
            return;
		}
		mergeTbxNtigsOrTigs(langSetsWithEqualLanguageName, langSetsWithEqualLanguageName.get(0), node, p_comp);
	}

    static private void mergeLanguageGrp(Element p_one, Element p_two,
        Element p_languageGrp, NodeComparator p_comp)
    {
        Element p_language = p_languageGrp.element("language");

        // language element is an empty element.
        if (p_language == null)
        {
            return;
        }

        String name = p_language.attributeValue("name");

        // Find language in entry 1.
        Element languageGrp = (Element)p_one.selectSingleNode(
            "languageGrp[language/@name='" + name + "']");

        if (languageGrp == null)
        {
            // languageGrp does not exist in entry 1, add to the end.
            p_one.add(p_languageGrp);

            return;
        }

        mergeInnerGroups(languageGrp, p_languageGrp, p_comp);
    }

    static private void mergeTermGrp(Element p_one, Element p_two,
        Element p_termGrp, NodeComparator p_comp)
    {
        Element p_term = p_termGrp.element("term");

        if (p_term == null || !p_term.hasContent())
        {
            return;
        }

        // Find the term node in entry 1.
        List matches = p_one.selectNodes("termGrp");

        for (int i = 0, max = matches.size(); i < max; i++)
        {
            Element termGrp = (Element)matches.get(i);
            Element term = termGrp.element("term");

            if (fieldEquals(term, p_term, p_comp))
            {
                mergeInnerGroups(termGrp, p_termGrp, p_comp);

                return;
            }
        }

        // termGrp does not exist in entry 1, add to the end.
        p_one.add(p_termGrp);
    }

    /**
     * Tests if two elements are equal. If the elements have simple
     * content they are compared as is (case-sensitive), otherwise the
     * comparison ignores case and embedded formatting.
     */
    static private boolean fieldEquals(Element p_one, Element p_two,
        NodeComparator p_comp)
    {
        if (p_comp.compare(p_one, p_two) == 0)
        {
            return true;
        }

        if (p_one.hasContent() || p_two.hasContent())
        {
            String text1 = getInnerText(p_one);
            String text2= getInnerText(p_two);

            if (text1.equalsIgnoreCase(text2))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * For descrip type="status|domain|project", finds a point at the
     * beginning, for other descrips a point at the end is returned.
     */
    static private int findDescripInsertionPoint(Element p_one, String p_type)
    {
        int result = 0;

        boolean occursOnce = fieldOccursOnce(p_type);

        List content = p_one.content();
        for (int i = 0, max = content.size(); i < max; i++)
        {
            Element elem = (Element)content.get(i);

            if (elem.getName().equals("languageGrp"))
            {
                // past descrips, bail out.
                result = i;
                break;
            }

            if (!elem.getName().equals("descripGrp"))
            {
                continue;
            }

            String type = elem.element("descrip").attributeValue("type");

            if (occursOnce)
            {
                if (!fieldOccursOnce(type))
                {
                    result = i;
                    break;
                }
            }

            result = i;
        }

        return result;
    }

    static public boolean fieldOccursOnce(String p_type)
    {
        if (p_type.equals("domain") || p_type.equals("project") ||
            p_type.equals("status"))
        {
            return true;
        }

        return false;
    }

    /** Helper method to throw an INVALID_ENTRY exception */
    static public void invalidEntry(String p_reason)
        throws TermbaseException
    {
        String[] args = { p_reason };
        throw new TermbaseException(
            TermbaseExceptionMessages.MSG_INVALID_ENTRY, args, null);
    }

    /* Test code
    static String s_entry =
        "<conceptGrp>" +
        " <descripGrp><descrip type=\"status\">Approved</descrip></descripGrp>" +
        " <descripGrp><descrip type=\"domain\">Natural Languages</descrip></descripGrp>" +
        " <descripGrp><descrip type=\"project\">System 4</descrip></descripGrp>" +
        "<languageGrp><language name=\"German\" locale=\"de\"/>" +
        "<termGrp><term>andere</term></termGrp>" +
        "<termGrp><term>preferred</term>" +
        "<descripGrp><descrip type=\"usage\">preferred</descrip></descripGrp>" +
        "</termGrp></languageGrp>" +
        "</conceptGrp>";

    public static void main(String[] args)
        throws Exception
    {
        Entry e = new Entry(s_entry);
        System.out.println(EntryUtils.getPreferredTerm(e, "German"));
        System.exit(0);
    }
    */

    /*
    static String s_entry1 =
        "<conceptGrp>" +
        " <descripGrp><descrip type=\"status\">Approved</descrip></descripGrp>" +
        "<languageGrp><language name=\"German\" locale=\"de\"/>" +
        "<termGrp><term>andere</term></termGrp>" +
        "</languageGrp>" +
        "<languageGrp><language name=\"French\" locale=\"fr\"/>" +
        "<termGrp><term>XXX</term></termGrp>" +
        "</languageGrp>" +
        "</conceptGrp>";

    static String s_entry2 =
        "<conceptGrp>" +
        " <descripGrp><descrip type=\"project\">System 4</descrip></descripGrp>" +
        "<languageGrp><language name=\"German\" locale=\"de\"/>" +
        "<termGrp><term>andere</term></termGrp>" +
        "<termGrp><term>preferred</term>" +
        "<descripGrp><descrip type=\"usage\">preferred</descrip></descripGrp>" +
        "</termGrp></languageGrp>" +
        "<languageGrp><language name=\"French\" locale=\"fr\"/>" +
        "<termGrp><term>XXX</term></termGrp>" +
        "<termGrp><term>YYY</term></termGrp>" +
        "</languageGrp>" +
        "</conceptGrp>";

    public static void main(String[] args)
        throws Exception
    {
        Entry e1 = new Entry(s_entry1);
        Entry e2 = new Entry(s_entry2);
        Entry e = EntryUtils.mergeEntries(e1, e2);
        System.out.println(e.getXml());
        System.exit(0);
    }
    */
}
