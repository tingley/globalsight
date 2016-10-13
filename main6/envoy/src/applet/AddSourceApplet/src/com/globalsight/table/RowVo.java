package com.globalsight.table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.vo.FileProfileVo;

public class RowVo
{
    private File file;
    private List<FileProfileVo> fileProfiles = new ArrayList<FileProfileVo>();
    private int selectIndex = -1;
    
    public String getSelectFileProfile()
    {
        if (selectIndex < 0)
            return "";
        
        return fileProfiles.get(selectIndex).getName();
    }
    
    public FileProfileVo getSelectedFileProfile()
    {
        if (selectIndex < 0)
            return null;
        
        return fileProfiles.get(selectIndex);
    }
    
    public File getFile()
    {
        return file;
    }
    public void setFile(File file)
    {
        this.file = file;
    }
    public List<FileProfileVo> getFileProfiles()
    {
        return fileProfiles;
    }
    public void setFileProfiles(List<FileProfileVo> fileProfiles)
    {
        this.fileProfiles = fileProfiles;
    }
    public int getSelectIndex()
    {
        return selectIndex;
    }
    public void setSelectIndex(int selectIndex)
    {
        this.selectIndex = selectIndex;
    }
}
