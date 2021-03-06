package org.apache.cayenne.testdo.embeddable.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.testdo.embeddable.Embeddable1;

/**
 * Class _EmbedEntity1 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _EmbedEntity1 extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "ID";

    public static final Property<Embeddable1> EMBEDDED1 = Property.create("embedded1", Embeddable1.class);
    public static final Property<Embeddable1> EMBEDDED2 = Property.create("embedded2", Embeddable1.class);
    public static final Property<String> NAME = Property.create("name", String.class);

    protected Embeddable1 embedded1;
    protected Embeddable1 embedded2;
    protected String name;


    public void setEmbedded1(Embeddable1 embedded1) {
        beforePropertyWrite("embedded1", this.embedded1, embedded1);
        this.embedded1 = embedded1;
    }

    public Embeddable1 getEmbedded1() {
        beforePropertyRead("embedded1");
        return this.embedded1;
    }

    public void setEmbedded2(Embeddable1 embedded2) {
        beforePropertyWrite("embedded2", this.embedded2, embedded2);
        this.embedded2 = embedded2;
    }

    public Embeddable1 getEmbedded2() {
        beforePropertyRead("embedded2");
        return this.embedded2;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "embedded1":
                return this.embedded1;
            case "embedded2":
                return this.embedded2;
            case "name":
                return this.name;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "embedded1":
                this.embedded1 = (Embeddable1)val;
                break;
            case "embedded2":
                this.embedded2 = (Embeddable1)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.embedded1);
        out.writeObject(this.embedded2);
        out.writeObject(this.name);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.embedded1 = (Embeddable1)in.readObject();
        this.embedded2 = (Embeddable1)in.readObject();
        this.name = (String)in.readObject();
    }

}
