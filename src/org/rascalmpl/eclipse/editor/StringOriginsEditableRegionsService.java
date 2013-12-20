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
import org.rascalmpl.values.IRascalValueFactory;
import org.rascalmpl.values.RascalValueFactory;

public class StringOriginsEditableRegionsService extends EditorServiceBase{
	
	private ISourceViewer sourceViewer;
	private EditableRegionsEventConsumer eventConsumer;
	private EditableRegionsTextListener textListener;
	private IConstructor previousAst;
	private LinkedHashMap<String, IRegion> regions;
	private IRascalValueFactory values;
	
	public StringOriginsEditableRegionsService() {
		super();
		this.values = RascalValueFactory.getInstance();
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
			LinkedHashMap<String, IRegion> regions = RegionsCalculator.getRegions((IConstructor) parseController.getCurrentAst());
			if (regions != null){
				this.regions = regions;
				eventConsumer = new EditableRegionsEventConsumer(regions);
				textListener = new EditableRegionsTextListener(regions);
				sourceViewer.setEventConsumer(eventConsumer);
				sourceViewer.addTextListener(textListener);
			}
		}
		if (previousAst != null){
			EditableRegionsRegistry.removeRegistryForDocument(previousAst);
			previousAst = (IConstructor) parseController.getCurrentAst();
			((IConstructor) parseController.getCurrentAst()).asAnnotatable()
				.setAnnotation("regions", RegionsCalculator.fromMap(values, regions, parseController.getDocument().get()));
		}
		EditableRegionsRegistry.setRegistryForDocument((IConstructor) parseController.getCurrentAst(), regions);
		
	}	
}
