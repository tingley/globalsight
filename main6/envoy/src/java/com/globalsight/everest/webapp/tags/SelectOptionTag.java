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

package com.globalsight.everest.webapp.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import java.util.ResourceBundle;

import com.globalsight.everest.webapp.pagehandler.PageHandler;

/**
 * This tag is a child tag of Select. It should be used inside the Select Tag.
 * It accepts three parameters : displayedText, value and selected.
 */
 
public class SelectOptionTag extends TagSupport {
    private String displayedText;
    private String value;
    private String selected;
    
    public SelectOptionTag() {
        super();
        displayedText = null;
        value         = null;
        selected      = null;
    }

    public String getDisplayedText() {
        return this.displayedText;
    }
    
    public void setDisplayedText(String displayedText) {
        this.displayedText = displayedText;
    }
    
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String getSelected() {
        return this.selected;
    }

	public void setSelected(String selected) {
        this.selected = selected;
    }

    
    public int doStartTag() throws JspException {
        SelectTag parent = 
            (SelectTag) findAncestorWithClass(this, SelectTag.class);

        if (parent == null) {
            throw 
                new AmbassadorTagException(pageContext, 
                        "A SelectOption tag has to be inside Select tag.");
        } else if (displayedText == null || value == null) {
            throw 
                new AmbassadorTagException(pageContext,
                        "In a SelectOption tag the displayedText " + 
                        "or value can't be null.");
        } else {
            ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
            String localizedText = null;
            try {
                localizedText = bundle.getString(displayedText);
            }
            catch (Exception e)
            {
                localizedText = displayedText;
            }
            SelectElement el = new SelectElement(value, localizedText);
        
            if (selected != null && selected.equalsIgnoreCase("true")) {
                el.setSelected(true);
            }
            parent.addElement(el);
        }
        return SKIP_BODY;
    }
    
    public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	public void release() {
        super.release();
        displayedText = null;
        value         = null;
        selected      = null;
    }
} 
