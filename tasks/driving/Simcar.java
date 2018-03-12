package actr.tasks.driving;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The driver's own vehicle and its controls.
 *  
 * 
 */
public class Simcar extends Vehicle
{
	Driver driver;
///modified by Yue, add a new variable "fa_state" to accumulate dfa
	double fa_state;
	double fa_dig;
	double v_ref;
	double v_err;
	double th_br_percent;
	double k_p;
	double F_tire;
	double Veh_mass;
	double kautocar2;
	///
	double steerAngle;
	double accelerator;
	double brake;
	long roadIndex;
	Position nearPoint;
	Position farPoint;
	Position carPoint;
	boolean externalSimCar;
	LinkedList<Double> dacc_queue,dacc_time,dsteer_queue,dsteer_time; 
	double timeDelay;
	double accelBrake = 0;

	public Simcar (Driver driver, Env env)
	{
		super();

		this.driver = driver;
		///modified by Yue, add a new variable "fa_state" to accumulate dfa
		fa_state=0;
		fa_dig=0;
		v_ref=0;
		v_err=0;
		th_br_percent=0;
		k_p=0.3;
		F_tire=2840;  //N
		Veh_mass=2688.0; //kg
		///
		steerAngle = 0;
		accelerator = 0;
		brake = 0;
		speed = Env.scenario.simcarSpeed;
		externalSimCar=Env.scenario.externalSimCar;
		timeDelay = Env.scenario.timeDelay;
		dacc_queue = new LinkedList<Double>();
		dacc_time = new LinkedList<Double>();
		dsteer_queue = new LinkedList<Double>();
		dsteer_time = new LinkedList<Double>();
	
		if (Env.scenario.autocarMode==1)
		{
			p.x=Autocar.pathData[1];
			p.z=Autocar.pathData[3];
			h.x=(Autocar.pathData[6]-Autocar.pathData[1])/Autocar.pathData[9];
			h.z=(Autocar.pathData[8]-Autocar.pathData[3])/Autocar.pathData[9];
			FileWriter fw = null;
			//$$$$$$$$$
			try
			{
				//File f = new File("DoSteerFile.txt");
				File f = new File("modeText.txt");
				fw = new FileWriter(f,true);
			
			}
			catch (IOException e)
			{
				System.out.println("Output file creation failure");
			}
			PrintWriter pw = new PrintWriter(fw);
			//String s_na = na.toString();
			pw.println(Env.scenario.autocarMode);
			//pw.println("S "+ cmd);
			pw.flush();
			try{
				fw.flush();
				pw.close();
				fw.close();
			}catch(IOException e){
				System.out.println("123");
			}
			//$$$$$$$$$$$$$$
		}
		//modified by Yue at Aug 2017///////////////////
		nearPoint = new Position(30,30);
		farPoint = new Position(50,50);
		carPoint = new Position(100,100);
//////////////////////////////////////////////////
	}

	int order = 6;
	int max_order = 10;
	double gravity = 9.8;
	double air_drag_coeff = .25;
	double engine_max_watts = 106000;
	double brake_max_force = 8000;
	double f_surface_friction = .2;
	double lzz = 2618;
	double ms = 1175;
	double a = .946;
	double b = 1.719;
	double caf = 48000;
	double car = 42000;
	double[] y = {0,0,0,0,0,0,0,0,0,0};
	double[] dydx = {0,0,0,0,0,0,0,0,0,0};
	double[] yout = {0,0,0,0,0,0,0,0,0,0};
	double heading = -999;
	double heading1 = -999;
	double heading2 = -999;
	double car_heading;
	double car_accel_pedal;
	double car_brake_pedal;
	double car_deltaf;
	double car_steer;
	double car_speed;
	double car_ke;

	void derivs (double y[], double dydx[])
	{
		double phi = y[1];
		double r = y[2];
		double beta = y[3];
		double ke = y[4];
		double u = (ke>0) ? Math.sqrt(ke*2/ms) : 0;
		double deltar = 0;
		double deltaf = car_deltaf;
		dydx[1] = r;
		if (u > 5)
		{
			dydx[2] = (2.0 * a * caf * deltaf - 2.0 * b * car * deltar - 
					2.0 * (a * caf - b * car) * beta - 
					(2.0 * (a*a * caf + b*b * car) * r / u)
					) / lzz ;
			dydx[3] = (2.0 * caf * deltaf + 2.0 * car * deltar - 
					2.0 * (caf + car) * beta -
					(ms * u + (2.0 * (a * caf - b * car) / u)) * r
					) / (ms * u);
		}
		else
		{
			dydx[1] = 0.0;
			dydx[2] = 0.0;
			dydx[3] = 0.0;
		}
		double pengine = car_accel_pedal * engine_max_watts;
		double fbrake = car_brake_pedal * brake_max_force;
		double fdrag = (f_surface_friction * ms * gravity) + (air_drag_coeff * u * u);
		dydx[4] = pengine - fdrag * u - fbrake * u;
		dydx[5] = u * Math.cos(phi);
		dydx[6] = u * Math.sin(phi);
	}

