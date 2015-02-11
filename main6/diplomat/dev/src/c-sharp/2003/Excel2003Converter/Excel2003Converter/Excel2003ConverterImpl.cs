using System;
using System.IO;
using System.Threading;
using Microsoft;
using Office = Microsoft.Office.Core;
using Excel = Microsoft.Office.Interop.Excel;
using GlobalSight.Common;

namespace Excel2003Converter
{
	/// <summary>
	/// Excel2003ConverterImpl is responsible for converting a MS Excel file to HTML and
	/// vice versa.
	/// </summary>
	public class Excel2003ConverterImpl : GlobalSight.Common.Converter
	{
		private Logger m_log = null;
		private Excel.ApplicationClass m_excelApp = null;
		private Excel._Workbook m_workbook = null;
		
		private string m_originalFileName = null;
		private string m_newFileName = null;
		private object m_newFormatType = null;
		private string m_fileExtensionSearchPattern = null;
		private ConversionType m_conversionType = ConversionType.IMPORT;
		private bool m_handleTrackedChanges = false;
		private bool m_acceptTrackedChanges = false;
		private string m_statusFileName = null;

		public const string EXPORT_FILE_EXT = "*.ex_command";
		public const string IMPORT_FILE_EXT = "*.im_command";
		//10 chars after . for both import and export
		private const int FILE_EXT_LEN = 10; 
		public enum ConversionType { IMPORT, EXPORT };

		//Excel file formats
		public const string HTML = "html";
		public const string XLS = "xls";
		
		/// <summary>
		/// Creates an Excel2003ConverterImpl to be used for import or export.
		/// </summary>
		/// <param name="p_conversionType"> the type of conversion (import or export)</param>
		public Excel2003ConverterImpl(ConversionType p_conversionType)
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

				m_statusFileName = p_fileName.Substring(0, p_fileName.Length - FILE_EXT_LEN) + "status";

				DetermineConversionValues(p_fileName);
				m_log.Log("Processing file " + m_originalFileName);

				CreateExcelAppClass();
				OpenDocument();
				SaveDocument();

				StatusFile.WriteSuccessStatus(m_statusFileName,m_originalFileName + " was converted successfully.");

