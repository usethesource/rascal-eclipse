package org.rascalmpl.eclipse.editor;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ProjectModuleLoader;
import org.rascalmpl.eclipse.console.ProjectSDFModuleContributor;
import org.rascalmpl.eclipse.console.ProjectURIResolver;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.FromCurrentWorkingDirectoryLoader;
import org.rascalmpl.interpreter.load.FromResourceLoader;
import org.rascalmpl.interpreter.load.ISdfSearchPathContributor;
import org.rascalmpl.interpreter.load.ModuleLoader;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.errors.SummaryAdapter;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class ParseController implements IParseController {
	private final ModuleLoader loader = new ModuleLoader();
	private final StaticChecker checker = StaticChecker.getInstance();
	
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
			loader.addFileLoader(new ProjectModuleLoader(project.getRawProject()));
		}
		loader.addFileLoader(new FromResourceLoader(RascalScriptInterpreter.class, "org/rascalmpl/eclipse/lib"));
		
		if (project != null) {
			loader.addSdfSearchPathContributor(new ProjectSDFModuleContributor(project.getRawProject()));
		}
		
		loader.addFileLoader(new FromCurrentWorkingDirectoryLoader());
		
		// everything rooted at the src directory 
		loader.addFileLoader(new FromResourceLoader(this.getClass()));

		// add current wd and sdf-library to search path for SDF modules
		loader.addSdfSearchPathContributor(new ISdfSearchPathContributor() {
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
			IConstructor result = loader.parseModule(uri, input, new ModuleEnvironment("***editor***"));
			
			if(result.getConstructorType() == Factory.ParseTree_Summary){
				ISourceLocation location = new SummaryAdapter(result).getInitialSubject().getLocation();
				handler.handleSimpleMessage("parse error: " + location, location.getOffset(), location.getOffset() + location.getLength(), location.getBeginColumn(), location.getEndColumn(), location.getBeginLine(), location.getEndLine());
			}else{
				parseTree = ParsetreeAdapter.addPositionInformation(result, uri);
//				try {
//					IConstructor newTree = checker.checkModule((IConstructor) TreeAdapter.getArgs(ParsetreeAdapter.getTop(parseTree)).get(1));
//					if (newTree != null) {
//						IValueFactory vf = ValueFactoryFactory.getValueFactory();
//						parseTree = (IConstructor) Factory.ParseTree_Top.make(vf, newTree, vf.integer(0)); 
//					}
//					else {
//						Activator.getInstance().logException("static checker returned null", new RuntimeException());
//					}
//				}
//				catch (Throwable e) {
//					Activator.getInstance().logException("static checker failed", e);
//				}
			}
			
			monitor.worked(1);
			return result;
		}
		catch(FactTypeUseException e){
			Activator.getInstance().logException("parsing rascal failed", e);
		}
		catch(IOException e){
			Activator.getInstance().logException("parsing rascal failed", e);
		}
		catch(SyntaxError e){
			ISourceLocation loc = e.getLocation();
			handler.handleSimpleMessage(e.getMessage(), loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
		}
		finally{
			monitor.done();
		}
		
		return null;
	}
}
