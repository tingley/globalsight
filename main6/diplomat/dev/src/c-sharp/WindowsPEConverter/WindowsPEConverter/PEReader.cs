using System;
using System.Collections.Generic;
using System.Collections;
using System.Linq;
using System.Text;
using System.IO;
using System.Runtime.InteropServices;

namespace GlobalSight.WinPEConverter
{
    public class PEReader
    {
        #region PE Structures

        // DOS header
        public struct IMAGE_DOS_HEADER
        {
            public UInt16 e_magic;              // Magic number
            public UInt16 e_cblp;               // Bytes on last page of file
            public UInt16 e_cp;                 // Pages in file
            public UInt16 e_crlc;               // Relocations
            public UInt16 e_cparhdr;            // Size of header in paragraphs
            public UInt16 e_minalloc;           // Minimum extra paragraphs needed
            public UInt16 e_maxalloc;           // Maximum extra paragraphs needed
            public UInt16 e_ss;                 // Initial (relative) SS value
            public UInt16 e_sp;                 // Initial SP value
            public UInt16 e_csum;               // Checksum
            public UInt16 e_ip;                 // Initial IP value
            public UInt16 e_cs;                 // Initial (relative) CS value
            public UInt16 e_lfarlc;             // File address of relocation table
            public UInt16 e_ovno;               // Overlay number
            public UInt16 e_res_0;              // Reserved words
            public UInt16 e_res_1;              // Reserved words
            public UInt16 e_res_2;              // Reserved words
            public UInt16 e_res_3;              // Reserved words
            public UInt16 e_oemid;              // OEM identifier (for e_oeminfo)
            public UInt16 e_oeminfo;            // OEM information; e_oemid specific
            public UInt16 e_res2_0;             // Reserved words
            public UInt16 e_res2_1;             // Reserved words
            public UInt16 e_res2_2;             // Reserved words
            public UInt16 e_res2_3;             // Reserved words
            public UInt16 e_res2_4;             // Reserved words
            public UInt16 e_res2_5;             // Reserved words
            public UInt16 e_res2_6;             // Reserved words
            public UInt16 e_res2_7;             // Reserved words
            public UInt16 e_res2_8;             // Reserved words
            public UInt16 e_res2_9;             // Reserved words
            public UInt32 e_lfanew;             // File address of new exe header
        }

        /// <summary>
        /// NumberOfRvaAndSizes - The number of directory entries in the remainder of the optional header
        /// </summary>
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_OPTIONAL_HEADER32
        {
            public UInt16 Magic;
            public Byte MajorLinkerVersion;
            public Byte MinorLinkerVersion;
            public UInt32 SizeOfCode;
            public UInt32 SizeOfInitializedData;
            public UInt32 SizeOfUninitializedData;
            public UInt32 AddressOfEntryPoint;
            public UInt32 BaseOfCode;
            public UInt32 BaseOfData;
            public UInt32 ImageBase;
            // alignment in memory
            public UInt32 SectionAlignment;
            // alignmen in file
            public UInt32 FileAlignment;
            public UInt16 MajorOperatingSystemVersion;
            public UInt16 MinorOperatingSystemVersion;
            public UInt16 MajorImageVersion;
            public UInt16 MinorImageVersion;
            public UInt16 MajorSubsystemVersion;
            public UInt16 MinorSubsystemVersion;
            public UInt32 Win32VersionValue;
            public UInt32 SizeOfImage;
            public UInt32 SizeOfHeaders;
            public UInt32 CheckSum;
            public UInt16 Subsystem;
            public UInt16 DllCharacteristics;
            public UInt32 SizeOfStackReserve;
            public UInt32 SizeOfStackCommit;
            public UInt32 SizeOfHeapReserve;
            public UInt32 SizeOfHeapCommit;
            public UInt32 LoaderFlags;
            public UInt32 NumberOfRvaAndSizes;
        }

