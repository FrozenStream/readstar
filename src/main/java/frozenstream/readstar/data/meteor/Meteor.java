package frozenstream.readstar.data.meteor;

import org.joml.Vector3f;

class Meteor {
    Vector3f startPosition;
    Vector3f direction;
    Vector3f color;
    float pathLength;
    float currentLength;
    float speed;

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