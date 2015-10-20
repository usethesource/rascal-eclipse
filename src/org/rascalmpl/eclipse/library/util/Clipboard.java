package org.rascalmpl.eclipse.library.util;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.uptr.RascalValueFactory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class Clipboard {
	private final IValueFactory vf;
	private org.eclipse.swt.dnd.Clipboard cb;
	private final TextTransfer tt;
	final Display display ;
	private String tmp;

	public Clipboard(IValueFactory vf) {
		this.vf = vf;
		this.tt = TextTransfer.getInstance();
		this.display = PlatformUI.getWorkbench().getDisplay();
		
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Clipboard.this.cb = new org.eclipse.swt.dnd.Clipboard(display);
			}
		});
	}
	
	public void copy(final IValue content) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				String str;
				
				if (content.getType().isString()) {
					str = ((IString) content).getValue();
				}
				else if (content.getType().isSubtypeOf(RascalValueFactory.Tree)) {
					str = TreeAdapter.yield((IConstructor) content);
				}
				else {
					str = content.toString();
				}
				
				cb.setContents(new Object[]{str}, new Transfer[]{tt});
			}
		});
	}
	
	public IString paste() {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				tmp = (String) cb.getContents(tt);
			}
		});
		
		if (tmp != null) {
			return vf.string(tmp);
		}
		else {
			return vf.string("");
		}
	}
}
