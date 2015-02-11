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

package com.globalsight.diplomat.javabeans;

import java.util.Set;
import java.util.TreeMap;

public class SortedNameValuePairsBean
{
  private TreeMap nameValuePairs = new TreeMap();

  public SortedNameValuePairsBean()
  {
  }

  public void addKeyValuePair(Comparable aKey, String aValue)
  {
    nameValuePairs.put(aKey, aValue);
  }

  public String getValue(String aKey)
  {
    return (String) nameValuePairs.get(aKey);
  }

  public Set keySet()
  {
    return nameValuePairs.keySet();
  }

/*  public void test()
  {
    Set s = this.keys();
    Iterator iter = s.iterator();
    while(iter.hasNext())
    {
      String key = (String) iter.next();
      String value = (String) jobTemplateOptions.get(key);
    }
  }
*/
}

