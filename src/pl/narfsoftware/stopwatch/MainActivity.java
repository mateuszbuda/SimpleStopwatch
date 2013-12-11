package pl.narfsoftware.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	static final String TAG = "MainActivity";

	private static final String FONT_PATH = "unispace.ttf";

	static final String KEY_RUNNING = "running";
	static final String KEY_ELAPSED_TIME = "elapsed_time";

	static final int MILISECOND = 1;
	static final int SECOND = 1000 * MILISECOND;
	static final int MINUTE = 60 * SECOND;
	static final int HOUR = 60 * MINUTE;

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int MSG_RESET_TIMER = 3;
	final int MSG_RESET_AND_CONTINUE = 4;

	TextView textViewTimer;

	Stopwatch timer = new Stopwatch();
	long time = 0L;
	boolean running = false;
	int refreshRate = 2 * MILISECOND;

	static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		textViewTimer = (TextView) findViewById(R.id.timer);
		textViewTimer.setTypeface(Typeface.createFromAsset(getAssets(),
				FONT_PATH));

		time = PreferenceManager.getDefaultSharedPreferences(this).getLong(
				KEY_ELAPSED_TIME, 0L);
		updateTimer(time);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_START_TIMER:
					timer.start(time);
					Log.d(TAG, "timer.start(" + time + ")");
					running = true;
					Log.d(TAG, "running = true");
					handler.sendEmptyMessage(MSG_UPDATE_TIMER);
					break;

				case MSG_UPDATE_TIMER:
					updateTimer(timer.getElapsedTime());
					handler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
							refreshRate);
					break;

				case MSG_STOP_TIMER:
					timer.stop();
					Log.d(TAG, "timer.stop()");
					handler.removeMessages(MSG_UPDATE_TIMER);
					break;

				case MSG_RESET_TIMER:
					updateTimer(0);
					handler.removeMessages(MSG_UPDATE_TIMER);
					timer.stop();
					break;

				case MSG_RESET_AND_CONTINUE:
					timer.start();
					handler.sendEmptyMessage(MSG_UPDATE_TIMER);
					break;

				default:
					break;
				}
			}
		};
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(KEY_ELAPSED_TIME, running ? timer.getElapsedTime()
				: time);
		outState.putBoolean(KEY_RUNNING, running);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		time = savedInstanceState.getLong(KEY_ELAPSED_TIME);
		running = savedInstanceState.getBoolean(KEY_RUNNING);

		if (running)
			handler.sendEmptyMessage(MSG_START_TIMER);
		else
			updateTimer(time);

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		handler.removeMessages(MSG_UPDATE_TIMER);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.edit()
				.putLong(KEY_ELAPSED_TIME,
						running ? timer.getElapsedTime() : time).commit();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void stopwatchStart(View view) {
		if (!running) {
			handler.sendEmptyMessage(MSG_START_TIMER);
		}
	}

	public void stopwatchStop(View view) {
		if (running) {
			running = false;
			Log.d(TAG, "running = false");
			time = timer.getElapsedTime();
			Log.d(TAG, "time = " + time);
			handler.sendEmptyMessage(MSG_STOP_TIMER);
		}
	}

	public void stopwatchReset(View view) {
		time = 0L;
		handler.sendEmptyMessage(running ? MSG_RESET_AND_CONTINUE
				: MSG_RESET_TIMER);
	}

	private void updateTimer(long milis) {
		long minutes = getMinutes(milis);
		if (minutes < 10)
			textViewTimer.setText(String.format("%1$01d:%2$02d.%3$03d",
					minutes, getSeconds(milis), getMiliseconds(milis)));
		else if (minutes < 100) {
			textViewTimer.setText(String.format("%1$02d:%2$02d.%3$02d",
					minutes, getSeconds(milis), getMiliseconds(milis)
							/ (10 * MILISECOND)));
			refreshRate = 10 * MILISECOND;
		} else if (minutes < 1000) {
			textViewTimer.setText(String.format("%1$03d:%2$02d.%3$01d",
					minutes, getSeconds(milis), getMiliseconds(milis)
							/ (100 * MILISECOND)));
			refreshRate = 50 * MILISECOND;
		} else {
			textViewTimer.setText(String.format("%1$04d:%2$02d", minutes,
					getSeconds(milis)));
			refreshRate = 200 * MILISECOND;
		}
	}

	private long getMiliseconds(long milis) {
		return (milis % SECOND);
	}

	private long getSeconds(long milis) {
		return (milis / SECOND) % (MINUTE / SECOND);
	}

	private long getMinutes(long milis) {
		return (milis / MINUTE);
	}
}
