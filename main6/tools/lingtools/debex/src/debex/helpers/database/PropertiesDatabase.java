
package debex.helpers.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public abstract class PropertiesDatabase
{
    private String fileName;
    private Properties props;

    public PropertiesDatabase()
    {
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String string)
    {
        fileName = string;
    }

    protected Properties getProps()
    {
        return props;
    }

    protected void setProps(Properties properties)
    {
        props = properties;
    }

    protected void load()
        throws FileNotFoundException, IOException
    {
        InputStream is = new FileInputStream(fileName);
        props = new Properties();
        props.load(is);
        is.close();
    }

    protected void store()
        throws IOException
    {
        OutputStream os = new FileOutputStream(fileName);
        props.store(os, "");
        os.close();
    }
}
