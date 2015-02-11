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
package com.globalsight.ling.util;

import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfDocument;

import java.util.Locale;
import java.io.File;

/*
import com.jacob.com.*;                     // Java to COM Bridge
import com.jacob.activeX.*;                 // code might be helpful later
import java.lang.reflect.*;
*/

class RtfParser
{
    public static void main(String args[])
    {
        setDefaultLocale(args);

        String rtffilename = null;
        boolean b_dump  = false;
        boolean b_trace = false;
        boolean b_debug = true;

        for (int ii = 0; ii < args.length; ii++)
        {
            String s = args[ii];
            if (s.charAt(0) == '-')
            {
                if (s.equals("-h"))
                {
                    usage();
                    System.exit(1);
                }
                else if (s.equals("-dump"))
                {
                    b_dump = true;
                }
                else if (s.equals("-trace"))
                {
                    b_trace = true;
                }
                else if (s.equals("-debug"))
                {
                    b_debug = true;
                }
                else
                {
                    usage();
                    System.exit(1);
                }
            }
            else
            {
                if (rtffilename == null)
                {
                    rtffilename = s;
                }
                else
                {
                    usage();
                    System.exit(1);
                }
            }
        }

        if (rtffilename != null && rtffilename.length() > 0)
        {
            rtffilename = (new File(rtffilename)).getAbsolutePath();
        }
        else
        {
            usage();
            System.exit(1);
        }

        if (rtffilename.toLowerCase().endsWith(".rtf") == false)
        {
            System.err.println("Illegal name of input file : " + rtffilename);
            System.exit(1);
        }

        /*
          else if (rtffilename.toLowerCase().endsWith(".doc") == true)
          {
          if (! Configuration.getProperty("majix.enable.msword").equals("1"))
          {
          System.err.println("Processing of .DOC files is not enabled : " +
            rtffilename);
          System.exit(1);
          }
          }
        */

        System.err.println("Parsing file " + rtffilename);

        long startTime = System.currentTimeMillis();

        RtfDocument document = RtfAPI.parse(rtffilename, b_dump, b_trace);
        if (document == null)
        {
            System.err.println("Parsing failed.");
            System.exit(1);
        }

        System.err.print("Parsing took ");
        System.err.print((System.currentTimeMillis() - startTime)/1000.0);
        System.err.println(" sec.");

        // Simplify RTF structure.  If there's an RtfParagraph with
        // child objects of type RtfText that share the same styles
        // (RtfTextProperties are ==), collapse the RtfTexts.  This is
        // to cleanup Unicode chars inside normal text stretches, or
        // stretches of pure Unicode.

        // CvdL: TODO.

        String text = RtfAPI.getText(document);
        System.out.println("Text of RTF document is " + text.length() +
          " bytes long:");
        System.out.println(text);

        System.exit(0);
    }


    static void usage()
    {
        System.err.println("usage: java com.globalsight.ling.util.RtfParser");
        System.err.println("                [ -debug ]");
        System.err.println("                [ -dump ]");
        System.err.println("                [ -trace ]");
        System.err.println("                rtf_file");
    }


    static public void setDefaultLocale(String arg[])
    {
        String next = null;
        Locale loc = Locale.getDefault();
        String lang = loc.getLanguage();
        String country = loc.getCountry();
        boolean locale_changed = false;

        for (int ii = 0; ii < arg.length; ii++)
        {

            if (next != null)
            {
                if (next.equals("lang"))
                {
                    lang = arg[ii];
                    next = null;
                    locale_changed = true;
                }
                else if (next.equals("country"))
                {
                    country = arg[ii];
                    next = null;
                    locale_changed = true;
                }
            }
            else
            {
                if (arg[ii].equals("-language"))
                {
                    next = "lang";
                }
                else if (arg[ii].equals("-country"))
                {
                    next = "country";
                }
            }
        }


        if (locale_changed)
        {
            Locale.setDefault(new Locale(lang, country));
        }
    }

    /*
      public String convertDoc2Rtf(String inputfile)

      {
      try

      {
      boolean must_delete = false;
      String rtfinputfile = inputfile;

      if (inputfile.toLowerCase().endsWith(".doc"))

      {
      rtfinputfile =
      inputfile.substring(0, inputfile.length() - 4) + ".rtf";

      System.out.println("doc file is " + inputfile);
      System.out.println("rtf file is " + rtfinputfile);

      String op = "";

      try

      {
      System.runFinalizersOnExit(true);

      ActiveXComponent app = new ActiveXComponent("Word.Application");
      app.setProperty("Visible", new Variant(true));

      op = "Documents";
      Object documents = app.getProperty("Documents").toDispatch();

      op = "Open";
      Object document = Dispatch.invoke(
      documents, "Open", Dispatch.Method,
      new Object[]
      {inputfile, new Variant(false), new Variant(true)},
      new int[1]).toDispatch();

      op = "SaveAs";
      Dispatch.invoke(
      document, "SaveAs", Dispatch.Method,
      new Object[]
      {rtfinputfile, new Integer(6)},
      new int[1]);

      op = "Close";
      Dispatch.invoke(
      document, "Close", Dispatch.Method,
      new Object[]
      {new Variant(0)},
      new int[1]);

      op = "Quit";
      Dispatch.invoke(
      app.getObject(), "Quit", Dispatch.Method,
      new Object[]
      {new Variant(-2)},
      new int[1]);
      }
      catch (Exception e)

      {
      System.out.println("Unexpected exception during operation " + op);
      System.out.println("Message is " + e.getMessage());
      e.printStackTrace();
      return false;
      }

      if (rtfinputfile == null)

      {
      System.out.println("Unable to convert to RTF: " + inputfile);
      return false;
      }
      else

      {
      must_delete = true;
      }
      }

      RtfReader reader;
      try

      {
      reader = new RtfReader(rtfinputfile);
      }
      catch (FileNotFoundException e)

      {
      System.out.println("File not found: " + inputfile);
      return false;
      }

      //              FileOutputStream fos;
      //              fos = new FileOutputStream(outputfile);
      //              BufferedOutputStream bos = new BufferedOutputStream (fos);
      //              PrintWriter pw = new PrintWriter(bos);

      PrintWriter pw = new PrintWriter(
      new OutputStreamWriter (System.out, "UTF-8"));

      RtfAnalyser ana = new RtfAnalyser(reader, pw);
      RtfDocument doc = ana.parse();

      if (dump)
      {
      pw.println("***Dump of the DOCRTF intermediate structure");
      doc.Dump(pw);
      pw.println("***Dump ends here");
      }

      reader.close();

      if (must_delete)
      {
      if (! (new File(rtfinputfile).delete()))
      {
      System.out.println("Unable to delete file " + rtfinputfile);
      }
      }

      return true;
      }
      catch (IOException e)

      {
      System.out.println("An exception occurred: " + e.toString());
      return false;
      }
      }
    */

}

