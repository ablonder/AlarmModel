/*
 * A common superclass for all agents, mostly to enable them to detect each other
 */

package AlarmModel;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.Grid2D;
import sim.util.Bag;

public abstract class Animal implements Steppable{
	
	// stores location
	public int x;
	public int y;
	// basically whether or not the animal is visible
	public boolean signal;
	// object returned by schedule to remove it from the schedule
	Stoppable stopbutton;
	
	/*
	 * Just initializes location and signal
	 */
	public Animal(AlarmModel model, boolean s, int x, int y) {
		this.x = x;
		this.y = y;
		this.signal = s;
	}
	
	/*
	 * Enables animals to detect each other based on class and returns the detected location
	 */
	public Integer[][] detect(AlarmModel model, int detrange, double detprob, Class c, boolean sig, boolean dir) {
		// initialize the integer array to be returned
		Integer[][] det = new Integer[2][];
		det[0] = new Integer[] {0};
		det[1] = new Integer[] {0, 0};
		// grab all of this animal's neighbors
		Bag neighbors = model.grid.getMooreNeighbors(this.x, this.y, detrange, Grid2D.BOUNDED, false);
		// determine if any of them is in the right class
		if(!neighbors.isEmpty()) {
			for(int n = 0; n < neighbors.size(); n++) {
				// if one of its neighbors is in the right class
				if(neighbors.get(n).getClass() == c) {
					// store that neighbor casted to an animal
					Animal a = (Animal) neighbors.get(n);
					// determine if it's giving an signal (if relevant) and if this animal detects it
					if((a.signal || !sig) && detprob > model.random.nextDouble()) {
						// if this animal has detected a signal, change the first result to 1
						det[0] = new Integer[] {1};
						// if the animal hasn't already discovered a closer neighbor
						if(Math.sqrt(Math.pow(this.x-a.x, 2)+Math.pow(this.y-a.y, 2))
								< Math.sqrt(Math.pow(det[1][0], 2)+Math.pow(det[1][1], 2))) {
							// if it's trying to go in the same direction as the other animal, return the direction it last moved
							if(dir && c == Agent.class) {
								// first cast it as an agent, because this should only apply to agents
								Agent agent = (Agent) a;
								// then grab the direction that agent is moving instead of the distance
								det[1] = agent.fleedir;
							} else {
								// otherwise store the distance between this animal and the neighbor (assumes trying to move away)
								det[1] = new Integer[] {this.x-a.x, this.y-a.y};
							}
						}
					}
				}
			}
		}
		// return the results
		return det;
	}
	
	
	/*
	 * Moves animals on the grid randomly or based on their distance to/from something else
	 */
	public Integer[] move(AlarmModel model, Integer[] movedir) {
		// choose a random direction if the distance is 0
		if(movedir[0] == 0 && movedir[1] == 0) {
			// I'm going to store the direction to go in movedir as -1, 0 or 1 to add to x and y
			// TODO - maybe also give them the option to stay still
			Integer[][] opts = new Integer[][] {{1,1},{1,0},{0,1},{-1,-1},{-1,0},{0,-1},{1,-1},{-1,1}};
			int choice = model.random.nextInt(opts.length);
			movedir = opts[choice];
		} else {
			// otherwise move toward/away from whatever it's spotted based on the sign of the distance
			movedir[0] = Integer.signum(movedir[0]);
			movedir[1] = Integer.signum(movedir[0]);
		}
		// then just add the first number in movedir to the animal's x and the second to its y
		this.x += movedir[0];
		this.y += movedir[1];
		// if it's at the edge, bounce
		if(this.x < 0) this.x = 1;
		if(this.x > model.grid.getWidth()) this.x = model.grid.getWidth()-1;
		if(this.y < 0) this.y = 1;
		if(this.y > model.grid.getHeight()) this.y = model.grid.getHeight()-1;
		// actually change its location on the grid
		model.grid.setObjectLocation(this, this.x, this.y);
		// return the direction the agent moved in
		return movedir;
	}
	
	/*
	 * Maintains the population in proportion with the number of agents
	 * Returns whether or not this agent is still alive at the end
	 */
	public boolean maintainpop(AlarmModel model, double count, double prop, char type) {
		// determine whether there are too many animals of this type
		// TODO - for now I'm not going to let either go extinct, but that may change
		if(count > 1 && count-1 >= prop*model.popsize) {
			// if there are, remove this one from the population
			// remove this agent from the schedule
			this.stopbutton.stop();
			// and the grid
			model.grid.remove(this);
			// and return false to indicate that this individual is dead
			return(false);
		} else if(count/model.popsize < prop) {
			// otherwise, if there aren't enough animals of this type, reproduce
			Animal offspring;
			if(type == 'h') {
				offspring = new Heterospecific(model);
				model.hetcount++;
			} else if(type == 'p') {
				offspring = new Predator(model);
				model.predcount++;
			} else {
				// this should never happen, but this will just return true and continue the simulation
				return(true);
			}
			// now add the new animal to the simulation
			offspring.stopbutton = model.schedule.scheduleRepeating(offspring);
			model.grid.setObjectLocation(offspring, offspring.x, offspring.y);			
		}
		// either way, this individual survived and this can return true
		return(true);
	}

}
