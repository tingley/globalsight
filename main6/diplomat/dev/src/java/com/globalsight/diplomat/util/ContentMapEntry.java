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

package com.globalsight.diplomat.util;

/** A ContentMapEntry corresponds to an entry in the content
 * mapping configuration file.
 */
public class ContentMapEntry
{
    /** Normal constructor for creating a ContentMapEntry */
    public ContentMapEntry (String p_fileExtension, String p_contentType,
			    String p_extractionEvent, String p_mergeEvent)
    {
	_fileExtension = p_fileExtension;
	_contentType = p_contentType;
	_extractionEvent = p_extractionEvent;
	_mergeEvent = p_mergeEvent;
    }

    //constructors
    /** Return a string representation */
    public String toString()
    {
	return "ContentType: " + _contentType +
	    " FileExtention: " + _fileExtension +
	    " ExtractEvent: " + _extractionEvent + 
	    " MergerEvent: " + _mergeEvent;
    }

    //accessors
    public String contentType() {return _contentType;}
    public String fileExtension() {return _fileExtension;}
    public String extractionEvent() {return _extractionEvent;}
    public String mergeEvent() {return _mergeEvent;}
    
    //member variables
    private String _contentType; //type of content (HTML,JSP,etc.)
    private String _fileExtension; //file extension (.html, .htm)
    private String _extractionEvent; //Active event from system source adapters
    private String _mergeEvent; //Active event from GSA Target Adapter
}
