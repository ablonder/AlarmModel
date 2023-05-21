# experiment.py
# Evaluates the effect of changing various parameters on how quickly simple neural networks learn to respond to alarm calls
# Aviva Blonder

from alarmEnv import environment
import numpy as np
import matplotlib.pyplot as plt

"""
Change the value of each parameter (leaving all others constant at the optimal conditions) to determine how it affects learning rate
"""
def eachParam():
    # list of all the parameters' names (for labeling the graph later)
    params = ["predfreq", "accalarm", "falarm", "conresp", "preddet", "alarmdet", "condet", "predr", "conr"]
    # create an array to contain all the resulting values
    results = np.zeros((len(params), 9))
    # loop through all the parameters to change their values in turn
    for p in range(len(params)):
        print(params[p])
        results[p, :] = testParam(p, range(.1, 1, .1), steps = 1000, thresh = .01)
    # plot!
    plt.plot(np.arange(.1, 1, .1), results)
    plt.legend(params)
    plt.show()


"""
Creates an environment with a given set of parameters, runs it, and returns the corresponding results
"""
def test(params, steps = None, thresh = None):
    # create the environment
        env = environment(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
                          lrate = .05, topology = [1, 2], actfunc = 's', seed = 1)
        # run and grab the value
        res = env.run(steps, thresh)
        # if there is a threshold, just return it
        if thresh:
            return res
        # otherwise, calculate the sum squared error with and without an alarm and return that in a tuple
        else:
            return (np.sum(([0, 1] - res[0])**2), np.sum(([1, 0] - res[1])**2))
    

"""
Loop through a list of values for a given parameter and return an array of the results (based on threshold or total number of steps).
paramdex - index of where the parameter appears as an argument to environment
vals - list of values of that parameter to check
baseparams - list of baseline parameter values (assumed optimal unless given others)
thresh - threshold error value for stopping training
steps - the total number of steps of training
"""
def testParam(paramdex, vals, baseparams = [.5, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1], steps = None, thresh = None):
    # initialize a numpy array to hold the results
    # if using a threshold, it just needs to be one dimensional
    if thresh:
        results = np.zeros(vals.shape[0])
    # otherwise, it needs to have one column for the error in response to an alarm and one column for the error in response to the absence of an alarm
    else:
        results = np.zeros((vals.shape[0], 2))
    # loop through a range of provided parameter values
    for v in range(vals.shape[0]):
        # grab the actual value
        val = vals[v]
        # change the parameter value
        baseparams[paramdex] = val
        # run the environment and get the results
        res = test(baseparams, steps, thresh)
        # add the results into the corresponding list
        if thresh:
            results[v] = res
        else:
            results[v, 0] = res[0]
            results[v, 1] = res[1]
    # once it's all done, return the results array
    return results


"""
Graph the change in error over a range of values of a single parameter.
"""
def testOne(paramdex, vals, steps, name = None, pname = None):
    results = testParam(paramdex, vals, steps=steps)
    plotOne(vals, results, name, pname)


"""
Graph the change in error over a range of values of two parameters to see how they effect each other.
"""
def testTwo(paramdex1, paramdex2, vals1, vals2, steps, name = None, p1name = None, p2name = None):
    # initialize two arrays to hold the results
    alarm = np.zeros((vals1.shape[0], vals2.shape[0]))
    noalarm = np.zeros((vals1.shape[0], vals2.shape[0]))
    # initialize baseparams
    baseparams = [.5, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1]
    # loop through all the values of param 1 and test all possible values of param 2 for each
    for v in range(vals1.shape[0]):
        # change baseparams to match this value of param 1
        baseparams[paramdex1] = vals1[v]
        # test all possible values of param 2 with this value of param 1
        results = testParam(paramdex2, vals2, baseparams, steps)
        # split the results into alarm and noalarm and add to the respective arrays
        alarm[v, :] = results[:, 0]
        noalarm[v, :] = results[:, 1]
    # plot both arrays as images
    plotTwo(alarm, noalarm, name, p1name, p2name)


"""
Graph the change in test error over pairs of parameter values
"""
def testPair(paramdex1, paramdex2, vals1, vals2, steps, name = None, p1name = None):
    # initialize an array to hold the results
    results = np.zeros((vals1.shape[0], 2))
    # initialize baseparams
    baseparams = [.5, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1]
    # loop through all paired values of params 1 and 2 to test with all values of param 3
    for v in range(vals1.shape[0]):
        # change baseparams to match this value of param 1
        baseparams[paramdex1] = vals1[v]
        # change baseparams to match the corresponding value of param 2
        baseparams[paramdex2] = vals2[v]
        # test this pair of values
        res = test(baseparams, steps = steps)
        # split the results into alarm and noalarm and add to the respective arrays
        results[v, 0] = res[0]
        results[v, 1] = res[1]
    # plot 
    plotOne(vals1, results, name, p1name)


"""
Tests all values of param 3 against all paired values of params 1 and 2
"""
def testTrio(paramdex1, paramdex2, paramdex3, vals1, vals2, vals3, steps, name = None, p12name = None, p3name = None):
    # initialize alarm and no alarm arrays to hold the results
    alarm = np.zeros((vals1.shape[0], vals3.shape[0]))
    noalarm = np.zeros((vals1.shape[0], vals3.shape[0]))
    # initialize baseparams
    baseparams = [.5, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1]
    # loop through all paired values of params 1 and 2
    for v in range(vals1.shape[0]):
        # change baseparams to match this value of param 1
        baseparams[paramdex1] = vals1[v]
        # change baseparams to match the corresponding value of param 2
        baseparams[paramdex2] = vals2[v]
        # test this pair of values on all values of param 3
        res = testParam(paramdex3, vals3, baseparams, steps = steps)
        # split the results into alarm and noalarm and add to the respective arrays
        alarm[v, :] = res[:, 0]
        noalarm[v, :] = res[:, 1]
    plotTwo(alarm, noalarm, name, p12name, p3name)


"""
Plot error with and without an alarm over values of a single parameter
"""
def plotOne(xvals, yvals, name = None, pname = None):
    plt.plot(xvals, yvals, 'o-')
    plt.legend(["Alarm", "No Alarm"])
    if pname:
        plt.xlabel(pname + "Value")
    else:
        plt.xlabel("Parameter Value")
    plt.ylabel("Total Error")
    if name:
        plt.title(name)
    plt.show()


"""
Plot error with and without an alarm as images over the values of two parameters
"""
def plotTwo(alarm, noalarm, name = None, p1name = None, p2name = None):
    for a in range(2):
        # choose which set of results to display
        if a == 0:
            data = alarm
        else:
            data = noalarm
        # show the results as a heatmap image
        plt.imshow(data, cmap = "hot", origin = 'lower')
        plt.colorbar()
        # set the title
        n = ""
        if name:
            n += name
        if a == 1:
            n += " No"
        n += " Alarm Response"
        plt.title(n)
        # label the axes
        if p1name:
            plt.xlabel(p1name)
        if p2name:
            plt.ylabel(p2name)
        plt.show()
