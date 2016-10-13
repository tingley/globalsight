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
package com.globalsight.everest.persistence.page;

/**
 * PageDescriptorModifier is an abstract extension of DescriptorModifier that
 * provides common methods needed by the Source & Target pages
 */
public abstract class PageDescriptorModifier
{
    protected static final String ID_ARG = "pageId";
    protected static final String STATE_ARG = "state";
    protected static final String LOCALE_ARG = "locale";
    protected static final String LOCALE_ID_ARG = "localeId";
    protected static final String EXTERNAL_ID_ARG = "externalPageId";
    protected static final String LEVERAGE_GROUP_LIST = "m_leverageGroupList";
}