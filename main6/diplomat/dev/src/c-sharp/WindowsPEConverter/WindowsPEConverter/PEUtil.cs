using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Globalization;
using System.Runtime.InteropServices;
using System.ComponentModel;

namespace GlobalSight.WinPEConverter
{
    public class PEUtil
    {
        // dos, Offset to start of PE header
        public static int h_3c = 60;
        // NumberOfSections offset
        public static int h_06 = 6;

        private static Dictionary<ushort, Int32> resourceIdDir;


        public static byte[] ConvertHexStringToByteArray(string hexString)
        {
            if (hexString.Length % 2 != 0)
            {
                throw new ArgumentException(String.Format(CultureInfo.InvariantCulture, "The binary key cannot have an odd number of digits: {0}", hexString));
            }

            byte[] HexAsBytes = new byte[hexString.Length / 2];
            for (int index = 0; index < HexAsBytes.Length; index++)
            {
                string byteValue = hexString.Substring(index * 2, 2);
                HexAsBytes[index] = byte.Parse(byteValue, NumberStyles.HexNumber, CultureInfo.InvariantCulture);
            }

            return HexAsBytes;
        }

        public static string ConvertByteArrayToHexString(byte[] data)
        {
            if (data == null && data.Length == 0)
            {
                throw new ArgumentException("The binary data cannot be empty or null");
            }

            return BitConverter.ToString(data).Replace("-", "");
        }

        public static string ConvertByteArrayToHexString(byte[] data, int startIndex, int length)
        {
            if (data == null && data.Length == 0)
            {
                throw new ArgumentException("The binary data cannot be empty or null");
            }

            return BitConverter.ToString(data, startIndex, length).Replace("-", "");
        }

        //3ch	DWORD   e_lfanew  , Offset to start of PE header
        public static string GetPEHeaderAddress(string allHex)
        {
            string temp = allHex.Substring(h_3c * 2, 8);
            return GetDwordValue(temp, true);
        }

        // 0800 to 0080 and then 80
        private static string GetDwordValue(string oriHex, bool isDword)
        {
            int len = isDword ? 4 : 2;
            string tmp1 = oriHex.Substring(0, len);
            string tmp2 = oriHex.Substring(len, len);

            if (isDword)
            {
                tmp1 = tmp1.Substring(2, 2) + tmp1.Substring(0, 2);
                tmp2 = tmp2.Substring(2, 2) + tmp2.Substring(0, 2);
            }

            return (tmp2 + tmp1).TrimStart(new char[] { '0' });
        }

        public static int GetIndexOfArray(byte[] all, byte[] sub)
        {
            byte sub0 = sub[0];
            int result = -1;

            for (int i = 0; i < all.Length; i++)
            {
                byte b = all[i];

                if (i + sub.Length > all.Length)
                {
                    break;
                }

                if (b == sub0)
                {
                    bool isAllSame = true;

                    for (int j = 0; j < sub.Length; j++)
                    {
                        if (all[i + j] != sub[j])
                        {
                            isAllSame = false;
                            break;
                        }
                    }

                    if (isAllSame)
                    {
                        result = i;
                        return result;
                    }
                }
            }

            return result;
        }

        // convert hex to int
        public static int ConvertHexToInt(string hex)
        {
            return Convert.ToInt32(hex, 16);
        }

        // convert hex to int
        public static byte ConvertHexToByte(string hex)
        {
            return Convert.ToByte(hex, 16);
        }

        public static string ConvertIntToBin(int intt)
        {
            return Convert.ToString(intt, 2);
        }

        public static string GetSectionName(PEReader.IMAGE_SECTION_HEADER secHeader)
        {
            byte[] data = new byte[8];
            data[0] = secHeader.Name_0;
            data[1] = secHeader.Name_1;
            data[2] = secHeader.Name_2;
            data[3] = secHeader.Name_3;
            data[4] = secHeader.Name_4;
            data[5] = secHeader.Name_5;
            data[6] = secHeader.Name_6;
            data[7] = secHeader.Name_7;
            data[0] = secHeader.Name_0;
            data[0] = secHeader.Name_0;

            string name = Encoding.ASCII.GetString(data);

            return name;
        }

