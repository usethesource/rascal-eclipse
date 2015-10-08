package org.rascalmpl.eclipse.terms;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.parser.IModelListener;
import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.IEditorService;

/*
 * NB: this might end up in an infinite loop when the function used to compute patches
 * does not reach a fixpoint.
 */
public class EditorUpdater implements IModelListener, IEditorService {
	public EditorUpdater() {
	}

//	@Override
//	public AnalysisRequired getAnalysisRequired() {
//		return AnalysisRequired.SYNTACTIC_ANALYSIS;
//	}
	
	class Job extends UIJob {

		private IParseController parseController;

		public Job(IParseController parseController) {
			super("updating editor");
			this.parseController = parseController;
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IDocument doc = parseController.getDocument();
			IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWindow != null) {
				IWorkbenchPage activePage = activeWindow.getActivePage();
				if (activePage != null) {
					IEditorPart activeEditor = activePage.getActiveEditor();
					if (activeEditor != null && activeEditor instanceof UniversalEditor) {
						
						ICallableValue func = TermLanguageRegistry.getInstance().getLiveUpdater(parseController.getLanguage());
						if (func == null) {
							return Status.CANCEL_STATUS;
						}
						IConstructor pt = (IConstructor) parseController.getCurrentAst();
						if (pt == null) {
							return Status.CANCEL_STATUS;
						}
						Type type = RascalTypeFactory.getInstance().nonTerminalType(pt);
						IList patch = null;
						synchronized (func.getEval()) {
							func.getEval().__setInterrupt(false);
							patch = (IList) func.call(new Type[] {type}, new IValue[] {pt}, null).getValue();
						}

						if (patch.isEmpty()) {
							return Status.OK_STATUS;
						}
						
				        DocumentRewriteSession session = ((IDocumentExtension4)doc).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
				        try {
				        	int offset = 0;
					        for (IValue v: patch) {
					        	ITuple subst = (ITuple)v;
					        	ISourceLocation loc = (ISourceLocation) subst.get(0);
					        	IString txt = (IString) subst.get(1);
					        	doc.replace(loc.getOffset() + offset, loc.getLength(), txt.getValue());
					        	offset += txt.length() - loc.getLength(); 
					        }
				        } catch (UnsupportedOperationException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (BadLocationException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
				        finally {
				        	((IDocumentExtension4)doc).stopRewriteSession(session);
				        }				        
					}
				}
			}
			return Status.OK_STATUS;
		}
		
	}

	@Override
	public void update(IParseController parseController, IProgressMonitor monitor) {
		try {
			Job job = new Job(parseController);
			job.schedule();
			job.join();
		} catch (InterruptedException e) {
			Activator.getInstance().logException("live updater interrupted", e);
		}
	}



	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setEditor(UniversalEditor arg0) {
		// TODO Auto-generated method stub
		
	}

}
