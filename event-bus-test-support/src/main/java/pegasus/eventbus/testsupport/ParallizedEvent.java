package pegasus.eventbus.testsupport;

import java.util.Date;

public class ParallizedEvent{

	protected int id;
	protected long threadId;
	protected int runTime;
	protected Date startTime;
	protected Date endTime;
    
	public ParallizedEvent(){}
	
	public ParallizedEvent(int id, int runTime) {
		this.id = id;
		this.runTime = runTime;
	}

   	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	
   	public long getThreadId(){
		return threadId;
	}
	public void setThreadId(long id){
		this.threadId = id;
	}
	

	public int getRunTime(){
		return runTime;
	}
	public void setRunTime(int runTime){
		this.runTime = runTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date time) {
		this.startTime = time;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date time) {
		this.endTime = time;
	}
}