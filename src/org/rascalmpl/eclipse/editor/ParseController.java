package org.rascalmpl.eclipse.editor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

public class ParseController implements IParseController {
	private final PrintWriter out = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
	private final GlobalEnvironment heap = new GlobalEnvironment();
	private final Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), out, out, new ModuleEnvironment("***parser***", heap), heap);
	private IMessageHandler handler;
	private ISourceProject project;
	private IConstructor parseTree;
	private IConstructor lastParseTree = null;
	private byte[] lastParsedInput = null;
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
		return parseTree != null ? new TokenIterator(parseTree) : null;
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		this.path = filePath;
		this.handler = handler;
		this.project = project;
		
		if (project != null) {
			try{
				parser.addRascalSearchPath(new URI("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
			}catch(URISyntaxException usex){
				throw new RuntimeException(usex);
			}
		}
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		URIResolverRegistry resolverRegistry = parser.getResolverRegistry();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		parser.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		parser.addClassLoader(getClass().getClassLoader());
		
		BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
		resolverRegistry.registerInput(bundleResolver);
		resolverRegistry.registerOutput(bundleResolver);

		try {
			String rascalPlugin = FileLocator.resolve(Platform.getBundle("rascal").getEntry("/")).getPath();
			String PDBValuesPlugin = FileLocator.resolve(Platform.getBundle("org.eclipse.imp.pdb.values").getEntry("/")).getPath();
			Configuration.setRascalJavaClassPathProperty(rascalPlugin + File.pathSeparator + PDBValuesPlugin + File.pathSeparator + rascalPlugin + File.separator + "src" + File.pathSeparator + rascalPlugin + File.separator + "bin" + File.pathSeparator + PDBValuesPlugin + File.separator + "bin");
		} catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}
	}

	public Object parse(String input, IProgressMonitor monitor) {
		parseTree = null;
		
		try{
			handler.clearMessages();
			monitor.beginTask("parsing Rascal", 1);
			
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
				parseTree = parser.parseModule(input.toCharArray(), uri, null);
				lastParseTree = parseTree;
			}
			monitor.worked(1);
			return parseTree;
		}
		catch (FactTypeUseException e){
			Activator.getInstance().logException("parsing rascal failed", e);
		}
		catch (SyntaxError e){
			ISourceLocation loc = e.getLocation();
//			e.printStackTrace();
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
