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

import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMessageHandler;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.library.lang.rascal.syntax.RascalParser;
import org.rascalmpl.parser.Parser;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.parser.gtd.result.out.DefaultNodeFlattener;
import org.rascalmpl.parser.uptr.UPTRNodeFactory;
import org.rascalmpl.parser.uptr.action.NoActionExecutor;
import org.rascalmpl.semantics.dynamic.Import;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

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
				ModuleEnvironment env;
				synchronized (parser) {
					if (project != null) {
						// if this is a source file in a Rascal project then
						// reload other modules to find out about new syntax definitions
						ProjectEvaluatorFactory.getInstance().reloadProject(project.getRawProject(), new WarningsToMessageHandler(uri, getMessageHandler()), Collections.emptySet());
					}

					parseTree = new RascalParser().parse(Parser.START_COMMANDS, uri.getURI(), input.toCharArray(), new NoActionExecutor() , new DefaultNodeFlattener<IConstructor, ITree, ISourceLocation>(), new UPTRNodeFactory(false));
					//parseTree = parser.parseCommands(rm, input, uri);
					
					GlobalEnvironment heap = parser.getHeap();
					env = new ModuleEnvironment("Scrapbook", heap);
					Environment oldEnv = parser.getCurrentEnvt();
					try {
						parser.setCurrentEnvt(env);
//						parser.event("defining syntax");
				        IValueFactory vf = parser.getValueFactory();
				        ISetWriter rulesWriter = vf.setWriter();  
				        ISetWriter importsWriter = vf.setWriter();  
				        ISetWriter extendsWriter = vf.setWriter();  
				        ISetWriter externalsWriter = vf.setWriter();  
				        
				        for (IValue v: TreeAdapter.getASTArgs(TreeAdapter.getArg(TreeAdapter.getArg(parseTree, "top"), "commands"))) {
				        	ITree cmd = (ITree) v;
				        	if (TreeAdapter.getConstructorName(cmd).equals("import")) {
				        		ITree imp = TreeAdapter.getArg(cmd, "imported");
				        		if (TreeAdapter.getConstructorName(imp).equals("syntax")) {
				        			rulesWriter.insert(imp);
				        		}
				        		else if (TreeAdapter.getConstructorName(imp).equals("default")) {
				        			importsWriter.insert(imp);
				        		}
				        		else if (TreeAdapter.getConstructorName(imp).equals("extend")) {
				        			extendsWriter.insert(imp);
				        		}
				        		else if (TreeAdapter.getConstructorName(imp).equals("external")) {
				        			externalsWriter.insert(imp);
				        		}
				        	}
				        }
				        
				        ISet rules = rulesWriter.done();
				        for (IValue rule : rules) {
				          Import.evalImport(parser, (IConstructor) rule);
				        }

//				        parser.event("importing modules");
				        ISet imports = importsWriter.done();
				        for (IValue mod : imports) {
				          Import.evalImport(parser, (IConstructor) mod);
				        }

//				        parser.event("extending modules");
				        ISet extend = extendsWriter.done();
				        for (IValue mod : extend) {
				          Import.evalImport(parser, (IConstructor) mod);
				        }

//				        parser.event("generating modules");
				       ISet externals = externalsWriter.done();
				        for (IValue mod : externals) {
				          Import.evalImport(parser, (IConstructor) mod);
				        }
				        
				        
				        if (env.definesSyntax() && input.indexOf('`') > -1) {
				        	parseTree = Import.parseFragments(parser, parseTree, uri, env);
				        }
					}
					finally {
						parser.setCurrentEnvt(oldEnv);
					}
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
