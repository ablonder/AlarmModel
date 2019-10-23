/*
 * @author - Aviva Blonder
 * 
 * TODO - figure out how to do JavaDocs
 */

package AlarmModel;
import java.util.ArrayList;
import java.util.Arrays;

import sim.engine.*;

public class Agent extends Animal {
	
	// the agent's genotype
	public char type;
	// an integer representing the agent's type (0 = 'v', 1 = 'i', 2 = 's')
	public int t;
	// the agent's learning algorithm
	public char learnalg;
	// number of times the agent has foraged
	int foragecount;
	// the agent's natural lifespan
	double lifespan;
	// how long the agent has lived so far
	int age;
	// number of offspring the agent has had
	int offspring;
	// indicator of whether the agent heard an alarm on the previous timestep for learning alarms as episodes
	boolean prevalarm;
	// indicator of whether the current alarm has been reinforced yet for learning alarms as episodes
	boolean prevreinforce;
	// learning rate for neural network and Rescorla-Wagner
	double lrate;
	// neural network parameters
	int input;
	Double[] weights;
	Double[] biases;
	// counts for Bayesian learning of number of alarms and number of steps without alarm
	int alarmcount;
	int noalarmcount;
	// and the number of times there is a predator with and without an alarm
	int alarmpred;
	int noalarmpred;
	// stored probabilities for Rescorla-Wagner
	double alarmv;
	double noalarmv;
	// learned responses to alarm and absence of alarm
    public double[] alarmout = new double[] {0.5, 0.5};
	public double[] noalarmout = new double[] {0.5, 0.5};
	// direction this agent last fled in
	public Integer[] fleedir = new Integer[] {0, 0};
	
	/*
	 * Initialize agent with genotype, learning algorithm, and no foraging
	 */
	public Agent(AlarmModel model, char t, char l, double lrate, int x, int y) {
		// sets the agent's initial coordinates and initializes as not fleeing using the superclass
		super(model, false, x, y);
		// sets genotype
		this.type = t;
		// sets genotype number
		if(this.type == 'v') this.t = 0;
		else if(this.type == 'i') this.t = 1;
		else this.t = 2;
		// add this individual to the total count by genotype
		model.totalcount[this.t]++;
		// sets learning algorithm
		this.learnalg = l;
		// sets the learning rate
		this.lrate = lrate;
		// initialize amount foraged to 0
		this.foragecount = 0;
		// if alarms are being learned as episodes, initialize past alarm and reinforcement to false
		if(model.alarmep) {
			this.prevalarm = false;
			this.prevreinforce = false;
		}
		// initialize lifespan from a normal distribution
		this.lifespan = model.random.nextGaussian()*model.lifespanrange + model.minlifespan;
		// initialize age to 0
		this.age = 0;
		// initialize neural network
		if(this.learnalg == 'n') {
			this.weights = new Double[] {model.random.nextGaussian()/10, model.random.nextGaussian()/10};
			this.biases = new Double[] {model.random.nextGaussian()/10, model.random.nextGaussian()/10};
		} if(this.learnalg == 'r') {
			// initialize Rescorla-Wagner
			this.alarmv = .5;
			this.noalarmv = .5;
		}
	}
	
