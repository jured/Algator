package algator;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.users.DatabaseInit;

import si.fri.algotest.users.Entity;
import si.fri.algotest.users.Entity_permission;
import si.fri.algotest.users.Group;
import si.fri.algotest.users.Owner;
import si.fri.algotest.users.PermTools;
import si.fri.algotest.users.Permission;
import si.fri.algotest.users.User;

/**
 *
 * @author Gregor
 */
public class Users {

  private static Connection conn;

  private static ArrayList<Entity> project_list = new ArrayList<Entity>();
  private static ArrayList<Entity> alg_list;
  private static ArrayList<Entity> test_list;
  private static ArrayList<Owner> owner_list;
  private static Map<String, List<Entity>> alg_map = new HashMap<String, List<Entity>>();
  private static Map<String, List<Entity>> test_map = new HashMap<String, List<Entity>>();
  private static ArrayList<Entity_permission> project_permissions;
  private static ArrayList<Entity_permission> algorithm_permissions;
  private static ArrayList<Entity_permission> test_permissions;

  private static String format = "json";

  public static void help() {
    System.out.println("[] - optional args for command\n"
            + "- init \n"
            + "     Inits database and creates tables.\n"
            + "- adduser <username><password>\n"
            + "     Creates new user, requires username and password.\n"
            + "- addgroup <groupname>\n"
            + "     Creates new group, requires name.\n"
            + "- moduser <groupname><username>\n"
            + "     Adds user to group, requires groupname and username\n"
            + "- userperms <username>\n"
            + "     Outputs all premissions for all entities in the system for username.\n"
            + "- userperm [-id] <username> <id_enitity>/<projectname>/<projectname:::algorithmname>/<projectname;;;testsetname>\n"
            + "     Outputs all premissions for username on specific enitity. Requires username and id entity if [-id] switch is on. If switch is not set"
            + "it requires <projectname> or <projectname:::algorithname> or <projectname;;;testsetname> instead.\n"
            + "- canuser [-id] <username><permission> <id_enitity>/<projectname>/<projectname:::algorithmname>/<projectname;;;testsetname>\n"
            + "     Return true or false if username has specific permission on entity. Requeires username, permission code and id enitity if [-id] is on"
            + "if switch [-id] is not set, it requires <projectname> or <projectname:::algorithname> or <projectname;;;testsetname> instead.\n"
            + "- setowner\n"
            + "     Checks all the files (proj, alg, test) in system directory. If file has no owner, commands sets it on user id 1 - algator.\n"
            + "- showowner [-p] <projectname> / [-a] <projectname><algorithmname> / [-t] <projectname><testsetname>  \n"
            + "     Shows who is owner of project, algorithm or testset. Requires switch (-p for projects, -a for algorithms, -t for testsets)."
            + "     Also requires projectname and/or algorithmname or testset.\n"
            + "- addpermproj [-g] <name><permission_code><projectname>\n"
            + "     Adds permission for project to user or group. -g stands for group\n"
            + "- rmvpermproj [-g] <name><permission_code><projectname>\n"
            + "     Removes permission for project from user or group. -g stands for group\n"
            + "- addpermalg [-g] <name><permission_code><projectname><algorithmname>\n"
            + "     Adds permission for algorithm in project to user or group. -g stands for group\n"
            + "- rmvpermalg [-g] <name><permission_code><projectname><algorithmname>\n"
            + "     Removes permission for algorithm in project to user or group. -g stands for group\n"
            + "- showusers [<username>]\n"
            + "     Show all active users in the system. If arg <username> is passed to command, it shows only that specific user.\n"
            + "- showgroups\n"
            + "     Show all active groups in the system.\n"
            + "- showpermissions\n"
            + "     Show all permissions in the system.\n"
            + "- showentities\n"
            + "     Show all entities in the system.\n"
            + "- changeuserstatus <username><status>\n"
            + "     Changes status of user active/inactive. Status must be int (0/1)\n"
            + "- changegroupstatus <groupname><status>\n"
            + "     Changes staus of group active/inactive. Status must be int (0/1)");
  }

  public static boolean adduser(String username, String password) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      int id_user = -1;
      int id_group = -1;

