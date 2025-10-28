package frozenstream.readstar.data.planet;

import net.minecraft.resources.ResourceLocation;
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

    public Vector3f Vec_current;
    public Vector3f Vec_noon;

    private ResourceLocation texture;
    private ResourceLocation icon;

    public static Planet createRoot() {
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


    public Planet(String name, Planet parent) {
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Vector3f getAxis() {
        if (axis == null) axis = new Vector3f(0, 1, 0);
        if (axis.lengthSquared() == 0f) axis.set(0, 1, 0);
        return axis;
    }

    public ResourceLocation getTexture() {
        if (texture == null) {
            if ("sun".equals(name)) {
                texture = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
            } else if ("moon".equals(name)) {
                texture = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
            } else {
                String path = "textures/environment/" + name + ".png";
                texture = ResourceLocation.fromNamespaceAndPath("readstar", path);
            }
        }
        return texture;
    }

    public ResourceLocation getIcon() {
        if (icon == null) {
            String path = "textures/icons/" + name + ".png";
            icon = ResourceLocation.fromNamespaceAndPath("readstar", path);
        }
        return icon;
    }

}
