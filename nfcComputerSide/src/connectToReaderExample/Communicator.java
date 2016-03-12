package connectToReaderExample;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;

public class Communicator extends Thread
{
	private CardTerminal terminal;
	private Card deviceAsCard;
    private CardChannel channel;
	
	private byte[] selectAidApdu;
	private ResponseAPDU readerResponse;	
	private byte[] readerResponseBytes={};
	private byte[] readerSenderBytes={};
    
    private final byte[] CLA_INS_P1_P2 = { 0x00, (byte)0xA4, 0x04, 0x00 };
	private final byte[] AID_ANDROID = { (byte)0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };
	private final byte[] DATA_EXCHANGE_ORDER={ (byte)0xD4, (byte) 0x4A, 0x01, 0x00};
	
	private byte[] firstMessage;
	private ByteArrayMap MESSAGES= new ByteArrayMap();
	
	private String[] statusInformationen={"Ladesaeule bereit","Mein Name ist Ladesaeule Cool","WeitereInfo Platzhalter 1","WeitereInfo Platzhalter 2"};
	private byte[] myBytes;
	
	private String DU2="";
	private String DP2="";
	
	
	private AuthentificationThread autTh=null;
	
	
	//initiert eine bidirektionale Verbindung mit einem Androidgerät
	public Communicator() throws CardException, IOException
	{
		//TEST STATUS SCHICKEN
		myBytes=convertToBytes(statusInformationen);
		
		//1. Finde den NFC Reader
		terminal = getTerminal();	
		
		//3. verschickbare Nachrichten ohne Daten
		firstMessage=DictInf.RAskForIdentModeid;	//empfange Initialisierungsmessage -> Sende ersten Befehl
		MESSAGES.put(DictInf.HcommitWaitid, DictInf.Rwaitid);	//Warten Message (wenn Thread im Hintergrund arbeitet
		MESSAGES.put(DictInf.HIdentModeSelectedid, DictInf.RAskForU2id);//Schicke bei Empfangen der Nachricht Mode selected, Anfrage um Daten zu kriegen
	
		
		//4. verschickbare Nachrichten mit Daten
		//MESSAGES_DATA(3prefixBytes, WhatIreceived, WhatIwantToSend);
		
		
		//5. starte nach empfangen von Daten eine Methode (java Reflection)
		//MESSAGES_METHOD(...,...,"methodName");
	
		//6. Sende Lange Nachrichten (z.B. Status) ueber Send Long Message
	}
	
