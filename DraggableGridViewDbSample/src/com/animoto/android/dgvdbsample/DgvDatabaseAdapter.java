package com.animoto.android.dgvdbsample;


import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;

import com.animoto.android.dgv.DraggableGridViewAdapter;
import com.animoto.android.dgv.DraggableGridViewCell;
import com.animoto.android.dgvdbsample.model.ORMHelper;
import com.animoto.android.dgvdbsample.model.Photo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DgvDatabaseAdapter extends DraggableGridViewAdapter {

	public DgvDatabaseAdapter(Context c) {
		super(c);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
			try {
				return (int)ORMHelper.photoDao.countOf(); //@TODO - more robust casting here. 
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Photo p = ORMHelper.photoDao.getPhotoWithPosition(position);
		
		CustomPhotoCell cellView = (CustomPhotoCell)this.getConvertibleCell(CustomPhotoCell.CUSTOM_PHOTO_CELL_IDENTIFIER);
		if (cellView == null) cellView = (CustomPhotoCell)inflater.inflate(R.layout.photo_cell, null);
		else {
			Log.i("dgv", "Will be recycling cell with old position " + cellView.getPhoto().position + " in to new cell with position " + p.position);
		}
		if (p != null) cellView.setPhoto(p);
		
		return cellView;
	}

}
