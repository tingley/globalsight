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

public class CreateDictionary
{
    static public String s_baseDirectory = ".";
    static
    {
        try
        {
            InputStream stream = CreateDictionary.class.getClassLoader().
                getResourceAsStream("spell/spell.properties");

            Properties props = new Properties();
            props.load(stream);

            String s = props.getProperty("dictionaryDirectory");
            if (s != null && s.length() > 0)
            {
                s_baseDirectory = s;
            }
        }
        catch (Throwable ex)
        {
        }
    }

    static public void main(String[] args)
        throws IOException
    {
        String target = s_baseDirectory;
        String lang = null;
        String file = null;

        Arguments getopt = new Arguments();
        int c;

        getopt.setUsage(new String[]
        {
        "Usage: CreateDictionary [-t dir] -l lang -f file",
        "",
        "\t<dir> is the target directory ",
        "\t<lang> must be a valid locale string, e.g. \"de\" or \"en_US\"",
        "\t<file> must be a text file in UTF-8.",
        "",
        "\tIf multiple files get loaded into the same dictionary,",
        "\ttheir contents will be merged."
        });

        getopt.parseArgumentTokens(args, new char[] {'t','l','f'});

        while ((c = getopt.getArguments()) != -1)
        {
            switch (c)
            {
            case 't':
            case 'T':
                target = getopt.getStringParameter();
                break;
            case 'l':
            case 'L':
                lang = getopt.getStringParameter();
                break;
            case 'f':
            case 'F':
                file = getopt.getStringParameter();
                break;
            case 'h':
            default:
                getopt.printUsage();
                System.exit(1);
                break;
            }
        }

        if (lang == null || lang.length() == 0 ||
            file == null || file.length() == 0)
        {
            getopt.printUsage();
            System.exit(1);
        }

        Locale locale = null;
        if (lang.length() == 2)
        {
            locale = new Locale(lang);
        }
        else if (lang.length() == 5)
        {
            locale = new Locale(lang.substring(0, 2), lang.substring(3, 5));
        }
        else
        {
            getopt.printUsage();
            System.exit(1);
        }

        File f = new File(file);
        if (!f.exists())
        {
            System.err.println("File `" + file + "' does not exist!");
            System.exit(1);
        }

        String indexName = locale.toString().toLowerCase();

        System.out.println("Using base directory " + target);
        System.out.println("Loading `" + file + "' into dictionary `" +
            indexName + "'...");

        SpellIndex index = SpellIndexList.createOrOpenIndex(
            target + "/" + indexName);

        // Don't clear index, allow loading of multiple files.
        // index.clearIndex();
        index.indexDictionary(new FileDictionary(new File(file)));

        System.out.println("Dictionary `" + indexName + "' contains " +
            index.getWordCount() + " words");
    }
}
