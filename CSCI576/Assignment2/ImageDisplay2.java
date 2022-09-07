import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay2 {

    private static final int BASELINE_MODE = 1;
    private static final int SPECTRAL_MODE = 2;
    private static final int SUCCESSIVE_BIT_MODE = 3;
    JFrame originalFrame;
    JLabel originalabel;
    JFrame frame;
    JLabel lbIm1;

    BufferedImage imgOne;
    BufferedImage original;
    int width = 352;
    int height = 288;

    private static double[][] COSINE = new double[8][8];
    static {
        double pi = Math.PI;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                COSINE[i][j] = Math.cos((2 * i + 1) * j * pi / 16);
            }
        }
    }
    private static double[][] CU_CV = new double[8][8];
    static {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0 && j == 0) {
                    CU_CV[i][j] = 0.5;
                } else if (i == 0 || j == 0) {
                    CU_CV[i][j] = 1 / Math.sqrt(2);
                } else {
                    CU_CV[i][j] = 1.0;
                }
            }
        }
    }

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

    double[][][] DCTMatrix = new double[3][width][height];
    double[][][] quantizedDCTMatrix = new double[3][width][height];
	double[][][] dequantizedDCTMatrix = new double[3][width][height];
	int[][][] decodedDCTMatrix = new int[3][width][height];


    /**
     * Read Image RGB
     * Reads the image dfgof given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showIms(String[] args){

        if (args.length != 4) {
            System.err.println("ERROR: There must be exactly 4 arguments.");
            return;
        }
        int quantizationLevel = 0; // 0 to 7, inclusive
        int deliveryMode = 1; // 1, 2 or 3
        int latency = 0;// unit: milliseconds
        try {
            quantizationLevel = Integer.parseInt(args[1]);
            deliveryMode = Integer.parseInt(args[2]);
            latency = Integer.parseInt(args[3]);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: Parameters are not in valid formats.");
            return;
        }
        if (!validate(quantizationLevel, deliveryMode, latency)) {
            System.err.println("ERROR: Parameters are not in valid ranges.");
            return;
        }
        try{
            imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            original = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            readImageRGB(width, height, args[0], original);
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("ERROR: Could not read the input file, please check.");
            return;
        }

        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        lbIm1 = new JLabel(new ImageIcon(imgOne));
        frame.getContentPane().add(lbIm1, c);
        frame.setLocationRelativeTo(originalFrame);


        originalFrame = new JFrame();
        originalFrame.getContentPane().setLayout(gLayout);
        originalabel = new JLabel(new ImageIcon(original));
        originalFrame.getContentPane().add(originalabel, c);
        originalFrame.pack();
        originalFrame.setVisible(true);



        getFreqCoefficients();
        quantization(quantizationLevel);
        dequantize(quantizationLevel);
        decode(latency, deliveryMode);

        if(deliveryMode == BASELINE_MODE){
            displayInSequentialMode(latency);
        }
        else if(deliveryMode == SPECTRAL_MODE){
            displayInSpectralSelection(latency);
        }
        else if(deliveryMode == SUCCESSIVE_BIT_MODE){
            displayInSuccessiveBitMode(latency);
        }
        else{
            System.err.println("The delivery mode is not valid. It should be 1, 2, or 3.");
        }

    }

    private void showImage(){
        lbIm1 = new JLabel(new ImageIcon(imgOne));
        frame.getContentPane().add(lbIm1, c);
        frame.pack();
        frame.setVisible(true);
    }

    private void displayInSequentialMode(int latency){
        for (int j = 0; j < height; j += 8) {
            for (int i = 0; i < width; i += 8) {
                setImageByBlock(i, j);


            }
            showImage();
            if (latency > 0) {
                try {
                    Thread.sleep(latency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void displayInSpectralSelection(int latency){
        for(int u = 0; u < 8; u++){
            for(int v = 0; v < 8; v++){
                for (int j = 0; j < height; j += 8) {
                    for (int i = 0; i < width; i += 8) {
                        int a = i + u;
                        int b = j + v;
                        for(int uu = u; uu < 8; uu++){
                            for(int vv = v; vv < 8; vv++){
                                int x = i + uu;
                                int y = j + vv;
                                setImageAs(x, y, a, b);
                            }
                        }
                    }
                }

                showImage();
                try {
                    Thread.sleep(latency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void displayInSuccessiveBitMode(int latency){
        for(int i = 1; i <= 8; i++){
            setImage(i);
            showImage();
            if (latency > 0) {
                try {
                    Thread.sleep(latency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void setImage(int bitOffset){
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((decodedDCTMatrix[color][i][j] & ((1 << bitOffset) - 1)) << (8 * color));
                }
                imgOne.setRGB(i, j, res);
            }
        }
    }

    private void setImageByBlock(int iOffset, int jOffSet){
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int res = 0xff000000;
                for(int color = 0; color < 3; color++){
                    res |= ((decodedDCTMatrix[color][i+iOffset][j+jOffSet] & 0xff) << (8 * color));
                }
                imgOne.setRGB(i+iOffset, j+jOffSet, res);
            }
        }
    }

    private void setImageByPosition(int x, int y){
        int res = 0xff000000;
        for(int color = 0; color < 3; color++){
            res |= ((decodedDCTMatrix[color][x][y] & 0xff) << (8 * color));
        }
        imgOne.setRGB(x, y, res);
    }

    private void setImageAs(int x, int y, int a, int b){
        int res = 0xff000000;
        for(int color = 0; color < 3; color++){
            res |= ((decodedDCTMatrix[color][a][b] & 0xff) << (8 * color));
        }
        imgOne.setRGB(x, y, res);
    }
    

    private void dequantize(int quantizationLevel){
		for(int color = 0; color < 3; color++){
			for(int i = 0; i < width; i++){
				for(int j = 0; j < height; j++){
					dequantizedDCTMatrix[color][i][j] = quantizedDCTMatrix[color][i][j] * Math.pow(2, quantizationLevel);
				}
			}
		}
	}

	private void decode(int latency, int mode){
		for (int j = 0; j < height; j += 8) {
			for (int i = 0; i < width; i += 8) {
                int[][][] curr = new int[3][8][8];
				for (int x = 0; x < 8; x++) {
					for (int y = 0; y < 8; y++) {
						double[] currRes = new double[3];
						for (int color = 0; color < 3; color++) {
							for (int u = 0; u < 8; u++) {
								for (int v = 0; v < 8; v++) {
									currRes[color] += CU_CV[u][v] * dequantizedDCTMatrix[color][i+u][j+v] * COSINE[x][u] * COSINE[y][v];
								}
							}
							curr[color][x][y] = (int) Math.round(currRes[color] / 4.0);
						}
					}
				}

				for(int x = 0; x < 8; x++){
				    for(int y = 0; y < 8; y++){
				        for(int color = 0; color < 3; color++){
				            if(curr[color][x][y] < 0){
				                curr[color][x][y] = 0;
				            }
				            if(curr[color][x][y] > 255){
				                curr[color][x][y] = 255;
                            }
                            decodedDCTMatrix[color][i+x][j+y] = curr[color][x][y];
                        }
                    }
                }
			}
		}
	}

    private void quantization(int quantizationLevel){
    	for(int color = 0; color < 3; color++){
    		for(int i = 0; i < width; i++){
    			for(int j = 0; j < height; j++){
    				quantizedDCTMatrix[color][i][j] = (double) Math.round(DCTMatrix[color][i][j] / Math.pow(2, quantizationLevel));
				}
			}
		}
	}

    private void getFreqCoefficients() {
        for (int i = 0; i < width; i += 8) {
            for (int j = 0; j < height; j += 8) {

                double[][][] currBlock = new double[3][8][8];
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {

                        double[] currRes = new double[3];
                        for (int color = 0; color < 3; color++) {
                            for (int x = 0; x < 8; x++) {
                                for (int y = 0; y < 8; y++) {
                                    currRes[color] += getPixel(color, x + i, y + j) * COSINE[x][u] * COSINE[y][v];
                                }
                            }
                            currBlock[color][u][v] = currRes[color] * CU_CV[u][v] / 4.0;
                        }

                    }
                }
                for(int u = 0; u < 8; u++){
                    for(int v = 0; v < 8; v++){
                        for(int color = 0; color < 3; color++){
                            DCTMatrix[color][i+u][j+v] = currBlock[color][u][v];
                        }
                    }
                }

            }
        }

    }

    private int getPixel(int color, int x, int y) {
        int offset = color * 8;
        return ((original.getRGB(x, y) >> offset) & 0xff);
    }


    private boolean validate(int param1, int param2, int param3) {
        return param1 >= 0 && param1 <= 7 && param2 >= 1 && param2 <= 3 && param3 >= 0;
    }

    public static void main(String[] args){
        ImageDisplay2 ren = new ImageDisplay2();
        ren.showIms(args);
    }

}
