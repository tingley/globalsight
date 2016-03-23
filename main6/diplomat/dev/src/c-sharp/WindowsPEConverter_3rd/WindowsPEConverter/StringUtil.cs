using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    class StringUtil
    {
        public static StringIndex GetBetween(string content, char start, char end, bool ignoreEscape, int fromIndex, int occurTimes)
        {
            int index = -1;
            int occurs = 0;
            int fromIndex1 = fromIndex;
            while (occurs < occurTimes)
            {
                index = content.IndexOf(start, fromIndex1);

                if (index == -1)
                {
                    return null;
                }

                fromIndex1 = index + 1;

                if (ignoreEscape && index > 0)
                {
                    char lastChar = content[index - 1];
                    if ('\\' == lastChar)
                    {
                        continue;
                    }
                }

                occurs = occurs + 1;
            }


            int index_end = content.IndexOf(end, index + 1);

            int fromIndex2 = index + 1;
            while (true)
            {
                index_end = content.IndexOf(end, fromIndex2);

                if (index_end == -1)
                {
                    return null;
                }

                fromIndex2 = index_end + 1;

                if (ignoreEscape && index_end > 0)
                {
                    char lastChar = content[index_end - 1];
                    if ('\\' == lastChar)
                    {
                        continue;
                    }
                }

                break;
            }

            string ccc = content.Substring(index + 1, index_end - index - 1);
            return new StringIndex(ccc, index + 1, index_end);
        }

        public static StringIndex GetBetween(string content, string start, string end, int fromIndex, int occurTimes)
        {
            int index = content.IndexOf(start, fromIndex);

            if (index == -1)
            {
                return null;
            }
            int occurs = 1;
            while (occurs < occurTimes)
            {
                index = content.IndexOf(start, index + 1);

                if (index == -1)
                {
                    return null;
                }

                occurs = occurs + 1;
            }


            int index_end = content.IndexOf(end, index + start.Length);

            if (index_end == -1)
            {
                return null;
            }

            string ccc = content.Substring(index + start.Length, index_end - index - start.Length);
            return new StringIndex(ccc, index + start.Length, index_end);
        }
    }

    class StringIndex
    {
        public string content { get; set; }
        public int startIndex { get; set; }
        public int endIndex { get; set; }

        public StringIndex(string ccc, int index1, int index2)
        {
            content = ccc;
            startIndex = index1;
            endIndex = index2;
        }
    }
}
