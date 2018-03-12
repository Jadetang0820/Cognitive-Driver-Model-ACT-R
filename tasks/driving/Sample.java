package actr.tasks.driving;

/**
 * The class that defines a collected data sample at a given point in time.
 *  
 * 
 */
public class Sample
{
    double time;
    Position simcarPos, simcarHeading;
    double simcarFracIndex, simcarSpeed;
    long simcarRoadIndex;
    Position nearPoint, farPoint, carPoint;
    double steerAngle, accelerator, brake;
  //* Modified by Yue at 2018_2_6 (point13) add 2 positions of the 2nd autocar: autocar2Pos, autocar2Heading
    Position autocarPos,autocar2Pos, autocarHeading, autocar2Heading;
  //*(point13)
    double autocarFracIndex, autocarSpeed;
    boolean autocarBraking;
//    LocationChunk eyeLocation;
//    LocationChunk handLocation;
    boolean handMoving;
    boolean listening;
    boolean inDriveGoal;
    
    int event;
    double lanepos;
    
    public String toString ()
    {
    	//return simcarPos.x+"\t"+time+"\t"+simcarPos.z+"\t"+simcarHeading.x+"\t"+simcarHeading.z+"\t"+simcarSpeed+"\t"+steerAngle+"\t"+accelerator+"\t"+brake+"\t"+autocarPos.x+"\t"+autocarPos.z+"\t"+autocarHeading.x+
    	//"\t"+autocarHeading.z+"\t"+autocarSpeed+"\t"+nearPoint.x+"\n";
    	//modified by YUE at Aug 2017
    	
    	/*return time+"\t"+simcarPos.x+"\t"+simcarPos.z+"\t"+simcarHeading.x+"\t"+simcarHeading.z+"\t"+simcarSpeed+"\t"+steerAngle+"\t"+accelerator+"\t"+brake+"\t"+autocarPos.x+"\t"+autocarPos.z+"\t"+autocarHeading.x+
    	    	"\t"+autocarHeading.z+"\t"+autocarSpeed+"\t"+nearPoint.x+"\t"+nearPoint.z+"\t"+farPoint.x+"\t"+farPoint.z+"\t"+carPoint.x+"\t"+carPoint.z+"\n";
    	    	*/
    	return time+"\t"+simcarPos.x+"\t"+simcarPos.z+"\t"+simcarHeading.x+"\t"+simcarHeading.z+"\t"+simcarSpeed+"\t"+steerAngle+"\t"+accelerator+"\t"+brake+"\t"+autocarPos.x+"\t"+autocarPos.z+"\t"+autocarHeading.x+
    	    	"\t"+autocarHeading.z+"\t"+autocarSpeed+"\t"+nearPoint.x+"\t"+nearPoint.z+"\t"+farPoint.x+"\t"+farPoint.z+"\t"+carPoint.x+"\t"+carPoint.z+"\t"+autocar2Pos.x+"\t"+autocar2Pos.z+"\t"+autocar2Heading.x+
    	    	"\t"+autocar2Heading.z+"\n";
    }
}
