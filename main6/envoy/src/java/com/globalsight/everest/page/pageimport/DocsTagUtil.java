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
package com.globalsight.everest.page.pageimport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.page.pageimport.tag.BoldTag;
import com.globalsight.everest.page.pageimport.tag.ColorTag;
import com.globalsight.everest.page.pageimport.tag.HyperlinkTag;
import com.globalsight.everest.page.pageimport.tag.ItalicTag;
import com.globalsight.everest.page.pageimport.tag.SpecialTag;
import com.globalsight.everest.page.pageimport.tag.SuperScriptTag;
import com.globalsight.everest.page.pageimport.tag.UnderlineTag;
import com.globalsight.everest.tuv.RemovedTag;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;

public class DocsTagUtil 
{
	private static String REGEX_BPT = "(<bpt[^>]*>)([^>]*)</bpt>[^<]*<ept[^>]*>([^>]*)</ept>";
    
	private static String PRESERVE = " xml:space=&quot;preserve&quot;";
    private static String RSIDRPR_REGEX = " w:rsidRPr=&quot;[^&]*&quot;";
    private static String RSIDR_REGEX = " w:rsidR=&quot;[^&]*&quot;";
    
    private static List<SpecialTag> SPECIAL_TAGS = new ArrayList<SpecialTag>();
    static
    {
    	SPECIAL_TAGS.add(new SuperScriptTag());
    	SPECIAL_TAGS.add(new HyperlinkTag());
    	SPECIAL_TAGS.add(new BoldTag());
    	SPECIAL_TAGS.add(new ColorTag());
    	SPECIAL_TAGS.add(new ItalicTag());
    	SPECIAL_TAGS.add(new UnderlineTag());
//    	SPECIAL_TAGS.add(new OtherTag());
    }

    public static  boolean hasSpecialTag(String gxml)
    {
    	for (SpecialTag tag : SPECIAL_TAGS)
    	{
    		if (tag.hasSpecialTag(gxml))
    			return true;
    	}
    	
    	return false;
    }
	
	public static TuvImpl handleSpecialTag(TuvImpl tuv, long p_jobId)
	{
		TuImpl tu = (TuImpl) tuv.getTu(p_jobId);

		if (tu.hasRemovedTags()) 
		{
			RemovedTag tag = tu.getRemovedTag();
			
			String gxml = tuv.getGxml();
			
			String removedPrefixTag = removeUnusedTag(tag.getPrefixString());
			String removesuffixTag = tag.getSuffixString();

	        Pattern p = Pattern.compile(REGEX_BPT);
	        Matcher m = p.matcher(gxml);
	        while (m.find())
	        {
	             String all = m.group();
	             String bptTag = m.group(1);
	             String bpt = m.group(2);
	             String ept = m.group(3);
	             
	             if (gxml.indexOf("</bpt>" + all) > -1)
	            	 continue;
	             
	             ept = ept.replaceAll("&lt;/w:hyperlink&gt;$", "");
	             
				if (ept.equals(removesuffixTag)) 
				{
					String prefixTag = removeUnusedTag(bpt);
					for (SpecialTag t : SPECIAL_TAGS) 
					{
						String newAll = t.handle(prefixTag, removedPrefixTag,
								all, bptTag);
						if (newAll != null)
						{
							 gxml = gxml.replace(all, newAll);
							 break;
						}
					}
				}
	        }
	        
	        tuv.setGxml(gxml);
		}
		
		return tuv;
	}
	
	private static String removeUnusedTag(String gxml)
	{
		gxml = gxml.replaceAll(RSIDRPR_REGEX, "");
		gxml = gxml.replaceAll(RSIDR_REGEX, "");
		gxml = gxml.replaceAll(PRESERVE, "");
		
		return gxml;
	}
}