	void rk4 (int n, double x, double h)
	{
		double dym[] = {0,0,0,0,0,0,0,0,0,0};
		double dyt[] = {0,0,0,0,0,0,0,0,0,0};
		double yt[] = {0,0,0,0,0,0,0,0,0,0};
		double hh = h * 5;
		double h6 = h/6;
		int i;

		for (i=1;i<=n;i++) 
			yt[i]=y[i]+hh*dydx[i];
		derivs (yt, dyt);
		for (i=1;i<=n;i++) 
			yt[i]=y[i]+hh*dyt[i];
		derivs (yt, dym);
		for (i=1;i<=n;i++) 
		{
			yt[i]=y[i]+h*dym[i];
			dym[i] += dyt[i];
		}
		derivs (yt, dyt);
		for (i=1;i<=n;i++)
			yout[i]=y[i]+h6*(dydx[i]+dyt[i]+2.0*dym[i]);
		// Modified by Yue *******************************
		FileWriter fw = null;
		try
		{
			//File f = new File("DoSteerFile.txt");
			File f = new File("Statefile.txt");
			fw = new FileWriter(f,true);
		
		}
		catch (IOException e)
		{
			System.out.println("Output file creation failure");
		}
		PrintWriter pw = new PrintWriter(fw);
		//String s_na = na.toString();
		pw.println("State"+yout[1]+" "+yout[2]+" "+yout[3]+" "+yout[4]+" "+yout[5]+" "+yout[6]);
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
	}

	void updateDynamics (Env env)
	{
		//Road road = env.road;
		double time = env.time;
		double sampleTime = Env.sampleTime;

		if (heading2 == -999.0)
		{
			heading = heading1 = heading2 = Math.atan2 (h.z, h.x);
			yout[1] = y[1] = car_heading = heading;
			yout[2] = y[2] = 0.0;
			yout[3] = y[3] = 0.0;
		    //yout[4] = y[4] = car_ke = 235000; // 0.0; // kinetic energy > 0, otherwise unstable at start
	// this.yout[4] = (this.y[4] = this.car_ke = this.ms / 2.0D * Env.scenario.simcarSpeed * Env.scenario.simcarSpeed);
			yout[4] = y[4] = car_ke = ms / 2.0 * Env.scenario.simcarSpeed * Env.scenario.simcarSpeed;
			yout[5] = y[5] = p.x;
			yout[6] = y[6] = p.z;
			if (car_ke > 0.0) car_speed = Math.sqrt(2.0 * car_ke / ms);
			else car_speed = 0.0;
		}

		car_steer = steerAngle;
		car_accel_pedal = accelerator;
		car_brake_pedal = brake;

		// original had lines below; changing to linear steering function
		//if (car_steer < 0.0) car_deltaf = -0.0423 * Math.pow(-1.0*car_steer, 1.3);
		//else car_deltaf =  0.0423 * Math.pow(car_steer,1.3);
		car_deltaf = 0.0423 * car_steer;

		double forcing =  0.125 * (0.01 * Math.sin(2.0 * 3.14 * 0.13 * time + 1.137) + 
				0.005 * Math.sin(2.0 * 3.14 * 0.47 * time + 0.875));                                
		car_deltaf +=  forcing;

		derivs (y, dydx);
		rk4 (order, time, sampleTime);

		y[1] = car_heading = yout[1];
		y[2] = yout[2];
		y[3] = yout[3];
		y[4] = car_ke = yout[4];
		y[5] = p.x = yout[5];
		y[6] = p.z = yout[6];

		if (car_ke > 0.0) car_speed = Math.sqrt(2.0 * car_ke / ms);
		else car_speed = 0.0;
		

		h.x = Math.cos (car_heading);
		h.z = Math.sin (car_heading);

		heading2 = heading1;
		heading1 = heading;
		heading = car_heading;

		speed = car_speed;

		if (Env.scenario.simCarConstantSpeed)
		{
			//double fullspeed = Utilities.mph2mps (Env.scenario.simCarMPH);
			// I'm overriding original constant speed mode
			double fullspeed = Env.scenario.simcarSpeed;
			speed=fullspeed;
			//if (speed < fullspeed) speed += .1;
			//else speed = fullspeed;
		}
		else
		{
			speed = car_speed;
		}

		/*long i = Math.max (1, roadIndex);
		long newi = i;
		Position nearloc = (road.middle (i)).subtract (p);
		double norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
		double mindist = norm;
		boolean done = false;
		while (!done)
		{
			i += 1;
			nearloc = (road.middle (i)).subtract (p);
			norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
			if (norm < mindist)
			{
				mindist = norm;
				newi = i;
			}
			else done = true;
		}
		Position vec1 = (road.middle (newi)).subtract (p);
		Position vec2 = (road.middle (newi)).subtract (road.middle (newi-1));
		double dotprod = - ((vec1.x * vec2.x) + (vec1.z * vec2.z));
		double fracdelta;
		if (dotprod < 0)
		{
			newi --;
			fracdelta = 1.0 + dotprod;
		}
		else fracdelta = dotprod;

		fracIndex = newi + fracdelta;
		roadIndex = newi; */
	}

