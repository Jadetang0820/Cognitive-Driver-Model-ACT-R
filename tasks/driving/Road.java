package actr.tasks.driving;

import java.util.*;
import java.awt.*;
import java.lang.Math;

/**
 * The primary class that defines a road.
 *  
 * 
 */
public class Road
{
	Vector<Segment> segments = null;
	int lanes = 3;

	public Road ()
	{
	}

	public class Segment
	{
		Position left, middle, right;
		Position h;
		Position l_left, r_right;
		Position ll_left, rr_right;
		Position lll_left, rrr_right;
		Position ll_mid, lr_mid, rl_mid, rr_mid;
		Position l_lmid, r_lmid, l_rmid, r_rmid;

		Segment (double a1, double a2, double a3, double a4, double a5, double a6)
		{
			left = new Position (a1, a2);
			middle = new Position (a3, a4);
			right = new Position (a5, a6);

			h = new Position (right.x - left.x, right.z - left.z);
			h = h.normalize ();

			double HALF_STRIPW = 0.08;
			double STRIPW = (2*HALF_STRIPW);
			double SHOULDER = 1.5;
			double WALL = 40;

			double dx = .05 * h.x;
			double dz = .05 * h.z;

			l_left = new Position (left.x - 2*STRIPW * h.x - dx, left.z  - 2*STRIPW * h.z - dz);
			r_right = new Position (right.x + 2*STRIPW * h.x + dx, right.z + 2*STRIPW * h.z + dz);

			ll_left = new Position (left.x - SHOULDER * h.x - dx, left.z - SHOULDER * h.z - dz);
			rr_right = new Position (right.x + SHOULDER * h.x + dx, right.z + SHOULDER * h.z + dz);

			lll_left = new Position (left.x - WALL * h.x, left.z - WALL * h.z);
			rrr_right = new Position (right.x + WALL * h.x, right.z + WALL * h.z);

			ll_mid = new Position (middle.x - 3*HALF_STRIPW * h.x, middle.z - 3*HALF_STRIPW * h.z);
			lr_mid = new Position (middle.x - HALF_STRIPW * h.x, middle.z - HALF_STRIPW * h.z);
			rl_mid = new Position (middle.x + HALF_STRIPW * h.x, middle.z + HALF_STRIPW * h.z);
			rr_mid = new Position (middle.x + 3*HALF_STRIPW * h.x, middle.z + 3*HALF_STRIPW * h.z);

			if (lanes == 4)
			{
				l_lmid = new Position ((0.5*ll_mid.x + 0.5*left.x) - HALF_STRIPW * h.x,
						(0.5*ll_mid.z + 0.5*left.z) - HALF_STRIPW * h.z);
				r_lmid = new Position ((0.5*ll_mid.x + 0.5*left.x) + HALF_STRIPW * h.x,
						(0.5*ll_mid.z + 0.5*left.z) + HALF_STRIPW * h.z);

				l_rmid = new Position ((0.5*rr_mid.x + 0.5*right.x) - HALF_STRIPW * h.x,
						(0.5*rr_mid.z + 0.5*right.z) - HALF_STRIPW * h.z);
				r_rmid = new Position ((0.5*rr_mid.x + 0.5*right.x) + HALF_STRIPW * h.x,
						(0.5*rr_mid.z + 0.5*right.z) + HALF_STRIPW * h.z);
			}
			else if (lanes == 3)
			{
				l_lmid = new Position ((0.666*middle.x + 0.334*left.x) - HALF_STRIPW * h.x,
						(0.666*middle.z + 0.334*left.z) - HALF_STRIPW * h.z);
				r_lmid = new Position ((0.666*middle.x + 0.334*left.x) + HALF_STRIPW * h.x,
						(0.666*middle.z + 0.334*left.z) + HALF_STRIPW * h.z);

				l_rmid = new Position ((0.666*middle.x + 0.334*right.x) - HALF_STRIPW * h.x,
						(0.666*middle.z + 0.334*right.z) - HALF_STRIPW * h.z);
				r_rmid = new Position ((0.666*middle.x + 0.334*right.x) + HALF_STRIPW * h.x,
						(0.666*middle.z + 0.334*right.z) + HALF_STRIPW * h.z);
			}
		}
	}

	void startup ()
	{
		boolean curved = Env.scenario.curvedRoad;
		segments = new Vector<Segment> ();

		Position p = new Position (0.0, 0.0);
		Position h = new Position (1.0, 0.0);
		h = h.normalize ();

		int seglen = 200;
		int segcount = 0;
		boolean curving = false;
		double da = 0;
		double dascale = .02;
		double d = lanes*3.66/2.0;

		for (int i=1 ; i<=40000 ; i++)
		{
			if (segcount >= seglen)
			{
				segcount = 0;
				seglen = 100;
				if (curved)
				{
					curving = !curving;
					if (curving) da = ((da > 0) ? -1 : +1) * dascale * 17; // (i % 17);
				}
			}
			if (curving) h = h.rotate (da);
			p = p.add (h);
			Segment s = new Segment (p.x + d*h.z, p.z - d*h.x, p.x, p.z, p.x - d*h.z, p.z + d*h.x);
			segments.addElement (s);
			segcount++;
		}
	}

