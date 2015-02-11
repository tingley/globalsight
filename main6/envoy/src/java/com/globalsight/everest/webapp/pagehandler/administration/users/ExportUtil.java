package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletResponse;

public class ExportUtil
{

    public static void writeToResponse(HttpServletResponse response,
            File file, String fileName)
    {
        response.reset();
        if (file == null)
        {
            return;
        }
        BufferedOutputStream outx = null;
        FileInputStream fileIn = null;
        try
        {
            outx = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + gbToUtf8(fileName));

            if (file.exists())
            {
                fileIn = new FileInputStream(file);
                int len = (int) file.length();
                if (fileIn != null)
                {
                    byte[] html = new byte[len];
                    while ((len = fileIn.read(html)) != -1)
                    {
                        outx.write(html, 0, len);
                    }
                }
            }
            outx.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fileIn != null)
                    fileIn.close();
                if (outx != null)
                    outx.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (file.exists())
            {
                try
                {
                    file.delete();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    file.deleteOnExit();
                }
            }
        }
    }

    public static String gbToUtf8(String src)
    {
        byte[] b = src.getBytes();
        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++)
        {
            c[i] = (char) (b[i] & 0x00FF);
        }
        return new String(c);
    }
}
