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
import org.rascalmpl.value.IAnnotatable;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IWithKeywordParameters;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.type.TypeFactory;
import org.rascalmpl.values.uptr.RascalValueFactory;

import io.usethesource.impulse.editor.hover.ProblemLocation;
import io.usethesource.impulse.services.IQuickFixAssistant;
import io.usethesource.impulse.services.IQuickFixInvocationContext;
import io.usethesource.impulse.utils.NullMessageHandler;

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
		IAnnotatable<? extends IConstructor> annotatedAst = ast.asAnnotatable();
		if (!annotatedAst.hasAnnotation(ANNOTATION_MESSAGES)) {
			return;
		}
		
		ISet annotations = (ISet) annotatedAst.getAnnotation(ANNOTATION_MESSAGES);
		
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
	
	@Override
	public void addProposals(IQuickFixInvocationContext context,
			ProblemLocation problem, Collection<ICompletionProposal> proposals) {
		
		IConstructor ast = (IConstructor)context.getModel().getAST(new NullMessageHandler(), new NullProgressMonitor());

		constructProposals(ast, problem.getOffset(), proposals);
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
