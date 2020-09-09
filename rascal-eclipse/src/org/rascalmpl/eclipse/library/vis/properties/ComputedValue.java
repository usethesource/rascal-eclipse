/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Atze van der Ploeg - Atze.van.der.Ploeg@cwi.nl - CWI
*******************************************************************************/

package org.rascalmpl.eclipse.library.vis.properties;

import org.rascalmpl.eclipse.library.vis.swt.IFigureConstructionEnv;
import org.rascalmpl.eclipse.library.vis.util.RascalToJavaValueConverters.Convert;
import org.rascalmpl.values.functions.IFunction;

import io.usethesource.vallang.IValue;

public  class ComputedValue<PropType> extends PropertyValue<PropType> {
		
	IFunction fun;
	PropType value;
	int lastComputeClock;
	IFigureConstructionEnv env;
	Convert<PropType> converter; 
	PropertyManager pm;

	public ComputedValue(IFunction fun, IFigureConstructionEnv env, PropertyManager pm, Convert<PropType> converter){
		this.fun = fun;
		lastComputeClock = -1;
		this.env = env;
		this.converter = converter;
		this.pm = pm;
	}
	
	void compute() {
		IValue res = env.getCallBackEnv().executeRascalCallBack(fun);
		value = converter.convert(res, pm, env);
	}
	
	public PropType getValue() {
		int currentComputeClock = env.getCallBackEnv().getComputeClock();
		if(currentComputeClock != lastComputeClock){
			compute();
			lastComputeClock = currentComputeClock;
		}
		return value;
	}	
}
	
