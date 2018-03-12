package actr.tasks.driving;

/**
 * A general vehicle class (subclassed by other classes).
 *  
 * 
 */
public class Vehicle
{
	Position p;
	Position h;
	//* Modified by Yue at 2018_2_6 
	// To add another Autocar (for adjusting speed) in the previous Autocar class
	Position autocar2p;
	Position autocar2h;
	double autocar2k;
	//*
	double speed;
	double fracIndex;
	
	
	public Vehicle()
    {
		p = new Position (0, 0);
		h = new Position (1, 0);
		//* Modified by Yue at 2018_2_6 
		// To add another Autocar (for adjusting speed) in the previous Autocar class
	    autocar2p = new Position (0, 0);
		autocar2h = new Position (1, 0);
		autocar2k = 0.0;
		//*
		fracIndex = 0;
		speed = 0;
	}
}
