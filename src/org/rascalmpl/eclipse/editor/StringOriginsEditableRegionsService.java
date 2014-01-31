package org.rascalmpl.eclipse.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.services.base.EditorServiceBase;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.rascalmpl.values.IRascalValueFactory;
import org.rascalmpl.values.RascalValueFactory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class StringOriginsEditableRegionsService extends EditorServiceBase{
	
	private ISourceViewer sourceViewer;
	private EditableRegionsEventConsumer eventConsumer;
	private EditableRegionsTextListener textListener;
	private IRascalValueFactory values;
	
	public StringOriginsEditableRegionsService() {
		super();
		this.values = RascalValueFactory.getInstance();
		this.sourceViewer = getSourceViewer();
	}

	@Override
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	@Override
	public void update(IParseController parseController,
			IProgressMonitor monitor) {
		if (sourceViewer == null)
			return;
		if (eventConsumer == null){
			createListeners((IConstructor) parseController.getCurrentAst());
		}
	}
	
	private void createListeners(IConstructor pt) {
		LinkedHashMap<String, ISourceLocation> regions = RegionsCalculator.getRegions(pt);
		if (regions != null){
			ISourceLocation loc = TreeAdapter.getLocation(pt);
			EditableRegionsRegistry.setRegistryForDocument(loc, regions);
			eventConsumer = new EditableRegionsEventConsumer(loc);
			textListener = new EditableRegionsTextListener(values, loc);
			sourceViewer.setEventConsumer(eventConsumer);
			sourceViewer.addTextListener(textListener);
		}		
	}

	private ISourceViewer getSourceViewer(){
		if (sourceViewer == null){
			try {
				Method getter = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
				getter.setAccessible(true);
				sourceViewer = (ISourceViewer) getter.invoke(super.editor, new Object[0]);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException e) {
				return null;
			}
		}
		return sourceViewer;
	}
}