	/*
	 * Agent's behavior on a given step
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState state) {
		// cast the state into the AlarmModel class
		AlarmModel model = (AlarmModel) state;
		// subtract this agent from the total number of fleeing agents
		model.fleecount--;
		// increment age for this individual and in the total
		this.age++;
		
		// if this agent has outlived its lifespan, it dies
		if(this.age > this.lifespan) {
			this.die(model);
		} else {
			// otherwise, continue
			// set fleeing to false for now
			this.signal = false;
			// initialize the presence of an alarm to false
			boolean alarm = false;
			// and the location to here
			Integer[] fleedist = new Integer[] {0, 0};
			// if the agent isn't vigilance only, determine whether it detects an alarm
			if(this.type != 'v') {
				Integer[][] res = detect(model, model.aalarmrange, model.alarmdet, Heterospecific.class, true, false);
				// store whether it has
				if(res[0][0] == 1) {
					alarm = true;
					this.prevalarm = true;
				}
				// and store the distance between the agent and the alarm
				fleedist = res[1];
			}
			// if this indivdiual is a social learner determine if it detected a conspecific fleeing
			// this is before it actually decides whether to flee so it won't count itself
			boolean con = false;
			if(this.type == 's' && (!model.alarmep || !this.prevalarm || !alarm || !this.prevreinforce)) {
				Integer[][] res = detect(model, model.apredrange, model.condet, Agent.class, true, model.align);
				// if the agent actually detected a fleeing conspecific
				if(res[0][0] == 1) {
					// set fleeing conspecific to true
					con = true;
					// if there is no alarm, store the distance to the conspecific/direction it's fleeing in instead
					if(!alarm) fleedist = res[1];
				}
			}	
			// agent decides what to do and returns an array of probabilities
			double[] probs = act(alarm);
			// store probability of successfully foraging
			double pforage;
			// actually decide what to do based on the probabilities and base rate of vigilance
			if(state.random.nextDouble() < probs[1] || state.random.nextDouble() < model.basevig) {
				// store that this agent is fleeing
				this.signal = true;
				// make the probability of foraging the probability while fleeing
				pforage = model.fleeforage;
			} else {
				// otherwise, reset their flee direction to staying still
				this.fleedir = new Integer[] {0, 0};
				// make the probability of foraging the probability while not fleeing
				pforage = model.stayforage;
			}
			// replace this agent's previous learned response with the latest response in the total by type
			if(alarm) {
				// first, remove its previous response
				if(this.type == 'i') model.alarmresp[1] -= this.alarmout[1];
				else if(this.type == 's') model.alarmresp[2] -= this.alarmout[1];
				// then add this one
				if(this.type == 'i') model.alarmresp[1] += probs[1];
				else if(this.type == 's') model.alarmresp[2] += probs[1];
			} else {
				// again, first remove previous
				if(this.type == 'i') model.noalarmresp[1] -= this.noalarmout[0];
				else if(this.type == 's') model.noalarmresp[2] -= this.noalarmout[0];
				// then add this
				if(this.type == 'i') model.noalarmresp[1] += probs[0];
				else if(this.type == 's') model.noalarmresp[2] += probs[0];
			}
			// in the vigilance condition just update both based on response to no alarm
			if(this.type == 'v') {
				model.alarmresp[0] += probs[1] - this.noalarmout[1];
				model.noalarmresp[0] += probs[0] - this.noalarmout[0];
			}
			
			// store their latest response based on whether an alarm was present
			if(alarm) this.alarmout = probs.clone();
			else this.noalarmout = probs.clone();
			// determine if this agent successfully foraged and see if it reproduces
			if(state.random.nextDouble() < pforage) {
				// increment the number of times this agent has foraged in their count and total
				this.foragecount++;
				// if the forage count is high enough, reproduce
				if(this.foragecount > model.fthresh) {
					// first, return forage count to 0
					this.foragecount = 0;
					// if there's space in the population, actually create a new agent
					if(model.popsize < model.popthresh) {
						// add to the reproduction rate total and for this type
						model.actreprodrate++;
						this.offspring++;
						// determine the offspring's type with some chance of mutation
						char newtype = this.type;
						// if there's a mutation, choose randomly among the three options
						if(model.mutrate > model.random.nextDouble()) {
							// if this is being run without any vigilance only agents, just switch
							if(!model.vigilance) {
								if(this.type == 'i') newtype = 's';
								else if(this.type == 's') newtype = 'i';
							} else {
								// otherwise, create a list of possible genotypes
								ArrayList<String> types = new ArrayList<String>(Arrays.asList("v", "i", "s"));
								// remove this individual's genotype
								types.remove(Character.toString(this.type));
								// choose a random one of the remaining types for the new agent
								double flip = model.random.nextDouble();
								if(flip < .5) newtype = types.get(0).charAt(0);
								else newtype = types.get(1).charAt(0);
							}
						}
						// if there's local reproduction, put the new agent in its parent's square
						int x = this.x;
						int y = this.y;
						// otherwise, choose a random location
						if(!model.localbirth) {
							x = model.random.nextInt(model.grid.getWidth());
							y = model.random.nextInt(model.grid.getHeight());
						}
						model.newAgent(newtype, x, y);
					}
				}
			} else {
				// if this individual didn't forage, add to forage cost based on type
				model.foragecost[this.t]++;
			}
			// if this alarm hasn't been reinforced yet, check to see if it should be
			if(!model.alarmep || !this.prevalarm || !this.prevreinforce || !alarm) {
				// determine if this individual detected a predator
				boolean pred = false;
				Integer[][] res = detect(model, model.apredrange, model.preddet, Predator.class, false, false);
				// if the agent actually detected a predator
				if(res[0][0] == 1) {
					// change pred to true
					pred = true;
					// store the distance to the predator
					fleedist = res[1];
				}
				// if there was an alarm, but no predator, increment the true false alarm rate
				// this will be a little weird for the episodic condition, but that's okay
				if(alarm && !pred) model.actfalarmrate++;
				// if this just needs to be reinforced, do so
				if(model.alarmep && !this.prevreinforce) this.prevreinforce = pred || con;
				// if this is the end of an alarm, actually do the learning
				if(!model.alarmep || !alarm) {
					boolean reinforce;
					// if alarms are learned episodically, reinforce based on the whole time
					if(model.alarmep) {
						reinforce = this.prevreinforce;
						// reset previous reinforcement to false
						this.prevreinforce = false;
						// update alarm
						alarm = this.prevalarm;
						this.prevalarm = false;
					} else { reinforce = pred || con; }
					// otherwise use the presence of a predator and fleeing conspecifics to determine reinforcement
					// learn!
					learn(probs, reinforce, alarm);
				}
			}
			// if this individual is fleeing, add them to the count and actually move them
			if(this.signal) {
				model.fleecount++;
				this.fleedir = move(model, fleedist);
			}
		}
	}
	
	/*
	 * How the agent decides what to do based on its learning algorithm and the presence
	 * or absence of an alarm
	 */
	public double[] act(boolean alarm) {
		// neural network decision process
		if(this.learnalg == 'n') {
			// convert presence of an alarm into an input of 1 and absence into -1
			if(alarm) this.input = 1;
			else this.input = -1;
			// calculate the two outputs, exponentiate and add them to the array
			double[] output = new double[2];
			output[0] = Math.exp(this.input*this.weights[0] + this.biases[0]);
			output[1] = Math.exp(this.input*this.weights[1] + this.biases[1]);
			// calculate the softmax
			double sum = output[0]+output[1];
			output[0] /= sum;
			output[1] /= sum;
			// return the results
			return output;
		} else if(this.learnalg == 'b') {
			// Bayesian decision process
			// get the relevant counts based on whether or not there is an alarm
			double priorcount;
			double condcount;
			if(alarm) {
				priorcount = this.alarmcount;
				condcount = this.alarmpred;
			} else {
				priorcount = this.noalarmcount;
				condcount = this.noalarmpred;
			}
			// if the prior count is 0, just return a 50% chance of each
			if(priorcount == 0) {
				return(new double[] {0.5, 0.5});
			} else {
				// otherwise, calculate the probability and return
				return(new double[] {1-(condcount/priorcount), condcount/priorcount});
			}
		} else if(this.learnalg == 'r') {
			// Rescorla-Wagner decision process
			// just return the learned probabilities based on whether or not there's an alarm
			if(alarm) return(new double[] {1-this.alarmv, this.alarmv});
			else return(new double[] {1-this.noalarmv, this.noalarmv});
		}
		// for now there's just going to be an empty return otherwise, to make it less grumpy
		return(new double[2]);
	}
	
