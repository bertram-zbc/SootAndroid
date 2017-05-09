/**
 * ��ȡһ��CFG�е��߼�Լ������
 * 2017/4/7
 * 
 * ��Ҫ����if�������Ϊ���
 * Ŀǰ������Լ��ֻ��equal
 */
package edu.nju.bertram.analysis;

import edu.nju.bertram.model.LogicConstraints;
import edu.nju.bertram.model.LogicType;
import edu.nju.bertram.model.Widget;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Logic {
	
	static ArrayList<LogicConstraints> consArray = new ArrayList<LogicConstraints>();

	public static ArrayList<LogicConstraints> getConstraints(CFGGraphType graphtype, Body b,
			ArrayList<Widget> widgetArray) {

		consArray.clear();

		DirectedGraph<Unit> graph = graphtype.buildGraph(b);
		

		Iterator<Unit> units = graph.iterator();
		while(units.hasNext()){
			Unit u = units.next();
			String uStr = u.toString();
			if(uStr.startsWith("if")){
				Recall(u,graph,widgetArray);
			}
		}

		return consArray;
		
	}

	private static void Recall(Unit u, DirectedGraph<Unit> graph, ArrayList<Widget> widgetArray) {
		String factor = u.toString().split(" ")[1];
		List<Unit> pres = graph.getPredsOf(u);
		for(int i=0;i<pres.size();i++){
			if(pres.get(i).toString().startsWith(factor)){
				dealWithUnit(pres.get(i), graph, widgetArray);
			}else{
				Recall(pres.get(i), graph, widgetArray);
			}
		}
		
	}

	private static void dealWithUnit(Unit unit, DirectedGraph<Unit> graph, ArrayList<Widget> widgetArray) {

		String u = unit.toString().trim();
		if (!u.contains("invoke")){
			return;
		}
		int index = u.indexOf("invoke");
		String key = u.substring(index+7,u.length());
		index = key.indexOf(".");
		String param1 = key.substring(0, index);
		//System.out.println(param1);
		Widget w1 = findWidget(param1,unit,graph,widgetArray);

		int startIndex = key.indexOf("<");
		int endIndex = key.indexOf(">");
		String func = key.substring(startIndex+1, endIndex);

		
		index = key.indexOf(">");
		String temp = key.substring(index+1);
		startIndex = temp.indexOf("(");
		endIndex = temp.indexOf(")");
		String param2 = temp.substring(startIndex+1, endIndex);
		//System.out.println(param2);
		if(param2.startsWith("$")){
			Widget w2 = findWidget(param2, unit, graph, widgetArray);
			if(w1!=null && w2!=null){
				LogicConstraints lc = new LogicConstraints(w1, w2);
				if(!isContainLogicConstraints(lc)){
					consArray.add(lc);
				}
			}
		}else{
			if (w1!=null&&param2!=null){
				LogicConstraints lc = new LogicConstraints(w1, param2);
				if(!isContainLogicConstraints(lc)){
					consArray.add(lc);
				}
			}
		}
		
	}

	private static boolean isContainLogicConstraints(LogicConstraints lc) {

		System.out.println("[isConstainLogicConstraints()]\t"+lc.param1+"\t"+lc.param2);

		for(int i=0;i<consArray.size();i++){
			if(lc.type == consArray.get(i).type){
				if(lc.type == LogicType.WIDGET_WIDGET){
					Widget w1 = (Widget) lc.param1;
					Widget w2 = (Widget) lc.param2;
					Widget w3 = (Widget) consArray.get(i).param1;
					Widget w4 = (Widget) consArray.get(i).param2;
					if (w1.name==null||w2.name==null){
						return true;
					}else{
						if(w1.name.equals(w3.name) && w2.name.equals(w4.name) ){
							return true;
						}
					}
				}else if(lc.type == LogicType.WIDGET_STRING){
					Widget w1 = (Widget) lc.param1;
					String s1 = (String) lc.param2;
					Widget w2 = (Widget) consArray.get(i).param1;
					String s2 = (String) consArray.get(i).param2;
					if (w1.name==null||s1==null){
						return true;
					}else{
						if(w1.name.equals(w2.name) && s1.equals(s2)){
							return true;
						}
					}
				}else if(lc.type == LogicType.STRING_WIDGET){
					String s1 = (String) lc.param1;
					Widget w1 = (Widget) lc.param2;
					String s2 = (String) consArray.get(i).param1;
					Widget w2 = (Widget) consArray.get(i).param2;
					if (s1==null||w1.name==null){
						return true;
					}else {
						if(s1.equals(s2) && w1.name.equals(w2.name)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private static Widget findWidget(String param, Unit unit, DirectedGraph<Unit> graph,
			ArrayList<Widget> widgetArray) {
		
		List<Unit> pres = graph.getPredsOf(unit);
		for(int i=0;i<pres.size();i++){
			String u = pres.get(i).toString().trim();
			if(u.startsWith(param)){
				Widget w = isContainWidget(pres.get(i),widgetArray);
				if(w!=null){
					//System.err.println(pres.get(i).toString()+"\t"+w.name+"\t"+w.resource_id);
					return w;
				}else{
					int index = u.indexOf("invoke");
					String key = u.substring(index+7,u.length());
					index = key.indexOf(".");
					String param1 = key.substring(0, index);
					return findWidget(param1, pres.get(i), graph, widgetArray);
				}
			}else{
				return findWidget(param, pres.get(i), graph, widgetArray);
			}
		}
		return null;
	}

	private static Widget isContainWidget(Unit unit,
			ArrayList<Widget> widgetArray) {
		
		String u = unit.toString();
		for(int i=0;i<widgetArray.size();i++){
			if(u.contains(widgetArray.get(i).name)){
				return widgetArray.get(i);
			}
		}
		
		return null;
	}

	

}