        /// <summary>
        /// NumberOfRvaAndSizes - The number of directory entries in the remainder of the optional header
        /// </summary>
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_OPTIONAL_HEADER64
        {
            public UInt16 Magic;
            public Byte MajorLinkerVersion;
            public Byte MinorLinkerVersion;
            public UInt32 SizeOfCode;
            public UInt32 SizeOfInitializedData;
            public UInt32 SizeOfUninitializedData;
            public UInt32 AddressOfEntryPoint;
            public UInt32 BaseOfCode;
            public UInt64 ImageBase;
            // alignment in memory
            public UInt32 SectionAlignment;
            // alignmen in file
            public UInt32 FileAlignment;
            public UInt16 MajorOperatingSystemVersion;
            public UInt16 MinorOperatingSystemVersion;
            public UInt16 MajorImageVersion;
            public UInt16 MinorImageVersion;
            public UInt16 MajorSubsystemVersion;
            public UInt16 MinorSubsystemVersion;
            public UInt32 Win32VersionValue;
            public UInt32 SizeOfImage;
            public UInt32 SizeOfHeaders;
            public UInt32 CheckSum;
            public UInt16 Subsystem;
            public UInt16 DllCharacteristics;
            public UInt64 SizeOfStackReserve;
            public UInt64 SizeOfStackCommit;
            public UInt64 SizeOfHeapReserve;
            public UInt64 SizeOfHeapCommit;
            public UInt32 LoaderFlags;
            public UInt32 NumberOfRvaAndSizes;
        }

        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_FILE_HEADER
        {
            public UInt16 Machine;
            public UInt16 NumberOfSections;
            public UInt32 TimeDateStamp;
            public UInt32 PointerToSymbolTable;
            public UInt32 NumberOfSymbols;
            public UInt16 SizeOfOptionalHeader;
            public UInt16 Characteristics;
        }

        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_DATA_DIRECTORY
        {
            public UInt32 VirtualAddress; // RVA from PE00
            public UInt32 Size;
        }

        // section table
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_SECTION_HEADER
        {
            public Byte Name_0;
            public Byte Name_1;
            public Byte Name_2;
            public Byte Name_3;
            public Byte Name_4;
            public Byte Name_5;
            public Byte Name_6;
            public Byte Name_7;
            public UInt32 PhysicalAddressOrVirtualSize;
            public UInt32 VirtualAddress; // rva
            public UInt32 SizeOfRawData; // size
            public UInt32 PointerToRawData; // offset to file begin
            public UInt32 PointerToRelocations;
            public UInt32 PointerToLinenumbers;
            public UInt16 NumberOfRelocations;
            public UInt16 NumberOfLinenumbers;
            public UInt32 Characteristics;
        }

        // resource
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_RESOURCE_DIRECTORY
        {
            public UInt32 Characteristics;
            public UInt32 TimeDateStamp;
            public UInt16 MajorVersion;
            public UInt16 MinorVersion;
            public UInt16 NumberOfNamedEntries;
            public UInt16 NumberOfIdEntries;
        }
        //--------------------------------------------------------
        // level 1 : name - type
        // name : 01h cursor, 02h bitmap, 03h icon, 04h menu, 05h dialog,
        //  06h String Table, 07h font directory, 08h font, 09h Accelerator, 
        //  0Ah Unformatted resources, 0Bh message table, 0Ch cursor group, 0Dh Icon Group,
        //  0Eh Version Information, 10h icon group, 18h assembly xml, others
        // OffsetToData_h : 
        //  start with 1, OffsetToData_l point to next level adress
        //  start with 1, OffsetToData_l point to IMAGE_RESOURCE_DATA_ENTRY
        //--------------------------------------------------------
        // level 2: name - resource name
        // 
        //--------------------------------------------------------
        // level 3: name - language id
        // 
        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_RESOURCE_DIRECTORY_ENTRY
        {
            public UInt16 Name_l;
            public UInt16 Name_h;
            public UInt16 OffsetToData_l; // offset to section beginning
            public UInt16 OffsetToData_h;
        }

        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_RESOURCE_DIR_STRING_U
        {
            public UInt32 Length;
            public UInt32 NameString;
        }

        [StructLayout(LayoutKind.Sequential, Pack = 1)]
        public struct IMAGE_RESOURCE_DATA_ENTRY
        {
            // RVA, offset to the beginning of the resource directory of the resource data
            public UInt32 OffsetToData; 
            public UInt32 Size;
            public UInt32 CodePage;
            public UInt32 Reserved;
        }

        #endregion PE Structures

        #region Private Fields

