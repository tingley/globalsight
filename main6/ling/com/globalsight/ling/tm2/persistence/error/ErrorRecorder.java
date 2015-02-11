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

package com.globalsight.ling.tm2.persistence.error;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.webapp.pagehandler.tm.management.FileUploadHelper;
import com.globalsight.util.FileUtil;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

/**
 * A class to record <code>BatchException e</code>. It will be used while import
 * tm.
 */
public class ErrorRecorder
{
    static public final String ERROR_FILE_SUFFIX = "-errorTuvs.html";
    static private final Logger logger = Logger
            .getLogger(ErrorRecorder.class);

    static private final String ERROR_TUV = FileUtil.lineSeparator
            + "<tr BGCOLOR=\"{0}\">" + FileUtil.lineSeparator
            + "<td class=\"whs5\" VALIGN=\"top\" align=\"center\">{1}</td>"
            + FileUtil.lineSeparator + "<td class=\"whs6\"><seg>{2}</seg>"
            + FileUtil.lineSeparator + "</td>" + FileUtil.lineSeparator
            + "<td class=\"whs6\" VALIGN=\"top\">" + FileUtil.lineSeparator + "{3}"
            + FileUtil.lineSeparator + "</td>" + FileUtil.lineSeparator
            + "</tr>" + FileUtil.lineSeparator;
    static private final String END = "</table></body></html>";
    static private final String ERROR_HEADER_PATH = "com/globalsight/ling/tm2/persistence/"
            + "error/html/error_header.htm";

    static private final String COLOR1 = "#EEEEEE";
    static private final String COLOR2 = "#FFFFFF";

    /**
     * Records the <code>BatchException e</code> to the file.
     * <p>
     * The count of <code>BatchException e</code> will be appended at the end of
     * the file.
     * 
     * @param file
     *            the file to record the <code>BatchException e</code>.
     * @param e
     *            the <code>BatchException e</code> to be recorded.
     * @param recordedCount
     *            The number of recorded tuv.
     */
    public synchronized static void record(File tmfile, BatchException e,
            IProcessStatusListener listener, int recordedCount)
    {
        File file = getErrorFile(tmfile);
        int i = recordedCount;

        StringBuilder content = new StringBuilder();
        for (BatchException.TuvError tuv : e.getTuvs())
        {
            i++;
            String seg = XmlUtil.escapeString(tuv.getSegment());
            String message = ProcessStatus.getStringFormattedFromResBundle(
                listener, tuv.getMessageKey(), tuv.getDefaultMessagePattern(),
                tuv.getMessageArguments());
            content.append(MessageFormat.format(ERROR_TUV, getColor(i), i, seg,
                    message));
        }
        FileUtil.appendFile(file, content.toString());
    }

    public synchronized static void init(File file)
    {
        try
        {
            String path = ErrorRecorder.class.getClassLoader().getResource(
                    ERROR_HEADER_PATH).getFile();
            FileUtil.copyFile(new File(path), getErrorFile(file));
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public synchronized static void end(File file)
    {
        FileUtil.appendFile(file, END);
    }

    /**
     * Gets a file that record all error tus.
     * 
     * @param file
     * @return
     */
    public static File getErrorFile(File file)
    {
        String fileName = file.getAbsolutePath();
        return new File(fileName + ERROR_FILE_SUFFIX);
    }

    public static String getStorePath(String file)
    {
        String path = file.substring(file
                .indexOf(FileUploadHelper.FILE_UPLOAD_DIR)).replace("\\", "/");
        return path + ERROR_FILE_SUFFIX;
    }

    private static String getColor(int num)
    {
        if (num % 2 == 0)
            return COLOR1;

        return COLOR2;
    }
}
