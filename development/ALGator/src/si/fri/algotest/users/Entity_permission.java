package si.fri.algotest.users;

/**
 *
 * @author Gregor
 */
public class Entity_permission {
    public int id;
    public String name;
    public int type;
    public String permission;
    public int parent_id;
    public String parent_name;

    public Entity_permission(int id, String name, int type, String permission, int parent_id, String parent_name) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.permission = permission;
        this.parent_id = parent_id;
        this.parent_name = parent_name;
    }
}
