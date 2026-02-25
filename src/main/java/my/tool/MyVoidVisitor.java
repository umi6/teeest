package my.tool;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import processing.app.syntax.JEditTextArea;

public class MyVoidVisitor extends VoidVisitorAdapter<String> {

    JEditTextArea textArea;
    String prefix;
    public boolean hasResult = false;
    int x = 0;
    int y = 0;

    public List<VariableInfo> variableInfoList = new ArrayList<>();
    public List<String> foundCandidates = new ArrayList<>();

    class VariableInfo {

        String type;
        String name;

        public VariableInfo(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    MyVoidVisitor(JEditTextArea textArea, String prefix) {
        this.textArea = textArea;
        this.prefix = prefix;
    }

    @Override
    public void visit(VariableDeclarator n, String arg) {
        String type = n.getType().toString();
        String name = n.getName().toString();
        if (n.getInitializer().isPresent() && n.getInitializer().get().toString().trim().equals("_DUMMY_")) {

            System.out.println("Found dummy variable!!");
            for (VariableInfo info : variableInfoList) {
                if (info.type.equals(type) && (prefix.isEmpty() || info.name.startsWith(prefix))) {
                    foundCandidates.add(info.name+" : "+info.type);
                }
            }
            calculatePopupPosition();
        } else {
            variableInfoList.add(new VariableInfo(type, name));
        }
        super.visit(n, arg);
    }

    public void calculatePopupPosition() {
        try {
            int caretOffset = textArea.getCaretPosition();
            int caretLine = textArea.getCaretLine();
            int caretColumn = caretOffset - textArea.getLineStartOffset(caretLine);
            x = textArea.offsetToX(caretLine, caretColumn);
            y = textArea.lineToY(caretLine) + textArea.getPainter().getLineHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
