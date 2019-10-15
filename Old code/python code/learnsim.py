# learnsim.py
# Over time, does learning converge on the actual probabilities?

"""
Runs a Bayes learner on the simulation until it converges
"""
def lsim(predfreq, accalarm, falarm, epsilon):
    # initialize prior probability of predator given alarm to .5
    ppred = 0
    newpred = .5
    # run Bayes's theorem until it converges
    while abs(ppred-newpred) > epsilon:
        # set the new probability as the prior
        ppred = newpred
        # calculate the next probability
        newpred = accalarm*ppred/(predfreq*accalarm + (1-predfreq)*falarm)
    print(newpred)

lsim(.5, 1, 0, .0001)
