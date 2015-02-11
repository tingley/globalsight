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

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * EnvoyImageLoader is responsible for loading images.
 */
public class EnvoyImageLoader
{
    // the media tracker in charge of loading the images
    private static final MediaTracker mediaTracker = new MediaTracker(
            new Component()
            {
                private static final long serialVersionUID = 3992499819476556678L;
            });

    // This is the color value that will be transparent
    private static final int blankPixelColor = 0x00000000;
    private static final int defaultTransparentPixelColor = -1;

    // This is the root for Envoy client images
    private static final Class baseClass = EnvoyApplet.class;
    private static final String IMG_PATH_SUFFIX="com/globalsight/everest/webapp/applet/common/";

    // Create a cache
    private static final Hashtable imageCache = new Hashtable();
    private static final Hashtable filteredImageCache = new Hashtable();

    //
    // Standard Caching Image Retrieval
    //
    /**
     * Get the image for the specified resource settings.  Performs
     * caching using a weak reference cache to avoid re-creating
     * images over and over needlessly.  Uses media tracker to ensure
     * loading occurs before returning.
     * @return the requested image
     * NOTE: used by graphical workflow
     */
    public static Image getImage(URL appletCodeBaseURL,
        final Class baseClass, final String gifFile)
    {
        // Determine the true filename of the image
        String imageFilename = getImageFilename(baseClass, gifFile);
        //System.out.println("Image filename is " + imageFilename);

        // Attempt to lookup the cache
        Image cachedImage = (Image)imageCache.get(imageFilename);
        if (cachedImage != null)
        {
            return cachedImage;
        }

        // this will hold the bytes
        byte[] buffer = null;
        Image image = null;

        try
        {
            // Copy resource into a byte array.  This is necessary
            // because several browsers consider Class.getResource a
            // security risk because it can be used to load additional
            // classes.  Class.getResourceAsStream just returns raw
            // bytes, which we can convert to an image.

            // Get the resource as a stream
            //System.out.println("Reading resource " + gifFile);
            InputStream resource = baseClass.getResourceAsStream(gifFile);

            // If the resource is null, retrieve the image by using
            // the applet.getImage with the specified url.

            if (resource == null)
            {
                //System.out.println("resource is null. getting as URL");
                StringBuffer imageUrl = new StringBuffer(
                    appletCodeBaseURL.toString().substring(0,
                        appletCodeBaseURL.toString().lastIndexOf("classes")));
                imageUrl.append("applet/").append(gifFile);

                URL url = new URL(imageUrl.toString());
                image = Toolkit.getDefaultToolkit().getImage(url);
                //System.out.println("The url looked up in search for image: " + url);
            }
            else
            {
                //System.out.println("resource is not null. reading bytes");

                // Create buffered input stream
                BufferedInputStream in = new BufferedInputStream(resource);

                // Get the available bytes
                int availableBytes = in.available();

                // Create Buffered array output stream, and copy input
                // stream into it
                ByteArrayOutputStream out =
                    new ByteArrayOutputStream(availableBytes);

                buffer = new byte[availableBytes];
                for (int n; (n = in.read(buffer)) > 0 ; )
                {
                    out.write(buffer, 0, n);
                }
                in.close();
                out.flush();

                // Copy the output to the buffer
                buffer = out.toByteArray();

                // Warn if the file is empty
                if (buffer.length == 0)
                {
                    System.err.println("warning: " + gifFile + " is zero-length");
                    return null;
                }

                // create the image.
                image = Toolkit.getDefaultToolkit().createImage(buffer);
            }
        }
        catch (MalformedURLException murle)
        {
            System.out.println("MalformedURLException: " + murle.getMessage());
            return null;
        }
        catch (IOException ioe)
        {
            // Dump any exceptions to console
            System.err.println(ioe.toString());
            return null;
        }

        // Wait for the image to load.
        // If the value is null, return null.
        if (waitForImageLoading(image) == null)
        {
            //System.out.println("Couldn't wait for image. Returning null");
            return null;
        }

        // Put the image into the cache
        imageCache.put(imageFilename, image);

        // Return the new image
        //  //System.out.println("Image is " + image);
        return image;
    }

    //
    // Local Methods
    //
    /**
     * Use the media tracker to wait for the specified image to load.
     *
     * @return The image if loading is successful, null if loading is
     * interrupted or fails.
     */
    private static Image waitForImageLoading(Image image)
    {
        // add the image to the tracker
        mediaTracker.addImage(image, 0);

        // tell the tracker to load the image
        try
        {
            mediaTracker.waitForID(0, 5000);
        }
        catch (InterruptedException e)
        {
            // Interrupted... check status?
        }

        // set the load status
        int loadStatus = mediaTracker.statusID(0, false);

        // remove the image from the tracker
        mediaTracker.removeImage(image, 0);

        // If load status is bad, abort
        if (loadStatus != MediaTracker.COMPLETE)
        {
            return null;
        }

        // Normally we return the image as confirmation that it loaded
        return image;
    }

    private static String getImageFilename(Class baseClass, String gifFile)
    {
        // Should really convert the '.' characters to '/' characters
        return baseClass.getName().toString() + "/" + gifFile;
    }

    //
    // Filtered Image Generators - NON CACHING
    //

    /**
     * Creates an alpha filtered image from an existing image using
     * color value 0x00ffffff as transparent.
     * @return The new image after MediaTracker gets through with it.
     */
    public static Image getFilteredImage(Image originalImage)
    {
        return getFilteredImage(originalImage, defaultTransparentPixelColor);
    }

