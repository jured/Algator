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
executes all the implemented algorithms, checks the correctness and compares  
the quality of their results.  Using the ALGator user can add additional 
quality criteria, draw graphs and perform evaluations and comparisons of 
defined algorithms. 

## System instalation

Instalation of the single-user version of the ALGator system on Linux system:

* Create ALGator folder

  We will refere to this folder as <algator_root> folder

* Download ALGator zip file from

    https://raw.githubusercontent.com/ALGatorDevel/Algator/ALGator.zip

* Unpack ALGator.zip file

  

*****************************************************************************
3) Kratka navodila za namestitev programa ALGator kot samostojne aplikacije
*****************************************************************************
1. Preveden program ALGator z vsemi potrebnimi dodatki se nahaja v datoteki
ALGator.zip. Za uporabo programa prenesemo datoteko 

https://github.com/ALGatorDevel/Algator/blob/master/ALGator.zip?raw=true

in jo odpakiramo v poljuben direktorij (imenujmo ga <algator_root>).

2. Ustvarimo mapi <algator_root>/app in <algator_root>/app/ALGator in v slednjo
   prenesemo datoteko ALGator.jar in mapo lib. Končen cilj je taka struktura:

   <algator_root>
     app
       ALGator
         ALGator.jar
         lib
     data_root   
     data_local
 
3. Nastavimo štiri sistemske spremenljivke: 
  
       ALGATOR_ROOT=<algator_root>
       ALGATOR_DATA_ROOT=$ALGATOR_ROOT/data_root
       ALGATOR_DATA_LOCAL=$ALGATOR_ROOT/data_local
       CLASSPATH=$CLASSPATH:$ALGATOR_ROOT/app/ALGator/ALGator.jar

   (v prvem ukazu <algator_root> zamenjamo s pravim imenom mape)


4. Z ukazom 

  java algator.Version

preverimo pravilnost namestitve sistema (ALGator izpiše verzijo 
in nastavitev zgoraj opisanih sistemskih spremenljivk).

       
5. Z ukazom 

  java algator.Admin -ca <ime_projekta> <ime_algoritma>

ustvarimo nov projekt. Pri tem v mapi (in njenih podmapah)

  $ALGATOR_DATA_ROOT/projects/PROJ-<ime_projekta> 

nastanejo konfiguracijske datoteke. Datoteke uredimo skladno
z navodili (glej doc/ALGator.docx, poglavje Izdelava projekta 
in algoritma).


6. Izvajalni del ALGatorja poženemo z ukazom

       java algator.Execute <ime_projekta>
	
    analizo rezultatov pa z 

       java algator.Analyse <ime_projekta>



*****************************************************************************
2) Podroben opis delovanja sistema ALGator.jar
*****************************************************************************
https://github.com/ALGatorDevel/Algator/blob/master/doc/ALGator.docx?raw=true




*****************************************************************************
4) Navodila za avtorje projektov
*****************************************************************************
Pred implementacijo novega projekta je priporočljivo, da avtor prebere 
opis delovanja celotnega sistema, ki se nahaja v doc/ALGator.docx


*****************************************************************************
5) Navodila za razvijalce ALGatorja
*****************************************************************************
Vsa izvorna koda programa ALGator se nahaja v mapi development/ALGator/src.




*****************************************************************************
6) Pot do ALGator githuba:
  https://github.com/ALGatorDevel/Algator
*****************************************************************************


*****************************************************************************
7) Zahtevana programska oprema za uporabo ALGatorja:
    - java >= 7.0
    - v okolju Windows: cygwin rsync (https://cygwin.com/install.html)
*****************************************************************************
