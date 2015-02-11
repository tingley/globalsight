using System;
using System.IO;
using System.Xml;
using System.Collections;

/// <summary>
/// This program is just an example program of how one could write a c-sharp
/// web service client to use the Ambassador Web Service
/// </summary>
namespace WebSvcTester
{
	/// <summary>
	/// The main Web Service Tester class.
	/// This tests the Ambassador Web Service.
	/// </summary>
	class Tester
	{
		private Ambassador.AmbassadorService m_service =null;
		private string m_accessToken = null;
				
		public Tester()
		{
			m_service = new Ambassador.AmbassadorService();
			m_service.CookieContainer = new System.Net.CookieContainer();
		}

		/// <summary>
		/// Logs in to the web service.
		/// </summary>
		/// <param name="p_username">username</param>
		/// <param name="p_password">password</param>
		public void Login(string p_username, string p_password)
		{
			m_accessToken = m_service.login(p_username,p_password);
		}

		/// <summary>
		/// Calls the HelloWorld() method on the service.
		/// </summary>
		public void Hello()
		{
			Console.WriteLine("Web service says: " + m_service.helloWorld());
		}

		/// <summary>
		/// Gets the file profile info in XML
		/// </summary>
		/// <returns>xml</returns>
		public string GetFileProfiles()
		{
			return m_service.getFileProfileInformation(m_accessToken);
		}

