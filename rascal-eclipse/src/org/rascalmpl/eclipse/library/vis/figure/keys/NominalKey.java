/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package org.rascalmpl.eclipse.library.vis.figure.keys;

import java.util.Vector;

import org.rascalmpl.eclipse.library.vis.figure.FigureFactory;
import org.rascalmpl.eclipse.library.vis.figure.combine.LayoutProxy;
import org.rascalmpl.eclipse.library.vis.figure.interaction.MouseOver;
import org.rascalmpl.eclipse.library.vis.properties.Properties;
import org.rascalmpl.eclipse.library.vis.properties.PropertyManager;
import org.rascalmpl.eclipse.library.vis.swt.IFigureConstructionEnv;
import org.rascalmpl.eclipse.library.vis.util.Key;
import org.rascalmpl.eclipse.library.vis.util.NameResolver;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.type.TypeFactory;


@SuppressWarnings("rawtypes")
public class NominalKey extends LayoutProxy implements Key{
  private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	IValue whole;
	IList possibilities;
	Vector<IValue> originals;
	private IList childProps;
	IValue[] tmpArray ;
	String id;
	IFigureConstructionEnv env;
	
	public NominalKey(IFigureConstructionEnv env, IList possibilties, IValue whole, PropertyManager properties,IList childProps){
		super(null,properties);
		this.env = env;
		this.childProps = childProps;
		this.whole = whole;
		this.possibilities = possibilties;
		this.originals = new Vector<IValue>(possibilties.length());
		tmpArray = new IValue[originals.size()];
		id = prop.getStr(Properties.ID);
	}
	
	public void setChildren(IFigureConstructionEnv env, NameResolver resolver) {
		super.setChildren(env,resolver);
		originals.clear();
	}
	

	public void initElem(IFigureConstructionEnv env, MouseOver mparent, boolean swtSeen, boolean visible, NameResolver resolver){
		if(innerFig != null){
			innerFig.destroy(env);
		}
		TypeFactory tf = TypeFactory.getInstance();
		IList originalsL = VF.list(originals.toArray(tmpArray));
		IConstructor figureCons = (IConstructor) env.getCallBackEnv().executeRascalCallBackSingleArgument(whole,tf.listType(tf.valueType()),originalsL).getValue();
		innerFig = FigureFactory.make(env, figureCons, prop, childProps);
		innerFig.registerIds(env, resolver);
		innerFig.registerConverts(resolver);
		setInnerFig( innerFig);
		prop.stealExternalPropertiesFrom(innerFig.prop);
	}

	public void registerValue(IValue val) {
		for(int i = 0 ; i < originals.size() ; i++ ){
			if(originals.get(i).equals(val)){
				return ;
			}
		}
		if(originals.size()  < possibilities.length()){
			originals.add(val);
		} 
	}
	
	public IValue scaleValue(IValue val) {
		
		for(int i = 0 ; i < originals.size()  ; i++){
			if(originals.get(i).equals((IValue)val)){
				return possibilities.get(i);
			}
		}
		return possibilities.get(0);
	}
	
	public String getId() {
		return id;
	}
}
