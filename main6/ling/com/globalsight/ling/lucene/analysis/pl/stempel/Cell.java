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

/**
 *  A Cell is a portion of a trie.
 *
 * @author    Leo Galambos
 */
class Cell {
    // next row id in this way
    int ref = -1;
    // command of the cell
    int cmd = -1;
    // how many cmd-s was in subtrie before pack()
    int cnt = 0;
    // how many chars would be discarded from input key in this way
    int skip = 0;


    /**
     *  Constructor for the Cell object.
     */
    Cell() { }


    /**
     *  Construct a Cell using the properties of the given Cell.
     *
     * @param  a  the Cell whose properties will be used
     */
    Cell(Cell a) {
        ref = a.ref;
        cmd = a.cmd;
        cnt = a.cnt;
        skip = a.skip;
    }


    /**
     *  Return a String containing this Cell's attributes.
     *
     * @return    a String representation of this Cell
     */
    public String toString() {
        return "ref(" + ref + ")cmd(" + cmd + ")cnt(" + cnt + ")skp(" + skip + ")";
    }
}
