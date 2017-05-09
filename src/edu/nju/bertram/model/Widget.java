package edu.nju.bertram.model;

import presto.android.gui.graph.NObjectNode;

import java.util.ArrayList;

public class Widget {
	public String type;
	public String name;
	public String resource_id;
	public String inputValue;
	public String activity;
//	public Widget(String type, String name){
//		this.type = type;
//		this.name = name;
//		this.resource_id = "";
//		this.inputValue = "";
//	}
	
	public Widget(String type, String name, String id) {
		this.resource_id = id;
		this.type = type;
		this.name = name;
		this.inputValue = "";
	}
	
	public Widget(Widget widget) {
		this.resource_id = widget.resource_id;
		this.type = widget.type;
		this.name = widget.name;
		this.inputValue = widget.inputValue;
	}

//	public Widget(NObjectNode widget) {
//		String s = widget.toString();
//		int start = s.indexOf("[");
//		int end = s.indexOf("]");
//		String info = s.substring(start+1,end+1);
//		String []array = info.split(",");
//		this.type = array[0];
//		this.name = array[1].substring(array[1].indexOf("|")+1,array[1].indexOf("]"));
//		this.resource_id = array[1].substring(array[1].indexOf("[")+1,array[1].indexOf("|"));
//	}

	public Widget(String type, String name, String id, String activity) {
		this.resource_id = id;
		this.type = type;
		this.name = name;
		this.activity = activity;
	}

	public Widget(NObjectNode widget, NObjectNode window) {
		String s = widget.toString();
		System.out.println("[Widget]"+s);
		if (s.startsWith("INFL")){
			int start = s.indexOf("[");
			int end = s.indexOf("]");
			String info = s.substring(start+1,end+1);
			String []array = info.split(",");
			if (!array[0].equals("*")&&!array[1].equals("*")){
				this.type = array[0];
				this.name = array[1].substring(array[1].indexOf("|")+1,array[1].indexOf("]"));
				this.resource_id = array[1].substring(array[1].indexOf("[")+1,array[1].indexOf("|"));
			}
		}

		s = window.toString();
		System.out.println("[Widget]"+s);
		if(s.startsWith("ACT")){
			int start = s.indexOf("[");
			int end = s.indexOf("]");
			this.activity = s.substring(start+1,end);
		}

	}

	public void print(){
		System.err.println(this.name + "\t" + this.inputValue);
	}
	
	
	

	static public ArrayList<String> getWidgetTypeArray(){
		ArrayList<String> widgetType = new ArrayList<String>();
		widgetType.add("android.widget.TextView");
		widgetType.add("android.widget.EditText");
		widgetType.add("android.widget.Button");
		widgetType.add("android.widget.ImageButton");
		widgetType.add("android.widget.RadioButton");
		widgetType.add("android.widget.RadioGroup");
		widgetType.add("android.widget.CheckButton");
		widgetType.add("android.widget.ImageView");
		widgetType.add("android.widget.ProgressBar");
		return widgetType;
	}
	
}
