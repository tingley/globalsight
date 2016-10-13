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


package com.globalsight.util;

import com.globalsight.io.GeneralFileFilter;

import java.io.IOException;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipEntry;

/**
 * <p>Standalone utility for determining where a specified class
 * appears in your classpath.</p>
 *
 * <p>Shows all occurrences.</p>
 *
 * <p>Requires the fully qualified class name (e.g. foo.bar.Howdy).</p>
 */
public class WhereIsClassInPath
{
    /**
     * Standalone utility for determining where a specified class
     * appears in your classpath.  Shows all occurrences.  Requires
     * the fully qualified class name (e.g. foo.bar.Howdy).
     */
    public static void main(String[] args)
        throws ParseException, IOException
    {
        if (args.length != 1)
        {
            System.err.println("USAGE: java " +
                WhereIsClassInPath.class.getName() +
                " <fully qualified class name>");
            return;
        }

        System.out.println(whereIs(args[0]));
    }

    /**
     * Find where a specified class appears in the classpath.
     * @param p_fullClassName fully qualified class name to find.
     * @return String contains all occurances
     * @throws ParseException if not a fully qualified class name
     * @throws IOException reading zip file
     */
    public static String whereIs(String p_fullClassName)
        throws ParseException, IOException
    {
        StringBuffer result = new StringBuffer();

        // Build a relative path name in the local file system's syntax.
        String classFileRelativePathNameForFileTrees =
            makeRelativePathName(p_fullClassName, File.separator);

        // Build a relative path name in zip file syntax.
        String classFileRelativePathNameForZipsJars =
            makeRelativePathName(p_fullClassName, "/");

        ArrayList placesFound = new ArrayList();
        String classpath = getImplicitClassPath();
        StringTokenizer stringtokenizer =
            new StringTokenizer(classpath, File.pathSeparator);

        while (stringtokenizer.hasMoreElements())
        {
            String classPathElementPath = stringtokenizer.nextToken().trim();
            if (classPathElementPath.endsWith(File.separator))
            {
                classPathElementPath = classPathElementPath.substring(0,
                    classPathElementPath.length() - 1);
            }

            File classPathElement = new File(classPathElementPath);
            if (classPathElement.isDirectory())
            {
                File classFile = new File(classPathElement,
                    classFileRelativePathNameForFileTrees);

                if (classFile.exists())
                {
                    placesFound.add(classPathElementPath + File.separator +
                        classFileRelativePathNameForFileTrees);
                }
            }
            else
            {
                if (classPathElement.isFile() &&
                    (classPathElement.toString().toLowerCase().endsWith(".jar") ||
                     classPathElement.toString().toLowerCase().endsWith(".zip")) &&
                    searchForEntry(classPathElement,
                        classFileRelativePathNameForZipsJars))
                {
                    placesFound.add(classPathElementPath);
                }
            }
        }

        // Output the places (directories and jars/zips) where the class was found.
        if (placesFound.size() == 0)
        {
            result.append(p_fullClassName + " not found.\n");
        }
        else
        {
            for (int i = 0; i < placesFound.size(); i++)
            {
                result.append("Found in " + (String)placesFound.get(i) + "\n");
            }
        }

        result.append("\nImplicit classpath:\n");
        result.append(classpath);
        result.append("\n");
        result.append("java home = ");
        result.append(System.getProperty("java.home"));
        result.append("\n");

        return result.toString();
    }


    /**
     * Find where a specified file appears in the path.
     * @param p_fileName file name to find.
     * @return - first occurance of file in path.
     */
    public static String whereIsFileInPath(String p_fileName, String p_path)
    {
        StringTokenizer stringtokenizer =
            new StringTokenizer(p_path, File.pathSeparator);

        while (stringtokenizer.hasMoreElements())
        {
            String elementPath = stringtokenizer.nextToken();
            if (elementPath.endsWith(File.separator))
            {
                elementPath = elementPath.substring(0, elementPath.length() - 1);
            }

            File element = new File(elementPath);

            if (element.isDirectory())
            {
                File file = new File(element, p_fileName);

                if (file.exists())
                {
                    return elementPath + File.separator + p_fileName;
                }
            }
        }

        return null;
    }

