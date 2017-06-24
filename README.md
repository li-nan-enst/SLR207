# SLR207
LI Nan's github for the course SLR207 at ENST

## The main idea:

### The phase of spliting

__Master__ lanch the __Slaves__.

And __Master__ lets __Slave__ copies the file source from to local space. (/tmp/username/splits) - Sx.txt

### The phase of mapping

Each __Slave__ does the mapping. - UMx.txt (corresponds to the Sx.txt)

### The phase of shuffling

__Master__ assign the task for shuffling by using a random way to avoid unbalance. 

The __Slaves__ do the shuffling. - SMx.txt

### The phase of reducing

Each __Slave__ then does the reducing. - RMx.txt (corresponds to the SMx.txt)

### The phase of final output

__Master__ does the summary.


## Requirement and instructions: 

https://goo.gl/R5jM3C

## Reference: 

http://ercoppa.github.io/HadoopInternals/HadoopArchitectureOverview.html

Intresting pictures:
