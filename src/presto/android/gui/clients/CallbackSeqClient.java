package presto.android.gui.clients;

import edu.nju.bertram.analysis.Constraints;
import edu.nju.bertram.analysis.WriteToFile;
import edu.nju.bertram.config.GetAllWidgets;
import edu.nju.bertram.model.Widget;
import edu.nju.bertram.model.WidgetCallWindowModel;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.listener.EventType;
import presto.android.gui.wtg.EventHandler;
import presto.android.gui.wtg.WTGAnalysisOutput;
import presto.android.gui.wtg.WTGBuilder;
import presto.android.gui.wtg.ds.WTG;
import presto.android.gui.wtg.ds.WTGEdge;
import soot.Body;
import soot.SootMethod;

import java.io.File;
import java.util.*;

public class CallbackSeqClient implements GUIAnalysisClient {

	private GUIAnalysisOutput guiOutput;

	@Override
	public void run(GUIAnalysisOutput guiOutput) {

		this.guiOutput = guiOutput;
		// construct the WTGBuilder, used to build WTG
		WTGBuilder wtgBuilder = new WTGBuilder();
		wtgBuilder.build(guiOutput);
		// build WTGAnalysisOutput, which provides access to WTG and other
		// related functionalities
		WTGAnalysisOutput wtgOutput = new WTGAnalysisOutput(this.guiOutput, wtgBuilder);

		WTG wtg = wtgOutput.getWTG();
		Helper helper = new Helper();
		helper.getAllHandlers(wtg);
		helper.buildCCFG(wtg);
        helper.recordPairs();
		//helper.processEventHandler();

	}

	class Helper {
		Set<EventHandler> all_handlers_ = new HashSet<EventHandler>();
		Map<EventHandler, Set<EventHandler>> ccfg_ = new HashMap<EventHandler, Set<EventHandler>>();

        Set<WidgetCallWindowModel> widget_to_windows = new HashSet<>();
        ArrayList<Widget> widgetArray = null;

		// Get all event handlers including windows and widget callbacks.
		// Input: wtg
		public void getAllHandlers(WTG wtg) {
			for (WTGEdge edge : wtg.getEdges()) {
				this.all_handlers_.addAll(edge.getWTGHandlers());
				this.all_handlers_.addAll(edge.getCallbacks());
			}
		}

		public void addCallbackInNextEdge(EventHandler src, WTGEdge next) {
			if (!ccfg_.containsKey(src))
				ccfg_.put(src, new HashSet<EventHandler>());
			
			boolean hasWidgetCallback = false;
			// Try to add any widget handler.
			for (EventHandler handler : next.getWTGHandlers()) {
				if (handler.getEventHandler() == null)
					continue;
				hasWidgetCallback = true;
				ccfg_.get(src).add(handler);
			}
			// If no widget handler, add the first window callback if any.
			if ((!hasWidgetCallback) && (!next.getCallbacks().isEmpty())) {
				ccfg_.get(src).add(next.getCallbacks().get(0));
			}
		}

		// Build the callback pairs.
		// Input: wtg
	public void buildCCFG(WTG wtg){
      for(WTGEdge edge : wtg.getEdges()){
        for(EventHandler handler : edge.getWTGHandlers()){
          if(edge.getCallbacks().isEmpty())
            continue;
          if(!ccfg_.containsKey(handler)){
            ccfg_.put(handler, new HashSet<EventHandler>());
          }
          if (handler.getEventHandler() != null)
          ccfg_.get(handler).add(edge.getCallbacks().get(0));
          for(int i=0;i<edge.getCallbacks().size()-1;i++){
            if(!ccfg_.containsKey(edge.getCallbacks().get(i))){
              ccfg_.put(edge.getCallbacks().get(i), new HashSet<EventHandler>());
            }
            ccfg_.get(edge.getCallbacks().get(i)).add(edge.getCallbacks().get(i+1));
          }
        }
      }
      
      for(WTGEdge src : wtg.getEdges()){
    	  for(WTGEdge tgt : wtg.getEdges()){
    		  if(!(src.getTargetNode().equals(tgt.getSourceNode()))){
    			  continue;
    		  }
    		  // If src -> tgt
    		  // If src has any window callback, use the last of it.
    		  if(!src.getCallbacks().isEmpty()){
    			  EventHandler last_from_src = src.getCallbacks().get(src.getCallbacks().size()-1);
    			  addCallbackInNextEdge(last_from_src, tgt);
    		  }else{ // Else use the widget callbacks.
    			  for(EventHandler handler : src.getWTGHandlers()){
    				  if(handler.getEventHandler() == null)
    					  continue;
    				  addCallbackInNextEdge(handler, tgt);
    			  }
    		  }
    	  }
      }
    }

		// Check if a handler is widget callback.
		// Input: EventHandler
		// Output: boolean
		public boolean isWidgetEventHandlerCallback(EventHandler handler) {

			EventType handlerEventType = handler.getEvent();
			boolean isImplicityEvt = false;
			if (handlerEventType.equals(EventType.implicit_async_event)
					|| handlerEventType.equals(EventType.implicit_back_event)
					|| handlerEventType.equals(EventType.implicit_create_context_menu)
					|| handlerEventType.equals(EventType.implicit_hierarchy_change)
					|| handlerEventType.equals(EventType.implicit_home_event)
					|| handlerEventType.equals(EventType.implicit_launch_event)
					|| handlerEventType.equals(EventType.implicit_lifecycle_event)
					|| handlerEventType.equals(EventType.implicit_on_activity_newIntent)
					|| handlerEventType.equals(EventType.implicit_on_activity_result)
					|| handlerEventType.equals(EventType.implicit_power_event)
					|| handlerEventType.equals(EventType.implicit_rotate_event)
					|| handlerEventType.equals(EventType.implicit_system_ui_change)
					|| handlerEventType.equals(EventType.implicit_time_tick)) {
				isImplicityEvt = true;
			}
			return !isImplicityEvt;
		}

