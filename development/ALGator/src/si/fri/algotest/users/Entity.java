package si.fri.algotest.users;

/**
 *
 * @author Gregor
 */
public class Entity {
    public int id;
    public String name;
    public int type;
    public int id_parent;

    public Entity(int id, String name, int type, int id_parent) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.id_parent = id_parent;
    }
}
