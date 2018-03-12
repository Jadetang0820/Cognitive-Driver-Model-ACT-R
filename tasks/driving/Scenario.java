package actr.tasks.driving;

/**
 * A class that defines the particular scenario represented by the driving environment.
 *  
 * 
 */
public class Scenario
{
	
	// Most of this first section are no longer being used
	boolean curvedRoad = false;
	
	int simCarMPH = 55;
	boolean leadCarConstantSpeed = false;
	int leadCarMPH = 55;
	boolean leadCarBrakes = true;
	int drivingMinutes = 15;
	int timeBetweenTrials = 240;
	boolean baselineOnly = false;
	
	// This section starts the relevant values for new functionality
	public int autocarMode = 0;		// 0 = analytic, 1 = waypoint
	
	public double timeDelay=0; // Could apply to any mode
	public double autocarSpeed = 20; // Applies to modes 0 and 1
	public double autocarStartX = 20;
	
	//* Modified by Yue at 2018_2_6 (point11)
	public double autocar2StartX = 20;
	//*(point11)
	// Applies to mode 0
	public double autocarStraightSeg = 100;
	public double autocarPathAmp = 10;
	public double autocarPathPeriod = 500;
	
	public boolean simCarConstantSpeed = false; // false;
	public double simcarSpeed = 20; // Really just initial speed except for constant mode
	
	// Applies to mode 1
	public String pathFile = new String("D:\\COG_Model\\Paths\\2obs_cogPath");
	//
	//public double scurr = 0.0;
	
	// Turn on external vehicle model support
	public boolean externalSimCar = false;
	
	public String outputBaseName = new String("D:\\COG_Model\\debug_test");
	
	public double steerScale = .6; // .85
	public double accelScale = .4;
	public double steerFactor_dfa = (16 * steerScale);
	public double steerFactor_dna = (4 * steerScale);
	public double steerFactor_na  = (3 * steerScale);
	public double steerFactor_fa  = (0 * steerScale);
	public double accelFactor_thw  = (1 * accelScale);
	public double accelFactor_dthw = (3 * accelScale);
	public double steerNaMax = .07;
	public double thwFollow = 1.0;
	public double thwMax = 4.0;

	public double startTime=0;
	public double endTime=100;
	public double accelBrake=0;
	public double speed=0;
	
	public Scenario () { }

	String writeString ()
	{
		String s = new String ("");
		s += ((curvedRoad) ? 1 : 0) + "\t";
		s += ((simCarConstantSpeed) ? 1 : 0) + "\t";
		s += Integer.toString(simCarMPH) + "\t";
		s += ((leadCarConstantSpeed) ? 1 : 0) + "\t";
		s += Integer.toString(leadCarMPH) + "\t";
		s += ((leadCarBrakes) ? 1 : 0) + "\t";
		s += drivingMinutes + "\t";
		s += timeBetweenTrials;
		return s;
	}
	
//	static Scenario readString (MyStringTokenizer st)
//	{
//		Scenario s = new Scenario();
//		s.curvedRoad = (st.nextInt() == 1);
//		s.simCarConstantSpeed = (st.nextInt() == 1);
//		s.simCarMPH = st.nextInt();
//		s.leadCarConstantSpeed = (st.nextInt() == 1);
//		s.leadCarMPH = st.nextInt();
//		s.leadCarBrakes = (st.nextInt() == 1);		
//		s.drivingMinutes = st.nextInt();
//		s.timeBetweenTrials = st.nextInt();
//		return s;
//	}
}
