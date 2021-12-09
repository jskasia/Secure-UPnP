package test;

import java.io.IOException;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import org.teleal.cling.model.ValidationException;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.controlpoint.*;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.message.UpnpResponse;


public class BinaryLightServer extends JFrame implements Runnable{
	public static void main(String argsp[]) throws Exception{
		Thread serverThread = new Thread( new BinaryLightServer());
		serverThread.setDaemon(false);
		serverThread.start();
	}
	
	private JLabel statusLabel = new JLabel("default");
	private JLabel clientCount = new JLabel("0");
	private JLabel authRet = new JLabel("-");
	
	public void run(){
		// init GUI
		JPanel panel = new JPanel();
				
		panel.add(new JLabel("Client Auth count: "));
		panel.add(clientCount);
		
		panel.add(new JLabel("Current Random: "));
		panel.add(authRet);
		
		add(panel);
		setVisible(true);
		setTitle("Pi Server");
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// init Upnp
		try{
			final UpnpService upnpService = new UpnpServiceImpl();
			
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					upnpService.shutdown();
				}
			});
			
			LocalDevice localDevice = createDevice();
			
			upnpService.getRegistry().addDevice( localDevice );
			
			// check if status is changed => update GUI
			checkStatusUpdate(upnpService);
			
			
			
						
		} catch( Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	
	}
	
	private void checkStatusUpdate(UpnpService upnpService)
	{
		LocalDevice localDevice = upnpService.getRegistry().getLocalDevice(UDN.uniqueSystemIdentifier("Pi Server"), true);
		Service service = localDevice.findService(new UDAServiceId("AuthenticateService"));
		
		SubscriptionCallback callback = new SubscriptionCallback(service, 600) {

		    @Override
		    public void established(GENASubscription sub) {
		        System.out.println("Established: " + sub.getSubscriptionId());
		    }

		    @Override
		    protected void failed(GENASubscription subscription,
		                          UpnpResponse responseStatus,
		                          Exception exception,
		                          String defaultMsg) {
		        System.err.println(defaultMsg);
		    }

		    @Override
		    public void ended(GENASubscription sub,
		                      CancelReason reason,
		                      UpnpResponse response) {
		        assert reason == null;
		    }

		    // status Changed
		    public void eventReceived(GENASubscription sub) {

		        System.out.println("Event Number: " + sub.getCurrentSequence().getValue());

		        Map<String, StateVariableValue> values = sub.getCurrentValues();
		        StateVariableValue clientCountstatus = values.get("ClientCount");
		        StateVariableValue authRetStatus = values.get("AuthRet");
		        

		        //assertEquals(status.getDatatype().getClass(), BooleanDatatype.class);
		        //assertEquals(status.getDatatype().getBuiltin(), Datatype.Builtin.BOOLEAN);

		        clientCount.setText(clientCountstatus.toString()); // => update GUI
		        System.out.println("Client count: " + clientCountstatus.toString());
		        
		        authRet.setText(authRetStatus.toString());
		       
		        
		        repaint(); // => refresh display

		    }

		    public void eventsMissed(GENASubscription sub, int numberOfMissedEvents) {
		        System.out.println("Missed events: " + numberOfMissedEvents);
		    }

		};

		upnpService.getControlPoint().execute(callback);
	}
	
	public LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {
		
		DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Pi Server"));
		
		DeviceType type = new UDADeviceType("TestKpdAuthenticate", 1);
		
		DeviceDetails details = new DeviceDetails("Pi Server", new ManufacturerDetails("CoE"), new ModelDetails("RaspberryPi UPnP", "A demo UPnP-Kpd authentication.", "v1"));
		
		//Icon icon = new Icon("image/png", 48, 48, 8, getClass().getResource("icon.png"));
		
		LocalService<SwitchPower> switchPowerService = new AnnotationLocalServiceBinder().read(SwitchPower.class);
		switchPowerService.setManager( new DefaultServiceManager(switchPowerService, SwitchPower.class));
		
		LocalService<AuthenticateService> authenticateService = new AnnotationLocalServiceBinder().read(AuthenticateService.class);
		authenticateService.setManager(new DefaultServiceManager(authenticateService, AuthenticateService.class) );
		
		LocalService[] services = {switchPowerService, authenticateService};
		
		return new LocalDevice(identity, type, details, /*icon,*/ services); 
	}
	
	
	
	
	
}

