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

using System;
using System.IO;
using System.Threading;
using Microsoft;
using Office_Core = Microsoft.Office.Core;
using Word_Class = Microsoft.Office.Interop.Word;
using GlobalSight.Common;

// NOTE: You must load the Microsoft Office 11/12 Object Library
// and the Microsoft Word 11/12 Object Library (Project->Add Reference,COM)
// Primary Interop Assemblies for Office 2010 must be installed
// from the Office 2010 installation CD through the setup program.
// 

namespace GlobalSight.Office2010Converters
{
	/// <summary>
	/// Implements the Converter interface. Can be used to convert
    /// DOCX->HTML
    /// HTML->DOCX
	/// DOC->HTML
	/// HTML->DOC
	/// RTF->HTML
	/// HTML->RTF
	/// </summary>
	public class WordConverterImpl : GlobalSight.Common.Converter
	{
		private Logger m_log = null;

		// 
		// Constants
		//

		public const string EXPORT_FILE_EXT = "*.ex_command";
		public const string IMPORT_FILE_EXT = "*.im_command";

		//10 chars after . for both import and export
		private const int FILE_EXT_LEN = 10;
		public enum ConversionType { IMPORT, EXPORT };

		//Word file formats
		public const string HTML = "html";
		public const string RTF = "rtf";
        public const string DOC = "doc";
        public const string DOCX = "docx";
        public const string PDF = "pdf";

		//
		// Members
		//

		private Word_Class.Application m_wordApp = null;
		private Word_Class.Document m_wordDoc = null;

		private string m_originalFileName = null;
		private string m_newFileName = null;
		private object m_newFormatType = null;
		private string m_fileExtensionSearchPattern = null;
		private ConversionType m_conversionType = ConversionType.IMPORT;
		private bool m_handleTrackedChanges = false;
		private bool m_acceptTrackedChanges = false;
		private string m_statusFileName = null;

