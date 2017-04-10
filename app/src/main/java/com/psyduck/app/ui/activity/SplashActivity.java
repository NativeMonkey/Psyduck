package com.psyduck.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import com.hyphenate.chat.EMClient;
import com.psyduck.app.R;
/**
 * 开屏页
 *
 */
public class SplashActivity extends AppCompatActivity {

	private static final int sleepTime = 2000;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.activity_splash);
		super.onCreate(arg0);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
			}
		}, sleepTime);
	}

	@Override
	protected void onStart() {
		super.onStart();

		new Thread(new Runnable() {
			public void run() {
				if (EMClient.getInstance().isLoggedInBefore()) {
					// auto login mode, make sure all group and conversation is loaed before enter the main screen
					long start = System.currentTimeMillis();
					EMClient.getInstance().chatManager().loadAllConversations();
					EMClient.getInstance().groupManager().loadAllGroups();
					long costTime = System.currentTimeMillis() - start;
					//wait
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					startActivity(new Intent(SplashActivity.this, HomeActivity.class));
					finish();
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					finish();
				}
			}
		}).start();

	}
	
	/**
	 * get sdk version
	 */
	private String getVersion() {
	    return EMClient.getInstance().VERSION;
	}
}
