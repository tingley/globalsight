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

package com.globalsight.terminology.exporter;

import org.apache.log4j.Logger;

import com.globalsight.exporter.IExportManager;

import com.globalsight.everest.util.system.SystemConfiguration;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.*;
import java.util.*;

public class ExportUtil
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            ExportUtil.class);

    static public String EXPORT_BASE_DIRECTORY = "/";
    static {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            EXPORT_BASE_DIRECTORY = root + IExportManager.EXPORT_DIRECTORY;

            if (!(EXPORT_BASE_DIRECTORY.endsWith("/") ||
                  EXPORT_BASE_DIRECTORY.endsWith("\\")))
            {
                EXPORT_BASE_DIRECTORY = EXPORT_BASE_DIRECTORY + "/";
            }

            File temp = new File(EXPORT_BASE_DIRECTORY);
            temp.mkdirs();
        }
        catch (Throwable e)
        {
            CATEGORY.error(
                "cannot create directory " + EXPORT_BASE_DIRECTORY);
        }
    }

    /** Static class, private constructor */
    private ExportUtil ()
    {
    }

    //
    // Public Methods
    //

    static public String getExportDirectory()
    {
        return EXPORT_BASE_DIRECTORY;
    }

    /**
     * Removes system fields from a termbase entry. System fields are:
     * <ul>
     * <li>all transactional timestamps (create, modify)
     * </ul>
     */
    static public Document removeSystemFields(Document p_dom)
    {
        Element root = p_dom.getRootElement();

        removeNodes(root, "//transacGrp");

        return p_dom;
    }

    /**
     * Removes nodes identified by the XPath p_path from a DOM Element.
     */
    static private void removeNodes(Element p_element, String p_path)
    {
        List nodes = p_element.selectNodes(p_path);

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = (Node)nodes.get(i);

            node.detach();
        }
    }
}
