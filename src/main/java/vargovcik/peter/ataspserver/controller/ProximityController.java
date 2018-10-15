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

public class ProximityController {
	private I2CBus bus;
    private I2CDevice myDevice;

    private final int I2C_DEVICE_ADDRESS = 0x7;
    private final int BAUD_RATE = 9600;
    private static List<ProximityInterface> proximityInterfaceList = new ArrayList<ProximityInterface>();
    private boolean fetching = false;
    
    
    private static class SingletonHelper{
        private static final ProximityController INSTANCE = new ProximityController();
    }
    
    public static ProximityController getInstance(){
        return SingletonHelper.INSTANCE;
    }
	
	private ProximityController(){
	 try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);

            //get device itself
            myDevice = bus.getDevice(I2C_DEVICE_ADDRESS);
            System.out.println("Connected to Proximity OK!");
        } catch (IOException ex) {
            Logger.getLogger(ProximityController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedBusNumberException e) {
            Logger.getLogger(ProximityController.class.getName()).log(Level.SEVERE, null, e);
		}

        // unsigned to signed convertion
        //int i3 = byteBuffer.get(0) & 0xFF;
        //System.out.println("buf: " + i3);
	}
	
	public void addProximityListenner(ProximityInterface proximityInterface) {
		proximityInterfaceList.add(proximityInterface);
	}
	
	public void startFetching() {
        fetching = true;
        fetch.start();
    }

    public void stopFetching() {
        fetching = false;
    }

    Thread fetch = new Thread(new Runnable() {

        @Override
        public void run() {
            while (fetching) {
                try {
                    byte b = getProximityReading();
                    for (ProximityInterface iface : proximityInterfaceList) {
                        if (iface != null) {
                            iface.onProximityUpdate(b);
                        }
                    }
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProximityController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ProximityController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    });

    public  byte getProximityReading() throws IOException {
        int val = obstacleDetected();
        return (byte) (val & 0xFF);
    }

    public synchronized int obstacleDetected() throws IOException {
        int value =  myDevice.read();
        int valueConverted = value & 0xFF;
        return valueConverted;
    }
    
    public static  int obstacleDetected(byte in){
        int valueConverted = in & 0xFF;
        return valueConverted;
    }

    public boolean[] getProximityArray() throws IOException {

        boolean[] array = new boolean[8];
        int proximityValue = obstacleDetected();
        while (proximityValue != 0) {

            if (proximityValue % 128 == 0) {
                array[0] = true;
                proximityValue -= 128;
            } else if (proximityValue % 64 == 0) {
                array[1] = true;
                proximityValue -= 64;
            } else if (proximityValue % 32 == 0) {
                array[2] = true;
                proximityValue -= 32;
            } else if (proximityValue % 16 == 0) {
                array[3] = true;
                proximityValue -= 16;
            } else if (proximityValue % 8 == 0) {
                array[4] = true;
                proximityValue -= 8;
            } else if (proximityValue % 4 == 0) {
                array[5] = true;
                proximityValue -= 4;
            } else if (proximityValue % 2 == 0) {
                array[6] = true;
                proximityValue -= 2;
            } else {
                array[7] = true;
                proximityValue -= 1;
            }
        }

        return array;
    }
    public static boolean[] getProximityArray(byte in) {

        boolean[] array = new boolean[8];
        int proximityValue = obstacleDetected(in);
        while (proximityValue != 0) {

            if (proximityValue % 128 == 0) {
                array[0] = true;
                proximityValue -= 128;
            } else if (proximityValue % 64 == 0) {
                array[1] = true;
                proximityValue -= 64;
            } else if (proximityValue % 32 == 0) {
                array[2] = true;
                proximityValue -= 32;
            } else if (proximityValue % 16 == 0) {
                array[3] = true;
                proximityValue -= 16;
            } else if (proximityValue % 8 == 0) {
                array[4] = true;
                proximityValue -= 8;
            } else if (proximityValue % 4 == 0) {
                array[5] = true;
                proximityValue -= 4;
            } else if (proximityValue % 2 == 0) {
                array[6] = true;
                proximityValue -= 2;
            } else {
                array[7] = true;
                proximityValue -= 1;
            }
        }

        return array;
    }
}
