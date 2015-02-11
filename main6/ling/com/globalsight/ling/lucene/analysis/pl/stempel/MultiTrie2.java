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
 *  patch commands. The MultiTrie handles patch commmands broken into their
 *  constituent parts, as a MultiTrie does, but the commands are delimited
 *  by the skip command.
 *
 * @author    Leo Galambos
 */
public class MultiTrie2 extends MultiTrie {
    /**
     *  Constructor for the MultiTrie object.
     *
     * @param  is               the input stream
     * @exception  IOException  if an I/O error occurs
     */
    public MultiTrie2(DataInput is) throws IOException {
        super(is);
    }


    /**
     *  Constructor for the MultiTrie2 object
     *
     * @param  forward  set to <tt>true</tt> if the elements should be read
     *      left to right
     */
    public MultiTrie2(boolean forward) {
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
        try {
            String lastkey = key;
            String p[] = new String[tries.size()];
            char lastch = ' ';
            for (int i = 0; i < tries.size(); i++) {
                String r = ((Trie) tries.elementAt(i)).getFully(lastkey);
                if (EOM.equalsIgnoreCase(r) || r == null) {
                    return result.toString();
                }
                if (cannotFollow(lastch, r.charAt(0))) {
                    return result.toString();
                } else {
                    lastch = r.charAt(r.length() - 2);
                }
//      key=key.substring(lengthPP(r));
                p[i] = r;
                if (p[i].startsWith("-")) {
                    if (i > 0) {
                        key = skip(key, lengthPP(p[i - 1]));
                    }
                    key = skip(key, lengthPP(p[i]));
                }
//                key = skip(key, lengthPP(r));
                result.append(r);
                if (key.length() != 0) {
                    lastkey = key;
                }
            }
        } catch (IndexOutOfBoundsException x) {
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
        try {
            String lastkey = key;
            String p[] = new String[tries.size()];
            char lastch = ' ';
            for (int i = 0; i < tries.size(); i++) {
                String r = ((Trie) tries.elementAt(i)).getLastOnPath(lastkey);
                if (EOM.equalsIgnoreCase(r) || r == null) {
                    return result.toString();
                }
//      System.err.println("LP:"+key+" last:"+lastch+" new:"+r);
                if (cannotFollow(lastch, r.charAt(0))) {
                    return result.toString();
                } else {
                    lastch = r.charAt(r.length() - 2);
                }
//      key=key.substring(lengthPP(r));
                p[i] = r;
                if (p[i].startsWith("-")) {
                    if (i > 0) {
                        key = skip(key, lengthPP(p[i - 1]));
                    }
                    key = skip(key, lengthPP(p[i]));
                }
//                key = skip(key, lengthPP(r));
                result.append(r);
                if (key.length() != 0) {
                    lastkey = key;
                }
            }
        } catch (IndexOutOfBoundsException x) {
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
        super.store(os);
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
//System.err.println( cmd );
        String p[] = decompose(cmd);
        int levels = p.length;
//System.err.println("levels "+key+" cmd "+cmd+"|"+levels);
        while (levels >= tries.size()) {
            tries.addElement(new Trie(forward));
        }
        String lastkey = key;
        for (int i = 0; i < levels; i++) {
            if (key.length() > 0) {
                ((Trie) tries.elementAt(i)).add(key, p[i]);
                lastkey = key;
            } else {
                ((Trie) tries.elementAt(i)).add(lastkey, p[i]);
            }
//System.err.println("-"+key+" "+p[i]+"|"+key.length());
            /*
             *  key=key.substring(lengthPP(p[i]));
             */
            if (p[i].startsWith("-")) {
                if (i > 0) {
                    key = skip(key, lengthPP(p[i - 1]));
                }
                key = skip(key, lengthPP(p[i]));
            }
//System.err.println("--->"+key);
        }
        if (key.length() > 0) {
            ((Trie) tries.elementAt(levels)).add(key, EOM);
        } else {
            ((Trie) tries.elementAt(levels)).add(lastkey, EOM);
        }
    }


    /**
     *  Break the given patch command into its constituent pieces. The
     *  pieces are delimited by NOOP commands.
     *
     * @param  cmd  the patch command
     * @return      an array containing the pieces of the command
     */
    public String[] decompose(String cmd) {
        int parts = 0;

        for (int i = 0; 0 <= i && i < cmd.length(); ) {
            int next = dashEven(cmd, i);
            if (i == next) {
                parts++;
                i = next + 2;
            } else {
                parts++;
                i = next;
            }
        }

        String part[] = new String[parts];
        int x = 0;

        for (int i = 0; 0 <= i && i < cmd.length(); ) {
            int next = dashEven(cmd, i);
            if (i == next) {
                part[x++] = cmd.substring(i, i + 2);
                i = next + 2;
            } else {
                part[x++] = (next < 0) ? cmd.substring(i) : cmd.substring(i, next);
                i = next;
            }
        }
        return part;
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
        MultiTrie2 m = new MultiTrie2(forward);
        m.tries = h;
        return m;
    }


    /**
     *  Description of the Method
     *
     * @param  after  Description of the Parameter
     * @param  goes   Description of the Parameter
     * @return        Description of the Return Value
     */
    private boolean cannotFollow(char after, char goes) {
        switch (after) {
            case '-':
            case 'D':
                return after == goes;
        }
        return false;
    }


    /**
     *  Description of the Method
     *
     * @param  in     Description of the Parameter
     * @param  count  Description of the Parameter
     * @return        Description of the Return Value
     */
    private String skip(String in, int count) {
        if (forward) {
            return in.substring(count);
        } else {
            return in.substring(0, in.length() - count);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  in    Description of the Parameter
     * @param  from  Description of the Parameter
     * @return       Description of the Return Value
     */
    private int dashEven(String in, int from) {
        while (from < in.length()) {
            if (in.charAt(from) == '-') {
                return from;
            } else {
                from += 2;
            }
        }
        return -1;
    }


    /**
     *  Description of the Method
     *
     * @param  cmd  Description of the Parameter
     * @return      Description of the Return Value
     */
    private int lengthPP(String cmd) {
        int len = 0;
        for (int i = 0; i < cmd.length(); i++) {
            switch (cmd.charAt(i++)) {
                case '-':
                case 'D':
                    len += cmd.charAt(i) - 'a' + 1;
                    break;
                case 'R':
                    len++;
                case 'I':
                    break;
            }
        }
        return len;
    }
}
