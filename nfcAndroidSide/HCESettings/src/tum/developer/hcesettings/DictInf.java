package tum.developer.hcesettings;

public class DictInf {
	//Nachrichtenkatalog 
	
	//ZU Debugging Zwecken macht es Sinn Nachrichten als Strings die zu bytes werden darzustellen
			//Modus Identifikation
			//von Reader verschickte Nachrichten
			public static final byte[] Rwaitid="waitPlease".getBytes();//"waitPlease".getBytes();
			public static final byte[] RAskForIdentModeid="GetModeIdentify".getBytes();
			public static final byte[] RAskForU2id="GetU2".getBytes();
			
			public static final byte[] RAskForP2id="GetP2".getBytes();
			public static final byte[] RCommitP2id="CommitU2".getBytes();
			public static final byte[] RokGoodid="rr0kggOOd".getBytes();
			public static final byte[] RokNotGoodid="notG00d".getBytes();
			
			//von Handy verschickte Nachrichten
			public static final byte[] HcommitWaitid="okWait".getBytes();
			public static final byte[] HAIDSelectionOkid="AIDSelectionOk".getBytes();
			public static final byte[] HIdentModeSelectedid="IdentModeInitialized".getBytes();			
			public static final byte[] HAskForModeid="AskForMode".getBytes();
			public static final byte[] HUPrefixid="UR: ".getBytes();
			public static final byte[] HDoneWithIdid="DoneWithIdProcess".getBytes();
}
