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
package com.globalsight.ling.jtidy;

/**
 * Input Stream.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */

public interface StreamIn
{

    /**
     * end of stream char.
     */
    int END_OF_STREAM = -1;

    /**
     * Getter for <code>curcol</code>.
     * @return Returns the curcol.
     */
    int getCurcol();

    /**
     * Getter for <code>curline</code>.
     * @return Returns the curline.
     */
    int getCurline();

    /**
     * reads a char from the stream.
     * @return char
     */
    int readCharFromStream();

    /**
     * Read a char.
     * @return char
     */
    int readChar();

    /**
     * Unget a char.
     * @param c char
     */
    void ungetChar(int c);

    /**
     * Has end of stream been reached?
     * @return <code>true</code> if end of stream has been reached
     */
    boolean isEndOfStream();

    /**
     * Setter for lexer instance (needed for error reporting).
     * @param lexer Lexer
     */
    void setLexer(Lexer lexer);

}