	private byte[] processDataIfNecessary(byte[] readerResponseAsBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException
	{
		//STARTET IMMER WENN EINE NACHRICHT VOM READER ANKOMMT (readerResponseAsBytes)
		
		//1. Interpretiere Input Bytes als String (wgn. Vergleichbarkeit)
		String inputStr=new String(readerResponseAsBytes, "US-ASCII");
		try{
			
		//1. Empfange Datenuebertragung abgeschlossen
		if(Arrays.equals(DictInf.HDoneWithIdid,readerResponseAsBytes))
			handleIdConversationDone();
		
		//2. Authentification Thread fertig mit Authentifizierung, beziehungsweise existiert überhaupt -> Sende entsprechende Nachrichten
		byte[] authState=checkAuthentificationComplete();
		if(authState!=null)
			return authState;

		//4. Empfange Username DU2
		if(inputStr.substring(0,3).equals("U1:"))
		{
			DU2=inputStr.substring(4,inputStr.length());
			return DictInf.RAskForP2id;
		}

		//5. Empfange Passwort DP2
		if(inputStr.substring(0,3).equals("P1:"))
		{
			DP2=inputStr.substring(4,inputStr.length());
			System.out.println("Password FOUND: "+DP2);
			
			//5. Starte Authentification Thread der die Gueltigkeit des Accounts prueft
			autTh= new AuthentificationThread(DU2, DP2);
			autTh.start();
			
			//derweil schicken die beiden sich wait hin und her 
			return DictInf.Rwaitid;
		}
		
		//6. Empfange das was nach Status kommt
		if(inputStr.substring(0,3).equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
		{
			System.out.println("Dacht ich mir");
			
		}
		
		//6. EMPFANGEN EINER LAengeren Nachricht (hat Prefix Bytes)
		
		
		}
		catch(Exception e)
		{
			DU2="";
			DP2="";
		}
	
		return null;
	}
	
	private void handleIdConversationDone() throws CardException, InterruptedException, IOException
	{
		//1. breche Verbindung ab und suche nach neuer Verbindung falls diese nicht erfolgreich war
		System.out.println("Fertig mit Daten uebertragung");
				
		//2. Erfolgreich oder nicht?
		if(autTh.validUser)
			{
			
				//JOptionPane.showMessageDialog(null, "User erkannt. "+"\nWillkommen: "+DU2, "User erkannt", JOptionPane.OK_CANCEL_OPTION);
				System.out.println("User: '"+ DU2 +"' konnte erfolgreich identifiziert werden.");
				//initialen Zustand herstellen
				readerSenderBytes=null;
				readerResponseBytes=null;
				readerResponse=null;
				autTh=null;
				
			}
		else
			{
				System.out.println("User konnte nicht identifiziert werden. Neuer Versuch");
				//JOptionPane.showMessageDialog(null, "User "+DU2+" mit Passwort "+DP2+" nicht gefunden", "User nicht erkannt", JOptionPane.OK_CANCEL_OPTION);
				
				//initialen Zustand herstellen
				readerSenderBytes=null;
				readerResponseBytes=null;
				readerResponse=null;
				autTh=null;
			}	
	}
	
	private byte[] checkAuthentificationComplete() throws IOException, CardException, InterruptedException
	{
		//Testet ob schon eine Authentifikation vorhanden ist und ob diese gueltig war
		if(autTh!=null)
		{
			if(autTh.done)
			{
				if(autTh.validUser)
				{
					//schicke Nachricht konnte User finden alles gut
					System.out.println("USER FOUND");
					
					//GEBE Nachricht mit allen Status Informationen zurück
					return DictInf.RokGoodid;
					
					//return sendMessagesWithStatusInfos();
				}
				else
				{
					//konnte keinen User finden mit diesen Daten :O
					System.out.println("THIS USER WAS NOT FOUND");
					
					return DictInf.RokNotGoodid;
				}
			}
		}
		return null;
	}
	
	private byte[] sendMessagesWithStatusInfos() throws IOException
	{		
	
		//2. Sende fertige Codierte Daten
		byte[] prefix="S1: ".getBytes();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(prefix);
		//outputStream.write(myBytes);
		byte[] SaeuleStatusBytes = outputStream.toByteArray();
		
		return SaeuleStatusBytes;
	}
	
	private byte[] getAnswer() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		//je nachdem was vorher reinkam hier Antwort raussuchen aus MESSAGES!!!!!!!!!!!!!
		//String inputStr=new String(readerResponseBytes, "US-ASCII");
		//String messageToSend="";
		
		//1. Wenn nötig verarbeite hier ankommende Daten
		byte[] data=processDataIfNecessary(readerResponseBytes);		
		
		//2. sende Daten aus processedData wenn noetig
		if(data!=null)
		{
			return data;
		}
		
		//3. Sende eigentliche Message je nach Input
		try
		{
			System.out.println("\nNIX2");
			byte[] messageToSens= MESSAGES.get(readerResponseBytes);
			printByteArray(messageToSens);
			return messageToSens; 
		}
		catch(Exception e)
		{
			System.out.println("\nNIX3");
			// ALLER ERSTE MESSAGE
			return firstMessage;
		}
	}
	
	public void run()
	{
		//1. check ob Karte vorhanden
		try {waitTillDevicePresent();}catch(CardException | InterruptedException e1){}
		
		//2. Verbindung herstellen
		establishConnection();
		
		//3. frage AID meiner App an
		try {selectAppByAID();} catch (CardException | InterruptedException e2){}
		
		//4. eigentliche Kommunikation
		while(true)
			{try {communicate();} catch (CardException | InterruptedException e) {e.printStackTrace();}}
	}
	
	private void communicate() throws CardException, InterruptedException
	{
		//1. checke ob Gerät einsatzbereit
		checkIfReady();
		
		//2. Sende Nachricht, je nach Input
		sendAndReceiveMessages();
	}
	
	private CardTerminal getTerminal() throws CardException 
	{
		//1. Terminal Factory (available terminals)
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);
       
        //2. Erster Terminal
        CardTerminal terminal = terminals.get(0);
		return terminal;
	}

