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
// This file was generated automatically by the Snowball to Java compiler
package com.globalsight.ling.lucene.analysis.snowball.snowball.ext;
import com.globalsight.ling.lucene.analysis.snowball.snowball.SnowballProgram;
import com.globalsight.ling.lucene.analysis.snowball.snowball.Among;

/**
 * Generated class implementing code defined by a snowball script.
 */
public class PorterStemmer extends SnowballProgram {

        private Among a_0[] = {
            new Among ( "s", -1, 3, "", this),
            new Among ( "ies", 0, 2, "", this),
            new Among ( "sses", 0, 1, "", this),
            new Among ( "ss", 0, -1, "", this)
        };

        private Among a_1[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "bb", 0, 2, "", this),
            new Among ( "dd", 0, 2, "", this),
            new Among ( "ff", 0, 2, "", this),
            new Among ( "gg", 0, 2, "", this),
            new Among ( "bl", 0, 1, "", this),
            new Among ( "mm", 0, 2, "", this),
            new Among ( "nn", 0, 2, "", this),
            new Among ( "pp", 0, 2, "", this),
            new Among ( "rr", 0, 2, "", this),
            new Among ( "at", 0, 1, "", this),
            new Among ( "tt", 0, 2, "", this),
            new Among ( "iz", 0, 1, "", this)
        };

        private Among a_2[] = {
            new Among ( "ed", -1, 2, "", this),
            new Among ( "eed", 0, 1, "", this),
            new Among ( "ing", -1, 2, "", this)
        };

        private Among a_3[] = {
            new Among ( "anci", -1, 3, "", this),
            new Among ( "enci", -1, 2, "", this),
            new Among ( "abli", -1, 4, "", this),
            new Among ( "eli", -1, 6, "", this),
            new Among ( "alli", -1, 9, "", this),
            new Among ( "ousli", -1, 12, "", this),
            new Among ( "entli", -1, 5, "", this),
            new Among ( "aliti", -1, 10, "", this),
            new Among ( "biliti", -1, 14, "", this),
            new Among ( "iviti", -1, 13, "", this),
            new Among ( "tional", -1, 1, "", this),
            new Among ( "ational", 10, 8, "", this),
            new Among ( "alism", -1, 10, "", this),
            new Among ( "ation", -1, 8, "", this),
            new Among ( "ization", 13, 7, "", this),
            new Among ( "izer", -1, 7, "", this),
            new Among ( "ator", -1, 8, "", this),
            new Among ( "iveness", -1, 13, "", this),
            new Among ( "fulness", -1, 11, "", this),
            new Among ( "ousness", -1, 12, "", this)
        };

        private Among a_4[] = {
            new Among ( "icate", -1, 2, "", this),
            new Among ( "ative", -1, 3, "", this),
            new Among ( "alize", -1, 1, "", this),
            new Among ( "iciti", -1, 2, "", this),
            new Among ( "ical", -1, 2, "", this),
            new Among ( "ful", -1, 3, "", this),
            new Among ( "ness", -1, 3, "", this)
        };

        private Among a_5[] = {
            new Among ( "ic", -1, 1, "", this),
            new Among ( "ance", -1, 1, "", this),
            new Among ( "ence", -1, 1, "", this),
            new Among ( "able", -1, 1, "", this),
            new Among ( "ible", -1, 1, "", this),
            new Among ( "ate", -1, 1, "", this),
            new Among ( "ive", -1, 1, "", this),
            new Among ( "ize", -1, 1, "", this),
            new Among ( "iti", -1, 1, "", this),
            new Among ( "al", -1, 1, "", this),
            new Among ( "ism", -1, 1, "", this),
            new Among ( "ion", -1, 2, "", this),
            new Among ( "er", -1, 1, "", this),
            new Among ( "ous", -1, 1, "", this),
            new Among ( "ant", -1, 1, "", this),
            new Among ( "ent", -1, 1, "", this),
            new Among ( "ment", 15, 1, "", this),
            new Among ( "ement", 16, 1, "", this),
            new Among ( "ou", -1, 1, "", this)
        };

        private static final char g_v[] = {17, 65, 16, 1 };

        private static final char g_v_WXY[] = {1, 17, 65, 208, 1 };

        private boolean B_Y_found;
        private int I_p2;
        private int I_p1;

        private void copy_from(PorterStemmer other) {
            B_Y_found = other.B_Y_found;
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            super.copy_from(other);
        }

        private boolean r_shortv() {
            // (, line 19
            if (!(out_grouping_b(g_v_WXY, 89, 121)))
            {
                return false;
            }
            if (!(in_grouping_b(g_v, 97, 121)))
            {
                return false;
            }
            if (!(out_grouping_b(g_v, 97, 121)))
            {
                return false;
            }
            return true;
        }

        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_Step_1a() {
            int among_var;
            // (, line 24
            // [, line 25
            ket = cursor;
            // substring, line 25
            among_var = find_among_b(a_0, 4);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 25
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 26
                    // <-, line 26
                    slice_from("ss");
                    break;
                case 2:
                    // (, line 27
                    // <-, line 27
                    slice_from("i");
                    break;
                case 3:
                    // (, line 29
                    // delete, line 29
                    slice_del();
                    break;
            }
            return true;
        }

        private boolean r_Step_1b() {
            int among_var;
            int v_1;
            int v_3;
            int v_4;
            // (, line 33
            // [, line 34
            ket = cursor;
            // substring, line 34
            among_var = find_among_b(a_2, 3);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 34
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 35
                    // call R1, line 35
                    if (!r_R1())
                    {
                        return false;
                    }
                    // <-, line 35
                    slice_from("ee");
                    break;
                case 2:
                    // (, line 37
                    // test, line 38
                    v_1 = limit - cursor;
                    // gopast, line 38
                    golab0: while(true)
                    {
                        lab1: do {
                            if (!(in_grouping_b(g_v, 97, 121)))
                            {
                                break lab1;
                            }
                            break golab0;
                        } while (false);
                        if (cursor <= limit_backward)
                        {
                            return false;
                        }
                        cursor--;
                    }
                    cursor = limit - v_1;
                    // delete, line 38
                    slice_del();
                    // test, line 39
                    v_3 = limit - cursor;
                    // substring, line 39
                    among_var = find_among_b(a_1, 13);
                    if (among_var == 0)
                    {
                        return false;
                    }
                    cursor = limit - v_3;
                    switch(among_var) {
                        case 0:
                            return false;
                        case 1:
                            // (, line 41
                            // <+, line 41
                            {
                                int c = cursor;
                                insert(cursor, cursor, "e");
                                cursor = c;
                            }
                            break;
                        case 2:
                            // (, line 44
                            // [, line 44
                            ket = cursor;
                            // next, line 44
                            if (cursor <= limit_backward)
                            {
                                return false;
                            }
                            cursor--;
                            // ], line 44
                            bra = cursor;
                            // delete, line 44
                            slice_del();
                            break;
                        case 3:
                            // (, line 45
                            // atmark, line 45
                            if (cursor != I_p1)
                            {
                                return false;
                            }
                            // test, line 45
                            v_4 = limit - cursor;
                            // call shortv, line 45
                            if (!r_shortv())
                            {
                                return false;
                            }
                            cursor = limit - v_4;
                            // <+, line 45
                            {
                                int c = cursor;
                                insert(cursor, cursor, "e");
                                cursor = c;
                            }
                            break;
                    }
                    break;
            }
            return true;
        }

        private boolean r_Step_1c() {
            int v_1;
            // (, line 51
            // [, line 52
            ket = cursor;
            // or, line 52
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    // literal, line 52
                    if (!(eq_s_b(1, "y")))
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                // literal, line 52
                if (!(eq_s_b(1, "Y")))
                {
                    return false;
                }
            } while (false);
            // ], line 52
            bra = cursor;
            // gopast, line 53
            golab2: while(true)
            {
                lab3: do {
                    if (!(in_grouping_b(g_v, 97, 121)))
                    {
                        break lab3;
                    }
                    break golab2;
                } while (false);
                if (cursor <= limit_backward)
                {
                    return false;
                }
                cursor--;
            }
            // <-, line 54
            slice_from("i");
            return true;
        }

        private boolean r_Step_2() {
            int among_var;
            // (, line 57
            // [, line 58
            ket = cursor;
            // substring, line 58
            among_var = find_among_b(a_3, 20);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 58
            bra = cursor;
            // call R1, line 58
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 59
                    // <-, line 59
                    slice_from("tion");
                    break;
                case 2:
                    // (, line 60
                    // <-, line 60
                    slice_from("ence");
                    break;
                case 3:
                    // (, line 61
                    // <-, line 61
                    slice_from("ance");
                    break;
                case 4:
                    // (, line 62
                    // <-, line 62
                    slice_from("able");
                    break;
                case 5:
                    // (, line 63
                    // <-, line 63
                    slice_from("ent");
                    break;
                case 6:
                    // (, line 64
                    // <-, line 64
                    slice_from("e");
                    break;
                case 7:
                    // (, line 66
                    // <-, line 66
                    slice_from("ize");
                    break;
                case 8:
                    // (, line 68
                    // <-, line 68
                    slice_from("ate");
                    break;
                case 9:
                    // (, line 69
                    // <-, line 69
                    slice_from("al");
                    break;
                case 10:
                    // (, line 71
                    // <-, line 71
                    slice_from("al");
                    break;
                case 11:
                    // (, line 72
                    // <-, line 72
                    slice_from("ful");
                    break;
                case 12:
                    // (, line 74
                    // <-, line 74
                    slice_from("ous");
                    break;
                case 13:
                    // (, line 76
                    // <-, line 76
                    slice_from("ive");
                    break;
                case 14:
                    // (, line 77
                    // <-, line 77
                    slice_from("ble");
                    break;
            }
            return true;
        }

        private boolean r_Step_3() {
            int among_var;
            // (, line 81
            // [, line 82
            ket = cursor;
            // substring, line 82
            among_var = find_among_b(a_4, 7);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 82
            bra = cursor;
            // call R1, line 82
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 83
                    // <-, line 83
                    slice_from("al");
                    break;
                case 2:
                    // (, line 85
                    // <-, line 85
                    slice_from("ic");
                    break;
                case 3:
                    // (, line 87
                    // delete, line 87
                    slice_del();
                    break;
            }
            return true;
        }

        private boolean r_Step_4() {
            int among_var;
            int v_1;
            // (, line 91
            // [, line 92
            ket = cursor;
            // substring, line 92
            among_var = find_among_b(a_5, 19);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 92
            bra = cursor;
            // call R2, line 92
            if (!r_R2())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 95
                    // delete, line 95
                    slice_del();
                    break;
                case 2:
                    // (, line 96
                    // or, line 96
                    lab0: do {
                        v_1 = limit - cursor;
                        lab1: do {
                            // literal, line 96
                            if (!(eq_s_b(1, "s")))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_1;
                        // literal, line 96
                        if (!(eq_s_b(1, "t")))
                        {
                            return false;
                        }
                    } while (false);
                    // delete, line 96
                    slice_del();
                    break;
            }
            return true;
        }

        private boolean r_Step_5a() {
            int v_1;
            int v_2;
            // (, line 100
            // [, line 101
            ket = cursor;
            // literal, line 101
            if (!(eq_s_b(1, "e")))
            {
                return false;
            }
            // ], line 101
            bra = cursor;
            // or, line 102
            lab0: do {
                v_1 = limit - cursor;
                lab1: do {
                    // call R2, line 102
                    if (!r_R2())
                    {
                        break lab1;
                    }
                    break lab0;
                } while (false);
                cursor = limit - v_1;
                // (, line 102
                // call R1, line 102
                if (!r_R1())
                {
                    return false;
                }
                // not, line 102
                {
                    v_2 = limit - cursor;
                    lab2: do {
                        // call shortv, line 102
                        if (!r_shortv())
                        {
                            break lab2;
                        }
                        return false;
                    } while (false);
                    cursor = limit - v_2;
                }
            } while (false);
            // delete, line 103
            slice_del();
            return true;
        }

        private boolean r_Step_5b() {
            // (, line 106
            // [, line 107
            ket = cursor;
            // literal, line 107
            if (!(eq_s_b(1, "l")))
            {
                return false;
            }
            // ], line 107
            bra = cursor;
            // call R2, line 108
            if (!r_R2())
            {
                return false;
            }
            // literal, line 108
            if (!(eq_s_b(1, "l")))
            {
                return false;
            }
            // delete, line 109
            slice_del();
            return true;
        }

        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_10;
            int v_11;
            int v_12;
            int v_13;
            int v_14;
            int v_15;
            int v_16;
            int v_17;
            int v_18;
            int v_19;
            int v_20;
            // (, line 113
            // unset Y_found, line 115
            B_Y_found = false;
            // do, line 116
            v_1 = cursor;
            lab0: do {
                // (, line 116
                // [, line 116
                bra = cursor;
                // literal, line 116
                if (!(eq_s(1, "y")))
                {
                    break lab0;
                }
                // ], line 116
                ket = cursor;
                // <-, line 116
                slice_from("Y");
                // set Y_found, line 116
                B_Y_found = true;
            } while (false);
            cursor = v_1;
            // do, line 117
            v_2 = cursor;
            lab1: do {
                // repeat, line 117
                replab2: while(true)
                {
                    v_3 = cursor;
                    lab3: do {
                        // (, line 117
                        // goto, line 117
                        golab4: while(true)
                        {
                            v_4 = cursor;
                            lab5: do {
                                // (, line 117
                                if (!(in_grouping(g_v, 97, 121)))
                                {
                                    break lab5;
                                }
                                // [, line 117
                                bra = cursor;
                                // literal, line 117
                                if (!(eq_s(1, "y")))
                                {
                                    break lab5;
                                }
                                // ], line 117
                                ket = cursor;
                                cursor = v_4;
                                break golab4;
                            } while (false);
                            cursor = v_4;
                            if (cursor >= limit)
                            {
                                break lab3;
                            }
                            cursor++;
                        }
                        // <-, line 117
                        slice_from("Y");
                        // set Y_found, line 117
                        B_Y_found = true;
                        continue replab2;
                    } while (false);
                    cursor = v_3;
                    break replab2;
                }
            } while (false);
            cursor = v_2;
            I_p1 = limit;
            I_p2 = limit;
            // do, line 121
            v_5 = cursor;
            lab6: do {
                // (, line 121
                // gopast, line 122
                golab7: while(true)
                {
                    lab8: do {
                        if (!(in_grouping(g_v, 97, 121)))
                        {
                            break lab8;
                        }
                        break golab7;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // gopast, line 122
                golab9: while(true)
                {
                    lab10: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab10;
                        }
                        break golab9;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // setmark p1, line 122
                I_p1 = cursor;
                // gopast, line 123
                golab11: while(true)
                {
                    lab12: do {
                        if (!(in_grouping(g_v, 97, 121)))
                        {
                            break lab12;
                        }
                        break golab11;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // gopast, line 123
                golab13: while(true)
                {
                    lab14: do {
                        if (!(out_grouping(g_v, 97, 121)))
                        {
                            break lab14;
                        }
                        break golab13;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // setmark p2, line 123
                I_p2 = cursor;
            } while (false);
            cursor = v_5;
            // backwards, line 126
            limit_backward = cursor; cursor = limit;
            // (, line 126
            // do, line 127
            v_10 = limit - cursor;
            lab15: do {
                // call Step_1a, line 127
                if (!r_Step_1a())
                {
                    break lab15;
                }
            } while (false);
            cursor = limit - v_10;
            // do, line 128
            v_11 = limit - cursor;
            lab16: do {
                // call Step_1b, line 128
                if (!r_Step_1b())
                {
                    break lab16;
                }
            } while (false);
            cursor = limit - v_11;
            // do, line 129
            v_12 = limit - cursor;
            lab17: do {
                // call Step_1c, line 129
                if (!r_Step_1c())
                {
                    break lab17;
                }
            } while (false);
            cursor = limit - v_12;
            // do, line 130
            v_13 = limit - cursor;
            lab18: do {
                // call Step_2, line 130
                if (!r_Step_2())
                {
                    break lab18;
                }
            } while (false);
            cursor = limit - v_13;
            // do, line 131
            v_14 = limit - cursor;
            lab19: do {
                // call Step_3, line 131
                if (!r_Step_3())
                {
                    break lab19;
                }
            } while (false);
            cursor = limit - v_14;
            // do, line 132
            v_15 = limit - cursor;
            lab20: do {
                // call Step_4, line 132
                if (!r_Step_4())
                {
                    break lab20;
                }
            } while (false);
            cursor = limit - v_15;
            // do, line 133
            v_16 = limit - cursor;
            lab21: do {
                // call Step_5a, line 133
                if (!r_Step_5a())
                {
                    break lab21;
                }
            } while (false);
            cursor = limit - v_16;
            // do, line 134
            v_17 = limit - cursor;
            lab22: do {
                // call Step_5b, line 134
                if (!r_Step_5b())
                {
                    break lab22;
                }
            } while (false);
            cursor = limit - v_17;
            cursor = limit_backward;            // do, line 137
            v_18 = cursor;
            lab23: do {
                // (, line 137
                // Boolean test Y_found, line 137
                if (!(B_Y_found))
                {
                    break lab23;
                }
                // repeat, line 137
                replab24: while(true)
                {
                    v_19 = cursor;
                    lab25: do {
                        // (, line 137
                        // goto, line 137
                        golab26: while(true)
                        {
                            v_20 = cursor;
                            lab27: do {
                                // (, line 137
                                // [, line 137
                                bra = cursor;
                                // literal, line 137
                                if (!(eq_s(1, "Y")))
                                {
                                    break lab27;
                                }
                                // ], line 137
                                ket = cursor;
                                cursor = v_20;
                                break golab26;
                            } while (false);
                            cursor = v_20;
                            if (cursor >= limit)
                            {
                                break lab25;
                            }
                            cursor++;
                        }
                        // <-, line 137
                        slice_from("y");
                        continue replab24;
                    } while (false);
                    cursor = v_19;
                    break replab24;
                }
            } while (false);
            cursor = v_18;
            return true;
        }

}

