using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using Microsoft.Win32;

namespace GlobalSight.InDesignConverter
{
    [RunInstaller(true)]
    public partial class ProjectInstaller : Installer
    {
    //    private bool m_interactWithDesktop = true;

        public ProjectInstaller()
        {
            InitializeComponent();
        }

        //protected override void OnAfterInstall(IDictionary savedState)
        //{
        //    string keyPath = @"SYSTEM\CurrentControlSet\Services\" + 
        //        this.serviceInstaller1.ServiceName;
        //    RegistryKey ckey = Registry.LocalMachine.OpenSubKey(keyPath, true);
        //    if ((null != ckey) && (null != ckey.GetValue("Type")))
        //    {
        //        ckey.SetValue("Type", (int)ckey.GetValue("Type") | 0x100);
        //    }
        //}
    }
}