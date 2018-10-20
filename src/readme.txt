1. To complie the project open a Windows Command Prompt and type:
	
	compile.sh

2. Then to run type:
	
	run.sh
	
 * This command run 3 servers and 3 clients connected to each of them. 
 ** These clients make write requests till they finish up the given test case file.
 *** To create more clients you may use the following command format:
 	
 	java ClientSide/Client [type] [ip:port] [inputfile] 
 	
 - For instance:

	java ClientSide/Client r localhost:6001 
	java ClientSide/Client w localhost:6002 11.txt
	java ClientSide/Client t localhost:6003 (This is used to measure execution time without Lamport)

	 
3. You may also find a time analysis for Lamport in "performance.pdf".
 

