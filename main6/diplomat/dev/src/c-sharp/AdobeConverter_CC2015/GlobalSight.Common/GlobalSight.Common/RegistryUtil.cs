using System;
using Microsoft.Win32;

namespace GlobalSight.Common
{
	/// <summary>
	/// Provides convenience operations for accessing the Windows
	/// Registry.
	/// </summary>
	public class RegistryUtil
	{
		/// <summary>
		/// Gets the value of the named key under HKLM\Software\GlobalSight
		/// </summary>
		/// <param name="p_key">name of the key to get</param>
		/// <returns>string value</returns>
		/// <exception cref="">throws Exception if the key cannot be found or is empty</exception>
		static public string GetGlobalSightRegistryValue(string p_key)
		{
			RegistryKey rkey = Registry.LocalMachine.OpenSubKey(@"Software\GlobalSight");
			string val = (string) rkey.GetValue(p_key);
			if (val == null || val.Length == 0)
				throw new Exception(p_key + @" registry key not found in HKLM\Software\GlobalSight");
			else
				return val;
		}
	}
}
