package si.fri.algotest.users;

/**
 *
 * @author Gregor
 */
public class Owner {
    public int id;
    public int id_owner;
    public int id_entity;

    public Owner(int id, int id_owner, int id_entity) {
        this.id = id;
        this.id_owner = id_owner;
        this.id_entity = id_entity;
    }
}
