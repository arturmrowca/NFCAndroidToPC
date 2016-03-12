package connectToReaderExample;

import java.util.ArrayList;
import java.util.Arrays;

public class ByteArrayMap {
	
	private ArrayList<byte[]> keys;
	private ArrayList<byte[]> values;
	
	public ByteArrayMap()
	{
		keys= new ArrayList<>();
		values= new ArrayList<>();
	}
	
	
	public void put(byte[] key, byte[] value)
	{
		if(keyExists(key))
		{
			updateValue(key,value);
		}
		else
		{
			keys.add(key);
			values.add(value);
		}
	}
	
	public byte[] get(byte[] key)
	{
		for(int i=0; i<keys.size();i++)
		{
			if(Arrays.equals(keys.get(i),key))
			{
				return values.get(i);
			}
		}
		return null;
	}
	
	private void updateValue(byte[] key, byte[] value)
	{
		for(int i=0; i<keys.size();i++)
		{
			if(Arrays.equals(keys.get(i),key))
			{
				values.set(i, value);
			}
		}		
	}
	
	public boolean keyExists(byte[] key)
	{
		for(int i=0; i<keys.size();i++)
		{
			if(Arrays.equals(keys.get(i),key))
			{
				return true;
			}
		}
		return false;
	}
	
	

}
