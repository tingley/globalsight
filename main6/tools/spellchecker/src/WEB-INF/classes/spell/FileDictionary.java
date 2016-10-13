/**
 * Copyright 2002-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spell;

import java.util.Iterator;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;


/**
 * <p>Dictionary represented by a wordlist file (must be in UTF-8).</p>
 *
 * Format allowed: 1 word per line:
 * word1
 * word2
 * word3
 *
 * @author Nicolas Maisonneuve
 */
public class FileDictionary
    implements Dictionary
{
    private BufferedReader in;
    private String line;
    private boolean has_next_called;

    public FileDictionary (File file)
        throws IOException
    {
        in = new BufferedReader(new InputStreamReader(
            new FileInputStream(file), "UTF-8"));
    }


    public Iterator getWordsIterator()
    {
        return new FileIterator();
    }


    final class FileIterator
        implements Iterator
    {
        public Object next ()
        {
            if (!has_next_called)
            {
                hasNext();
            }

            has_next_called = false;

            return line;
        }


        public boolean hasNext ()
        {
            has_next_called = true;

            try
            {
                line = in.readLine();

                if (line != null)
                {
                    line = line.trim();

                    if (line.charAt(0) == '\uFEFF')
                    {
                        line = line.substring(1);
                    }
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                line = null;
                return false;
            }

            return (line != null);
        }

        public void remove ()
        {
            throw new RuntimeException(
                "FileIterator.remove() not implemented");
        };
    }
}
