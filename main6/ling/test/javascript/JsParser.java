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
// 
// test/javascript/JsParser.java
// 
// Copyright (C) GlobalSight Corporation 2000 (Cornelis van der Laan)
// 
// Responsible     : Cornelis van der Laan
// Author          : Cornelis van der Laan
// EMail           : nils@globalsight.com
// Created On      : Fri Aug 11 03:02:15 2000
// Last Modified By: Cornelis van der Laan
// Last Modified On: Wed Aug 16 20:47:38 2000
package test.javascript;

import com.globalsight.ling.docproc.extractor.javascript.Parser;
import com.globalsight.ling.docproc.extractor.javascript.ParseException;
import com.globalsight.ling.docproc.extractor.javascript.IParseEvents;

import java.util.Vector;

public final class JsParser
    implements IParseEvents
{
    public static void main(String args[])
    {
        com.globalsight.ling.docproc.extractor.javascript.Parser parser;

        if ( args.length == 0 )
        {
            System.err.println("JS Parser: Reading from standard input");
            parser = new Parser(System.in);
        }
        else
            if ( args.length == 1 )
            {
                System.err.println("JS Parser: Reading from file " + args[0]);
                try
                {
                    parser = new Parser(new java.io.FileInputStream(args[0]));
                }
                catch (java.io.FileNotFoundException e)
                {
                    System.err.println("JS Parser: File " + args[0] + 
                      " not found.");
                    return;
                }
            }
            else
            {
                System.err.println("JS: Usage is one of:");
                System.err.println("  java test.javascript.JsParser < file");
                System.err.println("or");
                System.err.println("  java test.javascript.JsParser file");
                return;
            }

        try
        {
            parser.setHandler(new JsParser ());
            parser.parse();
            System.err.println("JS Parser: file parsed successfully: " + 
              args[0]);
        }
        catch (ParseException e)
        {
            System.err.println("JS Parser: Encountered errors during parse:");
            System.err.println(e.getMessage());
        }
    }


    public void handleStart() {}
    public void handleFinish() {}

    public void handleWhite(String s) { System.out.print(s); }
    public void handleEndOfLine(String s) { System.out.print(s); }
    public void handleComment(String s) { System.out.print(s); }
    public void handleCDO(String s) { System.out.print(s); }
    public void handleCDC(String s) { System.out.print(s); }

    public void handleLiteral(String s) { System.out.print(s); }
    public void handleString(String s) { System.out.print(s); }
    public void handleKeyword(String s) { System.out.print(s); }
    public void handleOperator(String s) { System.out.print(s); }

}

