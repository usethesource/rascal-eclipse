package org.meta_environment.rascal.eclipse.library.ecore;

import java.net.URI;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ECore{
	
	private static Resource loadECore(URI ecoreFile){
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
		
		return set.createResource(org.eclipse.emf.common.util.URI.createURI(ecoreFile.toString()));
	}
	
	// Temp
	public static void main(String[] args) throws Exception{
		Resource res = loadECore(new URI("file:///tmp/test.ecore"));
		System.out.println(res);
	}
}