	/*
	 * Learn from the actual conditions
	 */
	public void learn(double[] output, boolean reinforce, boolean alarm) {
		// neural network learning process
		if(this.learnalg == 'n') {
			// convert reinforcement into an array of ideal responses
			Double[] ideal;
			if(reinforce) ideal = new Double[] {0.0, 1.0};
			else ideal = new Double[] {1.0, 0.0};
			// adjust each set of weights and biases iteratively
			for(int i = 0; i < 2; i++) {
				// calculate error
				double error = ideal[i]-output[i];
				// use error to adjust the weights
				this.weights[i] += this.lrate*error*this.input;
				// and adjust the biases
				this.biases[i] += this.lrate*error;
			}
		} else if(this.learnalg == 'b') {
			// Bayesian learning process - just add to the relevant counts
			if(alarm) {
				this.alarmcount++;
				if(reinforce) this.alarmpred++;
			} else {
				this.noalarmcount++;
				if(reinforce) this.noalarmpred++;
			}
		} else if(this.learnalg == 'r') {
			// Rescorla-Wagner learning process - update probabilities
			if(alarm) {
				if(reinforce) this.alarmv = Math.min(this.alarmv + this.lrate*(1-this.alarmv), 1);
				else this.alarmv = Math.max(this.alarmv - this.lrate*this.alarmv, 0);
			} else {
				if(reinforce) this.noalarmv = Math.min(this.noalarmv + this.lrate*(1-this.noalarmv), 1);
				else this.noalarmv = Math.max(this.noalarmv - this.lrate*this.noalarmv, 0);
			}
		}
	}
	
	/*
	 * Remove the agent from the population
	 */
	public void die(AlarmModel model) {
		// remove this agent from the schedule
		this.stopbutton.stop();
		// and the grid
		model.grid.remove(this);
		// decrement population size
		model.popsize--;
		// remove this agent from the count by type
		if(this.type == 'v') {
			model.vigcount--;
		} else if(this.type == 'i') {
			model.indcount--;
		} else {
			model.soccount--;
		}
		// add its total lifetime reproduction and age to the overall count
		model.offspring[this.t] += this.offspring;
		model.lifespan[this.t] += this.age;
		model.deadcount[this.t]++;
	}

}