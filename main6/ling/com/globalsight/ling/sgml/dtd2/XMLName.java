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
// Changes from version 1.01:
package com.globalsight.ling.sgml.dtd2;

import java.util.*;

/**
 * Contains information about a name in a namespace.
 *
 * <p>This class contains the local, qualified, and universal forms of a
 * name, as well as the namespace prefix and URI. The local form of a name
 * is the unprefixed name. The qualified form is constructed from the prefix,
 * a colon, and the local name. The universal form is constructed from the
 * namespace URI, a caret (^), and the local name. If the name does not
 * belong to a namespace, then all three forms are the same and the prefix
 * and URI are null. Note that an empty string ("") is used as the prefix
 * for the default namespace. <b>IMPORTANT:</b> Unprefixed attribute names do not
 * belong to a namespace.</p>
 *
 * <p>For example:</p>
 *
 * <pre>
 *    &lt;foo:element1 attr1="bar" foo:attr2="baz" xmlns="http://foo"&gt;<br />
 *
 *    foo:element1:
 *    --------------------------------------
 *    Local name:      "element1"
 *    Qualified name:  "foo:element1"
 *    Universal name:  "http://foo^element1"
 *    Prefix:          "foo"
 *    Namespace URI:   "http://foo"<br />
 *
 *    attr1:
 *    --------------------------------------
 *    Local name:      "attr1"
 *    Qualified name:  "attr1"
 *    Universal name:  "attr1"
 *    Prefix:          null
 *    Namespace URI:   null<br />
 *
 *    foo:attr2:
 *    --------------------------------------
 *    Local name:      "attr2"
 *    Qualified name:  "foo:attr2"
 *    Universal name:  "http://foo^attr2"
 *    Prefix:          "foo"
 *    Namespace URI:   "http://foo"<br />
 *
 *    &lt;element2&gt;<br />
 *
 *    element2:
 *    --------------------------------------
 *    Local name:      "element2"
 *    Qualified name:  "element2"
 *    Universal name:  "element2"
 *    Prefix:          null
 *    Namespace URI:   null<br />
 *
 *    &lt;element3 xmlns="http://foo" &gt;<br />
 *
 *    element2:
 *    --------------------------------------
 *    Local name:      "element3"
 *    Qualified name:  "element3"
 *    Universal name:  "http://foo^element3"
 *    Prefix:          ""
 *    Namespace URI:   "http://foo"<br />
 *
 * </pre>
 *
 * <p>XMLName objects that have a namespace URI are not required to have a
 * prefix. However, setPrefix(String) must be called on such objects before
 * getPrefix() or getQualifiedName() can be called.</p>
 *
 * <p>Note that the methods in this class perform only cursory checks on
 * whether input local names, prefixes, and URIs are legal.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class XMLName
{
   //***********************************************************************
   // Public constants
   //***********************************************************************

   /**
    * The character used to separate the URI from the local name.
    *
    * <p>This follows the convention in John Cowan's SAX namespace filter and
    * uses a caret (^), which is neither a valid URI character nor a valid XML
    * name character.</p>
    */
   public static String SEPARATOR = "^";

   //***********************************************************************
   // Private constants
   //***********************************************************************

   private static String COLON = ":",
                         XML = "xml",
                         XMLNS = "xmlns",
                         W3CNAMESPACE = "http://www.w3.org/XML/1998/namespace";

   //***********************************************************************
   // Variables
   //***********************************************************************

   private String local = null;
   private String qualified = null;
   private String universal = null;
   private String prefix = null;
   private String uri = null;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   private XMLName()
   {
   }

   //***********************************************************************
   // Factory methods
   //***********************************************************************

   /**
    * Construct an XMLName from a local name, prefix, and namespace URI.
    *
    * <p>If the URI is non-null, the prefix may be null. In this case, getPrefix()
    * and getQualifiedName() may not be called until the prefix is set.</p>
    *
    * <p>If the prefix is non-null, the URI must not be null.</p>
    *
    * @param uri The namespace URI. May be null.
    * @param local The local name.
    * @param prefix The namespace prefix. May be null. Use an empty string for
    *    the default namespace.
    *
    * @return The XMLName.
    */
   public static XMLName create(String uri, String localName, String prefix)
   {
      XMLName xmlName;
      String  qualified, universal;

      if ((prefix != null) && (uri == null))
      {
         throw new IllegalArgumentException(
             "If prefix is non-null, URI must not be null.");
      }

      // Get the qualified and universal names. This also checks if
      // the names are legal.  Note that the qualified name is not set
      // if the URI is non-null and the prefix is null.

      qualified = ((uri != null) && (prefix == null)) ?
                  null : getQualifiedName(prefix, localName);
      universal = getUniversalName(uri, localName);

      // Create and return a new XMLName.

      xmlName = new XMLName();

      xmlName.local = localName;
      xmlName.prefix = prefix;
      xmlName.uri = uri;
      xmlName.qualified = qualified;
      xmlName.universal = universal;

      return xmlName;
   }

   /**
    * Construct an XMLName from a local name and a namespace URI.
    *
    * <p>getPrefix() and getQualifiedName() may not be called until
    * the prefix is set.</p>
    *
    * @param uri The namespace URI. May be null.
    * @param local The local name.
    *
    * @return The XMLName.
    */
   public static XMLName create(String uri, String localName)
   {
      return create(uri, localName, null);
   }

   /**
    * Construct an XMLName from a qualified name and a hashtable mapping
    * prefixes to namespace URIS.
    *
    * @param qualifiedName Qualified name. Not required to contain a prefix.
    * @param uris Hashtable containing prefixes as keys and namespace URIs as
    *   values. If qualifiedName does not contain a colon, this may be null.
    *   Use an empty string ("") for the prefix of the default namespace.
    * @return The XMLName.
    * @exception IllegalArgumentException Thrown if the qualified name contains
    *   more than one colon or the Hashtable does not contain the prefix as a
    *   key.
    *
    */
   public static XMLName create(String qualifiedName, Hashtable uris)
   {
      // This method takes a (possibly) prefixed name and a Hashtable
      // relating namespace prefixes to URIs and returns an XMLName.

      String local = qualifiedName, prefix = null, uri = null;
      int    colon;

      checkQualifiedName(qualifiedName);

      // Search the qualified name for a colon and get the prefix,
      // namespace URI, and local name.

      colon = qualifiedName.indexOf(COLON);
      if (colon == -1)
      {
         // If namespaces are used, check for a default namespace. If one is found,
         // set the prefix to an empty string.

         if (uris != null)
         {
            uri = (String)uris.get("");
            if (uri != null)
            {
               prefix = "";
            }
         }
      }
      else
      {
         if (uris == null)
         {
            throw new IllegalArgumentException(
                "Argument uris must not be null when the qualified name contains a prefix.");
         }

         // Get the local name, prefix, and namespace URI.

         prefix = qualifiedName.substring(0, colon);
         local = qualifiedName.substring(colon + 1);
         if (prefix.toLowerCase().equals(XML))
         {
            // By definition, xml prefixes have a namespace of
            // http://www.w3.org/XML/1998/namespace.
            uri = W3CNAMESPACE;
         }
         else if (!prefix.toLowerCase().equals(XMLNS))
         {
            // If the prefix is xmlns, there is no namespace (by definition).
            // Therefore, leave the URI null. Otherwise, get the URI corresponding
            // to the prefix.

            uri = (String)uris.get(prefix);
            if (uri == null)
               throw new IllegalArgumentException("No namespace URI corresponding to prefix: " + prefix);
         }
      }

      // Return a new XMLName.

      return create(uri, local, prefix);
   }

   /**
    * Construct an XMLName from a universal name.
    *
    * <p>getPrefix() and getQualifiedName() may not be called until
    * the prefix is set.</p>
    *
    * @param universalName The universal name.
    *
    * @return The XMLName.
    */
   public static XMLName create(String universalName)
   {
      return create(getURIFromUniversal(universalName), getLocalFromUniversal(universalName));
   }

   //***********************************************************************
   // Static utility methods
   //***********************************************************************

   /**
    * Construct a qualified name. Returns the local name if the URI is
    * null.
    *
    * @param prefix The namespace prefix. May be null or empty.
    * @param localName The local name.
    */
   public static String getQualifiedName(String prefix, String localName)
   {
      checkLocalName(localName);
      checkPrefix(prefix);

      // Return the local name if there is no prefix or if the prefix is
      // the empty string.

      if ((prefix == null) || (prefix.length() == 0)) return localName;
      return prefix + COLON + localName;
   }

   /**
    * Construct a qualified name from a universal name and a hashtable
    * mapping URIs to prefixes.
    *
    * @param universalName The universal name. Not required to contain a URI.
    * @param prefixes Hashtable containing namespace URIs as keys and prefixes as
    *    values. If the universal name does not contain a caret, this may be null.
    *    Use an empty string ("") for the prefix of the default namespace.
    * @exception IllegalArgumentException Thrown if no prefix corresponding to the
    *    namespace URI was found.
    */
   public static String getQualifiedName(String universalName, Hashtable prefixes)
   {
      String uri, prefix, localName;

      uri = getURIFromUniversal(universalName);
      localName = getLocalFromUniversal(universalName);
      if (uri == null) return localName;

      if (prefixes == null)
         throw new IllegalArgumentException("prefixes argument cannot be null when the universal name contains a URI.");

      prefix = (String)prefixes.get(uri);
      if (prefix == null)
         throw new IllegalArgumentException("No prefix corresponding to the namespace URI: " + uri);
      if (prefix.length() == 0)
      {
         return localName;
      }
      else
      {
         return prefix + COLON + localName;
      }
   }

   /**
    * Construct a universal name. Returns the local name if the URI is
    * null.
    *
    * @param uri The namespace URI.
    * @param localName The local name.
    */
   public static String getUniversalName(String uri, String localName)
   {
      checkLocalName(localName);
      checkURI(uri);

      if (uri == null) return localName;
      return uri + SEPARATOR + localName;
   }

   /**
    * Construct a universal name from a qualified name and a hashtable mapping
    * prefixes to namespace URIS.
    *
    * @param qualifiedName Qualified name. Not required to contain a prefix.
    * @param uris Hashtable containing prefixes as keys and namespace URIs as
    *   values.
    * @exception IllegalArgumentException Thrown if no URI corresponding to the
    *    prefix was found.
    */
   public static String getUniversalName(String qualifiedName, Hashtable uris)
   {
      String local = qualifiedName, prefix = null, uri = null;
      int    colon;

      checkQualifiedName(qualifiedName);

      // Search the qualified name for a colon and get the prefix
      // namespace URI, and local name.

      colon = qualifiedName.indexOf(COLON);
      if (colon == -1)
      {
         // If namespaces are used, check for a default namespace.

         if (uris != null)
         {
            uri = (String)uris.get("");
         }
      }
      else
      {
         if (uris == null)
            throw new IllegalArgumentException("Argument uris must not be null when the qualified name contains a prefix.");

         // Get the local name, prefix, and namespace URI.

         prefix = qualifiedName.substring(0, colon);
         local = qualifiedName.substring(colon + 1);

         if (prefix.toLowerCase().equals(XML))
         {
            // By definition, xml prefixes have a namespace of
            // http://www.w3.org/XML/1998/namespace.
            uri = W3CNAMESPACE;
         }
         else if (!prefix.toLowerCase().equals(XMLNS))
         {
            // If the prefix is xmlns, there is no namespace (by definition).
            // Therefore, leave the URI null. Otherwise, get the URI.

            uri = (String)uris.get(prefix);
            if (uri == null)
               throw new IllegalArgumentException("No namespace URI corresponding to prefix: " + prefix);
         }
      }

      // Return the universal name

      return getUniversalName(local, uri);
   }

   /**
    * Get the prefix from a qualified name.
    *
    * @param qualifiedName Qualified name.
    * @return The prefix or null if there is no prefix.
    */
   public static String getPrefixFromQualified(String qualifiedName)
   {
      int    colon;

      checkQualifiedName(qualifiedName);

      colon = qualifiedName.indexOf(COLON);
      if (colon == -1) return null;
      return qualifiedName.substring(0, colon);
   }

   /**
    * Get the local name from a qualified name.
    *
    * @param qualifiedName Qualified name.
    * @return The local name.
    */
   public static String getLocalFromQualified(String qualifiedName)
   {
      int    colon;

      checkQualifiedName(qualifiedName);

      colon = qualifiedName.indexOf(COLON);
      if (colon == -1) return qualifiedName;
      return qualifiedName.substring(colon + 1);
   }

   /**
    * Get the URI from a universal name.
    *
    * @param universalName Universal name.
    * @return The URI or null if there is no URI.
    */
   public static String getURIFromUniversal(String universalName)
   {
      int separator;

      checkUniversalName(universalName);

      separator = universalName.indexOf(SEPARATOR);
      if (separator == -1) return null;
      return universalName.substring(0, separator);
   }

   /**
    * Get the local name from a universal name.
    *
    * @param universalName Universal name.
    * @return The local name.
    */
   public static String getLocalFromUniversal(String universalName)
   {
      int separator;

      checkUniversalName(universalName);

      separator = universalName.indexOf(SEPARATOR);
      if (separator == -1) return universalName;
      return universalName.substring(separator + 1);
   }

   //***********************************************************************
   // Accessor and mutator methods
   //***********************************************************************

   /**
    * Get the local name.
    *
    * @return The local name.
    */
   public final String getLocalName()
   {
      return local;
   }

   /**
    * Get the qualified name.
    *
    * @return The qualified name.
    * @exception IllegalStateException Thrown if the namespace URI is non-null
    *    and the prefix has not been set.
    */
   public final String getQualifiedName()
   {
      if (qualified == null)
         throw new IllegalStateException("Cannot return the qualified name when the prefix is not set.");
      return qualified;
   }

   /**
    * Get the universal name.
    *
    * @return The universal name.
    */
   public final String getUniversalName()
   {
      return universal;
   }

   /**
    * Get the namespace prefix.
    *
    * @return The prefix.
    * @exception IllegalStateException Thrown if the namespace URI is non-null
    *    and the prefix has not been set.
    */
   public final String getPrefix()
   {
      if (qualified == null)
         throw new IllegalStateException("The prefix has not been set.");
      return prefix;
   }

   /**
    * Get the namespace URI.
    *
    * @return The namespace URI.
    */
   public final String getURI()
   {
      return uri;
   }

   /**
    * Set the namespace prefix.
    *
    * @param prefix The namespace prefix.
    */
   public final void setPrefix(String prefix)
   {
      if (uri == null)
         throw new IllegalStateException("Cannot set the prefix when the URI is null.");

      if (prefix == null)
         throw new IllegalArgumentException("prefix argument must be non-null.");

      this.qualified = getQualifiedName(this.local, prefix);
      this.prefix = prefix;
   }

   //***********************************************************************
   // Equals and hashCode methods
   //***********************************************************************

   /**
    * Overrides Object.equals(Object).
    *
    * <p>An object is equal to this XMLName object if: (1) it is an XMLName object
    * and (2) it has the same URI and local name. Note that two XMLName objects are
    * considered equal even if they have different namespace prefixes.</p>
    *
    * @param obj The reference object with which to compare.
    * @return true if this object is the same as the obj argument; false otherwise.
    */
   public boolean equals(Object obj)
   {
      String objectURI;

      // Return false if the object is not an XMLName object.

      if (!(obj instanceof XMLName)) return false;

      // Return false if the object has a different URI.

      objectURI = ((XMLName)obj).getURI();
      if (uri == null)
      {
         if (objectURI != null) return false;
      }
      else
      {
         if (!uri.equals(objectURI)) return false;
      }

      // Return true or false depending on whether the objects have the same local name.

      return local.equals( ((XMLName)obj).getLocalName() );
   }

   /**
    * Overrides Object.hashCode().
    *
    * <p>Two XMLName objects that are equal according to the equals method return
    * the same hash code.</p>
    *
    * @return The hash code
    */
   public int hashCode()
   {
      return universal.hashCode();
   }

   //***********************************************************************
   // Check methods
   //***********************************************************************

   private static void checkLocalName(String localName)
   {
      // Check for valid characters not implemented

      if (localName == null)
         throw new IllegalArgumentException("Local name cannot be null.");

      if (localName.length() == 0)
         throw new IllegalArgumentException("Local name must have non-zero length.");

      if (localName.indexOf(COLON) != -1)
         throw new IllegalArgumentException("Local name contains a colon: " + localName);

      if (localName.indexOf(SEPARATOR) != -1)
         throw new IllegalArgumentException("Local name contains a caret: " + localName);
   }

   private static void checkPrefix(String prefix)
   {
      // Check for valid characters not implemented

      if (prefix == null) return;

      if (prefix.indexOf(COLON) != -1)
         throw new IllegalArgumentException("Prefix contains a colon: " + prefix);

      if (prefix.indexOf(SEPARATOR) != -1)
         throw new IllegalArgumentException("Prefix contains a caret: " + prefix);
   }

   private static void checkURI(String uri)
   {
      // Check for valid characters not implemented

      if (uri == null) return;

      if (uri.length() == 0)
         throw new IllegalArgumentException("Namespace URI must have non-zero length.");

      if (uri.indexOf(SEPARATOR) != -1)
         throw new IllegalArgumentException("Namespace URI contains a caret: " + uri);
   }

   private static void checkQualifiedName(String qualifiedName)
   {
      int colon;

      if (qualifiedName == null)
         throw new IllegalArgumentException("Qualified name cannot be null.");

      if (qualifiedName.length() == 0)
         throw new IllegalArgumentException("Qualified name must have non-zero length.");

      colon = qualifiedName.indexOf(COLON);
      if (colon == -1)
      {
         checkLocalName(qualifiedName);
      }
      else
      {
         checkPrefix(qualifiedName.substring(0, colon));
         checkLocalName(qualifiedName.substring(colon + 1));
      }
   }

   private static void checkUniversalName(String universalName)
   {
      int separator;

      if (universalName == null)
         throw new IllegalArgumentException("Universal name cannot be null.");

      if (universalName.length() == 0)
         throw new IllegalArgumentException("Universal name must have non-zero length.");

      separator = universalName.indexOf(SEPARATOR);
      if (separator == -1)
      {
         checkLocalName(universalName);
      }
      else
      {
         checkURI(universalName.substring(0, separator));
         checkLocalName(universalName.substring(separator + 1));
      }
   }
}

