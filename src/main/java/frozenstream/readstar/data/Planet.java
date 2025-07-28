package frozenstream.readstar.data;

import frozenstream.readstar.Constants;
import org.joml.Vector3f;

public class Planet {
    public String name;
    public String description;
    float mass;
    Vector3f position;
    boolean pos_updated;
    Vector3f axis;
    Oribit oribit;
    Planet parent;

    Vector3f current_sky_vec;
    Vector3f noon_sky_vec;

    public static final Planet VOID = new Planet(
            "VOID",
            null,
            0,
            null,
            null,
            null);


    public Planet(){
        this.name = "VOID";
        this.description = null;
        this.mass = 0;
        this.position = new Vector3f();
        this.pos_updated = false;
        this.axis = null;
        this.oribit = null;
        this.parent = VOID;
    }

    public Planet(String name, String description, float mass, Vector3f axis, Oribit oribit, Planet parent) {
        this.name = name;
        this.description = description;
        this.mass = mass;
        this.position = new Vector3f();
        this.pos_updated = false;
        this.axis = axis;
        this.oribit = oribit;
        this.parent = parent;

        if (this.axis != null) {
            if (this.axis.lengthSquared() < 0.0001f) this.axis = new Vector3f(0, 1, 0);
            else this.axis.normalize();
        }
    }

    public void copy(Planet planet){
        this.name = planet.name;
        this.description = planet.description;
        this.mass = planet.mass;
        this.position = planet.position;
        this.pos_updated = planet.pos_updated;
        this.axis = planet.axis;
        this.oribit = planet.oribit;
        this.parent = planet.parent;
    }

    public void updateNoonSkyVec(){
        noon_sky_vec = new Vector3f();
        Vector3f parent_vec = new Vector3f();
        Vector3f tmp = new Vector3f();
        parent.position.sub(position, parent_vec);
        float n = axis.dot(parent_vec);
        parent_vec.sub(axis.mul(n, tmp), noon_sky_vec);
        if(noon_sky_vec.lengthSquared() < 0.0001) noon_sky_vec = new Vector3f(1,0,0);
        else noon_sky_vec.normalize();
    }

    public Vector3f updateCurrentSkyVec(long tick){
        current_sky_vec = new Vector3f();
        float theta = (tick - 6000) * PlanetManager.PI / 12000;
        Constants.LOG.info("theta: {}", theta);
        noon_sky_vec.rotateAxis(theta, axis.x, axis.y, axis.z, current_sky_vec);
        return new Vector3f(current_sky_vec);
    }
}
