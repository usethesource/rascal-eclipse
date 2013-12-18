package org.rascalmpl.eclipse.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.services.base.EditorServiceBase;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class StringOriginsEditableRegionsService extends EditorServiceBase{
	
	private ISourceViewer sourceViewer;
	private EditableRegionsEventConsumer eventConsumer;
	private EditableRegionsTextListener textListener;
	private EditableRegionsHighlightingController highlightingController;
	private IConstructor previousAst;
	private LinkedHashMap<String, IRegion> regions;
	
	
	public StringOriginsEditableRegionsService() {
		super();
	}

	@Override
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	@Override
	public void update(IParseController parseController,
			IProgressMonitor monitor) {
		if (sourceViewer == null){
			try {
				Method getter = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
				getter.setAccessible(true);
				sourceViewer = (ISourceViewer) getter.invoke(super.editor, new Object[0]);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException e) {
				return;
			}
		}
		if (eventConsumer == null){
			LinkedHashMap<String, IRegion> regions = RegionsCalculator.calculateRegions((IConstructor) parseController.getCurrentAst(), 
				 parseController.getDocument().get());
			this.regions = regions;
			eventConsumer = new EditableRegionsEventConsumer(regions);
			textListener = new EditableRegionsTextListener(regions);
			sourceViewer.setEventConsumer(eventConsumer);
			sourceViewer.addTextListener(textListener);
		}
		if (previousAst != null)
			EditableRegionsRegistry.removeRegistryForDocument((IConstructor) parseController.getCurrentAst());
		EditableRegionsRegistry.setRegistryForDocument((IConstructor) parseController.getCurrentAst(), regions);
		if (highlightingController != null)
			highlightingController.update(parseController, monitor);
	}

	
	
}
