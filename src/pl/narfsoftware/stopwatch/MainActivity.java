package pl.narfsoftware.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	static final String TAG = "MainActivity";

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int MSG_RESET_TIMER = 3;
	final int MSG_RESET_AND_CONTINUE = 4;

	TextView hours;
	TextView minutes;
	TextView seconds;
	TextView miliseconds;

	boolean running = false;
	Stopwatch timer = new Stopwatch();
	final int REFRESH_RATE = 1;

	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// hours = (TextView) findViewById(R.id.hours);
		// minutes = (TextView) findViewById(R.id.minutes);
		seconds = (TextView) findViewById(R.id.seconds);
		miliseconds = (TextView) findViewById(R.id.miliseconds);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_START_TIMER:
					timer.start();
					handler.sendEmptyMessage(MSG_UPDATE_TIMER);
					break;

				case MSG_UPDATE_TIMER:
					updateTimer(timer.getElapsedTime());
					handler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
							REFRESH_RATE);
					break;

				case MSG_STOP_TIMER:
					handler.removeMessages(MSG_UPDATE_TIMER);
					timer.stop();
					break;

				case MSG_RESET_TIMER:
					updateTimer(0);
					handler.removeMessages(MSG_UPDATE_TIMER);
					timer.stop();
					break;

				case MSG_RESET_AND_CONTINUE:
					handler.sendEmptyMessage(MSG_START_TIMER);
					break;

				default:
					break;
				}
			}
		};

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
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
			running = true;
			handler.sendEmptyMessage(MSG_START_TIMER);
		}
	}

	public void stopwatchStop(View view) {
		if (running) {
			running = false;
			handler.sendEmptyMessage(MSG_STOP_TIMER);
		}
	}

	public void stopwatchReset(View view) {
		if (!running)
			handler.sendEmptyMessage(MSG_RESET_TIMER);
		else
			handler.sendEmptyMessage(MSG_RESET_AND_CONTINUE);
	}

	private void updateTimer(long milis) {
		miliseconds.setText(String.format("%1$03d", getMiliseconds(milis)));
		seconds.setText(String.format("%1$02d", getSeconds(milis)));
		// minutes.setText(String.format("%1$2d", getMinutes(milis)));
		// hours.setText(String.format("%1$2d", getHours(milis)));
	}

	private long getMiliseconds(long milis) {
		return milis % 1000;
	}

	private long getSeconds(long milis) {
		return (milis / 1000) % 60;
	}

	private long getMinutes(long milis) {
		return (milis / 60000) % 60;
	}

	private long getHours(long milis) {
		return (milis / 3600000);
	}
}
