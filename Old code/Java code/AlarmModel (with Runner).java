/*
 * @author Aviva Blonder
 * 
 * TODO - figure out how to do JavaDocs
 */

package AlarmModel;
import java.util.ArrayList;
import java.util.Collection;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import sim.field.grid.SparseGrid2D;
import sweep.SimStateSweep;

public class AlarmModel extends SimState {
	
	// initialize grid for visualization
	public SparseGrid2D grid = new SparseGrid2D(100, 100);
	
	// probability of a predator being present
	public double predfreq = .001;
	// frequency with which each predator can consume prey - number of steps between hunting
	public int huntsteps = 1;
	// probability of a heterospecific being present
	public double hetfreq = .1;
	// probability of an alarm occurring when a predator is present
    public double accalarm = 1;
    // probability of an alarm occurring when a predator is not present
    public double falarm = 0;
    // probability of detecting a predator
    public double preddet = 1;
    // probability of hearing an alarm
    public double alarmdet = 1;
    // probability of seeing a conspecific
    public double condet = 1;
    // agent range of vision to detect predators
    public int apredrange = 1;
    // agent range of hearing to detect alarms
    public int aalarmrange = 4;
    // agent range of vision to detect fleeing conspecifics
    public int aconrange = 2;
    // heterospecific range of vision to detect predators
    public int hetrange = 4;
    // predator range of vision to detect agents
    public int predrange = 2;
    // whether agents flee in the same direction or away from fleeing conspecifics
    public boolean align = false;
    // initial and maximum number of agents
    public int popthresh = 1000;
    // whether to include vigilance only agents
    public boolean vigilance = true;
    // initial proportion of agents that just rely on their own vigilance
    public double vigprop = .334;
    // initial proportion of agents that just use individual learning
    public double indvprop = .333;
    // mutation rate
    public double mutrate = .001;
    // amount of foraging required to survive and reproduce
    public int fthresh = 25;
    // indicates whether offspring are born adjacent to their parents
    public boolean localbirth = false;
    // random seed
    public int seed = 0;
    // learning algorithm used for this simulation
    public char learnalg = 'n';
    // learning rate
    public double lrate = .01;
    
    // current population size
    public int popsize = 0;
    // current number of heterospecifics
    public int hetcount = 0;
    // current number of predators
    public int predcount = 0;
    // number of individuals fleeing at a given time, initialized to 0
    public int fleecount = 0;
    // number of agents of each genotype
    public int vigcount = 0;
    public int indcount = 0;
    public int soccount = 0;
    // total response for each genotype in each condition
    public double[] alarmresp = new double[3];
    public double[] noalarmresp = new double[3];
	
	
	/*
	 * constructor, just sets the seed really
	 */
	public AlarmModel(long seed){
		super(seed);
	}
	
//	/*
//	 * constructor that sets the seed and observer for sweep
//	 */
//	public AlarmModel(long seed, Class observer) {
//		super(seed, observer);
//	}
	
