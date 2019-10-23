/*
 * @author Aviva Blonder
 * 
 * TODO - figure out how to do JavaDocs
 */

package AlarmModel;
import java.util.Arrays;

import sim.field.grid.SparseGrid2D;

public class AlarmModel extends Model {
	
	// initialize grid
	public SparseGrid2D grid;
		
	// probability of a predator being present
	public double predfreq;
	// frequency with which each predator can consume prey - number of steps between hunting
	public int huntsteps;
	// probability of a heterospecific being present
	public double hetfreq;
	// probability of an alarm occurring when a predator is present
    public double accalarm;
    // probability of an alarm occurring when a predator is not present
    public double falarm;
    // probability of detecting a predator
    public double preddet;
    // probability of hearing an alarm
    public double alarmdet;
    // probability of seeing a conspecific
    public double condet;
    // agent range of vision to detect predators
    public int apredrange;
    // agent range of hearing to detect alarms
    public int aalarmrange;
    // agent range of vision to detect fleeing conspecifics
    public int aconrange;
    // heterospecific range of vision to detect predators
    public int hetrange;
    // predator range of vision to detect agents
    public int predrange;
    // whether agents flee in the same direction or away from fleeing conspecifics
    public boolean align;
    // initial and maximum number of agents
    public int popthresh;
    // baseline rate at which all agents look up regardless of learning
    public double basevig;
    // whether to include vigilance only agents
    public boolean vigilance;
    // initial proportion of agents that just rely on their own vigilance
    public double vigprop;
    // initial proportion of agents that just use individual learning
    public double indvprop;
    // mutation rate
    public double mutrate;
    // amount of foraging required to survive and reproduce
    public int fthresh;
    // probability of successfully foraging while fleeing
    public double fleeforage;
    // probability of successfully foraging while not fleeing
    public double stayforage;
    // probability of a predator successfully catching an agent while it is fleeing
    public double predflee;
    // indicates whether offspring are born adjacent to their parents
    public boolean localbirth;
    // minimum lifespan of agents in the population
    public int minlifespan;
    // range of lifespans of agents in the population
    public int lifespanrange;
    // learning algorithm used for this simulation
    public char learnalg;
    // learning rate
    public double lrate;
    // indicates whether agents learn alarms as episodes
    public boolean alarmep;
    
    // current population size
    public int popsize;
    // current number of heterospecifics
    public int hetcount;
    // current number of predators
    public int predcount;
    // number of individuals fleeing at a given time
    public int fleecount;
    // number of agents of each genotype
    public double vigcount;
    public double indcount;
    public double soccount;
    // total response for each genotype in each condition
    public double[] alarmresp;
    public double[] noalarmresp;
    // total number of kills, births, and perceived false alarms
    public int actpredrate;
    public int actreprodrate;
    public int actfalarmrate;
    // total number of kills for each genotype
    public double[] predcost;
    // number of missed foraging opportunities for each genotype
    public double[] foragecost;
    // number of offspring had by each genotype
    public double[] offspring;
    // total lifespan by genotype
    public double[] lifespan;
    // total number of individuals of each genotype over the whole simulation for averages
    public double[] totalcount;
    // total number of dead individuals of each genotype over the whole simulation
    public double[] deadcount;
	
    /*
     * Really just calls the superclass constructor to run everything
     */
    public AlarmModel(String[] args) {
		super(args);
	}
    
    /*
     * Calls the corresponding superclass constructor
     */
    public AlarmModel() {
    	super();
    }
    
    /*
     * Calls the corresponding superclass constructor
     */
    public AlarmModel(String fname) {
    	super(fname);
    }
    
    /*
     * Calls superclass constructor to split files
     */
    public AlarmModel(String fname, String[] splitparams, String[] snames) {
    	super(fname, splitparams, snames);
    }
    
    /*
     * sets parameter and result names for superclass
     */
    public void setNames() {
    	autoparams = true;
    	autores = true;
    	paramnames = new String[] {"Dims", "foragediff"};
    	resnames = new String[] {"VigAlarm", "IndAlarm", "SocAlarm", "VigNoAlarm",
    			"IndNoAlarm", "SocNoAlarm", "VigPredCost", "IndPredCost", "SocPredCost",
    			"VigForageCost", "IndForageCost", "SocForageCost", "VigOffspring",
    			"IndOffspring", "SocOffspring", "VigLifespan", "IndLifespan", "SocLifespan",
    			"PredRate", "ReprodRate", "FAlarmRate"};
    }
    
    /*
     * stores subclass name for access by Model
     * @see AlarmModel.Model#setSubclass()
     */
    public void setSubclass() {
    	subclass = AlarmModel.class;
    }
    
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
		// initialize learning arrays
		this.alarmresp = new double[3];
		this.noalarmresp = new double[3];
		// initialize predation, reproduction, and false alarm rate
		this.actpredrate = 0;
		this.actfalarmrate = 0;
		this.actreprodrate = 0;
		// initialize predation and forage cost, number of offspring, lifespan, and total count by type
		this.predcost = new double[3];
		this.foragecost = new double[3];
		this.offspring = new double[3];
		this.lifespan = new double[3];
	    this.totalcount = new double[3];
	    this.deadcount = new double[3];
		// if this is a no vigilance run, make all the vigilance only agents individual learners
		if(!vigilance) {
			this.indvprop += this.vigprop;
			this.vigprop = 0;
		}
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
	 * Turns user inputed arguments into parameters
	 */
	public void setParams(String[] args) {
		super.setParams(args);
		try {
			int dims = Integer.parseInt(args[Arrays.asList(paramnames).indexOf("Dims")]);
			this.grid = new SparseGrid2D(dims, dims);
			double fdiff = Double.parseDouble(args[Arrays.asList(paramnames).indexOf("foragediff")]);
			this.fleeforage = this.stayforage - fdiff; 
		} catch(NumberFormatException e) {
			System.out.println("Error in AlarmModel.setParams! Make sure all the parameters are the correct type!");
			System.exit(0);
		} catch(IndexOutOfBoundsException e) {
			System.out.println("Missing 'dims' or 'foragediff' parameter!");
			System.exit(0);
		}
	}
	
	/*
	 * updates the results array when needed 
	 */
	public double[] updateRes() {
		double[] results = super.updateRes();
		for(int i = 0; i < 3; i++) {
			results[i] = this.alarmresp[i]/this.totalcount[i];
			results[3+i] = this.noalarmresp[i]/this.totalcount[i];
			results[6+i] = this.predcost[i]/this.totalcount[i];
			results[9+i] = this.foragecost[i]/this.totalcount[i];
			results[12+i] = this.offspring[i]/this.deadcount[i];
			results[15+i] = this.lifespan[i]/this.deadcount[i];
		}
		results[18] = (double) (this.actpredrate)/this.schedule.getSteps();
		results[19] = (double) (this.actreprodrate)/this.schedule.getSteps();
		results[20] = (double) (this.actfalarmrate)/this.schedule.getSteps();
		return results;
	}
	
	
	public static void main(String[] args) {
		AlarmModel model = new AlarmModel("testInput.txt");
		// AlarmModel model = new AlarmModel(args[0]);
	}
}