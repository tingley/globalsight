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
    "lo", "como", "más", "pero", "sus", "le", "ya", "o", "este",
    "sí", "porque", "esta", "entre", "cuando", "muy", "sin", "sobre",
    "también", "me", "hasta", "hay", "donde", "quien", "desde", "todo",
    "nos", "durante", "todos", "uno", "les", "ni", "contra", "otros",
    "ese", "eso", "ante", "ellos", "e", "esto", "mí", "antes", "algunos",
    "qué", "unos", "yo", "otro", "otras", "otra", "él", "tanto", "esa",
    "estos", "mucho", "quienes", "nada", "muchos", "cual", "poco", "ella",
    "estar", "estas", "algunas", "algo", "nosotros",
    //  | es         from SER
    //  | fue        from SER
    //  | ha         from HABER
    //  | son        from SER
    //  | está       from ESTAR
    //  | ser        from SER
    //  | tiene      from TENER
    //  | han        from HABER
    //  | están      from ESTAR
    //  | estado     from ESTAR
    //  | estados    from ESTAR
    //  | fueron     from SER
    //  | había      from HABER
    //  | sea        from SER
    //  | haber      from HABER
    //  | estaba     from ESTAR
    //  | estamos    from ESTAR
    //      | other forms
    "mi", "mis", "tú", "te", "ti", "tu", "tus", "ellas",
    "nosotras", "vosostros", "vosostras", "os", "mío", "mía",
    "míos", "mías", "tuyo", "tuya", "tuyos", "tuyas", "suyo",
    "suya", "suyos", "suyas", "nuestro", "nuestra", "nuestros",
    "nuestras", "vuestro", "vuestra", "vuestros", "vuestras",
    "esos", "esas",
    // | forms of estar, to be (not including the infinitive):
    "estoy", "estás", "está", "estamos", "estáis", "están",
    "esté", "estés", "estemos", "estéis", "estén", "estaré",
    "estarás", "estará", "estaremos", "estaréis", "estarán",
    "estaría", "estarías", "estaríamos", "estaríais", "estarían",
    "estaba", "estabas", "estábamos", "estabais", "estaban",
    "estuve", "estuviste", "estuvo", "estuvimos", "estuvisteis",
    "estuvieron", "estuviera", "estuvieras", "estuviéramos",
    "estuvierais", "estuvieran", "estuviese", "estuvieses",
    "estuviésemos", "estuvieseis", "estuviesen", "estando",
    "estado", "estada", "estados", "estadas", "estad",
    // | forms of haber, to have (not including the infinitive):
    "he", "has", "ha", "hemos", "habéis", "han", "haya", "hayas", "hayamos",
    "hayáis", "hayan", "habré", "habrás", "habrá", "habremos", "habréis",
    "habrán", "habría", "habrías", "habríamos", "habríais", "habrían",
    "había", "habías", "habíamos", "habíais", "habían", "hube", "hubiste",
    "hubo", "hubimos", "hubisteis", "hubieron", "hubiera", "hubieras",
    "hubiéramos", "hubierais", "hubieran", "hubiese", "hubieses",
    "hubiésemos", "hubieseis", "hubiesen", "habiendo", "habido",
    "habida", "habidos", "habidas",
    // | forms of ser, to be (not including the infinitive):
    "soy", "eres", "es", "somos", "sois", "son", "sea", "seas",
    "seamos", "seáis", "sean", "seré", "serás", "será", "seremos",
    "seréis", "serán", "sería", "serías", "seríamos", "seríais", "serían",
    "era", "eras", "éramos", "erais", "eran", "fui", "fuiste", "fue",
    "fuimos", "fuisteis", "fueron", "fuera", "fueras", "fuéramos",
    "fuerais", "fueran", "fuese", "fueses", "fuésemos", "fueseis",
    "fuesen", "sintiendo", "sentido", "sentida", "sentidos", "sentidas",
    "siente", "sentid",
    // | forms of tener, to have (not including the infinitive):
    "tengo", "tienes", "tiene", "tenemos", "tenéis", "tienen",
    "tenga", "tengas", "tengamos", "tengáis", "tengan", "tendré",
    "tendrás", "tendrá", "tendremos", "tendréis", "tendrán", "tendría",
    "tendrías", "tendríamos", "tendríais", "tendrían", "tenía", "tenías",
    "teníamos", "teníais", "tenían", "tuve", "tuviste", "tuvo", "tuvimos",
    "tuvisteis", "tuvieron", "tuviera", "tuvieras", "tuviéramos",
    "tuvierais", "tuvieran", "tuviese", "tuvieses", "tuviésemos",
    "tuvieseis", "tuviesen", "teniendo", "tenido", "tenida", "tenidos",
    "tenidas", "tened",
    };

    public SpanishAnalyzer()
    {
        super("Spanish", s_stopwords);
    }
}
