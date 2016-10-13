package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.HtmlEntities;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.ling.tw.PseudoBaseHandler;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoOverrideItemException;
import com.globalsight.ling.tw.PseudoParser;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.XmlEntities;
import com.globalsight.ling.tw.online.OnlineApplet;

/**
 * Copy from OnlineApplet
 * 
 * As onlineApplet is used as utility class in UI, it implements segment check
 * and convert, so separate into here.
 * 
 * @author leon
 * 
 */
public class SegmentManager implements PseudoBaseHandler
{
    private String inputSegment = null;
    private String outputSegment = null;
    private String targetEncoding = null;
    private String segmentFormat = null;
    private TmxPseudo converter = null;
    private PseudoData withPtags = null;
    private HtmlTableWriter HtmlTableWriter = null;
    private XmlEntities xmlCodec = null;
    private boolean bPTagResourcesInitialized = false;
    private StringBuffer coloredPtags = null;
    private PseudoParser ptagParser = null;

    private static final String PTAG_COLOR_START = "<SPAN DIR=ltr class=ptag UNSELECTABLE=on CONTENTEDITABLE=true>";
    private static final String PTAG_COLOR_END = "</SPAN>";
    private PseudoErrorChecker errChecker = null;

    public SegmentManager()
    {
        coloredPtags = new StringBuffer();
        ptagParser = new PseudoParser(this);
    }
    
    public PseudoData getPseudoData()
    {
        return withPtags;
    }

