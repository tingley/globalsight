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
// Changes from version 1.x: New in version 2.0
package com.globalsight.ling.sgml.dtd2;

/**
 * <p>This class can encapsulate another Exception. The code
 * is largely copied from SAXException, by David Megginson.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class DTDException 
	extends Exception
{

   // ********************************************************************
   // Variables
   // ********************************************************************

    private Exception exception;

    // ********************************************************************
    // Constructors
    // ********************************************************************

   /**
     * Create a new DTDException.
     *
     * @param message The error or warning message.
     */
   public DTDException(String message)
   {
      super(message);
      this.exception = null;
   }

   /**
     * Create a new DTDException wrapping an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, and its message will become the default message for
     * the DTDException.</p>
     *
     * @param e The exception to be wrapped in a DTDException.
     */
   public DTDException(Exception e)
   {
      super();
      this.exception = e;
   }

   /**
     * Create a new DTDException from an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, but the new exception will have its own message.</p>
     *
     * @param message The detail message.
     * @param e The exception to be wrapped in a DTDException.
     */
   public DTDException(String message, Exception e)
   {
      super(message);
      this.exception = e;
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
     * Return a detail message for this exception.
     *
     * <p>If there is an embedded exception, and if the DTDException
     * has no detail message of its own, this method will return
     * the detail message from the embedded exception.</p>
     *
     * @return The error or warning message.
     */
   public String getMessage()
   {
      String message = super.getMessage();

      if(message == null && exception != null)
         return exception.getMessage();
      else
         return message;
   }

   /**
     * Return the embedded exception, if any.
     *
     * @return The embedded exception, or null if there is none.
     */
   public Exception getException()
   {
      if(exception == null)
         return new Exception(getMessage());
      else
         return exception;
   }

   /**
     * Override toString to pick up any embedded exception.
     *
     * @return A string representation of this exception.
     */
   public String toString ()
   {
      if (exception != null)
         return exception.toString();
      else
         return super.toString();
   }
}

