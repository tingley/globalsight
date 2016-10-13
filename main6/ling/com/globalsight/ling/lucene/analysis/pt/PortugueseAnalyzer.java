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
package com.globalsight.ling.lucene.analysis.pt;

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
public class PortugueseAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "de", "a", "o", "que", "e", "do", "da", "em", "um", "para", "com",
    "não", "uma", "os", "no", "se", "na", "por", "mais", "as", "dos",
    "como", "mas", "ao", "ele", "das", "à", "seu", "sua", "ou", "quando",
    "muito", "nos", "já", "eu", "também", "só", "pelo", "pela", "até",
    "isso", "ela", "entre", "depois", "sem", "mesmo", "aos", "seus",
    "quem", "nas", "me", "esse", "eles", "você", "essa", "num", "nem",
    "suas", "meu", "às", "minha", "numa", "pelos", "elas", "qual", "nós",
    "lhe", "deles", "essas", "esses", "pelas", "este", "dele",
    //  | é          from SER
    //  | foi        from SER
    //  | tem        from TER
    //  | ser        from SER
    //  | há         from HAV
    //  | está       from EST
    //  | era        from SER
    //  | ter        from TER
    //  | estão      from EST
    //  | tinha      from TER
    //  | foram      from SER
    //  | têm        from TER
    //  | havia      from HAV
    //  | seja       from SER
    //  | será       from SER
    //  | tenho      from TER
    //  | fosse      from SER

    // | other words. There are many contractions such as naquele = em+aquele,
    // | mo = me+o, but they are rare.
    // | Indefinite article plural forms are also rare.
    "tu", "te", "vocês", "vos", "lhes", "meus", "minhas", "teu", "tua",
    "teus", "tuas", "nosso", "nossa", "nossos", "nossas", "dela", "delas",
    "esta", "estes", "estas", "aquele", "aquela", "aqueles", "aquelas",
    "isto", "aquilo",

    // | forms of estar, to be (not including the infinitive):
    "estou", "está", "estamos", "estão", "estive", "esteve", "estivemos",
    "estiveram", "estava", "estávamos", "estavam", "estivera", "estivéramos",
    "esteja", "estejamos", "estejam", "estivesse", "estivéssemos",
    "estivessem", "estiver", "estivermos", "estiverem",

    // | forms of haver, to have (not including the infinitive):
    "hei", "há", "havemos", "hão", "houve", "houvemos", "houveram",
    "houvera", "houvéramos", "haja", "hajamos", "hajam", "houvesse",
    "houvéssemos", "houvessem", "houver", "houvermos", "houverem",
    "houverei", "houverá", "houveremos", "houverão", "houveria",
    "houveríamos", "houveriam",

    // | forms of ser, to be (not including the infinitive):
    "sou", "somos", "são", "era", "éramos", "eram", "fui", "foi",
    "fomos", "foram", "fora", "fôramos", "seja", "sejamos", "sejam",
    "fosse", "fôssemos", "fossem", "for", "formos", "forem", "serei",
    "será", "seremos", "serão", "seria", "seríamos", "seriam",

    // | forms of ter, to have (not including the infinitive):
    "tenho", "tem", "temos", "tém", "tinha", "tínhamos", "tinham",
    "tive", "teve", "tivemos", "tiveram", "tivera", "tivéramos",
    "tenha", "tenhamos", "tenham", "tivesse", "tivéssemos",
    "tivessem", "tiver", "tivermos", "tiverem", "terei", "terá",
    "teremos", "terão", "teria", "teríamos", "teriam",
    };

    public PortugueseAnalyzer()
    {
        super("Portuguese", s_stopwords);
    }
}
