package my.tool;

import processing.app.Base;
import processing.app.tools.Tool;

public class ProcessingToolTemplate implements Tool {
    public void init(Base base) {
        // Initialization code here
    }

    public void run() {
        // Main functionality here
        System.out.println("Tool is running!");
    }

    public String getMenuTitle() {
        // Title displayed in the Tools menu
        return "Processing Tool Template";
    }
}