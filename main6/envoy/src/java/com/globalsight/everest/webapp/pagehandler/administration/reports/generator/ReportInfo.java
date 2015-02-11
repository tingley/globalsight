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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;

public class ReportInfo 
{
    private File[] files = null;
    private String zipFileName = null;
    private boolean finished = false;
    
	public String getZipFileName() 
	{
		return zipFileName;
	}
	
	public void setZipFileName(String zipFileName) 
	{
		this.zipFileName = zipFileName;
	}

	public boolean isFinished() 
	{
		return finished;
	}

	public void setFinished(boolean finished) 
	{
		this.finished = finished;
	}

	public File[] getFiles() 
	{
		return files;
	}

	public void setFiles(File[] files) 
	{
		this.files = files;
	} 
}
