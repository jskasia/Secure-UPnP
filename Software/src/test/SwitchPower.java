package test;

import java.beans.PropertyChangeSupport;
import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;
import org.teleal.cling.binding.annotations.UpnpStateVariables;


@UpnpService( 
		serviceId = @UpnpServiceId("SwitchPower"),
		serviceType = @UpnpServiceType(value = "SwitchPower", version = 1)
)

@UpnpStateVariables(
        {
                @UpnpStateVariable(
                        name = "Target",
                        defaultValue = "0",
                        sendEvents = false
                ),
                @UpnpStateVariable(
                        name = "Status",
                        defaultValue = "0"
                ),
                @UpnpStateVariable(
                        name = "PublicMatrix",
                        defaultValue = "this is server's public matrix"
                )
        }
)




public class SwitchPower {
	@UpnpStateVariable(defaultValue ="0", sendEvents = true)
	private boolean target = false;
	
	@UpnpStateVariable(defaultValue = "0")
	private boolean status = false;
	
	@UpnpStateVariable(defaultValue = "")
	private String publicMatrix = "";

	
	@UpnpAction
	public void setTarget( @UpnpInputArgument(name = "NewTargetValue") boolean newTagetValue){
		// => add new 
		boolean targetOldValue = target;
		target = newTagetValue;
		boolean statusOldValue = status;
		status = newTagetValue;
		getPropertyChangeSupport().firePropertyChange("Status", statusOldValue, status);
		
		System.out.println("Switch is: "+status);
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name= "RetTargetValue"))
	public boolean getTarget(){
		return target;
	}
	
	@UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
	public boolean getStatus(){
		return status;
	}
	
	
	/*
	 * kpdGetMatrix service return Server's public id
	 */
	@UpnpAction(
			name = "KpdGetMatrix",
			out = @UpnpOutputArgument(
					name= "RetTargetValue",
					stateVariable = "PublicMatrix"
					)
	)
	public String kpdGetMatrix(){
		return publicMatrix;
	}
	
	
	// => add new
	// connect to ui to update its status through Upnp's state change
	private final PropertyChangeSupport propertyChangeSupport;
    public SwitchPower() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
}
