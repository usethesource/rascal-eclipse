package org.rascalmpl.eclipse.editor;

import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class ParseController implements IParseController, IMessageHandlerProvider {
	private IMessageHandler handler;
	private ISourceProject project;
	private IConstructor parseTree;
	private IConstructor lastParseTree = null;
	private byte[] lastParsedInput = null;
	private IPath path;
	private Language language;
	private IDocument document;
	private org.rascalmpl.eclipse.editor.ParseController.ParseJob job;
	
	private Evaluator getParser(IProject project) {
		return ProjectEvaluatorFactory.getInstance().getEvaluator(project);
	}
	
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return null;
	}

	public IMessageHandler getMessageHandler() {
		return handler;
	}
	
	public Object getCurrentAst() {
		return parseTree;
	}
	
    public void setCurrentAst(IConstructor parseTree) {
		this.parseTree = parseTree;
	}

	public Language getLanguage() {
		if (language == null) {
			language = LanguageRegistry.findLanguage("Rascal");
		}
		return language;
	}

	public ISourcePositionLocator getSourcePositionLocator() {
		return new NodeLocator();
	}

	public IPath getPath() {
		return path;
	}

	public ISourceProject getProject() {
		return project;
	}

	public ILanguageSyntaxProperties getSyntaxProperties() {
		return new RascalSyntaxProperties();
	}
	
	public Iterator<Token> getTokenIterator(IRegion region) {
		return parseTree != null ? new TokenIterator(parseTree) : null;
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		this.path = filePath;
		this.handler = handler;
		this.project = project;
		IValueFactory VF = ValueFactoryFactory.getValueFactory();
		this.job = new ParseJob("Rascal parser", VF.sourceLocation(ProjectURIResolver.constructProjectURI(project, path)), handler);
	}

	public IDocument getDocument() {
		return document;
	}
	
	public Object parse(IDocument doc, IProgressMonitor monitor) {
		if (doc == null) {
			return null;
		}
		this.document = doc;
		return parse(doc.get(), monitor);
	}
	
	private class ParseJob extends Job {
		private final IMessageHandler handler;
		public IConstructor parseTree = null;
		private String input;

		public ParseJob(String name, ISourceLocation loc, IMessageHandler handler) {
			super(name);
			this.handler = handler;
		}
		
		public void initialize(String input) {
			this.input = input;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			RascalMonitor rm = new RascalMonitor(monitor);
			rm.startJob("parsing", 135);
			parseTree = null;
			if (input == null || project == null || path == null) {
				// may happen when project is deleted before Eclipse was started
				return null;
			}
			
			try{
				handler.clearMessages();
				
				URI uri = ProjectURIResolver.constructProjectURI(project, path);

				// TODO: this may be a workaround for a bug that's not there anymore
				byte[] inputBytes = input.getBytes();
				boolean arraysMatch = true;
				if (lastParsedInput != null) { 
					if(inputBytes.length != lastParsedInput.length) {
						arraysMatch = false;
					} else {
						for (int n = 0; n < inputBytes.length; ++n)
							if (inputBytes[n] != lastParsedInput[n]) {
								arraysMatch = false;
								break;
							}
					}
				}
				
				if (lastParsedInput != null && arraysMatch) {
					parseTree = lastParseTree;
				} else {
					Evaluator parser = getParser(project.getRawProject());
					// TODO Re-enable the error tree thing when it's working.
					//parseTree = parser.parseModuleWithErrorTree(input.toCharArray(), uri, null);
					parseTree = parser.parseModule(rm, input.toCharArray(), uri, null);
					lastParseTree = parseTree;
					
					if(parseTree.getConstructorType() == Factory.Tree_Error){
						ISourceLocation parsedLocation = TreeAdapter.getLocation(parseTree);
						
						// Set the error location to where the error tree ends.
						setParseError(parsedLocation.getLength(), 0, parsedLocation.getEndLine(), parsedLocation.getEndColumn(), parsedLocation.getEndLine(), parsedLocation.getEndColumn(), "Parse error.");
					}
				}
			}catch (FactTypeUseException e){
				Activator.getInstance().logException("parsing rascal failed", e);
			}catch (SyntaxError e){
				ISourceLocation loc = e.getLocation();

				setParseError(loc.getOffset(), loc.getLength(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndLine(), loc.getEndColumn(), e.getMessage());
			}catch(Throw e){
				ISourceLocation loc = e.getLocation();
				
				setParseError(loc.getOffset(), loc.getLength(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndLine(), loc.getEndColumn(), e.getMessage());
			}
			finally {
				rm.endJob(true);
			}
			
			return Status.OK_STATUS;
		}
	}
	
	public Object parse(String input, IProgressMonitor monitor) {
		parseTree = null;
		try {
			job.initialize(input);
			job.schedule();
			job.join();
			parseTree = job.parseTree;
			return parseTree;
		} catch (InterruptedException e) {
			Activator.getInstance().logException("parser interrupted", e);
		}
		return null;
	}
	
	private void setParseError(int offset, int length, int beginLine, int beginColumn, int endLine, int endColumn, String message){
		if(offset >= 0){
			handler.handleSimpleMessage(message, offset, offset + length, beginColumn, endColumn, beginLine, endLine);
		}else{
			handler.handleSimpleMessage(message, 0, 0, 0, 0, 1, 1);
		}
	}
}
