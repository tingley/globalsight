package com.globalsight.util2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class FileUtils
{

	private static final Logger logger = Logger.getLogger(FileUtils.class);

	/**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * <p>
     * Syntax such as <code>file:///my%20docs/file.txt</code> will be
     * correctly decoded to <code>/my docs/file.txt</code>.
     * 
     * @param url
     *            the file URL to convert, null returns null
     * @return the equivalent <code>File</code> object, or <code>null</code>
     *         if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException
     *             if the file is incorrectly encoded
     */
	public static File toFile(URL url)
	{
		if (url == null || !url.getProtocol().equals("file"))
		{
			return null;
		}
		else
		{
			String filename = url.getFile().replace('/', File.separatorChar);
			int pos = 0;
			while ((pos = filename.indexOf('%', pos)) >= 0)
			{
				if (pos + 2 < filename.length())
				{
					String hexStr = filename.substring(pos + 1, pos + 3);
					char ch = (char) Integer.parseInt(hexStr, 16);
					filename = filename.substring(0, pos) + ch
							+ filename.substring(pos + 3);
				}
			}
			return new File(filename);
		}
	}

	public static void write(File file, String data) throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);
		try
		{
			out.write(data.getBytes());
		}
		finally
		{
			closeSilently(out);
		}
	}

	public static void write(File file, String data, String encoding)
			throws IOException
	{
		FileOutputStream out = new FileOutputStream(file);
		try
		{
			out.write(data.getBytes(encoding));
		}
		finally
		{
			closeSilently(out);
		}
	}

	public static String read(String fileName) throws IOException
	{
		// InputStream in =
		// Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		return read(new FileInputStream(fileName));
	}

	public static String read(File file) throws IOException
	{
		return read(new FileInputStream(file));
	}

	public static String read(InputStream in) throws IOException
	{
		return new String(readAsByte(in));
	}

	public static String read(File file, String encoding) throws IOException
	{
		return read(new FileInputStream(file), encoding);
	}

	public static String read(InputStream in, String encoding)
			throws IOException
	{
		return new String(readAsByte(in), encoding);
	}

	private static byte[] readAsByte(InputStream in) throws IOException
	{
		byte[] content = new byte[(int) in.available()];
		try
		{
			in.read(content, 0, content.length);
		}
		finally
		{
			closeSilently(in);
		}
		return content;
	}

	public static void closeSilently(InputStream in)
	{
		try
		{
			if (in != null) in.close();
		}
		catch (Exception e)
		{
			if (logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Cannot close inputstream: " + in);
			}
		}
	}

	public static void closeSilently(OutputStream out)
	{
		try
		{
			if (out != null) out.close();
		}
		catch (Exception e)
		{
			if (logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Cannot close outputstream: " + out);
			}
		}
	}

	public static void closeSilently(Reader reader)
	{
		try
		{
			if (reader != null) reader.close();
		}
		catch (Exception e)
		{
			if (logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Cannot close reader: " + reader);
			}
		}
	}

	public static void closeSilently(Writer writer)
	{
		try
		{
			if (writer != null) writer.close();
		}
		catch (Exception e)
		{
			if (logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Cannot close writer: " + writer);
			}
		}
	}

	public static void deleteSilently(String filename)
	{
		try
		{
			if (filename != null) new File(filename).delete();
		}
		catch (Exception e)
		{
			if (logger.isEnabledFor(Level.WARN))
			{
				logger.warn("Cannot delete file: " + filename, e);
			}
		}
	}

	/**
     * Returns base name of the specified file
     * 
     * <pre>
     * getBaseName(&quot;dir/file.txt&quot;) returns &quot;file.txt&quot;
     * </pre>
     * 
     * @param p_filename
     *            the file name
     * @return
     */
	public static String getBaseName(String p_filename)
	{
		return p_filename.substring(p_filename.lastIndexOf(File.separator) + 1);
	}

	/**
     * Returns the part of the specified filename after removing the suffix.
     * 
     * <pre>
     * getPrefix(&quot;dir/file.txt&quot;) returns &quot;dir/file&quot;
     * getPrefix(&quot;file.txt&quot;) returns &quot;file&quot;
     * </pre>
     * 
     * @param p_filename
     * @return
     */
	public static String getPrefix(String p_filename)
	{
		int index = p_filename.lastIndexOf('.');
		return index >= 0 ? p_filename.substring(0, index) : p_filename;
	}

	/**
     * @param p_filename
     * @return txt of test.txt
     */
	public static String getSuffix(String p_filename)
	{
		return p_filename.substring(p_filename.lastIndexOf('.') + 1);
	}

	public static String relativePath(String p_absolutePath,
			String p_currentPath)
	{
		// assert absolutePath.startsWith(currentPath)
		int index = p_currentPath.length();
		if (!p_currentPath.endsWith(File.separator))
		{
			index++;
		}
		return p_absolutePath.substring(index);
	}

	public static String getFileEncoding(File p_f)
	{
		String utf8 = "UTF-8";
		String utf16be = "UTF-16BE";
		String utf16le = "UTF-16LE"; // microsoft /intel order
		String utf32be = "UTF-32BE";
		String utf32le = "UTF-32LE";

		int[] refBytesUTF32be = { 0x00, 0x00, 0xFE, 0xFF };
		int[] refBytesUTF32le = { 0xFF, 0xFE, 0x00, 0x00 };
		int[] refBytesUTF8 = { 0xEF, 0xBB, 0xBF };
		int[] refBytesUTF16LE = { 0xFF, 0xFE };
		int[] refBytesUTF16BE = { 0xFE, 0xFF };

		FileInputStream in = null;
		try
		{
			in = new FileInputStream(p_f);
			int byte1 = in.read();
			int byte2 = in.read();
			int byte3 = in.read();
			int byte4 = in.read();

			if (byte1 == refBytesUTF32be[0] && byte2 == refBytesUTF32be[1]
					&& byte3 == refBytesUTF32be[2]
					&& byte4 == refBytesUTF32be[3])
			{
				return utf32be;
			}
			else if (byte1 == refBytesUTF32le[0] && byte2 == refBytesUTF32le[1]
					&& byte3 == refBytesUTF32le[2]
					&& byte4 == refBytesUTF32le[3])
			{
				return utf32le;
			}
			else if (byte1 == refBytesUTF8[0] && byte2 == refBytesUTF8[1]
					&& byte3 == refBytesUTF8[2])
			{
				return utf8;
			}
			else if (byte1 == refBytesUTF16BE[0] && byte2 == refBytesUTF16BE[1])
			{
				return utf16be;
			}
			else if (byte1 == refBytesUTF16LE[0] && byte2 == refBytesUTF16LE[1])
			{
				return utf16le;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			closeSilently(in);
		}

		InputStreamReader reader = null;
		try
		{
			reader = new InputStreamReader(new FileInputStream(p_f));
			String encoding = reader.getEncoding();
			if (encoding != null)
			{
				return encoding;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			closeSilently(reader);
		}

		String defaultEncoding = System.getProperty("file.encoding");
		if (defaultEncoding == null) defaultEncoding = "ISO-8859-1";
		return defaultEncoding;
	}

	public static String concatPath(String parent, String child)
	{
		return new File(parent, child).getAbsolutePath();
	}
	
	// Create File, include parent folder.
	public static File createFile(String p_filePath)
	{
	    File file = new File(p_filePath);
	    File parent = new File(file.getParent());
	    if(!parent.exists())
	    {
	        parent.mkdirs();
	    }
	    return file;
	}

	public static void main(String[] args) throws Exception
	{
		File file1 = new File("D:\\Training\\workspace\\TestAny\\test_resources\\xml encoding\\note_encode_1252_u.xml");
		File file2 = new File("D:\\Training\\workspace\\TestAny\\test_resources\\xml encoding\\note_encode_8859_u.xml");
		File file3 = new File("D:\\Training\\workspace\\TestAny\\test_resources\\xml encoding\\note_encode_none_u.xml");
		File file4 = new File("D:\\Training\\workspace\\TestAny\\test_resources\\xml encoding\\note_encode_utf8_u.xml");
		File file5 = new File("D:\\Training\\workspace\\TestAny\\test_resources\\xml encoding\\note_encode_utf16_u.xml");
		
		SAXReader reader = new SAXReader();
//		reader.setEncoding("UTF-8");
		
		Document doc1 = reader.read(file1);
		String xml1 = doc1.asXML();
		Document doc2 = reader.read(file2);
		String xml2 = doc2.asXML();
		Document doc3 = reader.read(file3);
		String xml3 = doc3.asXML();
		Document doc4 = reader.read(file4);
		String xml4 = doc4.asXML();
		Document doc5 = reader.read(file5);
		String xml5 = doc5.asXML();

		
		String f1 = read(file1, "windows-1252");
		String f2 = read(file2, "ISO-8859-1");
		String f3 = read(file3, "UTF-8");
		String f4 = read(file4, "UTF-8");
		String f5 = read(file5, "UTF-16");
		
		String e1 = getFileEncoding(file1);
		String e2 = getFileEncoding(file2);
		String e3 = getFileEncoding(file3);
		String e4 = getFileEncoding(file4);
		String e5 = getFileEncoding(file5);
		
		System.out.println(file1 +" " + e1);
		System.out.println(file2 +" " + e2);
		System.out.println(file3 +" " + e3);
		System.out.println(file4 +" " + e4);
		System.out.println(file5 +" " + e5);
		
	}
}
