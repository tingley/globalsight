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
 *  A Trie is used to store a dictionary of words and their stems.
 *  Actually, what is stored are words with their respective patch
 *  commands. A trie can be termed forward (keys read from left to right)
 *  or backward (keys read from right to left). This property will vary
 *  depending on the language for which a Trie is constructed.
 *
 * @author    Leo Galambos
 */
public class Trie {
    Vector rows = new Vector();
    Vector cmds = new Vector();
    int root;

    boolean forward = false;


    /**
     *  Constructor for the Trie object.
     *
     * @param  is               the input stream
     * @exception  IOException  if an I/O error occurs
     */
    public Trie(DataInput is) throws IOException {
        forward = is.readBoolean();
        root = is.readInt();
        for (int i = is.readInt(); i > 0; i--) {
            cmds.addElement(is.readUTF());
        }
        for (int i = is.readInt(); i > 0; i--) {
            rows.addElement(new Row(is));
        }
    }


    /**
     *  Constructor for the Trie object.
     *
     * @param  forward  set to <tt>true</tt>
     */
    public Trie(boolean forward) {
        rows.addElement(new Row());
        root = 0;
        this.forward = forward;
    }


    /**
     *  Constructor for the Trie object.
     *
     * @param  forward  <tt>true</tt> if read left to right, <tt>false</tt>
     *      if read right to left
     * @param  root     index of the row that is the root node
     * @param  cmds     the patch commands to store
     * @param  rows     a Vector of Vectors. Each inner Vector is a node of
     *      this Trie
     */
    public Trie(boolean forward, int root, Vector cmds, Vector rows) {
        this.rows = rows;
        this.cmds = cmds;
        this.root = root;
        this.forward = forward;
    }


    /**
     *  Gets the all attribute of the Trie object
     *
     * @param  key  Description of the Parameter
     * @return      The all value
     */
    public String[] getAll(String key) {
        int res[] = new int[key.length()];
        int resc = 0;
        Row now = getRow(root);
        int w;
        StrEnum e = new StrEnum(key, forward);
        boolean br = false;

        for (int i = 0; i < key.length() - 1; i++) {
            Character ch = new Character(e.next());
            w = now.getCmd(ch);
            if (w >= 0) {
                int n = w;
                for (int j = 0; j < resc; j++) {
                    if (n == res[j]) {
                        n = -1;
                        break;
                    }
                }
                if (n >= 0) {
                    res[resc++] = n;
                }
            }
            w = now.getRef(ch);
            if (w >= 0) {
                now = getRow(w);
            } else {
                br = true;
                break;
            }
        }
        if (br == false) {
            w = now.getCmd(new Character(e.next()));
            if (w >= 0) {
                int n = w;
                for (int j = 0; j < resc; j++) {
                    if (n == res[j]) {
                        n = -1;
                        break;
                    }
                }
                if (n >= 0) {
                    res[resc++] = n;
                }
            }
        }

        if (resc < 1) {
            return null;
        }
        String R[] = new String[resc];
        for (int j = 0; j < resc; j++) {
            R[j] = (String) cmds.elementAt(res[j]);
        }
        return R;
    }


    /**
     *  Return the number of cells in this Trie object.
     *
     * @return    the number of cells
     */
    public int getCells() {
        int size = 0;
        Enumeration e = rows.elements();
        while (e.hasMoreElements()) {
            size += ((Row) e.nextElement()).getCells();
        }
        return size;
    }


    /**
     *  Gets the cellsPnt attribute of the Trie object
     *
     * @return    The cellsPnt value
     */
    public int getCellsPnt() {
        int size = 0;
        Enumeration e = rows.elements();
        while (e.hasMoreElements()) {
            size += ((Row) e.nextElement()).getCellsPnt();
        }
        return size;
    }


    /**
     *  Gets the cellsVal attribute of the Trie object
     *
     * @return    The cellsVal value
     */
    public int getCellsVal() {
        int size = 0;
        Enumeration e = rows.elements();
        while (e.hasMoreElements()) {
            size += ((Row) e.nextElement()).getCellsVal();
        }
        return size;
    }


