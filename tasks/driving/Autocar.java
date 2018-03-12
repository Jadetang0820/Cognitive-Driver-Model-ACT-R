package actr.tasks.driving;

import java.awt.*;
import java.io.*;
import java.util.LinkedList;

/**
 * An automated car that drives itself down the road.
 *  
 * 
 */
public class Autocar extends Vehicle
{
	boolean braking = false;
	double lastBrakeTime = 0;
	double brakeSpacing = 4; // time between braking events


	//Think about making this a class in the future?

	double[] arcCentX = new double[8];
	double[] arcCentZ = new double[8];
	double[] arcStartAng = new double[8];
	int[] arcAngDir= {1, -1, -1, 1, -1, 1, 1, -1};
	//* Modified by Yue at 2018_2_6 (point5) add one variable startX2
	double startX,startX2,startSeg,P,A;
	//*(point5)
	//double cTheta,cR,arcSegLength,delX,s,subAng,tmpAng;
	double cTheta,cR,arcSegLength,delX,subAng,tmpAng;
	/// Modified by Yue, 12/8
	//* Modified by Yue at 2018_2_6 (point6) add one variable s2
	public double s = 0.0;
	public double s2 = 0.0;
	//*(point6)
	//* Modified by Yue at 2018_2_13 (point4)
	
	//*(point4)
	/// Modified by Yue, 12/8

	int nArc,nP,arcIndex;

	int autocarMode;
	public static String pathFile;
	public static double[] pathData;
	public static int currPathLoc = 4;

	//String pathFile;
	//public double[] pathData;
	//int currPathLoc = 4;  // Path length array location of first record
	boolean atLastPathPoint = false;

	//* Modified by Yue at 2018_2_6 (point1)
	public static String pathFile2;
	public static double[] pathData2;
	public static int currPathLoc2 = 4;
	boolean atLastPathPoint2 = false;
	//*(point1)
	//* Modified by Yue at 2018_2_13 (point1) record autocar2 position as a linklist
	LinkedList<Double> auto2x_queue,auto2z_queue;
	double p0_x,p1_x,p2_x;
	double p0_z,p1_z,p2_z;
	//*(point1)
	static double nextBrakeTime = 999999;
	
	public Autocar(Env env) throws IOException
	{
		super ();
		//* Modified by Yue at 2018_2_13 (point3)
		auto2x_queue = new LinkedList<Double>();
		auto2z_queue = new LinkedList<Double>();
	  /*auto2x_queue.add(0.0);
		auto2x_queue.add(0.0);
		auto2z_queue.add(41.5);
		auto2z_queue.add(41.5)*/;
		//*(point3)
		// Initialize variables
		
		speed = Env.scenario.autocarSpeed;
		startX = Env.scenario.autocarStartX;
		//* Modified by Yue at 2018_2_6 (point10)
		startX2 = Env.scenario.autocar2StartX;
		//*(point10)
		startSeg = Env.scenario.autocarStraightSeg;
		P = Env.scenario.autocarPathPeriod;
		A = Env.scenario.autocarPathAmp;

		autocarMode = Env.scenario.autocarMode;
		pathFile = new String(Env.scenario.pathFile);
		//* Modified by Yue at 2018_2_6 (point2)
		pathFile2 = new String(Env.scenario.pathFile);
		//*(point2)
		if (autocarMode == 0)
			computeArcPathParams(); // initialize the periodic path
		else
		{
			File pathFileHandle = new File(pathFile);

			if ( pathFileHandle.exists() && pathFileHandle.isFile())
			{
				int pathFileElements = (int) (pathFileHandle.length()/8);
				pathData = new double[pathFileElements];
				DataInputStream  pathFileStream = new DataInputStream(new FileInputStream(pathFile));
				for (int inputCount = 0; inputCount<pathFileElements;inputCount++)
				{
					pathData[inputCount]=pathFileStream.readDouble();
				}
				pathFileStream.close();
			}
		}
		//* Modified by Yue at 2018_2_6 (point3)
		File pathFileHandle2 = new File(pathFile2);

		if ( pathFileHandle2.exists() && pathFileHandle2.isFile())
		{
			int pathFileElements2 = (int) (pathFileHandle2.length()/8);
			pathData2 = new double[pathFileElements2];
			DataInputStream  pathFileStream2 = new DataInputStream(new FileInputStream(pathFile2));
			for (int input2Count = 0; input2Count<pathFileElements2;input2Count++)
			{
				pathData2[input2Count]=pathFileStream2.readDouble();
			}
			pathFileStream2.close();
		}
		//*(point3)
	}
	 public static void update_PATH_java()
			    throws IOException
			  {
			    currPathLoc = 4;
			    
			    pathFile = new String(Env.scenario.pathFile);
			    File pathFileHandle = new File(pathFile);
			    
			    int pathFileElements = (int)(pathFileHandle.length() / 8L);
			    pathData = new double[pathFileElements];
			    DataInputStream pathFileStream = new DataInputStream(new FileInputStream(pathFile));
			    for (int inputCount = 0; inputCount < pathFileElements; inputCount++) {
			      pathData[inputCount] = pathFileStream.readDouble();
			    }
			    pathFileStream.close();
	   //* Modified by Yue at 2018_2_6 (point4)  
                currPathLoc2 = 4;
			    
			    pathFile2 = new String(Env.scenario.pathFile);
			    File pathFileHandle2 = new File(pathFile2);
			    
			    int pathFileElements2 = (int)(pathFileHandle2.length() / 8L);
			    pathData2 = new double[pathFileElements2];
			    DataInputStream pathFileStream2 = new DataInputStream(new FileInputStream(pathFile2));
			    for (int input2Count = 0; input2Count < pathFileElements2; input2Count++) {
			      pathData2[input2Count] = pathFileStream2.readDouble();
			    }
			    pathFileStream2.close(); 
	   //*(point4)	    
			  }

