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

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import java.io.*;
import java.util.*;

public class SpellIndexList
{
    static private HashMap s_registry = new HashMap();
    static private Analyzer s_analyzer = new WhitespaceAnalyzer();

    static public String s_baseDirectory = ".";
    static
    {
        try
        {
            java.io.InputStream stream = SpellIndexList.class.getClassLoader()
                    .getResourceAsStream("spell/spell.properties");

            Properties props = new Properties();
            props.load(stream);

            String s = props.getProperty("dictionaryDirectory");
            if (s != null && s.length() > 0)
            {
                s_baseDirectory = s;
            }

            new File(s_baseDirectory).mkdirs();
        }
        catch (Throwable ex)
        {
        }
    }

    //
    // Methods
    //
    static public SpellIndex getSpellIndex(String p_name) throws IOException
    {
        SpellIndex result = null;

        synchronized (s_registry)
        {
            result = (SpellIndex) s_registry.get(p_name);

            if (result == null)
            {
                String indexName = s_baseDirectory + "/" + p_name;

                result = createOrOpenIndex(indexName);

                s_registry.put(p_name, result);
            }

            return result;
        }
    }

    /**
     * Deletes a SpellIndex on disk.
     * 
     * It involves a reaper thread that should try and delete the directory
     * files.
     */
    static public void deleteSpellIndex(String p_name) throws IOException
    {
        SpellIndex result = null;

        synchronized (s_registry)
        {
            result = (SpellIndex) s_registry.remove(p_name);
        }

        deleteDirectory(s_baseDirectory + "/" + p_name);
    }

    /**
     * Retrieves the names of the available spell indexes on disk.
     */
    static public ArrayList getSpellIndexes()
    {
        ArrayList result = new ArrayList();

        File base = new File(s_baseDirectory);
        File[] files = base.listFiles(new FileFilter()
        {
            public boolean accept(File p_file)
            {
                if (p_file.isDirectory())
                {
                    return true;
                }

                return false;
            }
        });

        for (int i = 0, max = files.length; i < max; i++)
        {
            result.add(files[i].getName());
        }

        return result;
    }

    /**
     * This should start a background thread to delete this lucene directory or
     * otherwise make sure that no reader has opened any files.
     */
    static public void deleteDirectory(String p_directory)
    {
        try
        {
            File base = new File(p_directory);
            File[] files = base.listFiles();

            for (int i = 0, max = files.length; i < max; i++)
            {
                files[i].delete();
            }

            base.delete();
        }
        catch (Throwable ignore)
        {
            ignore.printStackTrace();
        }
    }

    static protected SpellIndex createOrOpenIndex(String p_name)
            throws IOException
    {
        if (!IndexReader.indexExists(p_name))
        {
            IndexWriter writer = new IndexWriter(p_name, s_analyzer, true);
            writer.optimize();
            writer.close();
        }

        return new SpellIndex(FSDirectory.getDirectory(p_name, false));
    }
}
