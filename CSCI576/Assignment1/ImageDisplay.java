import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	int width = 512; // default image width and height
	int height = 512;

	int[] dx = {-1,1,0,-1,1,0,-1,1,0};
	int[] dy = {0,0,0,1,1,1,-1,-1,-1};

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void showIms(String[] args){

		if(args.length != 4){
			System.err.println("ERROR: There must be exactly 4 arguments.");
			return;
		}
		// default values for each parameter
		// in default, the image will not be changed
		double scale = 1.0;
		int numOfBit = 8;
		int param3 = -1;
		try{
			scale = Double.parseDouble(args[1]);
			numOfBit = Integer.parseInt(args[2]);
			param3 = Integer.parseInt(args[3]);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("ERROR: Parameters are not in valid formats.");
			return;
		}
		if(!validate(scale, numOfBit, param3)){
			System.err.println("ERROR: Parameters are not in valid ranges.");
			return;
		}

		// result image
		int resultWidth = (int) Math.floor(width * scale);
		int resultHeight = (int) Math.floor(height * scale);
		BufferedImage resultImg = new BufferedImage(resultWidth, resultHeight, BufferedImage.TYPE_INT_RGB);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		List<Integer> intervals = getQuantizationIntervals(numOfBit, param3);
		//System.out.println(intervals);
		for(int i = 0; i < resultWidth; i++){
			for(int j = 0; j < resultHeight; j++){
				int oi = (int) Math.floor(i / scale); // original x value
				int oj = (int) Math.floor(j / scale); // original y value
				int filtered = getFilteredValue(oi, oj, intervals);
				resultImg.setRGB(i, j, filtered);
			}
		}

		// display the result image instead of the original one
		imgOne = resultImg;

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	private int getFilteredValue(int x, int y, List<Integer> ruler){
		int sumR = 0;
		int sumG = 0;
		int sumB = 0;
		int count = 0;
		for(int dir = 0; dir < 9; dir++){
			int ii = x + dx[dir];
			int jj = y + dy[dir];
			if(ii >= 0 && jj >= 0 && ii < width && jj < height){
				int val = imgOne.getRGB(ii, jj);
				sumR += ((val >> 16) & 0xff);
				sumG += ((val >> 8) & 0xff);
				sumB += (val & 0xff);
				count++;
			}
		}

		int avgR = sumR / count;
		int avgG = sumG / count;
		int avgB = sumB / count;

		// quantization applied on R, G, and B values
		avgR = quantize(avgR, ruler);
		avgG = quantize(avgG, ruler);
		avgB = quantize(avgB, ruler);

		return 0xff000000 | ((avgR & 0xff) << 16) | ((avgG & 0xff) << 8) | (avgB & 0xff);
	}

	/**
	 * get the rgb value after quantization
	 * @param original the original R/G/B value
	 * @param intervals quantization intervals
	 * @return
	 */
	private int quantize(int original, List<Integer> intervals){
		int idx = 0;
		while(idx < intervals.size() && intervals.get(idx) < original){
			idx++;
		}
		int hi = 256 / (intervals.size() - 1) * idx;
		int low = 256 / (intervals.size() - 1) * Math.max(idx - 1, 0);
		return (hi + low) / 2;
	}

	public List<Integer> getQuantizationIntervals(int numOfBits, int offset){
		if(numOfBits == 1){
			List<Integer> res = new ArrayList<>();
			res.add(0);
			res.add(offset == -1? 128: offset);
			res.add(256);
			return res;
		}

		int num = (int) Math.pow(2, numOfBits);
		int interval = 256 / num;

		if(offset == -1){
			List<Integer> res = new ArrayList<>();
			for(int i = 0; i <= num; i++){
				res.add(i * interval);
			}
			return res;
		}

		int a = offset / interval;
		int b = num - a;
		List<Integer> A = divideRanges(a, offset);
		List<Integer> B = divideRanges(b, 256 - offset);
		Collections.reverse(B);
		for(int i = 1; i < A.size(); i++){
			B.add(- A.get(i));
		}
		for(int i = 0; i < B.size(); i++){
			B.set(i, B.get(i) + offset);
		}
		Collections.reverse(B);
		return B;
	}

	/**
	 * divide a range into several intervals
	 * @param num number of intervals needed
	 * @param len the length of the range
	 * @return
	 */
	public List<Integer> divideRanges(int num, int len){
		double x = Math.pow(Math.E, Math.log(len + 1.0) / num);
		List<Integer> res = new ArrayList<>();
		res.add(0);
		if(num == 1){
			res.add(len);
			return res;
		}
		for(int i = 0; i < num; i++){
			int curr = (int) Math.round(res.get(i) + Math.pow(x, i));
			res.add(curr);
		}
		double ratio = res.get(res.size() - 1) * 1.0 / len;
		res.set(res.size() - 1, len);
		for(int i = 1; i < res.size() - 1; i++){
			res.set(i, (int) Math.round(res.get(i) / ratio));
		}
		return res;
	}


	/**
	 * check whether these parameters are valid
	 * return true if valid
	 * @param scale larger than 0.0, less or equal to 1.0
	 * @param numOfBit 1 to 8, inclusive
	 * @param param3 -1 to 255, inclusive
	 * @return
	 */
	private boolean validate(double scale, int numOfBit, int param3){
		if(Double.compare(scale, 0) <= 0 || Double.compare(scale, 1.0) > 0){
			System.err.println("ERROR : the scale should be from 0.0 to 1.0.");
			return false;
		}
		if(numOfBit < 1 || numOfBit > 8){
			System.err.println("ERROR : number of bit: range from 1 to 8, inclusive.");
			return false;
		}
		if(param3 < -1 || param3 > 255){
			System.err.println("ERROR : quantization parameter: -1 or [0 - 255].");
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
