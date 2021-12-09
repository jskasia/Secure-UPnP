package th.ac.psu.coe.secureupnp;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.crypto.SecretKey;

import th.ac.psu.coe.secureupnp.util.KeyStoreUtils;

public class Trend {
	private  int k;
	private  int p;
	private static long[][] D;
	private long[][][] memberPublic;
	private long[][][] memberPrivate;
	private String path = null;

	public Trend() {
		this(1000, 3571);
	}

	public Trend(int nodeNumber, int prime) {
		this.k = nodeNumber;
		this.p = prime;
		this.path = "./res/matrix/";
	}

	public Trend(int nodeNumber, int prime, String path) {
		this(nodeNumber, prime);
		this.path = path;
	}
	
	
	public void generateD() {
		System.out.println("generate matrix for "+k+" nodes with prime "+p);
		D = generateRandomMatrix(k, p);
		// print(D, "D:");
	}

	public void generatePublic(String filename) throws IOException {
		memberPublic = new long[k][][];
		for (int i = 0; i < k; i++) {
			memberPublic[i] = generateRandomMatrixPublic(k, p);

			// save to file separate each client
			// publicId1.bin ...
			byte[] bytes = matrixToByteArray(memberPublic[i]);
			KeyStoreUtils.saveMatrixId(bytes, new File(path + filename
					+(i + 1) + "p.bin"));
		}

	}

	/*
	 * Generate secret and save to file
	 */
	public void generateSecret(String filename) throws IOException {
		memberPrivate = new long[k][][];
		for (int i = 0; i < k; i++) {
			memberPrivate[i] = multiplication(D, memberPublic[i], p);

			byte[] bytes = matrixToByteArray(memberPrivate[i]);
			KeyStoreUtils.saveMatrixId(bytes, new File(path + filename
					+(i + 1) + "s.bin"));
		}

	}
	
	/*
	 * For testing
	 * Return: secret key
	 */
	public SecretKey computeShareSecret(int i, int j) throws Exception{
		//load key i and j from file
		//load public id
		byte[] sAliceFile = KeyStoreUtils.loadMatrixId( new File(path+"test"+(i+1)+"s.bin") );
		long[][] sAlice = byteArrayToMatrix(sAliceFile);
		
		byte[] pBobFile = KeyStoreUtils.loadMatrixId( new File(path+"test"+(j+1)+"p.bin") );
		long[][] pBob = byteArrayToMatrix(pBobFile);
		
		long[][] keyAlice = multiplication(transposeMatrix(sAlice), pBob, p);
		byte[] bytes = longToByteArray(keyAlice[0][0]);
		
		//create secret key and encryptor
		return  KeyStoreUtils.generateKey( doubleBytesLength(bytes));
			
	}
	
	public SecretKey computeShareSecret(long[][] pMatrix, long[][] sMatrix ) throws Exception{
		long[][] keyAlice = multiplication(transposeMatrix(sMatrix), pMatrix, p);
		byte[] bytes = longToByteArray(keyAlice[0][0]);
		
		return KeyStoreUtils.generateKey( doubleBytesLength(bytes));
	}

	public static byte[] getByteArray() throws Exception {
		// 1. Trend generate matrix D
		//generateD();

		// 2. Trend random generate Alice's and Bob's public
		/*
		 * long[][] alice = generateRandomMatrixPublic(k, p); long[][] bob =
		 * generateRandomMatrixPublic(k, p); print(alice, "Alice:"); print(bob,
		 * "Bob:");
		 */

		// 2.1 save alice to file
/*
		byte[] byteAlice = matrixToByteArray(alice);
		test.key.KeyStoreUtils.savePublicId(byteAlice, new File(
				path+"alice.bin"));
		byte[] aliceFile = test.key.KeyStoreUtils.loadPublicId(new File(
				path+"alice.bin"));
		alice = byteArrayToMatrix(aliceFile);

		// 3. trend compute alice's private key
		// Galice = D*Ia, Gbob = D*Ib
		long[][] sAlice = multiplication(D, alice, p);
		print(sAlice, "secret Alice:");

		long[][] sBob = multiplication(D, bob, p);
		print(sBob, "secret Bob:");

		// 4. Compute share secret at Alice and Bob (to compare)
		// Alice
		long[][] keyAlice = multiplication(transposeMatrix(sAlice), bob, p);
		print(keyAlice, "key Alice:");

		// Bob
		long[][] keyBob = multiplication(transposeMatrix(sBob), alice, p);
		print(keyBob, "key Bob:");

		System.out.println(longToByteArray(keyAlice[0][0]).length);

		return longToByteArray(keyAlice[0][0]);
*/
		return null;
	}

