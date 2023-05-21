## Introduction

For an organism to have offspring, it must not only acquire enough resources to reproduce, but also survive long enough to accumulate and use those resources. For many species, predation is a major risk to survival. However, this presents a trade-off because it is often difficult, if not impossible, to simultaneously avoid predators and forage for resources. As a result, predators not only reduce their prey’s fitness through consumptive effects - by killing and eating their prey - but also through non-consumptive effects - the costly defences prey animals use to avoid predation. Costly defenses from predation include physiological defense such as shells and camouflage, as well as changes in behavior, such as altering habitat use, time budgeting, and tolerating the presence of conspecifics (Lima 1998).

Natural selection should favor strategies that minimize both consumptive and non-consumptive costs of predation to maximize fitness in a given environment. Models have shown how animals can optimize their movement in space (e.g. Bracis et al. 2018) and their behavior over time. Standard models of signal detection theory suggest that animals should avoid foraging when predators are prevalent and only forage when it appears to be safe. However, more recent state-dependent models make the opposite prediction; that prey animals should be more likely to flee when predators are rare than when they are common (Trimmer et al. 2017). Similarly, the risk allocation hypothesis suggests that animals should handle the trade-off between foraging and predation by only foraging when predators are absent if predators are rare, incurring low consumptive costs and higher, but acceptable non-consumptive costs. But that if predators are common, their prey should forage whether predators are present or not, incurring higher consumptive costs, but low non-consumptive costs (Lima & Bednekoff 1999).

The decision of when to be vigilant and when to forage can be determined innately, but it can also be learned from experience. However, learning about predators is not an easy feat. Predators are necessarily uncommon relative to their prey, cues are not necessarily reliable, and each encounter with a predator may be an individual’s last. Despite the high cost of not attending to an accurate cue, animals cannot respond to every potential cue of a predator or else they would spend all of their time being vigilant and never forage. However, that makes it harder to learn to attend to valuable cues from necessarily limited learning opportunities. One way of increasing the amount of learning opportunities is through social learning. For many species, it is more likely that a conspecific will be present than a predator, so they can learn whether a cue is informative more efficiently based on how conspecifics respond to the cue instead of just relying on the presence of a predator.

Models have shown how animals can use the behavior of others to determine when to be vigilant (Jackson & Ruxton 2006). However, much of the work on social learning has been done from a cultural transmission perspective (e.g. Boyd & Richerson 1985), though it has been applied to more ecological contexts (for review see Kendall et al. 2004). In the broadest sense, social learning is expected to only evolve when there are enough individuals in the population that can learn on their own, to generate new information for social learners to acquire, resulting in an equilibrium population that is a mix of social and individual learners (Rogers 1988). Models of changing environments suggest that social learning should evolve when the environment changes too rapidly for the optimal behavior to have time to become innate, but slowly enough for information received from other individuals to still be accurate (Boyd & Richerson 1985). Dewar (2003) proposed a simple model of whether social learning about predators should be favored based on the costs of responding to and ignoring a predator if it is present or absent.

In this paper, I use mathematical and agent-based models to predict what conditions should favor the evolution of social learning to recognize a particular cue indicating the presence of a predator; heterospecific alarm calls. Animals of many species use alarm calls to detect predators. Alarm calls are usually thought to be intended to scare away the predator or warn conspecifics. However, they can also be heard by members of other species that happen to be present. Heterospecific alarm calls can contain valuable information, possibly at a reduced cost relative to conspecific alarm calls. Though some species have an innate capacity to recognize other species’ alarm calls, they can also be learned from experience (Magrath et al. 2014). For example, gray squirrels (Sciurus carolinensis) have been found to respond more strongly to the familiar alarm call of the American robin (Turdus migratorius) than to unfamiliar, but acoustically similar alarm calls made by the common blackbird (Turdus merula; Getschow et al. 2013). However, it is unknown whether individual learning is sufficient to recognize alarm calls, or if instead animals such as grey squirrels rely on the responses of others to determine which alarm calls to attend to and which to ignore. My model evaluates whether selection should favor social learning in the context of learning to recognize heterospecific alarm calls.

## Mathematical Model

### Learning

The dynamics of social and individual learning can be evaluated analytically and numerically using a mathematical model. Following Rogers (1988), this model assumes that learning happens at a much faster rate than evolution, so the equilibrium outcomes of learning can be calculated to determine how individuals with different strategies will behave. These behaviors can then used to determine the resulting payoffs to the different learning strategies.

Individual learners just learn whether to respond to an alarm and in the absence of an alarm based on whether they detect a predator under each of those conditions.

$$ P(resp|alarm,I) = P(alarm|predator)P(predator)P(detect) $$

$$ P(resp|noAlarm,I) = (1-P(alarm|predator))P(predator)P(detect) $$

Social learners learn whether to respond to an alarm and in the absence of an alarm based on whether they detect a predator or, if they don't detect a predator, the response of a conspecific (either a social learner or individual learner).

![](Writeup/Figures/AlarmResp.png)

