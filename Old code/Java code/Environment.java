/*
 * Just some sweep infrastructure
 */

package AlarmModel;

import sweep.SimStateSweep;

public class Environment extends AlarmModel {

	public Environment(long seed, Class observer) {
		super(seed, observer);
	}
	
	public void start() {
		super.start();
		makeSpace(gridWidth, gridHeight);
		if(observer != null) {
			observer.initialize(space, spaces);
		}
	}

}
