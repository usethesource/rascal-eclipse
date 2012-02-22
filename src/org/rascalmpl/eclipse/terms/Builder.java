package org.rascalmpl.eclipse.terms;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.BuilderBase;
import org.eclipse.imp.builder.MarkerCreator;
import org.eclipse.imp.builder.MarkerCreatorWithBatching;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.runtime.PluginBase;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.control_exceptions.MatchFailed;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.parser.gtd.io.InputConverter;


public class Builder extends BuilderBase {
	private static final TermLanguageRegistry registry = TermLanguageRegistry.getInstance();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final String MARKER_ID = "rascal_eclipse.term_markers";
	private static final MessagesToMarkers messagesToMarkers = new MessagesToMarkers();

	@Override
	protected PluginBase getPlugin() {
		return Activator.getInstance();
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
		
		try {
			Language lang = registry.getLanguage(file.getFileExtension());
			ICallableValue parser = registry.getParser(lang);
			RascalMonitor rmonitor = new RascalMonitor(monitor);
			IValueFactory VF = parser.getEval().getValueFactory();
			ISourceProject project = ModelFactory.open(file.getProject());
			ISourceLocation loc = VF.sourceLocation(ProjectURIResolver.constructProjectURI(project, file.getProjectRelativePath()));
			contents = file.getContents();
			input = new String(InputConverter.toChar(contents));

			IConstructor tree;
			synchronized (parser.getEval()) {
				tree = (IConstructor) parser.call(rmonitor, new Type[] {TF.stringType(), TF.sourceLocationType()}, new IValue[] { VF.string(input), loc}).getValue();
			}

			ISetWriter messages = VF.setWriter();
			Type type = RascalTypeFactory.getInstance().nonTerminalType(tree);
			
			for (IValue elem : registry.getBuilders(lang)) {
				IConstructor container = (IConstructor) elem;
				ICallableValue builder = (ICallableValue) container.get("messages");
				
				ISet result = null;
				
				synchronized (builder.getEval()) {
					try {
						result = (ISet) builder.call(rmonitor, new Type[] { type }, new IValue[] { tree }).getValue();
					}
					catch (MatchFailed e) {
						Activator.getInstance().logException("builder function can not handle tree of type:" + type, e);
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
			
			handler.handleSimpleMessage("parse error", offset, offset + pe.getLength(), pe.getBeginColumn(), pe.getEndColumn(), pe.getBeginLine() + 1, pe.getEndLine() + 1);
		} 
		catch (Throw e) {
			IValue exc = e.getException();
			
			if (exc.getType() == RuntimeExceptionFactory.Exception) {
				if (((IConstructor) exc).getConstructorType() == RuntimeExceptionFactory.ParseError) {
					ISourceLocation loc = (ISourceLocation) ((IConstructor) e.getException()).get(0);
					handler.handleSimpleMessage("builder error: " + loc, loc.getOffset(), loc.getOffset() + loc.getLength(), loc.getBeginColumn(), loc.getEndColumn(), loc.getBeginLine(), loc.getEndLine());
				}
				else {
					Activator.getInstance().logException(e.getMessage(), e);
				}
			}
		}
		catch (IOException e) {
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
