package com.globalsight.util2;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * A util class for String used since DesktopIcon V3.0
 * 
 * @author quincy.zou
 */
public class StringUtil
{
	private static String algorithm = "AES";

	private static SecretKey skey = initKey();

	private static String postfix = "0123456789abcdef";

	public static String encryptString(String p_str)
	{
		if (p_str == null || p_str.equals(""))
		{
			return "";
		}
		try
		{
			Cipher c = Cipher.getInstance(algorithm);
			c.init(Cipher.ENCRYPT_MODE, skey);
			int blockSize = c.getBlockSize();
			int len = p_str.length();
			int mod = postfix.length() - len % blockSize;
			StringBuffer buffer = new StringBuffer(p_str);
			for (int i = 0; i < mod; i++)
			{
				buffer.append(postfix.charAt(mod));
			}
			for (int i = 0; i < blockSize; i++)
			{
				buffer.append(postfix.charAt(mod));
			}
			String newstr = buffer.toString();
			buffer.delete(0, buffer.length());
			for (int i = 0; i < newstr.length(); i = i + blockSize)
			{
				String temp = newstr.substring(i, i + blockSize);
				byte[] result = c.doFinal(temp.getBytes());
				for (int j = 0; j < result.length; j++)
				{
					buffer.append(result[j]).append("/");
				}
			}
			if (buffer.length() > 1) buffer.deleteCharAt(buffer.length() - 1);

			return buffer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static String decryptString(String p_str)
	{
		if (p_str == null || p_str.equals(""))
		{
			return "";
		}
		try
		{
			Cipher c = Cipher.getInstance(algorithm);
			c.init(Cipher.ENCRYPT_MODE, skey);
			int blockSize = c.getBlockSize();
			int resultSize = c.getOutputSize(blockSize);
			c.init(Cipher.DECRYPT_MODE, skey);
			String[] sb = p_str.trim().split("/");
			byte[] src = new byte[sb.length];
			for (int i = 0; i < src.length; i++)
			{
				src[i] = Byte.parseByte(sb[i].trim());
			}
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < src.length; i = i + resultSize)
			{
				byte[] bs = new byte[resultSize];
				System.arraycopy(src, i, bs, 0, resultSize);
				byte[] result = c.doFinal(bs);
				String temp = new String(result);
				buffer.append(temp);
			}

			char ch = buffer.charAt(buffer.length() - 1);
			int mod = postfix.indexOf(ch);
			for (int i = 0; i < mod + blockSize; i++)
			{
				buffer.deleteCharAt(buffer.length() - 1);
			}

			return buffer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	private static SecretKey initKey()
	{
		byte[] raw = new byte[] { 35, 22, -78, -26, 19, 19, -121, -61, 35, 22,
				-78, -26, 19, 19, -121, -61 };
		SecretKey skeySpec = new SecretKeySpec(raw, algorithm);
		return skeySpec;
	}
}
