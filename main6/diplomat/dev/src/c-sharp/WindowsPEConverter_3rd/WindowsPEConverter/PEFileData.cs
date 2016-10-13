using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
//using Mono.Cecil;

namespace GlobalSight.WinPEConverter
{
    public class PEResourceSign
    {
        public static byte MFR_END = PEUtil.ConvertHexToByte("80");
        public static long MF_POPUP = 0X10L;

        // 1 string, 0 binary
        public static byte[] VERSION_StringFileInfo_1 = { 0, 0, 1, 0, 83, 0, 116, 0, 114, 0, 105, 0, 110, 0, 103, 0, 70, 0, 105, 0, 108, 0, 101, 0, 73, 0, 110, 0, 102, 0, 111, 0 };
        public static byte[] VERSION_StringFileInfo_0 = { 0, 0, 0, 0, 83, 0, 116, 0, 114, 0, 105, 0, 110, 0, 103, 0, 70, 0, 105, 0, 108, 0, 101, 0, 73, 0, 110, 0, 102, 0, 111, 0 };
    }

    public class PEResourceData
    {
        private ushort[] TypeToHandle = { PEResoourceType.RT_MENU, 
                                            PEResoourceType.RT_MESSAGETABLE, 
                                            PEResoourceType.RT_STRING, 
                                            PEResoourceType.RT_DIALOG,
                                        PEResoourceType.RT_VERSION};

        private List<TranslateUnit> _tus;
        private List<DataSub> _datasubs;

        public ushort ResourceType;
        public uint FileOffset;
        public uint Size;

        public byte[] Data;
        public string Content;
        public string PEFileName;

        public List<TranslateUnit> GetTus()
        {
            return _tus;
        }

        public byte[] Merge(List<TranslateUnit> newTus)
        {
            if (_datasubs == null || _datasubs.Count == 0)
            {
                return new byte[0];
            }

            List<Byte> bytes = new List<Byte>();
            TranslateUnit oriTU = null;
            if (_tus != null && _tus.Count > 0)
            {
                oriTU = _tus[0];
                string category = oriTU.Category;
                string categoryIndex = oriTU.Index;

                int tuIndex = 0;
                for(int i = 0; i < _datasubs.Count; i++)
                {
                    DataSub sub = _datasubs[i];

                    if (sub.extracted)
                    {
                        tuIndex = tuIndex + 1;
                        oriTU = _tus[tuIndex - 1];
                        TranslateUnit tu = TranslateUnitUtil.GetTranslateUnit(newTus, category, categoryIndex, "" + tuIndex);
                        string newContent = tu.SourceContent;
                        string oriContent = oriTU.SourceContent;

                        if (oriContent.Contains("\n") && !oriContent.Contains("\r\n"))
                        {
                            newContent = newContent.Replace("\r\n", "\n");
                        }

                        bytes.AddRange(Encoding.Unicode.GetBytes(newContent));
                    }
                    else
                    {
                        bytes.AddRange(sub.data);
                    }
                }
            }
            else
            {
                if (Data != null)
                {
                    return Data;
                }

                foreach (DataSub sub in _datasubs)
                {
                    bytes.AddRange(sub.data);
                }
            }
            return bytes.ToArray();
        }

        public byte[] GetSrcData()
        {
            if (_datasubs == null || _datasubs.Count == 0)
            {
                return new byte[0];
            }

            List<Byte> bytes = new List<Byte>();
            foreach (DataSub sub in _datasubs)
            {
                bytes.AddRange(sub.data);
            }

            return bytes.ToArray();
        }

