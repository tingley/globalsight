using System;
using System.IO;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    class FileUtil
    {
        public static System.Text.Encoding GetEncoding(string fileName)
        {
            using(FileStream fs = new FileStream(fileName, FileMode.Open, FileAccess.Read))
            {
                Encoding r = GetEncoding(fs);
                return r;
            }
        }

        public static System.Text.Encoding GetEncoding(FileStream fs)
        {
            byte[] Unicode = new byte[] { 0xFF, 0xFE, 0x41 };
            byte[] UnicodeBIG = new byte[] { 0xFE, 0xFF, 0x00 };
            byte[] UTF8 = new byte[] { 0xEF, 0xBB, 0xBF };
            Encoding reVal = Encoding.Default;

            BinaryReader r = new BinaryReader(fs, System.Text.Encoding.Default);
            int i;
            int.TryParse(fs.Length.ToString(), out i);
            byte[] ss = r.ReadBytes(i);
            if (ss[0] == 0xFE && ss[1] == 0xFF)
            {
                reVal = Encoding.BigEndianUnicode;
            }
            else if (ss[0] == 0xFF && ss[1] == 0xFE)
            {
                reVal = Encoding.Unicode;
            } 
            else if (ss[0] == 0xEF && ss[1] == 0xBB && ss[2] == 0xBF)
            {
                reVal = Encoding.UTF8;
            }
             
            r.Close();
            return reVal;

        }

        private static bool IsUTF8Bytes(byte[] data)
        {
            int charByteCounter = 1;
            byte curByte;
            for (int i = 0; i < data.Length; i++)
            {
                curByte = data[i];
                if (charByteCounter == 1)
                {
                    if (curByte >= 0x80)
                    {
                        while (((curByte <<= 1) & 0x80) != 0)
                        {
                            charByteCounter++;
                        }

                        if (charByteCounter == 1 || charByteCounter > 6)
                        {
                            return false;
                        }
                    }
                }
                else
                {
                    if ((curByte & 0xC0) != 0x80)
                    {
                        return false;
                    }
                    charByteCounter--;
                }
            }
            if (charByteCounter > 1)
            {
                throw new Exception("unknown byte format");
            }
            return true;
        }

    }
}
