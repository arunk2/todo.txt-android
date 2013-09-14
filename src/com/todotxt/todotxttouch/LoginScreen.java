/**
 * This file is part of Todo.txt, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.todotxt.todotxttouch.remote.RemoteClient;
import com.todotxt.todotxttouch.util.Util;

public class LoginScreen extends Activity {
	final static String TAG = LoginScreen.class.getSimpleName();

	private TodoApplication m_app;
	private Button m_LoginButton;
	private BroadcastReceiver m_broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "LoginScreen onCreate");

		setContentView(R.layout.login);

		m_app = (TodoApplication) getApplication();

		// supposed to help with the banding on the green background
		findViewById(R.id.loginbackground).getBackground().setDither(true);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.todotxt.todotxttouch.ACTION_LOGIN");
		m_broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switchToTodolist();
			}
		};

		registerReceiver(m_broadcastReceiver, intentFilter);

		m_LoginButton = (Button) findViewById(R.id.login);
		m_LoginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startLogin();
			}
		});

		RemoteClient remoteClient = m_app.getRemoteClientManager()
				.getRemoteClient();

		if (remoteClient.isAuthenticated()) {
			switchToTodolist();
		}
	}

	private void switchToTodolist() {
		Intent intent = new Intent(this, TodoTxtTouch.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "LoginScreen onResume");

		finishLogin();
	}

	private void finishLogin() {
		RemoteClient remoteClient = m_app.getRemoteClientManager()
				.getRemoteClient();

		if (remoteClient.finishLogin() && remoteClient.isAuthenticated()) {
			Log.i(TAG, "LoginScreen: login complete: about to start the app.");

			Intent broadcastLoginIntent = new Intent(
					"com.todotxt.todotxttouch.ACTION_LOGIN");
			sendBroadcast(broadcastLoginIntent);
		} else {
			Log.i(TAG, "LoginScreen: not logged in. Showing login screen.");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(m_broadcastReceiver);
	}

	void startLogin() {
		final RemoteClient client = m_app.getRemoteClientManager()
				.getRemoteClient();

		if (!client.isAvailable()) {
			Log.d(TAG, "Remote service " + client.getClass().getSimpleName()
					+ " is not available; aborting login");

			Util.showToastLong(m_app, R.string.toast_login_notconnected);
		} else {
			client.startLogin();
		}
	}
}
