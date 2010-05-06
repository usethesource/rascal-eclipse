package org.rascalmpl.eclipse.box;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

public class SubScanner extends RuleBasedScanner {
	
	SubScanner(TextAttribute t) {
		 IToken token = new Token(t);
//		 IRule[] rules = new IRule[1];
//		 rules[0] = new MultiLineRule("\b{", "\b}12", token);
//		 setRules(rules);
		 this.setDefaultReturnToken(token);
	}

}