        // The DOS header
        private IMAGE_DOS_HEADER dosHeader;
        // The file header
        private IMAGE_FILE_HEADER fileHeader;
        // Optional 32 bit file header
        private IMAGE_OPTIONAL_HEADER32 optionalHeader32;
        // Optional 64 bit file header
        private IMAGE_OPTIONAL_HEADER64 optionalHeader64;
        //Directory Entries
        /**
        #define IMAGE_DIRECTORY_ENTRY_EXPORT          0   // Export Directory
        #define IMAGE_DIRECTORY_ENTRY_IMPORT          1   // Import Directory
        #define IMAGE_DIRECTORY_ENTRY_RESOURCE        2   // Resource Directory
        #define IMAGE_DIRECTORY_ENTRY_EXCEPTION       3   // Exception Directory
        #define IMAGE_DIRECTORY_ENTRY_SECURITY        4   // Security Directory
        #define IMAGE_DIRECTORY_ENTRY_BASERELOC       5   // Base Relocation Table
        #define IMAGE_DIRECTORY_ENTRY_DEBUG           6   // Debug Directory
        //      IMAGE_DIRECTORY_ENTRY_COPYRIGHT       7   // (X86 usage)
        #define IMAGE_DIRECTORY_ENTRY_ARCHITECTURE    7   // Architecture Specific Data
        #define IMAGE_DIRECTORY_ENTRY_GLOBALPTR       8   // RVA of GP
        #define IMAGE_DIRECTORY_ENTRY_TLS             9   // TLS Directory
        #define IMAGE_DIRECTORY_ENTRY_LOAD_CONFIG    10   // Load Configuration Directory
        #define IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT   11   // Bound Import Directory in headers
        #define IMAGE_DIRECTORY_ENTRY_IAT            12   // Import Address Table
        #define IMAGE_DIRECTORY_ENTRY_DELAY_IMPORT   13   // Delay Load Import Descriptors
        #define IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR 14   // COM Runtime descriptor
        # 15 unuse
        **/
        private IMAGE_DATA_DIRECTORY[] imageDataDirectory;

        private IMAGE_SECTION_HEADER[] imageSectionHeader;

        private uint resource_offset;
        private uint resource_rva;
        private uint resource_rawDataSize;
        private PEResourceEntries[] ResourceEntry_ALL;

        #endregion Private Fields

        #region Public Methods

