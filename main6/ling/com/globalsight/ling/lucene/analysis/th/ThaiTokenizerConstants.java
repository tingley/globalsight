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
package com.globalsight.ling.lucene.analysis.th;

public interface ThaiTokenizerConstants {

  int EOF = 0;
  int THAI = 1;
  int THAI_LETTER = 2;
  int ALPHANUM = 3;
  int APOSTROPHE = 4;
  int ACRONYM = 5;
  int COMPANY = 6;
  int EMAIL = 7;
  int HOST = 8;
  int NUM = 9;
  int P = 10;
  int HAS_DIGIT = 11;
  int ALPHA = 12;
  int LETTER = 13;
  int CJK = 14;
  int DIGIT = 15;
  int NOISE = 16;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "<THAI>",
    "<THAI_LETTER>",
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<HOST>",
    "<NUM>",
    "<P>",
    "<HAS_DIGIT>",
    "<ALPHA>",
    "<LETTER>",
    "<CJK>",
    "<DIGIT>",
    "<NOISE>",
  };

}
