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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import util.MergeInterface;
import util.PropertyList;
import util.Utilities;

public class MergeProperties implements MergeInterface
{
    static public final int COMPARE_MODE = 1;
    static public final int MERGE_MODE = 2;
    static public final int MERGE_CONFIRM_MODE = 3;
    public static final String PROPERTIES_PATH = "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties";
    public static final String MERGEPROPERTIES_FILE = "data/mergeProperties.properties";

    private int m_mode = COMPARE_MODE;
    private boolean m_ignoreOldKeys = false;
    private String[] m_ignoreParams =
    { "server.classes", };
    private List<String> m_ignoreList = Arrays.asList(m_ignoreParams);
    private String[] m_dontBreakParams =
    { "shutdown.ui.msg" };
    private List<String> m_dontBreakList = Arrays.asList(m_dontBreakParams);

    private MergeInterface m_mergeInterface = this;
    static private final int BUFFER_SIZE = 16384;
    static private final String BREAK_MARKERS = ",";

    public MergeProperties(MergeInterface p_mergeInterface)
    {
        m_mergeInterface = p_mergeInterface;
    }

    public MergeProperties(String p_previousAmbassadorHome,
            String p_currentAmbassadorHome) throws IOException
    {
        File destinationHome = new File(p_currentAmbassadorHome);
        File destinationDir = new File(p_currentAmbassadorHome
                + PROPERTIES_PATH);
        if (!destinationDir.exists())
        {
            System.out.println("The current Ambassador home "
                    + destinationHome.getCanonicalPath() + " is not valid.");
            System.out.println("It does not contain the folder "
                    + PROPERTIES_PATH + ".");
            System.out
                    .println("Please specify the current Ambassador home with the -currentHome option.");
            System.exit(1);
        }

        File sourceHome = new File(p_previousAmbassadorHome);
        File sourceDir = new File(p_previousAmbassadorHome + PROPERTIES_PATH);
        if (!sourceDir.exists())
        {
            System.out.println("The previous Ambassador home "
                    + sourceHome.getCanonicalPath() + " is not valid.");
            System.out.println("It does not contain the folder "
                    + PROPERTIES_PATH + ".");
            System.out
                    .println("Please specify the previous Ambassador home with the -previousHome option.");
            System.exit(1);
        }
        else if (sourceHome.getCanonicalPath().equals(
                destinationHome.getCanonicalPath()))
        {
            System.out.println("The previous Ambassador home "
                    + sourceHome.getCanonicalPath()
                    + " is the same as the current Ambassador home.");
            System.out
                    .println("Please specify a different previous Ambassador home with the -previousHome option.");
            System.exit(1);
        }

        Properties selectedList = loadProperties(MERGEPROPERTIES_FILE);

        ArrayList<String> selectedFiles = PropertyList.parseList(selectedList
                .getProperty("merge_files"));
        setMode(MERGE_MODE);
        for (int i = 0; i < selectedFiles.size(); i++)
        {
            mergeDirectories(p_previousAmbassadorHome + PROPERTIES_PATH + "/"
                    + selectedFiles.get(i), p_currentAmbassadorHome
                    + PROPERTIES_PATH);
        }

        selectedFiles = PropertyList.parseList(selectedList
                .getProperty("merge_comfirm_files"));
        setMode(MERGE_CONFIRM_MODE);
        for (int i = 0; i < selectedFiles.size(); i++)
        {
            mergeDirectories(p_previousAmbassadorHome + PROPERTIES_PATH + "/"
                    + selectedFiles.get(i), p_currentAmbassadorHome
                    + PROPERTIES_PATH);
        }

        // setMode(COMPARE_MODE);
        // System.out.println("\nComparing all properties files.");
        // mergeDirectories(p_previousAmbassadorHome + PROPERTIES_PATH,
        // p_currentAmbassadorHome + PROPERTIES_PATH);
    }

