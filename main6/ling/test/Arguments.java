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
package test;

import java.lang.Integer;

/**
 * Utility like Unix getopt.
 *
 * Usage:
 *<PRE>
 *  int c;
 *
 *  getopt.setUsage(new String[] {
 *            "Usage: java frobnicate [-v] [-f file]",
 *            "Frobnicates a file or stdin",
 *            "\t-v: frobnicate verbosely",
 *            "\t-f file: the file to frobnicate",
 *            } );
 *
 *  getopt.parseArgumentTokens(argv, new char[] {'f'});
 *  while ((c = getopt.getArguments()) != -1)
 *  {
 *    switch (c)
 *    {
 *       case 'v':
 *           System.out.println("v");
 *           break;
 *       case 'V':
 *           System.out.println("V");
 *           break;
 *       case 'f':
 *           filename = getopt.getStringParameter();
 *           break;
 *       default:
 *           getopt.printUsage();
 *           System.exit(1);
 *           break;
 *    }
 *  }
 *</PRE>
 *
 * @version $id$
 * @author Jeffrey Rodriguez
 */

public class Arguments
{
    private  boolean      fDbug                        = false;
    private  Queue        queueOfSwitches              = new Queue(20);
    private  Queue        queueStringParameters        = new Queue(20);
    private  Queue        queueOfOtherStringParameters = new Queue(20);
    private  String[]     messageArray                 = null;
    private  int          lastPopArgument              = 0;


    public Arguments() {}

    /**
     * Takes the array of standard Args passed
     * from main method and parses the '-' and
     * the characters after arg.
     *
     * - The value -1 is a special flag that is
     * used to indicate the beginning of the queue
     * of flags and it is also to tell the end of
     * a group of switches.
     *
     * This method will generate 3 internal queues.
     * - A queue that has the switch flag arguments.
     *   e.g.
     *          -dvV
     *   will hold  d, v, V, -1.
     *
     * - A queue holding the string arguments needed by
     *   the switch flag arguments.
     *   If character -p requires a string argument.
     *   The string argument is saved in the string argument
     *   queue.
     *
     * - A queue holding a list of files string parameters
     *   not associated with a switch flag.
     *   -a -v -p myvalue  test.xml test1.xml
     *   this queue will containt test.xml test1.xml
     *
     * @param arguments
     * @param argsWithOptions
     */
    public void parseArgumentTokens(String[] arguments, char[] argsWithOptions)
    {
        int  lengthOfToken   = 0;
        char[] bufferOfToken = null;
        Object[] temp;

        int  argLength = arguments.length;

  outer:
        for (int i = 0; i < argLength; i++)
        {
            bufferOfToken = arguments[i].toCharArray();
            lengthOfToken = bufferOfToken.length;

            if (bufferOfToken[0] == '-')
            {
                int token;

                for (int j = 1; j < lengthOfToken; j++)
                {
                    token = bufferOfToken[j];
                    queueOfSwitches.push((Object) new Integer(token));

                    for (int k = 0; k < argsWithOptions.length; k++)
                    {
                        if (token == argsWithOptions[k])
                        {
                            //queueOfSwitches.push((Object) new Integer(-1));
                            queueStringParameters.push(arguments[++i]);
                            continue outer;
                        }
                    }
                }

                if (i+1 < argLength)
                {
                    if (!(arguments[i+1].charAt(0) == '-'))
                    {
                        //next argument not start '-'; put -1 marker
                        queueOfSwitches.push( (Object ) new Integer( -1 ));
                    }
                }
            }
            else
            {
                queueOfOtherStringParameters.push(arguments[i]);
            }
        }

        if (this.fDbug)
        {
            queueOfSwitches.print();
            queueStringParameters.print();
            queueOfOtherStringParameters.print();
        }
    }


    public int getArguments()
    {
        if (this.fDbug)
        {
            queueOfSwitches.print();
        }

        //int value = ((Integer ) queueOfSwitches.pop()).intValue();
        //if ( this.fDbug ) {
        //  System.err.println("value = " + value );
        //}
        return queueOfSwitches.empty() ? -1 :
          ((Integer)queueOfSwitches.pop()).intValue();
    }

    public String getStringParameter()
    {
        String s = (String) queueStringParameters.pop();
        if (this.fDbug)
        {
            queueStringParameters.print();
        }
        if (this.fDbug)
        {
            System.err.println("string par = " + s);
        }
        return s;
    }


    public String getlistFiles()
    {
        if (this.fDbug)
        {
            queueOfOtherStringParameters.print();
        }

        String s = (String) queueOfOtherStringParameters.pop();
        return s;
    }


    public int stringParameterLeft()
    {
        return queueStringParameters.size();
    }


    public void setUsage(String[] message)
    {
        messageArray = message;
    }

    public void printUsage()
    {
        for (int i = 0; i< messageArray.length; i++)
        {
            System.err.println(messageArray[i]);
        }
    }

    // Private methods

    // Private inner classes

    private static final int  maxIncrement = 10;

    private class Queue
    {
        //private LinkedList queue;
        private Object[]          queue;
        private int               max;
        private int               front;
        private int               rear;
        private int               items;


        public Queue(int size)
        {
            queue  = new Object[size];
            front  = 0;
            rear   = -1;
            items  = 0;
            max    = size;
            //queue = new LinkedList();
        }

        public void push(Object token)
        {
            try
            {
                queue[++rear] = token;
                items++;
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                Object[] holdQueue = new Object[max + maxIncrement];
                System.arraycopy(queue, 0, holdQueue,0,max );
                queue = holdQueue;
                max   += maxIncrement;
                queue[rear] = token;
                items++;
            }

            //queue.addLast( token );
        }

        public Object pop()
        {
            Object token = null;
            if (items != 0)
            {
                token = queue[front++];
                items--;
            }
            return token;
        }

        public boolean empty()
        {
            return (items == 0);
        }

        public int size()
        {
            return items;
        }

        public void clear()
        {
            front  = 0;
            rear   = -1;
            items  = 0;
        }

        public void print()
        {
            for (int i = front; i <= rear; i++)
            {
                System.err.println("token[ " +  i + "] = " + queue[i]);
            }
        }
    }
}
