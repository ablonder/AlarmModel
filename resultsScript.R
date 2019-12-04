library(ggplot2)
library(tidyr)

setwd("Documents/Research/IMCapstone/ResultsCSVs/WithPredFlee/endresults")
setwd("../ResultsCSVs/WithPredFlee/endresults")
d = combineFiles(list.files(), c("vig", "lb", "la"), c("Testendresults.txt"),
                 c("Vigilance","LocalBirth","LearnAlg"))
setwd("../../../RResults")
# so graphs can be plotted by genotype
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

# all values of all parameters
plotResults(d2[d2$predfreq == .1 & d2$stayforage == 1, ], c("Vigilance", "LocalBirth", "huntsteps", "fleeforage", "noalarm", "LearnAlg type"), "realtestNoAlarm",
            list(c("count")), c("Survivng Agents"), c("Genotype"), scatter = T, shapelabels = c("Learning Algorithm"), savelv = 4)
plotResults(d2[d2$predfreq == .1 & d2$stayforage == 1, ], c("Vigilance", "LocalBirth", "huntsteps", "fleeforage", "type", "LearnAlg"), "realtestForageCost",
            list(c("foragecost")), c("Cost from Foraging"), c("Learning Algorithm"), scatter = F, savelv = 4)

plotResults(d2, c("Vigilance", "LocalBirth", "huntsteps", "predflee", "stayforage", "predfreq", "LearnAlg LearnAlg type"), "fleeforage0",
            list(c("alarm"), c("noalarm")), c("Response to Alarm", "Response to the Absence of an Alarm"), c("Genotype"), linelabels = c("Learning Algorithm"), shapelabels = c("Learning Algorithm"), savelv = 4)
plotResults(d2, c("Vigilance", "LocalBirth", "stayforage", "huntsteps", "predflee", "foragediff", "predfreq", "LearnAlg LearnAlg type"), "",
            list(c("count")), c("Surviving Agents"), c("Genotype"), linelabels = c("Learning Algorithm"), shapelabels = c("Learning Algorithm"), savelv = 4)

# histograms of parameter values
ggplot(d2, aes(x = lifespan, fill = type, color = type)) +
  geom_histogram(bins = 50, position = "identity", alpha = .5)

# comparing learning of social learners and individual learners
d3 = d
d3$AlarmDiff = d3$SocAlarm - d3$IndAlarm
d3$NoAlarmDiff = d3$IndNoAlarm - d3$SocNoAlarm
ggplot(d3, aes(x = NoAlarmDiff, fill = LearnAlg, color = LearnAlg)) +
  geom_histogram(binwidth = .01, alpha = .5, position = "identity")


# data without naive-bayes comparing all three genotypes
d5 = d2[d2$LearnAlg != 'b' & d2$Vigilance == "true",]
# I'm going to add log hunsteps, which will be more meaningful
d5$loghuntsteps = log(d5$huntsteps)
# start by looking at each parameter
testparams = c("LocalBirth", "loghuntsteps", "predflee", "stayforage", "foragediff", "predfreq", "LearnAlg")
for(p in testparams){
  plotResults(d5, c(p, "type"), p, list(c("count"), c("alarm"), c("noalarm"), c("predcost"), c("foragecost"),
                                        c("offspring"), c("lifespan")),
              c("Surviving Individuals", "Response to Alarm", "Response to No Alarm", "Cost of Predation",
                "Cost to Foraging", "Reproductive Success", "Lifespan"), c("Genotype"))
}

# a quick inspection of the cases where vigilance doesn't dominate
#d6 = d[d$LearnAlg != 'b' & d$Vigilance == "true" & d$vigcount < d$indcount+d$soccount, ]
d6 = d[d$LearnAlg != 'b' & d$Vigilance == "true", ]
# I'm going to add log hunsteps, which will be more meaningful
d6$loghuntsteps = log(d6$huntsteps)
#summary(d6)
d7 = gather(d6, type, count, c("vigcount", "indcount", "soccount"))
d7$type[d7$type == "vigcount"] = "vigilance"
d7$type[d7$type == "indcount"] = "individual"
d7$type[d7$type == "soccount"] = "social"

# data without naive-bayes for publication
d4 = d[d$LearnAlg != 'b' & d$Vigilance == "true",]
d4$SocProp = d4$soccount/(d4$soccount + d4$vigcount + d4$indcount)
d4$VigProp = d4$vigcount/(d4$soccount + d4$vigcount + d4$indcount)
# log huntsteps will be more informative when it's used on the x-axis
d4$loghuntsteps = log(d4$huntsteps)

# one parameter
testparams = c("Vigilance", "LocalBirth", "loghuntsteps", "predflee", "stayforage", "foragediff", "predfreq", "LearnAlg")
for(p in testparams){
  d4[,p] = as.factor(d4[,p])
  plot = ggplot(d4, aes(x = d4[, p], y = SocProp)) + geom_boxplot() + xlab(p)
  ggsave(paste(p, ".png"))
}

# all parameters split by pred freq
testparams = c("LocalBirth", "loghuntsteps", "predflee", "stayforage", "foragediff", "LearnAlg")
for(p in testparams){
  plotResults(d7, c("predfreq", p, "type"), paste("predfreq", p), list(c("count")), c("Surviving"), catlabels = "Genotype", savelv = 3)
}

# two parameters
for(p1 in testparams){
  for(p2 in testparams){
    if(p1 != p2){
      plotResults(d4, c(p1, p2), paste(p1, p2), list(c("VigProp")), c("Proportion of Vigilance Only"), catlabels = p2)
    }
  }
}

# three parameters
plotResults(d4, c("huntsteps", "predfreq", "stayforage", "foragediff"), "huntsteps predfreq stayforage foragediff", list(c("VigProp")), c("Proportion of Vigilance Only"), catlabels = "forage diff", savelv = 4)

# histogram of proportion of social learners
ggplot(d4, aes(x = SocProp)) +
  geom_histogram(bins = 50, position = "identity", alpha = .5)

# I'm going to look at those populations that are almost all social learners
d5 = d4[d4$SocProp > .9, ]
summary(d5)

# I wonder what happens if I look at the trajectories
setwd("../ResultsCSVs/WithPredFlee")
timed = combineFiles(list.files(), c("vig", "lb", "la"), c("Testtimeresults.txt"),
                 c("Vigilance","LocalBirth","LearnAlg"))
timed$condition = paste(timed$Seed, timed$predfreq, timed$huntsteps, timed$stayforage, timed$foragediff,
                        timed$predflee, timed$LearnAlg, timed$LocalBirth, timed$Vigilance)
timed1 = timed[timed$LearnAlg != 'b', ]
timed1$SocProp = timed1$soccount/(timed1$soccount + timed1$indcount + timed1$vigcount)
# I need to do something about this so it doesn't break the computer
ggplot(data = timed1, aes(x = Timestep, y = SocProp)) + geom_line()
