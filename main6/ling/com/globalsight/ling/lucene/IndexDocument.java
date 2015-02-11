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

package com.globalsight.ling.lucene;

import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;

public class IndexDocument
{
	static public final String MAINID = "mainid";
	static public final String SUBID  = "subid";
	static public final String TEXT   = "text";

    private IndexDocument() {}

    static public Document IndexDocument(long p_mainId, long p_subId, 
		String p_text)
    {
        Document result = new Document();
        FieldType ft;

        // Add the main id (tu id, concept id) as a field named
        // "mainid". Use a Keyword field so that the id is stored
        // with the document, and is searchable.
        //result.add(Field.Keyword(MAINID, String.valueOf(p_mainId)));
        ft = new FieldType();
        ft.setTokenized(false);
        ft.setIndexed(false);
        ft.setStored(true);
        result.add(new Field(MAINID, String.valueOf(p_mainId), ft));

        // Add the sub id (tuv id, term id) as a field named
        // "subid". Use a Keyword field so that the id is stored
        // with the document, and is searchable.
        //result.add(Field.Keyword(SUBID, String.valueOf(p_subId)));
        ft = new FieldType();
        ft.setTokenized(false);
        ft.setIndexed(false);
        ft.setStored(true);
        result.add(new Field(SUBID, String.valueOf(p_subId), ft));

        // Add the contents as an UnStored field so it will get
        // tokenized and indexed, but not stored.
        // result.add(Field.UnStored(TEXT, p_text));
        ft = new FieldType();
        ft.setTokenized(true);
        ft.setIndexed(true);
        ft.setStored(false);
        result.add(new Field(TEXT, p_text, ft));

        return result;
    }

    static public Document DataDocument(long p_mainId, long p_subId, 
		String p_text)
    {
        Document result = new Document();
        FieldType ft;

        // Add the main id (tu id, concept id) as a field named
        // "mainid". Use a Keyword field so that the id is stored
        // with the document, and is searchable.
        //result.add(Field.Keyword(MAINID, String.valueOf(p_mainId)));
        ft = new FieldType();
        ft.setTokenized(false);
        ft.setIndexed(false);
        ft.setStored(true);
        result.add(new Field(MAINID, String.valueOf(p_mainId), ft));

        // Add the sub id (tuv id, term id) as a field named
        // "subid". Use a Keyword field so that the id is stored
        // with the document, and is searchable.
        //result.add(Field.Keyword(SUBID, String.valueOf(p_subId)));
        ft = new FieldType();
        ft.setTokenized(false);
        ft.setIndexed(false);
        ft.setStored(true);
        result.add(new Field(SUBID, String.valueOf(p_subId), ft));

        // Add the contents as a Text field so it will get tokenized,
        // indexed, and stored. (Useful for short text but not for
        // long documents.)
        //result.add(Field.Text(TEXT, p_text));
        ft = new FieldType();
        ft.setTokenized(true);
        ft.setIndexed(true);
        ft.setStored(true);
        result.add(new Field(TEXT, p_text, ft));

        return result;
    }
}
