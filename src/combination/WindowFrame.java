package combination;

import java.util.ArrayList;


/**
 * main class, carry out the window frame function. The benchmark of matching:
 * there is text in window while frame is all white space
 * 
 * @author alvin
 *
 */
public class WindowFrame {
	private int frameWidth;
	private int windowWidth;
	private int windowLength;
	private int scanUnit = 10;
	private int[][] maskArray;
	private ArrayList<String> words;
	private ArrayList<Integer> x0List;
	private ArrayList<Integer> y0List;
	private ArrayList<Integer> x1List;
	private ArrayList<Integer> y1List;
	private ArrayList<int[]> sizes = new ArrayList<int[]>();
	private int length;
	private int width;
	private volatile ArrayList<int[]> boxes = new ArrayList<int[]>();
	private volatile ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
	private volatile ArrayList<Double> ratios = new ArrayList<>();
	private String output = "";

	public WindowFrame(ArrayList<String> words, ArrayList<Integer> x0List, ArrayList<Integer> y0List, 
			ArrayList<Integer> x1List, ArrayList<Integer> y1List, int[][] MaskArray, int length, int width) {
		this.words = words;
		this.x1List = x1List;
		this.x0List = x0List;
		this.y1List = y1List;
		this.y0List = y0List;
		this.length = length;
		this.width = width;
		process();
	}

	/**
	 * main method
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public void process() {

		int[] size1 = {700,200,20};
		int[] size2 = {800,200,20};
		sizes.add(size1);
		sizes.add(size2);
		// thread
		for(int[] size:sizes){
			windowWidth = size[0];
			windowLength = size[1];
			frameWidth = size[2];
			for(int i=0;i<width+1-windowWidth-frameWidth*2;i=i+scanUnit){
				try {
					ForLine(i);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		output = output + "WindowFrameFunction Captured:\n";
		output = output + "Box Number: " + boxes.size() + "\n";
		for (int i = 0; i < boxes.size(); i++) {
			output = output + "Box: [" + boxes.get(i)[0] + ", " + boxes.get(i)[1] + ", " + boxes.get(i)[2] + ", "
					+ boxes.get(i)[3] + "] Words: " + contents.get(i).toString() + " Ratio: " + ratios.get(i) + "\n";
		}
	}

	public String getOutput() {
		return output;
	}

	/**
	 * apply threads.
	 * 
	 * @param i
	 * @return
	 * @throws InterruptedException
	 */
	public Thread ForLine(int i) throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				for (int j = 0; j < length + 1 - frameWidth * 2 - windowLength; j = j + scanUnit) {
					// set booleans frameOK and windowOK to decide if one box meet the requirements
					boolean frameOK = true;
					boolean windowOK = false;
					for (int m = i; m < i + windowWidth + frameWidth * 2 + 1; m++) {
						for (int n = j; n < j + windowLength + frameWidth * 2 + 1; n++) {
							if (n - j >= frameWidth && n - j <= windowLength + frameWidth && m - i >= frameWidth
									&& m - i <= windowWidth + frameWidth) {
								continue;
							} else {
								if (maskArray[m][n] == 1) {
									frameOK = false;
									break;
								}
							}
						}
					}
					double count = 0;
					for (int m = i + frameWidth; m <= i + frameWidth + windowWidth; m++) {
						for (int n = j + frameWidth; n <= j + frameWidth + windowLength; n++) {
							if (maskArray[m][n] == 1) {
								count++;
							}
						}
					}

					double ratio = count / (windowLength * windowWidth);
					if (ratio > 0.2) {
						windowOK = true;
					}
					// get the content of the box
					if (frameOK && windowOK) {
						int[] box = { i, j, i + frameWidth * 2 + windowWidth, j + frameWidth * 2 + windowLength };
						ArrayList<String> content = new ArrayList<String>();
						for (int index = 0; index < words.size(); index++) {
							int x0 = x0List.get(index);
							int y0 = y0List.get(index);
							int x1 = x1List.get(index);
							int y1 = y1List.get(index);
							if (x0 >= i + frameWidth && x1 <= i + frameWidth + windowWidth && y0 > j + frameWidth
									&& y1 <= j + frameWidth + windowLength) {
								content.add(words.get(index));
							}
						}
						// if the contents are not repeat, add box to the list of all boxes
						if (!contents.contains(content)) {
							contents.add(content);
							boxes.add(box);
							ratios.add(ratio);
						}
					}
				}
			}
		});
		t1.start();
		t1.join();
		return t1;

	}
	
	public ArrayList<int[]> getBoxes(){
		return boxes;
	}
	public ArrayList<ArrayList<String>> getContents(){
		return contents;
	}
	public ArrayList<Double> getRatios(){
		return ratios;
	}

}
