# mathematical simulation for the alarm model

# start with functions for the learning model (at equilibrium)
indalarm = function(a, p, d){
  return(a*p*d)
}
indnoalarm = function(a, p, d){
  return((1-a)*p*d)
}
socalarm = function(a, p, d, s){
  return(a*p*d*(1+p*(1-d)*(1-s)*a)/(1-p*(1-d)*a*s))
}
socnoalarm = function(a, p, d, s){
  return((1-a)*p*d*(1+(1-p+p*(1-d)*(1-a))*(1-s))/(1-(1-p+p*(1-d)*(1-a))*s))
}

# I'm going to evaluate these for a range of parameter values
lresults = data.frame(a=double(), p=double(), d=double(), s=double(), ialarm=double(), inoalarm=double(),
                     salarm=double(), snoalarm=double())
for(a in seq(0, 1, .1)){
  for(p in seq(0, 1, .1)){
    for(d in seq(0, 1, .1)){
      for(s in seq(0, 1, .1)){
        results[nrow(results)+1, ] = c(a,p,d,s,indalarm(a,p,d),indnoalarm(a,p,d),
                                       socalarm(a,p,d,s),socnoalarm(a,p,d,s))
      }
    }
  }
}
# and make some plots
testparams = c("a", "p", "d", "s")
plotr = gather(results, type, value, c("salarm", "ialarm", "snoalarm", "inoalarm"))
for(param in testparams){
  plotr$param = plotr[,param]
  pltr = summarize(group_by(plotr, param, type), N = length(value), mean = mean(value, na.rm = T),
                   sd = sd(value, na.rm = T), se = sd / sqrt(N))
  plot = ggplot(pltr, aes(x = param, y = mean, color = type)) + geom_line() + xlab(param)
  ggsave(paste(param, "learning.png"))
}

# and now I can use those in the evolution model
# I'm going to start by calculating the relative fitness of each strategy, though there should be some way to solve for s
fitness = function(a, p, d, b, c, s = NULL){
  # if there proprotion of social learners is included, get their learning withand without alarm
  if(!is.null(s)){
    alarm = socalarm(a, p, d, s)
    noalarm = socnoalarm(a, p, d, s)
  } else {
    # otherwise get individual learning
    alarm = indalarm(a, p, d)
    noalarm = indalarm(a, p, d)
  }
  # and then use that to calculate the fitness
  return(a*p*(1-alarm)*(b-c)+((1-a)*p*(b-c)+(1-p)*b)*(1-noalarm))
}

# now I'm going to initialize a dataframe with all the parameter values, which will hold the results
eresults = data.frame(a=double(), p=double(), d=double(), s=double(), b=double(), c=double(), indv=double(),
                     ialarm=double(), inoalarm=double(), socv=double(), salarm=double(), snoalarm=double())
# loop through a bunch of parameter values and get the results
for(a in seq(0, 1, .2)){
  for(p in seq(0, 1, .2)){
    for(d in seq(0, 1, .2)){
      for(s in seq(0, 1, .2)){
        for(b in seq(0, 10, 2)){
          for(c in seq(0, 10, 2)){
            results[nrow(results)+1, ] = c(a,p,d,s,b,c,fitness(a,p,d,b,c),indalarm(a,p,d),indnoalarm(a,p,d),
                                           fitness(a,p,d,b,c,s=s),socalarm(a,p,d,s),socnoalarm(a,p,d,s))
          }
        }
      }
    }
  }
}

# replace NaNs with 0
results[is.na(results)] = 0

# one variable plots
testparams = c("a", "p", "d", "s", "b", "c")
plotr = gather(results, type, value, c("socv", "indv"))
for(param in testparams){
  plotr$param = plotr[,param]
  pltr = summarize(group_by(plotr, param, type), N = length(value), mean = mean(value, na.rm = T),
                   sd = sd(value, na.rm = T), se = sd / sqrt(N))
  plot = ggplot(pltr, aes(x = param, y = mean, color = type)) + geom_line() + xlab(param)
  ggsave(paste(param, ".png"))
}

# two variable plots
res = data.frame(alarm = results$a, predator = results$p, detection = results$d, social = results$s,
                 benefit = results$b, cost = results$c, socprop = results$socv/(results$socv + results$indv))
testparams = c("alarm", "predator", "detection", "social", "benefit", "cost")
for(p1 in testparams){
  for(p2 in testparams){
    if(p1 != p2){
      plotResults(res, c(p1, p2), paste(p1, p2), list(c("socprop")), c("Relative Fitness of Social Learners"), catlabels = p2)
    }
  }
}

# I'm going to independently look at the effect of responsiveness on fitness, just to get a sense of the fitness landscape
flandscape = data.frame(a=double(), p=double(), alarm=double(), noalarm=double(), b=double(), c=double(), fitness=double())
for(a in seq(0, 1, .2)){
  for(p in seq(0, 1, .2)){
    for(alarm in seq(0, 1, .2)){
      for(noalarm in seq(0, 1, .2)){
        for(b in seq(0, 10, 2)){
          for(c in seq(0, 10, 2)){
            fitness = a*p*(1-alarm)*(b-c)+((1-a)*p*(b-c)+(1-p)*b)*(1-noalarm)
            flandscape[nrow(results)+1, ] = c(a,p,alarm,noalarm,b,c,fitness)
          }
        }
      }
    }
  }
}