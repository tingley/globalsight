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
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.ServiceProcess;
using System.Text;
using GlobalSight.Common;
using Microsoft.Win32;

namespace GlobalSight.InDesignConverter
{
    public class Service1 : ServiceBase
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        private Logger m_log = null;
        private String m_watchDirName = null;
        private bool m_alreadyStarted = false;

        // Maintain a seperate ConverterRunner for import and export.
        private ConverterRunner m_importConverterRunner = null;
        private ConverterRunner m_exportConverterRunner = null;

        private InDesignApplication m_InDesignApp = null;

        public Service1()
        {
            InitializeComponent();
        }

        // The main entry point for the process
        static void Main()
        {
            System.ServiceProcess.ServiceBase[] ServicesToRun;

            // More than one user Service may run within the same process. To add
            // another service to this process, change the following line to
            // create a second service object. For example,
            //
            //   ServicesToRun = New System.ServiceProcess.ServiceBase[] 
            //      { new Service1(), new MySecondUserService() };
            //
            ServicesToRun = new System.ServiceProcess.ServiceBase[] { new Service1() };

            System.ServiceProcess.ServiceBase.Run(ServicesToRun);
        }

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            // 
            // Service1
            // 
            this.ServiceName = "GlobalSight Converter - InDesign";

        }

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        /// <summary>
        /// Set things in motion so your service can do its work.
        /// </summary>
        protected override void OnStart(string[] args)
        {
            if (m_alreadyStarted == true)
            {
                return;
            }

            m_alreadyStarted = true;

            try
            {
                m_watchDirName = RegistryUtil.GetGlobalSightRegistryValue(
                	"InDesignConvDir") + @"\indd";
                DirectoryInfo watchDir = new DirectoryInfo(m_watchDirName);
                watchDir.Create();
                Logger.Initialize(m_watchDirName + @"\InDesignConverter.log");

                m_log = Logger.GetLogger();
                m_log.Log("GlobalSight InDesign Converter starting up.");
                m_log.Log("Creating and starting threads to watch directory " +
                    m_watchDirName);

                m_importConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.IMPORT),
                    m_watchDirName);
                m_exportConverterRunner = new ConverterRunner(
                    new InDesignConverterImpl(InDesignConverterImpl.ConversionType.EXPORT),
                    m_watchDirName);

                m_importConverterRunner.Start();
                m_exportConverterRunner.Start();

                if (AllowInteractWithDesktop())
                {
                    m_InDesignApp = InDesignApplication.getInstance();
                }
                else
                {
                    throw new Exception("InDesign Converter Service needs to interact with desktop." 
                         + " The properties of InDesign Converter Service needs to " 
                         + "be changed to allow sevice to interact with desktop.");
                }
            }
            catch (Exception e)
            {
                string msg = "GlobalSight InDesign Converter failed to initialize because of: " +
                    e.Message + "\r\n" + e.StackTrace;
                EventLog.WriteEntry(msg, EventLogEntryType.Error);
                Logger.LogWithoutException(msg);
                throw e;
            }
        }

        private bool AllowInteractWithDesktop()
        {
            string keyPath = @"SYSTEM\CurrentControlSet\Services\" +
                this.ServiceName;
            RegistryKey ckey = Registry.LocalMachine.OpenSubKey(keyPath, true);
            if ((null != ckey) && (null != ckey.GetValue("Type")))
            {
                return ( (int)ckey.GetValue("Type") == 0x110 );
            }

            return false;
        }

        /// <summary>
        /// Stop this service.
        /// </summary>
        protected override void OnStop()
        {
            // TODO: Add code here to perform any tear-down necessary to stop your service.
            m_log.Log("InDesign Converter shutting down.");
            m_importConverterRunner.Stop();
            m_exportConverterRunner.Stop();
            m_InDesignApp.CloseInDesignApp();
            m_InDesignApp = null;
            m_alreadyStarted = false;
        }
    }
}
