package test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.InvalidValueException;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

import th.ac.psu.coe.secureupnp.Trend;
import th.ac.psu.coe.secureupnp.util.KeyStoreUtils;

public class BinaryLightClient extends JFrame implements Runnable {
	public static void main(String args[]) throws Exception {
		Thread controlPointThread = new Thread(new BinaryLightClient());
		controlPointThread.setDaemon(false);
		controlPointThread.start();

	}

	private String path = "./res/matrix/";
	private JLabel deviceDiscovered = new JLabel("0");
	private JLabel piServerDiscovered = new JLabel("0");
	private JLabel deviceAuthCode = new JLabel("null");

	public void run() {
		// init GUI
		JPanel panel = new JPanel();
		panel.add(new JLabel("UPnP discovered: "));
		panel.add(deviceDiscovered);

		panel.add(new JLabel("Pi Server discovered: "));
		panel.add(piServerDiscovered);

		panel.add(new JLabel("Auth Code: "));
		panel.add(deviceAuthCode);

		add(panel);
		setVisible(true);
		setTitle("Pi Client");
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 1. search for device in the network
		// searchDevice();
		UpnpService upnpService = new UpnpServiceImpl();

		// Add a listener for device registration events
		upnpService.getRegistry().addListener(
				createRegistryListener(upnpService));

		// Broadcast a search message for all devices
		upnpService.getControlPoint().search(new STAllHeader());

		// 2. find interesting service

		// 3. execute service (key exchange)

		// 4. generate public/private matrix and store on the system

	}

