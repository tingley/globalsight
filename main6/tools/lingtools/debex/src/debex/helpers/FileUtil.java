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
package debex.helpers;

import java.io.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.common.CodesetMapper;

public class FileUtil
{
    //
    // Constructor
    //
    private FileUtil()
    {
    }

    public static String readFile(String p_name, String p_encoding)
		throws IOException
    {
		StringBuffer result = new StringBuffer();

        String encoding = CodesetMapper.getJavaEncoding(p_encoding);

		BufferedReader in = new BufferedReader(new InputStreamReader(
			new FileInputStream(p_name), encoding));

		String line;
		while ((line = in.readLine()) != null)
		{
			result.append(line);
			result.append("\n");
		}

		in.close();

		return result.toString();
    }

    public static void writeFile(String p_name, String p_encoding,
		String p_content)
		throws IOException
    {
		// Internal line separator is \n.
		//Pattern p = Pattern.compile("\n|\r\n?");
		//Matcher m = p.matcher(p_content);
		//p_content = m.replaceAll("\r\n");

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(p_name), p_encoding));

		out.write(p_content);

		out.close();
    }
}
