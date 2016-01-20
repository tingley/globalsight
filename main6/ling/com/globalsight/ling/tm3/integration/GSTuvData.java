package com.globalsight.ling.tm3.integration;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.FuzzyIndexFormatHandler;
import com.globalsight.ling.tm2.lucene.LuceneUtil;
import com.globalsight.ling.common.SegmentTmExactMatchFormatHandler;
import com.globalsight.ling.tm3.core.TM3Data;
import com.globalsight.ling.tm3.core.Fingerprint;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.util.GlobalSightLocale;

/**
 * Default implementation of TuvData for holding GS GSXML.
 */
public class GSTuvData implements TM3Data {

    private String data;
    private String parsedData = null;
    private GlobalSightLocale locale;
    private Long fingerprint;
    private BaseTmTuv srcTuv = null;

    GSTuvData() {
        
    }
    
    public GSTuvData(BaseTmTuv srcTuv) {
        this(srcTuv.getSegment(), srcTuv.getLocale());
    	this.srcTuv = srcTuv;
    }
    
    public GSTuvData(String data, GlobalSightLocale locale) {
        this.data = data;
        this.locale = locale;
    }
    
    public String getData() {
        return data;
    }
    
    public GlobalSightLocale getLocale() {
        return locale;
    }

    protected String getParsedData() {
        if (parsedData == null) {
            try
            {
                // Use the SegmentTmTuv handler, rather than the
                // AbstractTmTuv (default) version
                SegmentTmExactMatchFormatHandler handler =
                    new SegmentTmExactMatchFormatHandler();
                DiplomatBasicParser diplomatParser =
                    new DiplomatBasicParser(handler);
                diplomatParser.parse(getData());
                parsedData = handler.toString();
            }
            catch (Exception ex)
            {
                throw new LingManagerException(ex);
            }
        }
        return parsedData;
    }
    
    @Override
    public long getFingerprint() {
        if (fingerprint == null) {
            fingerprint = Fingerprint.fromString(getParsedData());
        }
        return fingerprint;
    }

    @Override
    public String getSerializedForm() {
        return data;
    }

    /**
     * Return the token hashes that are used for trigramming.
     */
    @Override
    public Iterable<Long> tokenize() {
        List<Long> fps = new ArrayList<Long>();
        for (String tok : getTokens()) {
            fps.add(Fingerprint.fromString(tok));
        }
        return fps;
    }

    /**
     * Slightly unusual equals() implementation, because a string
     * compare on the underlying XML can give false *negatives* because
     * of the presence of optional attributes in the XML markup.  To do
     * a real comparison, we need to compare the parsed content, just
     * like we do for fingerprint comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof GSTuvData)) {
            return false;
        }
        return getParsedData().equals(((GSTuvData)o).getParsedData()) &&
               getLocale().equals(((GSTuvData)o).getLocale());
    }
 
    @Override
    public String toString() {
        return "[" + getData() + "]"; 
    }

    // Cache the tokens, but use a soft reference so they'll
    // get GC'd if we need to
    private SoftReference<List<String>> tokenCache;
    private SoftReference<List<String>> tokenCacheNoStopWord;
    
    /**
     * Perform locale-sensitive tokenization on the tuv data.
     * This logic is based on code from the tm2 SegmentForFuzzyMatching 
     * and FuzzyMatcher classes.
     */
    public List<String> getTokens() {
        try {
            if (tokenCache != null) {
                List<String> l = tokenCache.get();
                if (l != null) {
                    return l;
                }
            }
            List<String> l =  LuceneUtil.createTm3Tokens(normalizeTuvData(), 
                                                         locale);
            tokenCache = new SoftReference<List<String>>(l);
            return l;
        } 
        catch (Exception e) {
            throw new TM3Exception(e);
        }
    }

    /**
	 * Get tokens without regard to stop word file.
	 * 
	 * @return List<String>
	 */
    public List<String> getTokensNoStopWord() {
        try {
            if (tokenCacheNoStopWord != null) {
                List<String> l = tokenCacheNoStopWord.get();
                if (l != null) {
                    return l;
                }
            }
			List<String> l = LuceneUtil.createTm3TokensNoStopWord(
					normalizeTuvData(), locale);
            tokenCacheNoStopWord = new SoftReference<List<String>>(l);
            return l;
        }
        catch (Exception e) {
            throw new TM3Exception(e);
        }
    }

    // Normalize the content -- this comes from AbstractTmTuv.getFuzzyIndexFormat()
    public String normalizeTuvData() throws DiplomatBasicParserException {
        FuzzyIndexFormatHandler handler = new FuzzyIndexFormatHandler();
        DiplomatBasicParser diplomatParser = new DiplomatBasicParser(handler);

        diplomatParser.parse(getData());

        // add spaces at the beginning and the end of the string
        String fuzzyIndexFormat = " " + handler.toString() + " ";
        // normalize white space
        fuzzyIndexFormat = Text.normalizeWhiteSpaceForTm(fuzzyIndexFormat);
        // down case the string
        return fuzzyIndexFormat.toLowerCase(locale.getLocale());
    }

	public BaseTmTuv getSrcTuv() {
		return srcTuv;
	}

	public void setSrcTuv(BaseTmTuv srcTuv) {
		this.srcTuv = srcTuv;
	}
}
