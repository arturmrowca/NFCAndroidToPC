package tum.developer.hcesettings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EnterAccountDetailsActivity extends Activity implements OnClickListener{
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	public Button regBut;
	
	// Values for Username and password at the time of the login attempt.
	private String mUser;
	private String mPassword;

	// UI references.
	private EditText mUserView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//0. load
		String prevUsername="";
		String prevPassword="";
		try{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		prevUsername=preferences.getString("u2219aAsgiLPOo","");
		prevPassword=preferences.getString("p09ki8dieik87n","");}catch(Exception e){prevUsername="";prevPassword="";}
		
		setContentView(R.layout.activity_enter_account_details);
		
		//1. Fuege sofern vorhanden Logindaten ein
		mUserView = (EditText) findViewById(R.id.email);
		mUserView.setText(prevUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setText(prevPassword);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							saveCredentials();
							return true;
						}
						return false;
					}
				});

		//2. Setze Listener fuer Speichern Button
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		findViewById(R.id.saveAdButton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						saveCredentials();
					}
				});
		
		//3. Listener Registrieren Button
		regBut= (Button) findViewById(R.id.registerButton);
		try
		{
		regBut.setOnClickListener(this);
		}
		catch(Exception e)
		{
			Log.d("Tag",e.toString());
		}
		
							
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.enter_account_details, menu);
		return true;
	}

	/**
	 * Speichere Username und Passwort falls gueltig
	 **/
	public void saveCredentials() {
		if (mAuthTask != null) {
			return;
		}

		//1. Resete Fehler
		mUserView.setError(null);
		mPasswordView.setError(null);

		//2. Werte zum Zeitpunkt des Login Versuchs
		mUser = mUserView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		//3. Check ob Passwort gueltiges Format hat
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		//4. Check ob Benutzername gueltiges Format hat
		if (TextUtils.isEmpty(mUser)) {
			mUserView.setError(getString(R.string.error_field_required));
			focusView = mUserView;
			cancel = true;
		} 

		if (cancel) {
			// 5. Fehler entdeckt, kein Login, fokussiere fehlerhafte Box
			focusView.requestFocus();
		} else {
			//6. Zeige Spinner, und speichere im Hintergrund die Daten
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
			
			
			//7. Speichere
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("u2219aAsgiLPOo",mUser);
			editor.putString("p09ki8dieik87n",mPassword);
			editor.commit();
			
		}
	}

	 /**
	 * Zeigt den Progress Spinner 
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// Simulate network access.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
			
			Context context = getApplicationContext();
			CharSequence text = "Username saved";
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	@Override
	public void onClick(View v) {
		
			if(v==regBut)
			{
			Intent menuIntent = new Intent(this,RegistringActivity.class);
			startActivity(menuIntent);	
			}

	}
}
