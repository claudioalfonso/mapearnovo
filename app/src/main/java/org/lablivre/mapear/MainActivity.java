package org.lablivre.mapear;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import org.mixare.data.DataSourceStorage;

import java.io.File;

public class MainActivity extends AppCompatActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		DataSourceStorage.init(this);
		String dir = Environment.getExternalStorageDirectory() + "/mapear";
		File f = new File(dir);
		if(!f.isDirectory()) {
			File newdir = new File(dir);
			newdir.mkdirs();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public void Mapa(View view){
		startActivity(new Intent(this, MapsActivity.class));
	}

	public void Lista(View view){
		startActivity(new Intent(this, ListActivity.class));
	}

	public void QRcode(View view) {
        startActivity(new Intent(this, QrCodeActivity.class));
	}

	public void Form(View view) {
		startActivity(new Intent(this, FormActivity.class));
	}

}