	void update (Env env)
	{
		
		//s = speed*env.time;
		// Modified by Yue, 12/8
		s = s+speed*0.05;
		//* Modified by Yue at 2018_2_6 (point7)
		s2 = s2+speed*0.05;
		//*(point7)
		if (autocarMode==0)				// Constant speed parameterized path
		{

			if (s <= startSeg)
			{
				p.x = startX+s;
			}
			else if (s>startSeg)
			{
				s=s-startSeg;

				// Determine number of arc segments

				nArc=(int) Math.floor(s/arcSegLength);

				// Determine period number, zero-based

				nP=(int) Math.floor(nArc/8);

				// Determine subarc

				arcIndex=(int) (nArc-8*Math.floor(nArc/8));

				// Determine angle into final subarc

				subAng=(s-nArc*arcSegLength)/cR;

				// Compute position

				tmpAng=arcStartAng[arcIndex]+arcAngDir[arcIndex]*subAng;

				p.x=startX+startSeg+nP*P+arcCentX[arcIndex]+cR*Math.cos(tmpAng);
				p.z=arcCentZ[arcIndex]+cR*Math.sin(tmpAng);

				// Compute heading

				h.x=-arcAngDir[arcIndex]*Math.sin(tmpAng);
				h.z=arcAngDir[arcIndex]*Math.cos(tmpAng);

			}

		} // end autoCarMode = 0
		else if (autocarMode==1)    	// Constant speed custom path from file
		{
			
			//*(point3)
			boolean binFound = false;
			s=s+startX;
			//* Modified by Yue at 2018_2_6 (point8)
			boolean binFound2 = false;
			s2=s2+startX2;
			//*(point8)
			while (!binFound)
			{
				if (s >= pathData[currPathLoc] & s < pathData[currPathLoc+5])
				{
					binFound=true;
				}
				else if (s>=pathData[currPathLoc+5] && (currPathLoc+10)>pathData.length)
				{
					binFound=true;
					atLastPathPoint=true;
					//System.out.println("At end of path data");
				}
				else
				{
					currPathLoc+=5;
				}	

			}

			
			if (!atLastPathPoint)
			{
			double segLength=pathData[currPathLoc+5]-pathData[currPathLoc];

			double wgt1=(pathData[currPathLoc+5]-s)/segLength;
			double wgt2=(s-pathData[currPathLoc])/segLength;

			p.x=wgt1*pathData[currPathLoc-3]+wgt2*pathData[currPathLoc+2];
			p.z=wgt1*pathData[currPathLoc-1]+wgt2*pathData[currPathLoc+4];

			h.x=(pathData[currPathLoc+2]-pathData[currPathLoc-3])/segLength;
			h.z=(pathData[currPathLoc+4]-pathData[currPathLoc-1])/segLength;
			}
			
			if (atLastPathPoint)
			{
				h.x=pathData[currPathLoc+2] - env.simcar.p.x;
				h.z=pathData[currPathLoc+4] - env.simcar.p.z;

				double segLength=Math.sqrt(h.x*h.x+h.z*h.z);
				
				h.x=h.x/segLength;
				h.z=h.z/segLength;
				
				p.x=env.simcar.p.x+h.x*Math.max(11.0,Env.scenario.thwFollow*Env.scenario.simcarSpeed);
				p.z=env.simcar.p.z+h.z*Math.max(11.0,Env.scenario.thwFollow*Env.scenario.simcarSpeed);
			}
			
			//
			s=s-startX;
			//* Modified by Yue at 2018_2_6 (point9)
			while (!binFound2)
			{
				if (s2 >= pathData2[currPathLoc2] & s2 < pathData2[currPathLoc2+5])
				{
					binFound2=true;
				}
				else if (s2>=pathData2[currPathLoc2+5] && (currPathLoc2+10)>pathData2.length)
				{
					binFound2=true;
					atLastPathPoint2=true;
					//System.out.println("At end of path data");
				}
				else
				{
					currPathLoc2+=5;
				}	

			}

			
			if (!atLastPathPoint2)
			{
			double segLength2=pathData2[currPathLoc2+5]-pathData2[currPathLoc2];

			double wgt1_2=(pathData2[currPathLoc2+5]-s2)/segLength2;
			double wgt2_2=(s2-pathData2[currPathLoc2])/segLength2;

			autocar2p.x=wgt1_2*pathData2[currPathLoc2-3]+wgt2_2*pathData2[currPathLoc2+2];
			autocar2p.z=wgt1_2*pathData2[currPathLoc2-1]+wgt2_2*pathData2[currPathLoc2+4];

			autocar2h.x = (pathData2[currPathLoc2+2]-pathData2[currPathLoc2-3])/segLength2;
			autocar2h.z = (pathData2[currPathLoc2+4]-pathData2[currPathLoc2-1])/segLength2;
			}
			
			if (atLastPathPoint2)
			{
				autocar2h.x=pathData2[currPathLoc2+2] - env.simcar.p.x;
				autocar2h.z=pathData2[currPathLoc2+4] - env.simcar.p.z;

				double segLength2=Math.sqrt(autocar2h.x * autocar2h.x + autocar2h.z * autocar2h.z);
				
				autocar2h.x = autocar2h.x /segLength2;
				autocar2h.z = autocar2h.z /segLength2;
				
autocar2p.x=env.simcar.p.x+autocar2h.x*Math.max(11.0,Env.scenario.thwFollow*Env.scenario.simcarSpeed);

autocar2p.z=env.simcar.p.z+autocar2h.z*Math.max(11.0,Env.scenario.thwFollow*Env.scenario.simcarSpeed);
			}
			
			//
			s2=s2-startX2;
			//* Modified by Yue at 2018_2_13 (point2)
		  /*p0_x=auto2x_queue.removeFirst();
			p1_x=auto2x_queue.getLast();
			p2_x=autocar2p.x;
			p0_z=auto2z_queue.removeFirst();
			p1_z=auto2z_queue.getLast();
			p2_z=autocar2p.z;*/
			p0_x=pathData2[currPathLoc2-3];
			p1_x=autocar2p.x;
			p2_x=pathData2[currPathLoc2+2];
			p0_z=pathData2[currPathLoc2-1];
			p1_z=autocar2p.z;
			p2_z=pathData2[currPathLoc2+4];
			double dx1 =p1_x-p0_x;
			double dz1 =p1_z-p0_z;
			double dx2 =p2_x-p0_x;
			double dz2 =p2_z-p0_z;
			double area =Math.abs(dx1*dz2-dz1*dx2);
			double len0 =Math.hypot(p0_x-p1_x, p0_z-p1_z);
			double len1 =Math.hypot(p1_x-p2_x, p1_z-p2_z);
			double len2 =Math.hypot(p2_x-p0_x, p2_z-p0_z);
			double outproduct = (dx1*dz2-dx2*dz1)/(len0*len2);
		  if (Math.abs(outproduct)<1.00e-18)
		    {
				autocar2k =0.0;
			}
			else
			{
		        autocar2k = 4*area/(len0*len1*len2);
			}
			
			auto2x_queue.addLast(autocar2p.x);
		    auto2z_queue.addLast(autocar2p.z);
		 // Modified by Yue *******************************
			FileWriter fw = null;
			try
			{
				//File f = new File("DoSteerFile.txt");
				File f = new File("listsize.txt");
				fw = new FileWriter(f,true);
			
			}
			catch (IOException e)
			{
				System.out.println("Output file creation failure");
			}
			PrintWriter pw = new PrintWriter(fw);
			//String s_na = na.toString();
			pw.println(auto2x_queue+"\t"+p0_x+"\t"+p1_x+"\t"+p2_x+"\t"+p0_z+"\t"+p1_z+"\t"+p2_z+"\t"+dx1+"\t"+dz1+"\t"+dx2+"\t"+dz2+"\t"+area+"\t"+len0+"\t"+len1+"\t"+len2+"\t"+outproduct+"\t"+autocar2k);
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
		
			//*(point9)
		} // end autoCarMode = 1
		
		//*(point2)
	}

