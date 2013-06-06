setwd( "C:/Users/Emre/Documents/GitHub/dota2classifier")
args <- commandArgs(trailingOnly = TRUE)
library(changepoint)
deaths = read.table(args[2])
print(deaths[5,1])
N= length(deaths[,1])
print(N)

mydata = read.table(args[1])
m.data = ts(mydata[,2])
print(length(m.data))
m.bs=cpt.mean(m.data,penalty='Manual',pen.value='5*log(n)',method='PELT')
plot(m.bs)
for(i in 1:N) 
{
  abline(v=deaths[i,1],col=100,lty=5)
}
print(cpts(m.bs))