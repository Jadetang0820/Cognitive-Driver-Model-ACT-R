package actr.tasks.driving;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.*;
import java.io.*;

/**
 * A utility class with various useful methods.
 *  
 * 
 */
public class Utilities
{
	static DecimalFormat df1 = new DecimalFormat ("#.0");
	static DecimalFormat df2 = new DecimalFormat ("#.00");
	static DecimalFormat df3 = new DecimalFormat ("#.000");
	static DecimalFormat df4 = new DecimalFormat ("#.0000");
	static DecimalFormat df5 = new DecimalFormat ("#.0000");

	static Random random = new Random (System.currentTimeMillis());

	static int sign (double x) { return (x >= 0) ? 1 : -1; }
	static double sqr (double x) { return (x*x); }

	static double rotationAngle (double hx, double hz)
	{
		return (- 180 * (Math.atan2(hz,hx)) / Math.PI);
	}

	static double deg2rad (double x) { return x * (Math.PI / 180.0); }
	static double rad2deg (double x) { return x * (180.0 / Math.PI); }

	static double mps2mph (double x) { return x * 2.237; }
	static double mph2mps (double x) { return x / 2.237; }
	static double mph2kph (double x) { return x * 1.609; }
	static double kph2mph (double x) { return x / 1.609; }

	static int sec2ms (double x) { return (int) (Math.round(x*1000)); }

	static String randomize (String s)
	{
		String s2 = "";
		while (! s.equals(""))
		{
			int r = random.nextInt (s.length());
			s2 += s.substring (r, r+1);
			s = s.substring (0,r) + s.substring (r+1,s.length());
		}
		return s2;
	}

	static PrintStream uniqueOutputFile (String name) 
	{
		int num = 1;
		File file;
		String filename;
		do  {
			filename = name + num + ".txt";
			file = new File (filename);
			num++;
		} while (file.exists());
		PrintStream stream = null;
		try {
			stream = new PrintStream (new FileOutputStream (file));
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		return stream;
	}

	public static void setFullScreen (Frame frame)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = ge.getDefaultScreenDevice();
		device.setFullScreenWindow (frame);
		frame.validate ();
	}
}
