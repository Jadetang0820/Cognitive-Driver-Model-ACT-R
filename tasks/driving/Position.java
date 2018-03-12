package actr.tasks.driving;

/**
 * A simple class representing position in space in the simulation.
 *  
 * 
 */
public class Position
{
	double x, z;
	double y;

	Position (double xArg, double zArg)
	{
		x = xArg;
		z = zArg;
		y = 0;
	}

	Position (double xArg, double zArg, double yArg)
	{
		x = xArg;
		z = zArg;
		y = yArg;
	}

	Position add (Position l2)
	{
		return new Position (x + l2.x, z + l2.z);
	}

	Position subtract (Position l2)
	{
		return new Position (x - l2.x, z - l2.z);
	}

	Position scale (double s)
	{
		return new Position (s * x, s * y);
	}

	Position average (Position l2, double weight)
	{
		return new Position (((1.0 - weight) * x) + (weight * l2.x),
				((1.0 - weight) * z) + (weight * l2.z));
	}

	Position normalize ()
	{
		double m = Math.sqrt ((x * x) + (z * z));
		return new Position (x/m, z/m);
	}

	Position rotate (double degrees)
	{
		double angle = (- 180 * (Math.atan2(z,x)) / Math.PI);
		angle += degrees;
		double rad = -angle * Math.PI / 180;
		return new Position (Math.cos(rad), Math.sin(rad));
	}

	Position myclone ()
	{
		return new Position (x, z, y);
	}
	
    public String toString ()
    {
    	return "("+Utilities.df2.format(x)+","+Utilities.df2.format(z)+")";
    }
};