	Segment getSegment (int i)
	{
		return (Segment) (segments.elementAt (i));
	}

	Position location (double fracIndex, double lanePos)
	{
		int i = (int) (Math.floor (fracIndex));
		double r = fracIndex - i;
		double laner = (lanePos - 1) / 3;
		if (i == fracIndex)
		{
			Position locL = getSegment(i).left;
			Position locR = getSegment(i).right;
			return locL.average (locR, laner);
		}
		else
		{
			Position loc1L = getSegment(i).left;
			Position loc1R = getSegment(i).right;
			Position loc1 = loc1L.average (loc1R, laner);
			Position loc2L = getSegment(i+1).left;
			Position loc2R = getSegment(i+1).right;
			Position loc2 = loc2L.average (loc2R, laner);
			return loc1.average (loc2, r);
		}
	}

	Position left (double fracIndex)
	{ return location (fracIndex, 4); }

	Position left (double fracIndex, int lane)
	{ return location (fracIndex, lane+1); }

	Position middle (double fracIndex)
	{ return location (fracIndex, 2.5); }

	Position middle (double fracIndex, int lane)
	{ return location (fracIndex, lane+.5); }

	Position right (double fracIndex)
	{ return location (fracIndex, 1); }

	Position right (double fracIndex, int lane)
	{ return location (fracIndex, lane); }

	Position heading (double fracIndex)
	{
		Position locdiff = (middle (fracIndex+1)).subtract (middle (fracIndex-1));
		return locdiff.normalize ();
	}

	void vehicleReset (Vehicle v, int lane, double fracIndex)
	{
		Position p = middle (fracIndex, lane);
		Position h = heading (fracIndex);
		v.p.x = p.x;
		v.p.z = p.z;
		v.h.x = h.x;
		v.h.z = h.z;
		v.fracIndex = fracIndex;
	}

	public static boolean sign (double x) { return (x >= 0); }
	public static double sqr (double x) { return (x*x); }

	double vehicleLanePosition (Vehicle v)
	{
		double i = v.fracIndex;
		Position lloc = left (i);
		Position rloc = right (i);
		Position head = heading (i);
		double ldx = head.x * (v.p.z - rloc.z);
		double ldz = head.z * (v.p.x - rloc.x);
		double wx = head.x * (lloc.z - rloc.z);
		double wz = head.z * (lloc.x - rloc.x);
		double ldist = Math.abs (ldx) + Math.abs (ldz);
		double width = Math.abs (wx) + Math.abs (wz);
		double lanepos = (ldist / width) * 3;
		if (((Math.abs(wx) > Math.abs(wz)) && (sign(ldx) != sign(wx)))
				|| ((Math.abs(wz) > Math.abs(wx)) && (sign(ldz) != sign(wz))))
			lanepos = -lanepos;
		lanepos += 1;
		return lanepos;
	}

	int vehicleLane (Vehicle v)
	{
		return (int) Math.floor (vehicleLanePosition (v));
	}

	double nearDistance = 10.0;
	double farDistance = 100.0;
	double nearTime = 0.5;
	double farTime = 4.0;

	Position nearPoint (Simcar simcar)
	{ return middle (simcar.fracIndex + nearDistance); }

	Position nearPoint (Simcar simcar, int lane)
	{ return middle (simcar.fracIndex + nearDistance, lane); }

	String fpText = "";
	double fpTPfracIndex = 0;

