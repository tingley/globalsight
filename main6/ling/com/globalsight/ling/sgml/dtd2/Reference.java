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
package com.globalsight.ling.sgml.dtd2;

/**
 * Class representing a reference to an ElementType.
 *
 * <p>Reference is used in the members of a Group.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class Reference extends Particle
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** The referred-to element type. */
   public ElementType elementType = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new Reference. */
   public Reference()
   {
      this.type = Particle.TYPE_ELEMENTTYPEREF;
   }

   /**
    * Construct a new Reference and set the element type.
    *
    * @param elementType The referenced element type.
    */
   public Reference(ElementType elementType)
   {
      this.type = Particle.TYPE_ELEMENTTYPEREF;
      this.elementType = elementType;
   }
}
