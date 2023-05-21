/*
 * Wrapper class to run things so that I don't keep having to copy everything into main methods
 */

package AlarmModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import sim.display.Console;
import sim.engine.SimState;

public class Runner {
	
	// initialize empty array of parameters
	public String[] params;
	// initialize empty array of test parameters
	public ArrayList<Integer> testparams = new ArrayList<Integer>();
	// initialize empty nested array of test parameter values
	public ArrayList<ArrayList<String>> testvals = new ArrayList<ArrayList<String>>();
	// file writer for the end of run results
	public BufferedWriter endwriter;
	// file writer for timecourse results
	public BufferedWriter timewriter;
	// store list of parameter names
	public static String[] paramnames = new String[] {"PredFreq", "HuntSteps", "HetFreq",
			"AccAlarm", "FAlarm", "PredDet", "AlarmDet", "ConDet", "AgentPredRange",
			"AgentAlarmRange", "AgentConRange", "HetRange", "PredRange", "Align", "PopThresh",
			"Vigilance", "VigProp", "IndProp", "MutRate", "FThresh", "LocalBirth", "LearnAlg",
			"LRate"};
	// {"PredFreq", "AccAlarm", "FAlarm", "PredDet", "AlarmDet", "ConDet", "PopThresh",
	//	"VigProp", "IndProp", "MutRate", "FThresh", "PredForage", "PredFlee", "LearnAlg",
	//	"LRate", "Seed", "Steps", "Reps", "FileName", "TestInt"};
	
	/*
	 * checks all the arguments and manages the runs
	 */
	public Runner(String[] args) {
		// make sure there are enough parameters
		if(args.length < paramnames.length+5) {
			System.out.println("Not enough arguments!");
			System.exit(0);
		}
		// initialize the list of params
		this.params = new String[paramnames.length];
		// run through args to initialize the base params and determine which parameters to test
		for(int i = 0; i < paramnames.length; i++) {
			parse(i, args[i+5]);
		}
		// create output file
		try {
			// files to write the results to
			this.endwriter = new BufferedWriter(new FileWriter(args[3]+"endresults.txt"));
			this.timewriter = new BufferedWriter(new FileWriter(args[3] + "timeresults.txt"));
			// write in a header
			makeHeader(this.endwriter, false);
			makeHeader(this.timewriter, true);
		} catch(IOException e) {
			System.out.println("Something's wrong with your results files!");
			System.exit(0);
		}
		try {
			// if there are no test params, just test
			if(this.testparams.size() == 0) {
				// test the model for the given seed, number of steps, replications,
				// and the test interval
				test(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
					Integer.parseInt(args[2]),
					new ArrayList<String>(Arrays.asList(this.params)), Integer.parseInt(args[4]));
			} else {
				// otherwise sweep
				sweep(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
						Integer.parseInt(args[2]),
						new ArrayList<String>(Arrays.asList(this.params)), Integer.parseInt(args[4]), 0);
			}
		} catch (NumberFormatException e) {
			System.out.println("Error in Runner: Constructor! Make sure all the parameters are the right type!");
			System.exit(0);
		}
		try {
			this.endwriter.close();
			this.timewriter.close();
		} catch (IOException e) {
			System.out.println("Writer not closing...");
		}
	}
	
	/*
	 * parses inputed strings into lists of variables
	 */
	public void parse(int param, String val) {
		// split the value between spaces
		String[] vals = val.split(" ");
		// the first value is the starting value of that parameter
		this.params[param] = vals[0];
		// if there are more values provided store them for testing
		if(vals.length > 1) {
			// first store this parameter
			this.testparams.add(param);
			// then store the remaining values
			this.testvals.add((new ArrayList<String>(Arrays.asList(vals))));
		}
	}
	
