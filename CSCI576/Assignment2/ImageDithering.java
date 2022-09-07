import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * This class is for question 4: Image Dithering
 */

public class ImageDithering {

    private static final int width = 12;
    private static final int height = 8;

    private static final int width_zoom = 120;
    private static final int height_zoom = 80;


    private static GridBagConstraints c = new GridBagConstraints();
    static {
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
    }


    static BufferedImage imgOne;
    static BufferedImage imgTwo;
    static BufferedImage imgThree;
    static BufferedImage imgFour;

    static {
        imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        imgThree = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        imgFour = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private static final int[][] input = {
            {1,2,3,4,5,6,7,8,9,0,1,2},
            {0,1,2,3,4,5,6,7,8,9,0,1},
            {9,0,1,2,3,4,5,6,7,8,9,0},
            {8,9,0,1,2,3,4,5,6,7,8,9},
            {7,8,9,0,1,2,3,4,5,6,7,8},
            {6,7,8,9,0,1,2,3,4,5,6,7},
            {5,6,7,8,9,0,1,2,3,4,5,6},
            {4,5,6,7,8,9,0,1,2,3,4,5}
    };

    private static final int[][] D = {
            {6,8,4},
            {1,0,3},
            {5,2,7}
    };

    private static void displayImage(BufferedImage image, String filename){
        JFrame frame;
        JLabel lbIm1;
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        lbIm1 = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(lbIm1, c);
        frame.pack();
        frame.setVisible(true);
        try {
            ImageIO.write(image, "png", new File(filename));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void plotOne(){
        for(int j = 0; j < width; j++){
            for(int i = 0; i < height; i++){
                int rgbVal = 255 / 9 * input[i][j];
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((rgbVal & 0xff) << (8 * color));
                }
                imgOne.setRGB(j, i, res);
            }
        }
        displayImage(imgOne, "1.png");
    }

    private static void plotTwo(){
        for(int j = 0; j < width; j++){
            for(int i = 0; i < height; i++){
                int modified = input[i][j] > 4.5? 9: 0;
                int rgbVal = 255 / 9 * modified;
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((rgbVal & 0xff) << (8 * color));
                }
                imgTwo.setRGB(j, i, res);
            }
        }
        displayImage(imgTwo, "2.png");
    }

    private static void plotThree(){
        for(int j = 0; j < width; j++){
            for(int i = 0; i < height; i++){
                int modified = input[i][j] > D[i%3][j%3]? 1: 0;

                int rgbVal = 255 * modified;
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((rgbVal & 0xff) << (8 * color));
                }
                imgThree.setRGB(j, i, res);
            }
        }
        displayImage(imgThree, "3.png");

    }

    private static void plotFour(){
        for(int j = 0; j < width; j++){
            for(int i = 0; i < height; i++){
                int modified = input[i][j] > D[(i+1)%3][(j+1)%3]? 1: 0;

                int rgbVal = 255 * modified;
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((rgbVal & 0xff) << (8 * color));
                }
                imgFour.setRGB(j, i, res);
            }
        }
        displayImage(imgFour, "4.png");

    }




    public static void main(String[] args){
        plotOne();
        plotTwo();
        plotThree();
        plotFour();
    }
}
