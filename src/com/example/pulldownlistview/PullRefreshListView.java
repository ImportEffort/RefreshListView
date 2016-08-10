package com.example.pulldownlistview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PullRefreshListView extends ListView implements OnScrollListener {

	private int mHeaderViewHeight;
	private float startY;
	private View mHeaderView;
	private static final int STATE_PULL_REFEASH = 1;// 下拉刷新
	private static final int STATE_RELEASE_REFEASH = 2;// 正在刷新
	private static final int STATE_REFEASHING = 3;// 正在刷新
	private int currentState;// 当前所处的状态
	private RotateAnimation mRaUp;
	private RotateAnimation mRaDown;
	private ImageView mIvHeader;
	private ProgressBar mPbHeader;
	private TextView mTvTitle;
	private TextView mTvTime;
	private onRefreshLisener mLisener;
	private boolean isRefresh;
	private boolean isLoadMore;

	private View mFooterView;
	private int mFooterViewHeight;

	public PullRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeaderView();
		initFooterView();
	}

	public PullRefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public PullRefreshListView(Context context) {
		this(context, null);
	}

	/**
	 * 初始化上拉加载的view
	 */
	private void initFooterView() {
		mFooterView = View.inflate(getContext(), R.layout.footer_view, null);
		mFooterView.measure(0, 0);
		mFooterViewHeight = mFooterView.getMeasuredHeight();
		mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
		addFooterView(mFooterView);
		setOnScrollListener(this);
	}

	/**
	 * 初始化下拉刷新的view
	 */
	private void initHeaderView() {

		mHeaderView = View.inflate(getContext(), R.layout.header_view, null);
		mIvHeader = (ImageView) mHeaderView
				.findViewById(R.id.iv_pull_list_header);
		mPbHeader = (ProgressBar) mHeaderView
				.findViewById(R.id.pb_pull_list_header);
		mTvTitle = (TextView) mHeaderView
				.findViewById(R.id.tv_pull_list_header_title);
		mTvTime = (TextView) mHeaderView
				.findViewById(R.id.tv_pull_list_header_time);
		// 测量view的高度，宽度
		mHeaderView.measure(0, 0);
		// 获取view的高度
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		// 隐藏headerview
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
		// 添加headerview
		addHeaderView(mHeaderView);
		initAnimation();
	}

	private void initAnimation() {
		mRaUp = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mRaUp.setDuration(200);
		mRaUp.setFillAfter(true);
		mRaDown = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRaDown.setDuration(200);
		mRaDown.setFillAfter(true);

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isRefresh) {
			return super.onTouchEvent(ev);
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = ev.getY();
			int dy = (int) (moveY - startY);
			int paddingY = -mHeaderViewHeight + dy;
			// 当可见的条目是第一个条目时候允许下拉刷新 moveY > startY的时候
			if (dy > 0 && getFirstVisiblePosition() == 0) {
				if (currentState != STATE_PULL_REFEASH && paddingY < 0) {
					currentState = STATE_PULL_REFEASH;
					refreshHeaderView();
				} else if (currentState != STATE_RELEASE_REFEASH
						&& paddingY >= 0) {
					currentState = STATE_RELEASE_REFEASH;
					refreshHeaderView();
				}
				mHeaderView.setPadding(0, paddingY, 0, 0);
			}

			break;
		case MotionEvent.ACTION_UP:
			if (currentState == STATE_PULL_REFEASH) {
				mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
			} else if (currentState == STATE_RELEASE_REFEASH) {
				currentState = STATE_REFEASHING;
				mHeaderView.setPadding(0, 0, 0, 0);
				refreshHeaderView();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void refreshHeaderView() {
		switch (currentState) {
		case STATE_PULL_REFEASH:
			mIvHeader.startAnimation(mRaDown);
			mIvHeader.setVisibility(View.VISIBLE);
			mPbHeader.setVisibility(View.INVISIBLE);
			mTvTitle.setText("下拉刷新");
			break;
		case STATE_RELEASE_REFEASH:
			mIvHeader.startAnimation(mRaUp);
			mIvHeader.setVisibility(View.VISIBLE);
			mPbHeader.setVisibility(View.INVISIBLE);
			mTvTitle.setText("松开刷新");
			break;
		case STATE_REFEASHING:
			mIvHeader.clearAnimation();
			mIvHeader.setVisibility(View.INVISIBLE);
			mPbHeader.setVisibility(View.VISIBLE);
			mTvTitle.setText("正在刷新");
			if (mLisener != null) {
				isRefresh = true;
				mLisener.onRefreshing();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 供外部设置监听刷新的方法
	 * 
	 * @param lisener
	 */
	public void setOnRrefeshLisener(onRefreshLisener lisener) {
		mLisener = lisener;
	}

	/**
	 * 供外部调用的刷新回调接口
	 * 
	 * @author wangsj
	 * 
	 */
	public interface onRefreshLisener {
		void onRefreshing();

		void onLoadingMore();
	}

	/**
	 * 当刷新完成后调用此方法
	 * 
	 * @param isSuccess
	 */
	public void setRefreshComplited(boolean isSuccess) {
		if (isSuccess) {
			currentState = STATE_PULL_REFEASH;
			String currentTime = getCurrentTime();
			mTvTime.setText(currentTime);
			refreshHeaderView();
		}
		isRefresh = false;
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
	}

	/**
	 * 获取当前当前时间
	 * 
	 * @return
	 */
	private String getCurrentTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		String currentTime = dateFormat.format(new Date());
		return currentTime;
	}

	/**
	 * 上拉加载的滑动监听
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 如果滑动状态等于空闲
		if (scrollState == SCROLL_STATE_IDLE) {
			// 如果滑动到listview最后一个view
			if (isLoadMore) {
				return;
			}
			if (getLastVisiblePosition() == getAdapter().getCount() - 1) {
				mFooterView.setPadding(0, 0, 0, 0);
				if (mLisener != null) {
					isLoadMore = true;
					// 设置数据选中最后一条
					setSelection(getAdapter().getCount() - 1);
					mLisener.onLoadingMore();
				}
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}

	/**
	 * 上拉加载完成的回调
	 * 
	 * @param b
	 */
	public void onLoadMoreComplete(boolean isSuccess) {
		if (isSuccess) {

			isLoadMore = false;
		}
		mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
	}
}
