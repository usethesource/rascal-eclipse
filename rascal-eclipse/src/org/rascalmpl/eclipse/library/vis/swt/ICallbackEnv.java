/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.rascalmpl.eclipse.library.vis.swt;

import org.rascalmpl.values.functions.IFunction;

import io.usethesource.vallang.IValue;

public interface ICallbackEnv {
	public void checkIfIsCallBack(IValue fun);
	public void fakeRascalCallBack();
	public int getComputeClock();
	public void signalRecompute();
	public long getAndResetRascalTime(); // profiling
	public IValue executeRascalCallBack(IFunction callback, IValue... argVals) ;
	public void registerAnimation(Animation a);
	public void unregisterAnimation(Animation a);
}
