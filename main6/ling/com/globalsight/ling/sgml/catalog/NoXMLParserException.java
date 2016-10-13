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
// NoXMLParserException.java - Attempt to parse XML Catalog with no Parser
// Written by Norman Walsh, nwalsh@arbortext.com
// NO WARRANTY! This class is in the public domain.
package com.globalsight.ling.sgml.catalog;

/**
 * <p>Signal attempt to parse an XML Catalog without a Parser class.</p>
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * </blockquote>
 *
 * <p>This exception is thrown if an attempt is made to load an XML
 * Catalog, but no Parser class has been provided.</p>
 *
 * @see XMLCatalogReader
 * @see XMLCatalogReader#setParserClass
 */
public class NoXMLParserException
    extends Exception
{
    public NoXMLParserException()
    {
        super();
    }
}