    public MergeProperties(int p_mergeMode, boolean p_ignoreMode)
    {
        m_mode = p_mergeMode;
        m_ignoreOldKeys = p_ignoreMode;
    }

    public Properties loadProperties(String p_propertyFileName)
            throws IOException
    {
        Properties properties = new Properties();
        InputStream in;
        try
        {
            // Open the file in its specified location
            in = new FileInputStream(p_propertyFileName);
        }
        catch (FileNotFoundException ex)
        {
            // File is not found, so try to open it again as a resource.
            // If the file is stored in the same jar file as the class,
            // it will be found this way.
            in = getClass().getResourceAsStream(p_propertyFileName);

            // File is still not found, so pass on the exception
            if (in == null)
                throw ex;
        }

        properties.load(in);
        return properties;
    }

    public void setMode(int p_mode)
    {
        m_mode = p_mode;
    }

    public boolean isCompareMode()
    {
        return (m_mode == COMPARE_MODE);
    }

    public boolean isMergeMode()
    {
        return (m_mode != COMPARE_MODE);
    }

    public boolean shoudIgnore(String p_key)
    {
        return m_ignoreList.contains(p_key);
    }

    public boolean shoudFormatValue(String p_key)
    {
        return !m_dontBreakList.contains(p_key);
    }

    private void breakLine(BufferedWriter p_out, String p_endValue)
            throws IOException
    {
        p_out.write(p_endValue + "\\");
        p_out.newLine();
        p_out.write("\t");
    }

    private void parseValues(BufferedWriter p_out, String p_key, String p_value)
            throws IOException
    {
        // Tokenize the list. Commas will also become separate tokens.
        StringTokenizer st = new StringTokenizer(p_value, BREAK_MARKERS, true);

        // If the value shouldn't be split, or if there's only one element,
        // write it on the same line.
        if (!shoudFormatValue(p_key) || st.countTokens() == 1)
        {
            p_out.write(p_value);
        }
        // Else if there are more than one elements, write them on separate
        // lines.
        else if (st.countTokens() > 1)
        {
            breakLine(p_out, "");

            while (st.hasMoreTokens())
            {
                String valueToken = st.nextToken();
                if (BREAK_MARKERS.indexOf(valueToken) != -1)
                {
                    breakLine(p_out, valueToken);
                }
                else
                    p_out.write(valueToken.trim());
            }
        }
    }

    private String escapeValue(String p_value)
    {
        if (p_value == null)
            return p_value;

        p_value = p_value.replaceAll("\\\\", "\\\\\\\\");
        // p_value = p_value.replaceAll(":", "\\\\:");
        p_value = p_value.replaceAll("\n", "\\\\n");
        p_value = p_value.replaceAll("\t", "\\\\t");

        return p_value;
    }

    private void write(BufferedWriter p_out, String p_text) throws IOException
    {
        if (p_out != null)
        {
            p_out.write(p_text);
        }
    }

    private void writeKeyAndValue(BufferedWriter p_out, String p_key,
            String p_value) throws IOException
    {
        p_out.write(p_key);
        p_out.write(" = ");

        parseValues(p_out, p_key, p_value);
    }

    public File backupFile(File p_file) throws IOException
    {
        File backup = new File(p_file.getAbsolutePath() + ".bak");

        if (backup.exists())
        {
            if (!backup.delete())
            {
                System.out.println("Failed to delete the old backup file.");
                return null;
            }
        }

        if (!p_file.renameTo(backup))
        {
            System.out.println("Failed to backup the file.");
            return null;
        }

        return backup;
    }

    public void copyFile(File p_sourceFile, File p_destDir) throws IOException
    {
        File destFile = new File(p_destDir, p_sourceFile.getName());
        FileInputStream input = new FileInputStream(p_sourceFile);
        FileOutputStream output = new FileOutputStream(destFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;

        while ((size = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, size);
        }
        input.close();
        output.close();
    }

    private String readEntry(String prompt, String p_default)
            throws IOException
    {
        String userInput = "";
        while (!userInput.equals("y") && !userInput.equals("yes")
                && !userInput.equals("n") && !userInput.equals("no"))
        {
            System.out.print(prompt + "[" + p_default + "]: ");
            System.out.flush();
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    System.in));
            userInput = input.readLine();
            if (userInput == null || userInput.length() == 0)
                userInput = p_default;
        }

