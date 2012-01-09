package org.rascalmpl.eclipse.console.outputconsole;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.widgets.Display;


public class CircularLines extends OutputStream implements StyledTextContent{
	
	private static final int TMP_SIZE = 2;
	int start, end;
	int nrLines;
	int nrChars;
	ArrayList<TextChangeListener> listeners;
	
	String[] lines;
	int[] offsets;
	int[] lengths;
	byte[] tmp;
	int tmpSize;
	
	CircularLines(int maxLines){
		lines = new String[maxLines];
		start = 0;
		end = 0;
		nrLines = 1;
		offsets = new int[maxLines];
		lengths = new int[maxLines];
		listeners = new ArrayList<TextChangeListener>();
		tmp = new byte[TMP_SIZE];
		tmpSize = 0;
		for(int i = 0 ; i < lines.length; i++){
			lines[i] = "";
		}
	}
	
	private int cpStringLength(String s){
		return s.codePointCount(0, s.length());
	}
	
	private void notifyListenersLinesRemoved(int nrLinesRemoved) {
		int nrCharsRemoved = 
				offsets[(start + nrLinesRemoved) % lines.length] - offsets[start];
		final TextChangingEvent e = new TextChangingEvent(this);
		e.replaceCharCount = nrCharsRemoved;
		e.replaceLineCount = nrLinesRemoved;
		e.start = 0;
		sendTextChanging(e);
	}
	
	private void clearHeadLines(int len){
		notifyListenersLinesRemoved(len);
		for( int i = 0 ; i < len ; i++){
			lines[start] = "";
			lengths[start] = 0;
			offsets[start] = 0;
			nrChars-=lengths[start];
			start = (start + 1) %lines.length;
			nrLines--;
		}
		sendTextChanged();
	}

	private void notifyListenersLinesAdded(String[] ss) {
		int len = 0;
		for(String s : ss){
			len += s.codePointCount(0,s.length());
		}
		final TextChangingEvent e = new TextChangingEvent(this);
		e.replaceCharCount = 0;
		e.replaceLineCount = 0;
		e.newCharCount = len + ss.length-1;
		e.newLineCount = ss.length-1;
		e.start = offsets[mod(end - 1,lines.length)] - offsets[start];
		if(e.start <= 0){
			throw new Error("begin index negative!");
		}
		System.out.printf("Notifing added %d %d %d\n", e.newCharCount, e.newLineCount, e.start);
		sendTextChanging(e);
	}
	
	private void appendLines(String[] s){
		if(s.length == 0){
			return;
		}
		notifyListenersLinesAdded(s);
		int prev = mod(end - 1,lines.length);
		int offset = offsets[prev] + lengths[prev];
		
		for(int i = 0 ; i < s.length ; i++){
			lines[end]+=s[i];
			lengths[end] = cpStringLength(s[i]);
			offsets[end] = offset;
			offset+=lengths[end];
			nrChars+=lengths[end];
			if(i != s.length-1){
				nrChars+=1;
				offset+=1;
				end = (end + 1) % lines.length;
				nrLines++;
				if(nrLines >= lines.length){
					nrLines = lines.length;
				}
			}
		}
		sendTextChanged();
	}


	/**
	 * Assumes UTF16!
	 * @param input
	 * @return
	 */
	private static String[] split(byte[] input,int offset, int len){
		assert input.length % 2 == 0;
		try {
			List<String> result = new ArrayList<String>();
			int start = 0;
			for(int i = offset ; i < offset + len ; i+=2){
				if(input[i + 1] == '\n'){
					result.add(new String(input,start,i  - start  , "UTF16"));
					start = i + 2;
				}
			}
			if( input[offset + len - 1] == '\n'){
				result.add("");
			} else if(start < offset + len){
				result.add(new String(input,start,offset + len - start, "UTF16"));
			}
			return result.toArray(new String[]{});
		} catch (UnsupportedEncodingException e) {
			throw new Error("UTF16 not supported????!!",e);
		}
	}

