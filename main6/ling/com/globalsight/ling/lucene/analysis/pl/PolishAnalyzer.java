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
package com.globalsight.ling.lucene.analysis.pl;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.TokenStream;

import com.globalsight.ling.lucene.analysis.pl.stempel.Stemmer;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * Analyzer that uses Stemmer for stemming. It also uses
 * LowerCaseTokenizer to lowercase input tokens.
 *
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class PolishAnalyzer 
	extends Analyzer 
{
	private Stemmer stemmer = null;

	/** Creates the Analyzer that uses the default stemming table */
	public PolishAnalyzer() 
	{
		stemmer = new Stemmer();
	}

	/** Creates the Analyzer that loads stemming table from the resource path. */
	public PolishAnalyzer(String stemmerTable) 
	{
		stemmer = new Stemmer(stemmerTable);
	}

	/** Creates the Analyzer that uses the supplied Stemmer. */
	public PolishAnalyzer(Stemmer stemmer) 
	{
		this.stemmer = stemmer;
	}

	/**
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String,
	 *      java.io.Reader)
	 */
	protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
	    Tokenizer t = new StandardTokenizer(LuceneUtil.VERSION, reader);
	    LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, t);
	    PolishFilter pf = new PolishFilter(stemmer, lf);
        
        return new TokenStreamComponents(t, pf);
    }
}
