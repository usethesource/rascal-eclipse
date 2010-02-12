package org.rascalmpl.eclipse.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

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
import org.eclipse.imp.pdb.facts.io.PBFReader;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ProjectURIResolver;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.errors.SummaryAdapter;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ParsetreeAdapter;

import sglr.SGLRInvoker;

public class SDFParseController implements IParseController{
	private static String parseTable = null;
	
	private IMessageHandler handler;
	private ISourceProject project;
	private IConstructor parseTree;
	private IPath path;
	
	public static void setParseTable(String parseTable){
		SDFParseController.parseTable = parseTable;
	}
	
	public IAnnotationTypeInfo getAnnotationTypeInfo(){
		return null;
	}

	public Object getCurrentAst(){
		return parseTree;
	}

	public Language getLanguage(){
		return null;
	}

	public ISourcePositionLocator getSourcePositionLocator(){
		return new NodeLocator();
	}

	public IPath getPath(){
		return path;
	}

	public ISourceProject getProject(){
		return project;
	}

	public ILanguageSyntaxProperties getSyntaxProperties(){
		return new RascalSyntaxProperties();
	}
	
	public Iterator<Token> getTokenIterator(IRegion region){
		return new TokenIterator(parseTree);
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler){
		this.path = filePath;
		this.handler = handler;
		this.project = project;
	}

	public Object parse(String moduleString, IProgressMonitor monitor){
		parseTree = null;
	
		try{
			handler.clearMessages();
			monitor.beginTask("parsing SDF", 1);
			
			URI location = ProjectURIResolver.constructProjectURI(project, path);
			
			byte[] result = SGLRInvoker.getInstance().parseFromString(moduleString, parseTable);
			IConstructor tree = bytesToParseTree(location, result);
			
			if(tree.getConstructorType() == Factory.ParseTree_Summary){
				ISourceLocation range = new SummaryAdapter(tree).getInitialSubject().getLocation();
				handler.handleSimpleMessage("parse error: " + range, range.getOffset(), range.getOffset() + range.getLength(), range.getBeginColumn(), range.getEndColumn(), range.getBeginLine(), range.getEndLine());
			}else{
				parseTree = tree;
			}
			
			monitor.worked(1);
			return tree;
		}catch(FactTypeUseException ftuex){
			Activator.getInstance().logException("parsing SDF failed", ftuex);
		}catch(IOException ioex){
			Activator.getInstance().logException("parsing SDF failed", ioex);
		}catch(NullPointerException npex){
			Activator.getInstance().logException("parsing SDF failed", npex);
		}catch(SyntaxError se){
			ISourceLocation loc = se.getLocation();
			handler.handleSimpleMessage("parse error: " + loc, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
		}finally{
			monitor.done();
		}
		
		return null;
	}

	private IConstructor bytesToParseTree(URI location, byte[] result) throws IOException{
		PBFReader reader = new PBFReader();
		ByteArrayInputStream bais = new ByteArrayInputStream(result);
		IConstructor tree = (IConstructor) reader.read(ValueFactoryFactory.getValueFactory(), Factory.getStore(),Factory.ParseTree, bais);
		return ParsetreeAdapter.addPositionInformation(tree, location);
	}
}
