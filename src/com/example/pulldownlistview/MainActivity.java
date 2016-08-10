package com.example.pulldownlistview;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.example.pulldownlistview.PullRefreshListView.onRefreshLisener;

public class MainActivity extends Activity {
	private int i;
	private int j;
	private PullRefreshListView mLv;
	private ArrayList<String> list = new ArrayList<String>();
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mLv = (PullRefreshListView) findViewById(R.id.lv);
		for (int i = 0; i < 15; i++) {
			list.add(i + "");
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.items, list);

		mLv.setAdapter(adapter);
		mLv.setOnRrefeshLisener(new onRefreshLisener() {

			@Override
			public void onRefreshing() {

				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add("下拉刷新加载的数据" + i++ + "");
						adapter.notifyDataSetChanged();
						mLv.setRefreshComplited(true);
					}
				}, 3000);
			}

			@Override
			public void onLoadingMore() {
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						list.add("上拉加载的数据" + j++ + "");
						adapter.notifyDataSetChanged();
						mLv.onLoadMoreComplete(true);
					}
				}, 3000);
			}
		});
	}
}
