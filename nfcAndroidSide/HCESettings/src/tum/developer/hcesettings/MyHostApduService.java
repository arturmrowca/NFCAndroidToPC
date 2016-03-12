package tum.developer.hcesettings;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MyHostApduService extends HostApduService {

	public int selMode=0;
	
	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		//falls ich noch rechnen muss schicke antwort über sendResponseApdu
		//ALLER ERSTE MESSAGE WENN VERBINDUNG OK
		if (selectAidApdu(apdu)) {
			return DictInf.HAIDSelectionOkid;
		}
		
		//AB HIER MESSAGES SCHICKEN (Annahme, das ich nur Strings verschicke)		
		//Falls Reader nimmer weiter weiss schickt er Wait
		 String input="";
		try {
			input = new String(apdu, "US-ASCII");
			
			//Standby zustand
			if(Arrays.equals(DictInf.Rwaitid,apdu))
				return DictInf.HcommitWaitid;
			
			//1. Modus waehlen
			if(Arrays.equals(DictInf.RAskForIdentModeid,apdu))//Identifikation
				{selMode=1;
				return DictInf.HIdentModeSelectedid;}	
			
			//2. Sende Username
			if(Arrays.equals(DictInf.RAskForU2id,apdu))
				return getData1();
			
			//3. Sende Passwort
			if(Arrays.equals(DictInf.RAskForP2id,apdu))
				return getData2();
			
			
			//4. User erfolgreich erkannt
			if(Arrays.equals(DictInf.RokGoodid,apdu))
			{
				//statusRaw=input.substring(4,input.length());
				//1. Starte Activity der App je nach Einstellungen ______________________________TODO INTENT DER APP NICHT DER EINSTELLUNGEN!
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				boolean pref = prefs.getBoolean("app_autostart",true);
				
				if(pref)
				{
					Intent menuIntent = new Intent();
					menuIntent.setClass(this, MainActivity.class);
					menuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
					startActivity(menuIntent);
				}
				Context context = getApplicationContext();
				CharSequence text = "authentification completed !";
				int duration = 5;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				
				return DictInf.HDoneWithIdid;
			}
			
			//5. User wurde nicht erkannt
			if(Arrays.equals(DictInf.RokNotGoodid,apdu))
			{
				//1. Starte Activity mit Benutzerdaten
				Intent menuIntent = new Intent();
				menuIntent.setClass(this, EnterAccountDetailsActivity.class);
				menuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
				startActivity(menuIntent);	
				
				Context context = getApplicationContext();
				CharSequence text = "User not found. Please retry or register.";
				int duration = 5;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				
				return DictInf.HDoneWithIdid;
			}
			
			
			//6. Bekomme Status Info (Starte Intent und zeige sie an) -----------TODO
			if(input.substring(0,3).equals("S1:"))
			{		
				//return "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes();
				
				//return DictInf.HDoneWithIdid;
			}
			
			if(input.equals("Frage 5"))
				return "Antwort 5".getBytes();
			if(input.equals("Frage 6"))
				return "Antwort 6".getBytes();
			
		}catch (UnsupportedEncodingException e) {}
		
		
		return DictInf.HcommitWaitid;
	}
	
	private byte[] getData1()
	{
		//return username
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		String pu="U1: "+preferences.getString("u2219aAsgiLPOo","");

		return pu.getBytes();
	}
	
	private byte[] getData2()
	{
		//return password
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String pp="P1: "+preferences.getString("p09ki8dieik87n","");
		return pp.getBytes();
	}
	
	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4;
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i("HCEDEMO", "Deactivated: " + reason);
	}
}