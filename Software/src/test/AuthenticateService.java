package test;

import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;
import org.teleal.cling.binding.annotations.UpnpStateVariables;

import th.ac.psu.coe.secureupnp.Trend;
import th.ac.psu.coe.secureupnp.util.KeyStoreUtils;

@UpnpService(serviceId = @UpnpServiceId("AuthenticateService"), serviceType = @UpnpServiceType(value = "AuthenticateService", version = 1))
/*
 * @UpnpStateVariables({
 * 
 * @UpnpStateVariable(name = "PublicMatrix", defaultValue = ""),
 * 
 * @UpnpStateVariable(name = "PrivateMatrix", defaultValue = ""),
 * 
 * @UpnpStateVariable(name = "ClientMatrix", defaultValue = "") })
 */
public class AuthenticateService {
	
	private String path = "./res/matrix/";

	@UpnpStateVariable(name = "PublicMatrix", defaultValue = "", sendEvents = false)
	private Byte[] publicMatrix = null;

	@UpnpStateVariable(name = "PrivateMatrix", defaultValue = "", sendEvents = false)
	private Byte[] privateMatrix = null;

	@UpnpStateVariable(name = "ClientMatrix", defaultValue = "", sendEvents = false)
	private Byte[] clientMatrix = null;
	
	

	/*
	 * 1. kpdGetMatrix service return Server's public id
	 */
	@UpnpAction(name = "KpdGetMatrix", out = @UpnpOutputArgument(name = "PublicMatrix"))
	public Byte[] kpdGetMatrix(/*
								 * @UpnpInputArgument(name = "ClientMatrix")
								 * Byte[] clientMatrixValue
								 */) {
		// this.clientMatrix = clientMatrixValue;

		// test client's public matrix
		// computeShareSecretWithClient();

		return publicMatrix;
	}

	/*
	 * 2. auth() Input: cPubId is client public ID rand is client random (not
	 * secure for testing purpose only) crypt is encrypted rand, this is used to
	 * verify authentic Output: random code / null (failed)
	 */

	@UpnpStateVariable(name = "ClientRand", defaultValue = "", sendEvents = false)
	private String clientRand = null;

	@UpnpStateVariable(name = "Crypt", defaultValue = "", sendEvents = false)
	private String crypt = null;

	@UpnpStateVariable(name = "AuthRet", defaultValue = "", sendEvents = true)
	private String authRet = null;

	@UpnpStateVariable(name = "ClientCount", defaultValue = "", sendEvents = true)
	private int clientCount = 0;
	
	
	@UpnpAction(name = "Auth", out = @UpnpOutputArgument(name = "AuthRet"))
	public String auth(
			@UpnpInputArgument(name = "ClientMatrix") Byte[] clientMatrixValue,
			@UpnpInputArgument(name = "ClientRand") String clientRand,
			@UpnpInputArgument(name = "Crypt") String crypt) {

		System.out.println("Auth action executing");
		System.out.println("Client Random Str " + clientRand);
		System.out.println("Client Crypt str " + crypt);
		// 1. read client's public matrix
		this.clientMatrix = clientMatrixValue;

		// 2. compute share secret
		SecretKey shareKey = computeShareSecretWithClient();

		// 3. encrypt ClientRand

		String encryptText = null;
		try {
			encryptText = encryptText(shareKey, clientRand);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Server Crypt str " + encryptText);

		// 4. compare with Crypt
		int result = crypt.compareTo(encryptText); // 0 if ok
		if (result == 0) {
			System.out.println("Client authenticate result " + result);

			// 5. return authenticate result (random key)
			Random randCode = new Random();
			int randInt = randCode.nextInt(100000);

			getPropertyChangeSupport().firePropertyChange("AuthRet", this.authRet, this.authRet = Integer.toString(randInt));
			//this.authRet = Integer.toString(randInt);
			
			// 5.1 put random string to php
			System.out.println("Put code to php " + this.authRet);
			// 5.2 display code
			//clientCount++;
			getPropertyChangeSupport().firePropertyChange("ClientCount", clientCount, ++clientCount);
			

			// 6. send result to PHP module
			//sendToPhp("0","devTest", this.authRet);
			// return "Hello";
			return this.authRet;
		} else {
			System.out.println("Authentication Failed");
			return "failed";
		}
	}

	private void sendToPhp(String id, String name, String authRet)
	{
		String command = "php auth.php "+id+" "+name+" "+authRet;
		try{
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader( p.getInputStream()));
			
			String line = "";
            while( (line = reader.readLine()) != null ) {
                    System.out.println("PHP "+line);
            }
            
		}catch(Exception e){}
	}
	
