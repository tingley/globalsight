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

package spell;

import java.io.*;
import java.util.*;

public class test
{
    static public void main(String[] args)
        throws IOException
    {
        Locale locale = new Locale("ru", "RU");

        SpellIndex index = SpellIndexList.getSpellIndex(locale.toString());
        if (index.getWordCount() == 0)
        {
            System.out.println("Creating index " + locale);
            index.clearIndex();
            index.indexDictionary(new FileDictionary(new File("russian.txt")));
        }

        System.out.println("Index " + locale + " contains " +
            index.getWordCount() + " words");

        index = SpellIndexList.getSpellIndex("cvdl-" + locale);
        if (index.getWordCount() == 0)
        {
            System.out.println("Creating custom index " + "cvdl-" + locale);
            index.clearIndex();
            index.indexDictionary( // 153:159
                new StringDictionary("\u0431\u0443\u043a\u0432\u0430\u043c"));
        }

        System.out.println("Index " + "cvdl-" + locale + " contains " +
            index.getWordCount() + " words");

        System.out.println("Querying index " + locale + " and custom dict " +
            "cvdl-" + locale);

        ArrayList indexes = new ArrayList();
        indexes.add(locale.toString());
        indexes.add("cvdl-" + locale);

        SpellChecker sc = new SpellChecker(indexes, locale);
        SpellCheckResult result = sc.doSpell("\u042d\u0442\u043e\u0442 \u0432\u0430\u0440\u0438\u0430\u043d\u0442 applet \u043d\u0435\u0442 \u0438\u0434\u0435\u0430\u043b\u044c\u043d\u043e \u0434\u043b\u044f \u043c\u0435\u0441\u0442 \u043d\u0435 \u0436\u0435\u043b\u0430\u044e\u0442 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c applets, \u043f\u0440\u043e\u0441\u0442\u043e \u043f\u043e\u0432\u044b\u0448\u0435\u043d\u0438\u0435 \u0446\u0435\u043d\u044b html \u0431\u0443\u0434\u0435\u0442 \u043f\u0440\u0435\u0434\u0441\u0442\u0430\u0432\u043b\u0435\u043d \u0432 \u043e\u043a\u043d\u0435 \u043a\u043e\u043d\u0442\u0440\u043e\u043b\u0435\u0440\u0430 \u043f\u0440\u043e\u0438\u0437\u043d\u043e\u0448\u0435\u043d\u0438\u044f \u043f\u043e \u0431\u0443\u043a\u0432\u0430\u043c. \u042f \u043b\u044e\u0431\u043b\u044e \u043a\u043e\u043d\u0442\u0440\u043e\u043b\u0435\u0440\u0430 \u043f\u0440\u043e\u0438\u0437\u043d\u043e\u0448\u0435\u043d\u0438\u044f \u043f\u043e \u0431\u0443\u043a\u0432\u0430\u043c. \u0430\u0431\u0441\u043e\u043b\u044e\u0442\u043d\u0430\u044f.");

        printResult(result);

        System.out.println("\n\nAdding term 'test' to custom dict");
        index.addWord("test");

        System.out.println("Listing terms from custom dict");
        ArrayList words = index.getWords();
        for (int i = 0, max = words.size(); i < max; i++)
        {
            String word = (String)words.get(i);
            System.out.println("\tword = " + EditUtil.toJavascript(word));
        }

        System.out.println("\n\nRemoving term 'test' from custom dict");
        index.removeWord("test");

        System.out.println("Listing terms from custom dict");
        words = index.getWords();
        for (int i = 0, max = words.size(); i < max; i++)
        {
            String word = (String)words.get(i);
            System.out.println("\tword = " + EditUtil.toJavascript(word));
        }

        System.out.println("\nIndex " + "cvdl-" + locale + " contains " +
            index.getWordCount() + " words");

        result = sc.doSpell("\u0431\u0443\u043a\u0432\u0430\u043c");
        printResult(result);
    }

    static private void printResult(SpellCheckResult p_result)
    {
        System.out.println("Spellcheck locale " + p_result.m_locale +
            " text = `" + EditUtil.toJavascript(p_result.m_text) + "'");

        ArrayList words = p_result.getWords();
        for (int ii = 0, imax = words.size(); ii < imax; ii++)
        {
            Word word = (Word)words.get(ii);

            System.out.println("Word `" + EditUtil.toJavascript(word.m_word) +
                "' " + "(" + word.m_start + ":" + word.m_end + ")" +
                (word.isCorrect() ? " CORRECT" : ""));

            if (!word.isCorrect())
            {
                ArrayList suggestions = word.getSuggestions();
                for (int jj = 0, jmax = suggestions.size(); jj < jmax; jj++)
                {
                    String sugg = (String)suggestions.get(jj);

                    System.out.println("\t`" + EditUtil.toJavascript(sugg) + "'");
                }
            }
        }
    }
}
