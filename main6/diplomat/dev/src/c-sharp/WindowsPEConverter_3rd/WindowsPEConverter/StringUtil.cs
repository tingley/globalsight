using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    class StringUtil
    {
        public static StringIndex GetBetween(string content, char start, char end, int fromIndex, int occurTimes)
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


            int index_end = content.IndexOf(end, index + 1);

            if (index_end == -1)
            {
                return null;
            }

            string ccc = content.Substring(index + 1, index_end - index - 1);
            return new StringIndex(ccc, index + 1, index_end);
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