		/// <summary>
		/// Creates a Word 2010 ConverterImpl to be used for import or export.
		/// </summary>
		/// <param name="p_conversionType"> the type of conversion 
		/// (import or export)</param>
		public WordConverterImpl(ConversionType p_conversionType)
		{
            m_log = WordConverterUti.GetLogger();
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

				m_log.Log("Processing file " + m_originalFileName);

				CreateWordAppClass();
				OpenWordDoc();
				SaveDocument();

				StatusFile.WriteSuccessStatus(m_statusFileName,
					m_originalFileName + " was converted successfully.");

				m_log.Log("Converted successfully to " + m_newFileName);
			}
			catch (Exception e)
			{
				Logger.LogError(m_log, "Word 2010 Conversion Failed", e);
				StatusFile.WriteErrorStatus(m_statusFileName,e,(int)1);
			}
			finally
			{
				DeleteInputFile(p_fileName);
				QuitWord();
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
		/// Saves the document out with the appropriate new filename and format.
		/// Also does any special handling such as accepting/rejecting changes
		/// before saving.
		/// </summary>
		private void SaveDocument()
		{
			object missing = System.Reflection.Missing.Value;
			object newFileName = m_newFileName;
			object encoding = Office_Core.MsoEncoding.msoEncodingUTF8;
			object addToRecentFiles = false;
			object ttfonts = false;
			object linebreaks = false;
			object formsdata = false;
			object bidimarks = false; 
            // TODO: find out what this does.

			HandleTrackedChanges();
			HandleSmartTags();

			if (Word_Class.WdSaveFormat.wdFormatDocument == (Word_Class.WdSaveFormat) m_newFormatType)
			{
				m_wordDoc.ActiveWindow.View.Type = Word_Class.WdViewType.wdPrintView;
			}
			m_wordDoc.SaveAs(ref newFileName, ref m_newFormatType, ref missing,
				ref missing, ref addToRecentFiles, ref missing, ref missing, 
				ref ttfonts, ref missing, ref formsdata, ref missing, ref encoding, 
				ref linebreaks, ref missing, ref missing, /*bidi marks!!*/ref bidimarks);
		}

		/// <summary>
		/// Creates the Word Application Class and sets the default web options
		/// (UTF-8, optimize for browser, etc.)
		/// </summary>
		private void CreateWordAppClass()
		{
			m_wordApp = new Word_Class.Application();
			m_wordApp.Visible = false;
			m_wordApp.DisplayAlerts = Word_Class.WdAlertLevel.wdAlertsNone;
			m_wordApp.DefaultWebOptions().Encoding = 
				Office_Core.MsoEncoding.msoEncodingUTF8;
			m_wordApp.DefaultWebOptions().AlwaysSaveInDefaultEncoding = true;
			m_wordApp.DefaultWebOptions().BrowserLevel =
				Word_Class.WdBrowserLevel.wdBrowserLevelMicrosoftInternetExplorer5;
			m_wordApp.DefaultWebOptions().OptimizeForBrowser = false;
		}

		/// <summary>
		/// Opens the original word file and initializes the Word.Document object.
		/// The original file may be HTML, DOC, DOCX, or RTF
		/// </summary>
		private void OpenWordDoc()
		{
			object fileNameAsObj = m_originalFileName;
			object readOnly = false;
			object addToRecentFiles = false;
			object isVisible = true;
			object confirmConversion = false;
			object noEncodingDialog = true;
			object missing = System.Reflection.Missing.Value;
            
            m_wordDoc = m_wordApp.Documents.OpenNoRepairDialog(
                ref fileNameAsObj, ref confirmConversion, ref readOnly,
                ref addToRecentFiles, ref missing, ref missing, ref missing,
                ref missing, ref missing, ref missing, ref missing, ref isVisible,
                ref missing, ref missing, ref noEncodingDialog, ref missing);

			m_wordDoc.Activate();
			m_wordDoc.WebOptions.Encoding = Office_Core.MsoEncoding.msoEncodingUTF8;
			m_wordDoc.WebOptions.BrowserLevel =
				Word_Class.WdBrowserLevel.wdBrowserLevelMicrosoftInternetExplorer5;
			m_wordDoc.WebOptions.OptimizeForBrowser = false;
		}

		/// <summary>
		/// Quits the Word Application.
		/// </summary>
		private void QuitWord()
		{
			if (m_wordApp != null)
			{
				object saveChanges = Word_Class.WdSaveOptions.wdDoNotSaveChanges;
				object origFormat = Word_Class.WdOriginalFormat.wdOriginalDocumentFormat;
				object routeDoc = false;
				try
				{
					m_wordApp.Quit(ref saveChanges, ref origFormat,
						ref routeDoc);
				}
				catch (Exception e)
				{
                    Logger.LogError(m_log, "Failed to quit Word", e);
				}
			}
		}

		/// <summary>
		/// This method will either accept or reject tracked changes.
		/// </summary>
		private void HandleTrackedChanges()
		{
			if (m_handleTrackedChanges && m_wordDoc.Revisions.Count > 0)
			{
				if (m_acceptTrackedChanges)
				{
					m_wordDoc.AcceptAllRevisions();
				}
				else
				{
					m_wordDoc.RejectAllRevisions();
				}
			}
		}

		/// <summary>
		/// This method will remove smart tags from the word doc..
		/// </summary>
		private void HandleSmartTags()
		{
			// GSDEF 12884: remove Office 12 smart tags to clean
			// segments (international versions of Office 2010
			// do not support the same smart tags anyway).

            // For "Word Document Export Problem" issue
            //m_wordDoc.EmbedSmartTags = false;
			m_wordDoc.RemoveSmartTags();
		}

		/// <summary>
		/// Sets all internal state back to null
		/// </summary>
		private void ResetState()
		{
			m_wordApp = null;
			m_wordDoc = null;
			m_originalFileName = null;
			m_newFileName = null;
			m_newFormatType = null;
			m_handleTrackedChanges = false;
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
						p_fileName +" - "+e.ToString());
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

			// the ConvertTo and ConvertFrom lines can be html, rtf, or doc; 
			// for example:
			//ConvertFrom=doc
			//ConvertTo=html
			string convertFrom = sr.ReadLine().Split(delimiters,2)[1];
			string convertTo = sr.ReadLine().Split(delimiters,2)[1];
			setConversionType(convertFrom,convertTo,p_fileName);

			//AcceptChanges=false | true | NA
			string acceptChanges = sr.ReadLine().Split(delimiters,2)[1];
			setTrackedChangesHandling(acceptChanges);

			sr.Close();
		}

		/// <summary>
		/// Sets the conversion type (html,doc,docx,rtf)
		/// </summary>
		/// <param name="p_convertFrom">ConvertFrom value from the command file</param>
		/// <param name="p_convertTo">ConverTo value from the command file</param>
		/// <param name="p_fileName">the command file</param>
		private void setConversionType(string p_convertFrom, string p_convertTo, string p_fileName)
		{
			string baseFileName = p_fileName.Substring(
				0, p_fileName.Length - FILE_EXT_LEN);
 			m_originalFileName =  baseFileName + p_convertFrom.ToLower();

			if (p_convertTo.Equals(HTML))
			{
				m_newFormatType = Word_Class.WdSaveFormat.wdFormatHTML;
				m_newFileName = baseFileName + HTML;
			}
            else if (p_convertTo.Equals(PDF))
            {
                m_newFormatType = Word_Class.WdSaveFormat.wdFormatPDF;
                m_newFileName = baseFileName + PDF;
            }
            else if (p_convertTo.Equals(DOC))
            {
                m_newFormatType = Word_Class.WdSaveFormat.wdFormatDocument;
                m_newFileName = baseFileName + DOC;
            }
            else if (p_convertTo.Equals(DOCX))
            {
                m_newFormatType = Word_Class.WdSaveFormat.wdFormatXMLDocument;
                m_newFileName = baseFileName + DOCX;
            }
			else if (p_convertTo.Equals(RTF))
			{
				m_newFormatType = Word_Class.WdSaveFormat.wdFormatRTF;
				m_newFileName = baseFileName + RTF;
			}
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
                Logger.LogError(m_log, "Problem deleting input file", e);
			}
		}
	}
}