	private SecretKey computeShareSecretWithClient() {
		Byte[] clientPublicMatrix = this.clientMatrix;
		Byte[] privateMatrix = this.privateMatrix;

		if (clientPublicMatrix == null || privateMatrix == null)
			System.out.println("Byte null error");
		else {
			/*
			 * System.out.println("client public matrix"); for (int i = 0; i <
			 * clientPublicMatrix.length; i++) {
			 * System.out.println(clientPublicMatrix[i]); }
			 * 
			 * System.out.println("server public matrix"); for (int i = 0; i <
			 * privateMatrix.length; i++) {
			 * System.out.println(privateMatrix[i]); }
			 */
		}

		System.out.println("client " + clientPublicMatrix.length);
		System.out.println("server " + privateMatrix.length);

		// calculate secret key

		// convert to byte array
		byte[] clientArray = new byte[clientPublicMatrix.length];
		byte[] privateArray = new byte[privateMatrix.length];
		for (int i = 0; i < clientPublicMatrix.length; i++) {
			clientArray[i] = clientPublicMatrix[i];
			privateArray[i] = privateMatrix[i];
		}

		Trend t = new Trend();

		// convert to matrix (public and private
		long[][] pMatrix = t.byteArrayToMatrix(clientArray);
		long[][] sMatrix = t.byteArrayToMatrix(privateArray);

		System.out.println("p " + pMatrix.length + " " + pMatrix[0].length);
		System.out.println("s " + sMatrix.length + " " + sMatrix[0].length);

		// compute share secret
		SecretKey shareKey = null;

		try {
			shareKey = t.computeShareSecret(pMatrix, sMatrix);
			// test share key
			encryptText(shareKey, "Hello World");
			KeyStoreUtils.saveKey(shareKey, new File(path+"shareKey.bin"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return shareKey;
	}

	/*
	 * Private function for testing only
	 */
	private String encryptText(SecretKey key, String rand) throws Exception {
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);

		Cipher dec = Cipher.getInstance("AES");
		dec.init(Cipher.DECRYPT_MODE, key);

		// encrypt
		enc.update(rand.getBytes());
		byte[] cryptBytes = enc.doFinal();
		String str = test.Test.bytesToHex(cryptBytes);
		System.out.println(str);

		// decrypt
		dec.update(cryptBytes);
		String secretStr = new String(dec.doFinal());
		System.out.println(secretStr);

		return str;
	}

	// => add new
	// connect to ui to update its status through Upnp's state change
	private final PropertyChangeSupport propertyChangeSupport;

	public AuthenticateService() {
		// Load local public file
		byte[] publicMatrixBytes = null;
		byte[] privateMatrixBytes = null;
		try {
			publicMatrixBytes = KeyStoreUtils.loadMatrixId(new File(
					path+"test1p.bin"));
			privateMatrixBytes = KeyStoreUtils.loadMatrixId(new File(
					path+"test1s.bin"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// System.out.println("key bytes length "+keyBytes.length);

		// String encStr = null;
		/*
		 * try { //encStr = URLEncoder.encode(new String(keyBytes,"UTF-8"),
		 * "UTF-8"); //encStr = Base64Coder.encode(keyBytes); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); }
		 */
		this.privateMatrix = new Byte[privateMatrixBytes.length];
		for (int i = 0; i < privateMatrixBytes.length; i++)
			this.privateMatrix[i] = Byte.valueOf(privateMatrixBytes[i]);
		System.out.println("Public matrix length " + this.privateMatrix.length);

		// System.out.println("key string length "+encStr.length());
		this.publicMatrix = new Byte[publicMatrixBytes.length];
		for (int i = 0; i < publicMatrixBytes.length; i++)
			this.publicMatrix[i] = Byte.valueOf(publicMatrixBytes[i]);
		System.out.println("Public matrix length " + this.publicMatrix.length);

		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
}
