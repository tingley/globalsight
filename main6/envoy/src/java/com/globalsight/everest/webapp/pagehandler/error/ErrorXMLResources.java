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

package com.globalsight.everest.webapp.pagehandler.error;

// Envoy imports
//import com.globalsight.everest.webapp.AbstractXMLResourceWrapper;
//import com.globalsight.everest.webapp.XMLResourceBundle;

public class ErrorXMLResources /* extends AbstractXMLResourceWrapper */
{
  // the name of the xml bundle classk
  static public final String XML_RESOURCE_BUNDLE_NAME = "/XMLResources/errorBundle.xml";

  // the XML resource bundle that contains all the localized strings associated to this class
//  static private XMLResourceBundle m_XMLResourceBundle = null;

  // singleton instance of this class
  static private ErrorXMLResources m_instance = null;

  /**
   * Accessor for the singleton instance of this class
   *
   * @return the unique instance of this class
   */
  static public ErrorXMLResources getInstance()
  {
    if(m_instance == null)
      m_instance = new ErrorXMLResources();
    return m_instance;
  }

  /**
   * Accessor for the XML resource bundle
   *
   * @return the XMLResourceBundle owned by this class

  protected XMLResourceBundle getXMLResourceBundle()
  {
    return m_XMLResourceBundle;
  }
   */

  /**
   * Settor for the XMLResourceBundle
   *
   * @param p_theXMLResourceBundle an XML resource bundle
  protected void setXMLResourceBundle(XMLResourceBundle p_theXMLResourceBundle)
  {
    m_XMLResourceBundle = p_theXMLResourceBundle;
  }
   */

  /**
   * Accessor for the name of the XML resource bundle
   *
   * @return the name of the XML resource bundle
   */
  protected String getXMLResourceBundleName()
  {
    return XML_RESOURCE_BUNDLE_NAME;
  }
}
