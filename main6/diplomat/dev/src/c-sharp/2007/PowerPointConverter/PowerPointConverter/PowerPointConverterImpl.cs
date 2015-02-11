using System;
using System.IO;
using Microsoft;
using Office_Core = Microsoft.Office.Core;
using PowerPoint_Class = Microsoft.Office.Interop.PowerPoint;
using GlobalSight.Common;

namespace GlobalSight.PowerPointConverter
{
	/// <summary>
	/// PowerPointConverterImpl is responsible for converting PowerPoint
	/// documents to and from HTML.
	/// </summary>
	public class PowerPointConverterImpl : GlobalSight.Common.Converter
	{
		private Logger m_log = null;
		private PowerPoint_Class.Application m_pptApp = null;
		private PowerPoint_Class.Presentation m_presentation = null;

		private string m_originalFileName = null;
		private string m_newFileName = null;
		private PowerPoint_Class.PpSaveAsFileType m_newFormatType =
			PowerPoint_Class.PpSaveAsFileType.ppSaveAsHTML;
		private string m_fileExtensionSearchPattern = null;
		private ConversionType m_conversionType = ConversionType.IMPORT;
		private string m_statusFileName = null;

		public const string EXPORT_FILE_EXT = "*.ex_command";
		public const string IMPORT_FILE_EXT = "*.im_command";
		//10 chars after . for both import and export
		private const int FILE_EXT_LEN = 10;
		public enum ConversionType { IMPORT, EXPORT };

		//PowerPoint file formats
		public const string HTML = "html";
        public const string PPT = "ppt";
        public const string PPTX = "pptx";

		private const Office_Core.MsoTriState MS_FALSE = Office_Core.MsoTriState.msoFalse;
		private const Office_Core.MsoTriState MS_TRUE = Office_Core.MsoTriState.msoTrue;

		/// <summary>
		/// Creates a PowerPointConverterImpl to be used for import or export.
		/// </summary>
		/// <param name="p_conversionType"> the type of conversion (import or export)</param>
		public PowerPointConverterImpl(ConversionType p_conversionType)
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
		/// <param name="p_filename">The command file (.im_command, .ex_command)</param>
		/// <param name="p_language">the language of the directory the file was written in</param>
		public void Convert(string p_fileName, string p_language)
		{
			try
			{
				ResetState();

				m_statusFileName = p_fileName.Substring(
					0, p_fileName.Length - FILE_EXT_LEN) + "status";
				DetermineConversionValues(p_fileName);

				m_log.Log("Processing file " + m_originalFileName);

				CreatePowerPointAppClass();
				OpenDocument();
				SaveDocument();

				StatusFile.WriteSuccessStatus(m_statusFileName, 
					m_originalFileName + " was converted successfully.");

				m_log.Log("Converted successfully to " + m_newFileName);
			}
			catch (Exception e)
			{
				Logger.LogError("PowerPoint Conversion Failed", e);
				StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
			}
			finally
			{
				DeleteInputFile(p_fileName);
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
		/// Quits the PowerPoint Application.
		/// </summary>
		public void QuitPowerPoint()
		{
			if (m_pptApp == null)
			{
				return;
			}

			// closing the presentation would not close the application, so we
			// close it explicitly if there are no more presentations.  Since
			// only one instance of PowerPoint can be running at a time, we need
			// to make sure that there are no more presentations before quiting the
			// application
			if (m_pptApp.Presentations != null && m_pptApp.Presentations.Count == 0)
			{
				try 
				{
					m_pptApp.Quit();
				}
				catch (Exception e)
				{
					Logger.LogError("Failed to quit PowerPoint", e);
				}
			}

			m_pptApp = null;
		}

		/// <summary>
		/// Saves the document out with the appropriate new filename and format.
		/// Also does any special handling such as accepting/rejecting changes
		/// before saving.
		/// </summary>
		private void SaveDocument()
		{
			m_presentation.SaveAs(m_newFileName, m_newFormatType, MS_FALSE);

			try
			{
				m_presentation.Close();
			}
			catch (Exception e)
			{
				Logger.LogError("Failed to close PowerPoint presentation", e);
			}

			m_presentation = null;
		}

		/// <summary>
		/// Creates the PowerPoint Application Class and sets the default web options
		/// </summary>
		private void CreatePowerPointAppClass()
		{
			if (m_pptApp == null)
			{
				m_pptApp = new PowerPoint_Class.Application();
			}
            
   			m_pptApp.DefaultWebOptions.Encoding = Office_Core.MsoEncoding.msoEncodingUTF8;
			m_pptApp.DefaultWebOptions.AlwaysSaveInDefaultEncoding = MS_TRUE;
		}

		/// <summary>
		/// Opens the original word file and initializes the Word.Document object.
		/// The original file may be HTML, DOC, or RTF
		/// </summary>
		private void OpenDocument()
		{
			// Open the presentation (ppt file) based on the given file name
			// Note that the last parameter determines whether the
			// presentation should be opened within a visible window,
			// which in our case in "false".  Since in PowerPoint no
			// hiding of app is allowed ( i.e."pptApp.Visible = falseFlag;"
			//  won't work), we should not call pptApp.Activate();
            m_presentation = m_pptApp.Presentations.Open(m_originalFileName,
                MS_FALSE, MS_FALSE, MS_FALSE);
		}


		/// <summary>
		/// Sets all internal state back to null
		/// </summary>
		private void ResetState()
		{
			m_presentation = null;
			m_originalFileName = null;
			m_newFileName = null;
			m_statusFileName = null;
		}

		/// <summary>
		/// Reads the command file to figure out specific values for the 
		/// conversion such as the new filename, the format type, whether 
		// to track changes, etc.
		/// </summary>
		/// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
		private void DetermineConversionValues(string p_fileName)
		{
			FileInfo fi = new FileInfo(p_fileName);
			StreamReader sr = fi.OpenText();
			char[] delimiters = new char[1];
			delimiters[0] = '=';

			// The ConvertTo and ConvertFrom lines can be html, rtf, or doc; 
			// for example:
			//ConvertFrom=doc
			//ConvertTo=html
			string convertFrom = sr.ReadLine().Split(delimiters,2)[1];
			string convertTo = sr.ReadLine().Split(delimiters,2)[1];
			setConversionType(convertFrom,convertTo,p_fileName);
			sr.Close();
		}

		/// <summary>
		/// Sets the conversion type (html,doc,rtf)
		/// </summary>
		/// <param name="p_convertFrom">ConvertFrom value from the command file</param>
		/// <param name="p_convertTo">ConverTo value from the command file</param>
		/// <param name="p_fileName">the command file</param>
		private void setConversionType(string p_convertFrom, string p_convertTo,
			string p_fileName)
		{
			string baseFileName = p_fileName.Substring(
				0, p_fileName.Length - FILE_EXT_LEN);
			m_originalFileName =  baseFileName + p_convertFrom.ToLower();

			if (p_convertTo.Equals(HTML))
			{
				m_newFormatType = PowerPoint_Class.PpSaveAsFileType.ppSaveAsHTML;
				m_newFileName = baseFileName + HTML;
			}
            else if (p_convertTo.Equals(PPT))
            {
                m_newFormatType = PowerPoint_Class.PpSaveAsFileType.ppSaveAsPresentation;
                m_newFileName = baseFileName + PPT;
            }
            else if (p_convertTo.Equals(PPTX))
            {
                m_newFormatType = PowerPoint_Class.PpSaveAsFileType.ppSaveAsOpenXMLPresentation;
                m_newFileName = baseFileName + PPTX;
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
				Logger.LogError("Problem deleting input file", e);
			}
		}
    }
}
