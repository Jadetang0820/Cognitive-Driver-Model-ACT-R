package actr.tasks.driving;

/**
 * A simple class for x,y coordinates plus depth.
 *  
 * 
 */
public class Coordinate
{
	int x, y;
	double d;
	
	Coordinate (int xArg, int yArg)
	{
		x = xArg;
		y = yArg;
		d = 0;
	}

	Coordinate (int xArg, int yArg, double dArg)
	{
		x = xArg;
		y = yArg;
		d = dArg;
	}
};
