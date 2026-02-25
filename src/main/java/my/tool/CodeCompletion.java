package my.tool;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import processing.app.Base;
import processing.app.syntax.JEditTextArea;
import processing.app.tools.Tool;
import processing.app.ui.Editor;

public class CodeCompletion implements Tool {

    Base base;
    JEditTextArea textArea;
    Editor editor;
    boolean isRunning = true;

    Set<Editor> attachedEditors = new HashSet<>();

    private JPopupMenu completionPopup;// 補完候補を表示するポップアップ
    private JList<String> suggestionList;
    private String currentPrefix = "";

    @Override
    public void init(Base base) {
        this.base = base;
        //ポップアップの設定
        completionPopup = new JPopupMenu();
        completionPopup.setFocusable(false);
        completionPopup.setBorder(BorderFactory.createEmptyBorder());

        // 候補リストの設定
        suggestionList = new JList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //スクロールできるようにする
        JScrollPane scroll = new JScrollPane(suggestionList);
        completionPopup.add(scroll);

        //キーイベントをprocessingから奪う
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (completionPopup.isVisible() && textArea != null && textArea.hasFocus()) {
                    int code = e.getKeyCode();
                    char keyChar = e.getKeyChar();

                    boolean isNavigationKey = (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP
                            || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB
                            || code == KeyEvent.VK_ESCAPE
                            || keyChar == '\n' || keyChar == '\t' || keyChar == 27 // 27 is ESC
                            );

                    if (isNavigationKey) {
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            if (code == KeyEvent.VK_DOWN) {
                                int next = suggestionList.getSelectedIndex() + 1;
                                if (next < suggestionList.getModel().getSize()) {
                                    suggestionList.setSelectedIndex(next);
                                    suggestionList.ensureIndexIsVisible(next);
                                }
                            } else if (code == KeyEvent.VK_UP) {
                                int prev = suggestionList.getSelectedIndex() - 1;
                                if (prev >= 0) {
                                    suggestionList.setSelectedIndex(prev);
                                    suggestionList.ensureIndexIsVisible(prev);
                                }
                            } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB || keyChar == '\n' || keyChar == '\t') {
                                confirmSelection(textArea);
                            } else if (code == KeyEvent.VK_ESCAPE || keyChar == 27) {
                                closePopup(textArea);
                            }
                        }
                        return true;
                    }

                    if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            closePopup(textArea);
                        }
                        return false;
                    }
                }
                return false;
            }
        });

        //エディタを監視し、run()が呼ばれなくても補完できるようにする
        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(this::checkAndAttach);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    //エディタが変わったときに補完機能を新しいエディタにアタッチする
    private void checkAndAttach() {
        Editor activeEditor = base.getActiveEditor();
        if (activeEditor != null && !attachedEditors.contains(activeEditor)) {
            attachListener(activeEditor);
            attachedEditors.add(activeEditor);
        }
    }

    //補完候補が選択されたときの処理
    private void confirmSelection(JEditTextArea textArea) {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            String insertText = selected.split(" : ")[0];
            replaceText(textArea, insertText);
            closePopup(textArea);
        }
    }

    //ポップアップを閉じる
    private void closePopup(JEditTextArea textArea) {
        completionPopup.setVisible(false);
    }

    //エディターにキーリスナーをアタッチし、特定のキーイベントで補完処理を呼び出す
    private void attachListener(Editor editor) {
        JEditTextArea editorTextArea = editor.getTextArea();

        editorTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();

                //ナビゲーションキーはinit()で書いたためここでは処理しない
                if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP
                        || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT
                        || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB
                        || code == KeyEvent.VK_ESCAPE) {
                    return;
                }
                //補完処理を呼び出す
                if (completionPopup.isVisible() || Character.isLetterOrDigit(e.getKeyChar()) || code == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == '=' || e.getKeyChar() == ' ' || e.getKeyChar() == '.') {
                    runCompletion(editor, editorTextArea);
                }
            }

            //ctrl spaceでも補完できるようにする
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                    runCompletion(editor, editorTextArea);
                }
            }
        });
    }

    @Override
    public void run() {
        Editor activeEditor = base.getActiveEditor();
        if (activeEditor == null) {
            System.out.println("editor is null");
            return;
        }
        runCompletion(activeEditor, activeEditor.getTextArea());
    }

    public void runCompletion(Editor editor, JEditTextArea targetTextArea) {
        this.textArea = targetTextArea;
        this.editor = editor;

        String rawCode = textArea.getText();
        int caretPos = textArea.getCaretPosition();

        //テキストカーソルの前の部分の文字を取得
        currentPrefix = getPrefixAt(rawCode, caretPos);

        //テキストカーソルの位置に_DUMMY_を挿入
        StringBuilder sb = new StringBuilder(rawCode);
        sb.replace(caretPos - currentPrefix.length(), caretPos, " _DUMMY_;");
        boolean hasMethod = rawCode.matches("(?s).*\\b(void|int|float|String|boolean|char)\\s+[a-zA-Z0-9_]+\\s*\\(.*");

        try {
            //コードをパースして、_DUMMY_と同じ型の変数を探す
            CompilationUnit unit;
            if (hasMethod) {
                unit = StaticJavaParser.parse("class Sketch extends PApplet {" + sb.toString() + "}");
            } else {
                 unit = StaticJavaParser.parse("class Sketch extends PApplet { void example(){" + sb.toString() + "}}");
            }
            MyVoidVisitor visitor = new MyVoidVisitor(textArea, currentPrefix);
            unit.accept(visitor, null);

            updatePopup(textArea, visitor.foundCandidates, visitor.x, visitor.y);

        } catch (Exception e) {
            //パースエラーが起きたらポップアップを閉じる
            if (completionPopup.isVisible() && currentPrefix.isEmpty()) {
                closePopup(textArea);
            }
        }
    }

    private void updatePopup(JEditTextArea textArea, List<String> candidates, int x, int y) {
        if (candidates.isEmpty()) {
            closePopup(textArea);
            return;
        }

        //候補をリストにセットして表示
        suggestionList.setListData(candidates.toArray(new String[0]));
        suggestionList.setSelectedIndex(0);

        //表示する最大行数を制限
        int visibleRows = Math.min(candidates.size(), 10);

        //ポップアップのサイズを調整
        suggestionList.setVisibleRowCount(visibleRows);

        completionPopup.pack();

        if (!completionPopup.isVisible()) {
            completionPopup.show(textArea, x, y);
        }
    }

    //選択された候補でテキストを置き換える
    private void replaceText(JEditTextArea textArea, String candidate) {
        try {
            int caret = textArea.getCaretPosition();
            textArea.select(caret - currentPrefix.length(), caret);
            textArea.setSelectedText(candidate);
        } catch (Exception e) {
        }
    }

    //テキストカーソルの前の部分の文字を取得する
    private String getPrefixAt(String code, int caretPos) {
        if (caretPos == 0) {
            return "";
        }
        int start = caretPos - 1;
        while (start >= 0) {
            char c = code.charAt(start);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            start--;
        }
        return code.substring(start + 1, caretPos);
    }

    @Override
    public String getMenuTitle() {
        return "Code Completion";
    }
}
