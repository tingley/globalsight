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
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This tag writes out an html select tag
 */
public class SelectTag extends InputTag {

    protected int     blanks;       // number of blank spaces for the first option
                                                    // in the list box
    protected List    elements;     // storage for the list elements
    protected boolean multiple;     // whether multiple options can be selected
    protected String  showOnly;     // a common separated of keys to display
    protected String  onChange;     // name of javascript onchange function
    protected int     size;         // number of options viewable at one time

    public SelectTag() {
        super();
        init();
    }

    private void init() {
        blanks    = 0;
        elements  = new Vector();
        multiple  = false;
        showOnly  = null;
        onChange  = null;
        size      = 1;
    }

/*
 * getters and setters
 */
    public int getBlanks()
    {
        return blanks;
    }

    public void setBlanks(int blanks)
    {
        this.blanks = blanks;
    }

    protected List getElements()
    {
        return elements;
    }

    protected void setElements(List elements)
    {
        this.elements = elements;
    }

    public boolean isMultiple()
    {
        return multiple;
    }

    public void setMultiple(boolean multiple)
    {
        this.multiple = multiple;
    }

    public String getShowOnly()
    {
        return showOnly;
    }

    public void setShowOnly(String showOnly)
    {
        this.showOnly = showOnly;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public void setOnChange(String onChange)
    {
        this.onChange = onChange;
    }

    public String getOnChange()
    {
        return onChange;
    }

    protected boolean addElement(Object o)
    {
        if (o == null || elements == null)
        {
            return false;
        }
        return elements.add(o);
    }

    public int doStartTag() throws JspException
    {
        init();
        
        super.doStartTag();

        return EVAL_BODY_AGAIN;
    }

    public int doEndTag() throws JspException
    {
        super.doEndTag();
        return EVAL_PAGE;
    }


    public void printLocked()
    throws JspException
    {
        JspWriter   out  = pageContext.getOut();

        String selList = value;
        if (selList != null)
        {
            selList = selList.trim();
            if(selList.length() == 0)
            {
                selList = null;
            }
        }

        //Set up the selection list
        List selElements = null;
        if (selList!= null)
        {
            selElements  = new Vector();
            StringTokenizer st = new StringTokenizer(selList, ",");
            while (st.hasMoreTokens()) {
                String key = st.nextToken();
                selElements.add(key.trim());
            }
        }
        try 
        {
            ListIterator iter = elements.listIterator();
            while (iter.hasNext())
            {
                Object el = iter.next();

                if (el instanceof SelectElement)
                {
                    //override the body selections with the default selections
                    if (selElements == null) {
                        if (((SelectElement) el).isSelected() == true)
                        {
                            out.print(((SelectElement) el).getValue() + " " );
                        }
                    }
                    else if (isSelected(selElements, ((SelectElement) el).getKey()) == true)
                    {
                        out.print(((SelectElement) el).getValue() + " " );
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    public void printShared()
    throws JspException
    {
        JspWriter   out  = pageContext.getOut();

        String selList = value;
        if (selList != null)
        {
            selList = selList.trim();
            if(selList.length() == 0){
                selList = null;
            }
        }

        //Set up the selection list
        List selElements = null;
        if (selList!= null)
        {
            selElements  = new Vector();
            StringTokenizer st = new StringTokenizer(selList, ",");
            while (st.hasMoreTokens()) {
                String key = st.nextToken();
                selElements.add(key.trim());
            }
        }

        // write the actual HTML
        write(out, name, 
                  size, multiple, elements, blanks, showOnly, selElements);
    }

    public void release() {
        super.release();
        init();
    }

    public static boolean isSelected(List selectedList, String value) throws Exception{
        if (selectedList != null ){

            ListIterator iter = selectedList.listIterator();

            while(iter.hasNext()) {

                Object el = iter.next();

                if (el instanceof String) {
                    if( ((String)el).equalsIgnoreCase(value)){
                        return true;
                    }
                } else {
                    throw new Exception("Selected list should contain only String object types");
                }
            }
        }
        return false;
    }

  /**
   * Writes the HTML code for displaying the Select Widget
   *
   * @param out The writer used to output to the browser
   * @param name Name of the Select object
   * @param size  Number of viewable elements in the Select
   * @param multiple  Whether multiple options can be selected
   * @param elements  List representation of the Select elements
   * @param blanks  Number of blank spaces for the first option
   * @param showOnly  String of comma separated keys; Only these
   *                                 keys are displayed in the Select
   * @param selElements  List representation of the elements being
                                         selected
   * @exception JspException thrown on JSP Exception
   */
    public  void write(JspWriter out,
                             String    name,
                             int       size,
                             boolean   multiple,
                             List      elements,
                             int       blanks,
                             String    showOnly,
                             List      selElements)
            throws JspException {
            ListIterator iter = null;

            try {
                // SELECT
                out.print("<select ");
                out.print("name='" + name + "'");
                if (style != null)
                    out.print(" style='" + style + "'");
                if (styleClass != null)
                    out.print(" class='" + styleClass + "'");
                if (size > 0) {
                    out.print(" size='" + size + "'");
                } else {
                    out.print(" size=1 ");
                }
                if (multiple == true && size > 1) {
                    out.print(" multiple ");
                }
                if (disabled)
                    out.print(" disabled='" + disabled + "'");

                // events
                if (onChange != null)
                    out.print(" onChange=\"" + onChange + "\""); 

                out.println(">");

                // Blank entry?
                if (blanks > 0) {
                    String bStr = "";

                    for (int i = 0; i < blanks; i++) {
                        bStr += "&nbsp;";
                    }
                    out.print(" <option");
                    out.print(" value=\"\" ");
                    out.print(">" + bStr);
                    out.println("</option>");
                }

                // obtain the list iterator
                if (showOnly != null && !showOnly.equals("")) {
                    // show only the elements referenced in showOnly
                    List showElements  = new Vector();
                    StringTokenizer st = new StringTokenizer(showOnly, ",");

                    // loop through the keys to show
                    while (st.hasMoreTokens()) {
                        String key = st.nextToken();
                        ListIterator i = elements.listIterator();

                        key = key.trim();
                        while(i.hasNext()) {
                            Object el = i.next();
                            if (el instanceof SelectElement) {
                                if (((SelectElement) el).getKey().equalsIgnoreCase(key)) {
                                    showElements.add(el);
                                    break;
                                }
                            }
                        }
                    }
                    iter = showElements.listIterator();
                } else {
                    // show all the elements
                    iter = elements.listIterator();
                }
                while(iter.hasNext()) {
                    Object el = iter.next();

                    // OPTION
                    if (el != null) {
                        out.print(" <option ");
                        if (el instanceof SelectElement) {
                            out.print("value=\"" + ((SelectElement) el).getKey() + "\" ");

                            //override the body selections with the default selections
                            if( selElements == null) {
                                if (((SelectElement) el).isSelected() == true) {
                                    out.print("selected ");
                                }
                            } else if (isSelected(selElements, ((SelectElement) el).getKey()) == true) {
                                out.print("selected ");
                            }

                            out.println(">" + ((SelectElement) el).getValue() + "</OPTION>");
                        }
                    }
                }
                out.println("</select>");
            } catch (IOException e) {
                throw new JspTagException("I/O exception " + e.getMessage());
            } catch (Exception e) {
                throw new JspTagException("List box exception " + e.getMessage());
            }
    }

  /**
   * Overloaded write() method for Select with no selected elements
   *
   * @param out The writer used to output to the browser
   * @param widgetName Name of the Select widget
   * @param size  Number of viewable elements in the Select
   * @param multiple  Whether multiple options can be selected
   * @param elements  List representation of the Select elements
   * @param blanks  Number of blank spaces for the first option
   * @param showOnly  String of comma separated keys; Only these
   *                                 keys are displayed in the Select
   * @param locale Locale of the client user
   * @exception JspException thrown on JSP Exception
   */
    public void write(JspWriter out,
                             String    widgetName,
                             int       size,
                             boolean   multiple,
                             List      elements,
                             int       blanks,
                             String    showOnly)
        throws JspException {
        //call the write function with no selected items
        write(out, widgetName, 
                size, multiple, elements, blanks, showOnly, null);
    }

}
