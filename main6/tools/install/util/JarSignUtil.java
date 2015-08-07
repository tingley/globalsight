/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package util;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class JarSignUtil 
{

	public static boolean updateMf(File f) 
	{
		try 
		{
			String path = f.getAbsolutePath();
			String root = path.substring(0, path.lastIndexOf("."));
			ZipIt.unpackZipPackage(path, root);
			
			List<File> fs = FileUtil.getAllFiles(new File(root), new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return false;
					
					if (!pathname.getAbsolutePath().contains("META-INF"))
						return false;
					
					String name = pathname.getName();
					return name.endsWith(".RSA") || name.endsWith(".SF");
				}
			});
			
			for (File f1 : fs)
			{
				FileUtil.deleteFile(f1);
			}
			
			File m = new File(root + "/META-INF/MANIFEST.MF");
			String content = FileUtil.readFile(m);
			int index = content.indexOf("\nName:");
			if (index > 0)
			{
				content = content.substring(0, index + 1);
			}
			content = content.trim() + FileUtil.lineSeparator;
			if (!content.contains("Permissions:"))
			{
				content += "Permissions: all-permissions" + FileUtil.lineSeparator;
			}
			
			if (!content.contains("Caller-Allowable-Codebase:"))
			{
				content += "Caller-Allowable-Codebase: *" + FileUtil.lineSeparator;
			}
			
			if (!content.contains("Trusted-Library:"))
			{
				content += "Trusted-Library: true" + FileUtil.lineSeparator;
			}
			
			File mf = new File(f.getParent() + "/MANIFEST.MF");
			FileUtil.writeFile(mf, content);
			m.delete();
			
			f.delete();
			String[] cmd = new String[] { "jar", "-cfm", f.getAbsolutePath(), mf.getAbsolutePath(),
					"-C", root, "." };
			CmdUtil.run(cmd);
		} 
		catch (Exception e) 
		{
			return false;
		}
		
		return true;
	}
	
	public static void updateJars(File root, String JKS, String keyPass, String keyAlias, JLabel msg, JProgressBar m_progressBar)
	{
		List<File> fs = FileUtil.getAllFiles(root, new FileFilter() 
		{

			@Override
			public boolean accept(File pathname) 
			{
				return pathname.getName().endsWith(".jar");
			}
		});
		
		String sysTemp = System.getProperty("java.io.tmpdir");
		int n = 1;
		File temp = new File(sysTemp + "/" + n);
		while (temp.exists())
		{
			n++;
			temp = new File(sysTemp + "/" + n);
		}
		
		if (m_progressBar != null)
			m_progressBar.setMaximum(fs.size());
		int i = 0;
		for (File f : fs) 
		{
			File trg = new File(sysTemp + "/" + n + "/" + f.getName());
			try 
			{
				i++;
				FileUtil.copyFile(f, trg);
				if (msg != null)
				{
					msg.setText(f.getName());
					m_progressBar.setValue(i);
				}
				else
				{
					System.out.println("Signing applet jar: " + f.getName());
				}
				
				updateJar(trg, JKS, keyPass, keyAlias);
				FileUtil.copyFile(trg, f);
				
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		FileUtil.deleteFile(temp);
	}
	
	public static boolean signJar(File f, String JKS, String keyPass, String keyAlias) 
	{
		String[] cmd = new String[] { "jarsigner", "-tsa", "https://timestamp.geotrust.com/tsa", "-keystore",
				JKS, "-storepass", keyPass,
				f.getAbsolutePath(), keyAlias };
		try
		{
			CmdUtil.run(cmd);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean validate(String JKS, String keyPass, String keyAlias) 
	{
		String[] cmd = new String[] { "keytool", "-list","-keystore",
				JKS, "-storepass", keyPass, "-alias", keyAlias };
		try
		{
			CmdUtil.run(cmd);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean updateJar(File f, String JKS, String keyPass, String keyAlias) 
	{
		if (updateMf(f) && signJar(f, JKS, keyPass, keyAlias))
		{
			return true;
		}
		return false;
	}
}
