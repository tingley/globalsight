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
package com.globalsight.cxe.adapter.documentum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>FileAttr</code> class describes the a attribute of DCTM document object, 
 * including name, datatype, isRepeating, a set of values.
 * 
 */
public class FileAttr {

    private String name = null;
    private int datatype = -1;
    private boolean repeating = false;
    private List values = null;
    
    public FileAttr() {
        values = new ArrayList();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public int getDatatype() {
        return datatype;
    }
    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }
    
    public boolean isRepeating() {
        return repeating;
    }
    public void setRepeating(boolean isRepeating) {
        this.repeating = isRepeating;
    }

    public List getValues() {
        return values;
    }
    public void addValue(Object value) {
        values.add(value);
    }
    
    public String toString() {
        StringBuffer fileAttrs = new StringBuffer();
        fileAttrs.append("<fileattr name=\"").append(name).append("\" ");
        fileAttrs.append("datatype=\"").append(datatype).append("\" ");
        fileAttrs.append("isRepeating=\"").append(repeating).append("\">\r\n");
        Iterator valueIter = values.iterator();
        while(valueIter.hasNext()) {
            fileAttrs.append("\t<value>").append(valueIter.next().toString())
                     .append("</value>\r\n");
        }
        fileAttrs.append("</fileattr>");
        return fileAttrs.toString();
    }
    
}
