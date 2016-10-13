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
 * ArgElement
 * <p>
 * The ArgElement class corresponds to the <em>arg</em> element in
 * the Preview Url Xml. One arg has exactly one parameter and possibly
 * one value. Both the parameter and value can be substitutable (this
 * refers to the substitutable attribute of the parameter and value
 * elements in the DTD.
 */
public class ArgElement
{
    //private members
    private String m_parameter = null;
    private String m_value = null;
    private boolean m_parameterIsSubstitutable = false;
    private boolean m_valueIsSubstitutable = false;
    private String m_parSubstitutionSource; // "source", "target", "none"
    private String m_valSubstitutionSource;

    /**
     * Returns an ArgElement object corresponding to a <em>arg</em>
     * element in a Preview Url Xml file.
     * 
     * @param the parameter of the arg
     * @param whether the parameter is substitutable
     * @param the value of the parameter
     * @param whether the value is substitutable
     */
     public ArgElement(String p_parameter, String p_parSubstitutionSource,
		       String p_value, String p_valSubstitutionSource)
     {
	 m_parameter = p_parameter;
	 m_value = p_value;
	 m_parSubstitutionSource = p_parSubstitutionSource;
	 m_valSubstitutionSource = p_valSubstitutionSource;

	 if (m_parSubstitutionSource == null || ("none".equals(m_parSubstitutionSource)))
	     m_parameterIsSubstitutable = false; //not substitutable
	 else
	     m_parameterIsSubstitutable = true;

	 if (m_valSubstitutionSource == null || ("none".equals(m_valSubstitutionSource)))
	     m_valueIsSubstitutable = false;
	 else
	     m_valueIsSubstitutable = true;
     }

     public ArgElement(){}

     /** @return If the parameter is substitutable.*/
     public boolean isParameterSubstitutable()
     {
	 return m_parameterIsSubstitutable;
     }

     /** @return If the value is substitutable.*/
     public boolean isValueSubstitutable()
     {
	 return m_valueIsSubstitutable;
     }
     
     /** @return the parameter*/
     public String getParameter()
     {
	 return m_parameter;
     }

     /** @return the value*/
     public String getValue()
     {
	 return m_value;
     }

     /** @return the parameter's substitution source*/
     public String getParameterSubstitutionSource()
     {
	 return m_parSubstitutionSource;
     }

     /** @return the value's substitution source*/
     public String getValueSubstitutionSource()
     {
	 return m_valSubstitutionSource; 
     }

}

