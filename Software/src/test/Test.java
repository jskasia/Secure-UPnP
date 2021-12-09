package test;

import java.util.Random;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

public class Test {
	public static void main(String args[]){
		int k = 3;
		double p = 0x0000000000000011;
		//GaloisField gf = GaloisField.getInstance(k,p);
		
		//1. choose random matrix D(k,k) over GF(p)
		Random rand = new Random();
		DenseMatrix64F D = RandomMatrices.createSymmetric(k, 0, p, rand);
		int DSize = D.numCols * D.numRows;
		
		// set to zero floating point
		for( int i=0; i< DSize; i++){
			double x = D.get(i);
			System.out.println("x="+x+"->"+ x % p);
			D.set(i, x % p);
		}
		System.out.println("Matrix D:");
		D.print();
		
		
		//2. trend random alice's public and bob's public
		DenseMatrix64F iAlice = new DenseMatrix64F(k, 1);
		DenseMatrix64F iBob = new DenseMatrix64F(k, 1);
		for( int i=0; i< k; i++){
			iAlice.set(i, 0, Double.doubleToLongBits(rand.nextDouble()) % p);
			iBob.set(i, 0, Double.doubleToLongBits(rand.nextDouble()) % p);
		}
		System.out.println("Matrix iAlice:");
		//iAlice.print();
		System.out.println("Matrix iBob:");
		//iBob.print();
		
		
		//3. trend compute alice's private key
		// Galice = D*Ia, Gbob = D*Ib
		SimpleMatrix _D = SimpleMatrix.wrap(D);
		SimpleMatrix _iAlice = SimpleMatrix.wrap(iAlice);
		SimpleMatrix _iBob = SimpleMatrix.wrap(iBob);
		SimpleMatrix _sAlice = _D.mult(_iAlice);
		SimpleMatrix _sBob = _D.mult(_iBob);
		
		for(int i=0; i<k; i++){
			_sAlice.set(i, Double.doubleToLongBits(_sAlice.get(i)) % p);
			_sBob.set(i, Double.doubleToLongBits(_sBob.get(i)) % p);
		}
		System.out.println("Matrix sAlice:");
		//_sAlice.print();
		System.out.println("Matrix sBob:");
		//_sBob.print();
		
		
		//4. Compute share secret at Alice and Bob (to compare)
		//Alice
		SimpleMatrix s1 = _sAlice.transpose().mult(_iBob);
		s1.set(0, Double.doubleToLongBits(s1.get(0)) % p);
		s1.print();
		
		//Bob
		SimpleMatrix s2 = _sBob.transpose().mult(_iAlice);
		s2.set(0, Double.doubleToLongBits(s2.get(0)) % p);
		s2.print();
		
		//double d = s1.get(0);
		//System.out.println(Double.toHexString(d));
		
		double d = 65.44444443;
		byte[] output = new byte[8];
		long lng = Double.doubleToLongBits(d);
		for(int i = 0; i < 8; i++) output[i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
		System.out.println(bytesToHex(output));
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
