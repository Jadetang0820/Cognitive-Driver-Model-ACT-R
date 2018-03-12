package actr.tasks.driving;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.*;

import actr.task.*;
import actr.model.*;

/**
 * The main Driving task class that sets up the simulation and starts periodic updates.
 *  
 * 
 */
public class Driving extends actr.task.Task
{
	static Simulator simulator = null;

	Simulation simulation;
	JLabel nearLabel, carLabel, keypad;


	double steerFactor_dfa, steerFactor_dna;
	double steerFactor_na, steerFactor_fa;
	double accelFactor_thw, accelFactor_dthw;
	double steerNaMax, thwFollow, thwMax;
	double startTime,endTime,accelBrake,speed;

	static int minX=174, maxX=(238+24), minY=94, maxY=(262+32);
	static int centerX=(minX+maxX)/2, centerY=(minY+maxY)/2; 

	public Driving ()
	{
		super ();

		nearLabel = new JLabel (".");
		carLabel = new JLabel ("X");
		keypad = new JLabel ("*");

	}

	public void start ()
	{
		simulation = new Simulation (getModel());

		steerFactor_dfa = Env.scenario.steerFactor_dfa;
		steerFactor_dna = Env.scenario.steerFactor_dna;
		steerFactor_na  = Env.scenario.steerFactor_na;
		steerFactor_fa  = Env.scenario.steerFactor_fa;
		accelFactor_thw = Env.scenario.accelFactor_thw;
		accelFactor_dthw =Env.scenario.accelFactor_dthw;
		steerNaMax = Env.scenario.steerNaMax;
		thwFollow = Env.scenario.thwFollow;
		thwMax = Env.scenario.thwMax;
		startTime = Env.scenario.startTime;
		endTime = Env.scenario.endTime;
		accelBrake = Env.scenario.accelBrake; 
		speed = Env.scenario.speed;

		if (getModel().getRealTime())
		{
			setLayout (new BorderLayout());
			if (simulator == null) simulator = new Simulator ();
			add (simulator, BorderLayout.CENTER);
			simulator.useSimulation (simulation);
		}
		else
		{
			add (nearLabel);
			nearLabel.setSize (20, 20);
			nearLabel.setLocation (250, 250);
			add (carLabel);
			carLabel.setSize (20, 20);
			carLabel.setLocation (250, 250);
			add (keypad);
			keypad.setSize (20, 20);
			int keypadX = 250 + (int) (actr.model.Utilities.angle2pixels (10.0));
			keypad.setLocation (keypadX, 250);
		}

		getModel().runCommand ("(set-visual-frequency near .1)");
		getModel().runCommand ("(set-visual-frequency car .1)");

		accelBrake = 0;
		speed = 0;

		getModel().getVision().addVisual ("near", "near", "near", nearLabel.getX(), nearLabel.getY(), 1, 1, 10);
		getModel().getVision().addVisual ("car", "car", "car", carLabel.getX(), carLabel.getY(), 1, 1, 100);
		getModel().getVision().addVisual ("keypad", "keypad", "keypad", keypad.getX(), keypad.getY(), 1, 1);

		addPeriodicUpdate (Env.sampleTime);
		if (MatlabInterface.firstRun)
	    {
	      getModel().stop();
	      MatlabInterface.firstRun = false;
	    }
	}

	public void update (double time)
	{
		if (time <= endTime)
		{
			simulation.env.time = time - startTime;

			if (!Env.scenario.externalSimCar || getModel().getTime()<=0)
			{
			simulation.update();
			}
			
			if (Env.scenario.externalSimCar & getModel().getTime()>0)
			{
				
				getModel().addEvent (new actr.model.Event (getModel().getTime(), "task", "externalUpdate") 
				{
					public void action() 
					{
						externalUpdate();
					}
				});

				// Place vehicle control inputs into MatlabInterface
				// Opposite command will be used in Matlab when paused
				MatlabInterface.setControlValues(simulation.env.simcar.steerAngle,simulation.env.simcar.accelerator,simulation.env.simcar.brake);
				
				// Set model to stop running
				getModel().stop();

				// Set cogPause to true
				MatlabInterface.cogPause=true;
				
				// Set cogTime
				MatlabInterface.cogTime=time;
				
				// Print out status				
				//System.out.println("Time: " + getModel().getTime()  + " - External update scheduled and model set to stop");
				//System.out.println(getModel().getEvents().toString());

			}

			updateVisuals();
		}
		else getModel().stop();
	}

