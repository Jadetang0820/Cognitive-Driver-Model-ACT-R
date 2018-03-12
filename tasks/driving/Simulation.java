package actr.tasks.driving;

import java.util.*;
import java.awt.Graphics;
import java.lang.Math;
import java.io.*;

import actr.model.Model;

/**
 * A class that defines the entire simulation include driver, scenario, and samples.
 *  
 * 
 */
public class Simulation
{
	Model model;
	Driver driver;
	Scenario scenario;
	Env env = null;
	Vector<Sample> samples = new Vector<Sample> ();
	Results results = null;
	FileWriter fw = null;
	
	Simulation (Model model)
	{
		this.model = model;
		driver = new Driver (model, "Driver", 25, 1.0f, 1.0f);
		if (MatlabInterface.setNewScenario)
		{
			scenario = MatlabInterface.newScenario;
		}
		else
		{
			scenario = new Scenario();
		}
		
		env = new Env (driver, scenario);
		
		try
		{
		fw = new FileWriter(env.scenario.outputBaseName+".txt");
		}
		catch (IOException ioe)
		{
			System.out.println("Output file creation failure");
		}
		
		Sample tmpSample = new Sample();
		tmpSample=recordSample(env);
		
		samples.add (tmpSample);
		
		String tmpString = new String(tmpSample.toString());
		
		//System.out.println(tmpString);
		
		try
		{
			fw.write(tmpString);
			fw.flush();
		}
		catch (IOException ioe)
		{
			System.out.println("Write to output file failure");
		}
		
		
		
	}

	synchronized void update ()
	{
		env.update ();
		
		Sample tmpSample = new Sample();
		tmpSample=recordSample(env);
		
		samples.add (tmpSample);
		//System.out.println(tmpSample.toString());

		try
		{
		fw.write(tmpSample.toString());
		fw.flush();
		}
		catch (IOException ioe)
		{
			System.out.println("Write to output file failure");
		}
	}

	Results getResults ()
	{
		results = analyze ();
		return results;
	}

	int numSamples ()
	{
		return samples.size();
	}

	Sample recordSample (Env env)
	{
		Sample s = new Sample ();
		s.time = env.time;
		s.simcarPos = env.simcar.p.myclone();
		s.simcarHeading = env.simcar.h.myclone();
		s.simcarFracIndex = env.simcar.fracIndex;
		s.simcarSpeed = env.simcar.speed;
		s.simcarRoadIndex = env.simcar.roadIndex;
		if (env.simcar.nearPoint != null)
		{
			s.nearPoint = env.simcar.nearPoint.myclone();
			s.farPoint = env.simcar.farPoint.myclone();
			s.carPoint = env.simcar.carPoint.myclone();
		}
		s.steerAngle = env.simcar.steerAngle;
		s.accelerator = env.simcar.accelerator;
		s.brake = env.simcar.brake;
		s.autocarPos = env.autocar.p.myclone();
		s.autocarHeading = env.autocar.h.myclone();
		//* Modified by Yue at 2018_2_6 (point12) add 2 positions of the 2nd autocar: autocar2Pos, autocar2Heading
		s.autocar2Pos = env.autocar.autocar2p.myclone();
		s.autocar2Heading = env.autocar.autocar2h.myclone();
		//*(point12)
		//s.autocarFracIndex = env.autocar.fracIndex;
		s.autocarSpeed = env.autocar.speed;
		s.autocarBraking = env.autocar.braking;
		//		s.eyeLocation = env.simcar.simDriver.model.vision.eyeLocation;
		//		if (s.eyeLocation != null) s.eyeLocation = s.eyeLocation.myclone();
		//		s.handLocation = env.simcar.simDriver.model.motor.handLocation;
		//		if (s.handLocation != null) s.handLocation = s.handLocation.myclone();
		//		s.handMoving = (env.simcar.simDriver.model.motor.handLocationNext != null);
		//		s.listening = ! env.simcar.simDriver.model.vision.auralFree();

		//		if (env.simcar.simDriver.model.goals.size() > 0)
		//			s.inDriveGoal = (env.simcar.simDriver.model.goals.elementAt(0).getClass() == DriveGoal.class);
		//
		//		s.event = env.simcar.simDriver.model.event;
		// TEMP s.lanepos = env.road.vehicleLanePosition (env.simcar);

		return s;
	}

	synchronized void draw (Graphics g)
	{
		if (env != null) env.draw (g);
	}

