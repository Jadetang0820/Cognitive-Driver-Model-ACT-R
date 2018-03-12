package actr.tasks.driving;

import actr.env.*;
import actr.model.*;
import actr.task.*;

import java.io.*;
import java.lang.Thread;

public class MatlabInterface {

	public Main main;
	public Frame frame;
	public static Vehicle vehicleState = new Vehicle();
	public static double controlSteer = 0; // modified by yue -55 -> 0
	public static double controlAcc = 0;   // modified by yue -66 -> 0
	public static double controlBrake = 0; // modified by yue -77 -> 0
	public static double cogDelay=0;
	public static double cogDelayFreq=0;
	public static boolean useCogDelay=false;
	public static boolean cogPause = false;
	public static double cogTime = 0;
	public static Scenario newScenario = new Scenario();
	public static boolean setNewScenario = false;
	public static double nearPoint = 10;
	public static boolean firstRun = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//String modelName = "C:\\Users\\tangyue\\Desktop\\Integrated Driver Model2\\ACT-R\\driving\\Driving_utilChange.actr";
		//MatlabInterface test = new MatlabInterface();
		//test.startup(modelName);
		
		//System.out.println("Done with test program");
		//test whether autocarMode == 1;
		
		//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	}
	
	public MatlabInterface()
	{
	}
	
	public MatlabInterface(Scenario scenarioOverride)
	{
		newScenario = scenarioOverride;
		setNewScenario = true;
		
	}
	
	public void startup(String modelName){
		
		long sleepDuration = 5000;
		String[] callArgs = null;
				
		File modelFile = new File(modelName);
		
		main = new Main();
		Main.main(callArgs);
		
		try
		{
		Thread.sleep(sleepDuration);
		}
		catch (InterruptedException ie)
		{		
			System.out.println("Sleeping didn't work!");
		}
						
		//frame = Main.core.newFrame(modelFile);
		Main.core.openFrame(modelFile);
        frame = Main.core.currentFrame();

		
		// Run model.  model.run() doesn't update graphics, frame.run() does
		frame.run();
		//model.run();  
		
		
	}

	//public void update(){
		
		//System.out.println("In MatlabInterface.update function");
		//frame.resume();
	//}
	 public void restart()
	    {
	        frame.run();
	    }

	    public void update()
	    {
	        frame.resume();
	    }

	    public void update_PATH()
	        throws IOException
	    {
	       // Autocar.update_PATH_java();
	    }

	public static void setControlValues(double newControlSteer, double newControlAcc, double newControlBrake)
	{
		controlSteer=newControlSteer;
		controlAcc=newControlAcc;
		controlBrake=newControlBrake;
	}
	
	public static double getControlSteer()	
	{
		// This method called from Matlab
		return controlSteer;
	}
	
	public static double getControlAcc()
	{
		// This method called from Matlab
		return controlAcc;
	}
	
	public static double getControlBrake()
	{
		// This method called from Matlab
		return controlBrake;
	}
	
	public static boolean getCogPause()
	{
		// This method called from Matlab
		return cogPause;
	}
	
	public static double getCogTime()
	{
		// This method called from Matlab
		return cogTime;
	}
	
	public static void setVehicleState(double px, double py, double pz, double hx, double hy, double hz, double speed, double newCogDelay)
	{
		// This method called from Matlab
		// Need to pass in rest of required values and copy to vehicleState fields
		
		vehicleState.p.x=px;
		vehicleState.p.y=py;
		vehicleState.p.z=pz;
		
		vehicleState.h.x=hx;
		vehicleState.h.y=hy;
		vehicleState.h.z=hz;
		
		vehicleState.speed=speed;
		
		cogDelay = newCogDelay;
		
	}
	
	public static Vehicle getVehicleState()
	{
		return vehicleState;
	}

	
	public void shutdown(){
		
	}
	
}
