package vargovcik.peter.ataspserver.controller;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import vargovcik.peter.ataspserver.interfaces.ProximityInterface;

public class PeripheralsController {
	
	class Lock {
	    public Object proximityFetch = new Object();
	}
	
	private final int NEW_COMMAND_BYTE = 0;  
    private final int PAN_ROTATION_BYTE = 1;  
    private final int TILT_ROTATION_BYTE = 2;  
    private final int UNUSED_COMMAND1_BYTE = 3;  
    private final int UNUSED_COMMAND2_BYTE = 4;  
    private final int UNUSED_COMMAND3_BYTE = 5;      
    
    private final byte UNUSED_BYTE = (byte) 0b00000000;;  
    
    private I2CBus bus;
    private I2CDevice device;
    private byte[] commandBytes = new byte[6];
    private byte I2C_ADDRESS = 0x5;
    private boolean fetching;
    private static List<ProximityInterface> proximityInterfaceList = new ArrayList<ProximityInterface>();
	
	private PeripheralsController(){
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);            
            //get device itself
            device = bus.getDevice(I2C_ADDRESS);
            System.out.println("Connected to Peripherals OK!");
        } catch (IOException ex) {
            Logger.getLogger(PeripheralsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedBusNumberException e) {
            Logger.getLogger(PeripheralsController.class.getName()).log(Level.SEVERE, null, e);
		}
        fetching = false;
	}
	    
    private static class SingletonHelper{
        private static final PeripheralsController INSTANCE = new PeripheralsController();
    }
    
    public static PeripheralsController getInstance(){
        return SingletonHelper.INSTANCE;
    }
	
	public void addProximityListenner(ProximityInterface proximityInterface) {
		proximityInterfaceList.add(proximityInterface);
	}
	
	public synchronized void moveCamera(byte pan, byte tilt) throws IOException{
        commandBytes[NEW_COMMAND_BYTE]  = (byte) 0b11111111;
        commandBytes[PAN_ROTATION_BYTE] = pan;
        commandBytes[TILT_ROTATION_BYTE] = tilt;
        commandBytes[UNUSED_COMMAND1_BYTE] = UNUSED_BYTE;
        commandBytes[UNUSED_COMMAND2_BYTE] = UNUSED_BYTE;
        commandBytes[UNUSED_COMMAND3_BYTE] = UNUSED_BYTE;
        
        device.write(commandBytes, 0, 6);
    }
    
    public synchronized static  boolean rightSideObstacleDetected(byte in){
        int valueConverted = in & 0xFF;
        return (valueConverted !=0);
    }
    
    public void startFetching() {
        System.out.println("Fetching Right Hand proxymity!");
        fetching = true;
        fetch.start();
    }

    public void stopFetching() {
        fetching = false;
    }
    
     Thread fetch = new Thread(new Runnable() {

        @Override
        public void run() {
            while(fetching){
                try {
                    synchronized(new Lock().proximityFetch){
                        int r = device.read(commandBytes,0, commandBytes.length);
                    }
                    byte rightHandProximity = commandBytes[UNUSED_COMMAND2_BYTE];
                    
                    for(ProximityInterface iface : proximityInterfaceList){
                        if(iface !=null){
                            iface.onRightHandProximityUpdate(rightHandProximity);
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(PeripheralsController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PeripheralsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    });

}
