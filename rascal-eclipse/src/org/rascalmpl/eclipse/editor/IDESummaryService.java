/*******************************************************************************
 * Copyright (c) 2009-2018 CWI, 2019 NWO-I CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.rascalmpl.eclipse.editor;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IString;

public interface IDESummaryService {
	IConstructor calculate(IString moduleName, IConstructor pcfg);
	INode getOutline(IConstructor moduleTree);
}