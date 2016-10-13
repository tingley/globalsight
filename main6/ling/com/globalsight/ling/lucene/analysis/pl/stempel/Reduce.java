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

/**
 *  The Reduce object is used to remove gaps in a Trie which stores a
 *  dictionary..
 *
 * @author    Leo Galambos
 */
public class Reduce {

    /**
     *  Constructor for the Reduce object.
     */
    public Reduce() { }


    /**
     *  Optimize (remove holes in the rows) the given Trie and return the
     *  restructured Trie.
     *
     * @param  orig  the Trie to optimize
     * @return       the restructured Trie
     */
    public Trie optimize(Trie orig) {
        Vector cmds = orig.cmds;
        Vector rows = new Vector();
        Vector orows = orig.rows;
        int remap[] = new int[orows.size()];

        Arrays.fill(remap, -1);
        rows = removeGaps(orig.root, rows, new Vector(), remap);

        return new Trie(orig.forward, remap[orig.root], cmds, rows);
    }


    /**
     *  Description of the Method
     *
     * @param  ind    Description of the Parameter
     * @param  old    Description of the Parameter
     * @param  to     Description of the Parameter
     * @param  remap  Description of the Parameter
     * @return        Description of the Return Value
     */
    Vector removeGaps(int ind, Vector old, Vector to, int remap[]) {
        remap[ind] = to.size();

        Row now = (Row) old.elementAt(ind);
        to.addElement(now);
        Iterator i = now.cells.values().iterator();
        for (; i.hasNext(); ) {
            Cell c = (Cell) i.next();
            if (c.ref >= 0 && remap[c.ref] < 0) {
                removeGaps(c.ref, old, to, remap);
            }
        }
        to.setElementAt(new Remap(now, remap), remap[ind]);
        return to;
    }


    /**
     *  This class is part of the Egothor Project
     *
     * @author    Leo Galambos
     */
    class Remap extends Row {
        /**
         *  Constructor for the Remap object
         *
         * @param  old    Description of the Parameter
         * @param  remap  Description of the Parameter
         */
        public Remap(Row old, int remap[]) {
            super();
            Iterator i = old.cells.keySet().iterator();
            for (; i.hasNext(); ) {
                Character ch = (Character) i.next();
                Cell c = old.at(ch);
                Cell nc;
                if (c.ref >= 0) {
                    nc = new Cell(c);
                    nc.ref = remap[nc.ref];
                } else {
                    nc = new Cell(c);
                }
                cells.put(ch, nc);
            }
        }
    }
}
