using System;
using System.IO;
using System.Text;

namespace GlobalSight.Common
{
	/// <summary>
	/// Very simple logger class to print messages to a common log file.
	/// There is one logger per process.
	/// </summary>
	public class Logger : StreamWriter
	{		
		/// <summary>
		/// Private constructor used to create a Logger object.
		/// The logger will autoflush.
		/// </summary>
		/// <param name="p_path">log file name</param>
		private Logger(String p_path) : base(p_path,true)
		{
			this.AutoFlush = true;
		}

		/// <summary>
		/// Initializes the logger by creating the one static public logger object
		/// and associating it with a log filename. This method should only be called
		/// by the main startup class in a program.
		/// </summary>
		/// <param name="p_path">the filename to use for the log</param>
		public static Logger Initialize(String p_path)
		{
            Logger logger = new Logger(p_path);
            return logger;
		}

		/// <summary>
		/// Allows use of the logger without the chance of an exception being thrown.
		/// </summary>
		/// <param name="p_msg">the message to log out</param>
		public static void LogWithoutException(Logger logger, string p_msg)
		{
			try 
			{
                logger.Log(p_msg);
			}
			catch (Exception) {}
		}

		/// <summary>
		/// Logs out with the given message followed by the Exception's msg and stacktrace.
		/// The format is:
		/// date; ERROR; msg : exceptionMessage\nStackTrace
		/// This method does not throw any exceptions.
		/// </summary>
		/// <param name="p_msg">the message</param>
		/// <param name="p_ex">the exception</param>
		public static void LogError(Logger logger, string p_msg, Exception p_ex)
		{
			StringBuilder sb = new StringBuilder("ERROR; ");
			sb.Append(p_msg).Append(": ").Append(p_ex.Message.ToString());
			sb.Append("\r\n");
			sb.Append(p_ex.StackTrace.ToString());
			LogWithoutException(logger, sb.ToString());
		}
		/// <summary>
		/// Logs out with the given message as an ERROR.
		/// The format is:
		/// date; ERROR; msg
		/// This method does not throw any exceptions.
		/// </summary>
		/// <param name="p_msg">the message</param>
        public static void LogError(Logger logger, string p_msg)
		{
			StringBuilder sb = new StringBuilder("ERROR; ");
			sb.Append(p_msg);
			LogWithoutException(logger, sb.ToString());
		}

		/// <summary>
		/// Logs out the given msg with the date in the format:
		/// date; msg
		/// </summary>
		/// <param name="p_msg">the message to log</param>
		public void Log(string p_msg)
		{
			StringBuilder sb = new StringBuilder();
			sb.Append(DateTime.Now.ToString("G")); //08/17/2000 16:32:32
			sb.Append("; ");
			sb.Append(p_msg);
			this.WriteLine(sb.ToString());
		}

        public void Debug(string p_msg)
        {
            if (AppConfig.LogDebugMSG)
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(DateTime.Now.ToString("G")); //08/17/2000 16:32:32
                sb.Append("; DEBUG ");
                sb.Append(p_msg);
                this.WriteLine(sb.ToString());
            }
        }
	}
}
