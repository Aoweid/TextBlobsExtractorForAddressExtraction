package combination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * this class creates the maskarray out of input hocr file
 * @author alvin
 *
 */
public class MaskArray {
	//set lists to store the content word and related coordinates
	private ArrayList<Integer> x0List;
	private ArrayList<Integer> x1List;
	private ArrayList<Integer> y0List;
	private ArrayList<Integer> y1List;
	private ArrayList<String> content;
	private int length;
	private int width;
	private String hocrPath;
	/**
	 * constructor
	 * @param hocrPath
	 */
	public MaskArray(String hocrPath){
		this.hocrPath = hocrPath;
	}
	/**
	 * parse hocr file and get the information
	 */
	public void setUp(){
		x0List = new ArrayList<Integer>();
		x1List = new ArrayList<Integer>();
		y0List = new ArrayList<Integer>();
		y1List = new ArrayList<Integer>();
		content = new ArrayList<String>();
		File hocr = new File(hocrPath);
		try {
			//parse hocr file
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(false);
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
			dbFactory.setFeature("http://xml.org/sax/features/validation", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(hocr);
			doc.getDocumentElement().normalize();
			NodeList divList  = doc.getElementsByTagName("div");
			// get the length and the width of this page
			for(int i=0; i<divList.getLength();i++){
				Node node = divList.item(i);
				Element ele = (Element) node;
				if(ele.getAttribute("class").equals("ocr_page")){
					String[] coordinate = ele.getAttribute("title").split("; ")[1].split(" ");
					int tempX1 = Integer.parseInt(coordinate[3]);
					int tempY1 = Integer.parseInt(coordinate[4]);
					width = tempX1;
					length = tempY1;
				}
			}
			// get the words and related coordinates
			NodeList nList = doc.getElementsByTagName("span");
			for(int i = 0;i < nList.getLength();i++){
				Node node = nList.item(i);
				Element ele = (Element) node;
				if(ele.getAttribute("class").equals("ocrx_word")){
					if(!ele.getTextContent().equals(" ")){
						content.add(ele.getTextContent());
						String[] coordinate = ele.getAttribute("title").split(" ");
						int tempX0 = Integer.parseInt(coordinate[1]);
						int tempY0 = Integer.parseInt(coordinate[2]);
						int tempX1 = Integer.parseInt(coordinate[3]);
						int tempY1 = Integer.parseInt(coordinate[4].replace(";", ""));
						x0List.add(tempX0);
						y0List.add(tempY0);
						x1List.add(tempX1);
						y1List.add(tempY1);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * this returns one int[][] as the maskarray
	 * @return int[][]
	 */
	public int[][] getMaskArray(){
		int[][] maskArray = new int[width+1][length+1];
		//white space to 0
		for(int i=0;i<width+1;i++){
			for(int j=0;j<length+1;j++){
				maskArray[i][j]=0;
			}
		}
		//text blobs to 1
		for(int i=0;i<content.size();i++){
			int x0 = x0List.get(i);
			int y0 = y0List.get(i);
			int x1 = x1List.get(i);
			int y1 = y1List.get(i);
			for(int m=x0;m<=x1;m++){
				for(int n=y0;n<=y1;n++){
					maskArray[m][n] = 1;
				}
			}
		}
		return maskArray;
	}
	public int[][] getShrinkedMaskArray(int divisor){
		int[][] shrinkedMaskArray = new int[width/divisor+1][length/divisor+1];
		for(int i=0;i<width/divisor+1;i++){
			for(int j=0;j<length/divisor+1;j++){
				shrinkedMaskArray[i][j]=0;
			}
		}
		for(int i=0;i<content.size();i++){
			int x0 = x0List.get(i);
			int y0 = y0List.get(i);
			int x1 = x1List.get(i);
			int y1 = y1List.get(i);
			for(int m=x0;m<=x1;m++){
				for(int n=y0;n<=y1;n++){
					shrinkedMaskArray[m/divisor][n/divisor] = 1;
				}
			}
		}
		return shrinkedMaskArray;
	}
	public ArrayList<Integer> getX0List(){
		return x0List;
	}
	public ArrayList<Integer> getY0List(){
		return y0List;
	}
	public ArrayList<Integer> getX1List(){
		return x1List;
	}
	public ArrayList<Integer> getY1List(){
		return y1List;
	}
	public ArrayList<String> getContent(){
		return content;
	}
	public int getLength(){
		return length;
	}
	public int getWidth(){
		return width;
	}
}
