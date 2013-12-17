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
import org.eclipse.imp.services.EditableRegionsRegistry;

public class StringOriginsEditableRegionsService extends EditorServiceBase{
	
	private ISourceViewer sourceViewer;
	private EditableRegionsEventConsumer eventConsumer;
	
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
				EditableRegionsRegistry.setSourceViewerForDocument(parseController, sourceViewer);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException e) {
				return;
			}
		}
		if (eventConsumer == null){
			LinkedHashMap<String, IRegion> regions = RegionsCalculator.calculateRegions((IConstructor) parseController.getCurrentAst(), 
				 parseController.getDocument().get());
			eventConsumer = new EditableRegionsEventConsumer(regions);
			EditableRegionsRegistry.setRegistryForDocument(parseController, regions);
			sourceViewer.setEventConsumer(eventConsumer);	
		}
	}
	
	
}
