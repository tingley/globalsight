package com.globalsight.terminology.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.globalsight.importer.ImportOptions;
import com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor;

public class CsvReaderUtil {
	
	//Added for encoding comma within quoted string
    public static final String CVS_replace_str = "#(9#";
    public static final String quotStr = "\"";
    public static final String commaStr = ",";
    public static final String type = "Skip this column";
    public static final String noChooseType ="skip";
    private List columnChooseList = new ArrayList();
    
	/*
     * Added for decode/encoding comma within quoted string
     */
    public String decodeForCSV(String str){
		if(!str.contains(CVS_replace_str)){
			return str;
		}
		return str.replace(CVS_replace_str, ",");
	}
    
    public String[] decodeForCSV(String[] columns){
    	String[] result = columns;
		for(int i=0;i<columns.length;i++){
			result[i]= this.decodeForCSV(columns[i]);
		}
		return result;
	}
    
    public boolean ifEncodeForCSV(String str){
    	return str.indexOf(quotStr)>=0?true:false;
    }
    
    public String encodeForCSV(String str){
		
		if(ifEncodeForCSV(quotStr)){
			StringBuffer resStr= new StringBuffer(str);
			List<Integer> list = this.findQuot(str);
			int result[] = this.setIntArray(list);
			int startPos, endPos,commaPos;
			for(int i=result.length-1;i>0;){
				startPos = result[i-1];
				endPos = result[i];
				commaPos = resStr.indexOf(commaStr, startPos);
				
				while(startPos<endPos)
				{
					if(commaPos<0||commaPos>endPos)
						break;
					
					resStr.replace(commaPos, commaPos+1, CVS_replace_str);
					startPos=commaPos+1;
					endPos=endPos+CVS_replace_str.length()-1;
					commaPos = resStr.indexOf(commaStr, startPos);
					
				}
				i=i-2;
			}
			
			return resStr.toString();
		}else
			return str;
		
	}
	
	//Find quotation marks in String
	public List<Integer> findQuot(String str)
	{
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0,quotPos=0;i<str.length()||quotPos>=0;i++){
			quotPos=str.indexOf(quotStr, i);
			if(quotPos>=0){
				list.add(quotPos);
				i=quotPos;
			}
		}
		return list;
	}
	
	public int[] setIntArray(List list){
		int result[] = null;
		if(null==list||list.size()==0)
			return null;
		else{
			result= new int[list.size()];
			for(int i=0;i<list.size();i++)
				result[i]=(Integer) list.get(i);
		}
		return result;
	}
	
	public String[] getArrayStr(String[] arrStr,int length){
		String result[];
		if(arrStr.length<length||arrStr.length<0||length<0)
			return arrStr;
		else{
			for(int i=length;i<arrStr.length;i++){
				if(!(arrStr==null||arrStr[i].equals("")))
					return arrStr;
			}
			
			result = new String[length];
			for(int i=0;i<arrStr.length&&i<length;i++)
				result[i]=arrStr[i];
			return result;
		}
	}
	
	public String[] getArrayStrAfterTrim(String[] arrStr){
		for (int i = 0; i < arrStr.length; ++i){
			arrStr[i] = arrStr[i].trim();
        }
		return arrStr;
	}
	
	public void setColumnChooseList(ImportOptions toptions){
		com.globalsight.terminology.importer.ImportOptions options =
			(com.globalsight.terminology.importer.ImportOptions) toptions;
		if(columnChooseList!=null&&columnChooseList.size()>0){
			columnChooseList.clear();
		}
		ArrayList<ColumnDescriptor> optList = options.getColumns();
		Iterator it = optList.iterator();
		ColumnDescriptor columnDes;
		while(it.hasNext()){
			columnDes = (ColumnDescriptor) it.next();
			if(null!=columnDes
				&&!noChooseType.equalsIgnoreCase(columnDes.m_type)){
				columnChooseList.add(columnDes.m_position);
			}
		}		
	}
	
	public List getColumnChooseList() {
		return columnChooseList;
	}
	
	/*
	 * Delete the csv quotation mark and trim.
	 */
	public String csvDelQuotation(String str){
		if(str.startsWith("\"")&&str.endsWith("\"")){
			str = (String) str.subSequence(1, str.length()-1);
			str = str.replace("\"\"", "\"");
		}
		return str.trim();
	}
}