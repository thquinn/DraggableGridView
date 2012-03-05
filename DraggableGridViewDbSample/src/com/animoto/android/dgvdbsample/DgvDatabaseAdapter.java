package com.animoto.android.dgvdbsample;


import java.sql.SQLException;

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

public class DgvDatabaseAdapter extends BaseAdapter {
	protected Context mContext; 
	
	public DgvDatabaseAdapter(Context c) {
		super();
		this.mContext = c;
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
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Photo p = ORMHelper.photoDao.getPhotoWithPosition(position);
		CustomPhotoCell cellView = (CustomPhotoCell)inflater.inflate(R.layout.photo_cell, null);
		if (p != null) cellView.setPhoto(p);
		
		return cellView;
	}

}