	public void externalUpdate()
	{
		//System.out.println("In externalUpdate at " + getModel().getTime());
		
		// set cogPause to false
		MatlabInterface.cogPause=false;
		
		// Retrieve dynamics update from MatlabInterface and copy it to simcar
		// Opposite command was used in Matlab when paused
		Vehicle tempVehicle = MatlabInterface.getVehicleState();
		
		simulation.env.simcar.p=tempVehicle.p;
		simulation.env.simcar.h=tempVehicle.h;
		simulation.env.simcar.speed=tempVehicle.speed;
		//modified by yue at 12_8
		simulation.env.autocar.speed=tempVehicle.speed;
		//modified by yue at 12_8
		// Do simulation update
		simulation.update();
		
	}

	void updateVisuals ()
	{
		Env env = simulation.env;
		if (env.simcar.nearPoint != null)
		{
			Coordinate cn = env.world2image (env.simcar.nearPoint);
			Coordinate cc = env.world2image (env.simcar.carPoint);
			//Coordinate cc = env.world2image (env.simcar.farPoint);

			if (cn == null || cc == null) env.done = true;
			else
			{
				nearLabel.setLocation (cn.x, cn.y);
				carLabel.setLocation (cc.x, cc.y);
				getModel().getVision().moveVisual ("near", cn.x, cn.y,cn.d);
				getModel().getVision().moveVisual ("car", cc.x, cc.y,cc.d);
				//				getModel().getVision().moveVisual ("near", cn.x+5, cn.y+10);
				//				getModel().getVision().moveVisual ("car", cc.x+5, cc.y+10);
				speed = env.simcar.speed;
			}
		}
	}

	double minSigned (double x, double y)
	{
		return (x>=0) ? Math.min (x,y) : Math.max (x,-y);
	}

	void doSteer (double na, double dna, double dfa, double dt)
	{
		Simcar simcar = simulation.env.simcar;

		// To support time delay implementation, add computed 
		// command changes to a queue. Oldest commands are at top
		// of linked list
// wo gai de 
		//System.out.println(na);
		double dsteer = (dna * steerFactor_dna)
				+ (dfa * steerFactor_dfa)
				+ (minSigned (na, steerNaMax) * steerFactor_na * dt);
		dsteer *= simulation.driver.steeringFactor;

		simcar.dsteer_time.add(simulation.env.time);
		simcar.dsteer_queue.add(dsteer);
		////modified by Yue, add a new variable "fa_state" to accumulate dfa
        simcar.fa_state +=dfa;
        ////
		// Saving original code
		/*
		if (simcar.speed >= 10.0)
		{
			double dsteer = (dna * steerFactor_dna)
			+ (dfa * steerFactor_dfa)
			+ (minSigned (na, steerNaMax) * steerFactor_na * dt);
			dsteer *= simulation.driver.steeringFactor;
			simcar.steerAngle += dsteer;
		}
		else simcar.steerAngle = 0;
		 */

	}

	void doAccelerate (double fthw, double dthw, double dt)
	{
		Simcar simcar = simulation.env.simcar;

		// To support time delay implementation, add computed 
		// command changes to a queue. Oldest commands are at top
		// of linked list

		double dacc = (dthw * accelFactor_dthw)
				+ (dt * (fthw - thwFollow) * accelFactor_thw);

		simcar.dacc_time.add(simulation.env.time);
		simcar.dacc_queue.add(dacc);

		// Saving original code
		/*
		if (simcar.speed >= 10.0)
		{
			double dacc = (dthw * accelFactor_dthw)
			+ (dt * (fthw - thwFollow) * accelFactor_thw);
			accelBrake += dacc;
			accelBrake = minSigned (accelBrake, 1.0);
		}
		else
		{
			accelBrake = .65 * (simulation.env.time / 3.0);
			accelBrake = minSigned (accelBrake, .65);
		}
		simcar.accelerator = (accelBrake >= 0) ? accelBrake : 0;
		simcar.brake = (accelBrake < 0) ? -accelBrake : 0;
		 */

	}

