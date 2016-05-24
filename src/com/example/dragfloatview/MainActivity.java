package com.example.dragfloatview;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	FloatView view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		view = new FloatView(this);
		view.create(true).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		view.destroy();
	}
}
