package test;

import java.nio.ByteBuffer;


public class TestDouble {
	public static void main(String args[]){
		double d = 0;
		byte[] b = toByteArray(d);
		System.out.println(b.length);
		for(int i=0; i<b.length; i++)
			b[i] = 0x7F;
		double d2 = toDouble(b);
		
		System.out.println( Test.bytesToHex(b) );
		System.out.println( d2 );
		
		
		System.out.println( Double.SIZE+" "+ Long.SIZE);
	}
	
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}

	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
}
