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

public class Template {
  String path, name, tmplid, tmpltype, ext, table, mid, status;
  public Template(){}
  public Template(String a, String b, String c, String d, String e, String f, String g, String h){
    path= a;
    name= b;
    tmplid= c;
    tmpltype= d;
    ext= e;
    table= f;
    mid = g;
    status = h;
      
  }
  public String getTmplID(){return tmplid;}
  public String getPath(){return path;}
  public String getType(){return tmpltype;}
  public String getExt(){return ext;}
  public String getTable(){return table;}
  public String getMid(){return mid;}
  public String getStatus(){return status;}
  public String getName(){return name;}

}
