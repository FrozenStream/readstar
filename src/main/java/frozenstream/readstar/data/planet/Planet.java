package frozenstream.readstar.data.planet;

import frozenstream.readstar.util;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Planet {
    public String name;
    public double mass;
    public double radius;
    public Vector3f position;
    public Vector3f axis;
    public Oribit oribit;
    public Planet parent;
    public ArrayList<Planet> children;
    public int level = -1;
    public Planet mySun;

    Vector3f Vec_current;
    Vector3f Vec_noon;

    public static Planet createRoot(){
        return new Planet(
                "VOID",
                0,
                0,
                null,
                null);
    }


    public Planet(String name, double mass, double radius, Vector3f axis, Oribit oribit) {
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        this.position = new Vector3f();
        this.axis = axis;
        this.oribit = oribit;
        this.parent = null;
        this.children = new ArrayList<>();
    }


    public Planet(String name, Planet parent){
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

}
