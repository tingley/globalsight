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
 * Inline stack node.
 * <p>
 * Mosaic handles inlines via a separate stack from other elements We duplicate this to recover from inline markup
 * errors such as: &lt;i>italic text &lt;p> more italic text&lt;/b> normal text which for compatibility with Mosaic is
 * mapped to: &lt;i>italic text&lt;/i> &lt;p> &lt;i>more italic text&lt;/i> normal text Note that any inline end tag
 * pop's the effect of the current inline start tag, so that&lt;/b> pop's &lt;i>in the above example.
 * </p>
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class IStack
{

    /**
     * Next element in the stack.
     */
    protected IStack next;

    /**
     * tag's dictionary definition.
     */
    protected Dict tag;

    /**
     * name (null for text nodes).
     */
    protected String element;

    /**
     * Attributes.
     */
    protected AttVal attributes;

}
