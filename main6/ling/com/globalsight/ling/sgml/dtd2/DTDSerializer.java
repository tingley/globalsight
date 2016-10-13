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

import java.io.*;
import java.util.*;

/**
 * Class that serializes a DTD.
 *
 * <p>Note that this class does not write out parameter entities.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DTDSerializer
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private DTD     dtd;
   private Writer  writer = null;
   private boolean pretty = false;
   private int     indent = 0;

   // ********************************************************************
   // Constants
   // ********************************************************************

   private static String CHOICESEPARATOR = " | ",
                         COMMENTEND      = " -->",
                         COMMENTSTART    = "<!-- ",
                         DECLSTART       = "<!",
                         SEQSEPARATOR    = ", ",
                         QUOTENTITYREF   = "&quot;",
                         RETURN          = System.getProperty("line.separator");
   private static final int DECLEND     = '>',
                            GROUPEND    = ')',
                            GROUPSTART  = '(',
                            ONEORMORE   = '+',
                            OPTIONAL    = '?',
                            POUND       = '#',
                            SPACE       = ' ',
                            ZEROORMORE  = '*',
                            DOUBLEQUOTE = '"',
                            SINGLEQUOTE = '\'';

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new DTDSerializer. */
   public DTDSerializer()
   {
   }

   // ********************************************************************
   // Public Methods -- serialization
   // ********************************************************************

   /**
    * Serialize a DTD to a Writer.
    *
    * @param dtd The DTD.
    * @param writer The Writer.
    * @param pretty Whether to perform pretty printing.
    */
   public void serialize(DTD dtd, Writer writer, boolean pretty)
      throws IOException
   {
      this.dtd = dtd;
      this.writer = writer;
      this.pretty = pretty;

      writeEntities();
      writeElementTypes();
      writeNotations();
   }

   // ********************************************************************
   // Private methods -- serialization
   // ********************************************************************

   private void writeElementTypes()
      throws IOException
   {
      Enumeration e = dtd.elementTypes.elements();
      while (e.hasMoreElements())
      {
         writeElementType((ElementType)e.nextElement());
      }
   }

   private void writeElementType(ElementType elementType)
      throws IOException
   {
      ElementType child;

      if (pretty)
      {
         writer.write(RETURN);
         writer.write(RETURN);
      }

      writer.write(DECLSTART);
      writer.write(DTDConst.KEYWD_ELEMENT);
      writer.write(SPACE);
      writer.write(elementType.name.getQualifiedName());
      writer.write(SPACE);

      switch (elementType.contentType)
      {
         case ElementType.CONTENT_EMPTY:
            writer.write(DTDConst.KEYWD_EMPTY);
            break;

         case ElementType.CONTENT_ANY:
            writer.write(DTDConst.KEYWD_ANY);
            break;

         case ElementType.CONTENT_PCDATA:
            writer.write(GROUPSTART);
            writer.write(POUND);
            writer.write(DTDConst.KEYWD_PCDATA);
            writer.write(GROUPEND);
            break;

         case ElementType.CONTENT_MIXED:
            writer.write(GROUPSTART);
            writer.write(POUND);
            writer.write(DTDConst.KEYWD_PCDATA);

            // Write the children -- assume there is at least one...

            Enumeration e = elementType.children.elements();
            while (e.hasMoreElements())
            {
               writer.write(CHOICESEPARATOR);
               child = (ElementType)e.nextElement();
               writer.write(child.name.getQualifiedName());
            }

            writer.write(GROUPEND);
            writer.write(ZEROORMORE);
            break;

         case ElementType.CONTENT_ELEMENT:
            if (elementType.content.type == Particle.TYPE_ELEMENTTYPEREF)
            {
               // If the content model is a single element type reference,
               // we need to write the surrounding parentheses ourselves.
               // Otherwise, these are taken care of by writeGroup().

               writer.write(GROUPSTART);
               writeParticle(elementType.content);
               writer.write(GROUPEND);
            }
            else
            {
               writeParticle(elementType.content);
            }
            break;
      }
      writer.write(DECLEND);

      writeAttributes(elementType);
   }

   private void writeParticle(Particle particle)
      throws IOException
   {
      if (particle.type == Particle.TYPE_ELEMENTTYPEREF)
      {
         writeRef((Reference)particle);
      }
      else // if (type == Particle.TYPE_CHOICE || Particle.TYPE_SEQUENCE)
      {
         writeGroup((Group)particle);
      }
   }

   private void writeRef(Reference ref)
      throws IOException
   {
      writer.write(ref.elementType.name.getQualifiedName());
      writeFrequency(ref);
   }

   private void writeGroup(Group group)
      throws IOException
   {
      String separator;

      if (group.type == Particle.TYPE_CHOICE)
      {
         separator = CHOICESEPARATOR;
      }
      else // if (group.type == Particle.TYPE_CHOICE)
      {
         separator = SEQSEPARATOR;
      }

      writer.write(GROUPSTART);
      writeParticle((Particle)group.members.elementAt(0));
      for (int i = 1; i < group.members.size(); i++)
      {
         writer.write(separator);
         writeParticle((Particle)group.members.elementAt(i));
      }
      writer.write(GROUPEND);
      writeFrequency(group);
   }

   private void writeFrequency(Particle particle)
      throws IOException
   {
      if (particle.isRequired)
      {
         if (particle.isRepeatable)
         {
            writer.write(ONEORMORE);
         }
      }
      else if (particle.isRepeatable)
      {
         writer.write(ZEROORMORE);
      }
      else
      {
         writer.write(OPTIONAL);
      }
   }

   private void writeAttributes(ElementType elementType)
      throws IOException
   {
      if (elementType.attributes == null) return;
      if (elementType.attributes.size() == 0) return;

      if (pretty)
      {
         writer.write(RETURN);
      }

      writer.write(DECLSTART);
      writer.write(DTDConst.KEYWD_ATTLIST);
      writer.write(SPACE);
      writer.write(elementType.name.getQualifiedName());
      writer.write(SPACE);
      Enumeration e = elementType.attributes.elements();
      indent += 10;
      while (e.hasMoreElements())
      {
         writeAttribute((Attribute)e.nextElement());
      }
      indent -= 10;
      writer.write(DECLEND);
   }

   private void writeAttribute(Attribute attribute)
      throws IOException
   {
      if (pretty)
      {
         writer.write(RETURN);
         indent();
      }

      writer.write(attribute.name.getQualifiedName());
      writer.write(SPACE);
      writeAttributeType(attribute);
      writeAttributeDefault(attribute);
   }

   private void writeAttributeType(Attribute attribute)
      throws IOException
   {
      switch (attribute.type)
      {
         case Attribute.TYPE_CDATA:
            writer.write(DTDConst.KEYWD_CDATA);
            break;

         case Attribute.TYPE_ID:
            writer.write(DTDConst.KEYWD_ID);
            break;

         case Attribute.TYPE_IDREF:
            writer.write(DTDConst.KEYWD_IDREF);
            break;

         case Attribute.TYPE_IDREFS:
            writer.write(DTDConst.KEYWD_IDREFS);
            break;

         case Attribute.TYPE_ENTITY:
            writer.write(DTDConst.KEYWD_ENTITY);
            break;

         case Attribute.TYPE_ENTITIES:
            writer.write(DTDConst.KEYWD_ENTITIES);
            break;

         case Attribute.TYPE_NMTOKEN:
            writer.write(DTDConst.KEYWD_NMTOKEN);
            break;

         case Attribute.TYPE_NMTOKENS:
            writer.write(DTDConst.KEYWD_NMTOKENS);
            break;

         case Attribute.TYPE_NOTATION:
            writeEnumeration(attribute.enums, true);
            break;

         case Attribute.TYPE_ENUMERATED:
            writeEnumeration(attribute.enums, false);
            break;
      }
      writer.write(SPACE);
   }

   private void writeEnumeration(Vector enumeration, boolean isNotation)
      throws IOException
   {
      if (isNotation)
      {
         writer.write(DTDConst.KEYWD_NOTATION);
         writer.write(SPACE);
      }

      writer.write(GROUPSTART);
      writer.write(((String)enumeration.elementAt(0)));
      for (int i = 1; i < enumeration.size(); i++)
      {
         writer.write(CHOICESEPARATOR);
         writer.write(((String)enumeration.elementAt(i)));
      }
      writer.write(GROUPEND);
   }

   private void writeAttributeDefault(Attribute attribute)
      throws IOException
   {
      switch (attribute.required)
      {
         case Attribute.REQUIRED_REQUIRED:
            writer.write(POUND);
            writer.write(DTDConst.KEYWD_REQUIRED);
            break;

         case Attribute.REQUIRED_OPTIONAL:
            writer.write(POUND);
            writer.write(DTDConst.KEYWD_IMPLIED);
            break;

         case Attribute.REQUIRED_FIXED:
            writer.write(POUND);
            writer.write(DTDConst.KEYWD_FIXED);
            writer.write(SPACE);

         // WARNING! Above case falls through.
         case Attribute.REQUIRED_DEFAULT:
            writer.write(DOUBLEQUOTE);
            writer.write(replaceDoubleQuotes(attribute.defaultValue));
            writer.write(DOUBLEQUOTE);
            break;
      }
   }

   private void writeNotations()
      throws IOException
   {
      Enumeration e = dtd.notations.elements();
      while (e.hasMoreElements())
      {
         writeNotation((Notation)e.nextElement());
      }
   }

   private void writeNotation(Notation notation)
      throws IOException
   {
      if (pretty)
      {
         writer.write(RETURN);
         writer.write(RETURN);
      }

      writer.write(DECLSTART);
      writer.write(DTDConst.KEYWD_NOTATION);
      writer.write(SPACE);
      writer.write(notation.name);
      writer.write(SPACE);
      if (notation.publicID != null)
      {
         writer.write(DTDConst.KEYWD_PUBLIC);
         writer.write(SPACE);
         writeQuotedValue(notation.publicID);
      }
      else
      {
         // Assume that the object is constructed correctly and at least one
         // of publicID and systemID is not null.
         writer.write(DTDConst.KEYWD_SYSTEM);
      }
      writer.write(SPACE);
      if (notation.systemID != null)
      {
         writeQuotedValue(notation.systemID);
         writer.write(SPACE);
      }
      writer.write(DECLEND);
   }

   private void writeEntities()
      throws IOException
   {
      Enumeration e;

      e = dtd.unparsedEntities.elements();
      while (e.hasMoreElements())
      {
         writeEntity((Entity)e.nextElement());
      }

      e = dtd.parsedGeneralEntities.elements();
      while (e.hasMoreElements())
      {
         writeEntity((Entity)e.nextElement());
      }
   }

   private void writeEntity(Entity entity)
      throws IOException
   {
      if (pretty)
      {
         writer.write(RETURN);
         writer.write(RETURN);
      }

      writer.write(DECLSTART);
      writer.write(DTDConst.KEYWD_ENTITY);
      writer.write(SPACE);
      writer.write(entity.name);
      writer.write(SPACE);
      if (entity.systemID == null)
      {
         // Internal entity

         writeQuotedValue(((ParsedGeneralEntity)entity).value);
      }
      else
      {
         // External entity

         if (entity.publicID != null)
         {
            writer.write(DTDConst.KEYWD_PUBLIC);
            writer.write(SPACE);
            writeQuotedValue(entity.publicID);
         }
         else
         {
            writer.write(DTDConst.KEYWD_SYSTEM);
         }
         writer.write(SPACE);
         writeQuotedValue(entity.systemID);
         if (entity.type == Entity.TYPE_UNPARSED)
         {
            writer.write(SPACE);
            writer.write(DTDConst.KEYWD_NDATA);
            writer.write(SPACE);
            writer.write(((UnparsedEntity)entity).notation);
         }
      }
      writer.write(SPACE);
      writer.write(DECLEND);
   }

   private void writeQuotedValue(String value)
      throws IOException
   {
      int quote;

      quote = getQuote(value);
      writer.write(quote);
      writer.write(value);
      writer.write(quote);
   }

   private int getQuote(String value)
   {
      if (value.indexOf(DOUBLEQUOTE) != -1)
         return SINGLEQUOTE;
      else
         return DOUBLEQUOTE;
   }

   private String replaceDoubleQuotes(String value)
   {
      StringBuffer s = new StringBuffer();
      int          oldPos = 0, newPos = 0;

      while (newPos != -1)
      {
         newPos = value.indexOf(DOUBLEQUOTE, oldPos);
         if (newPos != -1)
         {
            s.append(value.substring(oldPos, newPos));
            s.append(QUOTENTITYREF);
            newPos++;
            oldPos = newPos;
         }
      }
      s.append(value.substring(oldPos, value.length()));
      return s.toString();
   }

   private void indent()
      throws IOException
   {
      for (int i = 0; i < indent; i++)
      {
         writer.write(SPACE);
      }
   }
}
