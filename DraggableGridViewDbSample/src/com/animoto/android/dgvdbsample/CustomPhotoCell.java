package com.animoto.android.dgvdbsample;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.animoto.android.dgv.DraggableGridViewCell;
import com.animoto.android.dgv.DraggableGridViewCell.CellDataNotSetException;
import com.animoto.android.dgvdbsample.model.Photo;

public class CustomPhotoCell extends FrameLayout implements DraggableGridViewCell {
	public static final String CUSTOM_PHOTO_CELL_IDENTIFIER = "CUSTOM_PHOTO_CELL";
	
	Photo mPhoto = null;
	TextView cellTitle = null;
	TextView cellInfo = null;
	ImageView cellImage = null;
	public CustomPhotoCell(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		cellTitle = (TextView) this.findViewById(R.id.photo_cell_contents);
		//cellInfo = (TextView) this.findViewById(R.id.photo_cell_contents2);
		//cellImage = (ImageView)this.findViewById(R.id.photo_cell_image);

	}
	
	
	public boolean changeDataForCell(Object obj) {
		if (obj != null && obj instanceof Photo) {
			this.setPhoto((Photo)obj);
			return true;
		}
		
		return false;
	}
	
	protected void setPhoto(Photo p) {
		this.mPhoto = p;
		if (p != null && cellTitle != null) {
			cellTitle.setText("" + p.position);
			/**cellInfo.setText(p.fileName);
			int imgId = this.getContext().getResources().getIdentifier(p.fileName, "drawable", "com.animoto.android.dgvdbsample");
			cellImage.setImageDrawable(this.getContext().getResources().getDrawable(imgId));**/
		}
	}
	
	public Photo getPhoto() {
		return mPhoto;
	}
	
	
	public int getPositionInData() throws CellDataNotSetException {
		if (mPhoto == null) {
			throw new CellDataNotSetException("Custom photo cell doesn't have an underlying model object set");
		} else return mPhoto.position;
	}
	
	public String getConvertIdentifier() {
		return CUSTOM_PHOTO_CELL_IDENTIFIER;
	}

}
