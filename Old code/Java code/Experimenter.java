/*
 * Sweep observer
 */

package AlarmModel;

import observer.Observer;
import sim.engine.SimState;
import sweep.ParameterSweeper;
import sweep.SimStateSweep;
import sim.util.Bag;

public class Experimenter extends Observer {
	
	// this class is supposed to store some data, I think
	int popsize = 0;
	int vigcount = 0;
	int indcount = 0;
	int soccount = 0;

	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
	}
	
	public void step(SimState state) {
		super.step(state);
		stop((Environment)state); //If there are no agents left, the observer stops
        nextInterval(); //send the data to data storage
	}
	
	public boolean nextInterval() {
		data.add(popsize);
		data.add(vigcount);
		data.add(indcount);
		data.add(soccount);
		return(false);
	}
	
	public void reSet(SimStateSweep state) {
		super.reSet(state);
		popsize = 0;
		vigcount = 0;
		indcount = 0;
		soccount = 0;
	}
	
	public void getData(AlarmModel model) {
		getData(model.popsize, model.vigcount, model.indcount, model.soccount);
	}
	
	public void getData(double pop, double vig, double ind, double soc) {
		popsize += pop;
		vigcount += vig;
		indcount += ind;
		soccount += soc;
	}
	
	public void stop(Environment state) {
        Bag agents = state.sparseSpace.getAllObjects(); //directly access sparseSpace
        if(agents == null || agents.numObjs == 0) {
            event.stop();
        }
    }
	
}
