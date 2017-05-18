using System;

namespace GlobalSight.Common
{
	/// <summary>
	/// A converter is used to convert a given document (or set of documents)
	/// in one format to another.
	/// </summary>
	public interface Converter
	{
		/// <summary>
		/// Converts the given document named by p_filename to a different format.
		/// For example from PDF to Word.
		/// NOTE: This method should catch and handle all exceptions
		/// </summary>
		/// <param name="p_filename">full path to file</param>
		/// <param name="p_srcLanguage">ISO code for source language</param>
		void Convert(string p_filename, string p_srcLanguage);

		/// <summary>
		/// Returns a file extension search pattern for the kind
		/// of files this converter should be used to convert
		/// like *.pdf
		/// </summary>
		/// <returns>search pattern</returns>
		string GetFileExtensionToWatch();

        string GetTestFileToWatch();
	}
}
