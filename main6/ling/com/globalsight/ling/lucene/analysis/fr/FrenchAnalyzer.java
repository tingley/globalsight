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
package com.globalsight.ling.lucene.analysis.fr;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.lucene.analysis.WordlistLoader;
import com.globalsight.ling.lucene.analysis.cjk.CJKTokenizer;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Analyzer for french language. Supports an external list of
 * stopwords (words that will not be indexed at all) and an external
 * list of exclusions (word that will not be stemmed, but indexed).  A
 * default set of stopwords is used unless an other list is specified,
 * the exclusionlist is empty by default.
 *
 * @author Patrick Talbot (based on Gerhard Schwarz work for German)
 * @version $Id: FrenchAnalyzer.java,v 1.2 2013/09/13 06:22:16 wayne Exp $
 */
public final class FrenchAnalyzer
    extends Analyzer
{

    /**
     * Extended list of typical french stopwords.
     */
    private String[] FRENCH_STOP_WORDS = {
    "a", "afin", "ai", "ainsi", "apr�s", "attendu", "au", "aujourd", "auquel", "aussi",
    "autre", "autres", "aux", "auxquelles", "auxquels", "avait", "avant", "avec", "avoir",
    "c", "car", "ce", "ceci", "cela", "celle", "celles", "celui", "cependant", "certain",
    "certaine", "certaines", "certains", "ces", "cet", "cette", "ceux", "chez", "ci",
    "combien", "comme", "comment", "concernant", "contre", "d", "dans", "de", "debout",
    "dedans", "dehors", "del�", "depuis", "derri�re", "des", "d�sormais", "desquelles",
    "desquels", "dessous", "dessus", "devant", "devers", "devra", "divers", "diverse",
    "diverses", "doit", "donc", "dont", "du", "duquel", "durant", "d�s", "elle", "elles",
    "en", "entre", "environ", "est", "et", "etc", "etre", "eu", "eux", "except�", "hormis",
    "hors", "h�las", "hui", "il", "ils", "j", "je", "jusqu", "jusque", "l", "la", "laquelle",
    "le", "lequel", "les", "lesquelles", "lesquels", "leur", "leurs", "lorsque", "lui", "l�",
    "ma", "mais", "malgr�", "me", "merci", "mes", "mien", "mienne", "miennes", "miens", "moi",
    "moins", "mon", "moyennant", "m�me", "m�mes", "n", "ne", "ni", "non", "nos", "notre",
    "nous", "n�anmoins", "n�tre", "n�tres", "on", "ont", "ou", "outre", "o�", "par", "parmi",
    "partant", "pas", "pass�", "pendant", "plein", "plus", "plusieurs", "pour", "pourquoi",
    "proche", "pr�s", "puisque", "qu", "quand", "que", "quel", "quelle", "quelles", "quels",
    "qui", "quoi", "quoique", "revoici", "revoil�", "s", "sa", "sans", "sauf", "se", "selon",
    "seront", "ses", "si", "sien", "sienne", "siennes", "siens", "sinon", "soi", "soit",
    "son", "sont", "sous", "suivant", "sur", "ta", "te", "tes", "tien", "tienne", "tiennes",
    "tiens", "toi", "ton", "tous", "tout", "toute", "toutes", "tu", "un", "une", "va", "vers",
    "voici", "voil�", "vos", "votre", "vous", "vu", "v�tre", "v�tres", "y", "�", "�a", "�s",
    "�t�", "�tre", "�"
    };

    /**
     * Contains the stopwords used with the StopFilter.
     */
    private CharArraySet stoptable = LuceneUtil.newCharArraySet();
    /**
     * Contains words that should be indexed but not stemmed.
     */
    private CharArraySet excltable = LuceneUtil.newCharArraySet();

    /**
     * Builds an analyzer.
     */
    public FrenchAnalyzer()
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, FRENCH_STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public FrenchAnalyzer(String[] stopwords)
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, stopwords);
    }

    /**
     * Builds an analyzer with the given stop words.
     * @throws IOException
     */
    public FrenchAnalyzer(File stopwords)
        throws IOException
    {
        stoptable = WordlistLoader.getWordSet(stopwords);
    }

    /**
     * Builds an exclusionlist from an array of Strings.
     */
    public void setStemExclusionTable(String[] exclusionlist)
    {
        excltable = StopFilter.makeStopSet(LuceneUtil.VERSION, exclusionlist);
    }

    /**
     * Builds an exclusionlist from the words contained in the given file.
     * @throws IOException
     */
    public void setStemExclusionTable(File exclusionlist)
        throws IOException
    {
        excltable = WordlistLoader.getWordSet(exclusionlist);
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the
     * provided Reader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered
     * with StandardFilter, StopFilter, FrenchStemFilter and
     * LowerCaseFilter
     */
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new StandardTokenizer(LuceneUtil.VERSION, reader);
        
        StandardFilter f = new StandardFilter(LuceneUtil.VERSION, t);
        StopFilter ts = new StopFilter(LuceneUtil.VERSION, f, stoptable);
        FrenchStemFilter gf = new FrenchStemFilter(ts, excltable);
        LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, gf);
        
        return new TokenStreamComponents(t, lf);
    }
}
