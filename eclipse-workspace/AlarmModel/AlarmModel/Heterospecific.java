/*
 * @author - Aviva Blonder
 * 
 * Manages heterospecific alarm calling for the alarm model
 */

package AlarmModel;

import sim.engine.SimState;

public class Heterospecific extends Animal {
	
	public Heterospecific(AlarmModel model) {
		// set initial random location and initialize signal (whether or not it's giving an alarm) to false
		super(model, false, model.random.nextInt(model.grid.getWidth()), model.random.nextInt(model.grid.getHeight()));
	}
	
	public void step(SimState state) {
		// cast the state as an alarm model
		AlarmModel model = (AlarmModel) state;
		// make sure the population is constant
		if(maintainpop(model, model.hetcount, model.hetfreq, 'h')) {
			// reset signal (alarm call) to false
			this.signal = false;
			// determine whether a predator is in range
			Integer[][] pred = detect(model, model.hetrange, 1, Predator.class, false, false);
			// if a predator is in range, determine whether it's been detected
			if(pred[0][0] == 1) {
				// and alarm call accordingly
				if(model.accalarm > model.random.nextDouble()) this.signal = true;
			} else {
				// otherwise, determine whether there's a false alarm
				if(model.falarm > model.random.nextDouble()) this.signal = true;
			}
		} else {
			// since I'm using a general function, I have to remove this from the count separately
			model.hetcount--;
		}
	}

}
