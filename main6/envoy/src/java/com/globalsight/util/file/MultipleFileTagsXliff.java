/**
* Copyright 2009 Welocalize, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
*
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/
package com.globalsight.util.file;

import java.util.ArrayList;

/**
 * @author Vincent Yan
 * @date 12/12/2011
 * @version 1.0
 * @since 8.2.2
 * 
 */
public class MultipleFileTagsXliff
{
    private String original = "";
    private String separatedFolderName = "";
    private int count = 0;
    private ArrayList<String> separtedFiles = new ArrayList<String>();
    private String header = "";
    private String footer = "";
    private String content = "";
    private String encoding = "";
    private boolean isWindowsReturnMethod = true;

    public String getOriginal()
    {
        return original;
    }

    public void setOriginal(String original)
    {
        this.original = original;
    }

    public String getSeparatedFolderName()
    {
        return separatedFolderName;
    }

    public void setSeparatedFolderName(String extractedFolderName)
    {
        this.separatedFolderName = extractedFolderName;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public ArrayList<String> getSeparatedFiles()
    {
        return separtedFiles;
    }

    public void setSeparatedFiles(ArrayList<String> extractedFiles)
    {
        this.separtedFiles = extractedFiles;
    }

    public void setHeader(String header)
    {
        this.header = header;
    }

    public String getHeader()
    {
        return header;
    }

    public String getFooter()
    {
        return footer;
    }

    public void setFooter(String footer)
    {
        this.footer = footer;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public boolean isWindowsReturnMethod()
    {
        return isWindowsReturnMethod;
    }

    public void setWindowsReturnMethod(boolean isWindowsReturnMethod)
    {
        this.isWindowsReturnMethod = isWindowsReturnMethod;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

}
