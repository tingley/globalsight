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
package com.globalsight.ling.lucene.analysis.es;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

import com.globalsight.ling.lucene.analysis.snowball.SnowballAnalyzer;

import java.io.Reader;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter},
 * {@link LowerCaseFilter}, {@link StopFilter} and {@link SnowballFilter}.
 *
 * Available stemmers are listed in {@link net.sf.snowball.ext}.  The
 * name of a stemmer is the part of the class name before "Stemmer",
 * e.g., the stemmer in {@link EnglishStemmer} is named "English".
 */
public class SpanishAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "de", "la", "que", "el", "en", "y", "a", "los", "del", "se",
    "las", "por", "un", "para", "con", "no", "una", "su", "al",
    "lo", "como", "m�s", "pero", "sus", "le", "ya", "o", "este",
    "s�", "porque", "esta", "entre", "cuando", "muy", "sin", "sobre",
    "tambi�n", "me", "hasta", "hay", "donde", "quien", "desde", "todo",
    "nos", "durante", "todos", "uno", "les", "ni", "contra", "otros",
    "ese", "eso", "ante", "ellos", "e", "esto", "m�", "antes", "algunos",
    "qu�", "unos", "yo", "otro", "otras", "otra", "�l", "tanto", "esa",
    "estos", "mucho", "quienes", "nada", "muchos", "cual", "poco", "ella",
    "estar", "estas", "algunas", "algo", "nosotros",
    //  | es         from SER
    //  | fue        from SER
    //  | ha         from HABER
    //  | son        from SER
    //  | est�       from ESTAR
    //  | ser        from SER
    //  | tiene      from TENER
    //  | han        from HABER
    //  | est�n      from ESTAR
    //  | estado     from ESTAR
    //  | estados    from ESTAR
    //  | fueron     from SER
    //  | hab�a      from HABER
    //  | sea        from SER
    //  | haber      from HABER
    //  | estaba     from ESTAR
    //  | estamos    from ESTAR
    //      | other forms
    "mi", "mis", "t�", "te", "ti", "tu", "tus", "ellas",
    "nosotras", "vosostros", "vosostras", "os", "m�o", "m�a",
    "m�os", "m�as", "tuyo", "tuya", "tuyos", "tuyas", "suyo",
    "suya", "suyos", "suyas", "nuestro", "nuestra", "nuestros",
    "nuestras", "vuestro", "vuestra", "vuestros", "vuestras",
    "esos", "esas",
    // | forms of estar, to be (not including the infinitive):
    "estoy", "est�s", "est�", "estamos", "est�is", "est�n",
    "est�", "est�s", "estemos", "est�is", "est�n", "estar�",
    "estar�s", "estar�", "estaremos", "estar�is", "estar�n",
    "estar�a", "estar�as", "estar�amos", "estar�ais", "estar�an",
    "estaba", "estabas", "est�bamos", "estabais", "estaban",
    "estuve", "estuviste", "estuvo", "estuvimos", "estuvisteis",
    "estuvieron", "estuviera", "estuvieras", "estuvi�ramos",
    "estuvierais", "estuvieran", "estuviese", "estuvieses",
    "estuvi�semos", "estuvieseis", "estuviesen", "estando",
    "estado", "estada", "estados", "estadas", "estad",
    // | forms of haber, to have (not including the infinitive):
    "he", "has", "ha", "hemos", "hab�is", "han", "haya", "hayas", "hayamos",
    "hay�is", "hayan", "habr�", "habr�s", "habr�", "habremos", "habr�is",
    "habr�n", "habr�a", "habr�as", "habr�amos", "habr�ais", "habr�an",
    "hab�a", "hab�as", "hab�amos", "hab�ais", "hab�an", "hube", "hubiste",
    "hubo", "hubimos", "hubisteis", "hubieron", "hubiera", "hubieras",
    "hubi�ramos", "hubierais", "hubieran", "hubiese", "hubieses",
    "hubi�semos", "hubieseis", "hubiesen", "habiendo", "habido",
    "habida", "habidos", "habidas",
    // | forms of ser, to be (not including the infinitive):
    "soy", "eres", "es", "somos", "sois", "son", "sea", "seas",
    "seamos", "se�is", "sean", "ser�", "ser�s", "ser�", "seremos",
    "ser�is", "ser�n", "ser�a", "ser�as", "ser�amos", "ser�ais", "ser�an",
    "era", "eras", "�ramos", "erais", "eran", "fui", "fuiste", "fue",
    "fuimos", "fuisteis", "fueron", "fuera", "fueras", "fu�ramos",
    "fuerais", "fueran", "fuese", "fueses", "fu�semos", "fueseis",
    "fuesen", "sintiendo", "sentido", "sentida", "sentidos", "sentidas",
    "siente", "sentid",
    // | forms of tener, to have (not including the infinitive):
    "tengo", "tienes", "tiene", "tenemos", "ten�is", "tienen",
    "tenga", "tengas", "tengamos", "teng�is", "tengan", "tendr�",
    "tendr�s", "tendr�", "tendremos", "tendr�is", "tendr�n", "tendr�a",
    "tendr�as", "tendr�amos", "tendr�ais", "tendr�an", "ten�a", "ten�as",
    "ten�amos", "ten�ais", "ten�an", "tuve", "tuviste", "tuvo", "tuvimos",
    "tuvisteis", "tuvieron", "tuviera", "tuvieras", "tuvi�ramos",
    "tuvierais", "tuvieran", "tuviese", "tuvieses", "tuvi�semos",
    "tuvieseis", "tuviesen", "teniendo", "tenido", "tenida", "tenidos",
    "tenidas", "tened",
    };

    public SpanishAnalyzer()
    {
        super("Spanish", s_stopwords);
    }
}
