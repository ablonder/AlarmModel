# agentPractice.py
# A replication of Arbilly et al.'s 2011 experiment, "Evolution of social learning when high expected payoffs are associated with high risk of failure"
# Aviva Blonder

import numpy as np
import matplotlib.pyplot as plt
import random

class simulation:

    """
    Creates a new simulation with a population of n agents and intializes class variables
    """
    def __init__(self, n, lrate, slfail = 0, sfail = 0):
        # this numpy array will house each organism in the population
        # each row represents one organism with two sets of four attributes; the first is its genome and the second is its knowledge about the environment
        self.population = np.zeros((n, 2, 4))
        # all individuals in the first generation are non-learners, they will be randomly designated as pure producers or 50% scroungers
        self.population[:,0,0] = np.random.randint(0, 2, n)

        # initialize learning rate
        self.lrate = lrate
        # initialize proability of attributing socially learned information to the wrong patch
        self.soclfail = slfail
        # initialize probability of a scrounger attempting to join an unsucessful producer
        self.sfail = sfail
        # initialize patch payoffs and probabilities
        self.patches = np.array([[.25, 4], [1/3, 1.5], [.5, .75], [1, .25]])
        # initialize population size
        self.popsize = n


    """
    Evolves the population for G generations of T steps.
    """
    def evolve(self, G, T):
        # initialize population and payoff history storage
        pophist = np.zeros((G, self.popsize, 2, 4))
        payhist = np.zeros((G, self.popsize))
        # evolve for G generations
        for g in range(G):
            # initialize all cumulative payoffs for the new generation to 0
            payoffs = np.zeros(self.popsize)
            # initialize knowledge about patches to the mean payoff of all patches (.53)
            self.population[:,1,:] = .53
            # play the producer scrounger game for T steps
            for t in range(T):
                # initialize a list of sucessful producers and payoffs during this step
                sproducers = []
                # a list of unsucessful producers and patches
                usprods = []
                # and a list of scroungers
                scroungers = []
                # determine which individuals will produce and which patch they choose
                for i in range(self.popsize):
                    # designate whether this individual is a producer
                    prod = False
                    # if this individual is always a producer, turn the indicator to true
                    if self.population[i,0,0] == 0:
                        prod = True
                    # otherwise, flip a coin
                    else:
                        prod = bool(random.getrandbits(1))
                    # if this individual is a producer this round, choose a patch and get a payoff
                    if prod:
                        # calculate a softmax over knowledge about patches to get the probabilities
                        prob = np.exp(self.population[i,1,:])
                        prob /= np.sum(prob)
                        # use the probabilities to choose a patch
                        patchn = np.random.choice(4, p = prob)
                        patch = self.patches[patchn]
                        # draw a random number to determine the payoff
                        payoff = random.random()
                        # if the individual got a payoff, add it to the list of successful producers
                        if payoff < patch[0]:
                            sproducers.append([i, patchn, patch[1]])
                            payoff = patch[1]
                        else:
                            usprods.append([i, patchn])
                            payoff = 0
                        # either way, if this individual is capable of individual learning, learn from the experience
                        if self.population[i,0,1] > 0 and self.population[i,0,2] > 0:
                            self.learn(i, patchn, payoff)
                    # if this individual is a scrounger, add it to the list
                    else:
                        scroungers.append(i)
                # initialize a list of taken producers
                takenprods = []
                # determine which producer each scrounger will follow
                for s in scroungers:
                    # initialize payoff to 0
                    payoff = 0
                    # determine if this individual will choose to scrounge off of a successful producer
                    success = random.random()
                    if success > self.sfail:
                        # if they do, choose a successful producer to scrounge off of
                        prod = random.choice(sproducers)
                        # if that producer hasn't been taken by another scrounger yet, scrounge off it
                        if prod not in takenprods:
                            # add it to the list of taken producers
                            takenprods.append(prod)
                            # take half of its produced value
                            payoff = prod[2]/2
                            payoffs[s] += payoff
                            # give the producer its reduced payoff
                            payoffs[prod[0]] += payoff
                    # otherwise, choose an unsucessful producer
                    else:
                        prod = np.random.choice(usprods)
                    # if this individual can learn, do so
                    if self.population[i,0,1] > 0 and self.population[i,0,3] > 0:
                        # grab the producer's patch
                        patchn = prod[1]
                        # determine whether this individual will learn accurately from the experience
                        slacc = random.random()
                        # if they don't learn accurately, choose a random patch to "learn" about
                        if slacc < self.soclfail:
                            # create a list of possible patches
                            ps = range(4)
                            # remove the accurate patch
                            ps.remove(patchn)
                            # choose a random patch
                            patchn = random.choice(ps)
                        # learn about the chosen patch
                        self.learn(i, patchn, payoff)
                # loop back through the producers and give the ones that weren't scrounged on their full reward
                for p in sproducers:
                    if p not in takenprods:
                        payoffs[p[0]] += p[2]
            # at the end of each generation, store its data in the history
            pophist[g,:,:,:] += self.population
            payhist[g,:] += payoffs
            # take the total payoffs for this round and use them to create the next generation
            self.reproduce(payoffs)
        # once the simulation is done, evaluate it and return the results
        return self.evaluate(pophist, payhist)


    """
    Updates an individual's knowledge about a patch based on a recieved payoff.
    """
    def learn(self, individual, patch, payoff):
        self.population[individual,1,patch] = self.population[individual,1,patch]*self.lrate + (1-self.lrate)*payoff


    """
    Creates the next generation based on the total payoffs recieved by the last
    """
    def reproduce(self, payoffs):
        # find the top 50% individuals
        top50 = np.argsort(payoffs)[:int(self.popsize/2)]
        # create a population composed of two of each of the top individuals
        self.population = np.repeat(self.population[top50,:,:], 2, axis=0)
        
        # introduce random mutations at a rate of 1/population size per locus
        # generate a random number for each gene in each individual
        mutation = np.random.rand(self.popsize, 4)
        # determine if there is a mutation
        mutation = mutation < 1/self.popsize
        # loop through loci and if there is a mutation, change the allele
        for i in range(self.popsize):
            for l in range(4):
                # if there is a mutation change the allele
                if mutation[i,l]:
                    # if this is the learning type allele, choose randomly between the two other possible alleles
                    if l == 1:
                        alleles = [0, 1, 2]
                    # otherwise just make a list of two items
                    else:
                        alleles = [0, 1]
                    # remove the allele that the individual already has from the list of possible alleles
                    alleles.remove(self.population[i,0,l])
                    # choose one of the remaining alleles to give to the individual (trivial in all but the learning type case)
                    self.population[i,0,l] = random.choice(alleles)

        # shuffle all of the new individuals
        np.random.shuffle(self.population)


    """
    Analyzes each run however I decide to.
    """
    def evaluate(self, populations, payoffs):
        self.phenoHist(populations[-1,:,:,:])


    """
    Depicts the final histogram of phenotype frequencies
    """
    def phenoHist(self, pop):
        # to hold the number of individuals of each phenotype (not taking learning type into account)
        phenoCount = np.zeros(6)
        # I'm going to do this the lazy, inefficient way
        for i in range(pop.shape[0]):
            # first separate the pure producers from the part time producers
            if pop[i,0,0] == 0:
                # then divide out the nonlearners
                if pop[i, 0, 1] == 0 or pop[i, 0, 2] == 0:
                    phenoCount[0] += 1
                # and learners
                else:
                    phenoCount[1] += 1
            # otherwise, if this is a part time scrounger
            else:
                # separate out the nonlearners
                if pop[i, 0, 1] == 0 or (pop[i, 0, 2] == 0 and pop[i, 0, 3] == 0):
                    phenoCount[2] += 1
                # from those calpable of both social and individual learning
                elif pop[i, 0, 2] == 1 and pop[i, 0, 3] == 1:
                    phenoCount[5] += 1
                # and those capable of just individual learning
                elif pop[i, 0, 2] == 1:
                    phenoCount[3] += 1
                # or just social learning
                else:
                    phenoCount[4] += 1
        # now divide all of the counts by the total to turn them into proportions
        phenoCount /= np.sum(phenoCount)
        # and create a bargraph
        plt.bar([.6, 1.6, 2.6, 3.6, 4.6, 5.6], phenoCount)
        plt.show()


    """
    Depicts the change in payoff and allele frequency over time
    """
    def timecourse(self, populations, payoffs):
        plt.plot(np.mean(payoffs, axis=1))
        plt.show()
        plt.plot(np.mean(populations[:,:,0,:], axis = 1))
        plt.legend(["strategy","learning type", "individual learning", "social learning"])
        plt.show()


sim = simulation(300, .5)
sim.evolve(3000, 100)
