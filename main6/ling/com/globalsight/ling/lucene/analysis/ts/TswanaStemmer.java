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
// http://issues.apache.org/bugzilla/show_bug.cgi?id=32580
package com.globalsight.ling.lucene.analysis.ts;

/**
 * A stemmer for Tswana words. The algorithm is based on the report.
 */
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.util.*;

public class TswanaStemmer
{
    /**
     * Buffer for the terms while stemming them.
     */
    private StringBuffer sb = new StringBuffer();
    String spec_fileName = "spec_fileName.properties";
    String fileName = "compoundwords.properties";

    /**
     * Amount of characters that are removed with
     * <tt>substitute()</tt> while stemming.
     */
    private int substCount = 0;

    /**
     * Stemms the given term to an unique <tt>discriminator</tt>.
     *
     * @param term  The term that should be stemmed.
     * @return      Discriminator for <tt>term</tt>
     */
    protected String stem(String term)
    {
        // Use lowercase for medium stemming.
        term = term.toLowerCase();

        if (!isStemmable(term))
        {
            return term;
        }

        // Reset the StringBuffer.
        sb.delete(0, sb.length());
        sb.insert(0, term);

        // Stemming starts here...
        substitute(sb);
        strip(sb);
        optimize(sb);
        resubstitute(sb);
        removeParticleDenotion(sb);

        return sb.toString();
    }

    /**
     * Checks if a term could be stemmed.
     *
     * @return true if, and only if, the given term consists of letters.
     */
    private boolean isStemmable(String term)
    {
        for (int c = 0; c < term.length(); c++)
        {
            if (!Character.isLetter(term.charAt(c)))
            {
                return false;
            }
        }

        return true;
    }

    protected String[] multipleStems(String term)
    {
        String results[] = null;

        try
        {
            InputStream popStream =
                TswanaStemmer.class.getResourceAsStream(fileName);

            Properties properties = new Properties();
            properties.load(popStream);
            String holdPropertiesResults = properties.getProperty(term);

            if (holdPropertiesResults != null)
            {
                results = new String[2];

                StringTokenizer stringTokenizer =
                    new StringTokenizer(holdPropertiesResults);

                int count = 0;
                results[0] = stringTokenizer.nextToken();
                results[1] = stringTokenizer.nextToken();
            }

        }
        catch (Throwable t)
        {
            System.out.println("problem: " + t);
        }

        return results;
    }

    /**
     * Suffix stripping (stemming) on the current term.
     */
    private void remove_prefix(StringBuffer inbuffer)
    {
        inbuffer.delete(0, 2);
    }


    public boolean check_diminutive(StringBuffer my_buffer)
    {
        int finish = my_buffer.length() - 3;

        int start = finish-4;
        int increment =0;
        boolean check_flag = false;
        String holdString = "";
        String holdDimResults = "notfound";
        Properties properties = null;

        try
        {
            InputStream myStream =
                TswanaStemmer.class.getResourceAsStream("diminutive_file.properties");
            properties = new Properties();
            properties.load(myStream);
        }
        catch(Throwable t)
        {
            System.out.println("cannot load diminutive file: " + t.getMessage());
        }

        for (int a = 0;a < 3; a++)
        {
            holdString = my_buffer.substring(start,finish);

            holdDimResults = properties.getProperty(holdString);

            if (holdDimResults != null)
            {
                if (holdDimResults.equals("found"))
                {
                    if (holdString.equals("ngw"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"m");

                        check_flag = true;
                        break;
                    }
                    else if (holdString.equals("tsh"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"r");

                        check_flag = true;
                        break;
                    }
                    else if (holdString.equals("tshw"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"f");

                        check_flag = true;
                        break;
                    }
                    else if (holdString.equals("jw"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"b");

                        check_flag = true;
                        break;
                    }
                    else if (holdString.equals("ngw"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"m");

                        check_flag= true;
                        break;
                    }
                    else if (holdString.equals("gw"))
                    {
                        my_buffer.delete(start,my_buffer.length());
                        my_buffer.insert(my_buffer.length(),"g");

                        check_flag = true;
                        break;
                    }
                    else if (holdString.equals("ny"))
                    {
                        my_buffer.delete(my_buffer.length()-4,my_buffer.length());

                        check_flag = true;
                        break;
                    }
                }
            }

            start += 1;
        }

        return check_flag;
    }


