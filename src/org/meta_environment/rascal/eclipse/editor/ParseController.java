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
import org.meta_environment.rascal.parser.Parser;
import org.meta_environment.uptr.Factory;
import org.meta_environment.uptr.ParsetreeAdapter;

public class ParseController implements IParseController {
	private Parser parser;
	private IMessageHandler handler;
	private ISourceProject project;
	private IConstructor parseTree;
	private IPath path;

	@Override
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return null;
	}

	@Override
	public Object getCurrentAst() {
		return parseTree;
	}

	@Override
	public Language getLanguage() {
		return null;
	}

	@Override
	public ISourcePositionLocator getNodeLocator() {
		return new NodeLocator();
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public ISourceProject getProject() {
		return project;
	}

	@Override
	public ILanguageSyntaxProperties getSyntaxProperties() {
		return new RascalSyntaxProperties();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator getTokenIterator(IRegion region) {
		return new TokenIterator(parseTree);
	}

	@Override
	public void initialize(IPath filePath, ISourceProject project,
			IMessageHandler handler) {
		this.path = filePath;
		this.parser = Parser.getInstance();
		this.handler = handler;
		this.project = project;
	}

	@Override
	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		try {
			monitor.beginTask("parsing Rascal", 1);
			IConstructor parseTree = parser.parseFromString(input, path.toOSString());
			
			if (parseTree.getConstructorType() == Factory.ParseTree_Summary) {
				ISourceLocation range = new SummaryAdapter(parseTree).getInitialSubject().getLocation();
				handler.handleSimpleMessage("parse error", range.getOffset(), range.getOffset() + range.getLength(), range.getBeginColumn(), range.getEndColumn(), range.getBeginLine(), range.getEndLine());
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
		}
		finally {
			monitor.done();
		}
		
		return null;
	}

}
