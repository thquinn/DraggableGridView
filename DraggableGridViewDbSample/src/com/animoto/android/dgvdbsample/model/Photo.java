package com.animoto.android.dgvdbsample.model;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Photo")
public class Photo {
	@DatabaseField (generatedId = true)
	public int id;
	
	@DatabaseField 
	public int position;
	
	@DatabaseField
	public Date created;
	
	@DatabaseField
	public String title;
	
	@DatabaseField
	public String fileName;

}
