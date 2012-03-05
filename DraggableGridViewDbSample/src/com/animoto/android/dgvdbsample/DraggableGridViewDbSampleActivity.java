package com.animoto.android.dgvdbsample;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.dgv.DraggableGridView;
import com.animoto.android.dgv.OnRearrangeListener;
import com.animoto.android.dgvdbsample.model.ORMHelper;
import com.animoto.android.dgvdbsample.model.Photo;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;


public class DraggableGridViewDbSampleActivity extends SherlockActivity implements ActionBar.OnNavigationListener {
	
	static Random random = new Random();
	static String[] words = "the of and a to in is be that was he for it with as his I on have at by not they this had are but from or she an which you one we all were her would there their will when who him been has more if no out do so can what up said about other into than its time only could new them man some these then two first may any like now my such make over our even most me state after also made many did must before back see through way where get much go well your know should down work year because come people just say each those take day good how long Mr own too little use US very great still men here life both between old under last never place same another think house while high right might came off find states since used give against three himself look few general hand school part small American home during number again Mrs around thought went without however govern don't does got public United point end become head once course fact upon need system set every war put form water took".split(" ");
	DraggableGridView dgv;
	Button button1, button2;
	ArrayList<String> poem = new ArrayList<String>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        // Remove text so the gallery items will fill the space
        getSupportActionBar().setTitle("");
        getSupportActionBar().setSubtitle("");
        
        dgv = ((DraggableGridView)findViewById(R.id.vgv));

        dgv.setAdapter(new DgvDatabaseAdapter(getBaseContext()));
        
        try {
			List<Photo> photos = ORMHelper.photoDao.queryForAll();
			Log.i("dgv", "Number of photos: " + photos.size());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        setListeners();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getSupportMenuInflater().inflate(R.menu.dgv_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) 
    {
        Log.i("dgv", "Selected: " + itemPosition);
       
        return true; 
    }
    
    
    private void setListeners()
    {

    }
    
    
}