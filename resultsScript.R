library(ggplot2)
library(tidyr)

setwd("Documents/Research/IMCapstone/ResultsCSVs/WithPredFlee/endresults")
setwd("../ResultsCSVs/WithPredFlee/EndResults")
d = combineFiles(list.files(), c("vig", "lb", "la"), c("Testendresults.txt"),
                 c("Vigilance","LocalBirth","LearnAlg"))
setwd("../../../RResults")
d2 = gather(d, type, count, c("vigcount", "indcount", "soccount"))
d2$type[d2$type == "vigcount"] = "vigilance"
d2$type[d2$type == "indcount"] = "individual"
d2$type[d2$type == "soccount"] = "social"
d2$alarm[d2$type == "vigilance"] = d2$VigAlarm[d2$type == "vigilance"]
d2$alarm[d2$type == "individual"] = d2$IndAlarm[d2$type == "individual"]
d2$alarm[d2$type == "social"] = d2$SocAlarm[d2$type == "social"]
d2$noalarm[d2$type == "vigilance"] = d2$VigNoAlarm[d2$type == "vigilance"]
d2$noalarm[d2$type == "individual"] = d2$IndNoAlarm[d2$type == "individual"]
d2$noalarm[d2$type == "social"] = d2$SocNoAlarm[d2$type == "social"]
d2$predcost[d2$type == "vigilance"] = d2$VigPredCost[d2$type == "vigilance"]
d2$predcost[d2$type == "individual"] = d2$IndPredCost[d2$type == "individual"]
d2$predcost[d2$type == "social"] = d2$SocPredCost[d2$type == "social"]
d2$foragecost[d2$type == "vigilance"] = d2$VigForageCost[d2$type == "vigilance"]
d2$foragecost[d2$type == "individual"] = d2$IndForageCost[d2$type == "individual"]
d2$foragecost[d2$type == "social"] = d2$SocForageCost[d2$type == "social"]
d2$offspring[d2$type == "vigilance"] = d2$VigOffspring[d2$type == "vigilance"]
d2$offspring[d2$type == "individual"] = d2$IndOffspring[d2$type == "individual"]
d2$offspring[d2$type == "social"] = d2$SocOffspring[d2$type == "social"]
d2$lifespan[d2$type == "vigilance"] = d2$VigLifespan[d2$type == "vigilance"]
d2$lifespan[d2$type == "individual"] = d2$IndLifespan[d2$type == "individual"]
d2$lifespan[d2$type == "social"] = d2$SocLifespan[d2$type == "social"]

plotResults(d2[d2$predfreq == .1 & d2$stayforage == 1, ], c("Vigilance", "LocalBirth", "huntsteps", "fleeforage", "noalarm", "LearnAlg type"), "realtestNoAlarm",
            list(c("count")), c("Survivng Agents"), c("Genotype"), scatter = T, shapelabels = c("Learning Algorithm"), savelv = 4)
plotResults(d2[d2$predfreq == .1 & d2$stayforage == 1, ], c("Vigilance", "LocalBirth", "huntsteps", "fleeforage", "type", "LearnAlg"), "realtestForageCost",
            list(c("foragecost")), c("Cost from Foraging"), c("Learning Algorithm"), scatter = F, savelv = 4)

plotResults(d2, c("Vigilance", "LocalBirth", "huntsteps", "predflee", "stayforage", "predfreq", "LearnAlg LearnAlg type"), "fleeforage0",
            list(c("alarm"), c("noalarm")), c("Response to Alarm", "Response to the Absence of an Alarm"), c("Genotype"), linelabels = c("Learning Algorithm"), shapelabels = c("Learning Algorithm"), savelv = 4)
plotResults(d2, c("Vigilance", "LocalBirth", "stayforage", "huntsteps", "predflee", "foragediff", "predfreq", "LearnAlg LearnAlg type"), "",
            list(c("count")), c("Surviving Agents"), c("Genotype"), linelabels = c("Learning Algorithm"), shapelabels = c("Learning Algorithm"), savelv = 4)

ggplot(d2, aes(x = lifespan, fill = type, color = type)) +
  geom_histogram(bins = 50, position = "identity", alpha = .5)


d3 = d
d3$AlarmDiff = d3$SocAlarm - d3$IndAlarm
d3$NoAlarmDiff = d3$IndNoAlarm - d3$SocNoAlarm
ggplot(d3, aes(x = NoAlarmDiff, fill = LearnAlg, color = LearnAlg)) +
  geom_histogram(binwidth = .01, alpha = .5, position = "identity")

d4 = d
d4$SocProp = d4$soccount/(d4$soccount + d4$vigcount + d4$indcount)
d4$LearnAlg = as.factor(d4$LearnAlg)
ggplot(d4, aes(x = LearnAlg, y = SocProp)) + geom_boxplot()
