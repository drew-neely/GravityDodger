/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gravitydodger;

/**
 *
 * @author Drew
 */
import static gravitydodger.GravityDodger.animation;
import java.awt.*;
import java.util.*;

public class Missile {

    static ArrayList<Missile> liveMissiles = new ArrayList<Missile>();
    static double gravity = 5;
    double[] pos = new double[2];
    int[] vel = new int[2];
    int speed = (int) (Math.random() * 5 + 5);
    ;
	int radius = (int) (Math.random() * 15 + 10);
    int myIndex;
    boolean explode = false;

    public Missile() {

        int index = (int) (Math.random() * 2);
        pos[index] = (int) (Math.random()
                * ((index == 0) ? GravityDodger.animation.width : GravityDodger.animation.height));
        int[] sidePos = {0, (index == 0) ? GravityDodger.animation.height : GravityDodger.animation.width};
        pos[(index == 0) ? 1 : 0] = sidePos[(int) (Math.random() * 2)];
        myIndex = liveMissiles.size();
        liveMissiles.add(this);

    }

    public static void nextAll() {
        for (int i = 0; i < liveMissiles.size(); i++) {
            Missile mis = liveMissiles.get(i);
            double dist = mis.holeDist();
            if (dist <= 3 || mis.explode) {
                //System.out.println("removing");
                liveMissiles.remove(i);
                //if(Math.random() < .05) GravityDodger.holeSize++;
            }
            double[] dir = new double[2];
            dir[0] = GravityDodger.animation.holePos[0] - mis.pos[0];
            dir[1] = GravityDodger.animation.holePos[1] - mis.pos[1];
            dir[0] /= dist;
            dir[1] /= dist;
            double gravity = GravityDodger.animation.gravity;
            mis.pos[0] += (double) mis.speed * (gravity * dir[0] + mis.vel[0]) / 2;
            mis.pos[1] += (double) mis.speed * (gravity * dir[1] + mis.vel[1]) / 2;
        }
    }

    public static void paintAll(Graphics g) {
        g.setColor(Color.yellow);
        for (int i = 0; i < liveMissiles.size(); i++) {
            try{
                Missile mis = liveMissiles.get(i);
                g.fillOval((int) mis.pos[0], (int) mis.pos[1], mis.radius, mis.radius);
            } catch(Exception e) {}
        }
    }

    public double holeDist() {
        double var1 = Math.pow(pos[0] + (double) radius / 2
                - GravityDodger.animation.holePos[0] - (double) GravityDodger.animation.holeSize / 2, 2);
        double var2 = Math.pow(pos[1] + (double) radius / 2
                - GravityDodger.animation.holePos[1] - (double) GravityDodger.animation.holeSize / 2, 2);
        return Math.sqrt(var1 + var2) - (double) GravityDodger.animation.holeSize - radius;
    }

    public void explode() {
        explode = true;
    }
}
