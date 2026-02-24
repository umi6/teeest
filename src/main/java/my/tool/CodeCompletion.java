package my.tool;

import processing.app.Base;
import processing.app.syntax.JEditTextArea;
import processing.app.tools.Tool;
import processing.app.ui.Editor;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;

public class CodeCompletion implements Tool {
    Base base;

    public void init(Base base) {
        // Initialization code here
        this.base = base;
    }

    public void run() {
        // Main functionality here
        Editor editor = base.getActiveEditor();
        if (editor == null) {
            System.out.println("editor is null");
            return;
        }

        JEditTextArea textArea = editor.getTextArea();
        int caret = textArea.getCaretPosition();
        String code = textArea.getText();
        textArea.setSelectedText("PVector");
        System.out.println("Caret position: " + caret);
        System.out.println("Code "+ code);
        System.out.println("START");
    }

    public String getMenuTitle() {
        // Title displayed in the Tools menu
        return "Code Completion";
    }
}
// _oo0oo_
// o8888888o
// 88" . "88
// (| -_- |)
// 0\ = /0
// ___/`---'\___
// .' \\| |// '.
// / \\||| : |||// \
// / _||||| -:- |||||- \
// | | \\\ - /// | |
// | \_| ''\---/'' |_/ |
// \ .-\__ '-' ___/-. /
// ___'. .' /--.--\ `. .'___
// ."" '< `.___\_<|>_/___.' >' "".
// | | : `- \`.;`\ _ /`;.`/ - ` : | |
// \ \ `_. \_ __\ /__ _/ .-` / /
// =====`-.____`.___ \_____/___.-`___.-'=====
// `=---='
//