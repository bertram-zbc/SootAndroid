package edu.nju.bertram.config;

import edu.nju.bertram.model.Widget;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by hadoop on 17-5-2.
 * 从文件中读取控件信息
 */
public class GetAllWidgets {

    static String fileName = "/home/hadoop/gator-3.2/SootAndroid/widgetFile/widget_ziyouxing.txt";

    public static ArrayList<Widget> getFromFile() {

        ArrayList<Widget> widgetArray = new ArrayList<>();

        File file = new File(fileName);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine())!=null) {
                String []s = tempString.split("\t");
                Widget w = new Widget(s[0],s[1],s[2],s[3]);
                widgetArray.add(w);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("widget file missing at:"+fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return widgetArray;
    }

    public static void main(String []args){
        ArrayList<Widget> widgetArray = GetAllWidgets.getFromFile();
        System.out.println(widgetArray.size());
        System.out.println(widgetArray.get(1).activity);
    }

}

