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
// This software is in the public static domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/
// Version 2.0
// Changes from version 1.x:
package com.globalsight.ling.sgml.dtd2
;

/**
 * DTD constants.
 *
 * <p>These are used by DTDParser and DTDSerializer.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DTDConst
{
   // *********************************************************************
   // DTD Strings
   // *********************************************************************

   public static String KEYWD_ANY      = "ANY";
   public static String KEYWD_ATTLIST  = "ATTLIST";
   public static String KEYWD_CDATA    = "CDATA";
   public static String KEYWD_ELEMENT  = "ELEMENT";
   public static String KEYWD_EMPTY    = "EMPTY";
   public static String KEYWD_ENTITY   = "ENTITY";
   public static String KEYWD_ENTITIES = "ENTITIES";
   public static String KEYWD_FIXED    = "FIXED";
   public static String KEYWD_ID       = "ID";
   public static String KEYWD_IDREF    = "IDREF";
   public static String KEYWD_IDREFS   = "IDREFS";
   public static String KEYWD_IMPLIED  = "IMPLIED";
   public static String KEYWD_NDATA    = "NDATA";
   public static String KEYWD_NMTOKEN  = "NMTOKEN";
   public static String KEYWD_NMTOKENS = "NMTOKENS";
   public static String KEYWD_NOTATION = "NOTATION";
   public static String KEYWD_PCDATA   = "PCDATA";
   public static String KEYWD_PUBLIC   = "PUBLIC";
   public static String KEYWD_REQUIRED = "REQUIRED";
   public static String KEYWD_SYSTEM   = "SYSTEM";

   public static final String[] KEYWDS = {KEYWD_ANY,
                                          KEYWD_ATTLIST,
                                          KEYWD_CDATA,
                                          KEYWD_ELEMENT,
                                          KEYWD_EMPTY,
                                          KEYWD_ENTITY,
                                          KEYWD_ENTITIES,
                                          KEYWD_FIXED,
                                          KEYWD_ID,
                                          KEYWD_IDREF,
                                          KEYWD_IDREFS,
                                          KEYWD_IMPLIED,
                                          KEYWD_NDATA,
                                          KEYWD_NMTOKEN,
                                          KEYWD_NMTOKENS,
                                          KEYWD_NOTATION,
                                          KEYWD_PCDATA,
                                          KEYWD_PUBLIC,
                                          KEYWD_REQUIRED,
                                          KEYWD_SYSTEM
                                         };

   // *********************************************************************
   // DTD Tokens
   // *********************************************************************

   public static final int KEYWD_TOKEN_UNKNOWN  = 0;
   public static final int KEYWD_TOKEN_ANY      = 1;
   public static final int KEYWD_TOKEN_ATTLIST  = 2;
   public static final int KEYWD_TOKEN_CDATA    = 3;
   public static final int KEYWD_TOKEN_ELEMENT  = 4;
   public static final int KEYWD_TOKEN_EMPTY    = 5;
   public static final int KEYWD_TOKEN_ENTITY   = 6;
   public static final int KEYWD_TOKEN_ENTITIES = 7;
   public static final int KEYWD_TOKEN_FIXED    = 8;
   public static final int KEYWD_TOKEN_ID       = 9;
   public static final int KEYWD_TOKEN_IDREF    = 10;
   public static final int KEYWD_TOKEN_IDREFS   = 11;
   public static final int KEYWD_TOKEN_IMPLIED  = 12;
   public static final int KEYWD_TOKEN_NDATA    = 13;
   public static final int KEYWD_TOKEN_NMTOKEN  = 14;
   public static final int KEYWD_TOKEN_NMTOKENS = 15;
   public static final int KEYWD_TOKEN_NOTATION = 16;
   public static final int KEYWD_TOKEN_PCDATA   = 17;
   public static final int KEYWD_TOKEN_PUBLIC   = 18;
   public static final int KEYWD_TOKEN_REQUIRED = 19;
   public static final int KEYWD_TOKEN_SYSTEM   = 20;

   public static final int[] KEYWD_TOKENS = {KEYWD_TOKEN_ANY,
                                             KEYWD_TOKEN_ATTLIST,
                                             KEYWD_TOKEN_CDATA,
                                             KEYWD_TOKEN_ELEMENT,
                                             KEYWD_TOKEN_EMPTY,
                                             KEYWD_TOKEN_ENTITY,
                                             KEYWD_TOKEN_ENTITIES,
                                             KEYWD_TOKEN_FIXED,
                                             KEYWD_TOKEN_ID,
                                             KEYWD_TOKEN_IDREF,
                                             KEYWD_TOKEN_IDREFS,
                                             KEYWD_TOKEN_IMPLIED,
                                             KEYWD_TOKEN_NDATA,
                                             KEYWD_TOKEN_NMTOKEN,
                                             KEYWD_TOKEN_NMTOKENS,
                                             KEYWD_TOKEN_NOTATION,
                                             KEYWD_TOKEN_PCDATA,
                                             KEYWD_TOKEN_PUBLIC,
                                             KEYWD_TOKEN_REQUIRED,
                                             KEYWD_TOKEN_SYSTEM
                                            };
}

