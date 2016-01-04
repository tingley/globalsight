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

package com.globalsight.everest.edit;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuType;

/**
 * Methods for handling original and uploaded images, which are shared
 * between the server-side editor code and the page handlers in the
 * UI.
 */
public class ImageHelper
{
	private static Logger s_logger = Logger.getLogger(ImageHelper.class);

    /**
     * Private constructor: this class can not be instantiated.
     */
    private ImageHelper()
    {
    }

    /**
     * <P>Determines which TU types (item types) are displayed in the
     * online editor's image editor.</P>
     */
    static public boolean isImageItemType(String p_item)
    {
        if (p_item == null || p_item.length() == 0)
        {
            return false;
        }

        if (p_item.equals(TuType.URL_IMG.getName()) ||
            p_item.equals(TuType.URL_INPUT.getName()))
        {
            return true;
        }

        return false;
    }

    /**
     * <P>When uploading an image for a segment (a URL), this method
     * computes the new value of the url based on the upladed image's
     * file name.
     *
     * Here's the table:
     *
     * SEGMENT                       | NAME     | Result
     * ---------------------------------------------------------------
     * http://x.com/images/image.gif | bild.jpg | bild.jpg
     * /images/image.gif             | bild.jpg | /images/bild.jpg
     * ../images/image.gif           | bild.jpg | ../images/bild.jpg
     * image.gif                     | bild.jpg | bild.jpg
     *
     * @param p_segment the content of the original segment,
     * e.g. "/image/bla.gif"
     * @param p_name the name of the uploaded image
     * e.g. "blu.jpg"
     * @return String the new value of the segment
     * e.g. "/image/blu.jpg"
     */
    static public String mergeSegmentAndFilename(String p_url, String p_name)
    {
        if (hasProtocol(p_url))
        {
            return p_name;
        }

        return getDirectory(p_url) + p_name;
    }

    /**
     * Called when modifying an image URL when an image has been
     * uploaded: ensures the new image url is relative and has the
     * same extension or an absolute path
     *
     * This method also serves to handle error checking, although
     * there is currently no mechanism to communicate error messages
     * back to the user. The update will just "not stick".
     */
    static public String mergeImages(String p_old, String p_new)
    {
        // this is an absolute path with "http:" at the beginning
        // no merging needed - just keep the new URL as is.
        if (hasProtocol(p_new))
        {
            return p_new;
        }

        int oldDot = p_old.lastIndexOf(".");
        int newDot = p_new.lastIndexOf(".");
        int newSlash = p_new.lastIndexOf("/");

        if (newDot < newSlash || newDot == newSlash + 1)
        {
            // no filename or no extension
            return p_old;
        }

        String extension = p_old.substring(oldDot);
        return p_new.substring(0, newDot) + extension;
    }

