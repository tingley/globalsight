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
package com.globalsight.ling.tm2.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.IndexReader.ReaderClosedListener;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * Cache class for Lucene searcher.
 */

public class LuceneCache implements ReaderClosedListener
{
    private String keyStr;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private boolean isMultiReader = false;

    private LuceneCache(String path, IndexReader iR, IndexSearcher iS)
    {
        this.keyStr = path;
        this.indexReader = iR;
        this.indexSearcher = iS;
    }

    public boolean isMultiReader()
    {
        return isMultiReader;
    }

    public void setMultiReader(boolean isMultiReader)
    {
        this.isMultiReader = isMultiReader;
    }

    public String getKeyStr()
    {
        return keyStr;
    }

    public void setKeyStr(String keyStr)
    {
        this.keyStr = keyStr;
    }

    public IndexReader getIndexReader()
    {
        return indexReader;
    }

    public void setIndexReader(IndexReader indexReader)
    {
        this.indexReader = indexReader;
    }

    public IndexSearcher getIndexSearcher()
    {
        return indexSearcher;
    }

    public void setIndexSearcher(IndexSearcher indexSearcher)
    {
        this.indexSearcher = indexSearcher;
    }

    public void destroy()
    {
        keyStr = null;

        if (indexReader != null)
        {
            try
            {
                indexReader.close();
            }
            catch (Exception e)
            {

            }
        }

        if (indexSearcher != null)
        {
            indexSearcher = null;
        }
    }

    @Override
    public void onClose(IndexReader arg0)
    {
        LuceneCache.cleanLuceneCache(getKeyStr());
    }

    private static Map<String, LuceneCache> cache = null;

    static
    {
        HashMap<String, LuceneCache> h = new HashMap<String, LuceneCache>();
        cache = Collections.synchronizedMap(h);
    }

    /**
     * For one dir searcher
     * 
     * @param path
     */
    public static LuceneCache getLuceneCache(String path) throws IOException
    {
        if (path == null)
        {
            return null;
        }

        return getLuceneCache(new File(path));
    }

    /**
     * clean one dir's searcher
     * 
     * @param path
     */
    public static void cleanLuceneCache(String path)
    {
        if (path == null)
        {
            return;
        }

        cleanLuceneCache(new File(path));
    }

    /**
     * For one dir searcher
     * 
     * @param path
     */
    public static LuceneCache getLuceneCache(File path) throws IOException
    {
        if (path == null)
        {
            return null;
        }

        String p = path.getPath();

        if (cache.containsKey(p))
        {
            LuceneCache lc = cache.get(p);
            return lc;
        }

        NIOFSDirectory dir = new NIOFSDirectory(path);
        if (dir != null && DirectoryReader.indexExists(dir))
        {
            // if (IndexWriter.isLocked(dir))
            // {
            // IndexWriter.unlock(dir);
            // }

            // clean lock
            // dir.clearLock(name);
            IndexReader iR = DirectoryReader.open(dir);
            IndexSearcher iS = new IndexSearcher(iR);
            LuceneCache lc = new LuceneCache(p, iR, iS);

            iR.addReaderClosedListener(lc);
            cache.put(p, lc);

            return lc;
        }
        else
        {
            return null;
        }
    }

    /**
     * clean one dir's searcher
     * 
     * @param path
     */
    public static void cleanLuceneCache(File path)
    {
        if (path == null)
        {
            return;
        }

        String p = path.getPath();

        if (cache.containsKey(p))
        {
            LuceneCache lc = cache.get(p);
            lc.destroy();
            cache.remove(p);
        }
    }

    /**
     * For MultiReader, convert TM IDs to key
     * 
     * @param tmIds
     * @return
     */
    public static String tmIdToKey(List<Long> tmIds)
    {
        if (tmIds == null || tmIds.size() == 0)
        {
            return "";
        }

        Collections.sort(tmIds);

        StringBuffer sb = new StringBuffer();

        for (Long long1 : tmIds)
        {
            sb.append(long1).append("_");
        }

        return sb.toString();
    }

    /**
     * For MultiReader
     * 
     * @param tmIds
     * @param ireaderArray
     * @return
     */
    public static LuceneCache getLuceneCache(ArrayList<Long> tmIds,
            IndexReader[] ireaderArray)
    {
        String key = tmIdToKey(tmIds);

        if (cache.containsKey(key))
        {
            LuceneCache lc = cache.get(key);
            return lc;
        }

        MultiReader indexReader = new MultiReader(ireaderArray);
        IndexSearcher iS = new IndexSearcher(indexReader);
        LuceneCache lc = new LuceneCache(key, indexReader, iS);
        lc.setMultiReader(true);

        indexReader.addReaderClosedListener(lc);
        for (IndexReader irOri : ireaderArray)
        {
            irOri.addReaderClosedListener(lc);
        }

        cache.put(key, lc);

        return lc;
    }
}
