package tum.developer.hcesettings;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegistringActivity extends Activity implements OnClickListener{

	
	private String firstName, lastName, username, email, emailConfirmation, pass, passConfirmation;
	private Button absendenButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registring);
		

	
		Log.d("Tag","TETE");
	
		//1. Erstelle Listener für meinen Button
		absendenButton= (Button) findViewById(tum.developer.hcesettings.R.id.sendItButton);
		absendenButton.setOnClickListener(this);
		
		Log.d("Tag","TETasdE");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registring, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		if(v==absendenButton)
		{
			EditText v1 = (EditText) findViewById(R.id.firstName);
			firstName=v1.getText().toString();
			
			EditText v2 = (EditText) findViewById(R.id.lastName);
			lastName=v2.getText().toString();
			
			EditText v3 = (EditText) findViewById(R.id.userName);
			username=v3.getText().toString();
			
			EditText v4 = (EditText) findViewById(R.id.email);
			email=v4.getText().toString();
			
			EditText v5 = (EditText) findViewById(R.id.emailConfirm);
			emailConfirmation=v5.getText().toString();
			
			EditText v6 = (EditText) findViewById(R.id.password);
			pass=v6.getText().toString();
			
			EditText v7 = (EditText) findViewById(R.id.passwordConfirm);
			passConfirmation=v7.getText().toString();
			
			//TODO Check Data and process it via Server
			
			
			
			
			
			
			
			
			
		}		
	}
}