		// Check if a handler is window callback.
		// Input: EventHandler
		// Output: boolean
		public boolean isWindowLifecycleCallback(EventHandler handler) {
			return !isWidgetEventHandlerCallback(handler);
		}

		// Print total number of handlers and the number of each type of
		// handlers, respectively.
		public void printStatistics() {
			int widgetCallbackNum = 0;
			int windowCallbackNum = 0;
			for (EventHandler handler : this.all_handlers_) {
				if (this.isWidgetEventHandlerCallback(handler))
					widgetCallbackNum++;
				if (this.isWindowLifecycleCallback(handler))
					windowCallbackNum++;
			}
			System.out.println("Total: " + this.all_handlers_.size());
			System.out.println("GUI widget EventHandler: " + widgetCallbackNum);
			System.out.println("Window lifecycle EventHandler: " + windowCallbackNum);
		}

		// Print all callback pairs in WTG.
		public void printPairs() {
			for (EventHandler src : this.ccfg_.keySet()) {
				for (EventHandler tgt : this.ccfg_.get(src)) {
					//System.out.println(src.getEventHandler() + " -> " + tgt.getEventHandler());
					if(isWidgetEventHandlerCallback(src)&&isWindowLifecycleCallback(tgt)){
						System.err.println(src.getWidget().toString()+" -> "+tgt.getEventHandler());

					}
				}
			}
		}

		public void printInfo() {
			for(EventHandler src : this.all_handlers_){
				if(isWidgetEventHandlerCallback(src)){
					System.out.println("*****************************");
					System.out.println(src.getWidget().id+"\t"+src.getWidget().toString());
					SootMethod sootMethod = src.getEventHandler();
					Body b = sootMethod.retrieveActiveBody();
					System.out.println(b);

				}
			}
		}

		//记录控件与其回调的窗口
        public void recordPairs(){
            for (EventHandler src : this.ccfg_.keySet()) {
                for (EventHandler tgt : this.ccfg_.get(src)) {
                    if (isWidgetEventHandlerCallback(src) && isWindowLifecycleCallback(tgt)) {
                        if (src.getEventHandler().toString().contains("onClick")){
                            WidgetCallWindowModel wcwm = new WidgetCallWindowModel(src.getWindow(),src.getWidget(),tgt.getEventHandler());
                            if(!isExist(wcwm,widget_to_windows)){
                                widget_to_windows.add(wcwm);
                            }
                        }
                    }
                }
            }
//            System.out.println("****************************");
//            for (WidgetCallWindowModel wc : widget_to_windows){
//                System.out.println(wc.activity+"\t"+wc.widgetType+"\t"+wc.widgetName+"\t"+wc.callBackClassName+":"+wc.callBackMethodName);
//            }
            WriteToFile.writePairs(widget_to_windows);
			System.out.println("Finish record widgets->windows pairs!");
        }

        private boolean isExist(WidgetCallWindowModel wcwm, Set<WidgetCallWindowModel> widget_to_windows) {
            for (WidgetCallWindowModel wc : widget_to_windows){
                if(wc.widgetName!=null&&wc.callBackClassName!=null&&wc.callBackMethodName!=null){
                    if(wc.widgetName.equals(wcwm.widgetName)
                            &&wc.callBackClassName.equals(wcwm.callBackClassName)
                            &&wc.callBackMethodName.equals(wcwm.callBackMethodName)){
                        return true;
                    }
                }
            }
            return false;
        }

        //获取所有带回调的控件
//        public void getAllWidgets(){
//            for (EventHandler src: this.all_handlers_){
//                if(isWidgetEventHandlerCallback(src)){
//                    NObjectNode widget = src.getWidget();
//                    Widget w = new Widget(widget);
//                    widgetArray.add(w);
//                }
//            }
//        }

        //处理控件回调，提取约束关系
        public void processEventHandler(){
			//初始化
			init();
			//获取所有控件
			widgetArray = GetAllWidgets.getFromFile();

            for (EventHandler src: this.all_handlers_){
                if(isWidgetEventHandlerCallback(src)){
					Widget widgetOnClick = new Widget(src.getWidget(),src.getWindow());
                    Body body = src.getEventHandler().retrieveActiveBody();
                    Constraints.process(widgetOnClick,body,widgetArray,widget_to_windows);
                }
            }
        }

		private void init() {
			//删除Output文件夹
			String outputPath = WriteToFile.ResultPath;
			File outputFile = new File(outputPath);
			deleteFile(outputFile);
            String logicPath = "/home/hadoop/gator-3.2/SootAndroid/LogicConstraints/";
            File logicFile = new File(logicPath);
            deleteFile(logicFile);
            System.out.println("Finish delete files!");
		}

		private void deleteFile(File file) {
			if (file.exists()){
				if (file.isFile()){
					file.delete();
				}else if (file.isDirectory()){
					File []files = file.listFiles();
					for (int i=0;i<files.length;i++){
						deleteFile(files[i]);
					}
					file.delete();
				}
			}
		}


	}
}
