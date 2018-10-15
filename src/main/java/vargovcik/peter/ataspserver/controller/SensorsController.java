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
import vargovcik.peter.ataspserver.interfaces.SensorsInterface;

public class SensorsController {	
	class Lock {
	    public Object proximityFetch = new Object();
	}
	
    private I2CBus bus;
    private I2CDevice device;
    private byte[] bytes = new byte[6];
    private byte I2C_ADDRESS = 0x6;
    private static List<SensorsInterface> sensorsInterfaceList = new ArrayList<SensorsInterface>();
    private boolean fetching = false;
    
    private static class SingletonHelper{
        private static final SensorsController INSTANCE = new SensorsController();
    }
    
    public static SensorsController getInstance(){
        return SingletonHelper.INSTANCE;
    }
	
	private SensorsController(){
		try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            
            //get device itself
            device = bus.getDevice(I2C_ADDRESS);
            System.out.println("Connected to Sensors OK!");
        } catch (IOException ex) {
            Logger.getLogger(SensorsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedBusNumberException e) {
            Logger.getLogger(SensorsController.class.getName()).log(Level.SEVERE, null, e);
		}
	}
	
	public void addSensorListenner(SensorsInterface sensorsInterface) {
		sensorsInterfaceList.add(sensorsInterface);
	}
	
	public void startFetching(){
        fetching = true;
        fetch.start();
    }
    
    public void stopFetching(){
        fetching = false;
    }
    
    Thread fetch = new Thread(new Runnable() {

        @Override
        public void run() {
            while(fetching){
                try {
                    synchronized(new Lock().proximityFetch){
                        int r = device.read(bytes,0, bytes.length);
                    }
                    
                    int lightSensor = ((bytes[1] & 0xff) << 8) | (bytes[2] & 0xff);
                    int distanceSensor = ((bytes[3] & 0xff) << 8) | (bytes[4] & 0xff);
                    
                    for(SensorsInterface iface : sensorsInterfaceList){
                        if(iface !=null){
                            iface.distance(distanceSensor);
                            iface.lightIntensity(lightSensor);
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(SensorsController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SensorsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    });

}
