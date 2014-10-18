package fi.atteheino.mediashuffler.app;

/**
 * Created by Atte on 18.10.2014.
 */
public class Folder {
    private String id;
    private String name;

    public Folder(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
