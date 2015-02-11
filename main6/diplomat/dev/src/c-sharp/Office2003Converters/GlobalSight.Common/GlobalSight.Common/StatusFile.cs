using System;
using System.IO;

namespace GlobalSight.Common
{
	/// <summary>
	/// Utility class to create status files
	/// </summary>
	public class StatusFile
	{
		/// <summary>
		/// Writes out error information to the status file
		/// </summary>
		/// <param name="p_statusFileName">status file name</param>
		/// <param name="e">some exception</param>
		/// <param name="p_errorCode">error code</param>
		static public void WriteErrorStatus(string p_statusFileName, Exception p_exception, int p_errorCode)
		{
			StreamWriter statusFile = new StreamWriter(p_statusFileName,false,System.Text.Encoding.Default);
			statusFile.WriteLine("Error:" + p_errorCode);
			statusFile.WriteLine("Msg: " + p_exception.Message);
			statusFile.Close();
		}

		/// <summary>
		/// Writes out a success status file with the given msg.
		/// </summary>
		/// <param name="p_statusFileName">status file name</param>
		/// <param name="p_msg">success message</param>
		static public void WriteSuccessStatus(string p_statusFileName, string p_msg)
		{
			StreamWriter statusFile = new StreamWriter(p_statusFileName,false,System.Text.Encoding.Default);
			statusFile.WriteLine("Error:0");
			statusFile.WriteLine("Msg: " + p_msg);
			statusFile.Close();
		}
	}
}
