package frozenstream.readstar.element.meteor;

import org.joml.Vector3f;

public class Meteor {
    public Vector3f startPosition;
    public Vector3f direction;
    public Vector3f color;
    public float pathLength;
    public float currentLength;
    public float speed;

    public boolean step(){
        currentLength += speed* 0.05f;
        return currentLength > pathLength;
    }

    public Vector3f getPosition(){
        Vector3f pos = new Vector3f();
        pos.set(direction).mul(currentLength).add(startPosition);
        return pos;
    }

    public void getPosition(Vector3f pos){
        pos.set(direction).mul(currentLength).add(startPosition);
    }
}