	/*
	 * Writes the header for a results file
	 */
	public void makeHeader(BufferedWriter writer, boolean time) {
		try {
			// start with the base parameters
			writer.write("% Base Parameters: ");
			// loop through each parameter and its base values
			for(int p = 0; p < this.params.length; p++) {
				writer.write(paramnames[p]);
				writer.write(" = ");
				writer.write(params[p]);
				writer.write(", ");
			}
			// new line
			writer.write("\n\n");
			// Now for the test parameters
			writer.write("% Test Parameters:\n");
			// loop through each test parameter and its values
			for(int t = 0; t < this.testparams.size(); t++) {
				writer.write("%" + paramnames[this.testparams.get(t)]);
				writer.write(" = ");
				writer.write(this.testvals.get(t).toString());
				writer.write("\n");
			}
			// make the header for the table
			// start this next line with a percent sign to comment it out for analysis
			writer.write("% ");
			// if this file will hold time results, add an extra tab to make space for the time
			if(time) {
				writer.write("\t");
			}
			// indicate the categories for the results if there are test parameters
			if(this.testparams.size() > 0) writer.write("Parameters");
			// leave enough space for each parameter
			for(int t = 0; t < this.testparams.size(); t++) writer.write("\t");
			// then indicate population and learning with and without alarm
			writer.write("Population\t\t\tLearning with Alarm\t\t\tLearning without Alarm\n");
			// if this file will hold time results, add an extra column for the timestep
			if(time) {
				writer.write("Timestep\t");
			}
			// then loop through all the test parameters and add headers for each parameter
			for(int t = 0; t < this.testparams.size(); t++) {
				writer.write(this.paramnames[this.testparams.get(t)] + "\t");
			}
			// and add the headers for the actual results for population and learning
			for(int i = 0; i < 3; i++) {
				writer.write("vigilance only\tindividual learning\tsocial learning\t");
			}
			// and then do a line break
			writer.write("\n");
		} catch(IOException e) {
			System.out.println("Something went wrong while making the header...");
			System.exit(0);
		}
	}
	
	/*
	 * In charge of running the sweeps recursively
	 */
	public void sweep(int seed, int steps, int reps, ArrayList<String> params, int testint, int tdex) {
		// test each value of this parameter
		for(int v = 0; v < this.testvals.get(tdex).size(); v++) {
			// create a copy of params with this parameter value
			ArrayList<String> paramcopy = new ArrayList<String>(params);
			paramcopy.set(this.testparams.get(tdex), this.testvals.get(tdex).get(v));
			// if this isn't the last parameter, test the values of the remaining parameters
			if(tdex < this.testparams.size()-1) {
				sweep(seed, steps, reps, paramcopy, testint, tdex+1);
			} else {
				// otherwise, run this simulation
				test(seed, steps, reps, paramcopy, testint);
			}
		}
	}
	
	/*
	 * Starts making the table in which the results will be recorded (not currently in use)
	 */
	public void startTable(BufferedWriter writer, int dex) {
		try {
			// if this is the second to last parameter, make a table
			if(dex == this.testparams.size()-2) {
				writer.write("\n");
				writer.write(paramnames[this.testparams.get(dex+1)]);
				writer.write("\n");
				writer.write(paramnames[this.testparams.get(dex)]);
				// loop through all the values of the next parameter for the header
				for(int v = 0; v < this.testvals.get(dex+1).size(); v++) {
					writer.write(this.testvals.get(dex+1).get(v));
					writer.write(" v\t i\t s\t");
				}
			}
			// if this is the only parameter, print the label
			if(this.testparams.size() == 1) {
				writer.write(paramnames[this.testparams.get(dex)]);
			}
		} catch (IOException e) {
			System.out.println("Trouble writing trial headers to file in startTable");
		}
	}
	
	/*
	 * Completes the table in which the results will be recorded (not in use)
	 */
	public void endTable(BufferedWriter writer, int dex, int v) {
		try {
			// if this is the second to last or only parameter, finish the table
			if(dex == this.testparams.size()-2 || this.testparams.size() == 1) {
				writer.write("\n");
				writer.write(this.testvals.get(dex).get(v));
				writer.write("\t");
			}
			// if this isn't one of the last two parameters write down this parameter value
			if(dex < this.testparams.size()-2) {
				writer.write("\n");
				writer.write(paramnames[this.testparams.get(dex)]);
				writer.write(" = ");
				writer.write(this.testvals.get(dex).get(v));
			}
		} catch (IOException e) {
			System.out.println("Trouble writing trial headers to file in endTable");
		}
	}
	
