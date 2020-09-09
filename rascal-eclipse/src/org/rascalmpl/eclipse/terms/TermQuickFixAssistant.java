package org.rascalmpl.eclipse.terms;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.values.RascalValueFactory;

import io.usethesource.impulse.editor.hover.ProblemLocation;
import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.services.IQuickFixAssistant;
import io.usethesource.impulse.services.IQuickFixInvocationContext;
import io.usethesource.impulse.utils.NullMessageHandler;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IWithKeywordParameters;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;

public class TermQuickFixAssistant implements IQuickFixAssistant {
	
	private static final String KEYWORD_QUICKFIX = "quickFixes";
	private static final String ANNOTATION_MESSAGES = "messages";

	@Override
	public boolean canFix(Annotation annotation) {
		return true;
	}

	@Override
	public boolean canAssist(IQuickFixInvocationContext invocationContext) {
		return true ;
	}

	@Override
	public String[] getSupportedMarkerTypes() {
		return new String[] { IMarker.PROBLEM };
	}

	private void constructProposals(IConstructor ast, int problemOffset, Collection<ICompletionProposal> proposals) {
		IWithKeywordParameters<? extends IConstructor> annotatedAst = ast.asWithKeywordParameters();
		if (!annotatedAst.hasParameter(ANNOTATION_MESSAGES)) {
			return;
		}
		
		ISet annotations = (ISet) annotatedAst.getParameter(ANNOTATION_MESSAGES);
		
		for (IValue annotation : annotations) {
			IConstructor cons = (IConstructor) annotation;
			ISourceLocation fixLoc = (ISourceLocation)cons.get("at");
			
			if (isProblemLocInQuickFixLoc(problemOffset, fixLoc)) {
				IWithKeywordParameters<? extends IConstructor> withKeywords = cons.asWithKeywordParameters();
			
				if (withKeywords.hasParameter(KEYWORD_QUICKFIX)) {
					IList quickFixes = (IList) withKeywords.getParameter(KEYWORD_QUICKFIX);
					
					for (IValue qf: quickFixes) {
						ITuple quickFix = (ITuple) qf;
						String label = ((IString)quickFix.get(0)).getValue();
						ICallableValue function = ((ICallableValue) quickFix.get(1));
						
						proposals.add(makeProposal(ast, fixLoc, label, function));
					}
				}
			}
		}
	}
	
	private boolean isProblemLocInQuickFixLoc(int problemOffset, ISourceLocation quickFixLoc) {
		return 	quickFixLoc.getOffset() == problemOffset;
	}
	
	private boolean languageHasQuickFixes(IQuickFixInvocationContext context) {
		TermLanguageRegistry tm = TermLanguageRegistry.getInstance();
		Language lang = tm.getLanguage(context.getModel().getFile().getFileExtension());
		if (lang != null) {
			return tm.getHasQuickFixes(lang).getValue();
		}
		return false;
	}
	
	@Override
	public void addProposals(IQuickFixInvocationContext context,
			ProblemLocation problem, Collection<ICompletionProposal> proposals) {
		if (languageHasQuickFixes(context)) {
            IConstructor ast = (IConstructor)context.getModel().getAST(new NullMessageHandler(), new NullProgressMonitor());

            constructProposals(ast, problem.getOffset(), proposals);
		}
	}

	private ICompletionProposal makeProposal(final IConstructor ast, final ISourceLocation loc, final String label,
			final ICallableValue f) {
		return new ICompletionProposal() {
			
			@Override
			public Point getSelection(IDocument document) {
				return new Point(loc.getOffset() + loc.getLength(), 0);
			}
			
			@Override
			public Image getImage() {
				return null;
			}
			
			@Override
			public String getDisplayString() {
				return label;
			}
			
			@Override
			public IContextInformation getContextInformation() {
				return null;
			}
			
			@Override
			public String getAdditionalProposalInfo() {
				return null;
			}
			
			@Override
			public void apply(IDocument document) {
				Type[] argTypes = new Type[] { RascalValueFactory.Tree, TypeFactory.getInstance().sourceLocationType() };
				IString newSrc = (IString)f.call(argTypes, new IValue[] {ast, loc},  Collections.<String,IValue>emptyMap()).getValue();
				document.set(newSrc.getValue());
			}
		};
	}

}
