using System;
using GlobalSight.Common;

namespace GlobalSight.PdfConverter
{
	/// <summary>
	/// The PdfConverter implements the Converter interface.
	/// It uses the Omni class to convert PDFs to Word,
	/// although it could switch based on source language and
	/// invoke other OCR technologies.
	/// </summary>
	public class PdfConverterImpl : Converter
	{
		private const string WATCHED_EXTENSION = "*.pdf";

		public PdfConverterImpl()
		{
		}

		/// <summary>
		/// Implements Converter.Convert(). See GlobalSight.Converter
		/// for more information.
		/// This method delegates to Omni to invoke ScanSoft OmniPage12
		/// to do the PDF->Word conversion.
		/// </summary>
		public void Convert(string p_filename, string p_srcLanguage)
		{
			Omni.convertPdftoWord(p_filename,p_srcLanguage);
		}

		/// <summary>
		/// Impelements Converter.GetFileExtensionToWatch()
		/// </summary>
		/// <returns>"*.pdf"</returns>
		public string GetFileExtensionToWatch()
		{
			return WATCHED_EXTENSION;
		}
	}
}