    /**
     * Get classpath including installed optional packages.
     */
    public static String getImplicitClassPath()
    {
        StringBuffer result = new StringBuffer();

        String classpath = System.getProperties().getProperty("java.class.path");

        // Add installed optional packages JAR files to the start of
        // the classpath.

        // Order is: jre/lib/endorsed, jre/lib, jre/lib/ext. Please verify.

        String javahome = System.getProperty("java.home");

        File dir = new File(javahome + File.separator +
            "lib" + File.separator + "endorsed");

        result.append(getJarsInDirectory(dir));

        dir = new File(javahome + File.separator + "lib");

        result.append(getJarsInDirectory(dir));

        dir = new File(javahome + File.separator +
            "lib" + File.separator + "ext");

        result.append(getJarsInDirectory(dir));

        // Append users' CLASSPATH.
        result.append(fixupClasspath(classpath));

        return result.toString();
    }

    // Adds \n at end of classpath elements to ease readability.
    public static String getJarsInDirectory(File p_file)
    {
        StringBuffer result = new StringBuffer();

        File[] directoryContent = p_file.listFiles(
            new GeneralFileFilter(new String[] {".jar", ".zip"},
                GeneralFileFilter.ENDS_WITH_PATTERN_OPERATOR,
                GeneralFileFilter.IGNORE_CASE,
                ! GeneralFileFilter.RECURSE_DIRECTORIES));

        if (directoryContent != null)
        {
            for (int i = 0; i < directoryContent.length; i++)
            {
                result.append(p_file);
                result.append(File.separator);
                result.append(directoryContent[i].getName());

                result.append(File.pathSeparator);
                result.append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Make a relative path name from fully qualified class name by
     * replacing "." with file sepatator.
     * @param p_fullClassName fully qualified class name.
     * @return p_fileSeparator file separator
     * @throws ParseException if not a fully qualified class name
     */
    private static String makeRelativePathName(String p_fullClassName,
        String p_fileSeparator)
        throws ParseException
    {
        final char DOT = '.';

        if ((p_fullClassName.indexOf( DOT ) == 0) ||
            (p_fullClassName.lastIndexOf( DOT ) == p_fullClassName.length() -1))
        {
            throw new ParseException("Invalid fully qualified class name " +
                p_fullClassName, 0);
        }

        StringBuffer relPathName = new StringBuffer();
        int dotIx = -1;
        int prevDotIx = dotIx;

        while ((dotIx = p_fullClassName.indexOf( DOT, prevDotIx + 1 )) != -1)
        {
            relPathName.append(p_fullClassName.substring(prevDotIx + 1, dotIx) +
                p_fileSeparator);

            prevDotIx = dotIx;
        }

        relPathName.append(p_fullClassName.substring(prevDotIx + 1,
            p_fullClassName.length()));

        return (relPathName.toString() + ".class");
    }

    /**
     * Search for the entry in the ZipFile.
     * @param p_file file to search.
     * @return p_entry entry to search for
     * @throws IOException reading zip file
     */
    private static boolean searchForEntry(File p_file, String p_entry)
        throws IOException
    {
        if (!p_file.exists() && !p_file.isFile())
        {
            return false;
        }

        boolean flag = false;

        try
        {
            ZipFile zipfile = new ZipFile(p_file);
            Enumeration<?> enumeration = zipfile.getEntries();

            while (enumeration.hasMoreElements())
            {
                ZipEntry zipentry = (ZipEntry)enumeration.nextElement();

                if (zipentry.getName().equals(p_entry))
                {
                    flag = true;
                    break;
                }
            }

            zipfile.close();
        }
        catch (IOException ioexception)
        {
            throw new IOException("Error opening the jar file " + p_file +
                " " + ioexception.toString());
        }

        return flag;
    }

    // Inserts a linebreak after every ";" to ease readability.
    public static String fixupClasspath(String p_path)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_path.length(); i < max; i++)
        {
            char ch = p_path.charAt(i);

            if (String.valueOf(ch).equals(File.pathSeparator))
            {
                result.append(ch);
                result.append("\n");
            }
            else
            {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
