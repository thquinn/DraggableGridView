package com.animoto.android.dgv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DraggableGridViewAdapter extends BaseAdapter {
	
	protected static final int RECYCLE_LIMIT_PER_IDENTIFIER = 20; //Maximum size of the recylce bin of cells for any given cell identifier. 

	private Hashtable<String, ArrayList<DraggableGridViewCell>> mRecycleBin; 
	private Context mContext; 
	
	public DraggableGridViewAdapter(Context c) {
		super();
		this.mContext = c;
		this.mRecycleBin = new Hashtable<String, ArrayList<DraggableGridViewCell>>();
	}
	
	public void recycleCell(DraggableGridViewCell cell) {
    	if (cell instanceof DraggableGridViewCell && ((DraggableGridViewCell)cell).getConvertIdentifier() != null) {
    		String convertIdentifier = ((DraggableGridViewCell)cell).getConvertIdentifier();
    		ArrayList<DraggableGridViewCell> convertSet = mRecycleBin.get(convertIdentifier);
    		if (convertSet == null) {
    			convertSet = new ArrayList<DraggableGridViewCell>();
    			mRecycleBin.put(convertIdentifier, convertSet);
    		}
    		
    		if (convertSet.size() <= RECYCLE_LIMIT_PER_IDENTIFIER) convertSet.add(cell);
    	}
	}
	
	
	public DraggableGridViewCell getConvertibleCell(String convertIdentifier) {
		ArrayList<DraggableGridViewCell> availableCells = mRecycleBin.get(convertIdentifier);
		
		if (availableCells == null || availableCells.size() <= 0) return null;
		
		return availableCells.remove(availableCells.size()-1);
	}
	
	
	protected Context getContext() {
		return mContext;
	}

}