	boolean isCarStable (double na, double nva, double fva)
	{
		double f = 2.5;
		return (Math.abs(na) < .025*f) && (Math.abs(nva) < .0125*f) && (Math.abs(fva) < .0125*f);
		//return true;
	
	}

	double image2angle (double x, double d)
	{
		Env env = simulation.env;
		double px = env.simcar.p.x + (env.simcar.h.x * d);
		double pz = env.simcar.p.z + (env.simcar.h.z * d);
		Coordinate im = env.world2image (new Position (px, pz));
		try { return Math.atan2 (.5*(x-im.x), 450); }
		catch (Exception e) { return 0; }
	}

	public void eval (Iterator<String> it)
	{
		it.next(); // (
		String cmd = it.next();
		if (cmd.equals ("do-steer"))
		{
			double na = Double.valueOf (it.next());
			double dna = Double.valueOf (it.next());
			double dfa = Double.valueOf (it.next());
			double dt = Double.valueOf (it.next());
	// Modified by Yue *******************************
			FileWriter fw = null;
			try
			{
				//File f = new File("DoSteerFile.txt");
				File f = new File("ActionFile.txt");
				fw = new FileWriter(f,true);
			
			}
			catch (IOException e)
			{
				System.out.println("Output file creation failure");
			}
			PrintWriter pw = new PrintWriter(fw);
			//String s_na = na.toString();
			pw.println("S "+na+" "+dna+" "+dfa+" "+dt);
			//pw.println("S "+ cmd);
			pw.flush();
			try{
				fw.flush();
				pw.close();
				fw.close();
			}catch(IOException e){
				System.out.println("123");
			}
	//*****************************************************************
			doSteer (na, dna, dfa, dt);
		}
		else if (cmd.equals ("do-accelerate"))
		{
			double fthw = Double.valueOf (it.next());
			double dthw = Double.valueOf (it.next());
			double dt = Double.valueOf (it.next());
			/*  *********************************************************************
						FileWriter fw = null;
						try
						{
							//File f = new File("DoAcceFile.txt");
							File f = new File("ActionFile.txt");
							fw = new FileWriter(f,true);
						
						}
						catch (IOException e)
						{
							System.out.println("Output file creation failure");
						}
						PrintWriter pw = new PrintWriter(fw);
						pw.println("A "+fthw+" "+dthw+" "+dt);
						//pw.println("A "+ cmd);
						pw.flush();
						try{
							fw.flush();
							pw.close();
							fw.close();
						}catch(IOException e){
							System.out.println("123");
						}
				/***************************************************************
				 */
			doAccelerate (fthw, dthw, dt);
		}
	}

	public boolean evalCondition (Iterator<String> it)
	{
		it.next(); // (
		String cmd = it.next();
		if (cmd.equals ("is-car-stable") || cmd.equals ("is-car-not-stable"))
		{
			double na = Double.valueOf (it.next());
			double nva = Double.valueOf (it.next());
			double fva = Double.valueOf (it.next());
			boolean b = isCarStable(na,nva,fva);
			return cmd.equals("is-car-stable") ? b : !b;
		}
		else return false;
	}

	public double bind (Iterator<String> it)
	{
		try
		{
			it.next(); // (
			String cmd = it.next();
			if (cmd.equals ("image->angle"))
			{
				double x = Double.valueOf (it.next());
				double d = Double.valueOf (it.next());
				return image2angle (x, d);
			}
			else if (cmd.equals ("mp-time")) return simulation.env.time;
			else if (cmd.equals ("get-thw"))
			{
				double fd = Double.valueOf (it.next());
				double v = Double.valueOf (it.next());
				double thw = (v==0) ? 4.0 : fd/v;
				return Math.min (thw, 4.0);
			}
			else if (cmd.equals ("get-velocity")) return speed;
			else return 0;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			return 0;
		}
	}

	public int numberOfSimulations () { return 1; }

	public Result analyze (Task[] tasks, boolean output)
	{
		return null;
	}
}
