package actr.tasks.driving;

import java.awt.*;
import javax.swing.*;

/**
 * The visual simulator as a JPanel.
 *  
 * 
 */
public class Simulator extends JPanel
{
	Simulation sim = null;

	public Simulator ()
	{
		super ();
		setOpaque (true);
		setBackground (Color.black);
		setSize (Env.envWidth, Env.envHeight);
	}

	public void paintComponent (Graphics g)
	{
		//super.paintComponent(g);
		if (sim == null) return;
		sim.draw (g);
	}

	void useSimulation (Simulation simArg)
	{
		sim = simArg;
	}
}
