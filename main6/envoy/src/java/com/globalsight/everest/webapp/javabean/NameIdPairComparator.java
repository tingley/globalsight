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
package com.globalsight.everest.webapp.javabean;
import java.io.Serializable;
import java.util.Comparator;

/**
 * can be used to compare NameIdPair objects
 */
public class NameIdPairComparator implements Serializable, Comparator
{
    /**
    * Compares two NameIdPair objects based on their name
    */
    public int compare(Object o1, Object o2) {
	if (o1 instanceof NameIdPair && o2 instanceof NameIdPair)
	{
	    NameIdPair n1 = (NameIdPair) o1;
	    NameIdPair n2 = (NameIdPair) o2;
	    return n1.getName().compareTo(n2.getName());
	}
	else throw new ClassCastException("NameIdPairComparator works only on NameIdPair objects.");

    }
    public boolean equals(Object obj) {
	if (obj instanceof NameIdPairComparator)
	    return true;
	else
	    return false;
    }
}

