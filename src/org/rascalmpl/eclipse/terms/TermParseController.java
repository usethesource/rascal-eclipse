/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Anya Helene Bagge - A.H.S.Bagge@cwi.nl (Univ. Bergen)
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.terms;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.NodeLocator;
import org.rascalmpl.eclipse.editor.Token;
import org.rascalmpl.eclipse.editor.TokenIterator;
import org.rascalmpl.eclipse.nature.IWarningHandler;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMessageHandler;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.values.ValueFactoryFactory;

public class TermParseController implements IParseController {
	private ISourceProject project;
	private IConstructor parseTree;
	private IPath path;
	private Language language;
	private IDocument document;
	private ParseJob job;
	private final static IValueFactory VF = ValueFactoryFactory.getValueFactory(); 
	private final static AnnotatorExecutor executor = new AnnotatorExecutor();
	
	public Object getCurrentAst(){
		return parseTree;
	}
	
	public void setCurrentAst(IConstructor parseTree) {
		this.parseTree = parseTree;
	}
	
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return null;
	}

	public Language getLanguage() {
		return language;
	}

	public IPath getPath() {
		return path;
	}

	public ISourceProject getProject() {
		return project;
	}

	public ISourcePositionLocator getSourcePositionLocator() {
		return new NodeLocator();
	}

	public ILanguageSyntaxProperties getSyntaxProperties() {
		return null;
	}

	public Iterator<Token> getTokenIterator(IRegion region) {
		return new TokenIterator(true, parseTree);
	}

	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		Assert.isTrue(filePath.isAbsolute() && project == null
				|| !filePath.isAbsolute() && project != null);

		this.path = filePath;
		this.project = project;

		TermLanguageRegistry reg = TermLanguageRegistry.getInstance();
		this.language = reg.getLanguage(path.getFileExtension());

		ISourceLocation location = null;

		if (project != null) {
			location = ProjectURIResolver.constructProjectURI(project, path);
		} else {
			location = FileURIResolver.constructFileURI(path.toOSString());
		}

		this.job = new ParseJob(language.getName() + " parser", location, handler);
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
		private final IWarningHandler warnings;
		private final ISourceLocation loc;
		
		private String input;
		public IConstructor parseTree = null;

		public ParseJob(String name, ISourceLocation loc, IMessageHandler handler) {
			super(name);
			
			this.loc = loc;
			this.handler = handler;
			this.warnings = new WarningsToMessageHandler(loc, handler);
		}
		
		public void initialize(String input) {
			this.input = input;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			RascalMonitor rm = new RascalMonitor(monitor, warnings);
			rm.startJob("Parsing Term", 105);
			
			try{
				handler.clearMessages();
				TypeFactory TF = TypeFactory.getInstance();
				ICallableValue parser = getParser();
				if (parser != null) {
					synchronized (parser.getEval()) {
						parseTree = (IConstructor) parser.call(rm, new Type[] {TF.stringType(), TF.sourceLocationType()}, new IValue[] { VF.string(input), loc}, null).getValue();
					}
					ICallableValue annotator = getAnnotator();
					if (parseTree != null && annotator != null) {
						rm.event("annotating", 5);
						IConstructor newTree = executor.annotate(annotator, parseTree, handler);
						if (newTree != null) {
							parseTree = newTree;
						}
					}
				}
			}
			catch (ParseError pe){
				int offset = pe.getOffset();
				if(offset == input.length()) --offset;
				
				handler.handleSimpleMessage("parse error", offset, offset + pe.getLength(), pe.getBeginColumn(), pe.getEndColumn(), pe.getBeginLine() + 1, pe.getEndLine() + 1);
			} 
			catch (Throw e) {
				IValue exc = e.getException();
				
				if (exc.getType() == RuntimeExceptionFactory.Exception) {
					if (((IConstructor) exc).getConstructorType() == RuntimeExceptionFactory.ParseError) {
						ISourceLocation loc = (ISourceLocation) ((IConstructor) e.getException()).get(0);
						handler.handleSimpleMessage("parse error: " + loc, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
					}
					else {
						Activator.getInstance().logException(e.getMessage(), e);
					}
				}
			}
			catch (FactTypeUseException ftuex) {
				Activator.getInstance().logException("parsing " + language.getName() + " failed", ftuex);
			}
			catch (NullPointerException npex){
				Activator.getInstance().logException("parsing " + language.getName() + " failed", npex);
			} 
			catch (Throwable e) {
				Activator.getInstance().logException("parsing " + language.getName() + " failed: " + e.getMessage(), e);
			}
			finally {
				rm.endJob(true);
			}
			
			return Status.OK_STATUS;
		}

		private ICallableValue getAnnotator() {
			return TermLanguageRegistry.getInstance().getAnnotator(language);
		}

		private ICallableValue getParser() {
			return TermLanguageRegistry.getInstance().getParser(language);
		}
	}
	
	public synchronized Object parse(String input, IProgressMonitor monitor){
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
}
