/*******************************************************************************
 * Copyright (c) 2009-2017 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Emilie Balland - (CWI)
 *   * Anya Helene Bagge - A.H.S.Bagge@cwi.nl (Univ. Bergen)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.nature.IWarningHandler;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.library.lang.rascal.syntax.RascalParser;
import org.rascalmpl.parser.Parser;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;
import org.rascalmpl.parser.gtd.result.out.DefaultNodeFlattener;
import org.rascalmpl.parser.uptr.UPTRNodeFactory;
import org.rascalmpl.parser.uptr.action.NoActionExecutor;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.language.LanguageRegistry;
import io.usethesource.impulse.model.ISourceProject;
import io.usethesource.impulse.parser.IMessageHandler;
import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.parser.ISourcePositionLocator;
import io.usethesource.impulse.services.IAnnotationTypeInfo;
import io.usethesource.impulse.services.ILanguageSyntaxProperties;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.exceptions.FactTypeUseException;

public class ParseController implements IParseController, IMessageHandlerProvider {
	protected IMessageHandler handler;
	protected ISourceProject project;
	protected IConstructor parseTree;
	protected ParseJob job;
	protected IPath path;
	protected Language language;
	protected IDocument document;
	protected Evaluator parser;
	protected IWarningHandler warnings;
    protected ISourceLocation sourceLocation;
	
	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return null;
	}

	public IMessageHandler getMessageHandler() {
		return handler;
	}
	
	public Object getCurrentAst() {
	    Activator.getInstance().writeInfoMsg(System.currentTimeMillis() + ": getting current AST for " + path);
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
	
	public Iterator<Object> getTokenIterator(IRegion region) {
		return parseTree != null ? new TokenIterator(false, parseTree) : null;
	}
	
	@Override
	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		Assert.isTrue(filePath.isAbsolute() && project == null
				|| !filePath.isAbsolute() && project != null);
		
		this.path = filePath;
		this.handler = handler;
		this.project = project;

		sourceLocation = recoverSourceLocation(filePath);
		
		initParseJob(handler, sourceLocation);
	}

	public ISourceLocation getSourceLocation() {
	    return sourceLocation;
	}

	private ISourceLocation recoverSourceLocation(IPath filePath) {
	    if (project != null && path != null) {
	        return ProjectURIResolver.constructProjectURI(project.getRawProject(), path);
	    }
	    else if (path != null && path.isUNC() && path.segment(0).equals("std")) {
	        return org.rascalmpl.uri.URIUtil.correctLocation("std", "", path.removeFirstSegments(1).toString());
	    }
	    else if (path != null && path.isUNC() && path.segment(0).equals("lib")) {
            return org.rascalmpl.uri.URIUtil.correctLocation("lib", path.segment(1), path.removeFirstSegments(2).toString());
        }
	    else if (path != null && path.isUNC()) {
	        return org.rascalmpl.uri.URIUtil.correctLocation(path.segment(0), path.segment(1), path.removeFirstSegments(2).toString());
	    }
	    else {
	        return null;
	    }
	}

	protected void initParseJob(IMessageHandler handler, ISourceLocation location) {
		this.job = new ParseJob("Rascal parser", location, handler);
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
	
	public class ParseJob extends Job {
		protected final ISourceLocation uri;
		private Set<IResource> markedFiles;

		protected String input;
		public ITree parseTree = null;

		public ParseJob(String name, ISourceLocation uri, IMessageHandler handler) {
			super(name);
			this.uri = uri;
		}
		
		public void initialize(String input) {
			this.input = input;
		}
		
		protected void clearMarkers() {
		    try {
		        if (markedFiles != null) {
		            for (IResource res : markedFiles) {
		                res.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, true, 0);
		            }

		            markedFiles = null;
		        }
		    } catch (CoreException e) {
		        Activator.log("could not erase markers completely", e);
		    }
		}
		
		private ITree parseModule(char[] input, ISourceLocation loc) {
	        IActionExecutor<ITree> actions = new NoActionExecutor();    
	        return new RascalParser().parse(Parser.START_MODULE, loc.getURI(), input, actions, new DefaultNodeFlattener<IConstructor, ITree, ISourceLocation>(), new UPTRNodeFactory(true));
	    }
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			RascalMonitor rm = new RascalMonitor(monitor, warnings);
			clearMarkers();
			rm.startJob("parsing", 500);
			parseTree = null;
			if (input == null || path == null || (path != null && !path.isAbsolute() && project == null)) {
				// may happen when project is deleted before Eclipse was started
				return null;
			}
			
			try {
			    parseTree = parseModule(input.toCharArray(), uri);
			}
			catch (FactTypeUseException ftue) {
				Activator.getInstance().logException("parsing rascal failed", ftue);
			}
			catch (ParseError pe){
				int offset = pe.getOffset();
				if(offset > 0 && offset == input.length()) {
					--offset;
				}
				int k = Math.min(offset + 20, input.length());
				String follow = input.substring(offset, k);
				StringBuffer msg = new StringBuffer();
				boolean hasUni = false;
				msg.append(pe.toString()).append(" FOLLOWED BY: ");
				for(int i = 0; i < follow.length();i ++){
					int c = follow.codePointAt(i);
					if((Character.isSpaceChar(c) &&
					   c != (int)' ' && c != (int)'\t' && c != (int)'\r' && c != (int)'\n')){
						
					   if(Character.charCount(c) == 1){
						   msg.append(String.format("\\u%04x", c));
					   } else {
						   msg.append(String.format("\\U%06x", c));
					   }
						hasUni = true;
					} else 
					   msg.appendCodePoint(c);
				}
				if(hasUni)
					msg.append(" NOTE: unrecognized characters occur at \\u followed by a hexadecimal number");
				setParseError(offset, pe.getLength(), pe.getBeginLine(), pe.getBeginColumn(), pe.getEndLine(), pe.getEndColumn(), msg.toString());
			}
			catch (StaticError e) {
				ISourceLocation loc = e.getLocation();
				
				if (loc.hasOffsetLength()) {
				  setParseError(loc.getOffset(), loc.getLength(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndLine(), loc.getEndColumn(), e.getMessage());
				}
				else {
				  Activator.log("weird error during parsing", e);
				}
			}
			catch (Throw t) {
				ISourceLocation loc = t.getLocation();
				
				setParseError(loc.getOffset(), loc.getLength(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndLine(), loc.getEndColumn(), t.getMessage());
			}
			catch (Ambiguous e) {
				ISourceLocation loc = e.getLocation();
				setParseError(loc.getOffset(), loc.getLength(), loc.getBeginLine(), loc.getBeginColumn(), loc.getEndLine(), loc.getEndColumn(), e.getMessage());
				// reparse with raw rascal parser to get the full forest
				Activator.log("unexpected ambiguity during parsing of Rascal module", e);
			}
			finally {
				rm.endJob(true);
			}
			
			return Status.OK_STATUS;
		}

	}
	
	@Override
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
	
	protected void setParseError(int offset, int length, int beginLine, int beginColumn, int endLine, int endColumn, String message){
		if(offset >= 0){
			handler.handleSimpleMessage(message, offset, offset + ((length == 0) ? 1 : length), beginColumn, endColumn, beginLine, endLine);
		}else{
			handler.handleSimpleMessage(message, 0, 0, 0, 0, 1, 1);
		}
	}

    public String getModuleName() {
        if (getCurrentAst() == null) {
            return null;
        }
        
        ITree top = TreeAdapter.getStartTop((ITree) getCurrentAst());
        ITree header = TreeAdapter.getArg(top, "header");
        ITree name = TreeAdapter.getArg(header, "name");

        return TreeAdapter.yield(name).replaceAll("\\\\","");
    }
}
