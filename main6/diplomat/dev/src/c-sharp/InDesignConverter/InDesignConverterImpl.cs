//                             -*- Mode: Csharp -*- 
// 
// Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
// 
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
// 
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
// 

using System.Collections.Generic;
using System.ServiceProcess;
using System.Threading;
using System.Text;
using System.IO;
using System;
using GlobalSight.Common;

namespace GlobalSight.InDesignConverter
{
    /// <summary>
    /// Implements the Converter interface. Can be used to convert
    /// INDD->XML
    /// </summary>
    public class InDesignConverterImpl : Converter
    {
        private Logger m_log = null;

        // Constants
        public const string EXPORT_FILE_EXT = "*.ex_command";
        public const string IMPORT_FILE_EXT = "*.im_command";

        //10 chars after . for both import(*.im_command) and export(*.ex_command)
        private const int FILE_EXT_LEN = 10;
        public enum ConversionType { IMPORT, EXPORT };


        private string m_originalFileName = null;
        private string m_newFileName = null;

 //       private object m_newFormatType = null;
        private string m_fileExtensionSearchPattern = null;
        private ConversionType m_conversionType = ConversionType.IMPORT;
        private bool m_handleTrackedChanges = false;
        private bool m_acceptTrackedChanges = false;
        private string m_statusFileName = null;
        
		/// <summary>
		/// Creates an InDesignConverterImpl to be used for import or export.
		/// </summary>
		/// <param name="p_conversionType"> the type of conversion 
		/// (import or export)</param>
		public InDesignConverterImpl(ConversionType p_conversionType)
		{
			m_log = Logger.GetLogger();
			m_conversionType = p_conversionType;

			if (m_conversionType == ConversionType.EXPORT)
			{
				m_fileExtensionSearchPattern = EXPORT_FILE_EXT;
			}
			else
			{
				m_fileExtensionSearchPattern = IMPORT_FILE_EXT;
			}
		}

        /// <summary>
        /// Converts the file from one format to another.
        /// </summary>
        /// <param name="p_filename">The command file 
        /// (.im_command, .ex_command)</param>
        /// <param name="p_language">the language of the directory 
        /// the file was written in</param>
        public void Convert(string p_fileName, string p_language)
        {
            try
            {
                ResetState();
                m_statusFileName = p_fileName.Substring(
                    0, p_fileName.Length - FILE_EXT_LEN) + "status";
                DetermineConversionValues(p_fileName);

                m_log.Log("The converter will process file: " + m_originalFileName + "\r\n");

                InDesignApplication indesignApp = InDesignApplication.getInstance();
                if (m_conversionType == ConversionType.EXPORT)
                {
                    indesignApp.ConvertXmlToIndd(m_originalFileName, m_newFileName);
                }
                else
                {
                    indesignApp.ConvertInddToXml(m_originalFileName, m_newFileName);
                }

                StatusFile.WriteSuccessStatus(m_statusFileName,
                    m_originalFileName + " was converted successfully.");

                m_log.Log("Converted successfully to: " + m_newFileName);
            }
            catch (Exception e)
            {
                Logger.LogError("InDesign Conversion Failed", e);
                StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
            }
            finally
            {
                DeleteInputFile(p_fileName);
            }
        }

        /// <summary>
        /// Sets all internal state back to null
        /// </summary>
        private void ResetState()
        {
            m_originalFileName = null;
            m_newFileName = null;
  //          m_newFormatType = null;
            m_handleTrackedChanges = false;
            m_acceptTrackedChanges = false;
            m_statusFileName = null;
        }

        /// <summary>
        /// Reads the command file to figure out specific values for
        /// the conversion such as the new filename, / the format type,
        /// whether to track changes, etc.
        /// </summary>
        /// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
        private void DetermineConversionValues(string p_fileName)
        {
            // Since this happens in a separate process, it should sleep
            // until the file is completely written out (to prevent the
            // file sharing exception).
            bool successful = false;
            for (int i = 0; (!successful && i < 3); i++)
            {
                try
                {
                    Thread.Sleep(1000);
                    _DetermineConversionValues(p_fileName);
                    successful = true;
                }
                catch (Exception e)
                {
                    m_log.Log("Failed to determine conversion values for: " +
                        p_fileName + " - " + e.ToString());
                }
            }
        }

        /// <summary>
        /// Reads the command file to figure out specific values for
        /// the conversion such as the new filename, / the format type,
        /// whether to track changes, etc.
        /// </summary>
        /// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
        private void _DetermineConversionValues(string p_fileName)
        {
            FileInfo fi = new FileInfo(p_fileName);
            StreamReader sr = fi.OpenText();
            char[] delimiters = new char[1];
            delimiters[0] = '=';

            // the ConvertTo and ConvertFrom lines can be xml, or indd; 
            // for example:
            //ConvertFrom=indd
            //ConvertTo=xml
            string convertFrom = sr.ReadLine().Split(delimiters, 2)[1];
            string convertTo = sr.ReadLine().Split(delimiters, 2)[1];
            setConversionType(convertFrom, convertTo, p_fileName);

            //AcceptChanges=false | true | NA
            string acceptChanges = sr.ReadLine().Split(delimiters, 2)[1];
            setTrackedChangesHandling(acceptChanges);

            sr.Close();
        }

        /// <summary>
        /// Sets the conversion type (xml, indd)
        /// </summary>
        /// <param name="p_convertFrom">ConvertFrom value from the command file</param>
        /// <param name="p_convertTo">ConverTo value from the command file</param>
        /// <param name="p_fileName">the command file</param>
        private void setConversionType(string p_convertFrom, string p_convertTo, string p_fileName)
        {
            string baseFileName = p_fileName.Substring(
                0, p_fileName.Length - FILE_EXT_LEN);
            m_originalFileName = baseFileName + p_convertFrom.ToLower();
            m_newFileName = baseFileName + p_convertTo.ToLower();
        }

        /// <summary>
        /// Sets how to handle tracked changes
        /// </summary>
        /// <param name="p_acceptChanges">AcceptChanges value from the command file</param>
        private void setTrackedChangesHandling(string p_acceptChanges)
        {
            if (p_acceptChanges.Equals("true"))
            {
                m_handleTrackedChanges = true;
                m_acceptTrackedChanges = true;
            }
            else if (p_acceptChanges.Equals("false"))
            {
                m_handleTrackedChanges = true;
                m_acceptTrackedChanges = false;
            }
            else if (p_acceptChanges.Equals("NA"))
            {
                m_handleTrackedChanges = false;
                m_acceptTrackedChanges = false;
            }
        }

        /// <summary>
        /// See Converter interface for details.
        /// This either return ".im_command" or ".ex_command" depending on whether
        /// the converter is used for import or export.
        /// </summary>
        /// <returns></returns>
        public string GetFileExtensionToWatch()
        {
            return m_fileExtensionSearchPattern;
        }

        /// <summary>
        /// Deletes the command file
        /// </summary>
        private void DeleteInputFile(string p_fileName)
        {
            try
            {
                FileInfo fi = new FileInfo(p_fileName);
                fi.Delete();
            }
            catch (Exception e)
            {
                Logger.LogError("Problem deleting input file ", e);
            }
        }

        /// <summary>
        /// Quits the InDesign Application.
        /// </summary>
/*        private void QuitInDesign()
        {
            if (m_inDesignApp != null && m_openedFileNumber == 0)
            {
                try
                {
                    m_inDesignApp.Quit(InDesign.idSaveOptions.idYes);
                }
                catch (Exception e)
                {
                    Logger.LogError("Failed to quit InDesign", e);
                }
            }
        }*/
    }
}