	void update (Env env)
	{
		
		// Check if new control inputs need to be applied
		// If so, apply them and remove applied commands from
		// control queues

		double relTime = env.time-timeDelay;
		boolean steerDone = false;
		boolean accDone = false;
		int nSteer,nAcc;


		// Update steering (Includes time delay command queue)

		while (!steerDone)
		{
			nSteer = dsteer_time.size();

			if (nSteer == 0 || (nSteer > 0 && relTime < dsteer_time.getFirst()))
			{
				steerDone = true;
			}
			else if (nSteer > 0 && relTime >= dsteer_time.getFirst())
			{
				// Apply first command and remove
				if (speed >= 1.0)
				{
					steerAngle += dsteer_queue.removeFirst();
					dsteer_time.removeFirst();
				}
				else 
				{
					steerAngle = 0;
					dsteer_queue.removeFirst();
					dsteer_time.removeFirst();
				}

			}

		}


		// Update acceleration (Includes time delay command queue)

		while (!accDone)
		{
			nAcc = dacc_time.size();

			if (nAcc == 0 || (nAcc > 0 && relTime < dacc_time.getFirst()))
			{
				accDone = true;
			}
			else if (nAcc > 0 && relTime >= dacc_time.getFirst())
			{
				// Apply first command and remove
				if (speed >= 1.0)
				{

					accelBrake += dacc_queue.removeFirst();
					accelBrake = minSigned (accelBrake, 1.0);
					dacc_time.removeFirst();
				}
				else
				{
					accelBrake = .65 * (env.time / 3.0);
					accelBrake = minSigned (accelBrake, .65);
					dacc_queue.removeFirst();
					dacc_time.removeFirst();
				}
			}

			//accelerator = (accelBrake >= 0) ? accelBrake : 0;
			//brake = (accelBrake < 0) ? -accelBrake : 0;
/////////////////////////////////
			//fa_dig=Math.ceil(fa_state/4.0*1000)/1000;
			kautocar2=env.autocar.autocar2k;
			v_ref=Math.sqrt(4*F_tire/(Veh_mass*Math.max(kautocar2,0.001)));
			v_err=v_ref-speed;
			th_br_percent=k_p*v_err;
			if (th_br_percent >= 1.0)
			{
				th_br_percent=1.0;
			}
			else if (th_br_percent <= -1.0)
			{
				th_br_percent=-1.0;
			}	
			else
			{
				th_br_percent+=0;
			}
			accelerator = (th_br_percent >= 0) ? th_br_percent : 0;
			brake = (th_br_percent < 0) ? -th_br_percent : 0;
			// Modified by Yue *******************************
						FileWriter fw = null;
						try
						{
							//File f = new File("DoSteerFile.txt");
							File f = new File("v_refandkauto2.txt");
							fw = new FileWriter(f,true);
						
						}
						catch (IOException e)
						{
							System.out.println("Output file creation failure");
						}
						PrintWriter pw = new PrintWriter(fw);
						//String s_na = na.toString();
						pw.println(env.time+" "+v_ref+" "+kautocar2+" "+speed);
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
			///////////////////////////////		
		}

		// If using internal model, update dynamics

		if (!externalSimCar)
		{
			updateDynamics (env);
		}

		//nearPoint = env.road.nearPoint (this, 2);
		//farPoint = env.road.farPoint (this, null, 2);

		nearPoint = new Position(p.x+MatlabInterface.nearPoint*h.x,p.z+MatlabInterface.nearPoint*h.z);
		//nearPoint = new Position(p.x+10*h.x,p.z+10*h.z);
		farPoint = new Position(p.x+100*h.x,p.z+100*h.z);
		carPoint = env.autocar.p;
	}

	void draw (Graphics g, Env env)
	{
		int dashHeight = 80;
		g.setColor (Color.black);
		g.fillRect (0, Env.envHeight - dashHeight, Env.envWidth, dashHeight);

		int steerX = 160;
		int steerY = Env.envHeight - 20;
		int steerR = 50;
		g.setColor (Color.darkGray);
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform saved = g2d.getTransform();
		g2d.translate (steerX, steerY);
		g2d.rotate (steerAngle);
		g2d.setStroke (new BasicStroke (10));
		g2d.drawOval (-steerR, -steerR, 2*steerR, 2*steerR);
		g2d.fillOval (-steerR/4, -steerR/4, steerR/2, steerR/2);
		g2d.drawLine (-steerR, 0, +steerR, 0);
		g2d.setTransform (saved);
	}

	double minSigned (double x, double y)
	{
		return (x>=0) ? Math.min (x,y) : Math.max (x,-y);
	}

	double devscale = .0015;
	double devx = -.7;
	double devy = .5;
	double ifc2gl_x (double x) { return devx + (devscale * -(x - Driving.centerX)); }
	double ifc2gl_y (double y) { return devy + (devscale * -(y - Driving.centerY)); }
	double gl2ifc_x (double x) { return Driving.centerX - ((x - devx) / devscale); }
	double gl2ifc_y (double y) { return Driving.centerY - ((y - devy) / devscale); }
}