	private void sendAndReceiveMessages()
	{
		//Sendet eine Nachricht an den Reader
		try
		{
			//1. zu verschickende Nachricht bestimmen
			readerSenderBytes = getAnswer();  
			
			//2. Nachricht für Reader aufbereiten
			byte[] preparedMessage=getMessage(readerSenderBytes);
			
			//3. Nachricht als Command APDU verschicken
			readerResponse = channel.transmit(new CommandAPDU(preparedMessage));
			
			//4. lese Reader Antwort aus
			readerResponseBytes= processResponse(readerResponse.getBytes());
			
			logStatus();
		}
		catch(Exception e2)
		{	
			handleConnectionLost();
		}
	}
	
	private void logStatus() throws UnsupportedEncodingException
	{
		//1. Vorbereiten
		String responseAsString = new String(readerResponseBytes, "US-ASCII");
		String inputAsString = new String(readerSenderBytes, "US-ASCII");
		
		//2. Input
		System.out.print("\n\nSende: \n  String: " + inputAsString +" \n  Bytes: ");
		printByteArray(readerSenderBytes);
		
		//3. Output
		System.out.print("\nEmpfange: \n  String: " + responseAsString +" \n  Bytes: ");
		printByteArray(readerResponseBytes);
	}
	
	private void handleConnectionLost()
	{
		//Versucht wieder eine Verbindung herzustellen falls diese abbricht
		try
		{
			System.out.println("NEED TO RECONNECT");
			reConnect();
		}
		catch(Exception ee)
		{
		}
	}
	
	private byte[] processResponse(byte[] rawMessage) 
	{
		//Extrahiert aus der Reader Antwort die Bytes mit den verschickten Daten
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		
		for(int i=0;i<rawMessage.length;i++)
		{
			if(i==(rawMessage.length-1) || i==(rawMessage.length-2) || i==0 || i==1 || i==2)
			{
				continue;
			}
			outputStream.write(rawMessage[i]);			
		}
		byte [] processedBytes = outputStream.toByteArray( );
		
		return processedBytes;
		
	}
	
	private byte[] getMessage(byte[] message) throws IOException
	{
		byte[] readerPreBytes={ (byte)0xff, 0x00, 0x00, 0x00};
		byte[] messageLength=lenByte(message.length+3);//3 wegen Länge des Dataexchange Formats
		byte[] dataExchangeFormat= {(byte) 0xD4, 0x40, 0x01};
		//Kann sein das am Ende nochmal 0x00 kommen muss nach message
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(readerPreBytes);
		outputStream.write(messageLength);
		outputStream.write(dataExchangeFormat);
		outputStream.write(message);
		
		byte [] preparedMessage = outputStream.toByteArray( );
		
		return preparedMessage;
	}
	
	private byte[] lenByte(int len)
	{
		byte[] bytes = ByteBuffer.allocate(4).putInt(len).array();
		byte[] byteArray={bytes[3]};

		return byteArray;
		
	}
	
	private void checkIfReady() throws CardException, InterruptedException
	{
		//1. fragt bei Reader an ob Karte noch vorhanden, 
		//wenn nicht versuche Verbindung wiederherzustellen		
		waitTillDevicePresent();
		
		try
    	{
			ResponseAPDU readerResponseStatus = channel.transmit(new CommandAPDU(DATA_EXCHANGE_ORDER));
    	}
    	catch(Exception c)
    	{
    		reConnect();
    	}
	}
	
	private void waitTillDevicePresent() throws CardException, InterruptedException
	{
		while(!terminal.isCardPresent())
        {
        	System.out.println("Keine Karte auf Gerät erkannt");
        	Thread.sleep(200);
        }
		
	}
	
	private void establishConnection() 
	{
		selectAidApdu = createSelectAidApdu(AID_ANDROID);
		
        boolean connectionOk=false;        
        while(!connectionOk)
        {
        	try
        	{
        	deviceAsCard=terminal.connect("T=1"); //Das ist der reader  // HIER AUCH NOCHMAL TRY CATCH
            
            channel = deviceAsCard.getBasicChannel();

    		connectionOk=true; 
        	}
        	catch(Exception eeee)
        	{  
        		System.out.println("WAITING FOR CONNECTION");
        	}
        }
	}
	
