using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.Text;

namespace IllustratorConvert
{
    [RunInstaller(true)]
    public partial class ProjectInstaller : Installer
    {
        private System.ServiceProcess.ServiceProcessInstaller serviceProcessInstaller1;
        private System.ServiceProcess.ServiceInstaller serviceInstaller1;
        private System.ComponentModel.Container components = null;
        //    private bool m_interactWithDesktop = true;

        public ProjectInstaller()
        {
            InitializeComponent();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            this.serviceProcessInstaller1 = new System.ServiceProcess.ServiceProcessInstaller();
            this.serviceInstaller1 = new System.ServiceProcess.ServiceInstaller();
            // 
            // serviceProcessInstaller1
            // 
            this.serviceProcessInstaller1.Account = System.ServiceProcess.ServiceAccount.LocalSystem;
            this.serviceProcessInstaller1.Password = null;
            this.serviceProcessInstaller1.Username = null;
            // 
            // serviceInstaller1
            // 
            this.serviceInstaller1.ServiceName = "GlobalSight Converter - Illustrator";
            this.serviceInstaller1.ServicesDependedOn = new string[] {
             "Event Log",
             "Remote Procedure Call (RPC)",
             "System Event Notification",
             "Workstation",
             "Server"};
            // 
            // ProjectInstaller
            // 
            this.Installers.AddRange(new System.Configuration.Install.Installer[] {
            this.serviceProcessInstaller1,
            this.serviceInstaller1});

        }
    }
}