        public PEReader(string filePath)
        {
            // Read in the DLL or EXE and get the timestamp
            using (FileStream stream = new FileStream(filePath, System.IO.FileMode.Open, System.IO.FileAccess.Read))
            {
                BinaryReader reader = new BinaryReader(stream);
                dosHeader = FromBinaryReader<IMAGE_DOS_HEADER>(reader);

                // Add 4 bytes to the offset
                stream.Seek(dosHeader.e_lfanew, SeekOrigin.Begin);

                UInt32 ntHeadersSignature = reader.ReadUInt32();
                fileHeader = FromBinaryReader<IMAGE_FILE_HEADER>(reader);
                if (this.Is32BitHeader)
                {
                    optionalHeader32 = FromBinaryReader<IMAGE_OPTIONAL_HEADER32>(reader);
                }
                else
                {
                    optionalHeader64 = FromBinaryReader<IMAGE_OPTIONAL_HEADER64>(reader);
                }

                // image data directory
                int numberOfRvaAndSizes = (int)((this.Is32BitHeader) ? optionalHeader32.NumberOfRvaAndSizes : optionalHeader64.NumberOfRvaAndSizes);
                imageDataDirectory = new IMAGE_DATA_DIRECTORY[numberOfRvaAndSizes];

                for (int i = 0; i < numberOfRvaAndSizes; i++)
                {
                    IMAGE_DATA_DIRECTORY direc = FromBinaryReader<IMAGE_DATA_DIRECTORY>(reader);
                    imageDataDirectory[i] = direc;
                }

                // image section header, optionalHeader offset 18h = 24
                uint optionSize = fileHeader.SizeOfOptionalHeader;
                stream.Seek(dosHeader.e_lfanew + 24 + optionSize, SeekOrigin.Begin);

                imageSectionHeader = new IMAGE_SECTION_HEADER[FileHeader.NumberOfSections];
                for (int i = 0; i < FileHeader.NumberOfSections; i++)
                {
                    IMAGE_SECTION_HEADER sectionHeader = FromBinaryReader<IMAGE_SECTION_HEADER>(reader);
                    imageSectionHeader[i] = sectionHeader;
                }

                // read resource
                IMAGE_DATA_DIRECTORY resourceDataDirectory = imageDataDirectory[2];
                uint resDataRVA = resourceDataDirectory.VirtualAddress;
                uint resDataSize = resourceDataDirectory.Size;
                uint resEndRVA = resDataRVA + resDataSize;
                resource_offset = 0;
                resource_rva = 0;
                resource_rawDataSize = 0;
                foreach (IMAGE_SECTION_HEADER sectionHeader in imageSectionHeader)
                {
                    uint secRVA = sectionHeader.VirtualAddress;
                    uint secEndRVA = secRVA + sectionHeader.SizeOfRawData;
                    if (secRVA <= resDataRVA && resEndRVA > secRVA && secEndRVA >= resEndRVA)
                    {
                        resource_offset = sectionHeader.PointerToRawData;
                        resource_rva = secRVA;
                        resource_rawDataSize = sectionHeader.SizeOfRawData;
                    }
                }

                if (resource_offset == 0)
                {
                    ResourceEntry_ALL = new PEResourceEntries[0];
                }
                else
                {
                    stream.Seek(resource_offset, SeekOrigin.Begin);

                    // resource level 1
                    IMAGE_RESOURCE_DIRECTORY d1 = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY>(reader);
                    int entriesCount1 = d1.NumberOfIdEntries + d1.NumberOfNamedEntries;
                    IMAGE_RESOURCE_DIRECTORY_ENTRY[] entries1 = new IMAGE_RESOURCE_DIRECTORY_ENTRY[entriesCount1];
                    ResourceEntry_ALL = new PEResourceEntries[entriesCount1];
                    for (int i = 0; i < entriesCount1; i++)
                    {
                        IMAGE_RESOURCE_DIRECTORY_ENTRY entry = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY_ENTRY>(reader);
                        entries1[i] = entry;
                    }

                    for (int i = 0; i < entriesCount1; i++)
                    {
                        IMAGE_RESOURCE_DIRECTORY_ENTRY entry = entries1[i];
                        PEResourceEntries resEntries = new PEResourceEntries();
                        resEntries.level1Entry = entry;
                        resEntries.level2Map3Entries = new Hashtable();
                        resEntries.level3DATA = new Hashtable();

                        // level 2
                        // type 
                        long offset_2 = resource_offset + entry.OffsetToData_l;
                        stream.Seek(offset_2, SeekOrigin.Begin);
                        if (PEUtil.ConvertIntToBin((int)entry.OffsetToData_h).StartsWith("1"))
                        {
                            IMAGE_RESOURCE_DIRECTORY d2 = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY>(reader);
                            int entriesCount2 = d2.NumberOfIdEntries + d2.NumberOfNamedEntries;
                            IMAGE_RESOURCE_DIRECTORY_ENTRY[] entries2 = new IMAGE_RESOURCE_DIRECTORY_ENTRY[entriesCount2];
                            for (int ii = 0; ii < entriesCount2; ii++)
                            {
                                IMAGE_RESOURCE_DIRECTORY_ENTRY entry2 = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY_ENTRY>(reader);
                                entries2[ii] = entry2;
                            }
                            resEntries.level2Entries = entries2;

                            // level 3
                            for (int j = 0; j < entriesCount2; j++)
                            {
                                IMAGE_RESOURCE_DIRECTORY_ENTRY entry2 = entries2[j];
                                // type 
                                long offset_3 = resource_offset + entry2.OffsetToData_l;
                                stream.Seek(offset_3, SeekOrigin.Begin);

                                IMAGE_RESOURCE_DIRECTORY d3 = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY>(reader);
                                int entriesCount3 = d3.NumberOfIdEntries + d3.NumberOfNamedEntries;
                                IMAGE_RESOURCE_DIRECTORY_ENTRY[] entries3 = new IMAGE_RESOURCE_DIRECTORY_ENTRY[entriesCount3];
                                for (int k = 0; k < entriesCount3; k++)
                                {
                                    IMAGE_RESOURCE_DIRECTORY_ENTRY entry3 = FromBinaryReader<IMAGE_RESOURCE_DIRECTORY_ENTRY>(reader);
                                    entries3[k] = entry3;

                                    long offset_4 = resource_offset + entry3.OffsetToData_l;
                                    stream.Seek(offset_4, SeekOrigin.Begin);
                                    IMAGE_RESOURCE_DATA_ENTRY dataEntry = FromBinaryReader<IMAGE_RESOURCE_DATA_ENTRY>(reader);
                                    
                                    if (!resEntries.level3DATA.ContainsKey(entry3))
                                    {
                                        resEntries.level3DATA.Add(entry3, dataEntry);
                                    }
                                }
                                resEntries.level2Map3Entries.Add(entry2, entries3);
                            }
                        }
                        else
                        {
                            throw new Exception("Resource level 2 OffsetToData_h can not start with 0");
                            //                        IMAGE_RESOURCE_DATA_ENTRY dataEntry = FromBinaryReader<IMAGE_RESOURCE_DATA_ENTRY>(reader);
                        }

                        ResourceEntry_ALL[i] = resEntries;
                    }
                }
            }
        }