	Position farPoint (Simcar simcar, Vector<Autocar> autocars, int lane)
	{
		double fracNearestRP = simcar.fracIndex;
		long nearestRP = (int) Math.floor (fracNearestRP);
		long j = nearestRP + 1;
		Position simcarLoc = new Position (simcar.p.x, simcar.p.z);
		int turn = 0; // left=1, right=2
		double aheadMin = nearDistance + 10;
		double aheadMax = Math.max (aheadMin, simcar.speed * farTime);

		int rln = (lane != 0) ? lane : 1;
		int lln = (lane != 0) ? lane : 2;

		Position h_l = (left (j, lln)).subtract (simcarLoc);
		Position hrd_l = (left (j, lln)).subtract (left (j-1, lln));
		Position h_r = (right (j, rln)).subtract (simcarLoc);
		Position hrd_r = (right (j, rln)).subtract (right (j-1, rln));

		double lxprod1 = (h_l.x * hrd_l.z) - (h_l.z * hrd_l.x);
		double norm_lxp1 = Math.abs (lxprod1 / (Math.sqrt (sqr(h_l.x) + sqr(h_l.z))
				+ Math.sqrt (sqr(hrd_l.x) + sqr(hrd_l.z))));
		double rxprod1 = (h_r.x * hrd_r.z) - (h_r.z * hrd_r.x);
		// note: below, lisp code has lxprod1 instead!!
		double norm_rxp1 = Math.abs (rxprod1 / (Math.sqrt (sqr(h_r.x) + sqr(h_r.z))
				+ Math.sqrt (sqr(hrd_r.x) + sqr(hrd_r.z))));

		boolean go_on = true;

		while (go_on)
		{
			j += 1;

			h_l = (left (j, lln)).subtract (simcarLoc);
			hrd_l = (left (j, lln)).subtract (left (j-1, lln));
			h_r = (right (j, rln)).subtract (simcarLoc);
			hrd_r = (right (j, rln)).subtract (right (j-1, rln));

			double lxprod2 = (h_l.x * hrd_l.z) - (h_l.z * hrd_l.x);
			double norm_lxp2 = Math.abs (lxprod1 / (Math.sqrt (sqr(h_l.x) + sqr(h_l.z))
					+ Math.sqrt (sqr(hrd_l.x) + sqr(hrd_l.z))));
			double rxprod2 = (h_r.x * hrd_r.z) - (h_r.z * hrd_r.x);
			// note: below, lisp code has lxprod1 instead!!
			double norm_rxp2 = Math.abs (rxprod1 / (Math.sqrt (sqr(h_r.x) + sqr(h_r.z))
					+ Math.sqrt (sqr(hrd_r.x) + sqr(hrd_r.z))));

			if (sign(lxprod1) != sign(lxprod2))
			{
				turn = 1;
				go_on = false;
			}
			if (sign(rxprod1) != sign(rxprod2))
			{
				turn = 2;
				go_on = false;
			}

			lxprod1 = lxprod2;
			norm_lxp1 = norm_lxp2;
			rxprod1 = rxprod2;
			norm_rxp1 = norm_rxp2;

			if (j >= (fracNearestRP + aheadMax))
			{
				turn = 0;
				go_on = false;
			}
			if (j <= (fracNearestRP + aheadMin))
			{
				j = (long) (fracNearestRP + aheadMin);
			}

			if (lane != 0)
			{
				if (turn == 1) // left
				{
					double fi = ((norm_lxp1 * (j-1)) + (norm_lxp2 * (j-2))) / (norm_lxp1 + norm_lxp2);
					fpText = "ltp";
					fpTPfracIndex = fi;
					return left (fi, lane);
				}
				else if (turn == 2) // right
				{
					double fi = ((norm_rxp1 * (j-1)) + (norm_rxp2 * (j-2))) / (norm_rxp1 + norm_rxp2);
					fpText = "rtp";
					fpTPfracIndex = fi;
					return right (fi, lane);
				}
				else
				{
					double fi = fracNearestRP + aheadMax;
					fpText = "vp";
					fpTPfracIndex = 0;
					return middle (fi, lane);
				}
			}
			else
			{
				// not implemented -- only for lane changes
			}
		}
		return null;
	}

	float distAhead = 400;

	void draw (Graphics g, Env env)
	{
		long ri = env.simcar.roadIndex;

		g.setColor (Color.darkGray);
		Polygon p = new Polygon ();
		Coordinate newLoc = env.world2image (location (ri+3, 1));
		if (newLoc==null) return;
		p.addPoint (newLoc.x, newLoc.y);
		newLoc = env.world2image (location (ri+distAhead, 1));
		p.addPoint (newLoc.x, newLoc.y);
		newLoc = env.world2image (location (ri+distAhead, 4));
		p.addPoint (newLoc.x, newLoc.y);
		newLoc = env.world2image (location (ri+3, 4));
		p.addPoint (newLoc.x, newLoc.y);
		g.fillPolygon (p);

		long di = 3;
		int[] lps = {1,2,3,4};
		Coordinate[] oldLocs = {null, null, null, null};
		while (di <= distAhead)
		{
			g.setColor (Color.white);
			for (int i=0 ; i<4 ; i++)
			{
				double lp = lps[i];
				Coordinate oldLoc = oldLocs[i];
				newLoc = env.world2image (location (ri+di, lp));
				if (oldLoc!=null && newLoc!=null
						&& (lp==1 || lp==4 || ((ri+di) % 5 < 2))
				)
					g.drawLine (oldLoc.x, oldLoc.y, newLoc.x, newLoc.y);
				oldLocs[i] = newLoc;
			}

			if (di < 50) di += 1;
			else if (di < 100) di += 3;
			else di += 25;
		}
	}
}