    private void strip(StringBuffer buffer)
    {
        int mycount = 1;
        boolean myflag;
        boolean check_if_dem = false;
        StringBuffer hold;
        boolean extendedVerb = false;
        Properties properties;
        int[] intarray;
        properties = null;
        String holdclass= " ";
        InputStream popStream = null;

        try
        {
            popStream = TswanaStemmer.class.getResourceAsStream(spec_fileName);
            properties = new Properties();
            properties.load(popStream);
        }
        catch(Throwable t)
        {
            System.out.println("problem loading " + spec_fileName + ": " + t);
        }

        boolean doMore = true;

        while (doMore && buffer.length()>3)
        {
            if (buffer.substring(0,2).equals("mo"))
            {
                if (buffer.length() >= 10)
                {
                    check_if_dem = check_diminutive(buffer);
                }

                if (check_if_dem ==false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }

                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("ba")&& buffer.length() > 4)
            {
                if (buffer.length() >= 10)
                {
                    check_if_dem = check_diminutive(buffer);
                }
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("mm"))
            {
                if (buffer.length() >= 10)
                {
                    check_if_dem = check_diminutive(buffer);
                }
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }

                buffer.delete(0,2);
                buffer.insert(0,"b");

                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("ng"))
            {
                if (buffer.toString().charAt(2) == 'w')
                {
                    if (buffer.length() >= 10)
                    {
                        check_if_dem = check_diminutive(buffer);
                    }
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,3);
                    buffer.insert(0,"b");
                    holdclass = "classification";
                }
            }
            else if (buffer.substring(0,2).equals("mo"))
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("me"))
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }

                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("mh"))
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                buffer.insert(0,"f");
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("le"))
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("lo"))
            {
                //String hold = properties.getProperty(buffer.toString());

                if (buffer.toString().charAt(2) == 's')
                {

                    if (buffer.length() >= 10)
                        check_if_dem = check_diminutive(buffer);
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,3);
                    buffer.insert(0,"ntsh");
                    holdclass = "classification";
                }
                else if (buffer.toString().charAt(2) == 'r'&&
                    buffer.toString().charAt(3)=='a')
                {
                    if (buffer.length() >= 10)
                        check_if_dem = check_diminutive(buffer);
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,3);
                    buffer.insert(0,"th");
                    holdclass = "classification";

                }
                else if (buffer.toString().charAt(2) == 'r' &&
                    buffer.toString().charAt(3)=='e')
                {
                    if (buffer.length() >= 10)
                        check_if_dem = check_diminutive(buffer);
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,3);
                    buffer.insert(0,"nth");
                    holdclass = "classification";
                }
                else if (buffer.toString().charAt(2) == 'g')
                {
                    if (buffer.length() >= 10)
                        check_if_dem = check_diminutive(buffer);
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,3);
                    buffer.insert(0,"kg");
                    holdclass = "classification";
                }
                else
                {
                    if (buffer.length() >= 10)
                        check_if_dem = check_diminutive(buffer);
                    if (check_if_dem == false)
                    {
                        buffer.deleteCharAt(buffer.length()-1);
                    }
                    buffer.delete(0,2);
                    holdclass = "classification";
                }
            }
            else if (buffer.substring(0,2).equals("di"))
                //    {
                //   String holdDiClass = properties.getProperty(buffer.toString());
                //   if (properties.getProperty(buffer.toString()) == null)
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            //      else
            //      {
            //        buffer.delete(0,buffer.length());
            //            buffer.insert(0,holdDiClass);
            //      }
            //    }
            else if (buffer.substring(0,2).equals("bo"))
            {
                if (buffer.length() >= 9)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("ma"))
            {
                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (buffer.substring(0,2).equals("se"))
            {

                if (buffer.length() >= 10)
                    check_if_dem = check_diminutive(buffer);
                if (check_if_dem == false)
                {
                    buffer.deleteCharAt(buffer.length()-1);
                }
                buffer.delete(0,2);
                holdclass = "classification";
            }
            else if (holdclass.equals(" "))
            {
                intarray = new int[5];

                for (int i=0;i<intarray.length;i++)
                    intarray[i] = 0;

                try
                {
                    String fileName = "try.properties";
                    popStream = TswanaStemmer.class.getResourceAsStream(fileName);
                    properties = new Properties();
                    properties.load(popStream);
                }
                catch (Throwable e)
                {
                    System.out.println(e.getMessage());
                }
                //hold = new StringBuffer("fetelela");

                boolean flag = true;
                int count = 3;
                int plus = 0;
                while (flag)
                {
                    if (count > buffer.length())
                        break;

                    String results = properties.getProperty(
                        buffer.substring(buffer.length()-count,buffer.length()));

                    if (results == null)
                        results = "notfound";
                    if (results.equals("found"))
                        intarray[plus] = 1;
                    else
                        intarray[plus] = 0;
                    plus += 1;
                    count += 1;
                    if (count > 7)
                        break;
                }

                int loop = 7;
                for (int b=intarray.length-1;b>=0;b--)
                {
                    if (intarray[b] == 1)
                    {
                        buffer.delete(buffer.length()-loop,buffer.length());
                        if (buffer.charAt(buffer.length()-1) != 'a')
                        {
                            buffer.insert(buffer.length(),'a');
                            extendedVerb = true;
                        }
                        break;
                    }
                    else
                    {
                        mycount += 1;

                        if (mycount > 5)
                        {
                            mycount = 0;
                            break;
                        }
                    }

                    loop -=1;
                    doMore = false;
                }
                //if (!extendedVerb)
                // buffer.deleteCharAt(buffer.length()-1);
            }
            else
            {
                //buffer.deleteCharAt(buffer.length()-1);
                doMore = false;
            }
        }
    }


    /**
     * Does some optimizations on the term. These optimisations are
     * contextual.
     */
    private void optimize(StringBuffer buffer)
    {
        // Additional step for female plurals of professions and inhabitants.
        if (buffer.length() > 5 && buffer.substring(buffer.length() - 5,
            buffer.length()).equals("erin*"))
        {
            buffer.deleteCharAt(buffer.length() -1);
            strip(buffer);
        }
        // Additional step for irregular plural nouns like "Matrizen -> Matrix".
        if (buffer.charAt(buffer.length() - 1) == ('z'))
        {
            buffer.setCharAt(buffer.length() - 1, 'x');
        }
    }

    /**
     * Removes a particle denotion ("ge") from a term.
     */
    private void removeParticleDenotion(StringBuffer buffer)
    {
        if (buffer.length() > 4)
        {
            for (int c = 0; c < buffer.length() - 3; c++)
            {
                if (buffer.substring(c, c + 4).equals("gege"))
                {
                    buffer.delete(c, c + 2);
                    return;
                }
            }
        }
    }

    /**
     * Do some substitutions for the term to reduce overstemming:
     */
    private void substitute(StringBuffer buffer)
    {
    }

    /**
     * Undoes the changes made by substitute().
     */
    private void resubstitute(StringBuffer buffer)
    {
    }
}
