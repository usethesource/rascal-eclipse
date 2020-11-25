/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Mike Bierlee - mike.bierlee@lostmoment.com
 *   * Tijs van der Storm - storm@cwi.nl
*******************************************************************************/

package org.rascalmpl.eclipse.terms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.eclipse.editor.proposer.Prefix;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.functions.IFunction;

import io.usethesource.impulse.editor.ErrorProposal;
import io.usethesource.impulse.editor.SourceProposal;
import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.IContentProposer;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class TermContentProposer implements IContentProposer {	
	private IConstructor cachedTree = null;
	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	
	@Override
	public ICompletionProposal[] getContentProposals(IParseController parseController, int requestOffset, ITextViewer textViewer) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Point selection = textViewer.getSelectedRange();

		Language lang = parseController.getLanguage();
		ISet proposerContributions = TermLanguageRegistry.getInstance().getContentProposer(lang);
		IFunction proposer = null;
		String prefixIdCharacters = "";
		if (proposerContributions.size() > 0) {
			IConstructor cons = (IConstructor) proposerContributions.iterator().next();
			proposer = (IFunction) cons.get("proposer");
			prefixIdCharacters = ((IString) cons.get("legalPrefixChars")).getValue();
		}
		
		IConstructor tree = (IConstructor) parseController.getCurrentAst();
		if (tree == null) {
			tree = cachedTree;
		} else {
			cachedTree = tree;
		}

		Prefix prefix = Prefix.getPrefix(parseController.getDocument(), selection.x, selection.y, prefixIdCharacters);
		IString _prefixText = VF.string(prefix.getText());
		IInteger _requestOffset = VF.integer(requestOffset) ;

		if (proposer != null && tree != null) {
			IList resultProposals = (IList) proposer.call(tree, _prefixText, _requestOffset);
			
			for (IValue proposal : resultProposals) {
				IConstructor propCons = (IConstructor) proposal;
				if (propCons.getName().equals("sourceProposal")) {
					IString newText = (IString) propCons.get("newText");
					IString proposalText = propCons.has("proposal") ? (IString) propCons.get("proposal") : newText;
					proposals.add(new SourceProposal(proposalText.getValue(), newText.getValue(), prefix.getText(), prefix.getOffset()));
				} else if (propCons.getName().equals("errorProposal")) {
					IString errorText = (IString) propCons.get("errorText");
					proposals.add(new ErrorProposal(errorText.getValue(), requestOffset));
				}
			}

			if (proposals.size() == 0) {
				proposals.add(new ErrorProposal("No proposals available.", requestOffset));
			}
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
}
