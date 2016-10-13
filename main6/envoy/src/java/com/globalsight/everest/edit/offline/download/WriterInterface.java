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



package com.globalsight.everest.edit.offline.download;

import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;

import java.io.OutputStream;
import java.util.Locale;
import java.io.IOException;

public interface WriterInterface
{
    /**
     * Sets a new OfflinePageData and writes the formatted contents to
     * the output stream.
     * @param p_page the OfflinePageData object that will be written.
     * @param p_outputStream the stream to write to
     * @param p_uiLocale the locale used to write non-translatable instructions
     */
    public void write(OfflinePageData p_page, OutputStream p_outputStream,
        Locale p_uiLocale)
        throws IOException, AmbassadorDwUpException;
}
