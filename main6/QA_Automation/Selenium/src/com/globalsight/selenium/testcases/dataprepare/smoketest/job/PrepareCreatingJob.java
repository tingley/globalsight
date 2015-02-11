package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

import java.util.ArrayList;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.pages.Workflows;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/**
 * Prepare for Creating Job
 * 
 * @author leon
 * 
 */
public class PrepareCreatingJob extends BaseTestCase
{

    private RatesFuncs ratesFuncs = new RatesFuncs();
    private UsersFuncs usersFuncs = new UsersFuncs();
    private TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();
    private TMFuncs tmFuncs = new TMFuncs();
    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();
    private ProjectsFuncs projectsFuncs = new ProjectsFuncs();
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();
    private FilterConfigurationFuncs filterConfigurationFuncs = new FilterConfigurationFuncs();
    private FileProfileFuncs fileProfileFuncs = new FileProfileFuncs();
    private WorkflowsFuncs workflowFuncs = new WorkflowsFuncs();
    
    private String properties = "com.globalsight.selenium.properties.dataprepare.smoketest.job.PrepareCreatingJob";

    @Test
    public void prepareCreatingJob() throws Exception
    {
        ConfigUtil.setPropertyFile(properties);
        String companyName = ConfigUtil.getProperty("user.company");
        
		// create Rate
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.RATES_SUBMENU);

		ratesFuncs.newRate(selenium, getProperty("rate.transRate"));
		ratesFuncs.newRate(selenium, getProperty("rate.viewerRate"));

		// create user
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.USERS_SUBMENU);

		usersFuncs.newUsers(selenium, getProperty("user.user1"));
		usersFuncs.newUsers(selenium, getProperty("user.user2"));

		// create TM
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.TRANSLATION_MEMORY_SUBMENU);

		tmFuncs.newTM(selenium, getProperty("tm.tm"));
		tmFuncs.importTM(selenium, getProperty("tm.importFile"));

		// create TM Profile
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

		tmProfileFuncs.newTMProfile(selenium, getProperty("tmp.profile"));

		// create Termbase
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.TERMINOLOGY_SUBMENU);
		terminologyFuncs.create(selenium, getProperty("tb.termbase"));

		// create project
		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.PROJECTS_SUBMENU);
		projectsFuncs.newProject(selenium, getProperty("project.project"));
		
         //import workflow
		 openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
		 MainFrame.WORKFLOWS_SUBMENU);
		 workflowFuncs.importWorkFlow(selenium,
		 ConfigUtil.getPath(getProperty("workflow.importFile")),
		         getProperty("workflow.workflow"), "Pwf1_en_US_de_DE");
//		openMenuItemAndWait(selenium, MainFrame.SETUP_MENU, MainFrame.WORKFLOWS_SUBMENU);
//		String workflowTemplate = ConfigUtil.getProperty("workflow.duplicate.template");
//		boolean check = SeleniumUtils.selectRadioButtonFromTable(selenium, workflowFuncs.MAIN_TABLE, workflowTemplate);
//		if (!check) {
//			Reporter.log("Cannot find the workflow template to duplicate!");
//			return;
//		}
//		selenium.click(Workflows.Duplicate_BUTTON);
//		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
//		selenium.type(Workflows.Name_TEXT_FIELD_DUPLICATE, getProperty("workflow.duplicate.prefix"));
//		selenium.select(Workflows.Project_SELECTION_DUPLICATE, getProperty("project.name"));
//		selenium.select(Workflows.SourceLocle_SELECTION_DUPLICATE, ConfigUtil.getConfigData("en_US"));
//		String targetLocales = getProperty("workflow.duplicate.targetLocales");
//		String[] locales = targetLocales.split(",");
//		for (String locale : locales) {
//			selenium.addSelection(Workflows.TargetLocale_SELECTION_DUPLICATE, locale);
//		}
//		selenium.click(Workflows.Add_BUTTON_DUPLICATE);
//		selenium.click(Workflows.Save_BUTTON_DUPLICATE);
//		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // create Localization Profile
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        localizationFuncs.create2(selenium,
                getProperty("localizationProfile.profile"));

        // create filter
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILTER_CONFIGURATION_SUBMENU);

        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.doc"), "msofficedoc");
        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.excel"), "msofficeexcel");
        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.ppt"), "msofficepowerpoint");
        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.openOffice"), "openoffice");

        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.office2010"), "msoffice2010");

        filterConfigurationFuncs.newFilters2(selenium,
                getProperty("filter.pptx2010"), "msoffice2010");

        // create File Profile
        openMenuItemAndWait(selenium, MainFrame.DATA_SOURCES_MENU,
                MainFrame.FILE_PROFILES_SUBMENU);

        ArrayList<String> array = new ArrayList<String>();
        array.add(getProperty("fileProfile.names"));
        array.add(getProperty("fileProfile.LocalizationProfileNames"));
        array.add(getProperty("fileProfile.sourceFileFormats"));
        array.add(getProperty("fileProfile.fileExtensions"));
        array.add(getProperty("fileProfile.fileDescriptions"));
        fileProfileFuncs.setup(array);
        fileProfileFuncs.create(selenium);
    }
}
