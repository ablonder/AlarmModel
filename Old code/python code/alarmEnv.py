# alarmEnv.py
# Manages the environment for the alarm call recognition experiment, so far just with a single neural network agent.
# Aviva Blonder

from neuralnet import neuralnet
import random
import numpy as np

class environment:

    """
    Initializes parameter values (can be changed later)
    predfreq - probability of a predator being present
    accalarm - probability of an alarm occuring when a predator is present
    falarm - probabiity of an alarm occuring when a predator is not present
    conresp - probability of a simulated conspecific responding to an alarm
    preddet - probability of detecting a predator
    alarmdet - probability of hearing an alarm
    condet - probability of seeing a conspecific
    predr - degree of reinforcement to fleeing from seeing a predator (to be incorporated later)
    conr - degree of reinforcement to fleeing from seeing a conspecific fleeing (to be incorporated later)
    seed - random seed
    lrate, topology, actfunc - parameters to create a neural network agent
    """
    def __init__(self, predfreq, accalarm, falarm, conresp, preddet, alarmdet, condet, predr, conr, lrate, topology, actfunc, seed):
        # initialize all parameter values based on inputs
        self.predfreq = predfreq
        self.accalarm = accalarm
        self.falarm = falarm
        self.conresp = conresp
        self.preddet = preddet
        self.alarmdet = alarmdet
        self.condet = condet
        self.predr = predr
        self.conr = conr
        self.seed = seed
        self.agent = neuralnet(lrate, topology, seed, actfunc)


    """
    Trains the agent for the given number of steps or until the alarm call response drops below a threshold.
    """
    def run(self, steps = None, thresh = None):
        # seed random
        random.seed(self.seed)
        # initialize continuing condition to true
        cont = True
        # initialize step count
        stepcount = 0
        # initialize response to alarm
        alarmresp = None
        # and response to the absence of an alarm
        noalarmresp = None
        # train until the stopping requiretments are met (either the given number of steps, or the provided threshold)
        while cont:
            # add to step count
            stepcount += 1
            # initialize presence of a predator to false
            pred = False
            # same with the presence of an alarm call
            alarm = False
            # and a conspecific fleeing
            con = False
            
            # determine whether a predator is present
            if random.random() < self.predfreq:
                # indicate that a predator is present
                pred = True
                # determine whether an accurate alarm call occurs
                if random.random() < self.accalarm:
                    alarm = True
            else:
                # otherwise, determine whether a false alarm occurs
                if random.random() < self.falarm:
                    alarm = True
            # if an alarm call occured, determine whether the agent heard it and feedforward the corresponding input
            if alarm:
                if random.random() < self.alarmdet:
                    # save output as the response to the alarm
                    alarmresp = self.agent.feedforward(np.array([1]))
                # otherwise, it is as though an alarm didn't occur
                else:
                    alarm = False
                # also determine if a conspecific responded
                if random.random() < self.conresp:
                    con = True
            # if no alarm call occured (or at least none that the agent detected) feedforward the corresponding input
            if not alarm:
                # save output as response to the absence of an alarm
                noalarmresp = self.agent.feedforward(np.array([-1]))
                
            # determine whether the agent detected a predator or a conspecific fleeing and backpropagate the result
            if (pred and random.random() < self.preddet) or (con and random.random() < self.condet):
                # if a predator has been detected or conspecifics have been dedtected fleeing, reinforce fleeing
                self.agent.backpropagate(np.array([0, 1]))
            else:
                # otherwise reinforce foraging
                self.agent.backpropagate(np.array([1, 0]))

            # determine whether the loop should continue
            # if we're stopping after a given number of steps, check to see if we've reached that amount
            if steps and stepcount == steps:
                # stop the loop
                cont = False
                # if there is also a threshold, just return the stepcount (this just acts as a failsafe so the loop doesn't go on forever)
                if thresh:
                    return stepcount
                # otherwise expose the network to an alarm and a lack of an alarm and return the response
                return (self.agent.feedforward(np.array([1])), self.agent.feedforward(np.array([-1])))
            # otherwise, if there is a threshold check to see if it has been reached
            if thresh:
                # initialize continue to false
                cont = False
                # if there has been an alarm so far, check to see if its response is above the threshold (and we should continue afterall)
                if alarmresp is not None and (alarmresp[0] > thresh or 1-alarmresp[1] > thresh):
                    cont = True
                # if there has been a step without an alarm so far, check to see if its reponse is above the threshold (and we should continue afterall)
                if noalarmresp is not None and (1-noalarmresp[0] > thresh or noalarmresp[1] > thresh):
                    cont = True
                # if it turns out the threshold has been met in both conditions (if they occur), return the number of steps that have passed
                if not cont:
                    return stepcount
