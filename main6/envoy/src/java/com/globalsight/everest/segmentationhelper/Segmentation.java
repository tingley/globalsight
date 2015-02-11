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
package com.globalsight.everest.segmentationhelper;

import com.globalsight.log.GlobalSightCategory;
import java.util.*;
import java.util.regex.*;

public class Segmentation {
	
	
	static private final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
        		Segmentation.class);
	

	/**
	 * translable represents the text to be segmented.
	 */
	private String translable = null;
	
	private SegmentationRule segmentationRule = new SegmentationRule();
	
	/**
	 * First we use breakIndex to hold the indexes
	 * originally produced, and then remove all nonbreak
	 * indexes holded by nonBreakIndex.
	 */
	private ArrayList breakIndex = new ArrayList();
	/**
	 * A temp ArrayList to hold the break index before
	 * handle format.
	 */
	private ArrayList interBreak = new ArrayList();
	
	/**
	 * The locale used by translable(text to be segmented).
	 */	
	private String locale = null;
	
	/**
	 *  nonBreakIndex is used to hold the indexs 
	 *  produced according to break exceptions in a rule
	 */
	private ArrayList nonBreakIndex = new ArrayList();
	
	/**
	 * White space is defined as [\t\n\f\r\p{Z}].
	 */
	private static final Pattern whitespace = Pattern.compile("\\s+");
	
	// "</  >"
	private static final Pattern closemark = Pattern.compile("</[^><]*[^/]>\\s*");
	// "<  />"
	private static final Pattern isolatemark = Pattern.compile("<[^/][^><]*/>\\s*");
	// "<   >"
	private static final Pattern openmark = Pattern.compile("<[^/][^><]*[^/]>\\s*");
	
	private Matcher whitespaceMatcher = null;
	
	private Matcher closemarkMatcher = null;
	
	private Matcher isolatemarkMatcher = null;
	
	private Matcher openmarkMatcher = null;
	
	
	public Segmentation()
	throws Exception
	{			
		
		locale = null;
        translable = null;
        segmentationRule = null;
		
	}
	
	public Segmentation(String p_locale, String p_translable, SegmentationRule p_srx)
	throws Exception
	{
		
		locale = p_locale;
		translable = p_translable;
		segmentationRule = p_srx;
		whitespaceMatcher = whitespace.matcher(translable);
		closemarkMatcher = closemark.matcher(translable);
		openmarkMatcher = openmark.matcher(translable);
		isolatemarkMatcher = isolatemark.matcher(translable);
		
	}
	
	/**
	 * Set segmentation rule used to segment a text.
	 * @param segmentationRule
	 */
	public void setSegmentationRule(SegmentationRule p_segmentationRule) 
	{
		this.segmentationRule = p_segmentationRule;
	}
	
	/**
	 * Set the text to be segmented.
	 * @param translable
	 */
	public void setTranslable(String p_translable) 
	{
		translable = p_translable;
		whitespaceMatcher = whitespace.matcher(translable);
		closemarkMatcher = closemark.matcher(translable);
		openmarkMatcher = openmark.matcher(translable);
		isolatemarkMatcher = isolatemark.matcher(translable);
	}
	
	/**
	 * Set locale used by text to be segmented.
	 * @param locale
	 */
	public void setLocale(String locale) 
	{
		this.locale = locale;
	}
	
	/**
	 * This method is used to segment text, it produces breakIndex
	 * and nonBreakIndex for latter use, also return one 
	 * String[] contains result segments.
	 * @return
	 */
	public String[] doSegmentation()
	throws Exception
	{
		
		ArrayList rules = segmentationRule.getRulesByLocale(locale);
	    CATEGORY.debug("locale is :" + locale);
		if (rules.size() == 0)
		{
			 CATEGORY.info("There is no rule associated : " + locale);
		}
		ArrayList segments = new ArrayList();
		// These two strings are used to hold 
		// regular exprissions in a rule element.
		String before = null;
		String after = null;
		int endIndex;
		int startIndex;
		Rule rule;
		int length = translable.length();
		
		try
		{
			for (int i = 0; i < rules.size(); i++)
			{
				rule = (Rule)rules.get(i);
				before = rule.getBeforeBreak();
				after = rule.getAfterBreak();
				
				if (before != null) 
				{
					// before regular expression is not null
					
					if (after != null) 
					{
						// after regular expression is not null
						Matcher be = Pattern.compile(before).matcher(translable);
						Matcher af = Pattern.compile(after).matcher(translable);						
						// both before and after are not null, we will find
						// out break indexes and nonbreak indexes. there nonbreak 
						// index is the index produced according to a rule whose 
						// break is set to "no" in rule element.																													
						while (be.find()) 
						{
							endIndex = be.end();
							
							if (af.find(endIndex) && (af.start() == endIndex) ) 
							{	
								
								endIndex = handleWhiteSpace(endIndex);								
								saveIndex(rule, endIndex);
								
							}
													
						}																																				

					}// end if (after != null)

					else 
					{
						// after is null, but before is not, we find out both break indexes and 
						// nobreak indexes only accoring to the before regular expression. 
						Matcher be = Pattern.compile(before).matcher(translable);
						
						while (be.find())
						{
							
							endIndex = be.end();
							endIndex = handleWhiteSpace(endIndex);							
							saveIndex(rule, endIndex);
																																																									
						}
						
					}// end else

				}// end if (before != null)

				else 
				{
					// Now before is null.
					
					if (after != null) 
					{
						// after is not null, we will find out both break indexes and 
						// nobreak indexes only according to the after regular expression.
						Matcher af = Pattern.compile(after).matcher(translable);

						while (af.find())
						{
															
							startIndex = af.start();
							startIndex = handleWhiteSpace(startIndex);																					
							saveIndex(rule, startIndex);																																
							
						}

					}// end if (after != null)

					else 
					{
						// (before == null) && (after == null)
						// we do nothing
					}

			   	 }// end else
				
			}// end for (int i = 0; i < rules.size(); i++)					
		 }
		 catch (Exception e)
		 {
			 e.printStackTrace();
			 CATEGORY.error("There is a Exception while doing segmentation :" + e.getMessage());
			 throw new Exception(e.getMessage());
		 }
				
	    // Remove all the nonbreak index from the break index.
		 interBreak.removeAll(nonBreakIndex);
		 
		// Now handle format while checking break index.
		 Integer integer = null;
		 int newIndex = 0;
		 for (int i = 0; i < interBreak.size(); i++)
		 {
			 integer = (Integer)interBreak.get(i);
			 newIndex = handleFormat(integer.intValue());
			 integer = new Integer(newIndex);
			 if (!breakIndex.contains(integer))
			 {
				 breakIndex.add(integer);
			 }
			 
		 }
		 // Now clear the temp ArrayList
		 interBreak.clear();
		 
		 Integer last = new Integer(length);
		 if ( !breakIndex.contains(last) )
		 {
			 breakIndex.add(last);
		 }
		// Now we sort the breakIndex to get the correct segments 
		 Collections.sort(breakIndex);
		 
	    // After remove nonbreak index, if the size of breakIndex is 0,
	    // the text should not be segmented.
		 if (breakIndex.size() == 0)
		 {
			 CATEGORY.info("The size of breakIndex is 0 after sorting");
			 segments.add(translable);
		 }
		 
	    // If the size of breakIndex is not 0, we should 
		// break the text into segments for testing interface
		 else
		 {
			 int start = 0;
			 int end;
			 String segment = null;
			 
			 for (int k = 0; k < breakIndex.size(); k++)
			 {
				end = ((Integer)breakIndex.get(k)).intValue();					
				segment = translable.substring(start, end);	
				
				if (segment != null )
				{
					segments.add(segment);
				}
				
				start = end;				
			}			 
	     }
		 // Convert ArrayList to String[].
		  int resultSize = segments.size();
	       
	      while (resultSize > 0 && segments.get(resultSize-1).equals(""))
	                resultSize--;
	      String[] result = new String[resultSize];
	      
	      return (String[])segments.subList(0, resultSize).toArray(result);
	  
	}
    
	/**
	 * Handle formating marks ("< >", "</ >" and "< />")
	 * according to rule.
	 * @param p_rule
	 * @param p_index
	 * @return new break index.
	 */
	private int handleFormat(int p_index)
	{
		int resultIndex = p_index;
		SrxHeader header = segmentationRule.getHeader();
		HashMap format = header.getFormatHandle();
		String include = null;
		int endIndex;
		
		while (isolatemarkMatcher.find())
		{			
			// <  />
			endIndex = isolatemarkMatcher.end();
			if (endIndex == p_index)
			{
				// Breaking condition happens right after the matched isolated formatting mark.
				include = (String)format.get("isolated");
				
				if ( (include != null) && include.equalsIgnoreCase("no") )
				{
					resultIndex = isolatemarkMatcher.start();
					isolatemarkMatcher.reset();
					return resultIndex;
				}
			}
			else if (endIndex > p_index)
			{
				// Breaking condition happens before the matched isolated formatting mark.
				isolatemarkMatcher.reset();
				break;
			}
		}
	
	
		while (openmarkMatcher.find())
		{
			// <  >
			endIndex = openmarkMatcher.end();
			if (endIndex == p_index)
			{
				// Breaking condition happens right after the matched opening formatting mark.
				include = (String)format.get("start");
				
				if ( (include !=  null) && include.equalsIgnoreCase("no"))
				{
					resultIndex = openmarkMatcher.start();
					openmarkMatcher.reset();
					return resultIndex;
				}
			}
			else if (endIndex > p_index)
			{
				// Breaking condition happens before the matched opening formtting mark.
				openmarkMatcher.reset();
				break;
			}
			
		}
	
	
		while (closemarkMatcher.find())
		{
			//  </  >
			endIndex = closemarkMatcher.end();
			if (endIndex == p_index)
			{
				// Breaking condition happens right after the matched closing formatting mark.
				include = (String)format.get("end");
				
				if ( (include != null) && include.equalsIgnoreCase("no"))
				{
					resultIndex = closemarkMatcher.start();
					closemarkMatcher.reset();
					return resultIndex;
				}
			}
			else if (endIndex > p_index)
			{
				// Breaking condition happens before the matched closing formatting mark.
				closemarkMatcher.reset();
				break;
			}
			
		}
			
		return resultIndex;
	}
	
	/**
	 * If the following characters of p_index are whitespaces,
	 * the break index should at the first non whitespace character
	 * after whitespaces.
	 * @param p_index
	 * @return new break index
	 */
	private int handleWhiteSpace( int p_index)
	{
		int index = p_index;
		
		if (whitespaceMatcher == null)
		{
			return index;
		}
		
		else if ( whitespaceMatcher.find(p_index) && (whitespaceMatcher.start() == p_index) )
		{
			index = whitespaceMatcher.end();
		}
		
		return index;
	}
	
	/**
	 * Save index into breakIndex or nonBreakIndex accroding
	 * to rule.
	 * @param p_rule
	 * @param p_index
	 */
	private void saveIndex(Rule p_rule, int p_index)
	{
		Integer it = new Integer(p_index);
		// Now we determine the index is break index or not.
		if (p_rule.isBreak()) 
		{
			// Test if we meet a break rule more than
			// one time.
			if ((p_index != 0) && (!interBreak.contains(it))) 
			{
				interBreak.add(it);
			}

		} 
		else 
		{
			// Test if we meet a break rule exception more than one
			// time.
			if (!nonBreakIndex.contains(it)) 
			{
				nonBreakIndex.add(it);
			}
		}
	}
		
	
	/**
	 * Get the final break index ArrayList which has
	 * been removed the nonbreak index and sorted, this 
	 * method should be used after doSegmentation().
	 * @return
	 */
	public ArrayList getBreakIndex() 
	{
		return breakIndex;
	}

}
