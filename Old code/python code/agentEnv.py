# agentEnv.py
# Manages the environment for the agent based alarm call recognition experiment
# Aviva Blonder

from neuralnet import neuralnet
import random
import numpy as np
import copy

class environment:

    """
    Initializes parameter values
    predfreq - probability of a predator being present
    accalarm - probability of an alarm occuring when a predator is present
    falarm - probabiity of an alarm occuring when a predator is not present
    preddet - probability of detecting a predator
    alarmdet - probability of hearing an alarm
    condet - probability of seeing a conspecific
    predr - degree of reinforcement to fleeing from seeing a predator (to be incorporated later)
    conr - degree of reinforcement to fleeing from seeing a conspecific fleeing (to be incorporated later)
    popsize - initial number of agents
    vigprop - initial proportion of agents that just rely on their own vigilance
    invprop - initial proportion of agents that just use individual learning
    mutrate - mutation rate
    fthresh - amount of foraging required to survive and reproduce
    predforage - probability of being eaten while foraging
    predflee - probablility of being eaten while fleeing
    seed - random seed
    lrate, topology, actfunc - parameters to create a neural network agent
    """
    def __init__(self, predfreq, accalarm, falarm, preddet, alarmdet, condet, predr, conr, popsize, vigprop, invprop, mutrate, fthresh, predforage, predflee, lrate, topology, actfunc, seed):
        # initialize all parameter values based on inputs
        self.predfreq = predfreq
        self.accalarm = accalarm
        self.falarm = falarm
        self.preddet = preddet
        self.alarmdet = alarmdet
        self.condet = condet
        self.predr = predr
        self.conr = conr
        self.popsize = popsize
        self.mutrate = mutrate
        self.fthresh = fthresh
        self.predforage = predforage
        self.predflee = predflee
        self.seed = seed
        self.netparams = [lrate, topology, actfunc]
        self.agents = []
        self.genomes = []
        for i in range(popsize):
            self.agents.append(neuralnet(lrate, topology, i*seed, actfunc))
            if i < vigprop*popsize:
                self.genomes.append('v')
            elif i < vigprop*popsize + invprop*popsize:
                self.genomes.append('i')
            else:
                self.genomes.append('s')


    """
    Runs the simulation for the given number of generations of the given number of steps each
    """
    def run(self, steps, gens):
        # seed random
        random.seed(self.seed)
        np.random.seed(self.seed)
        # if this simultion is only being run for one generation, store the surviving gene distributions and number of individuals fleeing after each step
        if gens == 1:
            genedist = np.zeros((steps, 4))
        # otherwise, store the genome distributions and average number of individuals fleeing for each generation
        else:
            genedist = np.zeros((gens, 4))
        # for each generation...
        for g in range(gens):
            # initialize no individuals to fleeing
            fleeing = np.zeros(len(self.agents))
            # initialize all individuals to have foraged no times
            foragecount = np.zeros(len(self.agents))
            # run the simulation for the designated number of steps and update the list of fleeing individuals and forage counts
            for s in range(steps):
                res = self.step(fleeing, foragecount, genedist, gens, steps, g, s)
                fleeing = res[0]
                foragecount = res[1]
                # at the end of each step, if all agents have died, the population is extinct and return gene distributions as they are
                if len(self.agents) == 0:
                    return genedist
            # if there is only one generation, return the genome distributions from each step now
            if gens == 1:
                return genedist
            # Otherwise, time for some evolution!
            # of the remaining agents, count up their genomes, and check to see which passed their threshold and have those reproduce
            for a in range(len(self.agents)):
                # add it to the gene distribution for this generation
                self.genecount(genedist, a, g)
            # sort agents by descending foraging success
            foragesuccess = np.argsort(-foragecount)
            # initialize the current individual to 0
            indv = 0
            # while there are fewer than the designated number of individuals in the population, reproduce in order of foraging success
            while len(self.agents) < self.popsize and foragecount[foragesuccess[0]] >= self.fthresh:
                # if this individual passed the threshold, reproduce
                if foragecount[foragesuccess[indv]] >= self.fthresh:
                    # create a new agent
                    self.agents.append(neuralnet(self.netparams[0], self.netparams[1], None, self.netparams[2]))
                    # check to see if there's a mutation
                    if random.random() < self.mutrate:
                        # if so, choose a random genome
                        self.genomes.append(np.random.choice(['v', 'i', 's']))
                    # otherwise just give the offspring its parent's genome
                    else:
                        self.genomes.append(self.genomes[foragesuccess[indv]])
                    # go on to the next individual
                    indv += 1
                # if they didn't pass the threshold, start back at the beginning
                else:
                    indv = 0
                # if we've reached the end of the list of individuals, start again at the front
                if indv >= len(foragecount):
                    indv = 0
            # and repeat!
        # once all the generations have been run, return the resulting change in gene distributions over time
        return genedist


    """
    Handles each step of the simulation
    """
    def step(self, fleeing, foragecount, genedist, gens, steps, g, s):
        # initialize the presence of an alarm to false
        alarm = False
        # determine whether a predator is present
        if random.random() < self.predfreq:
            # indicate that a predator is present
            pred = True
            # determine whether an accurate alarm call occurs
            if random.random() < self.accalarm:
                alarm = True
        else:
            # otherwise, indicate that a predator is absent
            pred = False
            #determine whether a false alarm occurs
            if random.random() < self.falarm:
                alarm = True
        # for each agent...
        a = 0
        while a < len(self.agents):
            # if there is only one generation, add this agent to the gene distribution counts
            if gens == 1:
                self.genecount(genedist, a, s)
            # initialize this agent to not fleeing
            fleeing[a] = 0
            # if an alarm call occured, determine whether the agent heard it and feedforward the corresponding input
            if self.genomes[a] != 'v' and alarm:
                if random.random() < self.alarmdet:
                    resp = self.agents[a].feedforward(np.array([1]))
                # otherwise, it is as though an alarm didn't occur
                else:
                    resp = self.agents[a].feedforward(np.array([-1]))
            # if no alarm call occured (or at least none that the agent detected) feedforward the corresponding input
            else:
                resp = self.agents[a].feedforward(np.array([-1]))
            # use a softmax to determine the agent's actual response
            probs = np.exp(resp)/np.sum(np.exp(resp))
            # determine whether the agent detected a predator or a conspecific fleeing and backpropagate the result
            if ((pred and random.random() < self.preddet) or
                (self.genomes[a] == 's' and np.max(np.random.binomial(1, fleeing*self.condet, fleeing.shape[0])) == 1)):
                # if a predator has been detected or conspecifics have been dedtected fleeing, reinforce fleeing
                self.agents[a].backpropagate(np.array([0, 1]))
            else:
                # otherwise reinforce foraging
                self.agents[a].backpropagate(np.array([1, 0]))
            # store this agent's response
            fleeing[a] = np.random.binomial(1,probs[1])
            # if they are foraging, increment the number of times they have foraged
            if fleeing[a] == 0:
                foragecount[a] += 1
            # If there is a predator, determine if it catches this agent
            if pred:
                # determine the agent's risk based on whether it is fleeing or foraging
                if fleeing[a] == 1:
                    risk = self.predflee
                else:
                    risk = self.predforage
                # determine if this agent is caught, and if so, remove its stats from the list
                if random.random() < risk:
                    self.agents.pop(a)
                    self.genomes.pop(a)
                    fleeing = np.delete(fleeing, a)
                    foragecount = np.delete(foragecount, a)
                    a -= 1
            # go to the next agent
            a += 1
        # Add the number of individuals fleeing to the extra row of genedist
        if gens == 1:
            # if there is only one generation, add it by step
            genedist[s, 3] = np.sum(fleeing)
        else:
            # otherwise add it by generation divided by number of steps
            genedist[g, 3] += np.sum(fleeing)/steps
        # at the end of each step, return the new list of fleeing agents and forage counts
        return (fleeing, foragecount)


    """
    Adds this agent to the genome distribution for the given time
    """
    def genecount(self, genedist, agent, time):
        # add this agent to the number of surviving agents with its genome
        if self.genomes[agent] == 'v':
            genedist[time, 0] += 1
        elif self.genomes[agent] == 'i':
            genedist[time, 1] += 1
        else:
            genedist[time, 2] += 1
