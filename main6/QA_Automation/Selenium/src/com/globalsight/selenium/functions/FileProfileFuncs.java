package com.globalsight.selenium.functions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import jodd.typeconverter.impl.BooleanConverter;
import jodd.util.StringUtil;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class FileProfileFuncs extends BasicFuncs
{
    private ArrayList<String> propertyNameArray = new ArrayList<String>();
    private static ArrayList<String> XML_FILE_TYPES = new ArrayList<String>();
    private static ArrayList<String> NEED_USE_UTF8 = new ArrayList<String>();

    static
    {
        if (XML_FILE_TYPES.size() == 0)
        {
            XML_FILE_TYPES.add("HTML");
            XML_FILE_TYPES.add("XML");
            XML_FILE_TYPES.add("RESX");
            XML_FILE_TYPES.add("JavaProperties (HTML)");
        }

        if (NEED_USE_UTF8.size() == 0)
        {
            NEED_USE_UTF8.add("Excel2003");
            NEED_USE_UTF8.add("Excel2007");
            NEED_USE_UTF8.add("HTML");
            NEED_USE_UTF8.add("InDesign Markup (IDML)");
            NEED_USE_UTF8.add("INX (CS2)");
            NEED_USE_UTF8.add("INX (CS3)");
            NEED_USE_UTF8.add("MIF 9");
            NEED_USE_UTF8.add("Office2010 document");
            NEED_USE_UTF8.add("OpenOffice document");
            NEED_USE_UTF8.add("Passolo 2011");
            NEED_USE_UTF8.add("PowerPoint2003");
            NEED_USE_UTF8.add("PowerPoint2007");
            NEED_USE_UTF8.add("Un-extracted");
            NEED_USE_UTF8.add("Word2003");
            NEED_USE_UTF8.add("Word2007");
        }
    }

    public void setup(ArrayList<String> propertyNameArray)
    {
        this.propertyNameArray = propertyNameArray;
    }

    /**
     * Create new file profiles
     * 
     * @param selenium
     * @param testCaseName
     */
    public void create(Selenium selenium)
    {
        String fileProfileNames = propertyNameArray.get(0);
        String descriptions = propertyNameArray.get(1);
        String localProfiles = propertyNameArray.get(2);
        String sourceFormats = propertyNameArray.get(3);
        String filters = propertyNameArray.get(4);
        String qa_filter = propertyNameArray.get(5);
        String extensions = propertyNameArray.get(6);
        
        

        String[] fileProfileNameArray = fileProfileNames.split(",");
        String[] descriptionArray = descriptions.split(",");
        String[] localProfileArray = localProfiles.split(",");
        String[] sourceFormatArray = sourceFormats.split(",");
        String[] filterArray = filters.split(",");
        String[] qa_filterArray = qa_filter.split(",");
        String[] extensionArray = extensions.split(",");
        

             

       

        if (descriptions != null)
        {
            descriptionArray = descriptions.split(",");
        }

        for (int i = 0; i < fileProfileNameArray.length; i++)
        {
        	selenium.type(FileProfile.SEARCH_CONTENT_TEXT, fileProfileNameArray[i]);
        	selenium.keyDown(FileProfile.SEARCH_CONTENT_TEXT, "\\13");
        	selenium.keyUp(FileProfile.SEARCH_CONTENT_TEXT, "\\13");
//        	selenium.waitForFrameToLoad("css=table.listborder", "1000");

        	 if (selenium.isElementPresent("link=" + fileProfileNameArray[i])){
     			continue;
     		} 
        	clickAndWait(selenium, FileProfile.NEW_BUTTON);
            selenium.type(FileProfile.NAME_TEXT, fileProfileNameArray[i]);

            if (descriptionArray != null && (i < descriptionArray.length - 1))
                selenium.type(FileProfile.DESCRIPTION_TEXT,
                        fileProfileNameArray[i]);

            selenium.select(FileProfile.LOCALIZATION_PROFILE_SELECT, "label="
                    + localProfileArray[i]);
            selenium.select(FileProfile.SOURCE_FILE_FORMAT_SELECT, "label="
                    + sourceFormatArray[i]);
            if (!(filterArray[i].equalsIgnoreCase("x"))){
            	selenium.select(FileProfile.FILTER_SELECT, "label=" 
                		+ filterArray[i]);	
            }
            if (!(qa_filterArray[i].equalsIgnoreCase("x"))){
            	selenium.select(FileProfile.QA_FILTER_SELECT, "label=" 
                		+ qa_filterArray[i]);	
            }
            
            selenium.select(FileProfile.SOURCE_FILE_ENCODING_SELECT,
                    "label=UTF-8");

            String[] tmp = extensionArray[i].split(";");
            for (int j = 0; j < tmp.length; j++)
            {
                selenium.addSelection(FileProfile.FILE_EXTENSION_TYPE_SELECT,
                        "label=" + tmp[j]);
            }
            // save and check
            selenium.click(FileProfile.SAVE_BUTTON);
            if (selenium.isAlertPresent())
            {
                Reporter.log(selenium.getAlert());
                // Assert.assertTrue(false, selenium.getAlert());
                clickAndWait(selenium, FileProfile.CANCEL_BUTTON);
                continue;
            }
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
    }

    public void remove(Selenium selenium, String iFileProfileNames)
            throws Exception
    {

        String fileProfileName[] = iFileProfileNames.split(",");
        for (String iFileProfileName : fileProfileName)
        {
            System.out.println(iFileProfileName);
            boolean selected = selectRadioButtonFromTable(selenium,
                    FileProfile.MAIN_TABLE, iFileProfileName);
            if (!selected)
            {
                Reporter.log("Cannot find a proper file profile "
                        + iFileProfileName + " to remove.");
                continue;
            }
            else
            {
                clickAndWait(selenium, FileProfile.REMOVE_BUTTON);
                Assert.assertEquals(
                        (selenium.getConfirmation()
                                .equals("Are you sure you want to remove this File Profile?")),
                        true);
            }
            // verify
            // boolean selected2 = selectRadioButtonFromTable(selenium,
            // FileProfileElements.MAIN_TABLE, iFileProfileName);
            // if (!selected2)
            // {
            // Reporter.log("The file profile "+iFileProfileName+" was removed successfully.");
            // continue;
            // }
        }
    }

    public void create(Selenium p_selenium, ArrayList<FileProfile> p_fpList)
    {
        if (p_fpList == null || p_fpList.size() == 0)
            return;

        for (FileProfile fp : p_fpList)
        {
            create(p_selenium, fp);
        }
    }

    public void create(Selenium p_selenium, FileProfile p_fp)
    {
        if (!FileProfile.TITLE.equals(p_selenium.getTitle()))
        {
            p_selenium.click(MainFrame.DATA_SOURCES_MENU);
            clickAndWait(p_selenium, MainFrame.FILE_PROFILES_SUBMENU);
        }

        clickAndWait(p_selenium, FileProfile.NEW_BUTTON);
        p_selenium.type(FileProfile.NAME_TEXT, p_fp.getName());
        p_selenium.type(FileProfile.DESCRIPTION_TEXT, p_fp.getDesc());
        p_selenium.select(FileProfile.LOCALIZATION_PROFILE_SELECT, "label="
                + p_fp.getLocalizationProfile());

        String tmp = p_fp.getSourceFileFormat();
        p_selenium
                .select(FileProfile.SOURCE_FILE_FORMAT_SELECT, "label=" + tmp);
        if (XML_FILE_TYPES.contains(tmp))
        {
            // Need some additional field for xml file format
        }
        tmp = p_fp.getFilter();
        if (StringUtil.isNotEmpty(tmp))
            p_selenium.select(FileProfile.FILTER_SELECT, "label=" + tmp);

        p_selenium.select(FileProfile.SOURCE_FILE_ENCODING_SELECT,
                p_fp.getSourceFileEncoding());
        tmp = p_fp.getFileExtensions();
        String[] exts = tmp.split(",");
        for (String ext : exts)
        {
            p_selenium.addSelection(FileProfile.FILE_EXTENSION_TYPE_SELECT,
                    "label=" + ext);
        }

        p_selenium.click(FileProfile.SAVE_BUTTON);
        if (p_selenium.isAlertPresent())
        {
            Reporter.log(p_selenium.getAlert());
            Assert.assertTrue(false, p_selenium.getAlert());
            clickAndWait(p_selenium, FileProfile.CANCEL_BUTTON);
        }
        p_selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }

    public void createWithSQL(Hashtable<String, String> p_params)
    {
        StringBuilder insertFields = new StringBuilder(
                "insert into file_profile (");
        StringBuilder insertValues = new StringBuilder("values (");
        String field = "", value = "";

        for (Iterator<String> fields = p_params.keySet().iterator(); fields
                .hasNext();)
        {
            field = fields.next();
            value = p_params.get(field);
            insertFields.append("'" + field + "',");
            insertValues.append("'" + value + "',");
        }
        insertFields.deleteCharAt(insertFields.length());
        insertValues.deleteCharAt(insertValues.length());
        insertFields.append(")");
        insertValues.append(")");

        String sql = insertFields.toString() + " " + insertValues.toString();
        Connection conn = null;
        try
        {
            conn = CommonFuncs.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
        catch (Exception e)
        {
            Reporter.log("Cannnot insert file profile correctly.");
        }
        finally
        {
            CommonFuncs.freeConnection(conn);
        }
    }

    public long getKnownFormatTypeId(String p_name)
    {
        long knownFormatTypeId = -1l;
        Connection conn = null;
        try
        {
            conn = CommonFuncs.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt
                    .executeQuery("select id from known_format_type where name='"
                            + p_name + "'");
            if (rs.next())
                knownFormatTypeId = rs.getLong(1);
            stmt.close();
        }
        catch (Exception e)
        {
            Reporter.log("Cannot get knownFormatTypeId for " + p_name
                    + " correctly. ");
        }
        finally
        {
            CommonFuncs.freeConnection(conn);
        }
        return knownFormatTypeId;
    }

    /**
     * Get file profile object from testcase properties file
     * 
     * @param testCaseName
     *            Test case name which is used to get properties file
     * @return FileProfile File profile object
     */
    public FileProfile getFileProfileInfo(String testCaseName)
    {
        return getFileProfileInfo(testCaseName, null);
    }

    /**
     * Get file profile object from testcase properties file
     * 
     * @param testCaseName
     *            Test case name which is used to get properties file
     * @param suffix
     *            Suffix string which can read different set of configurations
     * @return FileProfile File profile object
     */
    public FileProfile getFileProfileInfo(String testCaseName, String suffix)
    {
        FileProfile fileProfile = new FileProfile();

        if (StringUtil.isEmpty(suffix))
            suffix = "";

        fileProfile.setName(ConfigUtil.getDataInCase(testCaseName,
                "fileProfileName" + suffix));
        fileProfile.setDesc(ConfigUtil.getDataInCase(testCaseName,
                "fileProfileDesc" + suffix));
        fileProfile.setLocalizationProfile(ConfigUtil.getDataInCase(
                testCaseName, "localizationProfile" + suffix));
        fileProfile.setSourceFileFormat(ConfigUtil.getDataInCase(testCaseName,
                "sourceFileFormat" + suffix));
        fileProfile.setSourceFileEncoding(ConfigUtil.getDataInCase(
                testCaseName, "sourceFileEncoding" + suffix));
        fileProfile.setFilter(ConfigUtil.getDataInCase(testCaseName,
                "fileProfileFilter" + suffix));
        fileProfile.setFileExtensions(ConfigUtil.getDataInCase(testCaseName,
                "fileExtensions" + suffix));
        fileProfile.setUtf8Bom(ConfigUtil.getDataInCase(testCaseName, "utf8Bom"
                + suffix));
        fileProfile.setXlsFile(ConfigUtil.getDataInCase(testCaseName, "xlsFile"
                + suffix));
        fileProfile.setXmlDtd(ConfigUtil.getDataInCase(testCaseName, "xmlDtd"
                + suffix));
        fileProfile.setScriptOnImport(ConfigUtil.getDataInCase(testCaseName,
                "scriptOnImport" + suffix));
        fileProfile.setScriptOnExport(ConfigUtil.getDataInCase(testCaseName,
                "scriptOnExport" + suffix));
        fileProfile.setFileExtensionType(ConfigUtil.getDataInCase(testCaseName,
                "fileExtensionType" + suffix));
        String tmp = ConfigUtil.getDataInCase(testCaseName,
                "terminologyApproval" + suffix);
//        if (StringUtil.isNotEmpty(tmp))
//            fileProfile.setTerminologyApproval(BooleanConverter.valueOf(tmp));

        return fileProfile;
    }

    /**
     * Verify if the file profile with special name exists in DB
     * 
     * @param fileProfileName
     *            Name of file profile
     * @return Ture is returned if the file profile with special name exists,
     *         otherwise return false
     */
    public boolean existFileProfile(String fileProfileName)
    {
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = CommonFuncs.getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt
                    .executeQuery("select * from file_profile where is_active='Y' and name='"
                            + fileProfileName + "'");
            return rs.next();
        }
        catch (Exception e)
        {
            Reporter.log("Cannot check if exists file profile "
                    + fileProfileName);
            return false;
        }
        finally
        {
            try
            {
                stmt.close();
                CommonFuncs.freeConnection(conn);
            }
            catch (Exception e2)
            {
            }
        }
    }
}
