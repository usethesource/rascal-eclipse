package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.IDocumentationProvider;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ParsetreeAdapter;

/*
 * Assuming the innermost lexical node in the parse tree is given,  we simply return the annotation labeled
 * "doc" which is a string.
 */
public class DocumentationProvider  implements IDocumentationProvider {
	
	public String getDocumentation(Object target,
			IParseController parseController) {
		if (target instanceof IConstructor) {
			if (((IConstructor) target).getType() == Factory.Tree) {
				return getDocString((IConstructor) target);
			}
			if (((IConstructor) target).getConstructorType() == Factory.ParseTree_Top) {
				return getDocumentation(ParsetreeAdapter.getTop((IConstructor) target), parseController);
			}
		}
		
		return null;
	}

	private String getDocString(IConstructor arg) {
		IValue val = arg.getAnnotation("doc");

		if (val != null && val.getType().isStringType()) {
				return ((IString) val).getValue();
		}
		
		return null;
	}
}