	void computeArcPathParams()
	{
		cTheta=2*Math.atan(4*A/P);
		cR=P/(8*Math.sin(cTheta));

		// Compute arc segment length
		arcSegLength=cR*cTheta;

		// Compute arc parameter
		delX=cR*Math.sin(cTheta);

		// Compute arc centers and starting angles

		arcCentX[0]=0;
		arcCentX[1]=2*delX;
		arcCentX[2]=2*delX;
		arcCentX[3]=4*delX;
		arcCentX[4]=4*delX;
		arcCentX[5]=6*delX;
		arcCentX[6]=6*delX;
		arcCentX[7]=8*delX;

		arcCentZ[0]=cR;
		arcCentZ[1]=-(cR-A);
		arcCentZ[2]=-(cR-A);
		arcCentZ[3]=cR;
		arcCentZ[4]=-cR;
		arcCentZ[5]=cR-A;
		arcCentZ[6]=cR-A;
		arcCentZ[7]=-cR;

		arcStartAng[0]=3*Math.PI/2;
		arcStartAng[1]=Math.PI/2+cTheta;
		arcStartAng[2]=Math.PI/2;
		arcStartAng[3]=3*Math.PI/2-cTheta;
		arcStartAng[4]=Math.PI/2;
		arcStartAng[5]=3*Math.PI/2-cTheta;
		arcStartAng[6]=3*Math.PI/2;
		arcStartAng[7]=Math.PI/2+cTheta;
	}

	void draw (Graphics g, Env env)
	{
		double rotAng,tmpX,tmpZ;

		rotAng = Math.acos(h.x);

		tmpX=-0.5*Math.cos(rotAng)-0.5*Math.sin(rotAng);
		tmpZ=-0.5*Math.sin(rotAng)+0.5*Math.cos(rotAng);

		Position pos1 = new Position(p.x+tmpX,p.z+tmpZ,0);
		Coordinate im1 = env.world2image (pos1);

		tmpX=-0.5*Math.cos(rotAng)+0.5*Math.sin(rotAng);
		tmpZ=-0.5*Math.sin(rotAng)-0.5*Math.cos(rotAng);

		Position pos2 = new Position(p.x+tmpX,p.z+tmpZ,1);
		Coordinate im2 = env.world2image (pos2);

		g.setColor (Color.blue);
		g.fillRect (im1.x, im2.y, im2.x-im1.x, im1.y-im2.y);
	}
}
