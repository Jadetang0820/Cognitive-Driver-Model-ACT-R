package actr.tasks.driving;

import actr.model.Model;

/**
 * The class that defines the driver's particular behavioral parameters.
 *  
 * 
 */
public class Driver
{
	Model model;
	String name;
	int age;
	float steeringFactor;
	float stabilityFactor;
	
	public Driver (Model model, String nameArg, int ageArg, float steeringArg, float stabilityArg)
	{
		name = nameArg;
		age = ageArg;
		steeringFactor = steeringArg;
		stabilityFactor = stabilityArg;
	}

	public String writeString ()
	{
		return new String ("\"" + name + "\"\t" + age + "\t" + steeringFactor + "\t" + stabilityFactor + "\n");
	}
	
	public String toString ()
	{
		return name;
	}
}