        public void ParseData(int number)
        {
            _tus = new List<TranslateUnit>();
            _datasubs = new List<DataSub>();

            if (!TypeToHandle.Contains(this.ResourceType))
            {
                return;
            }

            int dataIndex = 0;
            int subIndex = 0;
            string category = null;
            PEWord word = null;
            DataSub datasub = null;
            string categoryIndex = null;
            string content = "";

            switch (this.ResourceType)
            {
                case PEResoourceType.RT_MENU:
                    category = "RT_MENU";
                    subIndex = 0;
                    categoryIndex = "" + PEUtil.GetPEResourceIndex(PEResoourceType.RT_MENU);
                    // ignore MENUHEADER struct (word, word)
                    dataIndex += 4;
                    datasub = new DataSub(false);
                    DataSub.AddData(Data, 0, 4, datasub.data);
                    // check next option
                    PEWord signWord = PEWord.GetNextWord(Data, dataIndex);
                    bool continueGet = true;

                    while (continueGet)
                    {
                        content = "";
                        long idIndex = signWord.Byte0 + signWord.Byte1 * 256;
                        TranslateUnit tu = new TranslateUnit("" + (++number), "" + idIndex, categoryIndex, "" + (++subIndex));
                        tu.Category = category;

                        // is NormalMenuItem or not
                        if (idIndex == 1 && Data[dataIndex + 2] <= 16)
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex = dataIndex + 2;
                        }

                        DataSub.AddData(Data, dataIndex, 2, datasub.data);
                        _datasubs.Add(datasub);
                        dataIndex = dataIndex + 2;

                        datasub = new DataSub(true);

                        word = PEWord.GetNextWord(Data, dataIndex);
                        while (word != null && !word.IsEmpty())
                        {
                            content = content + word.ToUnicodeStr();
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex = dataIndex + 2;
                            word = PEWord.GetNextWord(Data, dataIndex);
                        }

                        tu.SourceContent = content;
                        _tus.Add(tu);
                        _datasubs.Add(datasub);

                        if (word == null)
                        {
                            continueGet = false;
                        }
                        else
                        {
                            datasub = new DataSub(false);
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);

                            while (word.IsEmpty())
                            {
                                dataIndex = dataIndex + 2;
                                word = PEWord.GetNextWord(Data, dataIndex);

                                if (word == null)
                                {
                                    continueGet = false;
                                    break;
                                }

                                if (word.IsEmpty())
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                }
                            }
                        }

