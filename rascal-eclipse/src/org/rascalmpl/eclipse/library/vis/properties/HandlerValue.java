/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.rascalmpl.eclipse.library.vis.properties;

import org.rascalmpl.eclipse.library.vis.swt.ICallbackEnv;
import org.rascalmpl.values.functions.IFunction;

import io.usethesource.vallang.IValue;

public class HandlerValue extends PropertyValue<IValue>  {
	
	IFunction fun;
	IValue value;

	
	public HandlerValue(IFunction fun){
		this.fun = fun;
	}
	
	@Override
	public IValue execute(ICallbackEnv env, IValue... args) {
		value = env.executeRascalCallBack(fun, args);
		return value;
	}

	@Override
	public IValue getValue() {
		return value;
	}

}
