package connectToReaderExample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.smartcardio.*;

public class Main {

	public static void main(String[] args) throws CardException, InterruptedException, Exception 
	{
		//1. Starte Communicator als Thread
		Communicator androidCommuniactor=new Communicator();
		androidCommuniactor.run();	
	}
}
