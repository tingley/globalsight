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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Vector;

import java.text.BreakIterator;

/**
 * EnvoyWordWrapper is a helper class which is used to take care of
 * the word wrapping for a string.  The whole string is parsed based
 * on the component size so that the displayed string could be read
 * easily.
 */
public class EnvoyWordWrapper
{
    /**
     * Parse the text so that it can be viewed within the specified
     * component.  This method is useful for parsing a message that
     * should be displayed in a text component.  Note that the
     * component passed should be a displayed component with a
     * non-zero dimension.
     *
     * @param p_text - The text to be parsed.
     * @param p_component - The component that the text should be
     * displayed in it.
     * @return The parsed string.
     */
    public String parseText(String p_text, Component p_component, Font p_font)
        throws IllegalArgumentException
    {
        // make sure that the component is not null
        if (p_component == null)
        {
            throw new IllegalArgumentException(
                "EnvoyWordWrapper - invalid component...");
        }

        // used for getting the width of the string
        FontMetrics fontMetrics = p_component.getGraphics() == null ?
            p_component.getFontMetrics(p_font) :
            p_component.getGraphics().getFontMetrics();

        return parseText(p_text, fontMetrics, (p_component.getSize().width));
    }


    /**
     * Parse the text based on the speficied FontMetrics, and max
     * length per line.
     * @param p_text - The text to be parsed.
     * @param p_fontMetrics - FontMetrics used for calculating the
     * with of a string.
     * @param p_maxLength - The maximum length of a line.
     */
    public String parseText(String p_text, FontMetrics p_fontMetrics,
        int p_maxLength)
    {
        if (p_text == null)
        {
            p_text = "";
        }

        // used for getting the width of the string
        FontMetrics fontMetrics = p_fontMetrics;
        // the width of the component (the text width should be less
        // than this one)
        int maxLength = p_maxLength < 15 ? (p_maxLength + 12) : p_maxLength;

        // TomyD -- get the sysmbol for the system's line separator
        String endOfLineString = null;

        try
        {
            endOfLineString = System.getProperty("line.separator");
        }
        catch (SecurityException se)
        {
            endOfLineString = "\n";
        }

        // get the boundary based on a particular locale
        BreakIterator boundary = BreakIterator.getLineInstance(
            GlobalEnvoy.getLocale());
        boundary.setText(p_text);

        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;

        // intermediate string storage
        StringBuffer intermediate = new StringBuffer();
        // the buffer that stores the final result
        StringBuffer result = new StringBuffer();
        // stores a complete line
        String finishedLine = "";

        // go through the whole text until the iteration is "complete"
  complete:
        while (end != BreakIterator.DONE)
        {
            // update the line length
            lineLength = fontMetrics.stringWidth(intermediate.toString());

            while (lineLength < maxLength-12)
            {
                // if we're done, exit the main while loop
                if (end == BreakIterator.DONE)
                    break complete;

                // we successfully added another word, let's put in our bucket
                finishedLine = intermediate.toString();

                // populate the intermediate buffer (word by word)
                String temp = p_text.substring(start,end);
                lineLength += fontMetrics.stringWidth(temp);
                intermediate.append(temp);

                // update the starting and ending point
                start = end;
                end = boundary.next();
            }

            // we have exactly one lineful!  If our newly built line
            // is blank, then if the original text isn't blank, we
            // have a word that's too long!
            if (finishedLine.length() == 0 && intermediate.length() != 0)
            {
                // chop our word at a point that will just fit, and
                // remove from text as an optimization, we'll assume
                // that the word was just a little too long, and we'll
                // shrink the size back from there
                for (int i = intermediate.length(); i > 0; i--)
                {
                    String test = intermediate.toString().substring(0, i);

                    if (fontMetrics.stringWidth(test) < maxLength-5)
                    {
                        finishedLine = test;
                        break;
                    }
                }

                result.append(finishedLine + "-" + endOfLineString);
            }
            else
            {
                // all is OK, no extra-long words
                result.append(finishedLine+endOfLineString);
            }
            // there's still something left in intermediate, use for
            // next iteration
            intermediate = new StringBuffer(
                intermediate.toString().substring(finishedLine.length(),
                    intermediate.length()));
            finishedLine = "";
        }

        // include any remaining text in the intermediate
        result.append(intermediate.toString());
        return result.toString();
    }
}
