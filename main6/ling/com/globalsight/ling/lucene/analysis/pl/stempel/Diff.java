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
 *  The Diff object generates a patch string. A patch string is actually a
 *  command to a stemmer telling it how to reduce a word to its root. For
 *  example, to reduce the word teacher to its root teach the patch string
 *  Db would be generated. This command tells the stemmer to delete the
 *  last 2 characters from the word teacher to reach the stem (the patch
 *  commands are applied starting from the last character in order to save
 *
 * @author    Leo Galambos
 */
public class Diff {
    int sizex = 0;
    int sizey = 0;
    int net[][];
    int way[][];

    int INSERT;
    int DELETE;
    int REPLACE;
    int NOOP;


    /**
     *  Constructor for the Diff object.
     */
    public Diff() {
        this(1, 1, 1, 0);
    }


    /**
     *  Constructor for the Diff object
     *
     * @param  ins   Description of the Parameter
     * @param  del   Description of the Parameter
     * @param  rep   Description of the Parameter
     * @param  noop  Description of the Parameter
     */
    public Diff(int ins, int del, int rep, int noop) {
        INSERT = ins;
        DELETE = del;
        REPLACE = rep;
        NOOP = noop;
    }


    /**
     *  Apply the given patch string <tt>diff</tt> to the given string <tt>
     *  orig </tt> and return the new String with the patch command
     *  executed..
     *
     * @param  orig  java.lang.StringBuffer Original string
     * @param  diff  java.lang.String Patch string
     * @return       java.lang.StringBuffer New string
     */
    public static StringBuffer apply(StringBuffer orig, String diff) {
        StringBuffer _orig = new StringBuffer(orig.toString());

        try {

            if (diff == null) {
                return _orig;
            }
            if (orig == null) {
                return null;
            }
            int pos = _orig.length() - 1;
            if (pos < 0) {
                return orig;
            }
            // orig == ""
            for (int i = 0; i < diff.length() / 2; i++) {
                char cmd = diff.charAt(2 * i);
                char param = diff.charAt(2 * i + 1);
                int par_num = (param - 'a' + 1);
                switch (cmd) {
                    case '-':
                        pos = pos - par_num + 1;
                        break;
                    case 'R':
                        _orig.setCharAt(pos, param);
                        break;
                    case 'D':
                        int o = pos;
                        pos -= par_num - 1;
                        /*
                         *  delete par_num chars from index pos
                         */
                        //          String s = orig.toString();
                        //s = s.substring( 0, pos ) + s.substring( o + 1 );
                        //orig = new StringBuffer( s );
                        _orig = _orig.delete(pos, o + 1);

                        break;
                    case 'I':
                        _orig.insert(pos += 1, param);
                        break;
                }
                pos--;
            }
            return _orig;
        } catch (StringIndexOutOfBoundsException x) {
//      x.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException x) {
//      x.printStackTrace();
        }
        return orig;
    }


    /**
     *  Construct a patch string that transforms a to b.
     *
     * @param  a  String 1st string
     * @param  b  String 2nd string
     * @return    java.lang.String
     */
    public synchronized String exec(String a, String b) {
        if (a == null || b == null) {
            return null;
        }

        int x;
        int y;
        int maxx;
        int maxy;
        int go[] = new int[4];
        final int X = 1;
        final int Y = 2;
        final int R = 3;
        final int D = 0;

        /*
         *  setup memory if needed => processing speed up
         */
        maxx = a.length() + 1;
        maxy = b.length() + 1;
        if ((maxx >= sizex) || (maxy >= sizey)) {
            sizex = maxx + 8;
            sizey = maxy + 8;
            net = new int[sizex][sizey];
            way = new int[sizex][sizey];
        }

        /*
         *  clear the network
         */
        for (x = 0; x < maxx; x++) {
            for (y = 0; y < maxy; y++) {
                net[x][y] = 0;
            }
        }

        /*
         *  set known persistent values
         */
        for (x = 1; x < maxx; x++) {
            net[x][0] = x;
            way[x][0] = X;
        }
        for (y = 1; y < maxy; y++) {
            net[0][y] = y;
            way[0][y] = Y;
        }

        for (x = 1; x < maxx; x++) {
            for (y = 1; y < maxy; y++) {
                go[X] = net[x - 1][y] + DELETE;
                // way on x costs 1 unit
                go[Y] = net[x][y - 1] + INSERT;
                // way on y costs 1 unit
                go[R] = net[x - 1][y - 1] + REPLACE;
                go[D] = net[x - 1][y - 1] + ((a.charAt(x - 1) == b.charAt(y - 1)) ? NOOP : 100);
                //  diagonal costs 0, when no change
                short min = D;
                if (go[min] >= go[X]) {
                    min = X;
                }
                if (go[min] > go[Y]) {
                    min = Y;
                }
                if (go[min] > go[R]) {
                    min = R;
                }
                way[x][y] = min;
                net[x][y] = (short) go[min];
            }
        }

        // read the patch string
        StringBuffer result = new StringBuffer();
        final char base = 'a' - 1;
        char deletes = base;
        char equals = base;
        for (x = maxx - 1, y = maxy - 1; x + y != 0; ) {
            switch (way[x][y]) {
                case X:
                    if (equals != base) {
                        result.append("-" + (equals));
                        equals = base;
                    }
                    deletes++;
                    x--;
                    break;
                // delete
                case Y:
                    if (deletes != base) {
                        result.append("D" + (deletes));
                        deletes = base;
                    }
                    if (equals != base) {
                        result.append("-" + (equals));
                        equals = base;
                    }
                    result.append('I');
                    result.append(b.charAt(--y));
                    break;
                // insert
                case R:
                    if (deletes != base) {
                        result.append("D" + (deletes));
                        deletes = base;
                    }
                    if (equals != base) {
                        result.append("-" + (equals));
                        equals = base;
                    }
                    result.append('R');
                    result.append(b.charAt(--y));
                    x--;
                    break;
                // replace
                case D:
                    if (deletes != base) {
                        result.append("D" + (deletes));
                        deletes = base;
                    }
                    equals++;
                    x--;
                    y--;
                    break;
                // no change
            }
        }
        if (deletes != base) {
            result.append("D" + (deletes));
            deletes = base;
        }

        return result.toString();
    }
}
