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
package com.globalsight.ling.lucene.analysis.th;

import java.io.*;

import com.globalsight.ling.lucene.analysis.GSTokenizer;

/** A grammar-based tokenizer constructed with JavaCC.
 *
 * <p> This should be a good tokenizer for most European-language documents.
 *
 * <p>Many applications have specific tokenizer needs.  If this tokenizer does
 * not suit your application, please consider copying this source code
 * directory to your project and maintaining your own grammar-based tokenizer.
 */
public class ThaiTokenizer extends GSTokenizer
    implements ThaiTokenizerConstants
{

    /** Constructs a tokenizer for this Reader. */
    public ThaiTokenizer(Reader reader) {
        this(new FastCharStream(reader));
        this.input = reader;
    }

    /** Returns the next token in the stream, or null at EOS.
     * <p>The returned token's type is set to an element of {@link
     * ThaiTokenizerConstants#tokenImage}.
     */
    final public org.apache.lucene.analysis.Token next() throws IOException
    {
        try
        {
            Token token = null;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case THAI:
            token = jj_consume_token(THAI);
            break;
        case ALPHANUM:
            token = jj_consume_token(ALPHANUM);
            break;
        case APOSTROPHE:
            token = jj_consume_token(APOSTROPHE);
            break;
        case ACRONYM:
            token = jj_consume_token(ACRONYM);
            break;
        case COMPANY:
            token = jj_consume_token(COMPANY);
            break;
        case EMAIL:
            token = jj_consume_token(EMAIL);
            break;
        case HOST:
            token = jj_consume_token(HOST);
            break;
        case NUM:
            token = jj_consume_token(NUM);
            break;
        case CJK:
            token = jj_consume_token(CJK);
            break;
        case 0:
            token = jj_consume_token(0);
            break;
        default:
            jj_la1[0] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
        }
        if (token.kind == EOF) {
            {if (true) return null;}
        } else {
            {if (true) return
                           new org.apache.lucene.analysis.Token(token.image,
                               token.beginColumn,token.endColumn,
                               tokenImage[token.kind]);}
        }
        }
        catch (ParseException ex)
        {
            throw new IOException(ex.toString());
        }

        throw new Error("Missing return statement in function");
    }

    public ThaiTokenizerTokenManager token_source;
    public Token token, jj_nt;
    private int jj_ntk;
    private int jj_gen;
    final private int[] jj_la1 = new int[1];
    static private int[] jj_la1_0;
    static {
        jj_la1_0();
    }
    private static void jj_la1_0() {
        jj_la1_0 = new int[] {0x43fb,};
    }

    public ThaiTokenizer(CharStream stream) {
        super(null);
        token_source = new ThaiTokenizerTokenManager(stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 1; i++) jj_la1[i] = -1;
    }

    public void ReInit(CharStream stream) {
        token_source.ReInit(stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 1; i++) jj_la1[i] = -1;
    }

    public ThaiTokenizer(ThaiTokenizerTokenManager tm) {
        super(null);
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 1; i++) jj_la1[i] = -1;
    }

    public void ReInit(ThaiTokenizerTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 1; i++) jj_la1[i] = -1;
    }

    final private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    final public Token getNextToken() {
        if (token.next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next;
            else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    final private int jj_ntk() {
        if ((jj_nt=token.next) == null)
            return (jj_ntk = (token.next=token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    private java.util.Vector jj_expentries = new java.util.Vector();
    private int[] jj_expentry;
    private int jj_kind = -1;

    public ParseException generateParseException() {
        jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[17];
        for (int i = 0; i < 17; i++) {
            la1tokens[i] = false;
        }
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 1; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1<<j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 17; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.addElement(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = (int[])jj_expentries.elementAt(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    final public void enable_tracing() {
    }

    final public void disable_tracing() {
    }

}
