package org.rascalmpl.eclipse.box;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


public class BoxPartitionScanner extends RuleBasedPartitionScanner {
    public final static String IT = "it";
    public final static String BF = "bf";
    public final static String NM = "nm";
    public final static String DF = "df";
    
    
    
    public BoxPartitionScanner() {
    	IToken it = new Token(IT);
        IToken bf = new Token(BF);
        IToken nm = new Token(NM);
        IToken df = new Token(DF);
        
        IPredicateRule[] rules = new IPredicateRule[3];
       
        rules[0] = new MultiLineRule("\r{it", "\r}12", it);
        rules[1] = new MultiLineRule("\r{nm", "\r}12", nm);
        rules[2] = new MultiLineRule("\r{bf", "\r}12", bf);
        rules[3] = new MultiLineRule("\r{df", "\r}12", df);
    }
}
