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

package com.globalsight.everest.webapp.applet.common;

import java.awt.Toolkit;

import java.util.Locale;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.DecimalFormatSymbols;

import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class GenericJTextField
    extends JTextField
{
    // the maximum length of the text (default value)
    private int m_maxLength = -1;

    //
    // Constructors
    //

    /**
     * Constructs a new TextField.  A default model is created, the
     * initial string is null, and the number of columns is set to 0.
     */
    public GenericJTextField()
    {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new JTextField.  A default model is created, the
     * initial string is null, and the number of columns is set to 0.
     * @param p_maxLength maximum length of text allowed in this field.
     */
    public GenericJTextField(int p_maxLength)
    {
        super();
        m_maxLength = p_maxLength;
    }


    /**
     * Constructs a new TextField initialized with the specified text.
     * A default model is created and the number of columns is 0.
     *
     * @param p_text the text to be displayed, or null
     */
    public GenericJTextField(String p_text)
    {
        super(p_text, Integer.MAX_VALUE);
    }

    /**
     * Constructs a new TextField initialized with the specified text.
     * A default model is created and the number of columns is 0.
     *
     * @param p_text the text to be displayed, or null
     * @param p_maxLength maximum length of text allowed in this field.
     */
    public GenericJTextField(String p_text, int p_maxLength)
    {
        super(p_text);
        m_maxLength = p_maxLength;
    }

    //
    // Override Methods
    //
    /**
     * Creates the default implementation of the model to be used at
     * construction.  We are overriding this method of JTextField so
     * that we can have a model that would only allow a limited length
     * text to be entered.
     * @return The numeric model implementation.
     */
    protected Document createDefaultModel()
    {
        return new LimitedLengthDocument();
    }

    //
    // Inner Class
    //
    /**
     * This is a subclass of PlainDocument that scans all input and
     * only allows an input that has not reached the max allowed
     * length defined by the user.  If the max lenght is not defined,
     * the default value that is Integer.MAX_VALUE will be used.
     */
    public class LimitedLengthDocument extends PlainDocument
    {
        public LimitedLengthDocument()
        {
            super();
        }

        /**
         * Inserts some content into the document.  Inserting content
         * causes a write lock to be held while the actual changes are
         * taking place, followed by notification to the observers on
         * the thread that grabbed the write lock.
         *
         * @param offset the starting offset >= 0
         * @param str the string to insert; does nothing with
         * null/empty strings
         * @param a the attributes for the inserted content
         * @exception BadLocationException the given insert position
         * is not a valid position within the document.
         */
        public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException
        {
            /*StringBuffer s = new StringBuffer();
              // make sure there's only one decimal point

              for (int i = 0; i < str.length(); i++)
              {
              char c = str.charAt(i);
              // if there's already a decimal point, continue (only for Float)
              if (containsDecPoint && c == getDecimalSeparator())
              {
              continue;
              }
              // if the minus sign is not at index 0, continue
              if (c == getMinusSign() && (i != 0 || offset != 0))
              {
              continue;
              }

              // only append numeric values, decimal separators,
              // and minus sign (if applicable)
              if (Character.isDigit(c) || isCharValid(c))
              {
              s.append(c);
              }
              } */

            // check for the maximum allowed length of the text
            if (getLength() + str.length() > m_maxLength)
            {
                Toolkit.getDefaultToolkit().beep();
            }
            else
            {
                super.insertString (offset, str, a);
            }
        }
    }
}
