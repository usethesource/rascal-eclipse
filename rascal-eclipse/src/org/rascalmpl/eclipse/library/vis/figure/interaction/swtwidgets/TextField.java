/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *******************************************************************************/
package org.rascalmpl.eclipse.library.vis.figure.interaction.swtwidgets;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rascalmpl.eclipse.library.vis.graphics.GraphicsContext;
import org.rascalmpl.eclipse.library.vis.properties.Properties;
import org.rascalmpl.eclipse.library.vis.properties.PropertyManager;
import org.rascalmpl.eclipse.library.vis.swt.IFigureConstructionEnv;
import org.rascalmpl.eclipse.library.vis.swt.SWTFontsAndColors;
import org.rascalmpl.eclipse.library.vis.swt.applet.IHasSWTElement;
import org.rascalmpl.eclipse.library.vis.util.FigureColorUtils;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.functions.IFunction;

import io.usethesource.vallang.IValue;

public class TextField extends SWTWidgetFigureWithValidationAndCallBack<Text> {

	private final Color falseColor;
	

	public TextField(IFigureConstructionEnv env, String text, IFunction cb, IFunction validate, PropertyManager properties) {
		super(env, cb, validate, properties);
		falseColor = SWTFontsAndColors.getRgbColor(FigureColorUtils.colorNames.get("red").intValue());
		widget = makeWidget(env.getSWTParent(), env,text);
		widget.setVisible(false);
		widget.setText(text);
	}

	@Override
	public void drawElement(GraphicsContext gc, List<IHasSWTElement> visibleSWTElements) {
		super.drawElement(gc, visibleSWTElements);
		widget.setForeground(validated ? SWTFontsAndColors.getRgbColor(prop.getColor(Properties.FONT_COLOR)) : falseColor);
	}

	Text makeWidget(Composite comp, IFigureConstructionEnv env,String text) {
		Text textfield = new Text(comp, SWT.SINGLE | SWT.BORDER);
		
		textfield.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doValidate();
			}
		});
		textfield.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doCallback();
			}
		});

		return textfield;
	}

	@Override 
	IValue executeValidate() {
		return cbenv.executeRascalCallBack(validate,ValueFactoryFactory.getValueFactory().string(widget.getText()));
	}

	@Override
	void executeCallback() {
		cbenv.executeRascalCallBack(callback, ValueFactoryFactory.getValueFactory().string(widget.getText()));
	}

}
