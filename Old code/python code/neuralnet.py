# neuralnet.py
# Aviva Blonder
# A neural network model that learns through backpropagation.

import random
import math
import numpy as np
import matplotlib.pyplot as plt

class neuralnet:

    """
    constructor that creates intputpool and outputpool and sets lrate given...
    lrate - learning rate parameter
    topography - a list of the number of nodes of each pool in order from the input to output
    seed - a number to use to seed random
    actfunc - designates the activation function by its first character (s for sigmoid or r for relu)
    """
    def __init__(self, lrate, topography, seed = None, actfunc = None):
        # set learning rate parameter
        self.lrate = lrate
        # seed random
        #random.seed(seed)
        # save the activation function choice
        self.actfunc = actfunc

        # create the network
        # initialize a list of layer activations
        self.acts = []
        # and biases
        self.biases = []
        # and weights
        self.weights = []
        # go through the provided topography to add each layer and its parameters to the corresponding list
        for l in range(len(topography)):
            # create a numpy array to hold this layer's activation
            self.acts.append(np.zeros(topography[l]))
            # if this is not the first layer, also create parameters
            if l > 0:
                # create an array of biases
                self.biases.append(np.zeros(topography[l]))
                # and small random weights
                self.weights.append(.02*np.random.rand(topography[l], topography[l-1])-.01)


    """
    runs the neural network on a state of the environment
    state - a list of floats corresponding to some state of the environment
    returns the index of the node with the highest activation in the output layer
    """
    def feedforward(self, state):
        # save the activation of the first layer as the current state
        self.acts = [state]
        # loop through each layer and calculate its activation
        for l in range(len(self.biases)):
            # take the dot product of the activation and the weights, plus the biases
            act = np.dot(self.weights[l], self.acts[l]) + self.biases[l]
            # for the output layer just do that
            if l == len(self.biases)-1:
                self.acts.append(act)
            # for all other layers
            else:
                # then put it through the activation function if there is one
                self.acts.append(self.activation(act))
        # return the activation of the output pool
        return self.acts[-1]


    """
    backpropagates on the outcome of the action
    reward - actual outcome of taking the action
    """
    def backpropagate(self, target):
        # calculate the starting error for the output layer
        initerror = target - self.acts[-1]
        # set the current error to the starting error
        error = initerror
        # initialize newerror
        newerror = error
        # loop back through the layers and adjust the weights and biases by backpropagating the error
        for l in range(len(self.acts)-2, -1, -1):
            # if this isn't the input layer
            if l > 0:
                # calculate the error for the next layer using the derivative activation function
                newerror = self.acts[l]*self.dactivation(self.acts[l])*np.dot(error, self.weights[l])
            # adjust the bias by adding the error times the learning rate
            self.biases[l] += self.lrate*error
            # adjust the weights by taking into account the activation of the previous pool
            self.weights[l] += self.lrate*np.transpose(np.dot(np.reshape(self.acts[l], (self.acts[l].shape[0], 1)), np.reshape(error, (1, error.shape[0]))))
            # set error equal to the new error
            error = newerror
        # reutrn the initial error
        return initerror



    """
    Activation function.
    """
    def activation(self, inputs):
        # sigmoid
        if self.actfunc == 's':
            return 1/(1+np.exp(-inputs))
        # relu
        elif self.actfunc == 'r':
            # separate value to be returned from inputs
            output = np.copy(inputs)
            output[output < 0] = 0
            return output
        # no activation function
        else:
            return inputs


    """
    Derivative of the chosen activation function.
    """
    def dactivation(self, inputs):
        # sigmoid
        if self.actfunc == 's':
            return 1-inputs
        # relu
        elif self.actfunc == 'r':
            # separate value to be returned from inputs
            output = np.copy(inputs)
            output[output > 0] = 1
            output[output < 0] = 0
            return output
        # no activation function
        else:
            return 1