	double rotAngle (double hx, double hz)
	{
		return (- 180 * (Math.atan2(hz,hx)) / Math.PI);
	}

	double headingError (Sample s)
	{
		/*Road.Segment s2 = env.road.getSegment ((int) s.simcarRoadIndex);
		Road.Segment s1 = env.road.getSegment ((int) s.simcarRoadIndex - 1);
		Position rh = s2.middle.subtract (s1.middle);
		rh.normalize();
		return Math.abs (rotAngle (rh.x, rh.z) - rotAngle (s.simcarHeading.x, s.simcarHeading.z));*/
		return 1.0; // temp line
	}

	boolean lookingAhead (Sample s)
	{
		return true;
		//return (s != null && s.eyeLocation != null && s.eyeLocation.x < 350);
	}

	Results analyze ()
	{
		double startTime = 0;
		double stopTime = -1000;

		int numTasks = 0;
		int numTaskSamples = 0;
		double sumTime = 0;
		double sumLatDev = 0;
		double sumLatVel = 0;
		double sumSpeedDev = 0;
		double numTaskDetects = 0, numTaskDetectsCount = 0;
		double sumBrakeRTs = 0, numBrakeRTs = 0, lastBrakeTime = 0;
		boolean brakeEvent = false;
		double[] headingErrors = new double[samples.size()];
		int laneViolations = 0;
		boolean lvDetected = false;

		for (int i=1 ; i<samples.size() ; i++)
		{
			Sample s = samples.elementAt (i);
			Sample sprev = samples.elementAt (i-1);

			if ((s.event > 0) || (s.time < stopTime + 5.0))
			{
				numTaskSamples ++;
				double latdev = 3.66 * (s.lanepos - 2.5);
				sumLatDev += (latdev * latdev);
				sumLatVel += Math.abs ((3.66 * (s.lanepos - sprev.lanepos)) / Env.sampleTime);
				sumSpeedDev += (s.simcarSpeed - s.autocarSpeed) * (s.simcarSpeed - s.autocarSpeed);

				if ((s.event > 0) || (s.time < stopTime))
				{
					numTaskDetectsCount ++;
					if (lookingAhead(s))
					{
						numTaskDetects += 1;
						//if (s.listening) numTaskDetects -= .1;
					}
					//if (s.inDriveGoal) numTaskDetects ++;
				}

				//if ((s.event > 0) || (s.time < stopTime))
				//{
				if (((s.event > 0) || (s.time < stopTime))
						&& !brakeEvent && (s.autocarBraking && !sprev.autocarBraking))
				{
					brakeEvent = true;
					lastBrakeTime = s.time;
				}
				if (brakeEvent && !s.autocarBraking)
				{
					brakeEvent = false;
				}
				if (brakeEvent && (s.brake > 0))
				{
					//System.out.println ("BrakeRT: " + (s.time - lastBrakeTime));
					sumBrakeRTs += (s.time - lastBrakeTime);
					numBrakeRTs ++;
					brakeEvent = false;
				}
				//}

				headingErrors[numTaskSamples-1] = headingError (s);
				if (!lvDetected && (Math.abs(latdev) > (1.83-1.0))) { laneViolations ++;  lvDetected = true; }
			}

			if ((s.event == 1) && (sprev.event == 0))
			{
				startTime = s.time;
				lvDetected = false;
				brakeEvent = false;
			}
			else if ((s.event == 0) && (sprev.event == 1))
			{
				numTasks ++;
				stopTime = s.time;
				sumTime += (stopTime - startTime);
			}
		}

		Results r = new Results ();
		//		r.ifc = ifc;
		//		r.task = task;
		r.driver = driver;
		//		if (r.task.numActions() > 0) r.taskTime = sumTime / numTasks;
		//		else r.taskTime = 0;
		r.taskLatDev = Math.sqrt (sumLatDev / numTaskSamples);
		r.taskLatVel  = sumLatVel / numTaskSamples;
		r.taskSpeedDev = Math.sqrt (sumSpeedDev / numTaskSamples);

		r.detectionError = (numTaskSamples==0) ? 0 : (1.0 - (1.0 * numTaskDetects / numTaskDetectsCount));

		r.brakeRT = (numBrakeRTs==0) ? 0 : (sumBrakeRTs / numBrakeRTs);

		Arrays.sort (headingErrors, 0, numTaskSamples-1);
		int heIndex = (int) (0.9 * numTaskSamples);
		r.headingError = headingErrors[heIndex];

		r.laneViolations = laneViolations;

		return r;
	}
}
