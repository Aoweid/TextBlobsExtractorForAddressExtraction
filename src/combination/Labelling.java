package combination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labelling {
	private int[][] shrinkedMaskArray;
	private int[][] labelArray;
	private ArrayList<Integer> labelList;
	private ArrayList<String> words;
	private ArrayList<Integer> x1List;
	private ArrayList<Integer> y1List;
	private Map<Integer, List<String>> map;
	private int divisor;
	public Labelling(ArrayList<String> words, ArrayList<Integer> x1List, 
			ArrayList<Integer> y1List, int[][] shrinkedMaskArray, int divisor){
		this.words = words;
		this.x1List = x1List;
		this.y1List = y1List;
		this.shrinkedMaskArray = shrinkedMaskArray;
		this.divisor = divisor;
		labelList = new ArrayList<Integer>();
		map= new HashMap<>();
	}
	public void label(){
		ConnectedComponentLabelling l = new ConnectedComponentLabelling(shrinkedMaskArray.length, 
				shrinkedMaskArray[0].length);
		for (int i = 0; i < shrinkedMaskArray.length; i++) {
			for (int j = 0; j < shrinkedMaskArray[0].length; j++) {
				l.m[i][j]=shrinkedMaskArray[i][j];
			}
		}
		l.find_components();
		labelArray = l.label;
		
		for(int index=0;index<words.size();index++){
			int x1 = x1List.get(index)/divisor;
			int y1 = y1List.get(index)/divisor;
			labelList.add(l.label[x1][y1]);
			if(l.label[x1][y1]!=0){
				if(map.containsKey(l.label[x1][y1])){
					List<String> list=map.get(l.label[x1][y1]);
					list.add(words.get(index));
					map.put(l.label[x1][y1], list);
				}else{
					List<String> list=new ArrayList<>();
					list.add(words.get(index));
					map.put(l.label[x1][y1], list);
				}
			}
		}

	}
	public Map<Integer, List<String>> getMap(){
		return map;
	}
	public int[][] getLabelArray(){
		return labelArray;
	}
	public ArrayList<Integer> getLabelList(){
		return labelList;
	}
	
}
