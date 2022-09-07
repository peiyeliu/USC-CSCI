// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA1
// Fall 2020


import javax.swing.*;
import java.awt.*;

/**
 * This component draws three bar shapes.
 */
public class CoinSimComponent extends JComponent {

    //The color for each bar is fixed.
    private final Color TWO_HEADS_COLOR = Color.red;
    private final Color HEAD_TAIL_COLOR = Color.green;
    private final Color TWO_TAILS_COLOR = Color.blue;

    //simulator
    private CoinTossSimulator simulator;

    //parameters to calculation the location of bar and label
    private int verticalBuffer;
    private int barWidth;


    /**
     * the constructor of CoinSimComponent object
     *
     * @param total the total number of trials in the coin tossing simulation
     * @param vb    the vertical buffer space, in pixels
     * @param bw    the width of the bar, in pixels
     */
    public CoinSimComponent(int total, int vb, int bw) {
        //initialize and run the simulator
        simulator = new CoinTossSimulator();
        simulator.run(total);

        barWidth = bw;
        verticalBuffer = vb;

    }

    //Draw the three bars in the frame
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // the maximum bar height = the frame height - 2 * buffer space(top and bottom)
        // the maximum bar height is divided into 100 pieces, scale = maximum height/100
        // for each bar, the bar height = scale * percentage number (calculate in the Bar constructor)
        double scale = (getHeight() - 2 * verticalBuffer) / 100.0;
        int labelBottom = getHeight() - verticalBuffer;
        int barLeft = getWidth() / 4 - barWidth / 2;

        //after running simulator, the percentage number and label can be finalized for bar drawing
        int twoHeadPercentage = (int) Math.round(100.0 * simulator.getTwoHeads() / simulator.getNumTrials());
        int headTailPercentage = (int) Math.round(100.0 * simulator.getHeadTails() / simulator.getNumTrials());
        int twoTailPercentage = (int) Math.round(100.0 * simulator.getTwoTails() / simulator.getNumTrials());
        String twoHeadLabel = "Two Heads: " + simulator.getTwoHeads() + " (" + twoHeadPercentage + "%)";
        String headTailLabel = "One Head One Tail: " + simulator.getHeadTails() + " (" + headTailPercentage + "%)";
        String twoTailLabel = "Two Tails: " + simulator.getTwoTails() + " (" + twoTailPercentage + "%)";

        Bar twoHeadBar = new Bar(labelBottom, barLeft, barWidth, twoHeadPercentage, scale, TWO_HEADS_COLOR,
                twoHeadLabel);

        barLeft += getWidth() / 4;
        Bar headTailBar = new Bar(labelBottom, barLeft, barWidth, headTailPercentage, scale, HEAD_TAIL_COLOR,
                headTailLabel);

        barLeft += getWidth() / 4;
        Bar twoTailBar = new Bar(labelBottom, barLeft, barWidth, twoTailPercentage, scale, TWO_TAILS_COLOR,
                twoTailLabel);

        twoHeadBar.draw(g2);
        headTailBar.draw(g2);
        twoTailBar.draw(g2);
    }
}
