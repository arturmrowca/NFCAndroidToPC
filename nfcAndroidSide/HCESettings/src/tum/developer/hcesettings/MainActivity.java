package tum.developer.hcesettings;

import tum.developer.hcesettings.IsoDepTransceiver.OnMessageReceived;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener,OnMessageReceived, ReaderCallback{

	private Button openSettingsButton;
	
	private NfcAdapter nfcAdapter;
	private ListView listView;
	private IsoDepAdapter isoDepAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//1. Beim Start der App öffne diese Activity
		setContentView(R.layout.activity_main);
		
		//2. Erstelle Listener für meine Button
		openSettingsButton= (Button) findViewById(tum.developer.hcesettings.R.id.buttonOpenSettings);
		openSettingsButton.setOnClickListener(this);
		
		//3. NFC
		isoDepAdapter = new IsoDepAdapter(getLayoutInflater());
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View invokingView) {
		
		//1. Falls es sich um den Settings Button gehandelt hat öffne Einstellungen Menu
		if (invokingView==openSettingsButton)
		{
			Intent menuIntent = new Intent(MainActivity.this,SettingsActivity.class);
			//Intent menuIntent = new Intent(MainActivity.this,RegistringActivity.class);
			startActivity(menuIntent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
				null);
	}

	@Override
	public void onPause() {
		super.onPause();
		nfcAdapter.disableReaderMode(this);
	}

	@Override
	public void onTagDiscovered(Tag tag) {
		IsoDep isoDep = IsoDep.get(tag);
		IsoDepTransceiver transceiver = new IsoDepTransceiver(isoDep, this);
		Thread thread = new Thread(transceiver);
		thread.start();
	}

	@Override
	public void onMessage(final byte[] message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				isoDepAdapter.addMessage(new String(message));
			}
		});
	}

	@Override
	public void onError(Exception exception) {
		onMessage(exception.getMessage().getBytes());
	}
	
	public void openSettingsClicked(MenuItem item){
		
		Intent menuIntent = new Intent(MainActivity.this,SettingsActivity.class);
		startActivity(menuIntent);
		
	}
}
