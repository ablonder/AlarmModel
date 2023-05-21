package AlarmModel;
import sim.engine.*;

public class Ecosystem implements Steppable{
	
	public boolean pred = false;
	public boolean alarm = false;
	
	/*
	 * determines whether a predator or alarm is present this step
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState state) {
		// cast the state as an AlarmModel to access its fields
		AlarmModel model = (AlarmModel) state;
		if(state.random.nextDouble() < model.predfreq) {
			this.pred = true;
			if(model.random.nextDouble() < model.accalarm){
				this.alarm = true;
			} else this.alarm = false;
		} else {
			this.pred = false;
			if(model.random.nextDouble() < model.falarm) this.alarm = true;
			else this.alarm = false;
		}
	}

	}
