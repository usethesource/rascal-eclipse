//package org.rascalmpl.eclipse.library.vis.figure.keys;
//
//import io.usethesource.vallang.IConstructor;
//import io.usethesource.vallang.IList;
//import io.usethesource.vallang.INumber;
//import io.usethesource.vallang.IValue;
//import io.usethesource.vallang.impl.fast.ValueFactory;
//import io.usethesource.vallang.type.Type;
//import io.usethesource.vallang.type.TypeFactory;
//import org.rascalmpl.eclipse.library.vis.figure.FigureFactory;
//import org.rascalmpl.eclipse.library.vis.figure.combine.WithInnerFig;
//import org.rascalmpl.eclipse.library.vis.graphics.GraphicsContext;
//import org.rascalmpl.eclipse.library.vis.properties.Properties;
//import org.rascalmpl.eclipse.library.vis.properties.PropertyManager;
//import org.rascalmpl.eclipse.library.vis.swt.IFigureConstructionEnv;
//import org.rascalmpl.eclipse.library.vis.util.Key;
//import org.rascalmpl.eclipse.library.vis.util.NameResolver;
//
//public class IntervalKey extends WithInnerFig implements Key {
//
//	IValue explain;
//	IValue interpolate;
//	// TODO: can this type be more general? (i.e. Comparable)
//	INumber low,high, interval;
//	private IList childProps;
//	String id;
//	IFigureConstructionEnv env;
//	
//	public IntervalKey(IFigureConstructionEnv env,IValue interpolate, IValue explain, PropertyManager properties,IList childProps){
//		super(null,properties);
//		this.childProps = childProps;
//		id = getIdProperty();
//		this.explain = explain;
//		this.interpolate = interpolate;
//		this.env = env;
//	}
//	
//
//	public void init(){
//		super.init();
//		interval = high = low = null;
//	}
//	
//	public void finalize(){
//		if(high == null){
//			high = ValueFactory.getInstance().real(0);
//		} 
//		if(low == null){
//			low = ValueFactory.getInstance().real(0);
//		}
//		if(low.toReal().isEqual(high.toReal())){
//			low = ValueFactory.getInstance().real(0);
//		}
//		if(low.toReal().isEqual(high.toReal())){
//			interval = ValueFactory.getInstance().real(1);
//		} else {
//			interval = high.subtract(low);
//		}
//		
//		
//		//System.out.printf("Nominal finalize bbox start!\n");
//		if(innerFig != null){
//			innerFig.destroy();
//		}
//		
//		TypeFactory tf = TypeFactory.getInstance();
//		Type[] argTypes = {tf.valueType(),tf.valueType()};
//		IValue[] args = {low,high};
//		//System.out.printf("IntervalKey %s explain callBack!\n",id);
//		IConstructor figureCons = (IConstructor)
//				env.getCallBackEnv().executeRascalCallBack(explain,argTypes,args).getValue();
//		//System.out.printf("IntervalKey %s explain done!\n",id);
//		innerFig = FigureFactory.make(env, figureCons, prop, childProps);
//		innerFig.init();
//		innerFig.computeFiguresAndProperties(env.getCallBackEnv());
//		NameResolver resolver = new NameResolver( env.getRascalContext());
//		innerFig.registerNames(resolver);
//		innerFig.registerValues(resolver);
//		innerFig.getLikes(resolver);
//		innerFig.finalize();
//		prop = innerFig.prop;
//	}
//	
//	public void bbox(){
//		innerFig.bbox();
//		minSize.set(innerFig.minSize);
//		setResizable();
//		//System.out.printf("Nominal key bbox done!\n");
//	}
//	
//	@Override
//	public void layout() {
//		innerFig.size.set(size);
//		innerFig.location.set(location);
//		innerFig.layout();
//	}
//	
//	public void registerNames(NameResolver resolver) {
//		resolver.register(id,this);
//	}
//
//	@Override
//	public void draw(GraphicsContext gc) {
//		innerFig.draw(gc);
//		
//	}
//	
//	public void registerValue(Properties prop, IValue val) {
//		if(!(val instanceof INumber)) return;
//		INumber n = (INumber) val;
//		if(low == null){
//			low = n;
//		} else if(n.less(low).getValue()){
//			low = n;
//		}
//		if(high == null){
//			high = n;
//		} else if(n.greater(high).getValue()){
//			high = n;
//		}
//	}
//	
//	public void registerOffset(double offset) {
//		return;
//		
//	}
//	
//	public IValue scaleValue(IValue val) {
//		if(!(val instanceof INumber)) return ValueFactory.getInstance().real(0);
//		INumber n = (INumber) val;
//		INumber part;
//	
//			//System.out.printf("DIv undefined? %s %s %s",low, high,interval);
//			part = n.subtract(low).divide(interval,10);
//	
//		
//		TypeFactory tf = TypeFactory.getInstance();
//		//System.out.printf("IntervalKey %s scale callBack!\n",id);
//		IValue res =  
//				env.getCallBackEnv().executeRascalCallBackSingleArgument(interpolate, tf.valueType(), part).getValue();
//		//System.out.printf("IntervalKey %s scale done!\n",id);
//		return res;
//	}
//	
//	public String getId() {
//		return id;
//	}
//	
//}
