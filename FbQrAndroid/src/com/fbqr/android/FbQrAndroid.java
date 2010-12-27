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

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import com.facebook.android.*;
import com.facebook.android.Facebook.DialogListener;

public class FbQrAndroid extends Activity{
		public static final String APP_ID = "146472442045328";
		
		private Facebook facebook;
		private AsyncFacebookRunner mAsyncRunner;
		Button button,button2,button3,button4;
		Intent intent;
		private TextView tv;
		SOAPConnected mSoap = new SOAPConnected(FbQrAndroid.this);
		FbQrDatabase db=new FbQrDatabase(this);
		ReadFbQR readQR=new ReadFbQR() ;
	   /** Called when the activity is first created. */
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	       
	       //Facebook
	       facebook = new Facebook(APP_ID);		    
	       mAsyncRunner = new AsyncFacebookRunner(facebook);      
	       
	       
	       	
	       
	       //GUI
	       setContentView(R.layout.main);
	       
	       tv=(TextView) findViewById(R.id.TextView01);
	       
	       button = (Button) findViewById(R.id.Button01);
	       button2 = (Button) findViewById(R.id.Button02);
	       button3 = (Button) findViewById(R.id.Button03);
	       button4 = (Button) findViewById(R.id.Button04);
	       	       
	       
	       button.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {
			        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			        startActivityForResult(intent, 2);
			    }
	        });
	        
	        
	       button2.setOnClickListener(new OnClickListener() {
		    	   public void onClick(View v) {
		    		   Intent intent = new Intent(v.getContext(), FbQrWeb.class);
		    		   intent.putExtra("access_token", facebook.getAccessToken());
		    		   
		    		   startActivity(intent);
				    }
		        });
		     
	       button3.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {
	    		   Intent intent = new Intent(v.getContext(), FbQrContactlist.class); 
	     		   startActivity(intent); 
			    }
	        });   
	       
	       button4.setOnClickListener(new OnClickListener() {
	    	   public void onClick(View v) {
	    		   facebook.authorize(FbQrAndroid.this,new String[] {"offline_access"},new AuthorizeListener());
	    		   
			    }
	        });    
	       
	       
	       	//Start Code
	       	if(isOnline()) 	tv.setText("Internet Online");
	       	else tv.setText("Internet Offline");
	       
	        	
	     	//db.delete();
	     	FbQrProfile data = new FbQrProfile();
	     	data.id="1";
	     	data.phone="6683199838";
	     	data.email="3";
	     	data.name="blahblah";
	     	db.addData(data);
	     	data.id="2";
	     	data.phone="6683199838";
	     	data.email="3";
	     	data.name="blahblah2";
	     	db.addData(data);
	     	data.id="3";
	     	data.phone="6683199838";
	     	data.email="3";
	     	data.name="blahblah3";
	     	db.addData(data);
		    tv.setText(db.showData());
		    db.close();	
	
	     }
	   
	   
	     
	   
	   public boolean isOnline() {
		   
		    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}
	   
	   public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
		  facebook.authorizeCallback(requestCode, resultCode, data);
		  if (requestCode == 2) {
			  	//setContentView(tv);
			  	if (resultCode == RESULT_OK) {
		           String contents = data.getStringExtra("SCAN_RESULT");		           
		            //String format = data.getStringExtra("SCAN_RESULT_FORMAT");
		            //Toast.makeText(FbQrAndroid.this, contents+"/"+format, Toast.LENGTH_LONG).show();
		            readQR.read(contents);
		            if(readQR.type.matches("multiqr_n")){
		            	//Toast.makeText(FbQrAndroid.this, readQR.qrid, Toast.LENGTH_LONG).show();
		            	mSoap.getMulti(readQR.qrid, facebook.getAccessToken(),"",new getData());
		            	//mSoap.getMulti("15", facebook.getAccessToken(),"55555",new getData());
		            }		            
		            else{
		            	if(readQR.profileList.size()<1) Toast.makeText(FbQrAndroid.this, "FbQr not support this QRcode", Toast.LENGTH_LONG).show();
		            	else{
			            	if(readQR.profileList.get(0).id!=null){
			            		Toast.makeText(FbQrAndroid.this, "Single", Toast.LENGTH_LONG).show();
				            	String[] uid=new String[readQR.profileList.size()];
				            	for(int i=0;i<readQR.profileList.size();i++){
				            		uid[i]=readQR.profileList.get(i).id;
				            	}
				            	mSoap.getFriendInfo(uid, facebook.getAccessToken(),new getData());
			            	}
			            	else{
			            		if(readQR.type.matches("single")||readQR.type.matches("data")){
				            		FbQrProfile x;
						            String display=readQR.profileList.size()+"\n"+contents+"\n\n";
						            for(int i=0;i<readQR.profileList.size();i++){
						            	x=readQR.profileList.get(i);
						            	display+=x.show()+"\n";
						            }
							        tv.setText(display);			
			            		}
			            	}
		            	}
		            }
		                  
		            // Handle successful scan
		        } else if (resultCode == RESULT_CANCELED) {
		            // Handle cancel
		        }
		   }
		 		
		   // ... anything else your app does onActivityResult ...
		}
	   
	   class AuthorizeListener implements DialogListener {
		   
		   public void onComplete(Bundle values) {
			   Toast.makeText(FbQrAndroid.this, "logined", Toast.LENGTH_LONG).show();
			  //mSoap.getMulti("15", facebook.getAccessToken(),"55555");
			  // mSoap.getFriendInfoBypass("100001036241534", facebook.getAccessToken(), "5555");
			   
			   //String[] uid={"100001036241534","100000925243158"};
			   //mSoap.getFriendInfo(uid, facebook.getAccessToken());
			  // mSoap.getPhoneBook(facebook.getAccessToken(),new getData());
			   
			  // mAsyncRunner.request("me", new SampleRequestListener());
			  
			  // browser.clearCache(isFinishing());
			  /* browser.loadUrl("http://fbqr.tkroputa.in.th");
			   browser.getSettings().setJavaScriptEnabled(true);*/
		   }
		   public void onError(DialogError e)
		    {
			   Toast.makeText(FbQrAndroid.this, "login fail...", Toast.LENGTH_LONG).show();
			   System.out.println("Error: " + e.getMessage());
		    }

		    public void onFacebookError(FacebookError e)
		    {
		    	Toast.makeText(FbQrAndroid.this, "login fail...", Toast.LENGTH_LONG).show();
		        System.out.println("Error: " + e.getMessage());
		    }

		    
		    public void onCancel()
		    {
		    	Toast.makeText(FbQrAndroid.this, "login fail...", Toast.LENGTH_LONG).show();
		    }
		 }
	   
	   public class getData extends SoapConnectedListener{
			@Override
			public void onComplete(final ArrayList<FbQrProfile> response) {
				// TODO Auto-generated method stub
				try {
					
					FbQrAndroid.this.runOnUiThread(new Runnable() {
	                    public void run() {
	                       FbQrProfile x;
	     				   String display=""+response.size();
	     				   for(int i=0;i<response.size();i++){
	     					    db.addData(response.get(i));
	     					    x=response.get(i);
	     					    if(x.display!=null) saveDisplay(x.display,x.id);
	     			           	display+=x.show()+"\n";
	     			       }
	     				   tv.setText(display);	                   
	                    }
	                });
				 	
				} catch (Exception e) {
					Log.w("getData", e.toString());
	            } 
			}		   
	   }
	   
	   public class SampleRequestListener extends BaseRequestListener {

	        public void onComplete(final String response) {
	            try {
	                // process the response here: executed in background thread
	                Log.d("Facebook-Example", "Response: " + response.toString());
	                JSONObject json = Util.parseJson(response);
	                final String name = json.getString("name");

	                // then post the processed result back to the UI thread
	                // if we do not do this, an runtime exception will be generated
	                // e.g. "CalledFromWrongThreadException: Only the original
	                // thread that created a view hierarchy can touch its views."
	                FbQrAndroid.this.runOnUiThread(new Runnable() {
	                    public void run() {
	                        tv.setText("Hello there, " + name + "!");
	                    }
	                });
	            } catch (JSONException e) {
	                Log.w("Facebook-Example", "JSON Error in response");
	            } catch (FacebookError e) {
	                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
	            }
	        }
	    }
	   
		private void saveDisplay(String fileUrl,String uid){
			String PATH = "/data/data/com.fbqr.android/files/";  
			
			URL myFileUrl =null; 
			Bitmap bmImg;
			try {
				myFileUrl= new URL(fileUrl);
			} catch (MalformedURLException e) {		
				e.printStackTrace();
			}
			try {
				HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
				conn.setDoInput(true);
				conn.connect();
				//int length = conn.getContentLength();
				//int[] bitmapData =new int[length];
				//byte[] bitmapData2 =new byte[length];
				InputStream is = conn.getInputStream();
			
				bmImg= BitmapFactory.decodeStream(is);
				
				OutputStream outStream = null;
				File dir = new File(PATH);
				dir.mkdirs();
				File file = new File(PATH,uid+".PNG");
				   
				outStream = new FileOutputStream(file);
				bmImg.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				outStream.flush();
				outStream.close();
				   
				Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}