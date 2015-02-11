
/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 
THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 
THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
 */

package com.globalsight.ling.aligner;

import com.globalsight.ling.common.FileListBuilder;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Insert the type's description here.
 */
public class FileAligner
{
    // Version number
    static private String m_appVersion = "2.0";
    
    static private PropertyResourceBundle config = null;
    
    static private final String KEY_USER_LOCALE = "UserLocale";
    static private final String KEY_FILE_FORMAT = "FileFormat";
    static private final String KEY_EXTENSION_LIST = "FileExtensionList";
    static private final String KEY_ROOT_DIR = "RootDir";
    static private final String KEY_DB_IMPORT_ROOT_DIR = "ImportRootDir";
    static private final String KEY_DB_IMPORT_MODIFIED_SEGS_ONLY = "InsertModifiedSegmentsOnly";
    static private final String KEY_JDBC_DRIVER = "JdbcDriver";
    static private final String KEY_DB_URL = "DbUrl";
    static private final String KEY_USER_NAME = "UserName";
    static private final String KEY_USER_PSWD = "UserPswd";
    static private final String KEY_TM_NAME = "TM_Name";
    
    static private final String KEY_SRC_LOCALE = "SrcLocale";
    static private final String KEY_SRC_CODESET = "SrcCodeset";
    static private final String KEY_SRC_DIR = "SrcSubDir";
    
    // target config prefixes - actually followed by a number in config file
    static private final String KEY_TRG_LOCALE = "TrgLocale_";
    static private final String KEY_TRG_CODESET = "TrgCodeset_";
    static private final String KEY_TRG_DIR = "TrgSubDir_";
    
    // messages
    static private final String MSG_NO_TRG_DIR = "NoTrgDir";
    
    static private final Vector m_trgDirList = new Vector();
    static private final Vector m_alignmentResultsList = new Vector();
    
    static private DirParam m_srcDir = null;
    
    static private String m_fileFormat = null;
    static private String m_fileExtList = null;
    static private String m_alignmentRootDir = null;
    static private String m_jdbcDriver = null;
    static private String m_dbUrl = null;
    static private String m_tmName = null;
    static private String m_dbImportRootDir = null;
    static private boolean m_dbInsertModifiedSegsOnly = false;
    static private String m_userName = null;
    static private String m_userPswd = null;
    static private int m_groupIdx = 0;
    static private String m_configFile = null;
    
    static private ResourceBundle messages = null;
    
    public static Vector m_sourceFiles = new Vector();
    public static Vector m_alignedFiles = new Vector();
    public static Vector m_missingTrgFiles = new Vector();
    public static Vector m_extraTrgFiles = new Vector();
    public static int m_alignmentMode = FileAlignerConstants.UNKNOWN_MODE;
    
    // target directory parameter class
    private class DirParam
    {
        public String m_commonRoot = null;
        public String m_path = null;
        public String m_name = null;
        
        //public int m_rootLen = 0;
        public String m_locale = null;
        public String m_codeset = null;
        
        public FileListBuilder m_fileList = null;
        private File m_curFile = null;
        public String m_curRelativePath = null;
        
        public String getNextRelativeFilePath()
        {
            if ((m_curFile = this.m_fileList.getNextFile()) != null)
            {
                m_curRelativePath = makeCurRelativePath();
            }
            
            return m_curRelativePath;
            
        }
        
        public String getCurRelativeFilePath()
        {
            return m_curRelativePath;
        }
        
        private String makeCurRelativePath()
        {
            int len = m_path.length();
            String tmp = m_curFile.getPath().substring( len );
            tmp = tmp.substring(0, tmp.lastIndexOf(File.separator));
            
            // for consistancy leave it on
            /*if (tmp.equals(File.separator))
            {
                tmp = "";
            }*/
            return tmp;
        }
        