        public static int GetPEResourceIndex(ushort type)
        {
            if (resourceIdDir == null)
            {
                resourceIdDir = new Dictionary<ushort, int>();
            }

            if (resourceIdDir.ContainsKey(type))
            {
                int ori = resourceIdDir[type];
                int newId = ori + 1;
                resourceIdDir[type] = newId;
                return newId;
            }
            else
            {
                int newId = 1;
                resourceIdDir[type] = newId;
                return newId;
            }
        }

        public static void ResetPEResourceIndex()
        {
            resourceIdDir = null;
        }

        public static void UpdatePESetting(byte[] pefile, PEReader pereader, PESetting setting)
        {
        }

        public static void UpdatePEResource(byte[] pefile, PEReader pereader,
            PEResourceData data, List<TranslateUnit> newContent)
        {
        }

        public static string UpdateVersionInfo(string oriVersionInfor, List<TranslateUnit> units)
        {
            string result = oriVersionInfor;
            string category = "VersionInformation";

            result = UpdateVersionValue(oriVersionInfor, units, category, "CompanyName");
            result = UpdateVersionValue(oriVersionInfor, units, category, "FileDescription");
            result = UpdateVersionValue(oriVersionInfor, units, category, "InternalName");
            result = UpdateVersionValue(oriVersionInfor, units, category, "LegalCopyright");
            result = UpdateVersionValue(oriVersionInfor, units, category, "OriginalFilename");
            result = UpdateVersionValue(oriVersionInfor, units, category, "ProductName");

            return result;
        }

        private static string UpdateVersionValue(string oriVersionInfor, List<TranslateUnit> units, string category, string id)
        {
            TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, category, id);
            return oriVersionInfor;
        }

        unsafe public static void UpdateVersionInfoWithWinAPI(string peFile, byte[] buffer, List<TranslateUnit> units)
        {
            short* subBlock = null;
            uint len = 0;
            // Get the locale info from the version info:
            if (!WinAPI.VerQueryValue(buffer, @"\VarFileInfo\Translation", out subBlock, out len))
            {
                throw new Exception("Failed to query version information \\VarFileInfo\\Translation.");
            }

            string languageCode = subBlock[0].ToString("X4") + subBlock[1].ToString("X4");
            ushort wLanguage = (ushort)subBlock[0];

            string lpType = "RT_VERSION";
            string category = "VersionInformation";

            UpdateResource(peFile, units, wLanguage, lpType, category, "CompanyName");
            UpdateResource(peFile, units, wLanguage, lpType, category, "FileDescription");
            UpdateResource(peFile, units, wLanguage, lpType, category, "InternalName");
            UpdateResource(peFile, units, wLanguage, lpType, category, "LegalCopyright");
            UpdateResource(peFile, units, wLanguage, lpType, category, "OriginalFilename");
            UpdateResource(peFile, units, wLanguage, lpType, category, "ProductName");
        }

        /*
         * 
         * */

        unsafe public static void UpdateResource(string peFile, List<TranslateUnit> units,
            ushort wLanguage, string lpType, string category, string id)
        {
            IntPtr newV = IntPtr.Zero;
            try
            {
                TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(units, category, id);

                IntPtr hResource = WinAPI.BeginUpdateResource(peFile, true);
                if (hResource.ToInt32() == 0)
                {
                    throw new Win32Exception(Marshal.GetLastWin32Error());
                }

                newV = Marshal.StringToHGlobalUni(tu.SourceContent);
                uint cbData = (uint)Encoding.Unicode.GetBytes(tu.SourceContent).Length;

                if (WinAPI.UpdateResource(hResource, lpType, id, wLanguage, newV, cbData) == false)
                {
                    throw new Win32Exception(Marshal.GetLastWin32Error());
                }

                if (WinAPI.EndUpdateResource(hResource, false) == false)
                {
                    throw new Win32Exception(Marshal.GetLastWin32Error());
                }
            }
            finally
            {
                if (newV != IntPtr.Zero)
                {
                    Marshal.FreeHGlobal(newV);
                }
            }
        }

        public static void UpdateBinary(byte[] binary, uint resOffset, uint resSize, byte[] nnn)
        {
            int nnnsize = nnn.Length;

            int i = 0;
            for (; i < nnnsize; i++)
            {
                if (i >= resSize)
                {
                    break;
                }

                binary[resOffset + i] = nnn[i];
            }
        }
    }
}
