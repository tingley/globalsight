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
package com.globalsight.everest.page.pageexport.style.docx;

import org.w3c.dom.Node;


/**
 * A style class extends Italic style
 */
public class Hyperlink extends Style
{

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getNodeName()
     */
    @Override
    protected String getNodeName()
    {
        return "w:hyperlink";
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getAddNodeName()
     */
    @Override
    public String getAddNodeName()
    {
        return "w:hyperlink";
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getAddNodeValue()
     */
    @Override
    protected String getAddNodeValue()
    {
        return null;
    }
    
	@Override
	public boolean hasAttribute() 
	{
		return true;
	}
    
	@Override
	public String getStyle() 
	{
		return "hyperlink";
	}
	
	@Override
	protected void updateStyle(Node cNode, Node cloneNode, Node wtNode,
			Node wrNode, Node root)
    {
		changeText(cloneNode, wtNode.getNodeName(), cNode);

        if (cNode.getNodeName().equals(getNodeName()))
        {

        	Node hyplink = cNode.cloneNode(false);
        	hyplink.appendChild(cloneNode);
        	root.insertBefore(hyplink, wrNode);
            // Style node can be nested.
        }
        else
        {
        	root.insertBefore(cloneNode, wrNode);
        }
	}
}