        public String makeBranchPathCurFilename(String p_parrallelSubDir)
        {
            String rtn = null;
            int nameLen = 0;
            
            //File f = new File(m_root);
            //String parent = f.getParent();
            
            //String newPath = parent + File.separator + p_parrallelSubDir;
            
            String newPath = m_commonRoot + File.separator + p_parrallelSubDir;
            if (m_curRelativePath.length() <= 0)
            {
                newPath = newPath + File.separator + m_curFile.getName();
            }
            else
            {
                newPath = newPath + m_curRelativePath + File.separator + m_curFile.getName();
            }
            
            return newPath;
        }
        
        public boolean next()
        {
            boolean rtn = false;
            
            if ((m_curFile = this.m_fileList.getNextFile()) != null)
            {
                m_curRelativePath = makeCurRelativePath();
                rtn = true;
            }
            else
            {
                m_curRelativePath = "";
            }
            
            return rtn;
        }
        
        public String getCurParent()
        {
            return m_curFile.getParent();
        }
        
        public String getCurPath()
        {
            return m_curFile.getPath();
        }
        
        public String getCurFileName()
        {
            return m_curFile.getName();
        }
        
    }

    /**
     * FileAligner constructor comment.
     */
    public FileAligner()
    {
        super();
    }

    /**
     * Align directory/files.
     */
    public void align() throws AlignerException
    {
        DirParam TrgDir = null;
        String srcRelativePath = null;
        int i = 0;
        
        // get remaining source info
        String srcRootDir = m_alignmentRootDir + File.separator + m_srcDir.m_name;
        FileListBuilder SrcFiles = new FileListBuilder();
        SrcFiles.addRecursive(srcRootDir, m_fileExtList);
        
        m_srcDir.m_fileList = SrcFiles;
        m_srcDir.m_commonRoot = m_alignmentRootDir;
        m_srcDir.m_path = srcRootDir;
        
        // get remaining target info
        for (i = 0; i < m_trgDirList.size(); i++)
        {
            TrgDir = (DirParam) m_trgDirList.get(i);
            String trgRootDir = m_alignmentRootDir + File.separator + TrgDir.m_name;
            FileListBuilder TrgFiles = new FileListBuilder();
            TrgFiles.addRecursive(trgRootDir, m_fileExtList);
            
            TrgDir.m_fileList = TrgFiles;
            TrgDir.m_commonRoot = m_alignmentRootDir;
            TrgDir.m_path = trgRootDir;
        }
        
        // do matching and report errors
        while (m_srcDir.next())
        {
            AlignedFiles group = new AlignedFiles();
            
            // pass config params to each group
            group.setFormat(m_fileFormat);
            group.setSrcCodeset(m_srcDir.m_codeset);
            group.setSrcLocale(m_srcDir.m_locale);
            group.setSrcPath(m_srcDir.getCurParent());
            group.setSrcRelativePath(m_srcDir.getCurRelativeFilePath());
            group.setSrcFileName(m_srcDir.getCurFileName());
            group.setJdbcDriver(m_jdbcDriver);
            group.setDbUrl(m_dbUrl);
            group.setImportRootDir(m_dbImportRootDir);
            group.setInsertModifiedSegsOnly(m_dbInsertModifiedSegsOnly);
            group.setUserName(m_userName);
            group.setUserPswd(m_userPswd);
            
            m_sourceFiles.add(m_srcDir.getCurPath());
            m_alignmentResultsList.add(group);
            
            // match for each locale
            for (i = 0; i < m_trgDirList.size(); i++)
            {
                TrgDir = (DirParam) m_trgDirList.get(i);
                File f = new File(m_srcDir.makeBranchPathCurFilename(TrgDir.m_name));
                
                // does target exist for source?
                if (f.exists())
                {
                    m_alignedFiles.add(m_srcDir.getCurPath());
                    FileInfo trgFileItem = new FileInfo();
                    trgFileItem.setCodeset(TrgDir.m_codeset);
                    trgFileItem.setLocale(TrgDir.m_locale);
                    trgFileItem.setPath(f.getParent());
                    trgFileItem.setRelativePath(m_srcDir.getCurRelativeFilePath());
                    trgFileItem.setName(m_srcDir.getCurFileName());
                    group.addTrgFileInfo(trgFileItem);
                }
                else
                {
                    m_missingTrgFiles.add(f);
                }
                
                // do we have extra target files?
                while (TrgDir.next())
                {
                    f = new File(TrgDir.makeBranchPathCurFilename(m_srcDir.m_name));
                    if (f.exists())
                    {
                        //no-op
                    }
                    else
                    {
                        m_extraTrgFiles.add(TrgDir.getCurPath());
                    }
                }
            }
            
        }
    }

