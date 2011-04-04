/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

public class CommandExecutionException extends Exception{
	private static final long serialVersionUID = -7113571508827328424L;
	
	private final int offset;
	private final int length;
	
	public CommandExecutionException(String message){
		super(message);
		
		this.offset = -1;
		this.length = -1;
	}
	
	public CommandExecutionException(String message, int offset, int length){
		super(message);
		
		this.offset = offset;
		this.length = length;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public int getLength(){
		return length;
	}
}
