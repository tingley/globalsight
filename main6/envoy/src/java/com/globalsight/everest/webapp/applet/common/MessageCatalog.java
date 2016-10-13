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
package com.globalsight.everest.webapp.applet.common;


//JDK
import java.util.Locale;
import java.util.ResourceBundle;
import java.io.Serializable;

/**
 * Class <code>MessageCatalog</code> is a facility
 * to retrieve the translated text for the supported locale.
 * <P>
 * <PRE>
 * MessageCatalog msgCat = new MessageCatalog("I18nApp");
 * msgCat.setLocale();
 * Label okLabel = new Label(msgCat.getMsg("OK"));
 * </PRE>
 *
 */

public class MessageCatalog implements Serializable
{
    //The locale of this object
    private Locale locale;
    //ResourceBundle for this locale
    private ResourceBundle rb;
    //The product of this application
    private String product;

    /**
     * Constructs a new message catalog, with a given product name
     */
    public MessageCatalog(String aProduct)
    {
        this(aProduct, GlobalEnvoy.getLocale());
    	/*product = aProduct;
    	//locale = Locale.getDefault();
        locale = GlobalEnvoy.getLocale();
    	rb = ResourceBundle.getBundle(product, locale);*/
    }
    /**
     * Constructs a new message catalog, with a given product name and Locale
     */
    public MessageCatalog(String aProduct ,Locale p_locale)
    {
    	product = aProduct;
    	locale = p_locale;//Locale.getDefault();
    	rb = ResourceBundle.getBundle(product, locale);
    }


    /**
     * Retrieve the translated text for the locale
     * @param aMsg input message
     * @param translated message based on locale
     */
    public String getMsg(String aMsg) {
	    //System.err.println("aMsg: " + aMsg);
	    try
	    {
	        String msg = rb.getString(aMsg);
	        //System.err.println("oMsg: " + msg);
	        return msg;
	    }
	    catch (Exception e)
	    {
                //e.printStackTrace();
    	    return aMsg;
	    }
	}
}