		/// <summary>
		/// Submits a document for l10n with the job prefix "WebSvcTester"
		/// </summary>
		/// <param name="p_filename"></param>
		/// <param name="p_fileProfileId"></param>
		public string SubmitDocument(string p_filename, string p_fileProfileId)
		{
			FileInfo fi = new FileInfo(p_filename);
			FileStream fs = fi.OpenRead();
			System.Byte[] buffer = new Byte[(int)fs.Length];
			//byte[] buffer = new byte[fs.Length];
			fs.Read(buffer,0,(int)fs.Length);
			string basename = p_filename.Substring(p_filename.LastIndexOf(@"\")+1);

			//make the job name unique
			String jobname = "WebSvc" + DateTime.Now.Ticks;
			return m_service.submitDocument(m_accessToken, basename,jobname,buffer,p_fileProfileId);
			}

		/// <summary>
		/// Queries the status of the given job
		/// </summary>
		/// <param name="p_jobname">the name of the job</param>
		/// <returns></returns>
		public string GetStatus(string p_jobname)
		{
			return m_service.getStatus(m_accessToken, p_jobname);
		}

		/// <summary>
		/// Cancels a workflow for a job
		/// </summary>
		/// <param name="p_jobname">the name of the job</param>
		/// <param name="p_workflow">workflow locale</param>
		/// <returns></returns>
		public string CancelWorkflow(string p_jobname, string p_workflow)
		{
			return m_service.cancelWorkflow(m_accessToken, p_jobname,p_workflow);
		}

		/// <summary>
		/// Cancels a job
		/// </summary>
		/// <param name="p_jobname">the name of the job</param>
		/// <returns></returns>
		public string CancelJob(string p_jobname)
		{
			return m_service.cancelJob(m_accessToken, p_jobname);
		}

		/// <summary>
		/// Exports a workflow for a job
		/// </summary>
		/// <param name="p_jobname">the name of the job</param>
		/// <param name="p_workflow">workflow locale</param>
		/// <returns></returns>
		public string ExportWorkflow(string p_jobname, string p_workflow)
		{
			return m_service.exportWorkflow(m_accessToken, p_jobname,p_workflow);
		}

		/// <summary>
		/// Exports a job
		/// </summary>
		/// <param name="p_jobname">the name of the job</param>
		/// <returns></returns>
		public string ExportJob(string p_jobname)
		{
			return m_service.exportJob(m_accessToken, p_jobname);
		}


		/// <summary>
		/// Queries for the locations of the exported localized documents.
		/// </summary>
		/// <param name="p_jobname">the job</param>
		/// <returns></returns>
		public string GetLocalizedDocuments(string p_jobname)
		{
			return m_service.getLocalizedDocuments(m_accessToken, p_jobname);
		}

		public string AddComment(long objectId, int objectType,
			string userId, string commentText, ArrayList files,
			ArrayList accessList)
		{
			return m_service.addComment(m_accessToken,objectId,objectType,
				userId,commentText,files.ToArray(),accessList.ToArray());
		}


		/// <summary>
		/// Pretty prints the XML
		/// </summary>
		/// <param name="p_xml">xml</param>
		private static void PrettyPrint (string p_xml)
		{
			System.Xml.XmlTextWriter s = new XmlTextWriter(Console.Out);
			s.Formatting = System.Xml.Formatting.Indented;
			s.Indentation = 4;
			s.WriteRaw(p_xml);
		}

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main(string[] args)
		{
			try 
			{
				if (args.Length == 0)
				{
					printUsage();
					return;
				}

				Tester t = new Tester();
				if (args.Length < 3)
				{
					printUsage();
					return;
				}
				
				string username = args[0];
				string password = args[1];
				string command = args[2];
				if (command.Equals("hello"))
				{
					t.Hello();
					return;
				}

				t.Login(username,password);

				if (command.Equals("queryFP"))
				{
					//PrettyPrint(t.GetFileProfiles());
					Console.WriteLine(t.GetFileProfiles());
				}
				else if (command.Equals("submitDoc"))
				{
					if (args.Length != 5)
					{
						printUsage();
						return;
					}
					string filename = args[3];
					string fileProfileId = args[4];
					string jobname = t.SubmitDocument(filename,fileProfileId);
					Console.WriteLine("Document submitted. Your job is:\r\n" + jobname);
				}
				else if (command.Equals("getStatus"))
				{
					if (args.Length != 4)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					Console.WriteLine(t.GetStatus(jobname));
				}
				else if (command.Equals("getLocalizedDocs"))
				{
					if (args.Length != 4)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					Console.WriteLine(t.GetLocalizedDocuments(jobname));
				}
				else if (command.Equals("cancelWorkflow"))
				{
					if (args.Length != 5)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					string workflow= args[4];
					Console.WriteLine(t.CancelWorkflow(jobname,workflow));
				}
				else if (command.Equals("exportWorkflow"))
				{
					if (args.Length != 5)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					string workflow= args[4];
					Console.WriteLine(t.ExportWorkflow(jobname,workflow));
				}
				else if (command.Equals("cancelJob"))
				{
					if (args.Length != 4)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					Console.WriteLine(t.CancelJob(jobname));
				}
				else if (command.Equals("exportJob"))
				{
					if (args.Length != 4)
					{
						printUsage();
						return;
					}
					string jobname = args[3];
					Console.WriteLine(t.ExportJob(jobname));
				}
				else if (command.Equals("addComment"))
				{
					addComment(t,args);
				}
				else
					printUsage();
			}
			catch (Exception e)
			{
				Console.WriteLine("ERROR: " + e.Message);
				Console.WriteLine(e.StackTrace);
			}
		}

		static private void addComment(Tester t, string[] p_args)
		{
			if (p_args.Length  < 7)
			{
				printUsage();
				return;
			}
			long objectId = long.Parse(p_args[3]);
			int objectType = int.Parse(p_args[4]); 
			String userId = p_args[5];
			String commentText = p_args[6];
			ArrayList files = new ArrayList();
			ArrayList accessList = new ArrayList();
			// get the files and their access
			int index = 7;
			while (index < p_args.Length )
			{
				String filePath = p_args[index];
				// just verify that the file exists before adding
				// the file name to the argument list
				if (File.Exists(filePath))
				{
					files.Add(filePath);
					index++;
					if (index < p_args.Length)
					{
						String access = p_args[index];
						if ("R".Equals(access))
						{
							accessList.Add("Restricted");
						}
						else
						{
							accessList.Add("General");
						}
					}
					else
					{
						accessList.Add("General");
					}
				}
				else
				{
					Console.WriteLine("File " + filePath + " doesn't exist to attach to comment.");
					return;
				} 
				index++;
			}

			string s = t.AddComment(objectId,objectType,userId,commentText,files,accessList);
			Console.WriteLine(s);
		}

		/// <summary>
		/// Prints out how to use this tester program.
		/// </summary>
		static private void printUsage()
		{
			Console.WriteLine("USAGE: WebSvcTester <username> <password> <command> [args]");
			Console.WriteLine("\tWhere command is one of the following:");
			Console.WriteLine("\thello -- Causes the web service to say hello. Simple connectivity test.");
			Console.WriteLine("\tqueryFP -- queries file profile information.\r\n\t\tReturns XML describing the file profiles.");
			Console.WriteLine("\tsubmitDoc ... <path> <fileProfileId> -- submits the given doc identified\r\n\t\tby full path for l10n with the file profile.");
			Console.WriteLine("\tgetStatus ... <jobname> -- queries job status for the given job.");
			Console.WriteLine("\tgetLocalizedDocs ... <jobname>-- returns XML describing URLs that can\r\n\t\tbe used to download the localized docs.");
			Console.WriteLine("\texportWorkflow ... <jobname> <locale> -- exports all pages of the\r\n\t\tgiven workflow for the job.");
			Console.WriteLine("\texportJob ... <jobname> -- exports all pages of all workflows for the job.");
			Console.WriteLine("\tcancelWorkflow ... <jobname> <locale> -- cancels the given workflow\r\n\t\tfor the job.");
			Console.WriteLine("\tcancelJob ... <jobname> -- cancels the given job.");
			Console.WriteLine("\taddComment ... <objectId> <objectType> <user id> <comment text> [<fileAttachment1> <R|G> <fileAttachment2> <R|G> ...]\r\n\t- adds the comment and any attachments to the task or job specified.\r\n\t- objecType (1=Job, 3=Task)");
			Console.WriteLine("Each command except hello first automatically\r\n\t\tinvokes Login() on the webservice.");
			Console.WriteLine("*NOTE* -- you must use file profile ID, and not name.\r\nAn example ID would be 1001.");
		}
	}
}