    /**
     *  Return the element that is stored in a cell associated with the
     *  given key.
     *
     * @param  key  the key
     * @return      the associated element
     */
    public String getFully(String key) {
        Row now = getRow(root);
        int w;
        Cell c;
        int cmd = -1;
        StrEnum e = new StrEnum(key, forward);
        Character ch = null;
        Character aux = null;

        for (int i = 0; i < key.length(); ) {
            ch = new Character(e.next());
            i++;

            c = now.at(ch);
            if (c == null) {
                return null;
            }

            cmd = c.cmd;

            for (int skip = c.skip; skip > 0; skip--) {
                if (i < key.length()) {
                    aux = new Character(e.next());
                } else {
                    return null;
                }
                i++;
            }

            w = now.getRef(ch);
            if (w >= 0) {
                now = getRow(w);
            } else if (i < key.length()) {
                return null;
            }
        }
        return (cmd == -1) ? null : (String) cmds.elementAt(cmd);
    }


    /**
     *  Return the element that is stored as last on a path associated with
     *  the given key.
     *
     * @param  key  the key associated with the desired element
     * @return      the last on path element
     */
    public String getLastOnPath(String key) {
        Row now = getRow(root);
        int w;
        String last = null;
        StrEnum e = new StrEnum(key, forward);

        for (int i = 0; i < key.length() - 1; i++) {
            Character ch = new Character(e.next());
            w = now.getCmd(ch);
            if (w >= 0) {
                last = (String) cmds.elementAt(w);
            }
            w = now.getRef(ch);
            if (w >= 0) {
                now = getRow(w);
            } else {
                return last;
            }
        }
        w = now.getCmd(new Character(e.next()));
        return (w >= 0) ? (String) cmds.elementAt(w) : last;
    }


    /**
     *  Return the Row at the given index.
     *
     * @param  index  the index containing the desired Row
     * @return        the Row
     */
    private Row getRow(int index) {
        if (index < 0 || index >= rows.size()) {
            return null;
        }
        return (Row) rows.elementAt(index);
    }


    /**
     *  Write this Trie to the given output stream.
     *
     * @param  os               the output stream
     * @exception  IOException  if an I/O error occurs
     */
    public void store(DataOutput os) throws IOException {
        os.writeBoolean(forward);
        os.writeInt(root);
        Enumeration e = cmds.elements();
        os.writeInt(cmds.size());
        while (e.hasMoreElements()) {
            os.writeUTF((String) e.nextElement());
        }
        e = rows.elements();
        os.writeInt(rows.size());
        while (e.hasMoreElements()) {
            Row r = (Row) e.nextElement();
            r.store(os);
        }
    }


    /**
     *  Add the given key associated with the given patch command. If
     *  either parameter is null this method will return without executing.
     *
     * @param  key  the key
     * @param  cmd  the patch command
     */
    public void add(String key, String cmd) {
        if (key == null || cmd == null) {
            return;
        }
        if (cmd.length() == 0) {
            return;
        }
        int id_cmd = cmds.indexOf(cmd);
        if (id_cmd == -1) {
            id_cmd = cmds.size();
            cmds.addElement(cmd);
        }

        int node = root;
        Row r = getRow(node);

        StrEnum e = new StrEnum(key, forward);

        for (int i = 0; i < e.length() - 1; i++) {
            Character ch = new Character(e.next());
            node = r.getRef(ch);
            if (node >= 0) {
                r = getRow(node);
            } else {
                node = rows.size();
                Row n;
                rows.addElement(n = new Row());
                r.setRef(ch, node);
                r = n;
            }
        }
        r.setCmd(new Character(e.next()), id_cmd);
    }


    /**
     *  Remove empty rows from the given Trie and return the newly reduced
     *  Trie.
     *
     * @param  by  the Trie to reduce
     * @return     the newly reduced Trie
     */
    public Trie reduce(Reduce by) {
        return by.optimize(this);
    }


    /**
     *  Description of the Method
     *
     * @param  prefix  Description of the Parameter
     */
    public void printInfo(String prefix) {
        System.out.println(prefix + "nds " + rows.size() + " cmds " +
                cmds.size() + " cells " + getCells() +
                " valcells " + getCellsVal() +
                " pntcells " + getCellsPnt());
    }


    /**
     *  This class is part of the Egothor Project
     *
     * @author    Leo Galambos
     */
    class StrEnum {
        String s;
        int from;
        int by;


        /**
         *  Constructor for the StrEnum object
         *
         * @param  s   Description of the Parameter
         * @param  up  Description of the Parameter
         */
        StrEnum(String s, boolean up) {
            this.s = s;
            if (up) {
                from = 0;
                by = 1;
            } else {
                from = s.length() - 1;
                by = -1;
            }
        }


        /**
         *  Description of the Method
         *
         * @return    Description of the Return Value
         */
        int length() {
            return s.length();
        }


        /**
         *  Description of the Method
         *
         * @return    Description of the Return Value
         */
        char next() {
            char ch = s.charAt(from);
            from += by;
            return ch;
        }
    }
}
