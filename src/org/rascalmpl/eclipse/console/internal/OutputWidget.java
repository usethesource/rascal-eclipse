package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

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

public class OutputWidget implements PausableOutput{

	Text text;
	boolean lastNewLine;
	int bufferSize;
	boolean showAlways;
	boolean isEmpty;
	int size;
	Pausable pausable;
	private OutputStream outputStream;
	
	public OutputWidget(Composite parent,Color c, int bufferSize,boolean showAlways, Pausable pausable) {
		text = new Text(parent, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL| SWT.READ_ONLY);
		text.setEditable(false);
		this.bufferSize = bufferSize * 2;
		text.setTextLimit(this.bufferSize);
		text.setForeground(c);
		setVisibility(text.getHorizontalBar());
		setVisibility(text.getVerticalBar());
		lastNewLine = false;
		this.pausable = pausable;
		this.showAlways = showAlways;
		isEmpty = true;
		size = 0;
		if(!showAlways){
			text.setVisible(false);
		}
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
		if(!showAlways){
			text.setVisible(false);
			text.getParent().layout(true);
		}
		text.setText("");
	}
	
	public boolean isPaused(){
		return pausable.isPaused();
	}


	@Override
	public void output(byte[] b) throws IOException {
		final String s = new String(b,"UTF16");
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int newSize = size + s.length();
				if(newSize >= bufferSize){
					text.setText(text.getText(bufferSize/2, size));
					size = size - bufferSize/2;
				}
				size+=s.length();
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
				if(isEmpty && !showAlways){
					text.setVisible(true);
					text.getParent().layout();
				}
				setVisibility(text.getHorizontalBar());
				setVisibility(text.getVerticalBar());
				
			}
		});
	}


	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	
	
}