    /**
     * Get the current source string with maximally compressed p-tags. This
     * string would then be displayed as the initial target string.
     */
    public String getCompact() throws DiplomatBasicParserException,
            PseudoParserException
    {
        if (bPTagResourcesInitialized)
        {
            convertToPtags(PseudoConstants.PSEUDO_COMPACT);
            return withPtags.getPTagSourceString();
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Generate a string of ptags separated by comma ("l1,/l1,g1,/g1").
     */
    public String getPtagString() throws DiplomatBasicParserException
    {
        if (bPTagResourcesInitialized)
        {
            return HtmlTableWriter.getPtagString(withPtags
                    .getPseudo2NativeMap());
        }
        else
        {
            throw new DiplomatBasicParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    /**
     * Get the translated target string encoded with Diplomat tags.
     */
    public String getTargetDiplomat(String target) throws PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (target == null)
        {
            target = "";
        }

        // if (errChecker != null)
        // {
        // target = errChecker.revertInternalTags(target);
        // }

        if (bPTagResourcesInitialized)
        {
            withPtags.setPTagTargetString(target);
            outputSegment = converter.pseudo2Tmx(withPtags);
            return outputSegment;
        }
        else
        {
            throw new PseudoParserException(
                    "Should call setLocale() or setInputSegment() first.");
        }

    }

    private void convertToPtags(int mode) throws DiplomatBasicParserException
    {
        withPtags.setMode(mode);
        converter.tmx2Pseudo(inputSegment, withPtags);
    }

    /*
     * Initializes PTag resources.<p>
     * 
     * The main purpose for creating this method was to catch exceptions that
     * could be thrown when the PseudoOverride rules are initialized within the
     * PseudoData constructor.<pp>
     * 
     * @throws PseudoOverrideItemException
     */
    private void initPTagResources() throws PseudoOverrideItemException
    {
        xmlCodec = new XmlEntities();
        converter = new TmxPseudo();
        withPtags = new PseudoData();
        HtmlTableWriter = new HtmlTableWriter();
        errChecker = new PseudoErrorChecker();
    }

    /*
     * Set the Diplomat input string and convert it to p-tags internally. After
     * setting the string, you can getCompact() or getVerbose().
     * 
     * @param source String
     * 
     * @param encoding String
     * 
     * @param segmentFormat String - an empty, null or otherwise incorrect value
     * disables addables.
     */
    public void setInputSegment(String source, String encoding,
            String segmentFormat) throws PseudoOverrideItemException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (source == null)
        {
            source = "";
        }

        // we must init things here because we cannot throw exceptions
        // from the applets init() method.
        if (!bPTagResourcesInitialized)
        {
            initPTagResources();
            bPTagResourcesInitialized = true;
        }

        inputSegment = source;
        targetEncoding = encoding;
        this.segmentFormat = segmentFormat;

        withPtags.setAddables(segmentFormat);
    }

    /**
     */
    public String makeCompactColoredPtags(String diplomat)
            throws DiplomatBasicParserException, PseudoOverrideItemException,
            PseudoParserException
    {
        // Empty strings arrive as NULL pointer somehow, so fix that
        if (diplomat == null)
        {
            diplomat = "";
        }

        TmxPseudo converter = new TmxPseudo();
        PseudoData withPtags = new PseudoData();

        withPtags.setMode(PseudoConstants.PSEUDO_COMPACT);
        withPtags.setAddables(segmentFormat);
        converter.tmx2Pseudo(diplomat, withPtags);

        return convertToColored(withPtags.getWrappedPTagSourceString());
    }

    /**
     * Returns an HTML color encoded ptag string.
     * <p>
     * 
     * Only the necessary HTML to color the tags is added and the necessary
     * escapes are encoded.
     */
    private String convertToColored(String ptag) throws PseudoParserException
    {
        HtmlEntities html = new HtmlEntities();

        coloredPtags.setLength(0);
        ptagParser.tokenize(html.encodeString(ptag));

        return coloredPtags.toString();
    }

    /**
     * This method should always be called once before calling getVerbose() or
     * getCompact(). If you do not check for errors before calling getVerbose or
     * getCompact(), exceptions are likely to occur.
     * 
     * The method takes two boolean flags as parameters: 1) Verify leading
     * whitespace. (not implimented in this release) 2) Verify trailing
     * whitepsace. (not implimented in this release) NOTE: Neither are
     * implemented in this release.
     * 
     * @return String empty string or error message
     * @param target
     *            the target string
     * @param sourceWithSubContent
     *            the source string with subs (editors map-segment).
     * @param gxmlMaxLen
     *            maximum length of the gxml string as stored in the system3
     *            database if set to zero, no checking is performed.
     * @param gxmlStorageEncoding
     *            the encoding of the gxml string as stored in system3 database
     *            if empty string or null, exception is thrown.
     * @param nativeContentMaxLen
     *            maximum length of native content as stored in the clients
     *            database if set to zero, no checking is performed.
     * @param nativeStorageEncoding
     *            the encoding of native content as stored in the clients
     *            database if empty string or null, exception is thrown.
     * @throws Exception
     */
    public String errorCheck(String target, String sourceWithSubContent,
            int gxmlMaxLen, String gxmlStorageEncoding,
            int nativeContentMaxLen, String nativeStorageEncoding)
            throws Exception
    {
        // We now allow for an empty target string. If the user
        // thinks a non-empty source should be translated to empty,
        // this is what (s)he will be allowed to do. But only if the
        // source has no-ptags-at-all or only deletable ptags.

        // Empty strings arrive as NULL pointer somehow, so fix that
        if (target == null)
        {
            target = "";
        }

        if (bPTagResourcesInitialized)
        {
            // To add length checking with the least amount of impact
            // on editor we use the withPtags (PseudoData) object as
            // we did before. This keeps the rules for using the API
            // the same. We also have to stay in sync with the
            // compact/verbose mode.
            withPtags.setPTagTargetString(target);

            return errChecker.check(withPtags, sourceWithSubContent,
                    gxmlMaxLen, gxmlStorageEncoding, nativeContentMaxLen,
                    nativeStorageEncoding);
        }
        else
        {
            throw new Exception(
                    "Should call setLocale() or setInputSegment() first.");
        }
    }

    @Override
    public void processTag(String tagName, String originalString)
            throws PseudoParserException
    {
        coloredPtags.append(PTAG_COLOR_START + originalString + PTAG_COLOR_END);

    }

    @Override
    public void processText(String strText)
    {
        coloredPtags.append(strText);
    }

    public static void main(String[] args)
    {
        OnlineApplet oa = new OnlineApplet();
        // String diplomat =
        // "<bpt type=\"x-span\" x=\"1\" i=\"1\">&lt;span style=&apos;font-family:&quot;Arial&quot;,&quot;sans-serif&quot;&apos;&gt;</bpt>Sample Document<ept i=\"1\">&lt;/span&gt;</ept>";
        String diplomat = "<ph id=\"0\" xmlns=\"\">&lt;span contenteditable=\"false\"&gt;</ph>Update the following table as necessary when this document is changed:<ph id=\"1\" xmlns=\"\">&lt;/span&gt;</ph>";
        try
        {
            oa.init();
            oa.setInputSegment(diplomat, "UTF8", "xlf");
            String str = oa.makeCompactColoredPtags(diplomat);
            System.out.println("str : " + str);
            String tmp = "[x0]Update the following table as necessary when this document is changed changed :[x1]";
            String str2 = oa.getTargetDiplomat(tmp);
            System.out.println("str2 : " + str2);
        }
        catch (DiplomatBasicParserException e)
        {
            e.printStackTrace();
        }
        catch (PseudoOverrideItemException e)
        {
            e.printStackTrace();
        }
        catch (PseudoParserException e)
        {
            e.printStackTrace();
        }
    }
}