![](Writeup/Figures/NoAlarmResp.png)

For simplicity all further analyses assume that alarms never occur in the absence of a predator, or $P(alarm|noPredator) = 0$, so $P(alarm) = P(alarm|predator)$.

Simpler notation will be used for writing out equations:

![](Writeup/Figures/SimpleNotation.png)

The learning equations can then be rewritten in terms of this notation:
![](Writeup/Figures/SimpleEqns.png)

Recursively solving for $\bar{L}_{as}$ (see appendix), we find that the equilibrium responsiveness of social learners to the presence of an alarm is:

$$ \bar{L}_{as} = apd\frac{1 + p(1-d)(1-s)a}{1-p(1-d)as}.$$

As predation, detection, alarm frequency, and the proportion of social learners increase, the amount of learned responsiveness to alarms also increases. Note that $pda$ is the probability of learning to respond to an alarm on their own, $p(1-d)(1-s)a$ is the probability of having an opportunity to learn from an individual learner, and $p(1-d)as$ is the probability of having an opportunity to learn from another social learner, all of which positively impact the probability of a social learner learning to respond to an alarm.

Recursively solving for $\bar{L}_{ns}$ (see appendix), we find that the equilibrium responsiveness of social learners to the absence of an alarm is:

$$L_{ns} = (1-a)pd\frac{1 + ((1-p) + p(1-d)(1-a))(1-s)}{1 - ((1-p) + p(1-d)(1-a))s}$$

This is analogous to the previous equation for the learned response to an alarm at equilibrium. Increases in predation, detection, and the proportion of social learners increase the amount of learned responsiveness to the absence of an alarm. Increasing the frequency of alarms decreases the learned responsiveness to the absence of an alarm. Responsiveness also increases as the probability of individually learning to respond to the absence of an alarm, $(1-a)pd$, the probability of learning from an individual learner, $((1-p) + p(1-d)(1-a))(1-s)$, and the probability of learning from a social learner, $((1-p) + p(1-d)(1-a))s$, increase.

Because $a$, $p$, $d$, and $s$ are probabilities (i.e. values between 0 and 1), social learners must always be more responsive than individual learners, regardless of the conditions (see appendix).

### Evolution

Individuals' learned responses to the presence and absence of an alarm determine their payoffs in the presence and absence of a predator (see payoff matrix). If an individual responds, whether in the presence or absence of an alarm, it is assumed to recieve no benefit from foraging, but incurs no cost of predation. If an individual does not respond, it gains the benefit of foraging, $b$, but if a predator is present, it also incurs some cost of predation, $c$.
![](Writeup/Figures/Payoffs.png)

These payoffs determine the fitness, $V(L)$, of each learning strategy, $L$, based on whether or not a predator is present and whether or not the individual detects an alarm. Since, if the individual responds, the payoff is always 0, the fitness is calculated based on the probability of not responding to an alarm or in the absence of an alarm.
![](Writeup/Figures/FitnessEqn.png)

This equation can then be used to determine the fitness of each strategy:

![](Writeup/Figures/StrategyFitness.png)

This is not analytically tractable, but can be used to draw some general conclusions. Social learning is only an evolutionarily stable strategy when the cost of not responding when there is a predator outweighs the benefit of continuing to forage, or $c > b$. Furthermore, social learning is always an evolutionarily stable strategy if additionally,

$$\frac{b}{c} < \frac{p(1-a)}{1-pa}.$$

Note that this can only be true if it is already the case that $c > b$. The higher the probability that there is a predator, but no alarm, and the higher the probability of there being both an alarm and a predator, the easier it is for social learners to invade when the costs are already higher than the benefits. Social learners are more responsive under both circumstances, and so are more likely to evade the costs of predation than individual learners, though they lose opportunities to forage.


## Agent-Based Model

The mathematical model above abstractly represents the costs of predation and missed foraging opportunities as variables, $c$ and $-b$. However, in actuality, these costs are very different. Being caught by a predator may mean the end of an animal’s life - preventing it from having any more reproductive opportunities - while a missed foraging opportunity means that the animal has just a little less energy to go toward reproduction, which can accumulate over time. A more biologically realistic simulation model can be used to compare these very different costs, as well as to take a more in depth look into the dynamics of social learning in the context of learning to attend to heterospecific alarm calls.

The agent-based model consists of a population of learning agents, as well as predators and alarm-calling heterospecifics, whose population sizes are determined in proportion to the number of agents alive at a given time. Agents, predators, and heterospecifics are distributed on a 2-dimensional grid. Each agent lives for a set number of discrete time steps, at the end of which the agent dies and is removed from the simulation. Each time step, all living agents have the opportunity to forage with some set probability of success to accumulate resources for reproduction. Even when an agent has accumulated enough resources to reproduce, it can only be successful when the total number of agents is below a set carrying capacity. In simulations with local reproduction, new agents are placed at the same coordinates as their parents, whereas in simulations without local reproduction, new agents are placed randomly on the grid.

