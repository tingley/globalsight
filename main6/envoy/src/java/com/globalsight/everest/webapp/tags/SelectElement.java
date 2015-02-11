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

/**
 * Storage for the elements of a SelectTag. SelectOptionTag will create and
 * store in the elements list on SelectTag.
 */
public class SelectElement
{
    private String  key;
    private String  value;
    private boolean selected;
    
    public SelectElement() {
        this(null, null, false);
    }

    public SelectElement(String key, String value) {
        this(key, value, false);
    }

    public SelectElement(String key, String value, boolean selected) {
        this.key      = key;
        this.value    = value;
        this.selected = selected;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        return;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        return;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        return;
    }
}
