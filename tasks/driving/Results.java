package actr.tasks.driving;

/**
 * A class that defines useful measures for data collection.
 *  
 * 
 */
public class Results
{
	Driver driver;

	double taskTime;
	double taskLatDev;
	double taskLatVel;
	double headingError;
	int laneViolations;
	double taskSpeedDev;
	double detectionError;
	double brakeRT;

	public String toString ()
	{
		return "(" + taskTime + ", " + taskLatDev + ", " + taskLatVel + ", " + brakeRT + ", " + headingError + ", " + taskSpeedDev + ")";
	}
}