	/*
	 * Set interesting service which is "AuthenticateService" here.
	 */
	RegistryListener createRegistryListener(final UpnpService upnpService) {
		return new DefaultRegistryListener() {

			ServiceId serviceId = new UDAServiceId("AuthenticateService");

			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				deviceDiscovered.setText(registry.getDevices().size() + "");

				Service authenticateService;
				if ((authenticateService = device.findService(serviceId)) != null) {

					System.out.println("Service discovered: "
							+ authenticateService);

					int serverNumber = Integer.parseInt(piServerDiscovered
							.getText());
					piServerDiscovered.setText("" + (++serverNumber));

					// executeGetKpdMatrix(upnpService, authenticateService);
					/*
					 * change to execute synchronous
					 * myActionInvocation.setInput("foo", bar); new
					 * ActionCallback.Default(myActionInvocation,
					 * upnpService.getControlPoint()).run();
					 * myActionInvocation.getOutput("baz");
					 */
					ActionInvocation kpdGetMatrixInvocation = new ActionInvocation(
							authenticateService.getAction("KpdGetMatrix"));
					new ActionCallback.Default(kpdGetMatrixInvocation,
							upnpService.getControlPoint()).run();

					Byte[] publicMatrixRet = (Byte[]) kpdGetMatrixInvocation
							.getOutput("PublicMatrix").getValue();
					System.out.print("Output char ");
					for (int i = 0; i < publicMatrixRet.length; i++)
						System.out.print(publicMatrixRet[i]);

					SecretKey key = computeSecretKey(publicMatrixRet);
					shareKey = key;
					// set global
					try {
						KeyStoreUtils.saveKey(key, new File(path
								+ "tmpClientSecret.bin"));
					} catch (IOException e) {
						e.printStackTrace();
					}

					SecretKey shareKey;
					while ((shareKey = getSecretKey()) == null)
						;

					System.out.println("Execute auth");
					//executeAuth(upnpService, authenticateService, shareKey);

					/*
					 * change to execute synchronous
					 */
					ActionInvocation authInvocation = new ActionInvocation(
							authenticateService.getAction("Auth"));
					new ActionCallback.Default(kpdGetMatrixInvocation,
							upnpService.getControlPoint()).run();

					// set input
					System.out.println("Exe Bob");
					Byte[] pBob = getPBobFile();
					Random rand = new Random();
					int randInt = rand.nextInt(1000000);
					String randStr = Integer.toString(randInt);
					String crypt = encrypt(randStr, shareKey);

					authInvocation.setInput("ClientMatrix", pBob);
					authInvocation.setInput("ClientRand", randStr);
					authInvocation.setInput("Crypt", crypt);

					// execute
					new ActionCallback.Default(authInvocation,
							upnpService.getControlPoint()).run();

					// process result
					String authResultRet = (String) authInvocation.getOutput(
							"AuthRet").getValue();
					System.out.print("Output String ");
					System.out.print(authResultRet);

					// set to display at swing
					deviceAuthCode.setText(authResultRet);

				}
			}

			@Override
			public void remoteDeviceRemoved(Registry registry,
					RemoteDevice device) {
				Service switchPower;
				if ((switchPower = device.findService(serviceId)) != null) {
					System.out.println("Service disappeared: " + switchPower);
				}

				deviceDiscovered.setText(registry.getDevices().size() + "");
				Service authenticateService;
				if ((authenticateService = device.findService(serviceId)) != null) {
					int serverNumber = Integer.parseInt(piServerDiscovered
							.getText());
					piServerDiscovered.setText("" + (--serverNumber));
				}
			}

		};
	}

	private SecretKey getSecretKey() {
		try {
			File f = new File(path + "tmpClientSecret.bin");
			SecretKey s = KeyStoreUtils.loadKey(f);
			return s;
		} catch (IOException e) {
			return null;
		}
	}

	private byte[] pBobFile = null;

	private Byte[] getPBobFile() {
		Byte[] pBobfileByte = null;

		if (pBobFile == null) {
			// load client's public matrix
			try {
				pBobFile = KeyStoreUtils.loadMatrixId(new File(path
						+ "test2p.bin"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// then convert to Byte object
			pBobfileByte = new Byte[pBobFile.length];
			for (int i = 0; i < pBobFile.length; i++)
				pBobfileByte[i] = Byte.valueOf(pBobFile[i]);
		}

		return pBobfileByte;
	}

	private SecretKey shareKey = null;

	/*
	 * What to do when the service is found?
	 */
	void executeGetKpdMatrix(UpnpService upnpService,
			Service authenticateService) {
		// 1. get server' public matrix by invoking KpdGetMatrix

		// 2. compute share secret

		// 3. rand string

		// 4. encrypt rand

		// 5. invoke Auth

		// 6. save random code or print display to user ()

		ActionInvocation setTargetInvocation = new SetKpdGetMatrixActionInvocation(
				authenticateService);
		// setTargetInvocation.setInput( new ActionArgumentValue(
		// authenticateService.getAction("GetKpdMatrix").getInputArgument("ClientMatrix"),
		// pBobfileByte) );

		// Executes asynchronous in the background
		upnpService.getControlPoint().execute(
				new ActionCallback(setTargetInvocation) {

					@Override
					public void success(ActionInvocation invocation) {
						assert invocation.getOutput().length == 0;
						Byte[] output = (Byte[]) invocation.getOutput(
								"PublicMatrix").getValue();
						System.out.print("Output char ");
						for (int i = 0; i < output.length; i++)
							System.out.print(output[i]);

						SecretKey key = computeSecretKey(output);
						shareKey = key;
						// set global
						try {
							KeyStoreUtils.saveKey(key, new File(path
									+ "tmpClientSecret.bin"));
						} catch (IOException e) {
							e.printStackTrace();
						}

						/*
						 * try { testCrypto(key); } catch (Exception e) {
						 * e.printStackTrace(); }
						 */

					}

					@Override
					public void failure(ActionInvocation invocation,
							UpnpResponse operation, String defaultMsg) {
						System.err.println(defaultMsg);
					}
				});

	}

	private void executeAuth(UpnpService upnpService,
			Service authenticateService, SecretKey shareKey) {

		System.out.println("Exe Bob");
		Byte[] pBob = getPBobFile();
		Random rand = new Random();
		int randInt = rand.nextInt(1000000);
		String randStr = Integer.toString(randInt);
		String crypt = encrypt(randStr, shareKey);

		ActionInvocation setTargetInvocation = new SetAuthActionInvocation(
				authenticateService);

		ActionArgumentValue[] inputList = new ActionArgumentValue[3];

		inputList[0] = new ActionArgumentValue(authenticateService.getAction(
				"Auth").getInputArgument("ClientMatrix"), pBob);

		inputList[1] = new ActionArgumentValue(authenticateService.getAction(
				"Auth").getInputArgument("ClientRand"), randStr);

		inputList[2] = new ActionArgumentValue(authenticateService.getAction(
				"Auth").getInputArgument("Crypt"), crypt);

		setTargetInvocation.setInput(inputList);

		// Executes asynchronous in the background
		upnpService.getControlPoint().execute(
				new ActionCallback(setTargetInvocation) {

					@Override
					public void success(ActionInvocation invocation) {
						assert invocation.getOutput().length == 0;
						String output = (String) invocation
								.getOutput("AuthRet").getValue();
						System.out.print("Output String ");
						System.out.print(output);

						// set to display at swing
						deviceAuthCode.setText(output);
					}

					@Override
					public void failure(ActionInvocation invocation,
							UpnpResponse operation, String defaultMsg) {
						System.err.println(defaultMsg);
					}
				});

	}

	class SetKpdGetMatrixActionInvocation extends ActionInvocation {

		SetKpdGetMatrixActionInvocation(Service service) {
			super(service.getAction("KpdGetMatrix"));
			try {

				// Throws InvalidValueException if the value is of wrong type
				// setInput("NewTargetValue", true);

			} catch (InvalidValueException ex) {
				System.err.println(ex.getMessage());
				System.exit(1);
			}
		}
	}

	class SetAuthActionInvocation extends ActionInvocation {

		SetAuthActionInvocation(Service service) {
			super(service.getAction("Auth"));
			try {

				// Throws InvalidValueException if the value is of wrong type
				// setInput("NewTargetValue", true);

			} catch (InvalidValueException ex) {
				System.err.println(ex.getMessage());
				System.exit(1);
			}
		}
	}

	private SecretKey computeSecretKey(Byte[] serverPublic) {

		byte[] aliceFile = new byte[serverPublic.length];
		for (int i = 0; i < serverPublic.length; i++) {
			aliceFile[i] = serverPublic[i];
		}
		long[][] pAliceMatrix = Trend.byteArrayToMatrix(aliceFile);

		// load my secret from file
		byte[] sBobFile = null;
		try {
			sBobFile = KeyStoreUtils
					.loadMatrixId(new File(path + "test2s.bin"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		long[][] sBobMatrix = Trend.byteArrayToMatrix(sBobFile);

		// computeShareSecret
		long[][] keyAlice = Trend.multiplication(
				Trend.transposeMatrix(sBobMatrix), pAliceMatrix, 3571);
		byte[] bytes = Trend.longToByteArray(keyAlice[0][0]);

		SecretKey key = null;
		try {
			key = KeyStoreUtils.generateKey(Trend.doubleBytesLength(bytes));
		} catch (Exception e) {
		}

		System.out.println("Generate share key successful");
		return key;
	}

	private String encrypt(String str, SecretKey shareKey) {

		String cryptStr = null;
		try {
			Cipher enc = Cipher.getInstance("AES");

			// while( shareKey == null );

			enc.init(Cipher.ENCRYPT_MODE, shareKey);

			// encrypt
			enc.update(str.getBytes());
			byte[] cryptBytes = enc.doFinal();
			cryptStr = test.Test.bytesToHex(cryptBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(cryptStr);

		return cryptStr;

		// decrypt
		// load secret key
		/*
		 * SecretKey key2 = KeyStoreUtils.loadKey(new
		 * File(path+"shareKey.bin"));
		 * 
		 * Cipher dec = Cipher.getInstance("AES"); //
		 * dec.init(Cipher.DECRYPT_MODE, key); dec.init(Cipher.DECRYPT_MODE,
		 * key2);
		 * 
		 * dec.update(cryptBytes); String secretStr = new String(dec.doFinal());
		 * System.out.println(secretStr);
		 * 
		 * return "Hello crypt";
		 */
	}

	private void testCrypto(SecretKey key) throws Exception {
		Cipher enc = Cipher.getInstance("AES");
		enc.init(Cipher.ENCRYPT_MODE, key);

		// encrypt
		enc.update("Hello World2".getBytes());
		byte[] cryptBytes = enc.doFinal();
		String str = test.Test.bytesToHex(cryptBytes);
		System.out.println(str);

		// decrypt
		// load secret key

		SecretKey key2 = KeyStoreUtils.loadKey(new File(path + "shareKey.bin"));

		Cipher dec = Cipher.getInstance("AES");
		// dec.init(Cipher.DECRYPT_MODE, key);
		dec.init(Cipher.DECRYPT_MODE, key2);

		dec.update(cryptBytes);
		String secretStr = new String(dec.doFinal());
		System.out.println(secretStr);
	}
}

/**** note ***/
/*
 * public void searchDevice() { // annonymouse class listen to responds
 * RegistryListener listener = new RegistryListener() {
 * 
 * @Override public void remoteDeviceUpdated(Registry arg0, RemoteDevice arg1) {
 * // TODO Auto-generated method stub
 * 
 * }
 * 
 * @Override public void remoteDeviceRemoved(Registry arg0, RemoteDevice arg1) {
 * // TODO Auto-generated method stub
 * 
 * }
 * 
 * @Override public void remoteDeviceDiscoveryStarted(Registry registry,
 * RemoteDevice device) { System.out.println("Discovery started: " +
 * device.getDisplayString());
 * 
 * }
 * 
 * @Override public void remoteDeviceDiscoveryFailed(Registry arg0, RemoteDevice
 * arg1, Exception arg2) { // TODO Auto-generated method stub
 * 
 * }
 * 
 * @Override public void remoteDeviceAdded(Registry registry, RemoteDevice
 * device) { System.out.println("Discovery availiable: " +
 * device.getDisplayString());
 * deviceNumberLabel.setText(registry.getDevices().size() + "");
 * 
 * }
 * 
 * @Override public void localDeviceRemoved(Registry arg0, LocalDevice arg1) {
 * // TODO Auto-generated method stub
 * 
 * }
 * 
 * @Override public void localDeviceAdded(Registry registry, LocalDevice device)
 * { System.out.println("Local device added: " + device.getDisplayString());
 * 
 * }
 * 
 * @Override public void beforeShutdown(Registry registry) {
 * System.out.println("Beforshutdown: " + registry.getDevices().size());
 * 
 * }
 * 
 * @Override public void afterShutdown() {
 * System.out.println("Shutdown complete!");
 * 
 * } };
 * 
 * 
 * // create network resources for UPnP System.out.println("Start Cling...");
 * UpnpService upnpService = new UpnpServiceImpl(listener);
 * 
 * // send a search message to device services
 * upnpService.getControlPoint().search(new STAllHeader());
 * 
 * // wait 10 seconds System.out.println("Wait for 10 seconds"); try {
 * Thread.sleep(10000); } catch (Exception e) { }
 * 
 * System.out.println("Stopping Cling..."); upnpService.shutdown(); }
 */