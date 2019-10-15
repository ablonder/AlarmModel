/*
 * @author - Aviva Blonder
 * 
 * Manages predator location and movement for the alarm model
 */

package AlarmModel;

import sim.engine.SimState;
import sim.util.Bag;

public class Predator extends Animal {
	
	// number of steps since this individual last ate
	public int preysteps;
	
	public Predator(AlarmModel model){
		// set initial random location using the superclass and set signal to true (predators are always visible)
		super(model, true, model.random.nextInt(model.grid.getWidth()), model.random.nextInt(model.grid.getHeight()));
		// set the number of steps since it last ate to zero
		this.preysteps = 0;
	}
	
	public void step(SimState state) {
		// cast the state as an alarm model
		AlarmModel model = (AlarmModel) state;
		// maintain the population size and determine if this predator survives
		if(maintainpop(model, model.predcount, model.predfreq, 'p')) {
			// increment the number of steps since eating
			this.preysteps++;
			// if this individual is ready to hunt again, move accordingly
			if(this.preysteps >= model.huntsteps) {
				// determine whether an agent is in range
				// TODO - possibly make it so predators don't always detect their prey
				Integer[][] prey = detect(model, model.predrange, 1, Agent.class, false, false);
				// change the sign so it goes toward its prey instead of away
				for(int i = 0; i < 2; i++) prey[1][i] *= -1;
				// move, either toward prey or randomly
				move(model, prey[1]);
				// if it lands on prey, eat it
				// first grab all things at the new location
				Bag currentloc = model.grid.getObjectsAtLocation(this.x, this.y);
				// if it's not empty, check to see if any of them is an agent
				if(!currentloc.isEmpty()) {
					for(int i = 0; i < currentloc.size(); i++) {
						if(currentloc.get(i).getClass() == Agent.class) {
							// if there's an agent there, cast it as such
							Agent a = (Agent) currentloc.get(i);
							// determine if it successfully caught it
							if(!a.signal || model.random.nextDouble() < model.predflee) {
								// increment successful predation rate
								model.actpredrate++;
								// add to the cost of predation by type
								model.predcost[a.t]++;
								// and kill the agent off
								a.die(model);
								// reset steps since last eating to 0
								this.preysteps = 0;
								// and then break because it can only kill one per turn
								break;
							}
						}
					}
				}
			} else {
				// otherwise, if this individual is still digesting, just move randomly
				move(model, new Integer[] {0, 0});
			}
		} else {
			// since I'm using a general function, I have to decrease the number of predators here
			model.predcount--;
		}
	}

}
