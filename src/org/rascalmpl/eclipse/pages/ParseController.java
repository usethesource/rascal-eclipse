/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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
package org.rascalmpl.eclipse.pages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.ambidexter.ReportView;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMessageHandler;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.parser.gtd.exception.ParseError;

import io.usethesource.impulse.parser.IMessageHandler;

public class ParseController extends org.rascalmpl.eclipse.editor.ParseController {
	
	Evaluator getEvaluator() {
		return parser;
	}
	
	private class ParseJob extends org.rascalmpl.eclipse.editor.ParseController.ParseJob {
		public ParseJob(String name, ISourceLocation uri, IMessageHandler handler) {
			super(name, uri, handler);
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
				synchronized (parser) {
					if (project != null) {
						// if this is a source file in a Rascal project then
						// reload other modules to find out about new syntax definitions
						ProjectEvaluatorFactory.getInstance().reloadProject(project.getRawProject(), new WarningsToMessageHandler(uri, getMessageHandler()), ignore);
					}
					parseTree = parser.parseCommands(rm, input, uri);
				}
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
				setParseError(offset, pe.getLength(), pe.getBeginLine() + 1, pe.getBeginColumn(), pe.getEndLine() + 1, pe.getEndColumn(), msg.toString());
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
				ReportView.listAmbiguities(project.getName(), "editor", e.getTree(), new NullProgressMonitor());
				Activator.log("unexpected ambiguity during parsing of Rascal module", e);
			}
			finally {
				rm.endJob(true);
			}
			
			return Status.OK_STATUS;
		}

	}
	
	@Override
	protected void initParseJob(IMessageHandler handler, ISourceLocation location) {
		this.job = new ParseJob("Rascal parser", location, handler);
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
	
}
