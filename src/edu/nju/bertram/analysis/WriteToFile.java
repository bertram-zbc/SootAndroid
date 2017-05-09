package edu.nju.bertram.analysis;


import edu.nju.bertram.model.LogicConstraints;
import edu.nju.bertram.model.LogicType;
import edu.nju.bertram.model.Widget;
import edu.nju.bertram.model.WidgetCallWindowModel;
import soot.Body;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class WriteToFile {

	public static String ResultPath = "/home/hadoop/gator-3.2/SootAndroid/Output/";

	public static void writeLogic(ArrayList<LogicConstraints> lcons,
			String filePath) {

		for (int i=0;i<lcons.size();i++){
			System.out.println(lcons.get(i).param1+"\t"+lcons.get(i).param2);
		}

		createFolder(filePath); 

		int bits = lcons.size();
		double times = Math.pow(2, bits);
		for(int i=0;i<times;i++){
			char[] cases = getCase(bits,i);
			String fileName = filePath+i+".stm2";
			if(createFile(fileName)){
				writeLogicToStm2File(fileName,cases,lcons);
			}
		}
		
	}

	private static void writeLogicToStm2File(String fileName, char[] cases,
			ArrayList<LogicConstraints> lcons) {
		String information = ";"+fileName;
		try {
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(information);
			writer.write("\n");
			
			ArrayList<Widget> existWidgets = getExistWidgets(lcons);
			for(int i=0;i<existWidgets.size();i++){
				String s = "(declare-const "+existWidgets.get(i).name+" String)";
				writer.write(s);
				writer.write("\n");
			}
			writer.write("(push)");
			writer.write("\n");

			for(int i=0;i<lcons.size();i++){
				if(cases[i]=='0'){
					LogicConstraints lc = lcons.get(i);
					if(lc.type == LogicType.WIDGET_STRING){
						Widget w = (Widget) lc.param1;
						String s = (String) lc.param2;
						writer.write("(assert (= "+w.name+" "+s+"))");
						writer.write("\n");
					}
					if(lc.type == LogicType.STRING_WIDGET){
						String s = (String) lc.param1;
						Widget w = (Widget) lc.param2;
						writer.write("(assert (= "+w.name+" "+s+"))");
						writer.write("\n");
					}
					if(lc.type == LogicType.WIDGET_WIDGET){
						Widget w1 = (Widget) lc.param1;
						Widget w2 = (Widget) lc.param2;
						writer.write("(assert (= "+w1.name+" "+w2.name+"))");
						writer.write("\n");
					}
				}else{
					LogicConstraints lc = lcons.get(i);
					if(lc.type == LogicType.WIDGET_STRING){
						Widget w = (Widget) lc.param1;
						String s = (String) lc.param2;
						writer.write("(assert (not (= "+w.name+" "+s+")))");
						writer.write("\n");
					}
					if(lc.type == LogicType.STRING_WIDGET){
						String s = (String) lc.param1;
						Widget w = (Widget) lc.param2;
						writer.write("(assert (not (= "+w.name+" "+s+")))");
						writer.write("\n");
					}
					if(lc.type == LogicType.WIDGET_WIDGET){
						Widget w1 = (Widget) lc.param1;
						Widget w2 = (Widget) lc.param2;
						writer.write("(assert (not (= "+w1.name+" "+w2.name+")))");
						writer.write("\n");
					}
				}
			}
			
			writer.write("(check-sat)\n");
			writer.write("(get-model)\n");
			writer.write("(pop)\n");
			writer.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

	private static ArrayList<Widget> getExistWidgets(
			ArrayList<LogicConstraints> lcons) {
		
		ArrayList<Widget> widgets = new ArrayList<Widget>();
		
		for(int i=0;i<lcons.size();i++){
			LogicConstraints lc = lcons.get(i);
			if(lc.type == LogicType.WIDGET_STRING){
				Widget w = (Widget) lc.param1;
				if(!isContainWidget(w,widgets)){
					widgets.add(w);
				}
			}
			if(lc.type == LogicType.STRING_WIDGET){
				Widget w = (Widget) lc.param2;
				if(!isContainWidget(w,widgets)){
					widgets.add(w);
				}
			}
			if(lc.type == LogicType.WIDGET_WIDGET){
				Widget w1 = (Widget) lc.param1;
				if(!isContainWidget(w1,widgets)){
					widgets.add(w1);
				}
				Widget w2 = (Widget) lc.param2;
				if(!isContainWidget(w2,widgets)){
					widgets.add(w2);
				}
			}
		}
		return widgets;
	}

	private static boolean isContainWidget(Widget w, ArrayList<Widget> widgets) {
		for(int i=0;i<widgets.size();i++){
			if(w.name.equals(widgets.get(i).name)){
				return true;
			}
		}
		return false;
	}

	private static boolean createFile(String fileName) {
		try {
			File file = new File(fileName);
			if(!file.exists()){
				file.createNewFile();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static char[] getCase(int bits, int num) {
		char[] result = new char[bits];
		String s = Integer.toBinaryString(num);
		char[] c = s.toCharArray();
		for(int i=bits-1;i>=0;i--){
			if(i>bits-1-c.length){
				result[i]=c[c.length-bits+i];
			}else{
				result[i]='0';
			}
		}
		return result;
	}
	


	private static void createFolder(String filePath) {
		File path = new File(filePath);
		if(!path.exists()){
			path.mkdirs();
		}		
	}

	public static void writeLogicResult(ArrayList<Widget> z3ResultArray, ArrayList<ArrayList<Widget>> sequenceConstrains, Widget widgetOnClick, String logicpath, String logicFileName) {
		//TODO 这里只处理了顺序约束中的第一种情况
		if (z3ResultArray!=null){
			//不等于null说明有解
			ArrayList<Widget> s1 = sequenceConstrains.get(0);
			System.out.println("[z3ResultArray]"+z3ResultArray.size()+"\t"+z3ResultArray);
			createFolder(logicpath);
			if (createFile(logicpath+logicFileName)){
				try {
					FileWriter writer = new FileWriter(logicpath+logicFileName,true);
					String s = s1.get(0).resource_id;
					int index = s.indexOf("/");
					s = s.substring(0,index+1);
					writer.write("Widget:\t"+s+widgetOnClick.name+"\n");
					for (int i=0;i<s1.size();i++){
						for (int j=0;j<z3ResultArray.size();j++){
							System.out.println("[writeLogicResult]"+s1.get(i).name+"\t"+z3ResultArray.get(i).name);
							if (s1.get(i).name.equals(z3ResultArray.get(j).name)){
								writer.write(s1.get(i).resource_id+"\t"+z3ResultArray.get(j).inputValue+"\n");
							}
						}
					}
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//没有逻辑约束，就是说控件的值可以是任意的
	public static void writeSequenceResult(ArrayList<ArrayList<Widget>> sequenceConstrains, Widget widgetOnClick, String sequencepath, String sequenceFileName) {
		ArrayList<Widget>s1 = sequenceConstrains.get(0);
		createFolder(sequencepath);
		//System.out.println("create path:"+sequencepath);
		if (createFile(sequencepath+sequenceFileName)){
			try {
				//System.out.println("create file:"+sequencepath+sequenceFileName);
				FileWriter writer = new FileWriter(sequencepath+sequenceFileName,true);
				//生成resource-id值
				String s = s1.get(0).resource_id;
				int index = s.indexOf("/");
				s = s.substring(0,index+1);
				writer.write("Widget:\t"+s+widgetOnClick.name+"\n");
				for(int i=0;i<s1.size();i++){
					writer.write(s1.get(i).resource_id+"\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeNoLogicWidget(WidgetCallWindowModel wc, String noConstraintPath, ArrayList<Widget> widgetArray, String noConstrainFileName) {
		createFolder(noConstraintPath);
		createFile(noConstraintPath+noConstrainFileName);
		try {
			FileWriter writer = new FileWriter(noConstraintPath+noConstrainFileName,true);
			for(int i=0;i<widgetArray.size();i++){
				if (widgetArray.get(i).resource_id.contains(wc.widgetName)&&widgetArray.get(i).activity.replace("/","").equals(wc.activity)){
					writer.write(widgetArray.get(i).activity+"\t"+widgetArray.get(i).resource_id+"\t"+wc.callBackClassName+":"+wc.callBackMethodName+"\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeBody(Body body) {
		createFolder(ResultPath+"Body/");
		String file = ResultPath+"Body/body.txt";
		createFile(file);
		try {
			FileWriter writer = new FileWriter(file,true);
			writer.write(body.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writePairs(Set<WidgetCallWindowModel> widget_to_windows) {
		createFolder(ResultPath);
		String file = ResultPath+"Widget_Windows.txt";
		createFile(file);
		for (WidgetCallWindowModel wc : widget_to_windows){
			try {
				FileWriter writer = new FileWriter(file,true);
				writer.write(wc.widgetName+"\t"+wc.callBackClassName+":"+wc.callBackMethodName+"\t"+wc.activity+"\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
