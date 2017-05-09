/**
 * ��ȡһ��CFG�е�˳��Լ������
 * 2017/4/1
 * 
 * ��ǰ��ȡ��ֻ����Ϊ����ȱ���CFGʱ�ؼ����ֵ�˳��Ϊ˳��Լ������
 * ������Щ�����������Ҫ����
 */
package edu.nju.bertram.analysis;


import java.util.ArrayList;
import java.util.List;

import edu.nju.bertram.model.Widget;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;

public class Sequence {
	

	static ArrayList<ArrayList<Widget>> recordArr = new ArrayList<ArrayList<Widget>>();
	static int deepth = 0;
	static int MAX_DEEP = 300;
	
	public static ArrayList<ArrayList<Widget>> getConstraints(CFGGraphType graphtype, Body body, ArrayList<Widget> widgetArray){

        deepth = 0;
		recordArr.clear();

		DirectedGraph<Unit> graph = graphtype.buildGraph(body);
		List<Unit> head = graph.getHeads();
		
//		List<Unit> tails = graph.getTails();
//		System.err.println("============tail==============");
//		System.out.println(graph.getSuccsOf(tails.get(0)));
	

		for(int i=0;i<head.size();i++){
			//List<Unit> temp = graph.getPredsOf(head.get(i));
			ArrayList<Widget> record = new ArrayList<Widget>();
			Widget w = IsContainWidget(head.get(i), widgetArray);
			if(w!=null){
				record.add(w);
			}
			DepthSearch(graph,head.get(i),record,widgetArray);
		}
		//System.err.println(recordArr.size());
		return recordArr;
	
	}

	private static void DepthSearch(DirectedGraph<Unit> graph, Unit unit,
			ArrayList<Widget> record, ArrayList<Widget> widgetArray) {

		List<Unit> next = graph.getSuccsOf(unit);

//		String s = "$r3 = $r0.<com.test.baidumap.RegisterActivity: android.widget.EditText userName>";
//		if(next.size()>0 && next.get(0).toString().equals(s)){
//			System.err.println("check point");
//		}

		deepth++;
		//设置遍历最大深度
		if (deepth==MAX_DEEP){
			return;
		}

		if(next.size()==0){

			if(!isExistArray(record)&&record.size()>0){
				ArrayList<Widget> temp = new ArrayList<Widget>();
				temp = (ArrayList<Widget>) record.clone();
				recordArr.add(temp);
			}
		}
		
		for(int i=0;i<next.size();i++){
			Widget w = IsContainWidget(next.get(i), widgetArray);
			if(w!=null){
				record.add(w);
				DepthSearch(graph, next.get(i), record, widgetArray);
				record.remove(record.size()-1);
			}else{
				DepthSearch(graph, next.get(i), record, widgetArray);
			}
		}
		
	}

	
	private static boolean isExistArray(ArrayList<Widget> record) {
		for(int i=0;i<recordArr.size();i++){
			if(isEqualArray(recordArr.get(i),record)){
				return true;
			}
		}
		return false;
	}


	private static boolean isEqualArray(ArrayList<Widget> arrayList,
			ArrayList<Widget> record) {
		if(arrayList.size() != record.size()){
			return false;
		}else{
			for(int i=0;i<record.size();i++){
				if(!arrayList.get(i).name.equals(record.get(i).name)){
					return false;
				}
			}
			return true;
		}
	}


	private static void printRecord(ArrayList<Widget> record) {
		// TODO Auto-generated method stub
		System.err.println("size:"+record.size());
		for(int i=0;i<record.size();i++){
			System.out.print(record.get(i).name+"\t");
		}
		System.out.println();
	}


	private static Widget IsContainWidget(Unit u, ArrayList<Widget> widgetArray){
		for(int i=0;i<widgetArray.size();i++){
			if(u.toString().contains(widgetArray.get(i).name.toString())){
				return widgetArray.get(i);
			}
		}
		return null;
	}

	
	
}