	public static byte[] longToByteArray(long value) {
		return new byte[] { (byte) (value >> 56), (byte) (value >> 48),
				(byte) (value >> 40), (byte) (value >> 32),
				(byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value };
	}

	private static long byteArrayToLong(byte[] bytes) {
		long value = 0;
		for (int i = 0; i < bytes.length; i++) {
			value = (value << 8) + (bytes[i] & 0xff);
		}
		return value;
	}
	

	/*
	 * Make byte array double size
	 */
	public static byte[] doubleBytesLength(byte[] bytes){
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

	// generate symmetric matrix function
	private static long[][] generateRandomMatrix(int size, int prime) {
		long[][] d = new long[size][size];
		Random rand = new Random();
		// int x = rand.nextInt(p);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (j < i) {
					d[i][j] = d[j][i];
				} else {
					d[i][j] = (long) rand.nextInt(prime);
				}
			}
		}

		return d;
	}

	// generate symmetric matrix function (Public)
	private static long[][] generateRandomMatrixPublic(int size, int prime) {
		long[][] d = new long[size][1];
		Random rand = new Random();
		// int x = rand.nextInt(p);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 1; j++) {
				d[i][j] = (long) rand.nextInt(prime);
			}
		}

		return d;
	}

	private static byte[] matrixToByteArray(long[][] matrix) {
		byte[] tmp = new byte[8]; // each long has 8 byte length
		byte[] result = new byte[matrix.length * 8];
		int k = 0;
		for (int i = 0; i < matrix.length; i++) {
			tmp = longToByteArray(matrix[i][0]);
			for (int j = 0; j < 8; j++) {
				result[k] = tmp[j];
				k++;
			}
		}
		return result;
	}

	public static long[][] byteArrayToMatrix(byte[] bytes) {
		byte[] tmp = new byte[8];
		long[][] matrix = new long[bytes.length / 8][1];

		int k = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < 8; j++) {
				tmp[j] = bytes[j + k];
			}

			matrix[i][0] = byteArrayToLong(tmp);
			k += 8;// next long (8 bytes)
		}
		return matrix;
	}

	private static void print(long[][] matrix, String head) {
		System.out.println(head + " row:" + matrix.length + " col:"
				+ matrix[0].length);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	// matrix multiplication function
	public static long[][] multiplication(long[][] A, long[][] B, int prime) {

		int aRows = A.length;
		int aColumns = A[0].length;
		int bRows = B.length;
		int bColumns = B[0].length;

		if (aColumns != bRows) {
			throw new IllegalArgumentException("A:Rows: " + aColumns
					+ " did not match B:Columns " + bRows + ".");
		}

		long[][] C = new long[aRows][bColumns];
		for (int i = 0; i < 1; i++) {
			for (int j = 0; j < 1; j++) {
				C[i][j] = 0;
			}
		}

		for (int i = 0; i < aRows; i++) { // aRow
			for (int j = 0; j < bColumns; j++) { // bColumn
				for (int k = 0; k < aColumns; k++) { // aColumn
					C[i][j] += A[i][k] * B[k][j];
					C[i][j] %= prime;
				}
			}
		}

		return C;
	}

	// matrix transpose function
	public static long[][] transposeMatrix(long[][] m) {
		long[][] temp = new long[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}
}
