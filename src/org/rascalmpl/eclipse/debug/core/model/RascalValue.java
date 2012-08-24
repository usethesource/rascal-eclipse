/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.model;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.io.StandardTextWriter;
import org.eclipse.imp.pdb.facts.type.ITypeVisitor;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.interpreter.utils.LimitedResultWriter;
import org.rascalmpl.interpreter.utils.LimitedResultWriter.IOLimitReachedException;

public class RascalValue extends RascalDebugElement implements IValue {
	
	/* do not print more than MAX_VALUE_STRING characters */
	private final static int MAX_VALUE_STRING = 1000;
	private final RascalStackFrame target;
	private final org.eclipse.imp.pdb.facts.IValue value;

	private final String name;

	public RascalValue(RascalStackFrame target, String name, org.eclipse.imp.pdb.facts.IValue value) {
		super(target.getRascalDebugTarget());
		this.name = name;
		this.value = value;
		this.target = target;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof RascalValue) {
			RascalValue val = (RascalValue) arg0;
			return value.equals(val.value) && name.equals(val.name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return value.getType().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if (value == null) {
			return "";
		}
		
		Writer w = new LimitedResultWriter(MAX_VALUE_STRING);
		try {
			new StandardTextWriter(true, 2).write(value, w);
			return w.toString();
		} catch (IOLimitReachedException e) {
			return w.toString();
		}
		catch (IOException e) {
			return "error during serialization...";
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return value.getType().accept(new ITypeVisitor<IVariable[]>() {
			@Override
			public IVariable[] visitReal(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitInteger(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitRational(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitList(Type type) {
				IList list = (IList) value;
				IVariable[] result = new IVariable[list.length()];
				for (int i = 0; i < list.length(); i++) {
					result[i] = new RascalVariable(target, "[" + i + "]", list.get(i));
				}
				return result;
			}

			@Override
			public IVariable[] visitMap(Type type) {
				IMap map = (IMap) value;
				IVariable[] result = new IVariable[map.size()];
				int i = 0;
				for (org.eclipse.imp.pdb.facts.IValue key : map) {
					result[i++] = new RascalVariable(target, key.toString(), map.get(key));
				}
				return result;
			}

			@Override
			public IVariable[] visitNumber(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitAlias(Type type) {
				return type.getAliased().accept(this);
			}

			@Override
			public IVariable[] visitRelationType(Type type) {
				return visitSet(type);
			}

			@Override
			public IVariable[] visitSet(Type type) {
				ISet set = (ISet) value;
				IVariable[] result = new IVariable[set.size()];
				int i = 0;
				for (org.eclipse.imp.pdb.facts.IValue elem : set) {
					result[i++] = new RascalVariable(target, "[" + i + "]", elem);
				}
				return result;
			}

			@Override
			public IVariable[] visitSourceLocation(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitString(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitNode(Type type) {
				INode node = (INode) value;
				IVariable[] result = new IVariable[node.arity()];
				for (int i = 0; i < result.length; i++) {
					result[i] = new RascalVariable(target, "[" + i + "]", node.get(i));
				}
				return result;
			}

			@Override
			public IVariable[] visitConstructor(Type type) {
				IConstructor node = (IConstructor) value;
				IVariable[] result = new IVariable[node.arity()];
				for (int i = 0; i < result.length; i++) {
					result[i] = new RascalVariable(target, type.hasFieldNames() ? type.getFieldName(i) : "" + i, node.get(i));
				}
				return result;
			}

			@Override
			public IVariable[] visitAbstractData(Type type) {
				return visitConstructor(((IConstructor) value).getConstructorType());
			}

			@Override
			public IVariable[] visitTuple(Type type) {
				ITuple node = (ITuple) value;
				IVariable[] result = new IVariable[node.arity()];
				for (int i = 0; i < result.length; i++) {
					result[i] = new RascalVariable(target, type.hasFieldNames() ? type.getFieldName(i) : "[" + i + "]", node.get(i));
				}
				return result;
			}

			@Override
			public IVariable[] visitValue(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitVoid(Type type) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitBool(Type boolType) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitParameter(Type parameterType) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitExternal(Type externalType) {
				return new IVariable[0];
			}

			@Override
			public IVariable[] visitDateTime(Type type) {
				return new IVariable[0];
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		Type type = value.getType();
		return type.isListType() || type.isMapType() || type.isSetType() || type.isAliasType() || type.isNodeType() || type.isConstructorType() || type.isRelationType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return target.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return value.toString();	
	}
	
	public IValue getValue() {
		return this;
	}
}
