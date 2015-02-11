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
package com.globalsight.everest.persistence;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A simple comparator for comparing PersistentObject objects by their names.
 */
public class PersistentObjectNameComparator implements Comparator, Serializable
{
    public int compare(Object o1, Object o2) {
	if (o1 instanceof PersistentObject && o2 instanceof PersistentObject)
	{
	    PersistentObject p1 = (PersistentObject) o1;
	    PersistentObject p2 = (PersistentObject) o2;
	    return p1.getName().compareTo(p2.getName());
	}
	else throw new ClassCastException("can only compare PersistentObject");
    }

    public boolean equals(Object obj) {
	if (obj instanceof PersistentObjectNameComparator)
	    return true;
	else
	    return false;
    }
}

