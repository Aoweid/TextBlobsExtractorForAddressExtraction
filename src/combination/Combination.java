package combination;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Combination {
	static int frameWidth;
	static int windowWidth;
	static int windowLength;
	static int scanUnit = 10;
	static ArrayList<int[]> sizes = new ArrayList<int[]>();
	static int[][] maskArray;
	static ArrayList<String> words;
	static ArrayList<Integer> x0List;
	static ArrayList<Integer> y0List;
	static ArrayList<Integer> x1List;
	static ArrayList<Integer> y1List;
	static int length;
	static volatile  ArrayList<int[]> boxes;
	static volatile  ArrayList<ArrayList<String>> contents;
	static volatile  ArrayList<Double> ratios;
	public static void main(String[] args) throws InterruptedException{
		// TODO Auto-generated method stub
		String folderPath = "data/input/";
		GetFiles getFiles = new GetFiles(folderPath, "tif");
		ArrayList<File> files = new ArrayList<>();
		files = getFiles.getFilesArr();
		int[] size1 = {700,200,20};
		int[] size2 = {800,200,20};
		sizes.add(size1);
		sizes.add(size2);
		for (File file : files) {
			// set timer
			double startTime = System.currentTimeMillis();
			// get hocr output from input tiff file using tesseract
			String fileName = file.getName();
			String srcFile = file.getAbsolutePath();
			String targetHocrFile = "data/hocr/" + fileName.replace(".tif", "");
			// final String command1 = "tesseract " + srcFile + " " +
			// targetHocrFile + " hocr";
			final String command1 = "/usr/local/Cellar/tesseract/3.04.01_1/bin/tesseract " + srcFile + " "
					+ targetHocrFile + " hocr";
			try {
				Process p1 = Runtime.getRuntime().exec(command1);
				p1.waitFor();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// initiate maskarray and get basic information of this maskarray
			MaskArray MA = new MaskArray(targetHocrFile + ".hocr");
			MA.setUp();
			maskArray = MA.getMaskArray();
			int divisor = 20;
			int[][] shrinkedMA = MA.getShrinkedMaskArray(divisor);
			length = MA.getLength();
			int width = MA.getWidth();
			words = MA.getContent();
			x0List = MA.getX0List();
			y0List = MA.getY0List();
			x1List = MA.getX1List();
			y1List = MA.getY1List();
			boxes = new ArrayList<int[]>();
			contents = new ArrayList<ArrayList<String>>();
			ratios = new ArrayList<>();
			Labelling label = new Labelling(words, x1List, y1List, shrinkedMA, divisor);
			label.label();
			ArrayList<Integer> labelList = label.getLabelList();
			Map<Integer, List<String>> map = label.getMap();
			int[][] labelArray = label.getLabelArray();
			
			
			// thread
			for(int[] size:sizes){
				windowWidth = size[0];
				windowLength = size[1];
				frameWidth = size[2];
				for (int i = 0; i < width + 1 - windowWidth - frameWidth * 2; i = i + scanUnit) {
					new Combination().ForLine(i);

				}
			}
			String WFoutput = "";
			WFoutput = WFoutput + "WindowFrameFunction Captured:\n";
			WFoutput = WFoutput + "Box Number: " + boxes.size() + "\n";
			for (int i = 0; i < boxes.size(); i++) {
				WFoutput = WFoutput + "Box: [" + boxes.get(i)[0] + ", " + boxes.get(i)[1] + ", " + boxes.get(i)[2] + ", "
						+ boxes.get(i)[3] + "] Words: " + contents.get(i).toString() + " Ratio: " + ratios.get(i) + "\n";
			}
			// write outputs in text files
			String outputPath = "data/output/" + fileName.replace(".tif", "");
			File dir = new File(outputPath);
			dir.mkdir();
			
			// output matrix
			File outputMatrix = new File(outputPath +"/" +fileName.replace(".tif", "") + "_matrix.txt");
			try {
				outputMatrix.createNewFile();
				FileWriter writer = new FileWriter(outputMatrix);
				for (int i = 0; i < maskArray.length; i++) {
					for (int j = 0; j < maskArray[0].length; j++) {
						writer.write(maskArray[i][j] + ",");
					}
					writer.write("\n");
				}
				writer.close();
				// output label
				File outputLabel = new File(outputPath +"/"+ fileName.replace(".tif", "") + "_label.txt");
				outputLabel.createNewFile();
				writer = new FileWriter(outputLabel);
				for (int i = 0; i < labelArray.length; i++) {
					for (int j = 0; j < labelArray[0].length; j++) {
						writer.write(labelArray[i][j] + ",");
					}
					writer.write("\n");
				}
				writer.close();
				//output boxes and labels
				String outputFileName = fileName.replace(".tif", ".txt");
				File outputFile = new File(outputPath +"/"+ outputFileName);
				outputFile.createNewFile();
				writer = new FileWriter(outputFile);
				double endTime = System.currentTimeMillis();
				writer.write("Time costed: " + (endTime - startTime) / 1000 + "s\n");
				writer.write("BlobsExtractionFunction outputs:\n");
				for (Map.Entry<Integer, List<String>> entry : map.entrySet()) {
					// System.out.println("*****************************");
					// System.out.println(entry.getKey());
					writer.write(entry.getKey() + "#");
					for (String s : entry.getValue()) {
						// System.out.print(s+" ");
						writer.write(s.replaceAll(",", "") + " ");
					}
					// System.out.println("");
					writer.write("\n");
				}
				writer.write("\n");
				writer.write(WFoutput);
				writer.close();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}
	/**
	 * apply threads to this function
	 * @param i
	 * @return
	 * @throws InterruptedException
	 */
	public Thread ForLine(int i) throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				for (int j = 0; j < length + 1 - frameWidth * 2 - windowLength; j = j + scanUnit) {
					//set booleans to decide if all the benchmarks are satisfied
					boolean frameOK = true;
					boolean windowOK = false;
					for (int m = i; m < i + windowWidth + frameWidth * 2 + 1; m++) {
						for (int n = j; n < j + windowLength + frameWidth * 2 + 1; n++) {
							if (n - j >= frameWidth && n - j <= windowLength + frameWidth && m - i >= frameWidth
									&& m - i <= windowWidth + frameWidth) {
								continue;
							} else {
								if (maskArray[m][n] == 1) {
									// frame is not empty, dont fit
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
					if (frameOK && windowOK) {
						
						// store all the satisfied box to the list of captured boxes and the content contained 
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


}