      if (userExists(username, true) < 0) {
        String insert = "INSERT INTO algator.users(name,password) VALUES ('" + username + "','" + password + "')";

        int result = stmt.executeUpdate(insert, stmt.RETURN_GENERATED_KEYS);

        if (result > 0) {
          //get user id and group id
          ResultSet rs = stmt.getGeneratedKeys();
          if (rs.next()) {
            id_user = rs.getInt(1);
          }

          String select = "SELECT * from algator.groups WHERE name='Everyone'";
          rs = stmt.executeQuery(select);
          if (rs.next()) {
            id_group = rs.getInt(1);
          }

          insert = "INSERT INTO algator.group_users(id_user,id_group) VALUES (" + id_user + "," + id_group + ")";
          result = stmt.executeUpdate(insert);
          if (result > 0) {
            System.out.println(">>> User added!");
            return true;
          } else {
            System.out.println(">>> User added, but not added to group Everyone!");
            return true;
          }
        } else {
          System.out.println(">>> Error while adding user!");
          return false;
        }
      }
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean addgroup(String name) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      if (groupExists(name, true) < 0) {
        String insert = "INSERT INTO algator.groups(name) VALUES ('" + name + "')";

        int result = stmt.executeUpdate(insert);

        if (result > 0) {
          System.out.println(">>> Group added!");
          return true;
        } else {
          System.out.println(">>> Error while adding group!");
          return false;
        }
      }
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean moduser(String groupname, String username) {
    try {
      Statement stmt = (Statement) conn.createStatement();
      //init
      int id_group = groupExists(groupname, false);
      int id_user = userExists(username, false);

      if (id_group < 0) {
        System.out.println(">>> Group with this name does not exist!");
        return false;
      }

      if (id_user < 0) {
        System.out.println(">>> User with this username does not exist!");
        return false;
      }

      String select = "SELECT * from algator.group_users WHERE id_group=" + id_group + " AND id_user=" + id_user + "";
      ResultSet rs = stmt.executeQuery(select);

      if (rs.next()) {
        System.out.println(">>> User is already member of this group!");
        return false;
      } else {
        String insert = "INSERT INTO algator.group_users(id_group,id_user) VALUES ('" + id_group + "','" + id_user + "')";
        int result = stmt.executeUpdate(insert);

        if (result > 0) {
          System.out.println(">>> User added to group!");
          return true;
        } else {
          System.out.println(">>> Error while adding user to group!");
          return false;
        }
      }
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean addPermProj(String name, String permission, String entityname, String type) {
    try {
      Statement stmt = (Statement) conn.createStatement();
      int id = -1;

      if (type.equals("group")) {
        id = groupExists(name, false);
        if (id < 0) {
          System.out.println(">>> Group with this name does not exist!");
          return false;
        }
      } else {
        type = "users";
        id = userExists(name, false);
        if (id < 0) {
          System.out.println(">>> User with this name does not exist!");
          return false;
        }
      }

      int permExists = permExists(permission, true);
      int projExists = projExists(entityname, true);

      if (permExists < 0) {
        return false;
      }
      if (projExists < 0) {
        return false;
      }

      String insert = "INSERT INTO algator.permissions_" + type + "(id_" + type.substring(0, type.length() - 1) + ",id_entity,id_permission) VALUES (" + id + "," + projExists + "," + permExists + ")";

      int result = stmt.executeUpdate(insert);

      if (result > 0) {
        System.out.println(">>> Permission " + permission + " for project " + entityname + " added to " + name + "!");
        return true;
      } else {
        System.out.println(">>> Error while adding user!");
        return false;
      }

    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean addPermAlg(String name, String permission, String projectname, String algorithmname, String type) {
    load_entites();
    try {
      Statement stmt = (Statement) conn.createStatement();
      int id = -1;
      int proj_id = -1;
      if (type.equals("group")) {
        id = groupExists(name, false);
        if (id < 0) {
          System.out.println(">>> Group with this name does not exist!");
          return false;
        }
      } else {
        type = "users";
        id = userExists(name, false);
        if (id < 0) {
          System.out.println(">>> User with this name does not exist!");
          return false;
        }
      }
      int permExists = permExists(permission, true);

      if (permExists < 0) {
        return false;
      }
      for (Entity proj : project_list) {
        if (proj.name.equals(projectname)) {
          proj_id = proj.id;
        }
      }
      if (proj_id < 0) {
        System.out.println(">>> Project with this name does not exist!");
        return false;
      }
      List<Entity> alg_list = alg_map.get(Integer.toString(proj_id));

      for (Entity alg : alg_list) {
        if (alg.name.equals(algorithmname)) {
          String select = "SELECT * FROM algator.permissions_" + type + " WHERE id_" + type.substring(0, type.length() - 1) + "=" + id + " AND id_entity=" + alg.id + " AND id_permission=" + permExists + "";
          ResultSet rs = stmt.executeQuery(select);

          if (rs.next()) {
            System.out.println("Permission " + permission + " for algorithm " + alg.name + " already added to " + name + "");
            return false;
          }

          //else add perm
          String insert = "INSERT INTO algator.permissions_" + type + "(id_" + type.substring(0, type.length() - 1) + ",id_entity,id_permission) VALUES (" + id + "," + alg.id + "," + permExists + ")";

          int result = stmt.executeUpdate(insert);

          if (result > 0) {
            System.out.println(">>> Permission " + permission + " for algorithm " + alg.name + " added to " + name + "!");
            return true;
          } else {
            System.out.println(">>> Error while adding user!");
            return false;
          }
        }
      }
      System.out.println(">>> Algorithm " + algorithmname + " in project " + projectname + " does not exist!");
      return false;
    } catch (SQLException ex) {
      ATLog.log(ex.toString(), 0);
      return false;
    }
  }

  public static boolean rmvPermProj(String name, String permission, String projectname, String type) {
    try {
      Statement stmt = (Statement) conn.createStatement();
      int id = -1;

      if (type.equals("group")) {
        id = groupExists(name, false);
        if (id < 0) {
          System.out.println(">>> Group with this name does not exist!");
          return false;
        }
      } else {
        type = "users";
        id = userExists(name, false);
        if (id < 0) {
          System.out.println(">>> User with this name does not exist!");
          return false;
        }
      }

      int permExists = permExists(permission, true);
      int projExists = projExists(projectname, true);

      if (permExists < 0) {
        return false;
      }
      if (projExists < 0) {
        return false;
      }

      //check if this permission even exists on this user
      String select = "SELECT * FROM algator.permissions_" + type + " WHERE id_" + type.substring(0, type.length() - 1) + "=" + id + " AND id_entity=" + projExists + " AND id_permission=" + permExists + "";
      ResultSet rs = stmt.executeQuery(select);

      if (!rs.next()) {
        System.out.println("Permission " + permission + " for project " + projectname + " does not exist for " + name + "");
        return false;
      }

      String delete = "DELETE FROM algator.permissions_" + type + " WHERE id_" + type.substring(0, type.length() - 1) + "=" + id + " AND id_entity=" + projExists + " AND id_permission=" + permExists + "";
      int result = stmt.executeUpdate(delete);

      if (result > 0) {
        System.out.println(">>> Permission " + permission + " for project " + projectname + " removed from " + name + "!");
        return true;
      } else {
        System.out.println(">>> Error while removing!");
        return false;
      }

    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean rmvPermAlg(String name, String permission, String projectname, String algorithmname, String type) {
    load_entites();

    try {
      Statement stmt = (Statement) conn.createStatement();
      int id = -1;
      int proj_id = -1;

      if (type.equals("group")) {
        id = groupExists(name, false);
        if (id < 0) {
          System.out.println(">>> Group with this name does not exist!");
          return false;
        }
      } else {
        type = "users";
        id = userExists(name, false);
        if (id < 0) {
          System.out.println(">>> User with this name does not exist!");
          return false;
        }
      }

      int permExists = permExists(permission, true);

      if (permExists < 0) {
        return false;
      }
      for (Entity proj : project_list) {
        if (proj.name.equals(projectname)) {
          proj_id = proj.id;
        }
      }
      if (proj_id < 0) {
        System.out.println(">>> Project with this name does not exist!");
        return false;
      }
      List<Entity> alg_list = alg_map.get(Integer.toString(proj_id));

      for (Entity alg : alg_list) {
        if (alg.name.equals(algorithmname)) {
          String select = "SELECT * FROM algator.permissions_" + type + " WHERE id_" + type.substring(0, type.length() - 1) + "=" + id + " AND id_entity=" + alg.id + " AND id_permission=" + permExists + "";
          ResultSet rs = stmt.executeQuery(select);

          if (!rs.next()) {
            System.out.println("Permission " + permission + " for algorithm " + alg.name + " does not exist for " + name + "");
            return false;
          }

          //else remove perm
          String delete = "DELETE FROM algator.permissions_" + type + " WHERE id_" + type.substring(0, type.length() - 1) + "=" + id + " AND id_entity=" + alg.id + " AND id_permission=" + permExists + "";
          int result = stmt.executeUpdate(delete);

          if (result > 0) {
            System.out.println(">>> Permission " + permission + " for algorithm " + alg.name + " removed from " + name + "!");
            return true;
          } else {
            System.out.println(">>> Error while removing!");
            return false;
          }
        }
      }
      System.out.println(">>> Algorithm " + algorithmname + " in project " + projectname + " does not exist!");
      return false;

    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  private static void showUsers(String username) {
    String statement = "";
    if (username.equals("")) {
      statement = "SELECT * from algator.users";
    } else {
      statement = "SELECT * from algator.users WHERE name='" + username + "'";
    }
    try {
      Statement stmt = (Statement) conn.createStatement();
      ResultSet rs = stmt.executeQuery(statement);
      if (format.equals("json")) {
        Gson gson = new Gson();
        ArrayList<User> users = new ArrayList<>();
        while (rs.next()) {
          users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getInt("status")));
        }
        System.out.println(gson.toJson(users));
        return;
      }
      if (!rs.isBeforeFirst()) {
        System.out.println(">>> User with this username does not exist!");
        return;
      }
      System.out.printf("%1$-20s %2$10s", "Username", "Status");
      System.out.println("");
      while (rs.next()) {
        String lastName = rs.getString("name");
        String status = rs.getString("status");
        System.out.printf("%1$-20s %2$8s", lastName, status);
        System.out.println("");
      }
    } catch (SQLException e) {
      System.err.println(e);
    }
  }

  private static void showGroups() {
    try {
      Statement stmt = (Statement) conn.createStatement();
      String select = "SELECT * from algator.groups";
      ResultSet rs = stmt.executeQuery(select);
      if (format.equals("json")) {
        Gson gson = new Gson();
        ArrayList<Group> groups = new ArrayList<>();
        while (rs.next()) {
          groups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("status")));
        }
        System.out.println(gson.toJson(groups));
        return;
      }
      System.out.printf("%1$-20s %2$10s", "Group name", "Status");
      System.out.println("");
      while (rs.next()) {
        String groupName = rs.getString("name");
        System.out.printf("%1$-20s %2$8s", groupName, "1");
        System.out.println("");
      }
    } catch (SQLException e) {
      System.err.println(e);
    }
  }

  private static void showPermissions() {
    try {
      Statement stmt = (Statement) conn.createStatement();
      String select = "SELECT * from algator.permissions";
      ResultSet rs = stmt.executeQuery(select);
      if (format.equals("json")) {
        Gson gson = new Gson();
        ArrayList<Permission> permissions = new ArrayList<>();
        while (rs.next()) {
          permissions.add(new Permission(rs.getInt("id"), rs.getString("permission"), rs.getString("permission_code")));
        }
        System.out.println(gson.toJson(permissions));
        return;
      }
      System.out.printf("%1$-20s %2$10s", "Permission", "Permission code");
      System.out.println("");
      while (rs.next()) {
        String lastName = rs.getString("permission");
        String status = rs.getString("permission_code");
        System.out.printf("%1$-20s %2$8s", lastName, status);
        System.out.println("");
      }
    } catch (SQLException e) {
      System.err.println(e);
    }
  }

  public static void showEntities() {
    String[] types = {"Proj", "Alg", "Test"};
    System.out.printf("%1$-6s %2$-16s %3$s", "ID", "Type", "Entity name");
    System.out.println("");
    System.out.printf("___________________________________________");
    System.out.println("");

    for (Entity project : project_list) {
      System.out.printf("%1$-6s %2$-16s %3$s", project.id, types[project.type - 1], project.name);
      System.out.println("");
      if (alg_map != null && alg_map.containsKey(Integer.toString(project.id))) {
        for (Entity algorithm : alg_map.get(Integer.toString(project.id))) {
          System.out.printf("%1$-6s %2$-20s %3$s", algorithm.id, types[algorithm.type - 1], algorithm.name);
          System.out.println("");
        }
      }
      if (test_map != null && test_map.containsKey(Integer.toString(project.id))) {
        for (Entity testset : test_map.get(Integer.toString(project.id))) {
          System.out.printf("%1$-6s %2$-20s %3$s", testset.id, types[testset.type - 1], testset.name);
          System.out.println("");
        }
      }
    }
  }

  private static void changeUserStatus(String username, String status) {
    String[] status_string = {"Activated", "Deactivated"};
    int status_number = Integer.parseInt(status);

    if (status_number < 0 || status_number > 1) {
      System.out.println("Status number must be 0 or 1!");
      return;
    }
    if (userExists(username, false) > 0) {
      try {
        Statement stmt = (Statement) conn.createStatement();
        String insert = "UPDATE algator.users SET status = " + status + " WHERE name = '" + username + "'";
        int result = stmt.executeUpdate(insert);

        if (result > 0) {
          System.out.println(">>> " + username + " status changed to: " + status_string[status_number]);
        } else {
          System.out.println(">>> Error while adding user to group!");
        }

      } catch (Exception e) {
      }
    }
  }

  private static void changeGroupStatus(String groupname, String status) {
    String[] status_string = {"Activated", "Deactivated"};
    int status_number = Integer.parseInt(status);

    if (status_number < 0 || status_number > 1) {
      System.out.println("Status number must be 0 or 1!");
      return;
    }
    if (groupExists(groupname, false) > 0) {
      try {
        Statement stmt = (Statement) conn.createStatement();
        String insert = "UPDATE algator.groups SET status = " + status + " WHERE name = '" + groupname + "'";
        int result = stmt.executeUpdate(insert);

        if (result > 0) {
          System.out.println(">>> " + groupname + " status changed to: " + status_string[status_number]);
        } else {
          System.out.println(">>> Error while adding user to group!");
        }

      } catch (Exception e) {
      }
    }
  }

  private static int userExists(String username, boolean output) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      String select = "SELECT * from algator.users WHERE name='" + username + "'";

      ResultSet rs = stmt.executeQuery(select);
      if (rs.next()) {
        int id_user = rs.getInt("id");
        if (output) {
          System.out.println(">>> User with this username already exists!");
        }
        return id_user;
      } else {
        return -1;
      }
    } catch (SQLException e) {
      System.err.println(e);
      return -1;
    }
  }

  private static int groupExists(String groupname, boolean output) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      String select = "SELECT * from algator.groups WHERE name='" + groupname + "'";
      ResultSet rs = stmt.executeQuery(select);

      if (rs.next()) {
        int id_group = rs.getInt("id");
        if (output) {
          System.out.println(">>> Group with this name already exists!");
        }
        return id_group;
      } else {
        return -1;
      }
    } catch (SQLException e) {
      System.err.println(e);
      return -1;
    }
  }

