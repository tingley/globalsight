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

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A style class extends Italic style
 */
public class Comment extends Style
{

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getNodeName()
     */
    @Override
    protected String getNodeName()
    {
        return "comment";
    }

    /**
     * @see com.globalsight.everest.page.pageexport.style.docx.Style#getAddNodeName()
     */
    @Override
    public String getAddNodeName()
    {
        return "w:commentRange";
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
		return "comment";
	}
	
	@Override
	protected void updateStyle(Node cNode, Node cloneNode, Node wtNode,
			Node wrNode, Node root)
    {
		changeText(cloneNode, wtNode.getNodeName(), cNode);

        if (cNode.getNodeName().equals(getNodeName()))
        {
        	Element start = wrNode.getOwnerDocument().createElement("w:commentRangeStart");
        	Node id = getAttribute(cNode, "w:id");
        	if (id != null)
        		start.setAttribute("w:id", id.getNodeValue());
        	root.insertBefore(start, wrNode);
        	
        	Node c = getNode(cloneNode, "commentContent");
        	if (c != null)
        	{
        		c.getParentNode().removeChild(c);
        	}
        	
        	root.insertBefore(cloneNode, wrNode);
        	
        	Element end = wrNode.getOwnerDocument().createElement("w:commentRangeEnd");
        	if (id != null)
        		end.setAttribute("w:id", id.getNodeValue());
        	root.insertBefore(end, wrNode);
        	
        	Node last = cNode.getLastChild();
        	if (last != null && "commentContent".equals(last.getNodeName()))
        	{
        		Element commentContent = wrNode.getOwnerDocument().createElement("w:r");
        		last.getParentNode().removeChild(last);
        		commentContent.appendChild(last);
        		removeNode(last);
				
				root.insertBefore(commentContent, wrNode);
        	}
        }
        else
        {
        	root.insertBefore(cloneNode, wrNode);
        }
	}
}