	/*
	 * Actually runs the simulation on the provided parameters
	 */
	public void test(int seed, int steps, int reps, ArrayList<String> params, int testint) {
		System.out.println(params.toString());
		// create a model
		SimState state = new AlarmModel(seed);
		// cast the model as AlarmModel to access its fields
		AlarmModel model = (AlarmModel) state;
		// set model parameters from args
		String[] p = new String[params.size()];
		model.setParams(params.toArray(p));
		// gui is true, just run with gui
		// otherwise run the simulation manually and gather data
		// save the results in an array
		double[][] results = new double[3][steps/testint];
		// also store the final learning results
		double[][] alarmres = new double[3][steps/testint];
		double[][] noalarmres = new double[3][steps/testint];
		// run the same simulation for the designated number of replications
		for(int i = 0; i < reps; i++) {
			// start the simulation
			state.start();
			// run the simulation for that number of steps
			while(state.schedule.getSteps() < steps) {
				// if this is the right step add this step's counts to the results array
				if(state.schedule.getSteps()%testint == 0) {
					// store array of count
					int[] popcounts = new int[] {model.vigcount, model.indcount, model.soccount};
					System.out.println(state.schedule.getSteps());
					for(int j = 0; j < 3; j++) {
						results[j][(int) (state.schedule.getSteps()/testint)] += popcounts[j];
						alarmres[j][(int) (state.schedule.getSteps()/testint)] += model.alarmresp[j]/popcounts[j];
						noalarmres[j][(int) (state.schedule.getSteps()/testint)] += model.noalarmresp[j]/popcounts[j];
					}
				}
				if (!state.schedule.step(state)) break;
			}
			state.finish();
		}
		// try to write all those results to file
		try {
			// starting with the final population and learning results
			// first write in all the test parameter values for this run
			for(int t = 0; t < this.testparams.size(); t++) {
				this.endwriter.write(params.get(this.testparams.get(t)) + "\t");
			}
			// then write each set of results to the file from this array
			double[][][] allres = new double[][][] {results, alarmres, noalarmres};
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					this.endwriter.write(allres[i][j][steps/testint-1] + "\t");
				}
			}
			// lastly, make a newline
			this.endwriter.write("\n");
			// and then do the timecourse results
			// start with the parameter values used for this run
			this.timewriter.write("% " + params.toString() + "\n");
			// and then write the results and for each step
			for(int s = 0; s < steps/testint; s++) {
				// first write the timestep
				this.timewriter.write(testint*s + "\t");
				// then write the parameter values for this run (which will be the same on each line)
				for(int t = 0; t < this.testparams.size(); t++) {
					this.timewriter.write(params.get(this.testparams.get(t)) + "\t");
				}
				// then we can write the population and learning results (with and without alarm)
				for(int i = 0; i < 3; i++) {
					// and this is for each genotype
					for(int v = 0; v < 3; v++) {
						this.timewriter.write(allres[i][v][s]/reps + "\t");
					}
				}
				// and now go to the next line
				this.timewriter.write("\n");
			}
		} catch (IOException e) {
			System.out.println("Failed to write results to file...");
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			// for now I'm going to set the parameters manually
			// first some meta-parameters
			String seed = "0";			// 0
			String steps = "1000000";	// 1
			String reps = "1";			// 2
			String fname = "test";		// 3
			String testint = "10000";	// 4
			// and then all the base parameters
			String predfreq = ".001";	// 5
			String huntsteps = "1";		// 6
			String hetfreq = ".01";		// 7
			String accalarm = "1";		// 8
			String falarm = "0";		// 9
			String preddet = "1";		// 10
			String alarmdet = "1";		// 11
			String condet = "1";		// 12
			String apredrange = "1";	// 13
			String aalarmrange = "4";	// 14
		    String aconrange = "2";		// 15
		    String hetrange = "4";		// 16
		    String predrange = "2";		// 17
		    String align = "false";		// 18
			String popthresh = "1000";	// 19
			String vigilance = "true";	// 20
			String vigprop = ".334";	// 21
			String indvprop = ".333";	// 22
			String mutrate = ".001";	// 23
			String fthresh = "25";		// 24
			String localbirth = "false";// 25
			String learnalg = "n";		// 26
			String lrate = ".01";		// 27
			// and put them all into a string array
			String[] params = {seed, steps, reps, fname, testint, predfreq, huntsteps,
					hetfreq, accalarm, falarm, preddet, alarmdet, condet, apredrange,
					aalarmrange, aconrange, hetrange, predrange, align, popthresh, vigilance,
					vigprop, indvprop, mutrate, fthresh, localbirth, learnalg, lrate};
			args = params;
		}
		// TODO - make sure the probability of a false alarm is lower than the predator frequency
		// TODO - values to run for actual trials
//		args[0] = ".005 .01 .05";
//		args[3] = "0 .5 1";
//		args[4] = "";
//		args[14] = ".01 .05 .1"; // just for nn and rw, possibly ".01 .02 .1" for rw instead
				
//		args[1] = ".999";
//		args[2] = ".0001";
//		args[0] = ".00001 .0001 .001 .01 .1";
		args[26] = "n r b";
		args[3] = "LearnAlgSpaceTest";
		Runner run = new Runner(args);
	}
}
