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



package com.globalsight.everest.edit.offline.download.HTMLResourcePages;

import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;

import java.io.OutputStream;

public interface DownloadWriterInterface
{
    static public final String RESOURCE_DIR = "resources";
    static public final String JOBS_DIR = "resources/jobs";
    static public final String PAGE_LIST_FILE = "pagelist.html";
    static public final String JOB_LIST_FILE = "joblist.html";
    static public final String SEG_ID_LIST_FILE = "id_list.html";
    static public final String MAP_TABLE_TITLE = "";
    static public final String HTML_RESOURCES_ENCODING = "UTF8";

    public void write(OutputStream p_out)
        throws AmbassadorDwUpException;

    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException;

    public void write(OutputStream p_out, String p_encoding)
        throws AmbassadorDwUpException;
}
