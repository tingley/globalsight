package com.globalsight.selenium.functions;


import java.util.ArrayList;

import org.testng.Reporter;

import com.thoughtworks.selenium.Selenium;

public class BasicFuncs {
	/**
	 * This method is designed to verify if the element can be found on the page
	 * which have many sub-pages.
	 * 
	 * Author:Jester
	 */
	public boolean findElementsOnTables(Selenium selenium, String ielement)
			throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		
		ielement=ielement.trim();

		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}


		while (selenium.isElementPresent(Next_LINK)) {
			try {
				if (selenium.isElementPresent(ielement)) {
					return true;
				}
				selenium.click(Next_LINK);
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
		}

		try {
			if (selenium.isElementPresent(ielement)) {
				return true;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}

	/**
	 * This method is designed to click the radio button if the element can be
	 * found on the page.
	 * 
	 * Author:Jester
	 */
	public boolean selectElementsOnTables(Selenium selenium, String ielement)
			throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		ielement=ielement.trim();
		
		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}

		
		while (selenium.isElementPresent(Next_LINK)) {
			try {
				if (selenium.isElementPresent(ielement)) {
					selenium.click(ielement);
					return true;
				}
				selenium.click(Next_LINK);
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
		}
	
		try {
			if (selenium.isElementPresent(ielement)) {
				selenium.click(ielement);
				return true;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}
	
	/**
	 * This Method is designed to check the radio button with no value
	 * specified.
	 * 
	 * But you must provided the table string and the name string.
	 * 
	 * Author:Jester
	 */
	public boolean selectRadioButtonFromTable(Selenium selenium, String iTable,
			String iName) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		
		iTable=iTable.trim();
		iName=iName.trim();
		
		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}
		
		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[2]"))
					{							
						if (selenium.getText(iTable + "//tr[" + i + "]//td[2]")
								.equals(iName)) {
							selenium.click(iTable + "//tr[" + i
									+ "]//td[1]/input");
							return true;
						}
					}
							
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
			selenium.click(Next_LINK);
		}
	
		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
				if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[2]"))
				{
					if (selenium.getText(iTable + "//tr[" + i + "]//td[2]")
							.equals(iName)) 
					{

							selenium.click(iTable + "//tr[" + i + "]//td[1]//input");	
						return true;
					}						
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}

	/**
	 * This Method is designed to check the radio button with no value
	 * specified.
	 * 
	 * But you must provided the table string and the name string and the TD
	 * number.
	 * 
	 * Author:Jester
	 */
	public boolean selectRadioButtonFromTable(Selenium selenium, String iTable,
			String iName, int iTd) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}
		
		iTable=iTable.trim();
		iName=iName.trim();
		
		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[" + iTd + "]"))
					{							
						if (selenium.getText(
								iTable + "//tr[" + i + "]//td[" + iTd + "]")
								.equals(iName)) {
							selenium.click(iTable + "//tr[" + i
									+ "]//td[1]/input");
							return true;
						}
					}
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
			selenium.click(Next_LINK);
		}
	
		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
				if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[" + iTd + "]"))
				{						
					if (selenium.getText(
							iTable + "//tr[" + i + "]//td[" + iTd + "]").equals(
									iName)) {
						selenium.click(iTable + "//tr[" + i + "]//td[1]/input");
						return true;
					}
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}

	/**
	 * This Method is designed to check if the item presents in the table.
	 * 
	 * But you must provided the table string and the name string. Author:Jester
	 */
	public boolean isPresentInTable(Selenium selenium, String iTable,
			String iName) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";

		iTable=iTable.trim();
		iName=iName.trim();
		
		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}

		
		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[2]"))
					{
						if (selenium.getText(iTable + "//tr[" + i + "]/td[2]").trim()
								.equals(iName)) {
							return true;
						}
					}
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
			selenium.click(Next_LINK);
		}
	
		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
				if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[2]"))
				{						
					if (selenium.getText(iTable + "//tr[" + i + "]//td[2]").trim()
							.equals(iName)) {
						return true;
					}
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}

	/**
	 * This Method is designed to check if the item presents in the table.
	 * 
	 * But you must provided the table string and the name string and the TD
	 * number. Author:Jester
	 */
	public boolean isPresentInTable(Selenium selenium, String iTable,
			String iName, int iTd) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		
		iTable=iTable.trim();
		iName=iName.trim();

		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}

		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[" + iTd + "]"))
					{							
						if (selenium.getText(
								iTable + "//tr[" + i + "]//td[" + iTd + "]").trim()
								.equals(iName)) {
							return true;
						}
					}
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
			selenium.click(Next_LINK);
		}
	
		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
				if (selenium.isElementPresent(iTable + "//tr[" + i + "]//td[" + iTd + "]"))
				{						
					if (selenium.getText(
							iTable + "//tr[" + i + "]//td[" + iTd + "]").trim().equals(
									iName)) {
						return true;
					}
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;

	}

	// Author:Wally
	public void clickAndWait(Selenium selenium, String locator) {
		selenium.click(locator);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	}

	/**
	 * Select Radio Button From Table For Remove Local Pairs
	 * 
	 * Author:Totti
	 */
	public boolean selectRadioForRemove(Selenium selenium, String iTable,
			String iName1, String iName2) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		
		iTable=iTable.trim();
		iName1=iName1.trim();
		iName2=iName2.trim();
		
		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}

		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.getText(iTable + "//tr[" + i + "]//td[2]")
							.trim().equals(iName1.trim())
							&& selenium
									.getText(
											iTable + "//tr[" + i + "]//td[3]")
									.trim().equals(iName2.trim())) {
						selenium.click(iTable + "//tr[" + i
								+ "]//td[1]//input");
						return true;
					}
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return false;
			}
			selenium.click(Next_LINK);
		}
	
		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "/tr[" + i + "]")) {
				if ((selenium.getText(iTable + "//tr[" + i + "]//td[2]")
						.trim().equals(iName1.trim())) 
						&& (selenium.getText(iTable + "//tr[" + i + "]//td[3]")
								.trim().equals(iName2.trim()))){
					selenium.click(iTable + "//tr[" + i + "]//td[1]//input");
					return true;
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return false;
		}
		
		return false;
	}

	/*
	 * 1. Find the row whose name equals iName, 2. Return the text in column iTd
	 * of this row. Author: Shenyang Create Time: 2011-6-22
	 */
	public String getColumnText(Selenium selenium, String iTable, String iName,
			int iTd) throws Exception {
		String Next_LINK = "link=Next";
		String First_LINK = "link=1";
		
		iName=iName.trim();
		iTable=iTable.trim();

		if (selenium.isElementPresent(First_LINK)) {
			selenium.click(First_LINK);
		}


		while (selenium.isElementPresent(Next_LINK)) {
			try {
				int i = 1;
				while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
					if (selenium.getText(
							iTable + "//tr[" + i + "]//td[" + 2 + "]")
							.equals(iName)) {
						return selenium.getText(iTable + "//tr[" + i
								+ "]//td[" + iTd + "]");
					}
					i++;
				}
			} catch (Exception e) {
				Reporter.log(e.toString());
				return null;
			}
			selenium.click(Next_LINK);
		}

		try {
			int i = 1;
			while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
				if (selenium.getText(
						iTable + "//tr[" + i + "]//td[" + 2 + "]").equals(
						iName)) {
					return selenium.getText(iTable + "//tr[" + i + "]//td["
							+ iTd + "]");
				}
				i++;
			}
		} catch (Exception e) {
			Reporter.log(e.toString());
			return null;
		}
	
		return null;
	}
	/*
	 * From Job Name getting corresponding wordcount.
	 * author: Shenyang   2011-08-23
	 */
	public String jobgetWordCount(Selenium selenium, String iTable, String iName,
            int iTd) throws Exception {
        String Next_LINK = "link=Next";
        String First_LINK = "link=1";
        
        iName=iName.trim();
        iTable=iTable.trim();

        if (selenium.isElementPresent(First_LINK)) {
            selenium.click(First_LINK);
        }


        while (selenium.isElementPresent(Next_LINK)) {
            try {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
                    if (selenium.getText(
                            iTable + "//tr[" + i + "]//td[" + 4 + "]")
                            .equals(iName)) {
                        return selenium.getText(iTable + "//tr[" + i
                                + "]//td[" + iTd + "]");
                    }
                    i++;
                }
            } catch (Exception e) {
                Reporter.log(e.toString());
                return null;
            }
            selenium.click(Next_LINK);
        }

        try {
            int i = 1;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
                if (selenium.getText(
                        iTable + "//tr[" + i + "]//td[" + 4 + "]").equals(
                        iName)) {
                    return selenium.getText(iTable + "//tr[" + i + "]//td["
                            + iTd + "]");
                }
                i++;
            }
        } catch (Exception e) {
            Reporter.log(e.toString());
            return null;
        }
    
        return null;
    }
	/**
	 * author:Shenyang 
	 * Use arraylist to get jobname;
	 */
	public ArrayList<String> getAllColumnText(Selenium selenium, String iTable, int iTd) throws Exception {
        String Next_LINK = "link=Next";
        String First_LINK = "link=1";
        
        iTable=iTable.trim();
        ArrayList<String> resultList = new ArrayList<String>();
        
        if (selenium.isElementPresent(First_LINK)) {
            selenium.click(First_LINK);
        }

        while (selenium.isElementPresent(Next_LINK)) {
            try {
                int i = 1;
                while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
                    String textGetted = selenium.getText(iTable + "//tr[" + i + "]//td[" + iTd + "]");
                    resultList.add(textGetted);         
                    i++;
                }
            } catch (Exception e) {
                Reporter.log(e.toString());
                return null;
            }
            selenium.click(Next_LINK);
        }

        try {
            int i = 1;
            while (selenium.isElementPresent(iTable + "//tr[" + i + "]")) {
                String textGetted = selenium.getText(iTable + "//tr[" + i + "]//td[" + iTd + "]");
                resultList.add(textGetted);
                i++;
            }
        } catch (Exception e) {
            Reporter.log(e.toString());
            return null;
        }
    
        return resultList;
    }
}


