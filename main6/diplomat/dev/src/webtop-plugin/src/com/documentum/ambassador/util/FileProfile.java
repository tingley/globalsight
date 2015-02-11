/* Copyright (c) 2004-2005, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
package com.documentum.ambassador.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The <code>FileProfile</code> class describes the a specified file profile,
 * including id, name, a set of file extensions and target locales which
 * associated with a fileprofile.
 */
public class FileProfile {
    
    private String id = null;
    private String name = null;
    private String sourceLocale = null;
    private String description = null;
    /**
     * A set of file extensionswhich associated with a specified file profile.
     */
    private Set fileExtensions = null;
    
    /**
     * A set of target locales which associated with a specified file profile.
     */
    private Set targetLocales = null;
    
    public FileProfile() {
        fileExtensions = new HashSet();
        targetLocales = new HashSet();
    }
    
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    
    public void setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
    }
    public String getSourceLocale() {
        return sourceLocale;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    
    public void addFileExtension(String fileExtension) {
        fileExtensions.add(fileExtension);
    }
    public Set getFileExtensions() {
        return this.fileExtensions;
    }
    
    public void addTargetLocale(String targetLocale) {
        targetLocales.add(targetLocale);
    }
    public Set getTargetLocale() {
        return this.targetLocales;
    }
    
    public String toString() {
        
        StringBuffer result = new StringBuffer();
        result.append("<fileProfile>\r\n");
        result.append("\t<id>").append(id).append("</id>\r\n");
        result.append("\t<name>").append(name).append("</name>\r\n");
        result.append("\t<description>").append(description).append(
                "</description>\r\n");
        
        result.append("\t<fileExtensionInfo>\r\n");
        Iterator iter = fileExtensions.iterator();
        while(iter.hasNext()) {
            result.append("\t\t<fileExtension>").append(iter.next().toString())
                  .append("</fileExtension>\r\n"); 
        }
        result.append("\t</fileExtensionInfo>\r\n");

        result.append("\t<localeInfo>\r\n");
        result.append("\t\t<sourceLocale>").append(sourceLocale).append(
                "</sourceLocale>\r\n");
        iter = targetLocales.iterator();
        while(iter.hasNext()) {
            result.append("\t\t<targetLocale>").append(iter.next().toString())
                  .append("</targetLocale>\r\n"); 
        }
        result.append("\t</localeInfo>\r\n");
        result.append("</fileProfile>");
        
        return result.toString();
    }

}
