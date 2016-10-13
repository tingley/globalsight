/*
 * Created on Jun 24, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.getopt.luke;

import org.apache.lucene.index.Term;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TermInfo {
    TermInfo(Term t, int df) {
        term = t;
        docFreq = df;
    }
    int docFreq;
    Term term;
}