	/*
	 * sets up the model
	 * @see sim.engine.SimState#start()
	 */
	public void start() {
		super.start();
		// clear the grid
		this.grid.clear();
		// initialize popsize to 0
		this.popsize = 0;
		// initialize predator and conspecific counts to 0
		this.predcount = 0;
		this.hetcount = 0;
		// initialize counts to 0
		this.vigcount = 0;
		this.indcount = 0;
		this.soccount = 0;
		// create a bunch of agents
		for(int i = 0; i < popthresh; i++) {
			// determine agent's genotype
			char type;
			if(i < vigprop*popthresh) {
				// vigilance only
				type = 'v';
			} else if(i < (vigprop+indvprop)*popthresh) {
				// individual learning only
				type = 'i';
			} else {
				// social learning too
				type = 's';
			}
			newAgent(type, this.random.nextInt(this.grid.getWidth()), this.random.nextInt(this.grid.getHeight()));
		}
		// create a bunch of predators and add them to the schedule and the grid
		for(int i = 0; i < this.popsize*this.predfreq; i++) {
			Predator pred = new Predator(this);
			pred.stopbutton = schedule.scheduleRepeating(pred);
			grid.setObjectLocation(pred, pred.x, pred.y);
			this.predcount++;
		}
		// create a bunch of heterospecifics and add them to the schedule and the grid
		for(int i = 0; i < this.popsize*this.hetfreq; i++) {
			Heterospecific het = new Heterospecific(this);
			het.stopbutton = schedule.scheduleRepeating(het);
			grid.setObjectLocation(het, het.x, het.y);
			this.hetcount++;
		}
	}
	
	
	/*
	 * Creates a new agent of the designated type and schedules it
	 */
	public void newAgent(char type, int x, int y) {
		Agent agent = new Agent(this, type, this.learnalg, this.lrate, x, y);
		// schedule the agents and save the resulting stopabble so the agent can be stopped
		agent.stopbutton = schedule.scheduleRepeating(agent);
		// add the agent to the grid at its initial random location
		grid.setObjectLocation(agent, agent.x, agent.y);
		// increment popsize
		this.popsize++;
		// add the agent to the count by genotype and learning counts
		if(type == 'v') {
			this.vigcount++;
			this.alarmresp[0] += .5;
			this.noalarmresp[0] += .5;
		} else if(type == 'i') { 
			indcount++;
			this.alarmresp[1] += .5;
			this.noalarmresp[1] += .5;
		} else {
			soccount++;
			this.alarmresp[2] += .5;
			this.noalarmresp[2] += .5;
		}
	}
	
	/*
	 * Sets probability of a predator being present
	 */
	public void setPredFreq(double pfreq) { this.predfreq = pfreq; }
	
	/*
	 * Returns probability of a predator being present
	 */
	public Double getPredFreq() { return(this.predfreq); }
	
	/*
	 * Sets probability of an alarm occurring when a predator is present
	 */
	public void setAccAlarm(double aalarm) { this.accalarm = aalarm; }
	
	/*
	 * Returns probability of an alarm occurring when a predator is present
	 */
	public Double getAccAlarm() { return(this.accalarm); }
	
	/*
	 * Sets probability of an alarm occurring when a predator is not present
	 */
	public void setFAlarm(double falarm) { this.falarm = falarm; }
	
	/*
	 * Returns probability of an alarm occurring when a predator is not present
	 */
	public Double getFAlarm() { return(this.falarm); }
	
	/*
	 * Sets probability of detecting a predator
	 */
	public void setPredDet(double pdet) { this.preddet = pdet; }
	
	/*
	 * Returns probability of detecting a predator
	 */
	
	public Double getPredDet() { return(this.preddet); }
	
	/*
	 * Sets probability of detecting an alarm
	 */
	public void setAlarmDet(double adet) { this.alarmdet = adet; }
	
	/*
	 * Returns the probability of detecting an alarm
	 */
	public Double getAlarmDet() { return(this.alarmdet); }
	
	/*
	 * Sets the probability of detecting a conspecific
	 */
	public void setConDet(double cdet) { this.condet = cdet; }
	
	/*
	 * Returns the probability of detecting a conspecific
	 */
	public Double getConDet() { return(this.condet); }
	
	/*
	 * Sets the initial and maximum number of agents
	 */
	public void setPopThresh(int pthresh) { this.popthresh = pthresh; }
	
	/*
	 * Returns the initial and maximum number of agents
	 */
	public Integer getPopThresh() { return(this.popthresh); }
	
	/*
	 * Sets the initial proportion of vigilance only agents
	 */
	public void setVigProp(double vprop) { this.vigprop = vprop; }
	
	/*
	 * Returns the initial proportion of vigilance only agents
	 */
	public Double getVigProp() { return(this.vigprop); }
	
	/*
	 * Sets the initial proportion of individual learning only agents
	 */
	public void setIndProp(double iprop) { this.indvprop = iprop; }
	
	/*
	 * Returns the initial proportion of individual learning only agents
	 */
	public Double getIndProp() { return(this.indvprop); }
	
	/*
	 * Sets the mutation rate
	 */
	public void setMutRate(double mrate) { this.mutrate = mrate; }
	