  private static int permExists(String permname, boolean output) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      String select = "SELECT * from algator.permissions WHERE permission_code='" + permname + "'";
      ResultSet rs = stmt.executeQuery(select);

      if (rs.next()) {
        int id_perm = rs.getInt("id");
        return id_perm;
      } else {
        if (output) {
          System.out.println(">>> Permission with this name does not exist!");
        }
        return -1;
      }
    } catch (SQLException e) {
      System.err.println(e);
      return -1;
    }
  }

  private static int projExists(String entityname, boolean output) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      String select = "SELECT * from algator.entities WHERE name='" + entityname + "' AND type=1";
      ResultSet rs = stmt.executeQuery(select);

      if (rs.next()) {
        int id_entity = rs.getInt("id");
        return id_entity;
      } else {
        if (output) {
          System.out.println(">>> Project with this name does not exist!");
        }
        return -1;
      }
    } catch (SQLException e) {
      System.err.println(e);
      return -1;
    }
  }

  private static ArrayList<Integer> projectAlgs(int id_project) {
    ArrayList<Integer> alg_ids = new ArrayList<Integer>();
    try {
      Statement stmt = (Statement) conn.createStatement();

      String select = "SELECT * from algator.entities WHERE id_parent=" + id_project + " AND type=2";
      ResultSet rs = stmt.executeQuery(select);
      while (rs.next()) {
        alg_ids.add(rs.getInt(1));
      }
      return alg_ids;
    } catch (SQLException e) {
      System.err.println(e);
      return alg_ids;
    }
  }

  public static void load_entites() {
    project_list = new ArrayList<Entity>();
    alg_list = new ArrayList<Entity>();
    test_list = new ArrayList<Entity>();
    alg_map = new HashMap<String, List<Entity>>();
    test_map = new HashMap<String, List<Entity>>();

    try {
      Statement stmt = (Statement) conn.createStatement();
      String select = "SELECT * from algator.entities";
      ResultSet rs = stmt.executeQuery(select);
      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int type = rs.getInt("type");
        int id_parent = rs.getInt("id_parent");
        List tmp_map = null;
        switch (type) {
          case 1:
            project_list.add(new Entity(id, name, type, id_parent));
            break;
          case 2:
            tmp_map = alg_map.get(Integer.toString(id_parent));
            if (tmp_map == null) {
              tmp_map = new ArrayList<Entity>();
            }
            tmp_map.add(new Entity(id, name, type, id_parent));
            alg_map.put(Integer.toString(id_parent), tmp_map);
            alg_list.add(new Entity(id, name, type, id_parent));
            break;
          case 3:
            tmp_map = test_map.get(Integer.toString(id_parent));
            if (tmp_map == null) {
              tmp_map = new ArrayList<Entity>();
            }
            tmp_map.add(new Entity(id, name, type, id_parent));
            test_map.put(Integer.toString(id_parent), tmp_map);
            test_list.add(new Entity(id, name, type, id_parent));
            break;
        }
      }

    } catch (SQLException e) {
      System.err.println(e);
    }
  }

  public static void init_perm(String username) {
    project_permissions = new ArrayList<Entity_permission>();
    algorithm_permissions = new ArrayList<Entity_permission>();
    test_permissions = new ArrayList<Entity_permission>();
    try {
      Statement stmt = (Statement) conn.createStatement();
      String selectProj = "SELECT * FROM (SELECT DISTINCT ent.id,ent.name, 1 as 'type', 'all' as 'permissions', -1 as 'parent_id','' as 'parent_name'\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.owners as own ON ent.id = own.id_entity\n"
              + "JOIN algator.users as usr ON own.id_owner=usr.id\n"
              + "WHERE usr.name=\"" + username + "\" AND ent.type=1) as a\n"
              + "UNION\n"
              + "(SELECT DISTINCT ent.id,ent.name, 1 as 'type', perm.permission_code as 'permissions', -1 as 'parent_id','' as 'parent_name'  \n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.permissions_users as pu ON ent.id=pu.id_entity\n"
              + "JOIN algator.users as usr ON usr.id=pu.id_user\n"
              + "JOIN algator.permissions as perm ON perm.id=pu.id_permission\n"
              + "WHERE ent.type=1 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT DISTINCT ent.id,ent.name, 1 as 'type', perm.permission_code as 'permissions', -1 as 'parent_id','' as 'parent_name'\n"
              + "FROM algator.users as usr\n"
              + "JOIN algator.group_users as gu ON usr.id=gu.id_user\n"
              + "JOIN algator.groups as grp ON gu.id_group=grp.id\n"
              + "JOIN algator.permissions_group as pg ON pg.id_group=grp.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pg.id_permission\n"
              + "JOIN algator.entities as ent ON ent.id=pg.id_entity\n"
              + "WHERE ent.type=1 AND usr.name=\"" + username + "\");";

      String selectAlg = "SELECT * FROM\n"
              + "(SELECT ent2.id,ent2.name, 2 as \"type\", \"all\" as \"permissions\" , ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.owners as own ON ent.id = own.id_entity\n"
              + "JOIN algator.users as usr ON own.id_owner=usr.id\n"
              + "JOIN algator.entities as ent2 ON ent2.id_parent=ent.id\n"
              + "WHERE usr.name=\"" + username + "\" AND ent.type=1 AND ent2.type=2) as e\n"
              + "UNION\n"
              + "(SELECT ent.id,ent.name, 2 as \"type\", \"all\" as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.owners as own ON ent.id = own.id_entity\n"
              + "JOIN algator.users as usr ON own.id_owner=usr.id\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=2 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT ent2.id,ent2.name, 2 as \"type\", perm.permission_code as \"permissions\", ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.permissions_users as pu ON ent.id = pu.id_entity\n"
              + "JOIN algator.users as usr ON pu.id_user=usr.id\n"
              + "JOIN algator.entities as ent2 ON ent2.id_parent=ent.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pu.id_permission\n"
              + "WHERE usr.name=\"" + username + "\" AND ent.type=1 AND ent2.type=2)\n"
              + "UNION\n"
              + "(SELECT DISTINCT ent.id,ent.name, 2 as \"type\", perm.permission_code as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.permissions_users as pu ON ent.id=pu.id_entity\n"
              + "JOIN algator.users as usr ON usr.id=pu.id_user\n"
              + "JOIN algator.permissions as perm ON perm.id=pu.id_permission\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=2 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT distinct ent2.id,ent2.name, 2 as \"type\", perm.permission_code as \"permissions\", ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.users as usr\n"
              + "JOIN algator.group_users as gu ON usr.id=gu.id_user\n"
              + "JOIN algator.groups as grp ON gu.id_group=grp.id\n"
              + "JOIN algator.permissions_group as pg ON pg.id_group=grp.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pg.id_permission\n"
              + "JOIN algator.entities as ent ON ent.id=pg.id_entity\n"
              + "JOIN algator.entities as ent2 ON ent.id=ent2.id_parent\n"
              + "WHERE ent.type=1 AND ent2.type=2 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT distinct ent.id,ent.name, 2 as \"type\", perm.permission_code as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"\n"
              + "FROM algator.users as usr\n"
              + "JOIN algator.group_users as gu ON usr.id=gu.id_user\n"
              + "JOIN algator.groups as grp ON gu.id_group=grp.id\n"
              + "JOIN algator.permissions_group as pg ON pg.id_group=grp.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pg.id_permission\n"
              + "JOIN algator.entities as ent ON ent.id=pg.id_entity\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=2 AND usr.name=\"" + username + "\");";

      String selectTest = "SELECT * FROM\n"
              + "(SELECT ent2.id,ent2.name, 3 as \"type\", \"all\" as \"permissions\", ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.owners as own ON ent.id = own.id_entity\n"
              + "JOIN algator.users as usr ON own.id_owner=usr.id\n"
              + "JOIN algator.entities as ent2 ON ent2.id_parent=ent.id\n"
              + "WHERE usr.name=\"" + username + "\" AND ent.type=1 AND ent2.type=3) as e\n"
              + "UNION\n"
              + "(SELECT ent.id,ent.name, 3 as \"type\", \"all\" as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.owners as own ON ent.id = own.id_entity\n"
              + "JOIN algator.users as usr ON own.id_owner=usr.id\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=3 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT ent2.id,ent2.name, 3 as \"type\", perm.permission_code as \"permissions\", ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.permissions_users as pu ON ent.id = pu.id_entity\n"
              + "JOIN algator.users as usr ON pu.id_user=usr.id\n"
              + "JOIN algator.entities as ent2 ON ent2.id_parent=ent.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pu.id_permission\n"
              + "WHERE usr.name=\"" + username + "\" AND ent.type=1 AND ent2.type=3)\n"
              + "UNION\n"
              + "(SELECT DISTINCT ent.id,ent.name, 2 as \"type\", perm.permission_code as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"  \n"
              + "FROM algator.entities as ent\n"
              + "JOIN algator.permissions_users as pu ON ent.id=pu.id_entity\n"
              + "JOIN algator.users as usr ON usr.id=pu.id_user\n"
              + "JOIN algator.permissions as perm ON perm.id=pu.id_permission\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=3 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT distinct ent2.id,ent2.name, 3 as \"type\", perm.permission_code as \"permissions\", ent.id as \"parent_id\", ent.name as \"parent_name\"\n"
              + "FROM algator.users as usr\n"
              + "JOIN algator.group_users as gu ON usr.id=gu.id_user\n"
              + "JOIN algator.groups as grp ON gu.id_group=grp.id\n"
              + "JOIN algator.permissions_group as pg ON pg.id_group=grp.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pg.id_permission\n"
              + "JOIN algator.entities as ent ON ent.id=pg.id_entity\n"
              + "JOIN algator.entities as ent2 ON ent.id=ent2.id_parent\n"
              + "WHERE ent.type=1 AND ent2.type=3 AND usr.name=\"" + username + "\")\n"
              + "UNION\n"
              + "(SELECT distinct ent.id,ent.name, 3 as \"type\", perm.permission_code as \"permissions\", pent.id as \"parent_id\", pent.name as \"parent_name\"\n"
              + "FROM algator.users as usr\n"
              + "JOIN algator.group_users as gu ON usr.id=gu.id_user\n"
              + "JOIN algator.groups as grp ON gu.id_group=grp.id\n"
              + "JOIN algator.permissions_group as pg ON pg.id_group=grp.id\n"
              + "JOIN algator.permissions as perm ON perm.id=pg.id_permission\n"
              + "JOIN algator.entities as ent ON ent.id=pg.id_entity\n"
              + "JOIN algator.entities as pent ON ent.id_parent=pent.id\n"
              + "WHERE ent.type=3 AND usr.name=\"" + username + "\");";

      ResultSet rs_proj = stmt.executeQuery(selectProj);
      while (rs_proj.next()) {
        project_permissions.add(new Entity_permission(rs_proj.getInt("id"), rs_proj.getString("name"), rs_proj.getInt("type"), rs_proj.getString("permissions"), rs_proj.getInt("parent_id"), rs_proj.getString("parent_name")));
      }

      ResultSet rs_alg = stmt.executeQuery(selectAlg);
      while (rs_alg.next()) {
        algorithm_permissions.add(new Entity_permission(rs_alg.getInt("id"), rs_alg.getString("name"), rs_alg.getInt("type"), rs_alg.getString("permissions"), rs_alg.getInt("parent_id"), rs_alg.getString("parent_name")));
      }

      ResultSet rs_test = stmt.executeQuery(selectTest);
      while (rs_test.next()) {
        test_permissions.add(new Entity_permission(rs_test.getInt("id"), rs_test.getString("name"), rs_test.getInt("type"), rs_test.getString("permissions"), rs_test.getInt("parent_id"), rs_test.getString("parent_name")));
      }

    } catch (SQLException e) {
      System.out.println(e.toString());
    }
  }

  public static void print_permissions(String username) {
    init_perm(username);
    if (format.equals("json")) {
      Gson gson = new Gson();
      List<Entity_permission> combined = new ArrayList<Entity_permission>();
      combined.addAll(project_permissions);
      combined.addAll(algorithm_permissions);
      combined.addAll(test_permissions);
      System.out.println(gson.toJson(combined));
      return;
    }
    System.out.println("");
    String[] types = {"Proj", "Alg", "Test"};
    System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", "ID", "Type", "Entity name", "Permission", "P ID", "P name");
    System.out.println("");
    System.out.printf("___________________________________________________________________________");
    System.out.println("");
    for (Entity_permission project : project_permissions) {
      System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", project.id, types[project.type - 1], project.name, project.permission, project.parent_id, project.parent_name);
      System.out.println("");
    }
    for (Entity_permission algorithm : algorithm_permissions) {
      System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", algorithm.id, types[algorithm.type - 1], algorithm.name, algorithm.permission, algorithm.parent_id, algorithm.parent_name);
      System.out.println("");
    }
    for (Entity_permission test : test_permissions) {
      System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", test.id, types[test.type - 1], test.name, test.permission, test.parent_id, test.parent_name);
      System.out.println("");
    }

  }

  public static void user_permission(String username, int entity_id) {
    String[] types = {"Proj", "Alg", "Test"};
    init_perm(username);
    if (format.equals("json")) {
      Gson gson = new Gson();
      List<Entity_permission> perms = new ArrayList<Entity_permission>();
      for (Entity_permission project : project_permissions) {
        if (project.id == entity_id) {
          perms.add(project);
        }
      }
      for (Entity_permission algorithm : algorithm_permissions) {
        if (algorithm.id == entity_id) {
          perms.add(algorithm);
        }
      }
      for (Entity_permission test : test_permissions) {
        if (test.id == entity_id) {
          perms.add(test);
        }
      }
      System.out.println(gson.toJson(perms));
      return;
    }
    System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", "ID", "Type", "Entity name", "Permission", "P ID", "P name");
    System.out.println("");
    System.out.printf("___________________________________________________________________________");
    System.out.println("");
    for (Entity_permission project : project_permissions) {
      if (project.id == entity_id) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", project.id, types[project.type - 1], project.name, project.permission, project.parent_id, project.parent_name);
        System.out.println("");
      }
    }
    for (Entity_permission algorithm : algorithm_permissions) {
      if (algorithm.id == entity_id) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", algorithm.id, types[algorithm.type - 1], algorithm.name, algorithm.permission, algorithm.parent_id, algorithm.parent_name);
        System.out.println("");
      }
    }
    for (Entity_permission test : test_permissions) {
      if (test.id == entity_id) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", test.id, types[test.type - 1], test.name, test.permission, test.parent_id, test.parent_name);
        System.out.println("");
      }
    }
  }

  public static void user_permission2(String username, String entity) {
    String[] types = {"Proj", "Alg", "Test"};
    init_perm(username);
    String proj = entity;
    String alg = "";
    String test = "";

    if (entity.contains(":::")) {
      proj = entity.split(":::")[0];
      alg = entity.split(":::")[1];
    } else if (entity.contains(";;;")) {
      proj = entity.split(";;;")[0];
      test = entity.split(";;;")[1];
    }

    if (format.equals("json")) {
      Gson gson = new Gson();
      List<Entity_permission> perms = new ArrayList<Entity_permission>();
      for (Entity_permission project : project_permissions) {
        if (project.name == proj) {
          perms.add(project);
        }
      }
      for (Entity_permission algorithm : algorithm_permissions) {
        if (algorithm.name == alg) {
          perms.add(algorithm);
        }
      }
      for (Entity_permission test_set : test_permissions) {
        if (test_set.name == test) {
          perms.add(test_set);
        }
      }
      System.out.println(gson.toJson(perms));
      return;
    }
    System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", "ID", "Type", "Entity name", "Permission", "P ID", "P name");
    System.out.println("");
    System.out.printf("___________________________________________________________________________");
    System.out.println("");
    for (Entity_permission project : project_permissions) {
      if (project.name == proj) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", project.id, types[project.type - 1], project.name, project.permission, project.parent_id, project.parent_name);
        System.out.println("");
      }
    }
    for (Entity_permission algorithm : algorithm_permissions) {
      if (algorithm.name == alg) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", algorithm.id, types[algorithm.type - 1], algorithm.name, algorithm.permission, algorithm.parent_id, algorithm.parent_name);
        System.out.println("");
      }
    }
    for (Entity_permission test_set : test_permissions) {
      if (test_set.name == test) {
        System.out.printf("%1$-6s %2$-6s %3$-26s %4$-14s %5$-10s %6$-10s", test_set.id, types[test_set.type - 1], test_set.name, test_set.permission, test_set.parent_id, test_set.parent_name);
        System.out.println("");
      }
    }
  }

  public static boolean can_user(String username, String permission, int entity_id) {
    init_perm(username);
    for (Entity_permission project : project_permissions) {
      if (project.id == entity_id) {
        if (project.permission.equals("all") || project.permission.equals(permission)) {
          return true;
        }
      }
    }
    for (Entity_permission algorithm : algorithm_permissions) {
      if (algorithm.id == entity_id) {
        if (algorithm.permission.equals("all") || algorithm.permission.equals(permission)) {
          return true;
        }
      }
    }
    for (Entity_permission test : test_permissions) {
      if (test.id == entity_id) {
        if (test.permission.equals("all") || test.permission.equals(permission)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean can_user2(String username, String permission, String entity) {
    init_perm(username);
    String proj = entity;
    String alg = "";
    String test = "";
    if (entity.contains(":::")) {
      proj = entity.split(":::")[0];
      alg = entity.split(":::")[1];
      for (Entity_permission algorithm : algorithm_permissions) {
        if (algorithm.parent_name.equals(proj) && algorithm.name.equals(alg)) {
          if (algorithm.permission.equals("all") || algorithm.permission.equals(permission)) {
            return true;
          }
        }
      }
    } else if (entity.contains(";;;")) {
      proj = entity.split(";;;")[0];
      test = entity.split(";;;")[1];

      for (Entity_permission testset : test_permissions) {
        if (testset.parent_name.equals(proj) && testset.name.equals(test)) {
          if (testset.permission.equals("all") || testset.permission.equals(permission)) {
            return true;
          }
        }
      }
    } else {
      for (Entity_permission project : project_permissions) {
        if (project.name.equals(proj)) {
          if (project.permission.equals("all") || project.permission.equals(permission)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean showOwnerProj(String projectName) {
    try {
      Statement stmt = (Statement) conn.createStatement();

      int projExists = projExists(projectName, true);

      if (projExists < 0) {
        return false;
      }

      //get owner
      String select = "SELECT t1.id AS id_entity,"
              + "t1.name AS entity_name,"
              + "t1.type,"
              + "t1.id_parent,"
              + "t3.name AS username "
              + "FROM algator.entities AS t1 "
              + "JOIN algator.owners AS t2 on t1.id=t2.id_entity "
              + "JOIN algator.users AS t3 on t2.id_owner=t3.id "
              + "WHERE t1.name='" + projectName + "'";

      ResultSet rs = stmt.executeQuery(select);
      if (rs.next()) {
        String username = rs.getString("username");
        if (format.equals("json")) {
          Gson gson = new Gson();
          System.out.println(gson.toJson(username));
        } else {
          System.out.println(">>> Owner for " + projectName + " is user " + username + ".");
        }
        return true;

      } else {
        if (format.equals("json")) {
          Gson gson = new Gson();
          System.out.println(gson.toJson("No owner"));
        } else {
          System.out.println(">>> No owner data found for this project!");
        }
        return false;
      }
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean showOwnerAlg(String projectName, String algorithmName) {
    load_entites();
    try {
      int id = -1;
      int proj_id = -1;
      Statement stmt = (Statement) conn.createStatement();

      for (Entity proj : project_list) {
        if (proj.name.equals(projectName)) {
          proj_id = proj.id;
        }
      }
      if (proj_id < 0) {
        if (format.equals("json")) {
          Gson gson = new Gson();
          System.out.println(gson.toJson("No project"));
        } else {
          System.out.println(">>> Project with this name does not exist!");
        }
        return false;
      }

      List<Entity> alg_list = alg_map.get(Integer.toString(proj_id));

      for (Entity alg : alg_list) {
        if (alg.name.equals(algorithmName)) {

          String select = "SELECT t1.id AS id_entity,"
                  + "t1.name AS entity_name,"
                  + "t1.type,"
                  + "t1.id_parent,"
                  + "t3.name AS username "
                  + "FROM algator.entities AS t1 "
                  + "JOIN algator.owners AS t2 on t1.id=t2.id_entity "
                  + "JOIN algator.users AS t3 on t2.id_owner=t3.id "
                  + "WHERE t1.name='" + alg.name + "'"
                  + " AND t1.id_parent=" + proj_id + "";

          ResultSet rs = stmt.executeQuery(select);
          if (rs.next()) {
            String username = rs.getString("username");
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson(username));
            } else {
              System.out.println(">>> Owner for " + algorithmName + " is user " + username + ".");
            }
            return true;

          } else {
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson("No owner"));
            } else {
              System.out.println(">>> No owner data found for this algorithm!");
            }
            return false;
          }
        }
        if (format.equals("json")) {
          Gson gson = new Gson();
          System.out.println(gson.toJson("Does not exist"));
        } else {
          System.out.println(">>> Algorithm " + algorithmName + " in project " + projectName + " does not exist!");
        }
        return false;
      }
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static boolean showOwnerTest(String projectName, String testName) {
    //za popravit
    load_entites();
    try {
      int id = -1;
      int proj_id = -1;
      int alg_id = -1;

      Statement stmt = (Statement) conn.createStatement();

      for (Entity proj : project_list) {
        if (proj.name.equals(projectName)) {
          proj_id = proj.id;
        }
      }
      if (proj_id < 0) {
        if (format.equals("json")) {
          Gson gson = new Gson();
          System.out.println(gson.toJson("Does not exist"));
        } else {
          System.out.println(">>> Project with this name does not exist!");
        }

        return false;
      }

      List<Entity> test_list = test_map.get(Integer.toString(proj_id));
      for (Entity test : test_list) {
        if (test.name.equals(testName)) {
          String select = "SELECT t1.id AS id_entity,"
                  + "t1.name AS entity_name,"
                  + "t1.type,"
                  + "t1.id_parent,"
                  + "t3.name AS username "
                  + "FROM algator.entities AS t1 "
                  + "JOIN algator.owners AS t2 on t1.id=t2.id_entity "
                  + "JOIN algator.users AS t3 on t2.id_owner=t3.id "
                  + "WHERE t1.name='" + test.name + "'"
                  + " AND t1.id_parent=" + proj_id + "";

          ResultSet rs = stmt.executeQuery(select);
          if (rs.next()) {
            String username = rs.getString("username");
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson(username));
            } else {
              System.out.println(">>> Owner for " + testName + " is user " + username + ".");
            }
            return true;

          } else {
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson("No owner"));
            } else {
              System.out.println(">>> No owner data found for this testSet!");
            }
            return false;
          }
        } else {
          if (format.equals("json")) {
            Gson gson = new Gson();
            System.out.println(gson.toJson("Does not exist"));
          } else {
            System.out.println(">>> Project " + projectName + " does not have any TestSets!");
          }
          return false;
        }
      }
      return false;
    } catch (SQLException e) {
      System.err.println(e);
      return false;
    }
  }

  public static void setowner() {
    load_entites();
    boolean exists;
    String dataroot = ATGlobal.getALGatorDataRoot();
    String projRoot = ATGlobal.getPROJECTSfolder(dataroot);
    File file = new File(projRoot);
    int id_project;
    int proj_c = 0, alg_c = 0, test_c = 0, proj_x = 0, alg_x = 0, test_x = 0;
    String algName, testName;
    String[] projects = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });
    for (String proj : projects) {
      exists = false;
      id_project = -1;
      String projName = proj.split("-")[1];
      for (Entity pl : project_list) {
        if (pl.name.equals(projName)) {
          exists = true;
        }
      }
      if (!exists) {
        try {
          Statement stmt = (Statement) conn.createStatement();
          String proj_insert = "INSERT INTO algator.entities(name,type) VALUES ('" + projName + "',1)";
          int result = stmt.executeUpdate(proj_insert, stmt.RETURN_GENERATED_KEYS);

          if (result > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
              id_project = rs.getInt(1);
            }
          }
          proj_c++;
          System.out.println(">>> Project " + projName + " inserted to database");
        } catch (Exception e) {
          proj_x++;
          System.out.println(">>> Could not insert project " + projName);
        }
      } else {
        try {
          Statement stmt = (Statement) conn.createStatement();
          String proj_get = "SELECT id FROM algator.entities WHERE name='" + projName + "' and type=1";
          ResultSet result = stmt.executeQuery(proj_get);
          while (result.next()) {
            id_project = Integer.parseInt(result.getString("id"));
          }
        } catch (Exception e) {
          System.out.println(">>> Cannot select project ID of " + projName);
        }
      }
      exists = false;
      //ALG
      String algRoot = ATGlobal.getALGORITHMpath(dataroot, projName);
      file = new File(algRoot);
      String[] algorithms = file.list(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
          return new File(current, name).isDirectory();
        }
      });
      if (algorithms != null) {
        for (String alg : algorithms) {
          algName = alg.split("-")[1];
          for (Entity al : alg_list) {
            if (al.name.equals(algName) && al.id_parent == id_project) {
              exists = true;
            }
          }
          if (!exists) {
            try {
              Statement stmt = (Statement) conn.createStatement();
              String alg_insert = "INSERT INTO algator.entities(name,type,id_parent) VALUES ('" + algName + "',2," + id_project + ")";
              int result = stmt.executeUpdate(alg_insert, stmt.RETURN_GENERATED_KEYS);

              alg_c++;
              System.out.println(">>> Algorithm " + algName + " for project " + projName + " inserted to database");
            } catch (Exception e) {
              alg_x++;
              System.out.println(">>> Could not insert algorithm " + algName + " for project " + projName);
            }
          }
          exists = false;
        }
      }
      //TESTSET

      String testRoot = ATGlobal.getTESTSETpath(dataroot, projName);
      file = new File(testRoot);
      File[] testsets = file.listFiles();
      if (testsets != null) {
        for (File test : testsets) {
          if (test.isFile() && test.getName().contains(".atts")) {
            testName = test.getName().replace(".atts", "");
            for (Entity tst : test_list) {
              if (tst.name.equals(testName) && tst.id_parent == id_project) {
                exists = true;
              }
            }
            if (!exists) {
              try {
                Statement stmt = (Statement) conn.createStatement();
                String test_insert = "INSERT INTO algator.entities(name,type,id_parent) VALUES ('" + testName + "',3," + id_project + ")";
                int result = stmt.executeUpdate(test_insert, stmt.RETURN_GENERATED_KEYS);
                test_c++;
                System.out.println(">>> Testset " + testName + " for project " + projName + " inserted to database");
              } catch (Exception e) {
                test_x++;
                System.out.println(">>> Could not insert testset " + testName + " for project " + projName);
              }
            }
            exists = false;
          }
        }
      }
    }
    System.out.println(">>> #################################");
    System.out.println(">>> ########## REPORT ###############");
    System.out.println(">>> #################################");
    System.out.println(">>>");
    System.out.println(">>> ADDED TO DATABASE:");
    System.out.println(">>>     PROJECTS: " + proj_c);
    System.out.println(">>>     ALGORITHMS: " + alg_c);
    System.out.println(">>>     TESTSETS: " + test_c);
    System.out.println(">>>");
    System.out.println(">>> ERROR WHILE ADDING:");
    System.out.println(">>>     PROJECTS: " + proj_x);
    System.out.println(">>>     ALGORITHMS: " + alg_x);
    System.out.println(">>>     TESTSETS: " + test_x);
    insert_owner_table();
  }

  public static void load_owner_table() {
    try {
      Statement stmt = (Statement) conn.createStatement();
      String select = "SELECT * FROM algator.owners";
      ResultSet rs = stmt.executeQuery(select);
      owner_list = new ArrayList<Owner>();
      while (rs.next()) {
        int id = rs.getInt("id");
        int id_owner = rs.getInt("id_owner");
        int id_entity = rs.getInt("id_entity");
        owner_list.add(new Owner(id, id_owner, id_entity));
      }
    } catch (Exception e) {
      System.out.println(">>> Error loading owners");
    }
  }

  public static void set_owner_table(int id_owner, int id_entity) {
    try {
      Statement stmt = (Statement) conn.createStatement();
      String select = "SELECT * FROM algator.owners where id_entity=" + id_entity;
      ResultSet rs = stmt.executeQuery(select);
      if (!rs.next()) {
        String insert = "INSERT INTO algator.owners(id_owner,id_entity) VALUES (" + id_owner + "," + id_entity + ")";
        stmt.executeUpdate(insert);
      }
    } catch (Exception e) {
      System.out.println(e);
      System.out.println(">>> Error loading owners");
    }
  }

  public static void insert_owner_table() {
    load_owner_table();
    int count = 0;

    boolean inside;
    for (Entity proj : project_list) {
      inside = false;
      for (Owner own : owner_list) {
        if (proj.id == own.id_entity) {
          inside = true;
        }
      }
      if (!inside) {
        count++;
        set_owner_table(1, proj.id);
      }
    }
    for (Entity alg : alg_list) {
      inside = false;
      for (Owner own : owner_list) {
        if (alg.id == own.id_entity) {
          inside = true;
        }
      }
      if (!inside) {
        count++;
        set_owner_table(1, alg.id);
      }
    }
    for (Entity test : test_list) {
      inside = false;
      for (Owner own : owner_list) {
        if (test.id == own.id_entity) {
          inside = true;
        }
      }
      if (!inside) {
        count++;
        set_owner_table(1, test.id);
      }
    }
    System.out.println("ADDED OWNERS: " + count);
  }

  public static boolean main_switch(String[] sinput) {
    boolean result = true;
    
    switch (sinput[0]) {
      case "exit":
        break;
      case "help":
        help();
        break;
      case "init":
        DatabaseInit.init();
        break;
      case "adduser":
        try {
          adduser(sinput[1], sinput[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "addgroup":
        try {
          addgroup(sinput[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "moduser":
        try {
          moduser(sinput[1], sinput[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "showusers":
        if (sinput.length > 1) {
          showUsers(sinput[1]);
        } else {
          showUsers("");
        }
        break;
      case "showgroups":
        showGroups();
        break;
      case "showpermissions":
        showPermissions();
        break;
      case "showentities":
        if (format.equals("json")) {
          Gson gson = new Gson();
          List<Entity> combined = new ArrayList<Entity>();
          combined.addAll(project_list);
          combined.addAll(alg_list);
          combined.addAll(test_list);
          System.out.println(gson.toJson(combined));
        } else {
          showEntities();
        }
        break;
      case "changeuserstatus":
        try {
          changeUserStatus(sinput[1], sinput[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "changegroupstatus":
        try {
          changeGroupStatus(sinput[1], sinput[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "addpermproj":
        try {
          if (sinput[1].equals("-g")) {
            addPermProj(sinput[2], sinput[3], sinput[4], "group");
          } else {
            addPermProj(sinput[1], sinput[2], sinput[3], "user");
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "rmvpermproj":
        try {
          if (sinput[1].equals("-g")) {
            rmvPermProj(sinput[2], sinput[3], sinput[4], "group");
          } else {
            rmvPermProj(sinput[1], sinput[2], sinput[3], "user");
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "addpermalg":
        try {
          if (sinput[1].equals("-g")) {
            addPermAlg(sinput[2], sinput[3], sinput[4], sinput[5], "group");
          } else {
            addPermAlg(sinput[1], sinput[2], sinput[3], sinput[4], "user");
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "userperms":
        try {
          print_permissions(sinput[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "userperm":
        try {
          if (sinput[1].equals("-id")) {
            user_permission(sinput[2], Integer.parseInt(sinput[3]));
          } else {
            user_permission2(sinput[1], sinput[2]);
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "canuser":
        try {
          if (sinput[1].equals("-id")) {
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson(can_user(sinput[2], sinput[3], Integer.parseInt(sinput[4]))));
              return true;
            }
            System.out.println(can_user(sinput[2], sinput[3], Integer.parseInt(sinput[4])));
          } else {
            if (format.equals("json")) {
              Gson gson = new Gson();
              System.out.println(gson.toJson(can_user2(sinput[1], sinput[2], sinput[3])));
              return true;
            }
            System.out.println(can_user2(sinput[1], sinput[2], sinput[3]));
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "setowner":
        try {
          setowner();
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(e.toString());
          System.out.println(">>> Error: Missing arguments");
        }
        break;
      case "showowner":
        try {
          switch (sinput[1]) {
            case "-p":
              showOwnerProj(sinput[2]);
              break;
            case "-a":
              showOwnerAlg(sinput[2], sinput[3]);
              break;
            case "-t":
              showOwnerTest(sinput[2], sinput[3]);
              break;
            default:
              help();
              break;
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println(">>> Error: Missing arguments");
        }
        break;
        
      default:
        result = false;
        break;
    }
    return result;
  }

  private static void doConsoleInput() {
    while (true) {
      Scanner reader = new Scanner(System.in);
      System.out.print(">>> ");
      String input = reader.nextLine();

      if (input.equals("")) {
        continue;
      }
      String[] sinput = input.split(" ");

      main_switch(sinput);

      if (input.equals("exit")) {
        break;
      }
    }
  }
  
  private static void doConsoleInputWithJLIne() {
    try {
      ConsoleReader reader = new ConsoleReader();

      reader.setPrompt(">>> ");

      List<Completer> completors = new LinkedList<Completer>();
      completors.add(new StringsCompleter("cls", "quit", "exit", 
              "help","init","adduser","addgroup","moduser","showusers",
              "showgroups","showpermissions","showentities","changeuserstatus",
              "changegroupstatus","addpermproj","rmvpermproj","addpermalg",
              "userperms","userperm","canuser","setowner","showowner",
              "help init", "help canuser"
      ));

      for (Completer c : completors) {
        reader.addCompleter(c);
      }

      String line;
      PrintWriter out = new PrintWriter(reader.getOutput());

      while ((line = reader.readLine()) != null) {
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
          System.exit(0);
        }
        if (line.equalsIgnoreCase("cls")) {
          reader.clearScreen();
          continue;
        }
        
        String[] sinput = line.split(" ");
        boolean result = main_switch(sinput);

        if (!result) {
          out.println("Unknown command \"" + line + "\"");        
          out.flush();
        }
        
      }

    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
    }
    
  }

  public static void main(String[] args) {
    ATGlobal.logTarget = ATLog.LOG_TARGET_STDOUT;
    ATLog.setLogTarget(ATGlobal.logTarget);

    conn = PermTools.connectToDatabase();
    if (conn == null) {
      return;
    }

    load_entites();

    // execute action defined with arguments or ...
    if (args.length > 0) {
      format = "json";
      main_switch(args);
      return;
    }

    // ... open a console
    format = "string";
    System.out.println(
            "    ___     __    ______        __              \n"
            + "   /   |   / /   / ____/____ _ / /_ ____   _____\n"
            + "  / /| |  / /   / / __ / __ `// __// __ \\ / ___/\n"
            + " / ___ | / /___/ /_/ // /_/ // /_ / /_/ // /    \n"
            + "/_/  |_|/_____/\\____/ \\__,_/ \\__/ \\____//_/     \n"
            + "                                                ");
    System.out.println(">>> Welcome to Algator admin panel!");
    doConsoleInputWithJLIne();
    System.out.println(">>> Goodbye!");
  }
}
