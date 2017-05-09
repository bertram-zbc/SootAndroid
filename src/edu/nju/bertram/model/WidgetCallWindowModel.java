package edu.nju.bertram.model;

import presto.android.gui.graph.NNode;
import presto.android.gui.graph.NObjectNode;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by bertram on 17-4-29.
 * Record which windows lifecycle callback event will be invoked if the widget is used.
 */
public class WidgetCallWindowModel {

    public String widgetType;
    public String widgetIdNumber;
    public String widgetName;//注意这里其实是id不是变量名
    public String callBackClassName;
    public String callBackMethodName;
    public SootMethod callBackMethod;
    public String activity;//记录跳转的activity


    public WidgetCallWindowModel(NObjectNode window, NObjectNode widget, SootMethod sootMethod) {

        this.callBackMethod = sootMethod;

        getActivity(window.toString());
        getWidgetInfo(widget.toString());
        this.callBackClassName = sootMethod.getDeclaringClass().toString();
        this.callBackMethodName = sootMethod.getName();
//        System.out.println("[widgetType]"+this.widgetType);
//        System.out.println("[widgetIdNumber]"+this.widgetIdNumber);
//        System.out.println("[widgetName]"+this.widgetName);
//        System.out.println("[callbackClass]"+this.callBackClassName);
//        System.out.println("[callbackMethod]"+this.callBackMethodName);
    }

    private void getActivity(String s) {
        if(s.startsWith("ACT")){
            int start = s.indexOf("[");
            int end = s.indexOf("]");
            this.activity = s.substring(start+1,end);
        }
    }

    //get the infomation from widget
    private void getWidgetInfo(String s) {
        if (s.startsWith("INFL")) {
            int start = s.indexOf("[");
            int end = s.indexOf("]");
            String info = s.substring(start+1,end+1);
            String []array = info.split(",");
            if (!array[0].equals("*")) {
                this.widgetType = array[0];
            }
            if (!array[1].equals("*")) {
                this.widgetIdNumber = array[1].substring(array[1].indexOf("[") + 1, array[1].indexOf("|"));
                this.widgetName = array[1].substring(array[1].indexOf("|") + 1, array[1].indexOf("]"));
            }
        }
        //TODO 其他情况的处理

    }

}