    /**
     * Returns the aligment root directory under which the
     * source and target directories to be aligned reside.
     */
    public String getAligmentRootDir()
    {
        return m_alignmentRootDir;
    }

    /**
     * Returns path to the config file.
     */
    public String getConfigPathName()
    {
        return m_configFile;
    }

    /**
     * Get next file alignment
     */
    public AlignedFiles getNextFileGroup() throws AlignerException
    {
        if (m_alignmentResultsList.size() <= 0)
        {
            align();
        }
        
        if (m_groupIdx < m_alignmentResultsList.size())
        {
            return (AlignedFiles) m_alignmentResultsList.get(m_groupIdx++);
        }
        else
        {
            return null;
        }
    }

    /**
     * Load default config file
     */
    public void loadConfigFile(String p_path) throws AlignerException
    {
        AlignerResources messages = new AlignerResources();
        m_configFile = p_path;
        
        try
        {
            ExtractorRegistry ExtractorReg = ExtractorRegistry.getObject();
            
            //messages.setLocale( config.getString(KEY_USER_LOCALE) );
            FileInputStream fis = new FileInputStream(new File(p_path));
            config = new PropertyResourceBundle(fis);
            
            // get Database stuff
            m_jdbcDriver = config.getString(KEY_JDBC_DRIVER).trim();
            if (m_jdbcDriver == null || m_jdbcDriver.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_JDBC_DRIVER + "=" + m_jdbcDriver);
            }
            
            m_dbUrl = config.getString(KEY_DB_URL).trim();
            if (m_dbUrl == null || m_dbUrl.length() <= 0)
            {
                throw new Exception("Invalid parameter: " + KEY_JDBC_DRIVER + "=" + m_dbUrl);
            }
            
            m_userName = config.getString(KEY_USER_NAME).trim();
            if (m_userName == null || m_userName.length() <= 0)
            {
                throw new Exception("Invalid parameter: " + KEY_USER_NAME + "=" + m_userName);
            }
            
            m_userPswd = config.getString(KEY_USER_PSWD).trim();
            if (m_userPswd == null || m_userPswd.length() <= 0)
            {
                throw new Exception("Invalid parameter: " + KEY_USER_PSWD + "=" + m_userPswd);
            }
    
            // Optional - can be empty string
            m_dbImportRootDir = config.getString(KEY_DB_IMPORT_ROOT_DIR).trim();
            if (m_dbImportRootDir == null )
            {
                throw new Exception("Invalid parameter: " + KEY_DB_IMPORT_ROOT_DIR );
            }
            
            // ImportModifiedSegmentsOnly
            String value = config.getString(KEY_DB_IMPORT_MODIFIED_SEGS_ONLY).trim();
            if (value == null )
            {
                value = "";
            }
            
            if( value.toLowerCase().equals("yes") )
            {
                m_dbInsertModifiedSegsOnly = true;
            }
            else if( value.toLowerCase().equals("no") )
            {
                m_dbInsertModifiedSegsOnly = false;
            }
            else
            {
                throw new Exception("Invalid parameter: "
                + KEY_DB_IMPORT_MODIFIED_SEGS_ONLY
                + "=" + value);
            }
            
            
            
            // get common info
            m_fileFormat = config.getString(KEY_FILE_FORMAT).trim();
            if (!ExtractorReg.isValidFormat(m_fileFormat))
            {
                throw new Exception(
                "Invalid parameter: " + KEY_FILE_FORMAT + "=" + m_fileFormat);
            }
            
            m_fileExtList = config.getString(KEY_EXTENSION_LIST).trim();
            if (m_fileExtList == null || m_fileExtList.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_EXTENSION_LIST + "=" + m_fileExtList);
            }
            
