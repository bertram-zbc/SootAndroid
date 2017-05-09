package edu.nju.bertram.analysis;

import edu.nju.bertram.model.Widget;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class z3Run {

	private static String z3Addr = "/home/hadoop/z3-master/build/z3";

	public static ArrayList<Widget> startRun(File file, ArrayList<Widget> widgetArray) {
		
		System.out.println("start run "+file.getAbsolutePath());
		

		String cmd = z3Addr + " -smt2 "+file.getAbsolutePath();
		System.out.println(cmd);
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String lineStr = reader.readLine();
			if(lineStr.equals("sat")){
				ArrayList<Widget> result = new ArrayList<Widget>();
				while( (lineStr = reader.readLine()) != null ){
					//System.out.println(lineStr);
					for(int i=0;i<widgetArray.size();i++){
						if(lineStr.contains(widgetArray.get(i).name)){
							String value = reader.readLine().trim();
							value = value.substring(0, value.length()-1);
							//widgetArray.get(i).inputValue = value;
							Widget w = new Widget(widgetArray.get(i));
							w.inputValue = value;
							result.add(w);
						}
					}
				}
				reader.close();
				return result;
			}

		} catch (IOException e) {
			System.out.println("warning: z3 run "+file.getAbsolutePath()+" error!");
			e.printStackTrace();
		}
		return null;
		
	}

}
