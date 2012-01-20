package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

public class OutputWidget implements PausableOutput{

	StyledText text;
	boolean lastNewLine;
	int bufferSize;
	boolean showAlways;
	boolean isEmpty;
	int size;
	Pausable pausable;
	
	public OutputWidget(Composite parent,Color c, int bufferSize,boolean showAlways, Pausable pausable) {
		text = new StyledText(parent, SWT.MULTI | SWT.LEFT | SWT.H_SCROLL | SWT.V_SCROLL| SWT.READ_ONLY);
		setFont();
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
		size = 0;
	}
	
	public boolean isPaused(){
		return pausable.isPaused() || text.isDisposed();
	}


	@Override
	public void output(byte[] b) throws IOException {
		final String s = new String(b,"UTF16");
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int newSize = size + s.length();
				if(newSize >= bufferSize){
					String cur = text.getText();
					int start = cur.indexOf('\n',cur.length()/2 ) + 1;
					cur = cur.substring(start);
					text.setText(cur);
					size = cur.length();
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
				text.setTopPixel(Integer.MAX_VALUE);
			}
		});
	}


	private void setFont(){
		final FontRegistry fontRegistry= RuntimePlugin.getInstance().getFontRegistry();
		final String fontDescriptor = RuntimePlugin.getInstance().getPreferenceStore().getString(PreferenceConstants.P_SOURCE_FONT);
		
		if (fontDescriptor != null) {
			if (!fontRegistry.hasValueFor(fontDescriptor)) {
				FontData[] fontData= PreferenceConverter.readFontData(fontDescriptor);
				fontRegistry.put(fontDescriptor, fontData);
			}
			
			Display.getDefault().syncExec(new Runnable(){
				public void run(){
					Font sourceFont= fontRegistry.get(fontDescriptor);
					text.setFont(sourceFont);
				}
			});
		}
	}
	
	public void dispose(){
		text.dispose();
	}
}
