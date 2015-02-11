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
package com.globalsight.diplomat.util.previewUrlXml;

/**
 * UrlElement
 * <p>
 * The UrlElement class corresponds to the <em>url</em> element in
 * the Preview Url Xml. A url has a label, an href, and an array of
 * arg elements. A url also has a type which is either "get" or "post".
 */
public class UrlElement
{
	//private members
	private ArgElement[] m_args;	 //URL arguments
	private String m_type;		//POST or GET
	private String m_href;		//the actual url
	private String m_label;			   //the URL label

	/**
	 * Returns a UrlElement object corresponding to a <em>url</em>
	 * element in a Preview Url Xml file.
	 * 
	 * @param the url href
	 * @param the type of the URL (POST or GET)
	 * @param the label of the URL
	 * @param an array of arg elements
	 */
	public UrlElement(String p_href, String p_type, String p_label, ArgElement[] p_args)
	{
		m_href = p_href;
                
                if (p_type == null)
                    m_type = "get";
                else
                    m_type = p_type.toLowerCase();
		
                m_label = p_label;
		m_args = p_args;
	}

	public UrlElement()
	{
	}

	/** @return the URL*/
	public String getHref()
	{
		return m_href;
	}

	/** @return the label*/
	public String getLabel()
	{
		return m_label;
	}

	/** @return the type*/
	public String getType()
	{
		return m_type;
	}

	/** @return the number of URL args*/
	public int getNumArgs()
	{
		return m_args.length;
	}

	/** Returns the ith argument to the URL.
	* @param the index into the URL args
	* @return an ArgElement
	* @throws ArrayIndexOutOfBoundsException
	*/
	public ArgElement getArg(int p_i)
	throws ArrayIndexOutOfBoundsException
	{
		return m_args[p_i];
	}
}