        return userInput.toLowerCase();
    }

    public int checkToUpdateValue(String fileName, String key, String comment,
            String oldValue, String newValue) throws IOException
    {
        boolean doUpdate = false;

        // Key is ignored
        if (shoudIgnore(key))
        {
            return MergeInterface.EQUAL;
        }
        // Values are the same
        else if (PropertyList.compareValues(oldValue, newValue))
        {
            return MergeInterface.EQUAL;
        }
        // Values are different
        else
        {
            System.out.println("\n" + fileName);
            System.out.print(comment);
            System.out.println(key);
            System.out.println("- Previous value: '" + oldValue + "'");
            System.out.println("- Current value:  '" + newValue + "'");

            if (m_mode == COMPARE_MODE)
            {
                return MergeInterface.IGNORE;
            }
            else if (m_mode == MERGE_CONFIRM_MODE)
            {
                String answer = readEntry("Replace the current value with "
                        + "the previous version's value? ", "n");

                doUpdate = (answer.startsWith("y"));
            }
            else if (m_mode == MERGE_MODE)
            {
                doUpdate = true;
            }

        }

        if (isMergeMode())
        {
            System.out.println(doUpdate ? "Updated current version."
                    : "Ignored.");
        }
        return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
    }

    public int checkToAddKey(String fileName, String key, String comment,
            String value) throws IOException
    {
        System.out.println("\n" + fileName);
        System.out.println(key
                + " is in the previous version but not in the current version");
        System.out.println("- Previous value: '" + value + "'");

        if (isMergeMode())
        {
            boolean doUpdate = true;
            if (m_mode == MERGE_CONFIRM_MODE)
            {
                String answer = readEntry("Add the new key and value? ", "n");

                doUpdate = (answer.startsWith("y"));
            }

            System.out.println(doUpdate ? "Added to current version."
                    : "Ignored.");

            return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
        }

        return MergeInterface.EQUAL;
    }

    public int checkToCopyFile(String fileName) throws IOException
    {
        if (isCompareMode())
        {
            return MergeInterface.EQUAL;
        }
        else
        {
            boolean doUpdate = true;
            if (m_mode == MERGE_CONFIRM_MODE)
            {
                String answer = readEntry("Copy the file " + fileName + "? ",
                        "n");

                doUpdate = (answer.startsWith("y"));
            }

            if (doUpdate)
            {
                System.out.println("Copied " + fileName);
            }

            return doUpdate ? MergeInterface.UPDATE : MergeInterface.IGNORE;
        }
    }

    public void noDifference(String fileName) throws IOException
    {
        if (m_mode != COMPARE_MODE)
        {
            System.out.println("\n" + fileName);
            System.out.println("All values are the same.");
        }
    }

    public void mergePropertyFiles(String p_sourceFileName,
            String p_destinationDirName) throws IOException
    {
        mergePropertyFiles(new File(p_sourceFileName), new File(
                p_destinationDirName));
    }

    public void mergePropertyFiles(File p_sourceFile, File p_destinationDir)
            throws IOException
    {
        String fileName = p_sourceFile.getName();

        File destinationFile = new File(p_destinationDir, fileName);
        if (!destinationFile.exists())
        {
            copyFile(p_sourceFile, p_destinationDir);
            return;
        }

        boolean noDifference = true;

        PropertyList oldProperties = new PropertyList(p_sourceFile);
        PropertyList newProperties = new PropertyList(destinationFile);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(destinationFile), "ISO-8859-1"));
        BufferedWriter out = null;

        File inProgressFile = new File(p_destinationDir, fileName + ".merge");
        if (isMergeMode())
        {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(inProgressFile), "ISO-8859-1"));
        }

        String comment = "";
        String line;
        // Read each line the destination file
        while ((line = in.readLine()) != null)
        {
            // If the line is a blank, write it out as is
            String trimmed = line.trim();
            if (trimmed.equals(""))
            {
                if (out != null)
                {
                    out.write(line);
                }
                comment = "";
            }
            // If the line is a comment, write it out as is, and save the
            // comment in the variable.
            else if (trimmed.startsWith("#") || trimmed.startsWith("!"))
            {
                if (out != null)
                {
                    out.write(line);
                }
                comment += line + "\n";
            }
            // Otherwise, extract the key from the line, then retrieve
            // the value from the combined list, and write them out
            // to the file.
            else
            {
                StringTokenizer st = new StringTokenizer(line, "=: \t");
                if (st.hasMoreTokens())
                {
                    String key = st.nextToken().trim();

                    // Key is in both old and new files
                    if (oldProperties.containsKey(key))
                    {
                        String oldValue = escapeValue(oldProperties
                                .getValue(key));
                        String newValue = escapeValue(newProperties
                                .getValue(key));
                        String setValue = null;

                        int compareResult = m_mergeInterface
                                .checkToUpdateValue(fileName, key, comment,
                                        oldValue, newValue);
                        if (compareResult == MergeInterface.EQUAL)
                        {
                            setValue = newValue;
                        }
                        else
                        {
                            setValue = (compareResult == MergeInterface.UPDATE ? oldValue
                                    : newValue);
                            noDifference = false;
                        }

                        if (isMergeMode())
                        {
                            writeKeyAndValue(out, key, setValue);
                        }

                        // Remove the key from the combined list. Later,
                        // what remain are the keys that are in the source
                        // file, but not in the destination file.
                        oldProperties.remove(key);
                    }
                    // Key is in new file only
                    else
                    {
                        if (isMergeMode())
                        {
                            String newValue = escapeValue(newProperties
                                    .getValue(key));
                            writeKeyAndValue(out, key, newValue);
                        }
                    }

                    // Since we only care about the key in the destination file,
                    // ignore the continued values.
                    while (line != null && line.endsWith("\\")
                            && !line.endsWith("\\\\"))
                        line = in.readLine();
                    comment = "";
                }
            }

            if (out != null)
            {
                out.newLine();
            }
        }
        in.close();

        if (!m_ignoreOldKeys)
        {
            // Append the keys and values that are in the source file but not in
            // the
            // destination file.
            for (int i = 0; i < oldProperties.size(); i++)
            {
                String key = oldProperties.getKey(i);
                if (!shoudIgnore(key))
                {
                    String value = escapeValue(oldProperties.getValue(i));
                    comment = oldProperties.getComment(i);

                    int compareResult = m_mergeInterface.checkToAddKey(
                            fileName, key, comment, value);
                    if (isMergeMode() && compareResult == MergeInterface.UPDATE)
                    {
                        out.newLine();
                        if (!"".equals(comment))
                        {
                            out.newLine();
                            out.write(comment);
                        }
                        writeKeyAndValue(out, key, value);
                    }

                    noDifference = false;
                }
            }
        }

        if (out != null)
        {
            out.close();
        }

        if (noDifference)
        {
            m_mergeInterface.noDifference(fileName);
            inProgressFile.delete();
        }
        else if (isMergeMode())
        {
            File backupFile = new File(p_destinationDir, fileName + ".bak");
            backupFile.delete();
            destinationFile.renameTo(backupFile);
            inProgressFile.renameTo(destinationFile);
        }
    }

    public void mergeFiles(String p_sourceFileName, String p_destinationDirName)
            throws IOException
    {
        mergeFiles(new File(p_sourceFileName), new File(p_destinationDirName));
    }

    public void mergeFiles(File p_sourceFile, File p_destinationDir)
            throws IOException
    {
        String fileName = p_sourceFile.getName();
        if (fileName.endsWith(".properties"))
        {
            mergePropertyFiles(p_sourceFile, p_destinationDir);
        }
        else if (fileName.endsWith(".map") || fileName.endsWith(".pers"))
        {
            int copyResult = m_mergeInterface.checkToCopyFile(fileName);
            if (copyResult == MergeInterface.UPDATE)
            {
                backupFile(new File(p_destinationDir, fileName));
                copyFile(p_sourceFile, p_destinationDir);
            }
        }
    }

    public void mergeDirectories(String p_source, String p_destination)
            throws IOException
    {
        File sourceFile = new File(p_source);
        File p_destinationDir = new File(p_destination);

        if (!sourceFile.exists())
        {
            System.out.println(p_source + " does not exist.");
        }
        else if (!p_destinationDir.exists())
        {
            System.out.println(p_destination + " does not exist.");
        }
        else if (!p_destinationDir.isDirectory())
        {
            System.out.println(p_destination + " is not a directory.");
        }
        else if (sourceFile.isFile())
        {
            mergeFiles(sourceFile, p_destinationDir);
        }
        else
        {
            File[] directoryContents = sourceFile.listFiles();
            for (int i = 0; i < directoryContents.length; i++)
            {
                mergeFiles(directoryContents[i], p_destinationDir);
            }
        }
    }

    public static void main(String[] args)
    {
        Utilities.requireJava14();

        int compareMode = MergeProperties.COMPARE_MODE;
        boolean ignoreMode = false;
        String sourceHome = "../..";
        String targetHome = "..";
        String sourceFile = null;
        String targetDirectory = null;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-merge"))
            {
                compareMode = MergeProperties.MERGE_MODE;
            }
            else if (args[i].equalsIgnoreCase("-mergeconfirm"))
            {
                compareMode = MergeProperties.MERGE_CONFIRM_MODE;
            }
            else if (args[i].equalsIgnoreCase("-compare"))
            {
                compareMode = MergeProperties.COMPARE_MODE;
            }
            else if (args[i].equalsIgnoreCase("-ignoreoldkeys"))
            {
                ignoreMode = true;
            }
            else if (args[i].equalsIgnoreCase("-previousHome"))
            {
                sourceHome = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-currentHome"))
            {
                targetHome = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-source"))
            {
                sourceFile = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-target"))
            {
                targetDirectory = args[++i];
            }
            else if (args[i].toLowerCase().startsWith("-h"))
            {
                System.out.println("Usage: ");
                System.out
                        .println("    java -classpath . MergeProperties [-merge | -mergeconfirm | -compare] [-ignoreoldkeys] <source_file> <destination_dir>");
                System.out.println("or");
                System.out
                        .println("    java -classpath . MergeProperties [-merge | -mergeconfirm | -compare] [-ignoreoldkeys] <source_dir> <destination_dir>");
                System.out.println("    source_file:     file to read from");
                System.out
                        .println("    source_dir:      directory of source files to read from");
                System.out
                        .println("    destination_dir: directory of destination files to compare or copy to");
                System.out
                        .println("    -merge:          if the source and destination values are different,");
                System.out
                        .println("                     copy value from source");
                System.out
                        .println("    -mergeconfirm:   if the source and destination values are different,");
                System.out
                        .println("                     prompt whether to copy value from source");
                System.out
                        .println("    -compare:        if the source and destination values are different,");
                System.out
                        .println("                     print out the parameter");
                System.out
                        .println("    -ignoreoldkeys:  if source contains parameters not in destination,");
                System.out
                        .println("                     do not copy them to the destination");
                System.exit(0);
            }
        }

        try
        {
            if (sourceFile != null && targetDirectory != null)
            {
                MergeProperties mergeProperties = new MergeProperties(
                        compareMode, ignoreMode);
                mergeProperties.mergeDirectories(sourceFile, targetDirectory);
            }
            else
            {
                new MergeProperties(sourceHome, targetHome);
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex);
        }
    }
}