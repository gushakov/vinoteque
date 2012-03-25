package vinoteque.gui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author George Ushakov
 */
public class TextEditor extends CellEditor {

    private JTextField textField;

    public TextEditor() {
        textField = new JTextField("");
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value!=null){
            textField.setText(value.toString());
        }
        textField.selectAll();
        return textField;
    }

}