    /**
     * Creates an alpha filtered image from an existing image using
     * the specified pixel color as transparent.  Performs no caching,
     * but does use media tracker.
     * @return The new image after MediaTracker gets through with it.
     */
    public static Image getFilteredImage(Image originalImage,
        int transparentPixelColor)
    {
        // Return null if no image
        if (originalImage == null) return null;

        // Get the dimensions
        int imageWidth = originalImage.getWidth(new Component() {});
        int imageHeight = originalImage.getHeight(new Component() {});

        // Determine the number of pixels required
        int pixels[] = new int[imageWidth*imageHeight];

        // Create a pixel grabber for the image
        PixelGrabber pixelGrabber = new PixelGrabber(originalImage, 0, 0,
            imageWidth, imageHeight, pixels, 0, imageWidth);

        // Attempt to grab the pixels
        try
        {
            pixelGrabber.grabPixels();
        }
        catch (InterruptedException e)
        {
            // If we can't do it, throw up a message and also dump
            // stack trace, but return original image and go on
            e.printStackTrace();
            return originalImage;
        }

        // Declared outside of loop for speed
        int rowOffset = 0;
        boolean isCurrentPixelTransparent = false;

        // Looop throught the "rows" pixels
        for (int row = 0; row < imageHeight; row++)
        {
            // Determine the start of the row
            rowOffset = row*imageWidth;

            // Loop through the pixels in the row
            for (int x = 0; x < imageWidth; x++)
            {
                // Determine if current pixel is transparent
                isCurrentPixelTransparent =
                    pixels[rowOffset + x] == transparentPixelColor;

                // If the current pixel is transparent, and it with
                // the transparent bitfield to clear it otherwise,
                // don't screw up the normal value
                if (isCurrentPixelTransparent)
                {
                    // Clear the pixel
                    pixels[rowOffset + x] = blankPixelColor;
                }
            }
        }

        // Create the new image
        Image filteredImage = Toolkit.getDefaultToolkit().createImage(
            new MemoryImageSource(imageWidth, imageHeight, pixels, 0, imageWidth));

        // Wait for the image to load.
        if (waitForImageLoading(filteredImage) == null)
            return originalImage;

        // return the image successfully
        return filteredImage;
    }

    //
    // Filtered Image Caching Resource Retrieval
    //

    /**
     * Caching implementation of resource retreival, including
     * transparency filtering.
     * @return The requested image
     */
    public static Image getFilteredImage(URL appletCodeBaseURL,
        final Class baseClass, String gifFile, int transparentPixelColor)
    {
        // Determine the true filename of the image
        String imageFilename = getImageFilename(baseClass, gifFile);

        // This will hold the result
        Image image = null;

        // ONLY USE CACHE FOR STANDARD TRANSPARENCY COLOR
        if (transparentPixelColor == defaultTransparentPixelColor)
        {
            // Attempt to lookup the cache
            image = (Image)filteredImageCache.get(imageFilename);

            if (image != null)
            {
                return image;
            }
        }

        // Create the filtered image icon
        image = getFilteredImage(getImage(appletCodeBaseURL, baseClass, gifFile),
            transparentPixelColor);

        // If it's using standard transparency, we can cache it
        if (transparentPixelColor == defaultTransparentPixelColor)
            filteredImageCache.put(imageFilename, image);

        // Return the result
        return image;
    }

    //
    // Helper Methods
    //

    /**
     * Get the image or icon requested, using the
     * com.globalsight.everest.webapp.applet package directory as the
     * root folder for the images loaded, Calls standard handler but
     * uses fixed baseClass from com.globalsight.everest.webapp.applet
     * package to get the resource.
     * @return the requested image
     */
    public static Image getImage(URL appletCodeBaseURL, String gifFile)
    {
        StringBuffer imageUrl = new StringBuffer(appletCodeBaseURL.toString());
        imageUrl.append(IMG_PATH_SUFFIX);
        imageUrl.append(gifFile);
//        System.out.println("Requesting image " + imageUrl.toString());
        Image i = null;
        try {
            URL url = new URL(imageUrl.toString());
            i= Toolkit.getDefaultToolkit().getImage(url);
        }
        catch (MalformedURLException mue)
        {
            mue.printStackTrace();
        }

        return i;
    }

    /**
     * Get the image or icon requested, using the
     * com.globalsight.everest.webapp.applet package directory as the
     * root folder for the images loaded, Calls standard handler but
     * uses fixed baseClass from com.globalsight.everest.webapp.applet
     * package to get the resource.
     * @return the requested image
     */
    public static Image getFilteredImage(URL appletCodeBaseURL, String gifFile)
    {
        return getFilteredImage(appletCodeBaseURL, baseClass, gifFile);
    }

    /**
     * Shortcut to caching filtered image resource retreival.  Uses
     * default transparency color.
     *
     * @return The requested image
     */
    public static Image getFilteredImage(URL appletCodeBaseURL,
        final Class baseClass, String gifFile)
    {
        return getFilteredImage(appletCodeBaseURL, baseClass, gifFile,
            defaultTransparentPixelColor);
    }

    /**
     * Get the image or icon requested, using the
     * com.globalsight.everest.webapp.applet package directory as the
     * root folder for the images loaded, Calls standard handler but
     * uses fixed baseClass from com.globalsight.everest.webapp.applet
     * package to get the resource.
     * @return the requested image
     */
    public static Image getFilteredImage(URL appletCodeBaseURL, String gifFile,
        int transparentPixelColor)
    {
        return getFilteredImage(appletCodeBaseURL, baseClass, gifFile,
            transparentPixelColor);
    }
}