Over the course of an agent’s lifespan, it also learns whether to respond to alarm calls, made by heterospecifics, in order to avoid being eaten by predators. If an agent responds to an alarm or in the absence of an alarm, its foraging success drops by a set amount, but it can move to avoid a predator and can, with some probability, escape a predator even if the predator enters the same square as the agent. If a predator successfully catches an agent, that agent dies and is removed from the simulation. For half of the simulations, all agents learned using a simple neural network, and in half of the simulations, all agents learned according to the Rescorla-Wagner (1972) model.

There are three learning strategies in the simulation; individual learning, social learning, and vigilance only. Which strategy an agent uses is determined by its genotype. Vigilance only agents ignore all alarm calls and just learn how frequently to respond based on how often predators are present. Individual learning agents learn whether to respond in the presence and absence of an alarm based on how often predators are present under each condition. Social learning agents learn whether to respond in the presence and absence of an alarm based on how often predators are present and how often they see other agents responding under each condition. Initially, there are equal proportions of all genotypes in the population. When an agent reproduces, it passes on its learning strategy to its offspring with a .001 probability of mutation.

The full ODD description of the model is in the attached appendix.


## Simulation Experiments

###Experiment 1

To evaluate the effect of learning rate on learned responsiveness to alarms and in the absence of an alarm, a miniature version of the model was run with one agent of each learning strategy, one predator, and one alarm-calling heterospecific on a five-by-five grid. Each simulation was run for 150 steps with no opportunities for death or reproduction. Simulations were run with a range of learning rates (.001, .01, and .1), for each learning algorithm (neural network and Rescorla-Wagner).

### Experiment 2

To determine the relative fitness of the different learning strategies under a range of conditions, the full model was run with a population of 1000 agents on a 100-by-100 square grid, using a learning rate of .01. The model was tested under all possible combinations of parameter values (listed in table below) to evaluate the effect of predator frequency, number of steps between predation attempts, baseline probability of successfully foraging, decrease in foraging success while attending to predators, probability of being caught by a predator while attentive, local reproduction, the learning algorithm used, and the presence of vigilance only agents.

![](Writeup/Figures/Exp2Params.png)

## Results and Discussion

As evidenced by the mathematical model, social learners should always be more attentive to predators, whether an alarm is present or not. This is consistent with the agent-based model when the learning rate is .01 for both the neural network and Rescorla-Wagner learning algorithms (see figure 1). That is, when learning is allowed to occur and reach a stable equilibrium, social learning always results in a stronger association between a cue, such as an alarm call, and a predator, because social learners have more opportunities for learning by attending to the responses of others.

However, just because social learners learn more efficiently does not mean social learning should necessarily be favored by natural selection. The mathematical model suggests that social learning should only be favored when alarms are highly accurate, the benefits of foraging are low, and the costs of predation are high. The mathematical results further suggest that the fitness of social learning sharply decreases as the number of social learners increases (see figure 2), because they are more likely to become overresponsive by responding to each other. This indicates that for social learners to become prevalent, the benefits of increased responsiveness must greatly outweigh the costs.

The costs and benefits of responding to predators were more explicitly modeled in the agent-based model. The agent-based model also compared the fitness of individual and social learning to a third strategy that did not attend to alarms at all, and merely learned how often to respond to predators based on their prevalence in the environment. Under most conditions, it was found that the third vigilance only strategy was the most successful. Learning to attend to alarms was only advantageous when the costs of missing foraging opportunities while attending to predators were low. That is, both the baseline probability of foraging successfully and the probability of foraging successfully while attending to predators had to be high. When attending to alarms was advantageous, social learning was generally favored (see figure 3) because of social learners’ increased responsiveness. Individual learning was not clearly favored over vigilance only and social learning, however the individual learning and vigilance only strategies were approximately equally advantageous when the decrease in foraging while respondsive was low (see figure 3) or predators were uncommon (see figure 4), in which case their responsiveness to alarms and in the absence of an alarm was the same (see figure 5). All three strategies were approximately equally advantageous when the cost of missed foraging opportunities was low and it was difficult for agents to evade predators even when attending to them (see figure 6).

These results not only suggest when and how animals should learn to attend to alarm calls, but also when and how animals should learn to respond to all different kinds of signals of predation. This suggests that it is not predation risk, but the cost of missed foraging opportunities that should primarily determine when attending to signals of predation is advantageous. This result is in line with previous work which suggests that when predators are prevalent, it is not worth the missed foraging opportunities to attend to them (e.g. Lima & Bednekoff 1999; Trimmer et al. 2017). It is reasonable that other decreases in foraging success would likewise make attending to the presence of predators untenable. It is also worth noting that this effect is evident in a model where resources only determine whether animals can reproduce, and there is no risk of dying from insufficient resources. If that was added, the effect of foraging success would likely be even stronger. This result also suggests that in cases where missed foraging opportunities are less costly, such as if animals live in groups where resources are pooled, social learning will be more strongly favored. This could result in a positive feedback loop where group living selects for social learning, which makes group living even more essential.