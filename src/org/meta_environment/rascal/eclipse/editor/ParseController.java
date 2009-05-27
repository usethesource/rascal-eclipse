package org.meta_environment.rascal.eclipse.editor;

import java.io.IOException;
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
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.meta_environment.errors.SummaryAdapter;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.interpreter.load.ModuleLoader;
import org.meta_environment.rascal.interpreter.staticErrors.SyntaxError;
import org.meta_environment.uptr.Factory;
import org.meta_environment.uptr.ParsetreeAdapter;

public class ParseController implements IParseController {
	private ModuleLoader loader = new ModuleLoader();
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

	public void initialize(IPath filePath, ISourceProject project,
			IMessageHandler handler) {
		this.path = filePath;
		this.handler = handler;
		this.project = project;
	}

	public Object parse(String input, IProgressMonitor monitor) {
		try {
			handler.clearMessages();
			monitor.beginTask("parsing Rascal", 1);
			IConstructor parseTree = loader.parseModule(path.toOSString(), "-", input);
			
			if (parseTree.getConstructorType() == Factory.ParseTree_Summary) {
				ISourceLocation range = new SummaryAdapter(parseTree).getInitialSubject().getLocation();
				handler.handleSimpleMessage("parse error: " + range, range.getOffset(), range.getOffset() + range.getLength(), range.getBeginColumn(), range.getEndColumn(), range.getBeginLine(), range.getEndLine());
				parseTree = null;
			}
			else {
				parseTree = new ParsetreeAdapter(parseTree).addPositionInformation(path.toFile().getAbsolutePath());
				this.parseTree = parseTree;
			}
			monitor.worked(1);
			return parseTree;
		} catch (FactTypeUseException e) {
			Activator.getInstance().logException("parsing rascal failed", e);
			monitor.done();
		} catch (IOException e) {
			Activator.getInstance().logException("parsing rascal failed", e);
		} catch (SyntaxError e) {
			ISourceLocation loc = e.getLocation();
			handler.handleSimpleMessage("parse error: " + loc, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
			parseTree = null;
		}
		finally {
			monitor.done();
		}
		
		return null;
	}
}
