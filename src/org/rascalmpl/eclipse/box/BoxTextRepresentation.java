package org.rascalmpl.eclipse.box;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


public class BoxTextRepresentation extends TextPresentation {
	
	private final IDocument d;
	
	BoxTextRepresentation(IDocument d) {
		this.d = d;
		// System.err.println("Doclen:"+d.getLength());
		setDefaultStyleRange(df(new Position(0, d.getLength())));
			try {
				for (Position p: d.getPositions(IDocument.DEFAULT_CATEGORY)) {
					// System.err.println("S:"+c+" "+p);
					TypedPosition q = (TypedPosition) p;
					if (q.getType()=="bf") this.replaceStyleRange(bf(p));
					if (q.getType()=="it") this.replaceStyleRange(it(p));
					if (q.getType()=="nm") this.replaceStyleRange(nm(p));
					if (q.getType()=="df") this.replaceStyleRange(df(p));
				}
			} catch (BadPositionCategoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	final IDocument getDocument() {
		return this.d;
	}
	
	public StyleRange bf(Position p) {
		return new StyleRange(p.getOffset(), p.getLength(), keyColor, bgColor, SWT.BOLD);
	}

	public StyleRange it(Position p) {
		return new StyleRange(p.getOffset(), p.getLength(), textColor, bgColor, SWT.ITALIC);
	}

	public StyleRange nm(Position p) {
		return new StyleRange(p.getOffset(), p.getLength(), numColor, bgColor, SWT.NORMAL);
	}
	
	public StyleRange df(Position p) {
		return new StyleRange(p.getOffset(), p.getLength(), textColor, bgColor, SWT.NORMAL);
	}

	private static Color getColor(final int which) {
		Display display = Display.getCurrent();
		if (display != null)
			return display.getSystemColor(which);
		display = Display.getDefault();
		final Color result[] = new Color[1];
		display.syncExec(new Runnable() {
			public void run() {
				synchronized (result) {
					result[0] = Display.getCurrent().getSystemColor(which);
				}
			}
		});
		synchronized (result) {
			return result[0];
		}
	}

	static Color keyColor = new Color(Display.getCurrent(), new RGB(127, 0, 85));
	static Color textColor = getColor(SWT.COLOR_BLACK);
	static Color varColor = getColor(SWT.COLOR_GRAY);
	static Color boldColor = getColor(SWT.COLOR_MAGENTA);
	static Color bgColor = getColor(SWT.COLOR_WHITE);
	static Color numColor = new Color(Display.getCurrent(), new RGB(0, 0, 192));	
}
