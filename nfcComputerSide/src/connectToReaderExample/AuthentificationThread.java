package connectToReaderExample;

import java.util.Hashtable;

import javax.swing.JOptionPane;

public class AuthentificationThread extends Thread{

	public boolean done=false;
	public boolean validUser=false;
	
	public String user="";
	public String pw= "";
	
	private Hashtable<String, String> userdata= new Hashtable();
	
	public AuthentificationThread(String u, String p)
	{
		this.user=u;
		this.pw=p;	
		done=false;
		validUser=false;
		
		
		//im Moment user Daten aus Dictionary auslesen // Beliebig erweiterbar
		userdata.put("Artur Mrowca","passwort");
		userdata.put("Tesrperson","test");
		userdata.put("InCharge","incharge");
	}
	
	@Override
	public void run() {
		try
		{
			String foundPW =(String) userdata.get(user);
			if(foundPW.equals(pw))
				{
				validUser=true;
				}
			else
				{
					validUser=false;
				}
		}catch(Exception e){}
		
		//Wenn fertig done=true!
		done=true;
		
		//VON HIER AUS JE NACH GUELTIGKEIT DES ACCOUNTS NAECHSTEN ZUSTAND DES READERS SETZEN
		//z.B. Zeige Output an und starte Communicator neu
		
	}

}
