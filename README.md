<h1 align="center"><img src="doc/algator.png" alt="ALGator logo" /></h1>
<h4 align="center">An automatic algorithm evaluation system </h4>
<br>



## About the ALGator

ALGator facilitates automatic algorithm evaluation process by executing 
algorithms' implementations on the given predefined sets of test cases
and analyzing various indicators of the execution.  
To use the ALGator, user defines a project including the definition of 
the problem to be solved, sets of test cases, parameters 
of the input and indicators of the output data  and the criteria for the 
algorithm quality evaluation. When a project is defined, any number of 
algorithm implementations can be added. When requested, system 
executes all the implemented algorithms, checks for the correctness 
and compares the quality of their results. Using the ALGator user can 
add additional quality criteria, draw graphs and perform evaluations and 
comparisons of defined algorithms. 

## System instalation

Instalation of the single-user version of the ALGator system on Linux system:

1. **Create ALGator folder**

    We will refere to this folder as `<algator_root>` folder

2. **Download ALGator.zip** file from

    https://raw.githubusercontent.com/ALGatorDevel/Algator/ALGator.zip

3. **Unpack ALGator.zip** file to `<algator_root>`

	```algator_root$ unzip ALGator.zip```

 
4. **Set environment variables** 
  
  Add the following lines to `~/.bash_profile`:
  
  ```
  export ALGATOR_ROOT=<algator_root>
  export ALGATOR_DATA_ROOT=$ALGATOR_ROOT/data_root
  export ALGATOR_DATA_LOCAL=$ALGATOR_ROOT/data_local
  export CLASSPATH=$CLASSPATH:$ALGATOR_ROOT/app/ALGator/ALGator.jar
  ```

  Note: in the first line change the `<algator_root>` with the name of 
  your ALGator folder.

5. **To test correctness** of the instalation, type

  ```java algator.Version```


