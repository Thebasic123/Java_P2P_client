This program is a standard version of COMP3331 programming assignment(without extension function).

use standard bash scrip to generate users for program, which has to follow format :

xterm -hold -title "Peer x" -e "java cdht args1 args2 args3" &
where x represents peer name and args1, args2 and args3 are integer numbers 
which are current peer identity,first successor identity and second successor identity respectively.

There are two commands in this java program:
1.request xxxx
xxxx must be integers from 0-9, then current peer will get file position
2.quit
current peer will inform two predecessors, after receving confirmation from two predecessors, it will stop running automatically. 
Important: responses from two predecessors will not print out, and since 
peer doesn't know it's predecessors at the beginning, user must wait until program receives ping messages twice to run "quit" command, 
otherwise, program will tell user to wait few seconds then try again.

For the other inputs, program will not react but only print out:
"command" is not a valid command !!!