	/*
	 * Returns the mutaiton rate
	 */
	public Double getMutRate() { return(this.mutrate); }
	
	/*
	 * Sets the foraging threshold
	 */
	public void setFThresh(int thresh) { this.fthresh = thresh; }
	
	/*
	 * Returns the foraging threshold
	 */
	public Integer getFThresh() { return(this.fthresh); }
	
	/*
	 * Sets the learning algorithm
	 */
	public void setLearnAlg(char lalg) { this.learnalg = lalg; }
	
	/*
	 * Returns the learning algorithm
	 */
	public Character getLearnAlg() { return(this.learnalg); } 
	
	/*
	 * Returns number of vigilance only agents
	 */
	public Integer getVigilanceCount() { return(this.vigcount); }
	
	/*
	 * Returns number of individual learning only agents
	 */
	public Integer getIndividualCount() { return(this.indcount); }
	
	/*
	 * Returns the number of social learning agents
	 */
	public Integer getSocialCount() { return(this.soccount); }
	
	/*
	 * Returns population size
	 */
	public Integer getPopSize() { return(this.popsize); }
	
	/*
	 * Returns number of individuals fleeing
	 */
	public Integer getFleeCount() { return(this.fleecount); }
	
	
	/*
	 * Turns user inputed arguments into parameters
	 */
	public void setParams(String[] args) {
		try {
			this.predfreq = Double.parseDouble(args[0]);
			this.huntsteps = Integer.parseInt(args[1]);
			this.hetfreq = Double.parseDouble(args[2]);
			this.accalarm = Double.parseDouble(args[3]);
			this.falarm = Double.parseDouble(args[4]);
			this.preddet = Double.parseDouble(args[5]);
			this.alarmdet = Double.parseDouble(args[6]);
			this.condet = Double.parseDouble(args[7]);
			this.apredrange = Integer.parseInt(args[8]);
			this.aalarmrange = Integer.parseInt(args[9]);
		    this.aconrange = Integer.parseInt(args[10]);
		    this.hetrange = Integer.parseInt(args[11]);
		    this.predrange = Integer.parseInt(args[12]);
		    this.align = Boolean.parseBoolean(args[13]);
			this.popthresh = Integer.parseInt(args[14]);
			this.vigilance = Boolean.parseBoolean(args[15]);
			this.vigprop = Double.parseDouble(args[16]);
			this.indvprop = Double.parseDouble(args[17]);
			this.mutrate = Double.parseDouble(args[18]);
			this.fthresh = Integer.parseInt(args[19]);
			this.localbirth = Boolean.parseBoolean(args[20]);
			this.learnalg = args[21].charAt(0);
			this.lrate = Double.parseDouble(args[22]);
		} catch(NumberFormatException e) {
			System.out.println("Error in AlarmModel: setParams! Make sure all the parameters are the correct type!");
			System.exit(0);
		}
	}
	
	
	
	public static void main(String[] args) {
		// for now I'm going to set the args manually
		// 0predfreq, 1accalarm, 2falarm, 3preddet, 4alarmdet, 5condet, 6popthresh, 7vigprop,
		// 8indvprop, 9mutrate, 10fthresh, 11predforage, 12predflee, 13learnalg, 14seed, 15steps
		args = new String[] {".01", "1", "0", "1", "1", "1", "1000", ".34", ".33", ".01",
				"100", ".01", "0", "n", "0", "1000"};
		// make sure there are enough arguments provided to run
		if(args.length < 16) {
			System.out.println("Insufficient arguments");
			System.exit(0);
		}
		// grab seed from args
		int seed = Integer.parseInt(args[14]);
		// create new model
		SimState state = new AlarmModel(seed);
		// cast the model as AlarmModel to access its fields
		AlarmModel model = (AlarmModel) state;
		// set model parameters from args
		model.setParams(args);
		// start the simulation
		state.start();
		// grab number of steps from args
		int steps = Integer.parseInt(args[15]);
		// run the simulation for that number of steps
		while(state.schedule.getSteps() < steps) {
			if (!state.schedule.step(state)) break;
		}
		state.finish();
		System.exit(0);
	}

}