    /**
     * <P>Converts a image url in a segment to a display url that can
     * be used inside an IMG tag.  Relative urls are made absolute
     * relative to the System 4 docs directory, absolute and FQ urls
     * are left as is.</P>
     *
     * <P>The decision table looks as follows: </P>
     * <PRE>
     * INT = page-internal base href
     * EXT = location of file relative to System 4's docroot
     * EXT(1) = first directory ( = root of files in source language)
     * DOC = System 4's docroot directory relative to Weblogic's
     *       public_html directory (spelled "/docs" in urls)
     * URL = the image url as specified in the image tag
     *
     * INT          | EXT        | URL         | Result
     * ------------------------------------------------------------------
     * -            | en-US/dir/ | http://     | url
     *              | en-US/dir/ | /x          | doc + ext(1) + url
     *              | en-US/dir/ | ../x        | doc + ext + url
     * ------------------------------------------------------------------
     * /x           | en-US/dir/ | http://     | url
     *              | en-US/dir/ | /x          | doc + ext(1) + url
     *              | en-US/dir/ | ../x        | doc + ext(1) + int + url
     * ------------------------------------------------------------------
     * ../          | en-US/dir/ | http://     | url
     *              | en-US/dir/ | /x          | doc + ext(1) + url
     *              | en-US/dir/ | ../x        | doc + ext + int + url
     * ------------------------------------------------------------------
     * http://      | en-US/dir/ | http://     | url
     *              | en-US/dir/ | /x          | int + url
     *              | en-US/dir/ | ../x        | int + url
     * ------------------------------------------------------------------
     * </PRE>
     *
     * @param p_url The url from a web page's image tag.
     * @param p_doc The docroot where to find a local image (either
     * System 4 doc directory - a virtual directory
     * @param p_int The page's internal BASE HREF. Cannot be null but
     * can be the empty string.
     * @param p_ext The page's external BASE, which is a subdirectory
     * relative to System 4's document directory. Cannot be null but
     * can be the empty string.
     */
    static public String getDisplayImageUrl(String p_url, String p_doc,
        String p_int, String p_ext)
    {
        String docRoot = "/ambassador" + p_doc;
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("p_url=" + p_url);
            s_logger.debug("docRoot=" + docRoot);
            s_logger.debug("p_int=" + p_int);
            s_logger.debug("p_ext=" + p_ext);            
        }

        try
        {
            if (hasProtocol(p_url))
            {
                // I win, said the fully qualified url
                return p_url;
            }
            else if (hasProtocol(p_ext))
            {
                return p_ext + "/" +  p_url;
            }

            // Docroot relative to Weblogic's (or System 4's) "docs"
            // directory.  The File servlet is registered under the
            // virtual name "cxedocs" in web.xml to allow us
            // access to files in the System 4 docs directory.
            String ext1 = getFirstDirectory(p_ext);

            if (isUndefined(p_int))
            {
                if (isAbsolute(p_url)) return docRoot + ext1  + p_url;
                else /* relative url*/ return docRoot + p_ext + p_url;
            }
            if (isAbsolute(p_int))
            {
                if (isAbsolute(p_url)) return docRoot + ext1 + p_url;
                else /* relative url*/ return docRoot + ext1 + p_int + p_url;
            }

            if (hasProtocol(p_int))
            {
                return p_int + p_url;
            }
            else
            {
                if (isAbsolute(p_url)) return docRoot + ext1 + p_url;
                else /* relative url*/ return docRoot + ext1 + p_int + p_url;
            }
        }
        catch (Exception e)
        {
            s_logger.error("ImageHelper error determining image display url.",
                e);
            return "";
        }
    }

    static public boolean hasProtocol(String p_url)
    {
        return p_url.startsWith("http:");
    }      

    //
    // Private Methods
    //

    static private boolean isUndefined(String p_string)
    {
        return (p_string == null || p_string.length() == 0 || p_string.trim().length() == 0);
    }
   
    static private boolean isAbsolute(String p_url)
    {
        return p_url.startsWith("/");
    }

    /**
     * Extracts the first directory from a path of the form
     * "en-US/dir1/dir2/", e.g. "en-US".
     */
    static private String getFirstDirectory(String p_path)
    {
        if (p_path == null)
        {
            return "";
        }

        int index = p_path.indexOf('/');

        if (index < 0)
        {
            index = p_path.indexOf('\\');
        }

        if (index < 0)
        {
            return p_path;
        }

        return p_path.substring(0, index);
    }

    /**
     * Extracts the directory from a path of the form
     * "../en-US/dir1/dir2/file.ext", e.g. "../en-US/dir1/dir2/".
     * The result includes the trailing path separator "/".
     */
    static private String getDirectory(String p_path)
    {
        if (p_path == null)
        {
            return "";
        }

        int index = p_path.lastIndexOf('/');

        if (index < 0)
        {
            index = p_path.lastIndexOf('\\');
        }

        if (index < 0)
        {
            return "";
        }

        return p_path.substring(0, index + 1);
    }
}
