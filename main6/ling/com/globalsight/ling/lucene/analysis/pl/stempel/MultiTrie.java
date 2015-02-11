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
package com.globalsight.ling.lucene.analysis.pl.stempel;

import java.util.*;
import java.io.*;

/**
 *  The MultiTrie is a Trie of Tries. It stores words and their associated
 *  patch commands. The MultiTrie handles patch commmands individually
 *  (each command by itself).
 *
 * @author    Leo Galambos
 */
public class MultiTrie extends Trie {
    final String EOM = "*";

    Vector tries = new Vector();

    int BY = 1;


    /**
     *  Constructor for the MultiTrie object.
     *
     * @param  is               the input stream
     * @exception  IOException  if an I/O error occurs
     */
    public MultiTrie(DataInput is) throws IOException {
        super(false);
        forward = is.readBoolean();
        BY = is.readInt();
        for (int i = is.readInt(); i > 0; i--) {
            tries.addElement(new Trie(is));
        }
    }


    /**
     *  Constructor for the MultiTrie object
     *
     * @param  forward  set to <tt>true</tt> if the elements should be read
     *      left to right
     */
    public MultiTrie(boolean forward) {
        super(forward);
    }


    /**
     *  Return the element that is stored in a cell associated with the
     *  given key.
     *
     * @param  key  the key to the cell holding the desired element
     * @return      the element
     */
    public String getFully(String key) {
        StringBuffer result = new StringBuffer(tries.size() * 2);
        for (int i = 0; i < tries.size(); i++) {
            String r = ((Trie) tries.elementAt(i)).getFully(key);
            if (EOM.equalsIgnoreCase(r)) {
                return result.toString();
            }
            result.append(r);
        }
        return result.toString();
    }


    /**
     *  Return the element that is stored as last on a path belonging to
     *  the given key.
     *
     * @param  key  the key associated with the desired element
     * @return      the element that is stored as last on a path
     */
    public String getLastOnPath(String key) {
        StringBuffer result = new StringBuffer(tries.size() * 2);
        for (int i = 0; i < tries.size(); i++) {
            String r = ((Trie) tries.elementAt(i)).getLastOnPath(key);
            if (EOM.equalsIgnoreCase(r)) {
                return result.toString();
            }
            result.append(r);
        }
        return result.toString();
    }


    /**
     *  Write this data structure to the given output stream.
     *
     * @param  os               the output stream
     * @exception  IOException  if an I/O error occurs
     */
    public void store(DataOutput os) throws IOException {
        os.writeBoolean(forward);
        os.writeInt(BY);
        os.writeInt(tries.size());
        Enumeration e = tries.elements();
        while (e.hasMoreElements()) {
            ((Trie) e.nextElement()).store(os);
        }
    }


    /**
     *  Add an element to this structure consisting of the given key and
     *  patch command. This method will return without executing if the
     *  <tt>cmd</tt> parameter's length is 0.
     *
     * @param  key  the key
     * @param  cmd  the patch command
     */
    public void add(String key, String cmd) {
        if (cmd.length() == 0) {
            return;
        }
        int levels = cmd.length() / BY;
        while (levels >= tries.size()) {
            tries.addElement(new Trie(forward));
        }
        for (int i = 0; i < levels; i++) {
            ((Trie) tries.elementAt(i)).add(key, cmd.substring(BY * i, BY * i + BY));
        }
        ((Trie) tries.elementAt(levels)).add(key, EOM);
    }


    /**
     *  Remove empty rows from the given Trie and return the newly reduced
     *  Trie.
     *
     * @param  by  the Trie to reduce
     * @return     the newly reduced Trie
     */
    public Trie reduce(Reduce by) {
        Vector h = new Vector();
        Enumeration e = tries.elements();
        while (e.hasMoreElements()) {
            Trie a = (Trie) e.nextElement();
            h.addElement(a.reduce(by));
        }
        MultiTrie m = new MultiTrie(forward);
        m.tries = h;
        return m;
    }


    /**
     *  Print the given prefix and the position(s) in the Trie where it
     *  appears.
     *
     * @param  prefix  the desired prefix
     */
    public void printInfo(String prefix) {
        int c = 0;
        for (Enumeration e = tries.elements(); e.hasMoreElements(); c++) {
            Trie a = (Trie) e.nextElement();
            /*
             *  if (c==0) {
             *  Enumeration f = a.cmds.elements();
             *  while (f.hasMoreElements()) {
             *  System.out.print( "("+((String)f.nextElement())+")" );
             *  }
             *  }
             */
            a.printInfo(prefix + "[" + c + "] ");
        }
    }
}
