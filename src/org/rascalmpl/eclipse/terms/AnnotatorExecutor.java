package org.rascalmpl.eclipse.terms;
 
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessageProcessor;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.values.uptr.TreeAdapter;

/**
 * This class connects the Eclipse IDE with Rascal functions that annotate parse trees.
 * 
 * It makes sure these annotator functions are called after each successful parse.
 * After proper annotations have been added, features such as documentation tooltips
 * and hyperlinking uses to definitions start working.
 * 
 * Note that this class only works for languages that have been registered using the
 * API in SourceEditor.rsc
 */
public class AnnotatorExecutor {
	private final MessageProcessor marker = new MessageProcessor();
	
	public synchronized void annotate(ICallableValue func, IConstructor parseTree, IMessageHandler handler) {
		try {
			IConstructor top = parseTree;
			boolean start = false;
			IConstructor tree;
			if (TreeAdapter.isAppl(top) && TreeAdapter.getSortName(top).equals("<START>")) {
				tree = (IConstructor) TreeAdapter.getArgs(top).get(1);
				start = true;
			}
			else {
				tree = top;
			}
			
			Type type = RascalTypeFactory.getInstance().nonTerminalType(tree);
			IConstructor newTree = (IConstructor) func.call(new Type[] {type}, new IValue[] {tree}).getValue();
			
			if (newTree != null) {
				if (start) {
					IList newArgs = TreeAdapter.getArgs(top).put(1, newTree);
					newTree = top.set("args", newArgs).setAnnotation("loc", top.getAnnotation("loc"));
				}
				parseTree = newTree;
				marker.process(parseTree, handler);
			}
			else {
				Activator.getInstance().logException("annotator returned null", new RuntimeException());
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("annotater failed", e);
		}
	}
}
