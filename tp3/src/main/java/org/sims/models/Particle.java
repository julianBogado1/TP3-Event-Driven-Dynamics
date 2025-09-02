package org.sims.models;

public class Particle {
    Vector position;
    Vector velocity;
    double radius;
    public Particle(Vector position,  Vector velocity, double radius) {
        this.radius = radius;
        this.position = position;
        this.velocity = velocity;
    }

    public Vector getVelocity() {
        return velocity;
    }
    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
    public Vector getPosition() {
        return position;
    }
    public void setPosition(Vector position) {
        this.position = position;
    }

    /**
     * Compute timo of collision of this particle with another particle
     * @param p description of the parameter
     * @return description of what the method returns (if not void)
     */
    public double collisionTime(Particle p){
        double relativeVelocityX = p.velocity.getX() - this.velocity.getX();
        double relativeVelocityY = p.velocity.getY() - this.velocity.getY();
        double relativePositionX = p.position.getX() - this.position.getX();
        double relativePositionY = p.position.getY() - this.position.getY();
        double sigma = this.radius + p.radius;

        Vector relativeVelocity = new Vector(relativeVelocityX, relativeVelocityY);
        Vector relativePosition = new Vector(relativePositionX, relativePositionY);

        double d = Math.pow(relativeVelocity.dot(relativePosition), 2) -
                relativeVelocity.dot(relativeVelocity) *
                (relativePosition.dot(relativePosition) - sigma * sigma);
        if (d < 0 || relativeVelocity.dot(relativePosition) >= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return -(relativeVelocity.dot(relativePosition) + Math.sqrt(d)) / relativeVelocity.dot(relativeVelocity);
    }
}
