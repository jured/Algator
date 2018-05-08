HOWTO debug ALGator algorithms with NetBeans

1) install algator 

2) create project (P1), algorithm (A1) and test set (TestSet1)

  java algator.Admin -cp P1
  java algator.Admin -ca P1 A1
  java algator.Admin -ct P1 TestSet1
  
  
3) create a new NetBeans project
  - select "New Project ..."   
      Choose Project
        select "Java Project With Existing sources" and click "Next >"
      Name and Location
        Project Name: P1
        Project Folder: <any_folder>
        click "Next >"
      Existing Sources:
        Source Package Folders - add two folders
          data_root/projects/PROJ-P1/proj/src and
          data_root/projects/PROJ-P1/algs/ALG-A1/src
        click "Finish"
  - select "Project P1 / Properties / Libraries / Add JAR/Folder"
    and add algator_root/app/ALGator/ALGator.jar and all
    jars from the algator_root/app/ALGator/lib folder
        
4) create a class Debug with the following main() method:

  public static void main(String[] args) {
    si.fri.algotest.global.ATGlobal.debugMode = true;
    algator.Execute.main(args);
  }
  
5) set execution parameters:
  - select "Project P1 / Properties / Run" 
      Main class: Debug
      Arguments : -dr <pot_do_algator_data_root_folderja> P1 -a A1 
  
5) Select  "Run / Clear and Build project (P1)"        
        
6) Set a brekpoint in source code and select "Debug / Debug project (P1)"
