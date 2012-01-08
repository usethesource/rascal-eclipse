package org.rascalmpl.eclipse.console.outputconsole;

public class NewLineIndexes {
	
	int[] elems;
	int start, end;
	/* index of the first element a than is smaller than the previous element */
	int firstWrappedAround; 
	int nrElems;
	
	public NewLineIndexes(int initialSize) {
		start = end = nrElems = 0;
		elems = new int[initialSize];
		firstWrappedAround = 0;
	}
	
	static int mod(int a, int mod){
		int res = a % mod;
		if( a < 0){
			return res + mod;
		} else {
			return res;
		}
	}
	
	private void doubleArray(){
		int[] old = elems;
		elems=  new int[elems.length * 2];
		for(int i = 0 ; i < old.length ; i++){
			elems[i] = old[(start + i) % old.length];
		}
		start = 0;
		end = old.length;
	}
	
	public int size(){
		return nrElems;
	}
	
	public void append(int a){
		if(nrElems == elems.length){
			doubleArray();
		}
		elems[end] = a;
		nrElems++;
		if(nrElems != 1 && a < elems[mod(end-1,elems.length)] ){
			firstWrappedAround = end;
		}
		end = (end + 1) % elems.length;
	}
	
	public void removeHead(){
//		System.out.printf("Removing head");
		int oldStart = start;
		start = (start + 1) % elems.length;
		if(oldStart == firstWrappedAround){
			firstWrappedAround = start;
		}
		nrElems--;
	}

	public int getRelIndex(int i){
		int res = elems[mod(start + i,elems.length)];
		System.err.printf("Get rel index %d %d\n", i, res);
		return res;
	}
	
	private int arrayFloorBinSearch(int start, int end, int toSearch){
		if(start >= end){
			return toSearch >= getRelIndex(start) ? start : -1;
		} else {
			int middle = (start + end) / 2;
			int cmp = toSearch - getRelIndex(middle);
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
	
	public int getLineContaining(int j){
		if(nrElems == 0){
			return -1;
		}
		int beforeWrappedAround = mod(firstWrappedAround - 1, nrElems);
		if(j >= getRelIndex(0)){
			return mod(arrayFloorBinSearch(0, beforeWrappedAround, j) -start, nrElems);
		} else {
			return mod(arrayFloorBinSearch(beforeWrappedAround + 1, size(),j) - start,nrElems);
		}
	}
}
