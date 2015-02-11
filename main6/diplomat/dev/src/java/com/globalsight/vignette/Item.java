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

package com.globalsight.vignette;

public class Item implements Comparable {
  String path, mid, status;
  public Item(){}
  public Item(String p_mid, String p_path, String p_status){
    mid = p_mid;
    path = p_path;
    status = p_status;
  }
  public String getPath(){return path;}
  public String getMid(){return mid;}
  public String getStatus(){return status;}

  public int compareTo(Object p_o)
  {
      Item o = (Item)p_o;
      return mid.compareTo(o.mid);
  }

  public boolean equals(Object p_o)
  {
      if (compareTo(p_o)==0)
	  return true;
      else
	  return false;
  }
}