        // Gets the header of the .NET assembly that called this function
        public static PEReader GetCallingAssemblyHeader()
        {
            string pathCallingAssembly = System.Reflection.Assembly.GetCallingAssembly().Location;

            // Get the path to the calling assembly, which is the path to the
            // DLL or EXE that we want the time of
            string filePath = System.Reflection.Assembly.GetCallingAssembly().Location;

            // Get and return the timestamp
            return new PEReader(filePath);
        }

        // Reads in a block from a file and converts it to the struct
        // type specified by the template parameter
        public static T FromBinaryReader<T>(BinaryReader reader)
        {
            // Read in a byte array
            byte[] bytes = reader.ReadBytes(Marshal.SizeOf(typeof(T)));

            // Pin the managed memory while, copy it out the data, then unpin it
            GCHandle handle = GCHandle.Alloc(bytes, GCHandleType.Pinned);
            T theStructure = (T)Marshal.PtrToStructure(handle.AddrOfPinnedObject(), typeof(T));
            handle.Free();

            return theStructure;
        }

        #endregion Public Methods

        #region Properties

        // Gets if the file header is 32 bit or not
        public bool Is32BitHeader
        {
            get
            {
                UInt16 IMAGE_FILE_32BIT_MACHINE = 0x0100;
                return (IMAGE_FILE_32BIT_MACHINE & FileHeader.Characteristics) == IMAGE_FILE_32BIT_MACHINE;
            }
        }

        // Gets the file header
        public IMAGE_FILE_HEADER FileHeader
        {
            get
            {
                return fileHeader;
            }
        }

        // Gets the optional header
        public IMAGE_OPTIONAL_HEADER32 OptionalHeader32
        {
            get
            {
                return optionalHeader32;
            }
        }

        // Gets the optional header
        public IMAGE_OPTIONAL_HEADER64 OptionalHeader64
        {
            get
            {
                return optionalHeader64;
            }
        }

        public IMAGE_DATA_DIRECTORY[] ImageDataDirectory
        {
            get
            {
                return imageDataDirectory;
            }
        }

        public IMAGE_SECTION_HEADER[] ImageSectionHeader
        {
            get
            {
                return imageSectionHeader;
            }
        }

        // Gets the timestamp from the file header
        public DateTime TimeStamp
        {
            get
            {
                // Timestamp is a date offset from 1970
                DateTime returnValue = new DateTime(1970, 1, 1, 0, 0, 0);

                // Add in the number of seconds since 1970/1/1
                returnValue = returnValue.AddSeconds(fileHeader.TimeDateStamp);
                // Adjust to local timezone
                returnValue += TimeZone.CurrentTimeZone.GetUtcOffset(returnValue);

                return returnValue;
            }
        }

        public PEResourceEntries[] ResourceEntriesAll
        {
            get
            {
                return ResourceEntry_ALL;
            }
        }

        public uint ResourceOffSet
        {
            get
            {
                return resource_offset;
            }
        }

        public uint ResourceRVA
        {
            get
            {
                return resource_rva;
            }
        }

        #endregion Properties
    }
}
