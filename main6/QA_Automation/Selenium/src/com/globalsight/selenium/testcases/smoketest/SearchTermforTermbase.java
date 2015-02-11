package com.globalsight.selenium.testcases.smoketest;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.testcases.BaseTestCase;

public class SearchTermforTermbase extends BaseTestCase
{

    private TerminologyFuncs terminologyFuncs = new TerminologyFuncs();

    @Test
    public void searchTermForTermbase() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TERMINOLOGY_SUBMENU);

        clickAndWait(selenium, TerminologyElements.MAIN_SEARCHTERMS_BUTTON);

        String iterm = getProperty("tb.search.term");
        String sourceLocale = getProperty("tb.search.sourceLocale");
        String targetLocale = getProperty("tb.search.targetLocale");
        String matchType = getProperty("tb.search.matchType");
        String tbName = getProperty("tb.name");

        selenium.select(TerminologyElements.SOURCE_LOCALE, "label="
                + sourceLocale.trim());
        selenium.select(TerminologyElements.TARGET_LOCALE, "label="
                + targetLocale.trim());
        selenium.addSelection(TerminologyElements.SELECTED_TERMBASE, "label="
                + tbName.trim());
        selenium.select(TerminologyElements.MATCH_TYPE,
                "label=" + matchType.trim());
        selenium.type(TerminologyElements.SEARCH_TERM, iterm);

        clickAndWait(selenium, TerminologyElements.Search_Button);

        String result = terminologyFuncs.searchterms(selenium, iterm.trim(), 1);
        Reporter.log(result);
    }
}
