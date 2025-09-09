package org.sims.models;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class ParticleCollision extends Event{

    private Particle p1;
    private Particle p2;


    public ParticleCollision(Particle p1, Particle p2, double collisionTime) {
        this.collisionTime = collisionTime;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void execute() {
        collide(p1, p2);
    }

    private double ct = 1, cn = 1;
    /**
     * Changes velocities of the particles received
     * @param p first particle
     * @param other second particle
     */
    private void collide(Particle p, Particle other) {
        Vector normalVersor = Vector.subtract(p.getPosition(), other.getPosition());
        Vector xVersor = new Vector(1,0);
        double alpha = Vector.angle(normalVersor, xVersor);    //angle between normal versor of collision and x axis
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);
        double m11 = (-cn * cosAlpha*cosAlpha) + (ct * sinAlpha*sinAlpha);
        double m12 = -(cn+ct)*sinAlpha*cosAlpha;
        double m21 = m12;
        double m22 = (-cn * sinAlpha*sinAlpha) + (ct * cosAlpha*cosAlpha);
        double[][] m = new double[][]{  {m11, m12},
                {m21, m22} };
        RealMatrix collisionOperator = MatrixUtils.createRealMatrix(m);

        //=======First particle=======
        RealMatrix v1 = MatrixUtils.createRealMatrix(p.getVelocity().toColumnMatrix());
        double[] v1Prime = collisionOperator.multiply(v1).getColumn(0);
        p.setVelocity(new Vector(v1Prime[0], v1Prime[1]));

        //=======Second particle=====
        RealMatrix v2 = MatrixUtils.createRealMatrix(other.getVelocity().toColumnMatrix());
        double[] v2Prime = collisionOperator.multiply(v2).getColumn(0);
        other.setVelocity(new Vector(v2Prime[0], v2Prime[1]));
    }
}
