package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ProjectSDFModuleContributor;
import org.rascalmpl.eclipse.console.ProjectURIResolver;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.ISdfSearchPathContributor;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.uri.ClassResourceInputStreamResolver;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

public class ParseController implements IParseController {
	private Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), new PrintWriter(System.err), new PrintWriter(System.out), new ModuleEnvironment("***parser***"), new GlobalEnvironment());
	private IMessageHandler handler;
	private ISourceProject project;
	private IConstructor parseTree;
	private IPath path;
	
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return null;
	}

	public Object getCurrentAst() {
		return parseTree;
	}
	
    public void setCurrentAst(IConstructor parseTree) {
		this.parseTree = parseTree;
	}

	public Language getLanguage() {
		return null;
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
		return new TokenIterator(parseTree);
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		this.path = filePath;
		this.handler = handler;
		this.project = project;
		
		if (project != null) {
			parser.addRascalSearchPath(URI.create("project://" + project.getName()));
		}
		
		IURIInputStreamResolver library = new ClassResourceInputStreamResolver("rascal-eclipse-library", RascalScriptInterpreter.class);
		URIResolverRegistry.getInstance().registerInput(library.scheme(), library);
		
		parser.addRascalSearchPath(URI.create("rascal-eclipse-library:///org/rascalmpl/eclipse/lib"));
		
		if (project != null) {
			parser.addSdfSearchPathContributor(new ProjectSDFModuleContributor(project.getRawProject()));
		}
		
		// add current wd and sdf-library to search path for SDF modules
		parser.addSdfSearchPathContributor(new ISdfSearchPathContributor() {
			public java.util.List<String> contributePaths() {
				java.util.List<String> result = new LinkedList<String>();
				result.add(System.getProperty("user.dir"));
				result.add(Configuration.getSdfLibraryPathProperty());
				return result;
			}
		});
	}

	public Object parse(String input, IProgressMonitor monitor) {
		parseTree = null;
		
		try{
			handler.clearMessages();
			monitor.beginTask("parsing Rascal", 1);
			
			URI uri = ProjectURIResolver.constructProjectURI(project, path);
			parseTree = parser.parseModule(uri, new ModuleEnvironment("***editor***"));
			monitor.worked(1);
			return parseTree;
		}
		catch(FactTypeUseException e){
			Activator.getInstance().logException("parsing rascal failed", e);
		}
		catch(IOException e){
			Activator.getInstance().logException("parsing rascal failed", e);
		}
		catch(SyntaxError e){
			ISourceLocation loc = e.getLocation();
			e.printStackTrace();
			if (loc.getOffset() >= 0) {
				handler.handleSimpleMessage(e.getMessage(), loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
			}
			else {
				handler.handleSimpleMessage(e.getMessage(), 0, 0, 0, 0, 1, 1);
			}
		}
		finally{
			monitor.done();
		}
		
		return null;
	}

	
}