            m_tmName = config.getString(KEY_TM_NAME).trim();
            if (m_tmName == null || m_tmName.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_TM_NAME + "=" + m_tmName);
            }
            
            m_alignmentRootDir = config.getString(KEY_ROOT_DIR).trim();
            if (m_alignmentRootDir == null || m_alignmentRootDir.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_ROOT_DIR + "=" + m_alignmentRootDir);
            }
            if (m_alignmentRootDir.endsWith(File.separator))
            {
                m_alignmentRootDir =
                m_alignmentRootDir.substring(0, m_alignmentRootDir.length() - 1);
            }
            
            // get source info
            m_srcDir = new DirParam();
            m_srcDir.m_locale = config.getString(KEY_SRC_LOCALE).trim();
            if (m_srcDir.m_locale == null || m_srcDir.m_locale.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_SRC_LOCALE + "=" + m_srcDir.m_locale);
            }
            
            m_srcDir.m_codeset = config.getString(KEY_SRC_CODESET).trim();
            if (m_srcDir.m_codeset == null || m_srcDir.m_codeset.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_SRC_CODESET + "=" + m_srcDir.m_codeset);
            }
            
            m_srcDir.m_name = config.getString(KEY_SRC_DIR).trim();
            if (m_srcDir.m_name == null || m_srcDir.m_name.length() <= 0)
            {
                throw new Exception(
                "Invalid parameter: " + KEY_SRC_DIR + "=" + m_srcDir.m_name);
            }
            
            // get multiple target info
            try
            {
                int i = 1;
                while (true)
                {
                    DirParam TDP = new DirParam();
                    
                    TDP.m_locale = config.getString(KEY_TRG_LOCALE + "" + i).trim();
                    if (TDP.m_locale == null || TDP.m_locale.length() <= 0)
                    {
                        throw new Exception(
                        "Invalid parameter: " + KEY_TRG_LOCALE + i + "=" + TDP.m_locale );
                    }
                    
                    TDP.m_codeset = config.getString(KEY_TRG_CODESET + "" + i).trim();
                    if (TDP.m_codeset == null || TDP.m_codeset.length() <= 0)
                    {
                        throw new Exception(
                        "Invalid parameter: " + KEY_TRG_CODESET + i + "=" + TDP.m_codeset);
                    }
                    
                    TDP.m_name = config.getString(KEY_TRG_DIR + "" + i).trim();
                    if (TDP.m_name == null || TDP.m_name.length() <= 0)
                    {
                        throw new Exception("Invalid parameter: " + KEY_TRG_DIR + i + "=" + TDP.m_name);
                    }
                    
                    m_trgDirList.add(TDP);
                    i++;
                }
            }
            catch (MissingResourceException e)
            {
                // *** Intentionaly left empty ****
                // Target defs must be incremental.
                // Next incremental target definition not found or incomplet.
            }
            
            if (m_trgDirList.size() <= 0)
            {
                throw new AlignerException(
                AlignerExceptionConstants.PROPERTY_NOT_FOUND,
                messages.getResource(MSG_NO_TRG_DIR));
            }
            
            fis.close();
            
        }
        catch (Exception e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.CONFIG_FILE_NOTFOUND,
            e.toString());
        }
        
    }

    public static void main(String args[])
    throws Exception
    {
        FileAligner fileAligner = new FileAligner();
        AlignedFiles alignedFiles = null;
        SegmentAligner segmentAligner = new SegmentAligner();
        String param = "";
        
        fileAligner.showTitleBar();
        if ( args.length != 2 )
        {
            fileAligner.showUsage();
            System.exit(1);
        }
        else
        {
            for(int i=0; i< args.length  ; i++ )
            {
                param = args[i];
                if( param.toLowerCase().equals("-a") )
                {
                    fileAligner.setModeAnalysis();
                }
                else if( param.toLowerCase().equals("-d") )
                {
                    fileAligner.setModeDirAlign();
                }
                else if( param.toLowerCase().equals("-u") )
                {
                    fileAligner.setModeUpdate();
                }
                else if( param.toLowerCase().equals("-t") )
                {
                    fileAligner.setModeWriteDefaultConfigFile();
                }
                else
                {
                    m_configFile = param;
                }
            }
            
            if( fileAligner.isModeUnknown() )
            {
                fileAligner.showUsage();
                System.exit(1);
            }
            
            if( fileAligner.isModeWriteDefaultConfigFile() )
            {
                fileAligner.writeDefaultConfigFile(m_configFile);
                System.exit(0);
            }
            
            
        }
        
//          try
//          {
            
            // configuration
            fileAligner.loadConfigFile(m_configFile);
            
            // create reporter
            AlignmentReporter reporter = AlignmentReporter.instance();
            reporter.openSummary(m_alignmentRootDir);
            reporter.addSummaryNotificationMsg("FileAligner version " + m_appVersion );
            reporter.enableVerbose();
            
            // prompt user to confirm database update
            if( fileAligner.isModeUpdate()  )
            {
                if( fileAligner.confirmUpdateMode() == false )
                {
                    System.out.println("Update mode aborted...");
                    reporter.addSummaryNotificationMsg("Update mode aborted...");
                    reporter.closeAnalysisSummary();
                    System.exit(1);
                }
            }
            
            // align directories
            System.out.println("Begining directory analysis.");
            fileAligner.align();
            reporter.addSummaryFileAlignmentStats(fileAligner);
            System.out.println("Directory analysis completed.");
            
            if( fileAligner.isModeDirAlign() )
            {
                System.out.println("Final report placed in: " + m_alignmentRootDir);
                reporter.addSummaryNotificationMsg("Directory Analysis completed.");
                reporter.closeAnalysisSummary();
                System.exit(0);
            }
            
            
//              try
//              {
                StoreL10NedPages storeDb = null;
                
                System.out.println(
                "Begining alignment of " + fileAligner.m_sourceFiles.size() + " file(s).");
                
                if( fileAligner.isModeUpdate() )
                {
                    // Establish the connection to DB
                    Class.forName(m_jdbcDriver);
                    Connection connection = DriverManager.getConnection(m_dbUrl, m_userName, m_userPswd);
                    storeDb = new StoreL10NedPages(fileAligner.m_dbInsertModifiedSegsOnly);
                    storeDb.createTM(connection, m_tmName, m_srcDir.m_locale);
                }
                
                // process filegroups
                int progressCount=0;
                while ((alignedFiles = fileAligner.getNextFileGroup()) != null)
                {
                    AlignedPageBlocks pages = segmentAligner.align(alignedFiles);
                    reporter.addSummaryGroupStats(pages);
                    
                    // progress bar
                    System.out.print(((++progressCount % 10) == 0 ? ":" : ".") );
                    if((progressCount % 50) == 0 ) { System.out.print(" " + progressCount + "\n"); }
                    
                    if( fileAligner.isModeUpdate() )
                    {
                        storeDb.storePages(pages);
                    }
                    
                    // an aide to garbage collecting?
                    pages.delete();
                    pages = null;
                    System.gc();
                }
                System.out.print("\n");
                
                
                if( fileAligner.isModeAnalysis() )
                {
                    System.out.println("\nFull Analysis completed.\n"
                    +"Final report placed in: " + m_alignmentRootDir);
                    reporter.addSummaryNotificationMsg("Full Analysis completed.");
                }
                else if( fileAligner.isModeUpdate() )
                {
                    System.out.println("\nDB Update completed.\n"
                    +"Final report placed in: " + m_alignmentRootDir);
                    reporter.addSummaryNotificationMsg("DB Update completed.");
                }
                
//              }
//              catch (Exception e)
//              {
//                  System.out.println(e.toString());
//                  reporter.addSummaryErrMsg(e.toString());
//              }
            
            reporter.closeSummary();
//          }
//          catch (Exception e)
//          {
//              System.out.println(e.toString());
//          }
    }

    /**
     * Prompts the user to confirm the update process.
     */
    public boolean confirmUpdateMode()
    {
        System.out.println("WARNING: You have choosen to UPDATE the database!\n");
        
        System.out.println("          DbUrl.........: " + m_dbUrl);
        System.out.println("          ImportRootDir.: " + m_dbImportRootDir);
        System.out.println("          UserName......: " + m_userName);
        System.out.println("          JdbcDriver....: " + m_jdbcDriver + "\n" );
        
        System.out.print("         Are you sure you want to continue? (y/n): ");
        
        try
        {
            BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
            String line = input.readLine();
            System.out.println(" " );
            
            if( line.toLowerCase().equals("y") )
            {
                return true;
            }
        }
        catch(IOException e)
        {
            return false;
        }
        
        return false;
    }

    /**
     * Returns true when the current mode is full Analysis.
     */
    public boolean isModeAnalysis()
    {
        return (m_alignmentMode == FileAlignerConstants.ANALYSIS_MODE) ? true : false;
    }

    /**
     * Returns true when the current mode is Directory Analysis.
     */
    public boolean isModeDirAlign()
    {
        return (m_alignmentMode == FileAlignerConstants.DIR_MODE) ? true : false;
    }

    /**
     * Returns true when the current mode is Unknown.
     */
    public boolean isModeUnknown()
    {
        return (m_alignmentMode == FileAlignerConstants.UNKNOWN_MODE) ? true : false;
    }

    /**
     * Returns true when the current mode is Update. (Update the db).
     */
    public boolean isModeUpdate()
    {
        return (m_alignmentMode == FileAlignerConstants.UPDATE_MODE) ? true : false;
    }

    /**
     * Returns true when the current mode is to write the default config file.
     */
    public boolean isModeWriteDefaultConfigFile()
    {
        return (m_alignmentMode == FileAlignerConstants.UPDATE_WRITE_CONFIG) ? true : false;
    }

    /**
     * Set the current mode to full Analysis.
     */
    public void setModeAnalysis()
    {
        m_alignmentMode = FileAlignerConstants.ANALYSIS_MODE;
    }

    /**
     * Set the current mode to Directory Analysis.
     */
    public void setModeDirAlign()
    {
        m_alignmentMode = FileAlignerConstants.DIR_MODE;
    }

    /**
     * Set the current mode to Update. (Update the db).
     */
    public void setModeUpdate()
    {
        m_alignmentMode = FileAlignerConstants.UPDATE_MODE;
    }

    /**
     * Set the current mode to write the configuration template. (and then exit).
     */
    public void setModeWriteDefaultConfigFile()
    {
        m_alignmentMode = FileAlignerConstants.UPDATE_WRITE_CONFIG;
    }

    /**
     * Prints the usage instructions to std output.
     */
    public void showUsage()
    {
        
        System.out.println("Usage: FileAligner -d [config file]... Directory analysis only (no DB access).\n"
        +"       FileAligner -a [config file]... Analysis (directories and pages - no DB access).\n"
        +"       FileAligner -u [config file]... Runs the full procces. Updating the database.\n"
        +"       FileAligner -t [config file]... Create a configuration template.\n");
    }

    /**
     * Prints the title bar.
     */
    public void showTitleBar()
    {
        System.out.println("\n\nFileAligner version " + m_appVersion + ", Copyright (c) 2000 GlobalSight Corporation.\n");
    }

    public void writeDefaultConfigFile(String p_fullPath)
    {
        String curDir = null;
        File f = null;
        try
        {
            f = new File(p_fullPath);
            if(f.exists())
            {
                System.out.print("The file already exists. Overwrite(y/n)? : ");
                try
                {
                    BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
                    String line = input.readLine();
                    if( line.toLowerCase().equals("n") )
                    {
                        return;
                    }
                }
                catch(IOException e)
                {
                    System.out.println( e );
                    return;
                }
            }
            //curDir = System.getProperty("user.dir");
                        
            FileOutputStream fos = new FileOutputStream( f );
            PrintWriter pw = new PrintWriter(fos);
       
            pw.println( "############################################################################");
            pw.println( "# Aligner configuration file template.                                      ");
            pw.println( "#                                                                           ");
            pw.println( "# FILL IN THE MISSING VALUES BELOW                                          ");
            pw.println( "#                                                                           ");
            pw.println( "# NOTE: To add an additional Target language, copy/paste the last set of    ");
            pw.println( "#       target keys and increment the index. The first missing target key   ");
            pw.println( "#       terminates the reading of the target directories. The alignment then");
            pw.println( "#       begins using the list of \"sequential\" target entries that         ");
            pw.println( "#       were found. (it was quick and dirty)                                ");
            pw.println( "#                                                                           ");
            pw.println( "#       For example :                                                       ");
            pw.println( "#                                                                           ");
            pw.println( "#       TrgLocale_1  = pt_BR                                                ");
            pw.println( "#       TrgCodeset_1 = Cp1252                                               ");
            pw.println( "#       TrgSubDir_1  = pt_BR                                                ");
            pw.println( "#                                                                           ");
            pw.println( "#       TrgLocale_2  = fr_FR                                                ");
            pw.println( "#       TrgCodeset_2 = Cp1252                                               ");
            pw.println( "#       TrgSubDir_2  = fr_FR                                                ");
            pw.println( "#                                                                           ");
            pw.println( "############################################################################");
            pw.println( "                                                                            ");
            pw.println( "### DATABASE                                                                ");
            pw.println( "                                                                            ");
            pw.println( "JdbcDriver = com.mysql.jdbc.Driver                                ");
            pw.println( "DbUrl = jdbc:mysql://[servername]:[port]/[sid]                         ");
            pw.println( "UserName =                                                                  ");
            pw.println( "UserPswd =                                                                  ");
            pw.println( "                                                                            ");
            pw.println( "# The ImportRootDir refers to the System3 source import directory relative  ");
            pw.println( "# to cap/docs. For instance, if the import dir is cap/docs/en_US, you would ");
            pw.println( "# simply enter en_US below.                                                 ");
            pw.println( "ImportRootDir =                                                             ");
            pw.println( "                                                                            ");
            pw.println( "# Insert segments only if source and target are different, \"yes\" or \"no\"");
            pw.println( "InsertModifiedSegmentsOnly =                                                ");
            pw.println( "                                                                            ");
            pw.println( "                                                                            ");
            pw.println( "### FILE FORMAT                                                             ");
            pw.println( "                                                                            ");
            pw.println( "# For FileFormat, enter one of the following registered extractor names:    ");
            pw.println( "# ( html, xml, css, javascript, javaprop, plaintext )                       ");
            pw.println( "FileFormat = html                                                           ");
            pw.println( "FileExtensionList = .html, .htm, .shtml                                     ");
            pw.println( "                                                                            ");
            pw.println( "                                                                            ");
            pw.println( "### DIRECTORIES AND LOCALES                                                 ");
            pw.println( "                                                                            ");
            pw.println( "# RootDir refers to the common portion of the path under which both the     ");
            pw.println( "# source and target directories reside. Each relative sub directory path    ");
            pw.println( "# defined below is appended to this path to form a complete path.           ");
            pw.println( "RootDir =                                                                   ");
            pw.println( "                                                                            ");
            pw.println( "SrcLocale = en_US                                                           ");
            pw.println( "SrcCodeset = Cp1252                                                         ");
            pw.println( "SrcSubDir = en_US                                                           ");
            pw.println( "                                                                            ");
            pw.println( "TrgLocale_1 =                                                               ");
            pw.println( "TrgCodeset_1 =                                                              ");
            pw.println( "TrgSubDir_1 =                                                               ");
            pw.println( "                                                                            ");
            pw.println( "# To add another target, copy/paste the last of target keys and increment   ");
            pw.println( "# the index sequentially like this:( excluding the pound sign ).            ");
            pw.println( "#TrgLocale_2 = fr_FR                                                        ");
            pw.println( "#TrgCodeset_2 = Cp1252                                                      ");
            pw.println( "#TrgSubDir_2 = fr_FR                                                        ");
            pw.close();
            System.out.println("done");
        }
        catch(IOException e)
        {
            System.out.println( e );        
        }
    
    }
}
