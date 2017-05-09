package edu.nju.bertram.analysis;

import edu.nju.bertram.model.LogicConstraints;
import edu.nju.bertram.model.Widget;
import edu.nju.bertram.model.WidgetCallWindowModel;
import soot.Body;
import soot.util.cfgcmd.CFGGraphType;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by hadoop on 17-4-29.
 */
public class Constraints {

    static CFGGraphType graphtype = CFGGraphType.getGraphType("BriefUnitGraph");

    public static void process(Widget widgetOnClick, Body body, ArrayList<Widget> widgetArray, Set<WidgetCallWindowModel> widget_to_windows) {

        System.out.println("dealing : "+widgetOnClick.name+"......");
        //得到顺序约束
        ArrayList<ArrayList<Widget>> sequenceConstrains = Sequence.getConstraints(graphtype,body,widgetArray);
        System.out.println("sequence:"+sequenceConstrains.size());
        if (sequenceConstrains.size()>0){
            //得到逻辑约束
            ArrayList<LogicConstraints> logicConstraintses = Logic.getConstraints(graphtype,body,widgetArray);
            System.out.println("logic:"+logicConstraintses.size());
            if (logicConstraintses.size()>0) {
                //将逻辑约束写入smt2文件用于z3求解器求解
                String filePath = "/home/hadoop/gator-3.2/SootAndroid/LogicConstraints/"+widgetOnClick.name+widgetOnClick.resource_id+"/";
                WriteToFile.writeLogic(logicConstraintses,filePath);
                System.out.println("Finish write stm2 file!");
                //运行Z3
                File file = new File(filePath);
                File[] fileList = file.listFiles();
                for (int i=0;i<fileList.length;i++){
                    ArrayList<Widget> z3ResultArray = z3Run.startRun(fileList[i],widgetArray);
                    String logicpath = WriteToFile.ResultPath + "logic/" + widgetOnClick.name+widgetOnClick.resource_id + "/";
                    String logicFileName = i+".txt";
                    WriteToFile.writeLogicResult(z3ResultArray,sequenceConstrains,widgetOnClick,logicpath,logicFileName);
                }
                System.out.println("Finish write logic file!");
            }else {
                //TODO
                String sequencepath = WriteToFile.ResultPath + "sequence/";
                String sequenceFileName = widgetOnClick.name+widgetOnClick.resource_id+".txt";
                WriteToFile.writeSequenceResult(sequenceConstrains,widgetOnClick,sequencepath,sequenceFileName);
                System.out.println("Finish write sequence file!");
            }
        } else {
            //没有约束的控件
            String noConstraintPath = WriteToFile.ResultPath;
            String noConstrainFileName = "noConstraints.txt";
            for (WidgetCallWindowModel wc : widget_to_windows){
                if (wc.widgetName!=null&&wc.activity!=null){
                    if (wc.activity.equals(widgetOnClick.activity)&&wc.widgetName.equals(widgetOnClick.name)) {
                        WriteToFile.writeNoLogicWidget(wc,noConstraintPath,widgetArray,noConstrainFileName);
                    }
                }
            }
        }




    }
}
