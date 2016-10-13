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
// c:/GS/ling/test/Css/CssParser.java
// 
// Copyright (C) GlobalSight Corporation 2000 (Cornelis van der Laan)
// 
// Responsible     : Cornelis van der Laan
// Author          : Cornelis van der Laan
// EMail           : nils@globalsight.com
// Created On      : Fri Aug 04 15:26:28 2000
// Last Modified By: Cornelis van der Laan
// Last Modified On: Thu Aug 31 04:18:55 2000
package test.css;

import com.globalsight.ling.docproc.extractor.css.*;
import java.util.Vector;

public final class CssParser
    implements IParseEvents
{
    public static void main(String args[])
    {
        com.globalsight.ling.docproc.extractor.css.Parser parser;
        if ( args.length == 0 )
        {
            System.out.println("CSS Parser: Reading from standard input");
            parser = new Parser(System.in);
        }
        else
            if ( args.length == 1 )
            {
                System.out.println("CSS Parser: Reading from file " + args[0]);
                try
                {
                    parser = new Parser(new java.io.FileInputStream(args[0]));
                }
                catch (java.io.FileNotFoundException e)
                {
                    System.out.println("CSS Parser: File " + args[0]+ " not found.");
                    return;
                }
            }
            else
            {
                System.out.println("CSS: Usage is one of:");
                System.out.println("         java test.css.CssParser < inputfile");
                System.out.println("OR");
                System.out.println("         java test.css.CssParser inputfile");
                return;
            }

        try
        {
            parser.setHandler(new CssParser ());
            parser.Parse();
            System.out.println("Css Parser: file parsed successfully: ." + 
              args[0]);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            System.out.println("Css Parser: Encountered errors during parse.");
        }
    }


    public void handleStart() {}
    public void handleFinish() {}

    public void handleWhite(String s) {}
    public void handleEndOfLine(String s) {}
    public void handleComment(String s) {}
    public void handleCDO(String s) {}
    public void handleCDC(String s) {}

    public void handleStartCharSet(String s) {}
    public void handleEndCharSet(String s) {}

    public void handleStartFontFace(String s) {}
    public void handleEndFontFace(String s) {}

    public void handleStartImport(String s) {}
    public void handleImport(String s) {}
    public void handleEndImport(String s) {}

    public void handleStartMedia(String s) {}
    public void handleMedia(String s) {}
    public void handleEndMedia(String s) {}

    public void handleStartAtRule(String s) {}

    public void handleStartBlock(String s) {}
    public void handleEndBlock(String s) {}

    public void handleStartDeclarations(String s) {}
    public void handleEndDeclarations(String s) {}

    public void handleToken(String s) {}
    public void handleDelimiter(String s) {}
    public void handleFunction(String s) {}

    public void handleStyle(String s) {}
    public void handleStartValues(String s) {}
    public void handleEndValues() {}
}

