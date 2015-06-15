/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gravitydodger;

/**
 *
 * @author Drew
 */
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class GravityDodger extends JPanel implements KeyListener {

    int lives = 10;
    long safetyTime = 3000;
    boolean recovering = false;
    long lastTimeHit = Integer.MIN_VALUE;
    /////////////////////////////////////////////////////////////
    static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static GravityDodger animation = new GravityDodger();
    int width = (int) (screenSize.getWidth() * 19 / 20);
    int height = (int) (screenSize.getHeight() * 17 / 20);
    int paintDelay = 10;
    int runDelay = 25;
    int launchDelay = 54720 / width;
    long startTime = (new Date()).getTime();
    long timePassed = 0l;
    /////////////////////////////////////////////////////////////
    double[] myPos = {width * .1, height * .1};
    double[] myVel = {0.0, 0.0};
    int size = 15;
    double speed = 8;
    /////////////////////////////////////////////////////////////
    double[] holePos = {width * .9, height * .9};
    double[] holeVel = {0.0, 0.0};
    int startingHoleSize = 30;
    int holeSize = startingHoleSize;
    double holeSpeed = speed * 2 / 3;
    double gravity = 1;

    /////////////////////////////////////////////////////////////
    public static void main(String args[]) {
        //System.out.println(animation.width + " :: " + animation.height + " :: " + animation.myPos[0] + " :: " + animation.myPos[1]);
        animation.addKeyListener(animation);
        animation.setFocusable(true);
        animation.setPreferredSize(new Dimension(animation.width, animation.height));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame f = new JFrame("Gravity Dodger");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setLocation(0, 0);
                f.add(animation);
                f.pack();
                f.setLocationByPlatform(true);
                f.setResizable(true);
                f.setVisible(true);
            }
        });

        ///////////////////

        TimerTask painting = new paintTiming();
        TimerTask running = new runTiming();
        TimerTask missiles = new MissileLaunch();
        Timer timer = new Timer(true);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {}
        timer.schedule(painting, 0, animation.paintDelay);
        timer.schedule(running, 0, animation.runDelay);
        timer.schedule(missiles, 0, animation.launchDelay);
    }

    static class MissileLaunch extends TimerTask {

        @Override
        public void run() {
            new Missile();
        }
    }

    static class paintTiming extends TimerTask {

        @Override
        public void run() {
            animation.repaint();
            animation.timePassed = (new Date()).getTime() - animation.startTime;
            //System.out.println(timePassed);
        }
    }

    /////////////////////////////////////////////////////////////
    static class runTiming extends TimerTask {

        @Override
        public void run() {
//            System.out.println(animation.width + " :: " + animation.height + " :: " + animation.myPos[0] + " :: " + animation.myPos[1]);
            animation.playerNextPos();
            animation.holeNextPos();
            Missile.nextAll();


        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.red);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.blue);
        g2.setFont(new Font("Arial Bold", 1, 40));
        g2.drawString("" + (timePassed / 1000l), 10, 35);
        g2.drawString("Lives : " + lives, width - 200, 35);

        g2.setColor(Color.blue);
        if (recovering) {
            //System.out.println("changing color");
            int blinkTime = 100;
            if ((int) (Math.abs(timePassed - lastTimeHit) / blinkTime) % 2 == 0) {
                g2.setColor(Color.red);
                //System.out.println("Im green");
            }

        }
        g2.fillOval((int) myPos[0], (int) myPos[1], size, size);

        g2.setColor(Color.black);
        g2.fillOval((int) holePos[0], (int) holePos[1], holeSize, holeSize);
        Missile.paintAll(g2);
    }

    public void playerNextPos() {

        myPos[0] += myVel[0] * speed;
        myPos[1] += myVel[1] * speed;

        if (myPos[0] < 0) {
            myPos[0] = 0;
        }
        if (myPos[0] > width - size) {
            myPos[0] = width - size;
        }
        if (myPos[1] < 0) {
            myPos[1] = 0;
        }
        if (myPos[1] > height - size) {
            myPos[1] = height - size;
        }

        checkHits();

    }

    public void checkHits() {
        double[] myCenter = {myPos[0] + size/2, myPos[1] + size/2};
        double[] holeCenter = {holePos[0] + holeSize/2, holePos[1] + holeSize/2};
        if(Math.hypot(myCenter[0] - holeCenter[0], myCenter[1] - holeCenter[1]) < (size + holeSize) / 2) lose();
        if (!recovering) {
            //check for impacts
            ArrayList<Missile> miss = Missile.liveMissiles;
            for (int i = 0; i < miss.size(); i++) {
                Missile mis = miss.get(i);
                double xdist = mis.pos[0] + (double) mis.radius / 2
                        - myPos[0] - (double) size / 2;
                double ydist = mis.pos[1] + (double) mis.radius / 2
                        - myPos[1] - (double) size / 2;
                double dist = Math.hypot(xdist, ydist);
                if (dist < (double) (size + mis.radius) / 2) {
                    mis.explode();
                    recovering = true;
                    lastTimeHit = timePassed;
                    launchDelay = launchDelay * 2 / 3; //not working
                    lives--;
                    if (lives <= 0) {
                        lose();
                    }
                }
            }
        } else if (Math.abs(timePassed - lastTimeHit) >= safetyTime) {
            recovering = false;
        }
    }

    public void lose() {
        lives = 10;
        Missile.liveMissiles.clear();
        myPos = new double[]{width * .2, height * .2};
        myVel = new double[]{0.0, 0.0};
        holePos = new double[]{width * .8, height * .8};
        holeVel = new double[]{0.0, 0.0};
        holeSize = startingHoleSize;
        startTime = (new Date()).getTime();
        timePassed = 0l;
        lastTimeHit = Integer.MIN_VALUE;
    }

    public void holeNextPos() {
        setHoleVel();
        holePos[0] += holeVel[0] * holeSpeed;
        holePos[1] += holeVel[1] * holeSpeed;
    }

    public double diagonalLength() {
        return Math.sqrt(Math.pow(holePos[0] + (double) holeSize / 2 - myPos[0] - (double) size / 2, 2)
                + Math.pow(holePos[1] + (double) holeSize / 2 - myPos[1] - (double) size / 2, 2));
    }

    public void setHoleVel() {
        double[] newHoleVel = {myPos[0] + (double) size / 2 - holePos[0] - (double) holeSize / 2,
            myPos[1] + (double) size / 2 - holePos[1] - (double) holeSize / 2};
        for (int i = 0; i <= 1; i++) {
            newHoleVel[i] /= diagonalLength();
        }
        holeVel = newHoleVel;
    }

    public void keyPressed(KeyEvent event) {
        //System.out.println(event.getKeyCode());
        switch (event.getKeyCode()) {
            case 38: //up
                myVel[1] = -1;
                break;
            case 39: //right
                myVel[0] = 1;
                break;
            case 37: //left
                myVel[0] = -1;
                break;
            case 40: //down
                myVel[1] = 1;
                break;
        }
        event.consume();
    }

    public void keyReleased(KeyEvent event) {
        //System.out.println(event.getKeyCode());
        switch (event.getKeyCode()) {
            case 38: //up
                if (myVel[1] == -1) {
                    myVel[1] = 0;
                }
                break;
            case 39: //right
                if (myVel[0] == 1) {
                    myVel[0] = 0;
                }
                break;
            case 37: //left
                if (myVel[0] == -1) {
                    myVel[0] = 0;
                }
                break;
            case 40: //down
                if (myVel[1] == 1) {
                    myVel[1] = 0;
                }
                break;
        }
        event.consume();
    }

    public void keyTyped(KeyEvent event) {
    }
}