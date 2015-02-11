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
// This software is in the public domain.
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
// Changes from version 1.0: None
// Changes from version 1.01: None
package com.globalsight.ling.sgml.dtd2;

import java.util.*;

/**
 * Manages a list of tokens.
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 * @see InvertedTokenList
 */

public class TokenList
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Hashtable hash;
   private int       defaultToken = 0;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
    * Construct a TokenList without a default.
    *
    * @param names Token names.
    * @param tokens Token values.
    */
   public TokenList(String[] names, int[] tokens)
   {
      initTokens(names, tokens);
   }

   /**
    * Construct a TokenList with a default.
    *
    * @param names Token names.
    * @param tokens Token values.
    * @param defaultToken Default value (returned when a name is not found).
    */
   public TokenList(String[] names, int[] tokens, int defaultToken)
   {
      this.defaultToken = defaultToken;
      initTokens(names, tokens);
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Get the token value for a particular name, overriding the default value
    * if necessary.
    *
    * @param name Token name.
    * @param overrideDefault The temporary default value.
    * @return The token value or overrideDefault if the name is not found.
    */
   public int getToken(String name, int overrideDefault)
   {
      Integer i = (Integer) hash.get(name);
      return (i == null) ? overrideDefault : i.intValue();
   }

   /**
    * Get the token value for a particular name.
    *
    * @param name Token name.
    * @return The token value or the list default if the name is not found.
    *  If no list default has been set, 0 is returned.
    */
   public int getToken(String name)
   {
      return getToken(name, defaultToken);
   }

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private void initTokens(String[] names, int[] tokens)
   {
      hash = new Hashtable(names.length);

      for (int i = 0; i < names.length; i++)
      {
         hash.put(names[i], new Integer(tokens[i]));
      }
   }

/*
   private void initTokens(String uri, String[] names, int[] tokens)
   {
      Hashtable hash = new Hashtable(names.length);

      for (int i = 0; i < names.length; i++)
      {
         hash.put(uri + XMLName.SEPARATOR + names[i], new Integer(tokens[i]));
      }
   }
*/

}

