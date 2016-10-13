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
package com.globalsight.ling.jtidy;

/**
 * HTML attribute.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class Attribute
{

    /**
     * attribute name.
     */
    private String name;

    /**
     * don't wrap attribute.
     */
    private boolean nowrap;

    /**
     * unmodifiable attribute?
     */
    private boolean literal;

    /**
     * html versions for this attribute.
     */
    private short versions;

    /**
     * checker for the attribute.
     */
    private AttrCheck attrchk;

    /**
     * Instantiates a new Attribute.
     * @param attributeName attribute name
     * @param htmlVersions versions in which this attribute is supported
     * @param check AttrCheck instance
     */
    public Attribute(String attributeName, short htmlVersions, AttrCheck check)
    {
        this.name = attributeName;
        this.versions = htmlVersions;
        this.attrchk = check;
    }

    /**
     * Is this a literal (unmodifiable) attribute?
     * @param isLiteral boolean <code>true</code> for a literal attribute
     */
    public void setLiteral(boolean isLiteral)
    {
        this.literal = isLiteral;
    }

    /**
     * Don't wrap this attribute?
     * @param isNowrap boolean <code>true</code>= don't wrap
     */
    public void setNowrap(boolean isNowrap)
    {
        this.nowrap = isNowrap;
    }

    /**
     * Returns the checker for this attribute.
     * @return instance of AttrCheck.
     */
    public AttrCheck getAttrchk()
    {
        return this.attrchk;
    }

    /**
     * Is this a literal (unmodifiable) attribute?
     * @return <code>true</code> for a literal attribute
     */
    public boolean isLiteral()
    {
        return this.literal;
    }

    /**
     * Returns the attribute name.
     * @return attribute name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Don't wrap this attribute?
     * @return <code>true</code>= don't wrap
     */
    public boolean isNowrap()
    {
        return this.nowrap;
    }

    /**
     * Returns the html versions in which this attribute is supported.
     * @return html versions for this attribute.
     * @see Dict
     */
    public short getVersions()
    {
        return this.versions;
    }

}
