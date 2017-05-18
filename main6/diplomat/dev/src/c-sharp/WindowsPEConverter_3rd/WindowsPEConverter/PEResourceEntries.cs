using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace GlobalSight.WinPEConverter
{
    public class PEResourceEntries
    {
        public PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY level1Entry;
        public PEReader.IMAGE_RESOURCE_DIRECTORY_ENTRY[] level2Entries;
        public Hashtable level2Map3Entries;
        public Hashtable level3DATA;
    }

    public class PEResoourceType
    {
        public const ushort RT_CURSOR = 1;
        public const ushort RT_BITMAP = 2;
        public const ushort RT_ICON = 3;
        public const ushort RT_MENU = 4;
        public const ushort RT_DIALOG = 5;
        public const ushort RT_STRING = 6;
        public const ushort RT_FONTDIR = 7;
        public const ushort RT_FONT = 8;
        public const ushort RT_ACCELERATOR = 9;
        public const ushort RT_RCDATA = 10;
        public const ushort RT_MESSAGETABLE = 11;
        public const ushort RT_GROUP_CURSOR = 12;
        public const ushort RT_GROUP_ICON = 14;
        public const ushort RT_VERSION = 16;
        public const ushort RT_DLGINCLUDE = 17;
        public const ushort RT_PLUGPLAY = 19;
        public const ushort RT_VXD = 20;
        public const ushort RT_ANICURSOR = 21;
        public const ushort RT_ANIICON = 22;
        public const ushort RT_HTML = 23;
        public const ushort RT_MANIFEST = 24;
    }
}
