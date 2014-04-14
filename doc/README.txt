Uporaba programa ALGator.jar

1. Program ALGator.jar (skupaj z mapo lib in njeno vsebino) namestimo na 
    sistem v poljuben direktorij (<algator_root>).

2. V CLASSPATH dodamo pot do programa ALGator.jar

       set CLASSPATH=$CLASSPATH;<algator_root>/dist/ALGator.jar

3. Nastavimo še spremenjlivko ALGATOR_DATA_ROOT, da kaže na mapo  
    z ALGator podatki. 


       set ALGATOR_DATA_ROOT=<algator_root>/data_root


4. Izvajalni del ALGatorja poženemo z ukazom

       java algator.Execute
	

analizo rezultatov pa z 


       java algator.Analyse
