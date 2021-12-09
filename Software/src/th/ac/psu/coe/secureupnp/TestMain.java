package th.ac.psu.coe.secureupnp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class TestMain {
	private static final int k = 100;
	private static final int p = 3571;
	public static void main(String args[]) throws Exception{
		test1Gen(); 
		//test2Load(20,30);
	}
	
	/*
	 * Test2 is for load public/private matrix from file
	 * then try to generate secret key from them between member i and j
	 * and use it to encrypt and decrypt the string
	 */
	public static void test2Load(int i, int j) throws Exception{
		Trend t = new Trend(k,p);
		SecretKey key = t.computeShareSecret(i, j);
		if(key == null){
			System.out.println("Error: generate secret key");
		}
		
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);
		
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
		
	}
	
	/*
	 * Test 1 Generate whole system public and private matrix,
	 * then store in /tmp/
	 * input k = MaximumNumberOfNode
	 * 	p= prime , should be big prime
	 * 
	 * output public/private stored in /tmp/ and name them as *.bin
	 */
	public static void test1Gen()throws Exception{
		Trend t = new Trend(k,p);
		t.generateD();
		t.generatePublic("test");
		t.generateSecret("test");
	}
}
