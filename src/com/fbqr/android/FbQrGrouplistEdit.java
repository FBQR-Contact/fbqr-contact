package com.fbqr.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.fbqr.android.FbQrContactlist.ContactView;
import com.fbqr.android.FbQrContactlist.FbQrArrayAdapter;
import com.fbqr.android.FbQrContactlist.ViewHolder;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FbQrGrouplistEdit extends FbQrGrouplist {
	/** Called when the activity is first created. */
	private FbQrDatabase db=null;
	private ArrayAdapter<ContactView>  adapList=null;
	private ArrayList<ContactView> contactList=null,searchList=null;
	private Button delBtn,updateBtn,slectBtn;
	private static boolean isSelectAll = false;
	private final static String PATH = "/data/data/com.fbqr.android/files/"; 
	private SOAPConnected mSoap = new SOAPConnected(FbQrGrouplistEdit.this);
	private EditText filterText;
	private int posTextLength=0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		db=new FbQrDatabase(this);
		
		//UI
		setContentView(R.layout.contactlayout_edit);		
		
		filterText = (EditText) findViewById(R.id.searchfield);
		filterText.addTextChangedListener(filterTextWatcher);		
		filterText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		
		delBtn = (Button) findViewById(R.id.deleteBtn);
		updateBtn = (Button) findViewById(R.id.updateBtn);
		slectBtn = (Button) findViewById(R.id.selectBtn);
		filterText = (EditText) findViewById(R.id.searchfield);
		
		slectBtn.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {		
	    		   int _size = contactList.size();
	    		   if(isSelectAll){		   		    	
		    	        for (int i = 0; i < _size; i++) {
		    	        	contactList.get(i).setChecked(false);
		    	        } 
		    	        isSelectAll=false;
		    	        slectBtn.setText("Select All");
	    		   }else{		    			   
		    	        for (int i = 0; i < _size; i++) {
		    	        	contactList.get(i).setChecked(true);
		    	        } 
		    	        isSelectAll=true;
		    	        slectBtn.setText("unSelect All");
	    		   }
	    		   adapList.notifyDataSetChanged();
	    		   setListAdapter(adapList);
			    }
	        });
		
		delBtn.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {		    		   
	    		    int _size = contactList.size();
	    	        for (int i = 0; i < _size; i++) {
	    	          boolean isChecked = contactList.get(i).isChecked();
	    	          if(isChecked==true){
	    	        	  db.removeGroup(contactList.get(i).getGid());
	    	        	  File img=new File(PATH+contactList.get(i).getGid()+".PNG");
	    	        	  if(img.exists()) img.delete();
	    	          }
	    	          
	    	        } 	 		    	        
	    	        //adapList.notifyDataSetChanged();
	    	        Bundle stats = new Bundle();
	    			Intent intent = new Intent();
	    			stats.putString("MODE", "delete");
	    			intent.putExtras(stats);
	    			
	    			setResult(RESULT_OK, intent);
	    		    finish();
			    }
	        });
		updateBtn.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {  
	    		   ArrayList<String> uidList=new ArrayList<String>();
	    		   ArrayList<String> pwdList=new ArrayList<String>();
	    		   int _size = contactList.size() - 1;
	    		   
	    	        for (int i = 0; i < _size; i++) {
	    	          boolean isChecked = contactList.get(i).isChecked();
	    	          if (isChecked == true) {
	    	        	  if(contactList.get(i).getGid()==null) continue;
	    	        	  uidList.add(contactList.get(i).getGid());
	    	        	  String password = db.getProfile(contactList.get(i).getGid()).password;
	    	        	  if(password==null) password="";
	    	        	  pwdList.add(password);
	    	          }
	    	        } 	 
	    	        
	    	        String[] uids=new String[uidList.size()];
	    	        String[] pwds=new String[uidList.size()];
	    	        for(int i=0;i<uidList.size();i++){
	    	        	uids[i]=uidList.get(i);
	    	        	pwds[i]=pwdList.get(i);		    	        	
	    	        }
	    	        
	    	        Bundle stats = new Bundle();
	    			Intent intent = new Intent();
	    			stats.putString("MODE","update");
	    			stats.putStringArray("uids", uids);
	    			stats.putStringArray("pwds", pwds);
	    			intent.putExtras(stats);		    			
	    			setResult(RESULT_OK, intent);
	    		    finish();
			    }
	        });			
		db.close();
	}
	
	
	public void onStart(){
		super.onStart();
		db=new FbQrDatabase(this);
		reLoading();
	}
	
	public void onResume(){
		super.onResume();
		db=new FbQrDatabase(this);
		reLoading();
	}
	
	 public void onPause(){
		 super.onPause();
		 db.close();
	 }
	 
	 public void onStop(Bundle savedInstanceState) {
	       super.onStop();
	       db.close();
	 }
	
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
	}
	 
	 private TextWatcher filterTextWatcher = new TextWatcher() {
		 
			public void afterTextChanged(Editable s) {
			}
		 
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		 
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				 String name,gid;
				 int pos;
				 int textlength=filterText.getText().length();
				 if(textlength==0){
					 reLoading();
					 return;				 
				 }
				 else{ 
					 if(posTextLength>=textlength)
						 reLoading();
				 }
				 searchList = new ArrayList<ContactView>();  
				 for(int i=0;i<adapList.getCount();i++){
					 name=adapList.getItem(i).name;
					 if(textlength<=name.length()){
						 if(name.toLowerCase().indexOf(filterText.getText().toString().toLowerCase())>=0){
							 gid = adapList.getItem(i).gid;
							 searchList.add(new  ContactView(name,gid,adapList.getItem(i).website,adapList.getItem(i).uids,adapList.getItem(i).pos));
							 
						 }
					 }
				 }
				 contactList=searchList;
				 adapList=new FbQrArrayAdapter(FbQrGrouplistEdit.this,contactList);
				 FbQrGrouplistEdit.this.setListAdapter(adapList);
				 adapList.notifyDataSetChanged();
				 posTextLength=textlength;
			}
		};
	
		private void reLoading(){		
			//start activity code
			Cursor cursor=db.getGroup();
			FbQrGroup group;
	     	contactList = new ArrayList<ContactView>();  
	     	while (cursor.moveToNext()) {     		  
	     		group=db.getGroupData(cursor);
	     		contactList.add(new  ContactView(group.name,group.gid,group.website,group.uids,group.position));
		    }     
	     	db.close();
	     	adapList=new FbQrArrayAdapterEdit(this,contactList);
	     	this.setListAdapter(adapList);
	     	adapList.notifyDataSetChanged();
	     	startManagingCursor(cursor);
		}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {		
		super.onListItemClick(l, v, position, id);
	}
	
	public static class FbQrArrayAdapterEdit extends ArrayAdapter<ContactView> {
		private final Activity context;
		
		private LayoutInflater inflater;  		
		private final List<ContactView> contactLists;
		
		public FbQrArrayAdapterEdit(Activity context,List<ContactView> contactLists) {			
			super(context, R.layout.rowlayout,contactLists);
			inflater = LayoutInflater.from(context) ;
			this.context=context;
			this.contactLists=contactLists;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ContactView contact = contactLists.get(position);  
		    
		   convertView = inflater.inflate(R.layout.rowlayout_chkbox, null); 
		   
		   
		   final TextView textView = (TextView) convertView.findViewById( R.id.label );  
		   CheckBox checkBox = (CheckBox) convertView.findViewById( R.id.CheckBox01 );  
	       ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
	       	
	          
	        File img=new File(PATH+contact.getGid()+".PNG");
		    if(img.exists())
		         imageView.setImageBitmap(BitmapFactory.decodeFile(img.getPath()));    

		    convertView.setTag( new ViewHolder(textView,checkBox) );  
		  			    
		    checkBox.setOnClickListener( new View.OnClickListener() {  
		    	public void onClick(View v) {  
		            CheckBox cb = (CheckBox) v ;  		            
		            contact.setChecked( cb.isChecked() ); 
		            textView.setText(contact.getName());
		          }  
		    });          
		     
		      checkBox.setTag( contact );   
		        
		      // Display planet data  
		      checkBox.setChecked( contact.isChecked() );  
		      textView.setText( contact.getName() );        
		        
		      return convertView;  
		    } 			
	}			
}