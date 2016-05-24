package com.example.dragfloatview;

import android.content.Context;

public class FloatView extends AbsDraggedFloatView<String> {

	public FloatView(Context activity) {
		super(activity);
		setImageResource(R.drawable.android);
	}

	@Override
	public void applyData(String data) {
	}

}
