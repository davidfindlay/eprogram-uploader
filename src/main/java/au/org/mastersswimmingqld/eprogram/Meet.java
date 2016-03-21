package au.org.mastersswimmingqld.eprogram;

/**
 * Created by david on 18/03/2016.
 */
public class Meet {
    private int id;
    private String name;

    public Meet() {
    }

    public Meet(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Meet(String id, String name) {
        this(new Integer(id), name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