                        if (continueGet && !word.IsEmpty())
                        {
                            if (word.Byte0 == PEResourceSign.MFR_END)
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex = dataIndex + 2;
                                signWord = PEWord.GetNextWord(Data, dataIndex);
                            }
                            else
                            {
                                signWord = word;
                            }
                        }
                    }

                    _datasubs.Add(datasub);
                    // end of RE_MENU

                    // remove tus if error
                    if (!XmlUtil.TryWriteTus(_tus))
                    {
                        ClearThisData();
                    }

                    break;

                case PEResoourceType.RT_STRING:
                    category = "RT_STRING";
                    subIndex = 0;
                    categoryIndex = "" + PEUtil.GetPEResourceIndex(PEResoourceType.RT_STRING);

                    PEWord w = PEWord.GetNextWord(Data, dataIndex);
                    datasub = new DataSub(false);
                    while (w != null)
                    {
                        if (w.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex = dataIndex + 2;
                            w = PEWord.GetNextWord(Data, dataIndex);
                        }
                        else
                        {
                            string idIndex = "" + _tus.Count;
                            int count = w.Byte1 * 256 + w.Byte0;

                            // cannot handle this TR_STRING, clear it.
                            if (count * 2 + dataIndex > Data.Length)
                            {
                                ClearThisData();
                                break;
                            }

                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            _datasubs.Add(datasub);

                            dataIndex = dataIndex + 2;
                            content = PEWord.GetNextWords(Data, dataIndex, count * 2);
                            datasub = new DataSub(true);
                            DataSub.AddData(Data, dataIndex, count * 2, datasub.data);

                            dataIndex = dataIndex + count * 2;
                            w = PEWord.GetNextWord(Data, dataIndex);

                            if (w != null)
                            {
                                count = w.Byte1 * 256 + w.Byte0;
                                while (w != null && !w.IsEmpty() && Data.Length - dataIndex < count * 2)
                                {
                                    content += w.ToUnicodeStr();
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    w = PEWord.GetNextWord(Data, dataIndex);
                                    if (w != null)
                                    {
                                        count = w.Byte1 * 256 + w.Byte0;
                                    }
                                }
                            }

                            _datasubs.Add(datasub);
                            datasub = new DataSub(false);

                            TranslateUnit tu = new TranslateUnit("" + (++number), idIndex, categoryIndex, "" + (++subIndex));
                            tu.Category = category;
                            tu.SourceContent = content;
                            _tus.Add(tu);
                        }
                    }

                    _datasubs.Add(datasub);
                    // end of RE_STRING

                    // remove tus if error
                    if (!XmlUtil.TryWriteTus(_tus))
                    {
                        ClearThisData();
                    }

                    break;

                case PEResoourceType.RT_DIALOG:
                    category = "RT_DIALOG";
                    subIndex = 0;
                    categoryIndex = "" + PEUtil.GetPEResourceIndex(PEResoourceType.RT_DIALOG);

                    // extract extend dialog : DLGTEMPLATEEX DLGITEMTEMPLATEEX
                    PEDWord dw = PEDWord.GetNextDWord(Data, 0);
                    if (dw.Byte0 == 1 && dw.Byte1 == 0 && dw.Byte2 == 255 && dw.Byte3 == 255)
                    {
                        // first 22 bytes
                        datasub = new DataSub(false);
                        DataSub.AddData(Data, dataIndex, 22, datasub.data);
                        dataIndex = 22;

                        // styles dword : 12 13 14 15
                        long ddd = 40L;
                        long lll = 8L;
                        long ned = ddd | lll;
                        string hext = PEUtil.ConvertByteArrayToHexString(Data, 12, 4);
                        int style = PEUtil.ConvertHexToInt(hext);
                        bool setStyle = (style == ddd || style == ned);

                        // todo: check menu and windowsClass
                        // short   menu : 0000
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else if (word.Byte0 == 255 && word.Byte1 == 255)
                        {
                            DataSub.AddData(Data, dataIndex, 4, datasub.data);
                            dataIndex += 4;
                        }
                        else
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;

                            word = PEWord.GetNextWord(Data, dataIndex);
                            while (!word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        // short   windowClass : 0000
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else if (word.Byte0 == 255 && word.Byte1 == 255)
                        {
                            DataSub.AddData(Data, dataIndex, 4, datasub.data);
                            dataIndex += 4;
                        }
                        else
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;

                            word = PEWord.GetNextWord(Data, dataIndex);
                            while (!word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }

                        // title or not : 0000 or title
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else
                        {
                            _datasubs.Add(datasub);
                            datasub = new DataSub(true);

                            content = "";
                            while (!word.IsEmpty())
                            {
                                content += word.ToUnicodeStr();
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            TranslateUnit tu = new TranslateUnit("" + (++number), "0", categoryIndex, "" + (++subIndex));
                            tu.Category = category;
                            tu.SourceContent = content;
                            _tus.Add(tu);

                            _datasubs.Add(datasub);
                            datasub = new DataSub(false);
                            while (word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }
                        }

                        bool firstLap = true;
                        // DLGITEMTEMPLATEEX
                        // pointsize word, weight word, italic byte, charset byte, typeface WCHAR[stringLen]
                        if (!setStyle)
                        {
                            while (dataIndex < Size - 27)
                            {
                                // find 80 and start again
                                if (firstLap)
                                {
                                    firstLap = false;
                                    word = PEWord.GetNextWord(Data, dataIndex);

                                    while (word.Byte1 != 80)
                                    {
                                        DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                        dataIndex += 2;
                                        word = PEWord.GetNextWord(Data, dataIndex);

                                        if (word == null)
                                        {
                                            _datasubs.Add(datasub);
                                            return;
                                        }
                                    }

                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }

                                // DLGITEMTEMPLATEEX following
                                // first 12 
                                DataSub.AddData(Data, dataIndex, 12, datasub.data);
                                dataIndex += 12;

                                // windowClass : 0xFFFF or not
                                word = PEWord.GetNextWord(Data, dataIndex);

                                //0x0080 Button
                                //0x0081 Edit
                                //0x0082 Static 
                                //0x0083 List box
                                //0x0084 Scroll bar
                                //0x0085 Combo box
                                if (word.Byte0 == 255 && word.Byte1 == 255)
                                {
                                    DataSub.AddData(Data, dataIndex, 4, datasub.data);
                                    dataIndex += 4;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }
                                //a null-terminated Unicode string 
                                else
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                    while (!word.IsEmpty())
                                    {
                                        DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                        dataIndex += 2;
                                        word = PEWord.GetNextWord(Data, dataIndex);
                                    }

                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }

                                // title  : 0xFFFF or not
                                // If the first element of this array is 0xFFFF, the array has one additional element 
                                // that specifies the ordinal value of a resource, such as an icon, in an executable file.
                                if (word.IsEmpty())
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }
                                else if (word.Byte0 == 255 && word.Byte1 == 255)
                                {
                                    DataSub.AddData(Data, dataIndex, 4, datasub.data);
                                    dataIndex += 4;

                                    _datasubs.Add(datasub);
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }
                                //a null-terminated Unicode string 
                                else
                                {
                                    _datasubs.Add(datasub);
                                    datasub = new DataSub(true);
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                    content = "";
                                    while (!word.IsEmpty())
                                    {
                                        content += word.ToUnicodeStr();
                                        DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                        dataIndex += 2;
                                        word = PEWord.GetNextWord(Data, dataIndex);
                                    }

                                    TranslateUnit tu = new TranslateUnit("" + (++number), "0", categoryIndex, "" + (++subIndex));
                                    tu.Category = category;
                                    tu.SourceContent = content;
                                    _tus.Add(tu);

                                    _datasubs.Add(datasub);
                                    datasub = new DataSub(false);
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }

                                // creat data : 0x0000 or length
                                while (word != null)
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);

                                    if (word != null && word.Byte1 == 80)
                                    {
                                        dataIndex += 2;
                                        word = PEWord.GetNextWord(Data, dataIndex);
                                        break;
                                    }
                                }
                            }

                            if (dataIndex < Size)
                            {
                                datasub = new DataSub(false);
                                DataSub.AddData(Data, dataIndex, (int)Size - dataIndex, datasub.data);
                                _datasubs.Add(datasub);
                            }
                        }
                    }
                    // extract common dialog : DLGTEMPLATE DLGITEMTEMPLATE
                    else
                    {
                        // first 14 bytes
                        datasub = new DataSub(false);
                        DataSub.AddData(Data, dataIndex, 14, datasub.data);
                        dataIndex = 14;

                        // todo: check menu and windowsClass
                        // short   menu : 0000
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else if (word.Byte0 == 255 && word.Byte1 == 255)
                        {
                            DataSub.AddData(Data, dataIndex, 4, datasub.data);
                            dataIndex += 4;
                        }
                        else
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;

                            word = PEWord.GetNextWord(Data, dataIndex);
                            while (!word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        // short   windowClass : 0000
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else if (word.Byte0 == 255 && word.Byte1 == 255)
                        {
                            DataSub.AddData(Data, dataIndex, 4, datasub.data);
                            dataIndex += 4;
                        }
                        else
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;

                            word = PEWord.GetNextWord(Data, dataIndex);
                            while (!word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }

                        // title or not : 0000 or title
                        word = PEWord.GetNextWord(Data, dataIndex);
                        if (word.IsEmpty())
                        {
                            DataSub.AddData(Data, dataIndex, 2, datasub.data);
                            dataIndex += 2;
                        }
                        else
                        {
                            _datasubs.Add(datasub);
                            datasub = new DataSub(true);

                            content = "";
                            while (word != null && !word.IsEmpty())
                            {
                                content += word.ToUnicodeStr();
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            TranslateUnit tu = new TranslateUnit("" + (++number), "0", categoryIndex, "" + (++subIndex));
                            tu.Category = category;
                            tu.SourceContent = content;
                            _tus.Add(tu);

                            _datasubs.Add(datasub);
                            datasub = new DataSub(false);
                            if (word != null)
                            {
                                while (word.IsEmpty())
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }
                            }
                        }

                        bool firstLap = true;
                        // DLGITEMTEMPLATE
                        while (dataIndex < Size - 21)
                        {
                            // find 80 and start again
                            if (firstLap)
                            {
                                firstLap = false;
                                word = PEWord.GetNextWord(Data, dataIndex);

                                while (word.Byte1 != 80)
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);

                                    if (word == null)
                                    {
                                        _datasubs.Add(datasub);
                                        return;
                                    }
                                }

                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            // DLGITEMTEMPLATE following
                            // first 14 
                            DataSub.AddData(Data, dataIndex, 14, datasub.data);
                            dataIndex += 14;

                            // windowClass : 0xFFFF or not
                            word = PEWord.GetNextWord(Data, dataIndex);

                            //0x0080 Button
                            //0x0081 Edit
                            //0x0082 Static 
                            //0x0083 List box
                            //0x0084 Scroll bar
                            //0x0085 Combo box
                            if (word.Byte0 == 255 && word.Byte1 == 255)
                            {
                                DataSub.AddData(Data, dataIndex, 4, datasub.data);
                                dataIndex += 4;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }
                            //a null-terminated Unicode string 
                            else
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                                while (!word.IsEmpty())
                                {
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }

                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            // title  : 0xFFFF or not
                            // If the first element of this array is 0xFFFF, the array has one additional element 
                            // that specifies the ordinal value of a resource, such as an icon, in an executable file.
                            if (word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }
                            else if (word.Byte0 == 255 && word.Byte1 == 255)
                            {
                                DataSub.AddData(Data, dataIndex, 4, datasub.data);
                                dataIndex += 4;

                                _datasubs.Add(datasub);
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }
                            //a null-terminated Unicode string 
                            else
                            {
                                _datasubs.Add(datasub);
                                datasub = new DataSub(true);
                                word = PEWord.GetNextWord(Data, dataIndex);
                                content = "";
                                while (!word.IsEmpty())
                                {
                                    content += word.ToUnicodeStr();
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                }

                                TranslateUnit tu = new TranslateUnit("" + (++number), "0", categoryIndex, "" + (++subIndex));
                                tu.Category = category;
                                tu.SourceContent = content;
                                _tus.Add(tu);

                                _datasubs.Add(datasub);
                                datasub = new DataSub(false);
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }

                            // creat data : 0x0000 or length
                            while (word != null)
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);

                                if (word != null && word.Byte1 == 80)
                                {
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                    break;
                                }
                            }
                        }

                        if (dataIndex < Size)
                        {
                            datasub = new DataSub(false);
                            DataSub.AddData(Data, dataIndex, (int)Size - dataIndex, datasub.data);
                            _datasubs.Add(datasub);
                        }
                    }

                    // end of RT_DIALOG

                    // remove tus if error
                    if (!XmlUtil.TryWriteTus(_tus))
                    {
                        ClearThisData();
                    }

                    break;

                case PEResoourceType.RT_VERSION:
                    category = "RT_VERSION";
                    subIndex = 0;
                    dataIndex = 0;
                    categoryIndex = "" + PEUtil.GetPEResourceIndex(PEResoourceType.RT_VERSION);

                    word = PEWord.GetNextWord(Data, dataIndex);
                    int wLength_VERSION = word.Byte0 + 256 * word.Byte1;

                    dataIndex += 2;
                    word = PEWord.GetNextWord(Data, dataIndex);
                    int wValueLength_VERSION = word.Byte0 + 256 * word.Byte1;

                    int fileInfoIndex = PEUtil.GetIndexOfArray(Data, PEResourceSign.VERSION_StringFileInfo_1);
                    if (fileInfoIndex != -1)
                    {
                        dataIndex = fileInfoIndex - 2;
                        word = PEWord.GetNextWord(Data, dataIndex);
                        int wLength_StringFileInfo = word.Byte0 + 256 * word.Byte1;
                        int end_StringFileInfo = dataIndex + wLength_StringFileInfo;

                        // find String structure
                        dataIndex = dataIndex + PEResourceSign.VERSION_StringFileInfo_1.Length + 2;
                        word = PEWord.GetNextWord(Data, dataIndex);

                        while (word.IsEmpty())
                        {
                            dataIndex += 2;
                            word = PEWord.GetNextWord(Data, dataIndex);
                        }

                        // starting of StringTable
                        int wLength_StringTable = word.Byte0 + 256 * word.Byte1;
                        dataIndex += 22;
                        datasub = new DataSub(false);
                        DataSub.AddData(Data, 0, dataIndex, datasub.data);

                        word = PEWord.GetNextWord(Data, dataIndex);

                        if (word.IsEmpty())
                        {
                            while (word.IsEmpty())
                            {
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);
                            }
                        }
                        _datasubs.Add(datasub);

                        // starting of String
                        while (dataIndex < end_StringFileInfo)
                        {
                            datasub = new DataSub(false);
                            PEWord wLength_String = PEWord.GetNextWord(Data, dataIndex);
                            PEWord wValueLength_String = PEWord.GetNextWord(Data, dataIndex + 2);

                            int wLength_String_int = wLength_String.Byte0 + 256 * wLength_String.Byte1;
                            int wValueLength_String_int = 2 * (wValueLength_String.Byte0 + 256 * wValueLength_String.Byte1);

                            // get id
                            string id = "";
                            int idIndex = dataIndex + 6;
                            PEWord nw = PEWord.GetNextWord(Data, idIndex);

                            if (nw.Byte0 == 1 && nw.Byte1 == 0)
                            {
                                idIndex += 2;
                                nw = PEWord.GetNextWord(Data, idIndex);
                            }

                            while (!nw.IsEmpty())
                            {
                                id += nw.ToUnicodeStr();
                                idIndex += 2;
                                nw = PEWord.GetNextWord(Data, idIndex);
                            }

                            int startOfValue = wLength_String_int - wValueLength_String_int;

                            if (startOfValue < 0 || startOfValue < (id.Length * 2 + 6))
                            {
                                startOfValue = wLength_String_int - wValueLength_String_int / 2;
                            }

                            if (startOfValue < 0)
                            {
                                while (nw.IsEmpty())
                                {
                                    idIndex += 2;
                                    nw = PEWord.GetNextWord(Data, idIndex);
                                }

                                startOfValue = idIndex - dataIndex;
                            }

                            // miss first char of AdobeConverter.exe
                            word = PEWord.GetNextWord(Data, dataIndex + startOfValue - 2);
                            if (!word.IsEmpty())
                            {
                                startOfValue = startOfValue - 2;
                            }

                            word = PEWord.GetNextWord(Data, dataIndex + startOfValue);
                            while (word.IsEmpty())
                            {
                                startOfValue = startOfValue + 2;
                                word = PEWord.GetNextWord(Data, dataIndex + startOfValue);
                            }

                            DataSub.AddData(Data, dataIndex, startOfValue, datasub.data);
                            _datasubs.Add(datasub);
                            dataIndex += startOfValue;
                            content = "";
                            datasub = new DataSub(true);
                            word = PEWord.GetNextWord(Data, dataIndex);

                            while (content.Length < wValueLength_String_int / 2)
                            {
                                content += word.ToUnicodeStr();
                                DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                dataIndex += 2;
                                word = PEWord.GetNextWord(Data, dataIndex);

                                if (word.IsEmpty())
                                {
                                    _datasubs.Add(datasub);

                                    datasub = new DataSub(false);
                                    DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                    dataIndex += 2;
                                    word = PEWord.GetNextWord(Data, dataIndex);
                                    while (word != null && word.IsEmpty())
                                    {
                                        DataSub.AddData(Data, dataIndex, 2, datasub.data);
                                        dataIndex += 2;
                                        word = PEWord.GetNextWord(Data, dataIndex);
                                    }

                                    break;
                                }
                            }

                            TranslateUnit tu = new TranslateUnit("" + (++number), id, categoryIndex, "" + (++subIndex));
                            tu.Category = category;
                            tu.SourceContent = content;
                            _tus.Add(tu);

                            _datasubs.Add(datasub);
                        }

                        datasub = new DataSub(false);
                        DataSub.AddData(Data, dataIndex, Data.Length - dataIndex, datasub.data);
                        _datasubs.Add(datasub);
                    }

                    // end of RT_VERSION

                    // remove tus if error
                    if (!XmlUtil.TryWriteTus(_tus))
                    {
                        ClearThisData();
                    }

                    break;
            }
        }

        private void ClearThisData()
        {
            _tus.Clear();
            _datasubs.Clear();
        }
    }

    public class PEWord
    {
        public byte Byte0;
        public byte Byte1;
        public PEWord(byte f, byte s)
        {
            Byte0 = f;
            Byte1 = s;
        }

        public bool IsEmpty()
        {
            return Byte0 == 0 && Byte1 == 0;
        }

        public string ToUnicodeStr()
        {
            return Encoding.Unicode.GetString(new byte[] { Byte0, Byte1 });
        }

        public static PEWord GetNextWord(byte[] data, int index)
        {
            if (index > data.Length - 2)
            {
                return null;
            }

            byte f = data[index];
            byte s = data[index + 1];

            return new PEWord(f, s);
        }

        public static string GetNextWords(byte[] data, int index, int count)
        {
            return Encoding.Unicode.GetString(data, index, count);
        }
    }

    public class PEDWord
    {
        public byte Byte0;
        public byte Byte1;
        public byte Byte2;
        public byte Byte3;
        public PEDWord(byte first, byte second, byte three, byte third)
        {
            Byte0 = first;
            Byte1 = second;
            Byte2 = three;
            Byte3 = third;
        }

        public bool IsEmpty()
        {
            return Byte0 == 0 && Byte1 == 0 && Byte2 == 0 && Byte3 == 0;
        }

        public string ToUnicodeStr()
        {
            return Encoding.Unicode.GetString(new byte[] { Byte0, Byte1, Byte2, Byte3 });
        }

        public static PEDWord GetNextDWord(byte[] data, int index)
        {
            if (index > data.Length - 4)
            {
                return null;
            }

            byte b0 = data[index];
            byte b1 = data[index + 1];
            byte b2 = data[index + 2];
            byte b3 = data[index + 3];

            return new PEDWord(b0, b1, b2, b3);
        }
    }

    public class DataSub
    {
        public DataSub(bool p_extracted)
        {
            data = new List<byte>();
            extracted = p_extracted;
        }

        public static void AddData(byte[] data, int index, int length, List<byte> list)
        {
            for (int i = 0; i < length; i++)
            {
                byte b = data[index + i];
                list.Add(b);
            }
        }

        public List<Byte> data;
        public bool extracted;
    }

    public class PEResourceDataList : IEnumerable<PEResourceData>
    {
        private List<PEResourceData> list = new List<PEResourceData>();

        public void Add(PEResourceData resData)
        {
            list.Add(resData);
        }

        public List<PEResourceData> GetByType(int resType)
        {
            List<PEResourceData> result = new List<PEResourceData>();

            foreach (PEResourceData d in list)
            {
                if (d.ResourceType == resType)
                {
                    result.Add(d);
                }
            }

            return result;
        }

        public List<PEResourceData> GetAll()
        {
            return list;
        }

        #region IEnumerable<PEResourceData> Members

        public IEnumerator<PEResourceData> GetEnumerator()
        {
            return list.GetEnumerator();
        }

        #endregion

        #region IEnumerable Members

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return list.GetEnumerator();
        }

        #endregion
    }
}
