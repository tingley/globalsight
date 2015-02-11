using System;
using System.IO;
using OmniPage12;
using System.Collections;
using GlobalSight.Common;

namespace GlobalSight.PdfConverter
{
	/// <summary>
	/// The Omni class is used to call the OmniPage12 API to do the conversion
	/// of a PDF document to Word format.
	/// </summary>
	public class Omni
	{
		private OmniPage12.ApplicationClass omni = null;
		private OmniPage12.IProcess proc = null;
		private StreamWriter statusFile = null;
		private int m_errorCode = 0;
		private String m_baseFileName;
		private Logger m_log = null;
						
		//create a table of known language code mappings
		private static string langCodeLock = "langCodeLock";
		private static LangCodeTable langCodes = null;

		public Omni()
		{
			m_log = Logger.GetLogger();
			if (langCodes == null)
			{
				lock (langCodeLock)
				{
					if (langCodes == null)
						langCodes = new LangCodeTable();
				}
			}
		}

		/// <summary>
		/// Does the full conversion process of initializing OmniPage,
		/// and all its settings. Then converting the PDF document,
		/// and writing out a status file.
		/// </summary>
		/// <param name="p_pdfFileName">fullpath to PDF file</param>
		public static void convertPdftoWord(String p_pdfFileName, String language)
		{
			Omni converter = new Omni();
			try 
			{
				converter.initOmniPage();
				converter.setOcrSettings(language);
				converter.performOcr(p_pdfFileName);
				converter.writeSuccessStatus();
			}
			catch (Exception e)
			{
				Logger.LogError("Failed to convert PDF to Word",e);
				converter.writeErrorStatus(e);
			}
			finally
			{
				converter.deleteInputFile();
			}

		}

		/// <summary>
		/// Deletes the original PDF file
		/// </summary>
		public void deleteInputFile()
		{
			FileInfo fi = new FileInfo(m_baseFileName + ".pdf");
			fi.Delete();
		}

		/// <summary>
		/// Initializes the OmniPage12 app and logs in
		/// </summary>
		public void initOmniPage()
		{
			
			omni = new OmniPage12.ApplicationClass();
			proc = (IProcess) omni.Login("");
			if (omni.AppInitialized == false)
			{
				m_errorCode = 1;
				throw new Exception("Failed to login to OmniPage12.");
			}
		}

		/// <summary>
		/// Writes out error information to the status file
		/// </summary>
		/// <param name="p_ex">some exception</param>
		public void writeErrorStatus(Exception p_ex)
		{
			StatusFile.WriteErrorStatus(m_baseFileName + ".status",p_ex,m_errorCode);
		}
		
		/// <summary>
		/// Writes out that the given PDF file was converted successfully
		/// </summary>
		public void writeSuccessStatus()
		{
			StatusFile.WriteSuccessStatus(m_baseFileName + ".status",
				m_baseFileName + ".pdf was converted successfully.");
		}

		/// <summary>
		/// Converts the PDF to Word by loading the document into OmniPage12
		/// then recognising it through OCR, then exporting it in Word format.
		/// This performs the actual OCR conversion process. It does not initialize
		/// OmniPage, or write out a status file.
		/// </summary>
		/// <param name="p_pdfFileName">the pdf file name</param>
		public void performOcr(String p_pdfFileName)
		{
			m_baseFileName = p_pdfFileName.Substring(0,p_pdfFileName.Length - 4); //remove ".pdf"

			//read in the pdf
			m_log.Log("Reading in PDF file " + p_pdfFileName);
			m_errorCode = proc.LoadImages(p_pdfFileName);
			if (m_errorCode > 0) throw new Exception("PDF loading failed for " + p_pdfFileName);
                
			//now perform the OCR recognition
			m_log.Log("Performing OCR...");
			m_errorCode = proc.Recognize(OmniPage12.RECMODE.RM_PROCESSALL);
			if (m_errorCode > 0) throw new Exception("OCR Recognition Failed for " + p_pdfFileName);

			//now export the document in Word format
			String docFile = m_baseFileName + ".doc";
			m_log.Log("Exporting document..." + docFile);
			m_errorCode = proc.ExportDocument(docFile);
			if (m_errorCode > 0) throw new Exception("Conversion to Word failed." + p_pdfFileName);
		}

		/// <summary>
		/// Sets a variety of OCR settings include language, export format, proofing, layout, etc.
		/// </summary>
		/// <param name="p_language">the language ISO code</param>
		public void setOcrSettings(String p_language)
		{
			omni.SilentMode = true;
			proc.ImageSource = OmniPage12.IMGSRC.IMGSRC_DISKFILE;
			proc.AutoZoneState = true;
			proc.AutoProof = false;
			proc.ExportFormattingLevel = OmniPage12.FMTLVL.eFmtLvlTruePage;
			proc.LayoutDescription = OmniPage12.LYTDESCR.eLytFmtAuto;
			proc.ExportTarget = OmniPage12.EXPTRG.eExpToDisk;
			proc.TextSeparation = OmniPage12.EXPFILEOPT.eExpOneFile;
			proc.LaunchOnExport = false;

			//m_log.Log("Silent Mode: " + omni.AppInitialized);
			//m_log.Log("Image Source: " + proc.ImageSource);
			//m_log.Log("AutoZoneState: " + proc.AutoZoneState);
			//m_log.Log("AutoProof: " + proc.AutoProof);
			//m_log.Log("Layout: " + proc.LayoutDescription);
			//m_log.Log("export target: " + proc.ExportTarget);
			//m_log.Log("formatting level: " + proc.ExportFormattingLevel);
			//m_log.Log("separation: " + proc.TextSeparation);
			//m_log.Log("launch: " + proc.LaunchOnExport);

			OmniPage12.ExportTextFormats formats = (ExportTextFormats) proc.ExportTextFormats;
			ExportTextFormat etf = (ExportTextFormat) formats.get_Item(11); //Word 2000
			proc.ExportTextFormat = etf;
			//m_log.Log("Export format is: " + etf.Name);
			
			//Set the OCR language
			OmniPage12.OPLanguages langs = (OPLanguages) proc.Languages;
			int langCode = lookupLangCode(p_language);
			for (int k=0; k < langs.Count; k++)
			{
				OmniPage12.OPLanguage lang = (OPLanguage) langs.get_Item(k);

				//set English and the current language to true, all others to false
				if (lang.Identifier == 0 || lang.Identifier == langCode)
				{
					lang.OCRLanguage = true;
					if (lang.Identifier != 0)
						m_log.Log("Using English and " + lang.Name + " as OCR languages");
				}
				else
					lang.OCRLanguage = false;
			}
		}

		/// <summary>
		/// Returns the ScanSoft language code for the given language.
		/// If the language is not in the list, then return 0 (English)
		/// </summary>
		/// <param name="p_language">the language ISO code</param>
		/// <returns>langCode</returns>
		private int lookupLangCode(String p_language)
		{
			if (langCodes.Contains(p_language))
				return (int) langCodes[p_language];
			else
				return 0;
		}
	}
}
