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
package com.globalsight.everest.page.pageimport.tag;

public class ItalicTag extends SpecialTag 
{

	@Override
	boolean check(String prefixTag, String removedPrefixTag) 
	{
		prefixTag = prefixTag.replace("&lt;w:i/&gt;", "");
		prefixTag = prefixTag.replace("&lt;w:rPr&gt;&lt;/w:rPr&gt;", "");
		removedPrefixTag = removedPrefixTag.replace("&lt;w:rPr&gt;&lt;/w:rPr&gt;", "");
		return prefixTag.equals(removedPrefixTag);
	}

	@Override
	String modifyTag(String bptTag, String all) 
	{
		 String newTag = bptTag.replaceAll(" innerTextNodeIndex=\"[^\"]*\"", "");
		 newTag = newTag.replaceAll(" erasable=\"[^\"]*\"", "");
		 
		 newTag = newTag.replaceAll(" type=\"[^\"]*\"", " type=\"office-italic\" erasable=\"yes\"");
		 
		 return all.replace(bptTag, newTag);
	}

	@Override
	public boolean hasSpecialTag(String gxml) 
	{
		return gxml.contains("&lt;w:i/&gt;");
	}

}