				m_log.Log("Converted successfully to " + m_newFileName);
			}
			catch (Exception e)
			{
				Logger.LogError("Excel 2003 Conversion Failed", e);
				StatusFile.WriteErrorStatus(m_statusFileName, e, (int)1);
			}
			finally
			{
				DeleteInputFile(p_fileName);
				QuitExcel();
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
		/// Quits the Excel Application.
		/// </summary>
		public void QuitExcel()
		{
			if (m_excelApp != null)
			{
				try
				{
					// may need to re-set this value before quitting since sometimes 
					// Excel may change it itself
					m_excelApp.UserControl = false;
					m_excelApp.Quit();
				}
				catch (Exception e)
				{
					Logger.LogError("Failed to quit Excel",e);
				}
				
				m_excelApp = null;
			}
		}

		/// <summary>
		/// Creates the Excel Application Class and sets the default web options
		/// </summary>
		private void CreateExcelAppClass()
		{
			if (m_excelApp == null)
			{
				//this ensures that there will only be one Excel process
				//running per thread since each thread runs this converter class
				m_excelApp = new Excel.ApplicationClass();
			}

			m_excelApp.Visible = false;
			m_excelApp.DisplayAlerts = false;
			m_excelApp.Interactive = false;
			m_excelApp.DefaultWebOptions.Encoding = Office.MsoEncoding.msoEncodingUTF8;
			m_excelApp.DefaultWebOptions.AlwaysSaveInDefaultEncoding = true;
		}

		/// <summary>
		/// Opens the original word file and initializes the Word.Document object.
		/// The original file may be HTML, DOC, or RTF
		/// </summary>
		private void OpenDocument()
		{
			object missing = System.Reflection.Missing.Value;
			// doesn't update any reference links
			object updateLinks = 0;
			object readOnly = true;
			// not display the read-only recommended message
			object ignoreReadOnlyRecommended = true;
			
			//We could pass in a real password for these two fields, but currently
			//there is no way to pass in a document specific password in System4
			object password = missing;
			//for write-reserved workbook
			object writeResPassword = password;

			m_workbook = m_excelApp.Workbooks.Open(
				m_originalFileName, updateLinks, readOnly, missing, 
				password, writeResPassword, ignoreReadOnlyRecommended,
				missing, missing, missing, missing, missing, missing,
				missing, missing);

			m_workbook.Activate();				
			m_workbook.WebOptions.Encoding = Office.MsoEncoding.msoEncodingUTF8;
		}

		/// <summary>
		/// Saves the document out with the appropriate new filename and format.
		/// Also does any special handling such as accepting/rejecting changes
		/// before saving.
		/// </summary>
		private void SaveDocument()
		{
			HandleTrackedChanges();
			
			object missing = System.Reflection.Missing.Value;
			object newFileName = m_newFileName;
			Excel.XlSaveAsAccessMode accessMode = Excel.XlSaveAsAccessMode.xlNoChange;
			object conflictResolution =  Excel.XlSaveConflictResolution.xlOtherSessionChanges;

			m_workbook.SaveAs(m_newFileName, m_newFormatType, missing, 
				missing, missing, missing, accessMode, 
				conflictResolution, missing, missing, missing, missing);				
			
			object saveChanges = false;
			object routeWb= false;

			m_workbook.Close(saveChanges, missing, routeWb);

			m_workbook = null;			
		}

		
		/// <summary>
		/// This method will either accept or reject tracked changes.
		/// </summary>
		private void HandleTrackedChanges()
		{
			/* this is what the code should do!, but we can't figure out how to get it to work */
			//object missing = System.Reflection.Missing.Value;
			//if (m_handleTrackedChanges && m_workbook.RevisionNumber > 0)
			//{
			//	if (m_acceptTrackedChanges)
			//		m_workbook.AcceptAllChanges(missing, missing, missing);
			//	else
			//		m_workbook.RejectAllChanges	(missing, missing, missing);
			//}
		}

		/// <summary>
		/// Sets all internal state back to null
		/// </summary>
		private void ResetState()
		{
			m_workbook = null;
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
                        p_fileName + " - " + e.ToString());
                }
            }
        }

		/// <summary>
		/// Reads the command file to figure out specific values for the conversion 
		/// such as the new filename, the format type, whether to track changes, etc.
		/// </summary>
		/// <param name="p_fileName">the command file (.im_command) or (.ex_command)</param>
		private void _DetermineConversionValues(string p_fileName)
		{
			FileInfo fi = new FileInfo(p_fileName);
			StreamReader sr = fi.OpenText();
			char[] delimiters = new char[1];
			delimiters[0] = '=';
			//the ConvertTo and ConvertFrom lines can be html, rtf, or doc; for example:
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
		/// Sets the conversion type (html,doc,rtf)
		/// </summary>
		/// <param name="p_convertFrom">ConvertFrom value from the command file</param>
		/// <param name="p_convertTo">ConverTo value from the command file</param>
		/// <param name="p_fileName">the command file</param>
		private void setConversionType(string p_convertFrom, string p_convertTo, 
			string p_fileName)
		{
			string baseFileName = p_fileName.Substring(0, p_fileName.Length - FILE_EXT_LEN);
			m_originalFileName =  baseFileName + p_convertFrom.ToLower();

			if (p_convertTo.Equals(HTML))
			{
				m_newFormatType = Excel.XlFileFormat.xlHtml;
				m_newFileName = baseFileName + HTML;
			}
			else if (p_convertTo.Equals(XLS))
			{
				m_newFormatType = Excel.XlFileFormat.xlExcel9795;
				m_newFileName = baseFileName + XLS;
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
                lock(new Object()){
                    FileInfo fi = new FileInfo(p_fileName);
                    fi.Delete();
                }
			}
			catch (Exception e)
			{
				Logger.LogError("Problem deleting input file",e);
			}
		}
	}
}