	private void selectAppByAID() throws CardException, InterruptedException
	{
		System.out.print("\ninput: ");
        printByteArray(selectAidApdu);
        
        boolean AidSelectionSuccess=false;
        String s="";
        readerResponse=null;
        while(!AidSelectionSuccess)
        {
        	try
        	{
        		//1. Versuche die gegebene App über AID zu erreichen
        		readerResponse = channel.transmit(new CommandAPDU(selectAidApdu));
		        byte[] test=readerResponse.getBytes();
		        
		        System.out.print("\nAID Selektion erfolgreich. Verbindung hergestellt!\nResponse after AID Selection: ");
		        s = new String(readerResponse.getBytes(), "US-ASCII");
		        System.out.print(s);
		        
		        //2. bei Erfolg fährt Programm fort
		        AidSelectionSuccess=true;
        	}
        	catch(Exception ef)
        	{
        		//3. Bei erfolgloser Selektion neuer Versuch
        		deviceAsCard.disconnect(true);
        		reConnect();
        		System.out.print("\nKeine AID vorhanden. Keine erfolgreiche Verbindung!");
        	}
        }		
	}
	
	private void reConnect() throws CardException, InterruptedException
	{
		waitTillDevicePresent();
        
        //2. Verbindung neu aufbauen
		establishConnection();

        //3. AID
		selectAppByAID();
	}
	
	private byte[] createSelectAidApdu(byte[] aid) {
		byte[] result = new byte[6 + aid.length];
		System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
		result[4] = (byte)aid.length;
		System.arraycopy(aid, 0, result, 5, aid.length);
		result[result.length - 1] = 0;
		return result;
	}

	private void printByteArray(byte[] input)
	{
		for (int j=0; j<input.length; j++) 
        {
			 System.out.print("0x");
        	   System.out.format("%02X ", input[j]);
        }
	}

	private  String[] convertToStrings(byte[] bytesStrings) throws UnsupportedEncodingException {
		
		List<String> outStrings = new ArrayList<String>();
		
		int tmp=0;
		int elemCnt=0;
		byte[] lenByte= new byte[4];
		int curLen=0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		boolean first=true;
		for(int i=0;i<bytesStrings.length;i++)
		{
			if(tmp==0)
			{
				lenByte[tmp]=bytesStrings[i];
				outputStream = new ByteArrayOutputStream( );
				tmp++;
				continue;
			}
				
			if(tmp==1 || tmp==2)
			{
				lenByte[tmp]=bytesStrings[i];
				tmp++;
				continue;
			}
			if(tmp==3)
			{
				lenByte[tmp]=bytesStrings[i];
				ByteBuffer wrapped = ByteBuffer.wrap(lenByte); 
				curLen = wrapped.getInt();
				elemCnt=0;
				tmp++;
				continue;
			}
			
			if(elemCnt<curLen)
			{
				outputStream.write(bytesStrings[i]);
			}
			if(elemCnt==curLen)
			{	
				outStrings.add(new String(outputStream.toByteArray(), "US-ASCII"));
				
				tmp=0;
				elemCnt=-1;
				outputStream = new ByteArrayOutputStream( );
				
				tmp=0;
				lenByte[tmp]=bytesStrings[i];
				outputStream = new ByteArrayOutputStream( );
				tmp++;
				continue;
			 }
			
			elemCnt++;
		}
		
		outStrings.add(new String(outputStream.toByteArray(), "US-ASCII"));
		
		return (String[]) outStrings.toArray(new String[outStrings.size()]);

	}

	private byte[] convertToBytes(String[] strings) throws IOException {
	    
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		
		for (int i=0;i<strings.length;i++)
		{
			String curStr=strings[i];

			byte[] lengByte=lenByte4(curStr.length());
			

			byte[] data = curStr.getBytes();
			
			outputStream.write(lengByte);
			outputStream.write(data);
		}
		
		byte [] preparedMessage = outputStream.toByteArray();
		
	    return preparedMessage;
	}
	
	private byte[] lenByte4(int len)
	{
		byte[] bytes = ByteBuffer.allocate(4).putInt(len).array();
		return bytes;
		
	}
}
