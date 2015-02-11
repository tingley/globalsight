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

package com.globalsight.everest.nativefilestore;

// globalsight
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.foundation.User;

import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.io.File;

/**
 * Persistence manager for Native file 
 */
 public interface NativeFileManager
{          
    
    /**
     * Service name for RMI registration.
     */
    public static final String SERVICE_NAME = "NativeFileManager";

    /** 
     * Overwrites the specified SecondaryTargetFile. 
     * @param p_sft The Secondary Target description
     * @param p_tmpFile temporary location of the uploaded file contents 
     * @ param p_user the user who is saving the file
     * @ param p_newFilename an optional new name for this existing file 
     *         Note: passing null will retain the original filename.
     * @throws NativeFileException
     */
    public void save(SecondaryTargetFile p_stf, File p_tmpFile, 
        User p_user, String p_newFilename) 
        throws NativeFileManagerException;

    /** 
     * Overwrites the specified UnextractedFile. 
     * @param p_unextractedFile The UnextractedFile object
     * @param p_tmpFile temporary location of the uploaded file contents
     * @param p_user The user who is saving the updated file.
     * 
     * @throws NativeFileException
     */
    public void save(UnextractedFile p_unextractedFile, File p_tmpFile, User p_user) 
        throws NativeFileManagerException;
    

    /**
     * Saves the given file content to the path relative
     * to the file storage dir, in the specified encoding.
     * It will overwrite the file if it already exists.
     *
     * @param p_fileContent
     *                   file content as a String
     * @param p_encoding Java char encoding, for example "UTF8"
     * @param p_relPath  path relative to the file storage dir, for example "corpus/blah/foo/bar.doc"
     * @return String -- full path to saved file
     * @throws NativeFileManagerException
     */
    public String save(String p_fileContent, String p_encoding, String p_relPath) throws NativeFileManagerException;


    /**
     * Returns the file named by the path
     * relative to the file storage dir
     * 
     * @param p_relPath relative path
     * @return File
     */
    public File getFile(String p_relPath)  throws NativeFileManagerException;


    /**
     * Returns the bytes of the file named by the path
     * relative to the file storage dir
     * 
     * @param p_relPath relative path
     * @return 
     */
     public byte[] getBytes(String p_relPath) throws NativeFileManagerException;

     /**
      * Returns the String of the file named by the path
      * relative to the file storage dir
      * 
      * @param p_relPath relative path
      * @param encoding the encoding of file
      * @return 
      */
     public String getString(String p_relPath, String p_encoding) throws NativeFileManagerException;
     
    /** 
     * Returns an array of bytes representing the complete file contents
     * @param p_stf the secondray Tagrget file description
     * @throws NativeFileException
     * @return the File representing the complete file contents
     */
    public File getFile(SecondaryTargetFile p_stf) 
        throws NativeFileManagerException;
    
    /** 
     * Returns an array of bytes representing the complete file contents
     * @param p_stf the secondray Tagrget file description
     * @throws NativeFileException
     * @return the File representing the complete file contents
     */
    public File getFile(SecondaryTargetFile p_stf, String companyId) 
        throws NativeFileManagerException;

    /** 
     * Returns an array of bytes representing the complete file contents
     * @param p_unextractedFile The unextracted file object
     * @throws NativeFileException
     * @return the File representing the complete file contents
     */
    public File getFile(UnextractedFile p_unextractedFile) 
        throws NativeFileManagerException;
    
    /**
     * Moves a file into the native file store.
     * For internal FileSystem storage, the storage path provided in the 
     * SecondrayTargetFile parameter will be used to store the file relative to
     * the internal storage directory.
     *
     * NOTE: This method is intended to move a temporary file into permanent 
     * storage. Upon success or failure, the temporary file is deleted from its
     * original location.
     *
     * @param p_absolutePath the absolute path of the file that is to be moved.
     * @param p_sft The Secondary Target object to be associated with this file.
     * @return SecondaryTargetFile with the file size and timestamp updated.
     */
    public SecondaryTargetFile moveFileToStorage(String p_absolutePath,
        SecondaryTargetFile p_stf, int p_sourcePageBomType)
        throws NativeFileManagerException;

    /**
     * Moves a file into the native file store.
     * For internal FileSystem storage, the storage path provided in the 
     * UnextractedFile parameter will be used to store the file relative to
     * the internal storage directory.
     *
     * NOTE: This method is intended to copy a file into storage for the
     * application.  The original file is left as is in its original location if
     * the p_removeOriginal parameter is set to 'false'
     * 
     * @param p_absolutePath The absolute path of the file that is to be moved.
     * @param p_unextractedFile The Unextracted File object to be associated with this file.
     * @param p_removeOriginal Specifices if the original file (the 'from' location)
     *        should be removed 'true' or left as is 'false'.
     * @return UnextractedFile with the file size and timestamp updated.
     */
    public UnextractedFile copyFileToStorage(String p_absolutePath,
                                             UnextractedFile p_unextractedFile,
                                             boolean p_removeOriginal)
        throws NativeFileManagerException;

    /**
     * Copies a file into the native file store.
     * The new file will be overwritten if it already exists.
     * 
     * @param p_absolutePathToOriginalFile
     *               absolute path to the original file to copy
     * @param p_relPathToNewFile
     *               relative path to the new file
     * @param p_removeOriginal
     *               if true, then the original file is deleted
     * @return the absolute path of the new (copied) file
     * @exception NativeFileManagerException
     */
    public String copyFileToStorage(String p_absolutePathToOriginalFile,
                                    String p_relPathToNewFile,
                                    boolean p_removeOriginal)
    throws NativeFileManagerException;
}