	@Override
	public void write(byte[] input,int offset , int length){
		final String[] newLines =split(input,offset,length);
		final int newNrLines = newLines.length -1 + nrLines;
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if(newNrLines > lines.length){
					System.out.printf("Clearing %d lines\n",newNrLines - lines.length);
					clearHeadLines(newNrLines - lines.length);
				}
				appendLines(newLines);
			}
		});
		
	}
	
	@Override
	public void write(byte[] input){
		write(input,0,input.length);
	}
	
	@Override
	public void write(int b) throws IOException {
		tmp[tmpSize]=(byte)b;
		tmpSize++;
		if(tmpSize==2){
			write(tmp);
			tmpSize = 0;
		}
	}
	
	@Override
	public void addTextChangeListener(TextChangeListener listener) {
		listeners.add(listener);
		
	}

	@Override
	public int getCharCount() {
		return nrChars;
	}

	@Override
	public String getLine(int lineIndex) {
		System.out.printf("getLine(%d)=%s\n",lineIndex,lines[(start + lineIndex) % lines.length] );
		return lines[(start + lineIndex) % lines.length];
	}

	private int getRelOffset(int i){
		return offsets[(start + i) % offsets.length];
	}

	private int arrayFloorBinSearch(int start, int end, int toSearch){
		if(start >= end){
			return toSearch >= getRelOffset(start) ? start : -1;
		} else {
			int middle = (start + end) / 2;
			int cmp = toSearch - getRelOffset(middle);
			if(cmp == 0){
				return middle;
			} else if(cmp < 0){
				return arrayFloorBinSearch( start, middle-1, toSearch);
			} else {
				int res = arrayFloorBinSearch( middle+1, end, toSearch);
				if(res == -1){
					return middle;
				} else {
					return res;
				}
			}
		}
	}
	
	@Override
	public int getLineAtOffset(int offset) {
		
		int realOffset = offsets[start] + offset;
		System.out.printf("getLineAtOffset(%d)=%d\n",offset,arrayFloorBinSearch(0,nrLines-1,realOffset));
		return arrayFloorBinSearch(0,nrLines,realOffset);
	}

	@Override
	public int getLineCount() {
		System.out.printf("getLineCount()=%d\n",nrLines);
		return nrLines;
	}

	@Override
	public String getLineDelimiter() {
		return "\n";
	}

	@Override
	public int getOffsetAtLine(int lineIndex) {
		System.out.printf("getOffsetAtLine(%d)=%d\n",lineIndex,offsets[(start + lineIndex) % lines.length] - offsets[start]);
		return offsets[(start + lineIndex) % lines.length] - offsets[start];
	}

	@Override
	public String getTextRange(int start, int length) {
		throw new Error("getTextRange not implemented");
	}

	@Override
	public void removeTextChangeListener(TextChangeListener listener) {
		throw new Error("Remove not supported on text circular lines");
		
	}

	@Override
	public void replaceTextRange(int start, int replaceLength, String text) {
		throw new Error("Remove not supported on text circular lines");
		
	}

	@Override
	public void setText(String text) {
		throw new Error("Set text not supported on text circular lines");
		
	}
	
	
	private void sendTextChanging(final TextChangingEvent e){
		for(TextChangeListener l : listeners){
			l.textChanging(e);
		}
	}
	
	private void sendTextChanged(){
		final TextChangedEvent e = new TextChangedEvent(this);
			for(TextChangeListener l : listeners){
				l.textChanged(e);
			}
			
	}
	
	public static void main(String[] argv) throws IOException{
		ByteArrayOutputStream res =  new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(res, "UTF16");
		writer.write("\n\uD83C\uDC30\uD83C\uDC30\uD83C\uDC30\uD83C\uDC30\n\uD83C\uDC30\uD83C\uDC30\uD83C\uDC30\n\uD83C\uDC30\uD83C\uDC30");
//		writer.write("\n");
		writer.flush();
		byte[] jada = res.toByteArray();
		String[] bla = split(jada,0,jada.length);
	System.out.println(bla.length);
		for(String s : bla) {
			System.out.println("s: "  + s);
		}
		
	}
	
	static int mod(int a, int mod){
		int res = a % mod;
		if( res < 0){
			return res + mod;
		} else {
			return res;
		}
	}
}
