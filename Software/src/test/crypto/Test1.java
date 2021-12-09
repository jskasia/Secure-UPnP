package test.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;


public class Test1 {
	public static void main(String args[]) throws Exception{
		// test encryption
		
		//get byte array
		byte[] bytes = test.Test2.getByteArray();
		
		//create secret key and encryptor
		SecretKey key = test.key.KeyStoreUtils.generateKey( doubleBytesLength(bytes));
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		
		//create decryptor
		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);
		
		//encrypt
		enc.update("Hello World".getBytes());
		byte[] cryptBytes = enc.doFinal();
		String str = test.Test.bytesToHex(cryptBytes);
		System.out.println(str);
		
		
		//decrypt
		dec.update(cryptBytes);
		String secretStr = new String(dec.doFinal());
		System.out.println(secretStr);
		
		/*
		// 6. test encode/decode
		byte[] input = "Hello World".getBytes();
		byte[] output = new byte[input.length];
		Cipher enc = Cipher.getInstance("DES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		int ctLenght = enc.update(input, 0,input.length, output,0 );
		ctLenght += enc.doFinal(output, ctLenght);
		
		enc.init(Cipher.DECRYPT_MODE, key);
		String hello = new String(enc.doFinal(output));
		System.out.println( hello );
		 */
	}
	
	/*
	 * Make byte array double size
	 */
	private static byte[] doubleBytesLength(byte[] bytes){
		int len = bytes.length;
		byte[] result = new byte[len*2];
		for(int i=0; i<len; i++){
			result[i] = bytes[i];
		}
		
		for(int i=len; i<len*2; i++){
			result[i] = bytes[i-len];
		}
		
		return result;
		
	}
}
