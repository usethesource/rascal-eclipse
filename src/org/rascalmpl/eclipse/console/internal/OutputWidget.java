package org.rascalmpl.eclipse.console.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;

public class OutputWidget{

	boolean paused;
	Text text;
	boolean lastNewLine;
	int bufferSize;
	
	public OutputWidget(Composite parent,Color c, int bufferSize) {
		text = new Text(parent, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL| SWT.READ_ONLY);
		text.setEditable(false);
		this.bufferSize = bufferSize;
		text.setTextLimit(bufferSize);
		text.setForeground(c);
		setVisibility(text.getHorizontalBar());
		setVisibility(text.getVerticalBar());
		lastNewLine = false;
		paused = false;
	}

	
	public void append(final String s){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(lastNewLine){
					text.append("\n");
					lastNewLine = false;
				}
				if(s.endsWith("\n")){
					lastNewLine = true;
					text.append(s.substring(0, s.length()-1));
				} else {
					text.append(s);
				}
				setVisibility(text.getHorizontalBar());
				setVisibility(text.getVerticalBar());
			}
		});
	}
	
	
	void setVisibility(final ScrollBar sb){
		boolean nessary = isScrollBarNessary(sb);
		if(sb.isVisible() != nessary ){
			sb.setVisible(nessary);
		}
	}
	
	boolean isScrollBarNessary(final ScrollBar sb){
		return sb.getMaximum() != sb.getThumb();
	}
	
	// NEED TO RUN FROM UI THREAD
	void clear(){
		text.setText("");
	}
	
	
	
}
