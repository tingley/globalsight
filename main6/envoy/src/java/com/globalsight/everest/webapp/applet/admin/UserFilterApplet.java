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
package com.globalsight.everest.webapp.applet.admin;
// java
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Vector;
// com.globalsight
import com.globalsight.everest.webapp.applet.common.EnvoyApplet;
import com.globalsight.everest.servlet.ExceptionMessage;
/**
 * UserFilterApplet is a subclass of EnvoyApplet and is strictly used for going from one
 * applet to another.  In our case we only have one applet which is responsible for displaying
 * a particular panel.  Therefore, overriding the init method would cause reloading the applet.
 */

public class UserFilterApplet extends EnvoyApplet
{

    ///////////////////////////////////////////////////////////////////////////////
    // BEGIN: Init -- creating components
    ///////////////////////////////////////////////////////////////////////////////
    public void init()
    {
        super.init();
    }
    ///////////////////////////////////////////////////////////////////////////////
    // End: Init -- creating components
    ///////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////
    // BEGIN: Overriding methods
    ///////////////////////////////////////////////////////////////////////////////
    
    /**
     * Post data and redirect page if necessary.
     * @param p_outgoingData - a vector serializable object to be passed to the servlet.
     * @param p_targetParam - the parameter of the target url.
     * @return An array of serializable objects (as a result of an action).
     */
    public Vector appendDataToPostConnection(Vector p_outgoingData, String p_targetParam)
    {
	Vector incomingData = new Vector();
	String randID = this.getParameter(RANDOM);
        String servletUrl = this.getParameter(SERVLET_URL);
        String targetUrl = this.getParameter(p_targetParam);
        String urlPrefix = getUrlPrefix();
        String servletLocation = urlPrefix+servletUrl+randID;
        String targetLocation = targetUrl == null ? null :
                                urlPrefix+targetUrl+randID;
	try
        {
            String lineRead = null;
            String result = null;
	    //Serializable objs[] = {p_objs, randID};
	    Vector outgoingData = new Vector();
	    outgoingData.addElement(p_outgoingData);
	    outgoingData.addElement(randID);
	    InputStream inputStream = makePostConnectionSend(servletLocation, outgoingData);
	    ObjectInputStream inputStreamFromServlet = new ObjectInputStream(inputStream);
            incomingData = (Vector)inputStreamFromServlet.readObject();

	    if (incomingData != null) {
		if (incomingData.elementAt(0) instanceof ExceptionMessage) {
		    //displayErrorPage((ExceptionMessage)incomingData.elementAt(0));
		}
	    }
	    else
	    {
                // if there's a target url, go there...
                if(targetLocation != null)
                {
		    URL url = new URL(targetLocation);
                }
            }
        }
        catch (IOException ioe)
        {
            getErrorDlg(ioe.getMessage(), null);
            ioe.printStackTrace();
        }
        catch (ClassNotFoundException exception)
        {
            getErrorDlg(exception.getMessage(), null);
            System.err.println(exception);
        }

        return incomingData;
    }
    ///////////////////////////////////////////////////////////////////////////////
    // END: Overriding methods
    ///////////////////////////////////////////////////////////////////////////////
}
