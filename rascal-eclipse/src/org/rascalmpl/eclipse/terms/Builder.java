/*******************************************************************************
 * Copyright (c) 2009-2021 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.terms;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToErrorLog;
import org.rascalmpl.eclipse.util.SchedulingRules;
import org.rascalmpl.exceptions.RuntimeExceptionFactory;
import org.rascalmpl.exceptions.Throw;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.control_exceptions.MatchFailed;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.parser.gtd.io.InputConverter;
import org.rascalmpl.types.RascalTypeFactory;
import org.rascalmpl.uri.ProjectURIResolver;

import io.usethesource.impulse.builder.BuilderBase;
import io.usethesource.impulse.builder.MarkerCreator;
import io.usethesource.impulse.builder.MarkerCreatorWithBatching;
import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.model.ISourceProject;
import io.usethesource.impulse.model.ModelFactory;
import io.usethesource.impulse.parser.IMessageHandler;
import io.usethesource.impulse.runtime.PluginBase;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.StandardTextWriter;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;

public class Builder extends BuilderBase {
	private static final TermLanguageRegistry registry = TermLanguageRegistry.getInstance();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final String MARKER_ID = IRascalResources.ID_TERM_MARKER;
	private static final MessagesToMarkers messagesToMarkers = new MessagesToMarkers();
	private static final WarningsToErrorLog warnings = new WarningsToErrorLog();

	@Override
	protected PluginBase getPlugin() {
		return Activator.getInstance();
	}

	@Override
    public ISchedulingRule getRule(int kind, Map<String, String> args) {
	    return SchedulingRules.getRascalProjectBinFolderRule(getProject());
    }
	
    @Override
	protected boolean isSourceFile(IFile file) {
		return registry.getLanguage(file.getFileExtension()) != null;
	}

	@Override
	protected boolean isNonRootSourceFile(IFile file) {
		return false;
	}

	@Override
	protected boolean isOutputFolder(IResource resource) {
		return resource.getFullPath().lastSegment().equals("bin");
	}

	@Override
	protected void compile(IFile file, IProgressMonitor monitor) {
		InputStream contents = null;
		String input = null;
		IMessageHandler handler = new MarkerCreator(file, MARKER_ID);
		Language lang = registry.getLanguage(file.getFileExtension());
		ISet builders = registry.getBuilders(lang);
		IEvaluatorContext evalForErrors = null;
		if (builders == null || builders.size() == 0) {
			return;
		}
		
		try {
			ICallableValue parser = (ICallableValue) registry.getParser(lang);
			evalForErrors = parser.getEval();
			RascalMonitor rmonitor = new RascalMonitor(monitor, warnings) {
			    @Override
			    public boolean jobIsCanceled(String name) {
			        // makes compilation processes interuptible by the next build
			        return super.jobIsCanceled(name) || isInterrupted();
			    }  
			};
			
			IValueFactory VF = parser.getEval().getValueFactory();
			ISourceProject project = ModelFactory.open(file.getProject());
			ISourceLocation loc = ProjectURIResolver.constructProjectURI(project.getRawProject(), file.getProjectRelativePath());
			contents = file.getContents();
			input = new String(InputConverter.toChar(contents, Charset.forName(file.getCharset())));

			IConstructor tree;
			synchronized (parser.getEval()) {
				tree = (IConstructor) parser.call(rmonitor, new Type[] {TF.stringType(), TF.sourceLocationType()}, new IValue[] { VF.string(input), loc}, null).getValue();
			}

			ISetWriter messages = VF.setWriter();
			Type type = RascalTypeFactory.getInstance().nonTerminalType(tree);
			
			
			for (IValue elem : builders) {
				IConstructor container = (IConstructor) elem;
				ICallableValue builder = (ICallableValue) container.get("messages");
				
				ISet result = null;
				
				synchronized (builder.getEval()) {
					try {
						result = (ISet) builder.call(rmonitor, new Type[] { type }, new IValue[] { tree }, null).getValue();
					}
					catch (MatchFailed e) {
						builder.getEval().getErrorPrinter().write("builder function can not handle tree of type:" + type + "\n");
						builder.getEval().getErrorPrinter().write(e.toString() + "\n");
						builder.getEval().getErrorPrinter().flush();
					}
				}
				
				if (result != null) {
					messages.insertAll(result);
				}
			}
			
			messagesToMarkers.process(loc, messages.done(), handler);
			handler.endMessages();
			
			// TODO: this MarkerCreatorWithBatching should just implement endMessages() correctly.
			if (handler instanceof MarkerCreatorWithBatching) {
				((MarkerCreatorWithBatching) handler).flush(monitor);
			}
		}
		catch (ParseError pe){
			int offset = pe.getOffset();
			if(offset == input.length()) --offset;
			
			handler.handleSimpleMessage("parse error", offset, offset + pe.getLength(), pe.getBeginColumn(), pe.getEndColumn(), pe.getBeginLine(), pe.getEndLine());
		} 
		catch (Throw e) {
			IValue exc = e.getException();
			
			if (exc.getType() == RuntimeExceptionFactory.Exception) {
				if (((IConstructor) exc).getConstructorType() == RuntimeExceptionFactory.ParseError) {
					ISourceLocation loc = (ISourceLocation) ((IConstructor) e.getException()).get(0);
					handler.handleSimpleMessage("builder error: " + loc, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
				}
				else if (evalForErrors != null) {
				    ReadEvalPrintDialogMessages.throwMessage(evalForErrors.getErrorPrinter(), e, new StandardTextWriter());
				    evalForErrors.getErrorPrinter().println();
					evalForErrors.getErrorPrinter().flush();
				}
				else {
					Activator.getInstance().logException(e.getMessage(), e);
				}
			}
			else {
				if (evalForErrors != null) {
				    ReadEvalPrintDialogMessages.throwMessage(evalForErrors.getErrorPrinter(), e, new StandardTextWriter());
				    evalForErrors.getErrorPrinter().println();
					evalForErrors.getErrorPrinter().flush();
				}
				else {
					Activator.getInstance().logException(exc.toString(), e);
				}
			}
		}
		catch (IOException e) {
			String error = "could not read file in builder: " + file;
			if (evalForErrors != null) {
				evalForErrors.getErrorPrinter().write(error + "\n");
				evalForErrors.getErrorPrinter().write(e.toString() + "\n");
				evalForErrors.getErrorPrinter().flush();
			}
			else 
				Activator.getInstance().logException("could not read file in builder: " + file, e);
			return;
		}
		catch (Throwable e) {
			Activator.getInstance().logException("exception in builder for: " + file, e);
		}
		finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (IOException e) {
					Activator.getInstance().logException("exception in builder for: " + file, e);
				}
			}
		}
	}

	@Override
	protected void collectDependencies(IFile file) {
		// nothing for now
	}

	@Override
	protected String getErrorMarkerID() {
		return MARKER_ID;
	}

	@Override
	protected String getWarningMarkerID() {
		return MARKER_ID;
	}

	@Override
	protected String getInfoMarkerID() {
		return MARKER_ID;
	}
}
