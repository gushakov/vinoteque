package vinoteque.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author George Ushakov
 */
public class ListEditor extends CellEditor {
    private JComboBox cmb;
    private EditWithFocusListener focusListener;
    private Object selectedItem;

    public ListEditor(String[] list, EditWithFocusListener focusListener) {
        cmb = new JComboBox(list);
        cmb.setEditable(false);
        cmb.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER){
                    //stop editing on enter
                    fireEditingStopped();
                }
            }

        });
        this.focusListener = focusListener;
        selectedItem = null;
    }

    @Override
    public boolean isCellEditable(EventObject e) {        
        if (e instanceof KeyEvent){
            KeyEvent evt = (KeyEvent)e;
            boolean isEditable = super.isCellEditable(e);
            if (isEditable){
                boolean selected = cmb.selectWithKeyChar(evt.getKeyChar());
                if (selected){
                    selectedItem = cmb.getSelectedItem();
                }
                focusListener.editWithFocus(cmb);
                return false;
            }
        }
        return super.isCellEditable(e);
    }

    @Override
    public Object getCellEditorValue() {
        return cmb.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (selectedItem!=null){
            cmb.getEditor().setItem(selectedItem);
            selectedItem = null;
            cmb.getEditor().selectAll();
        }
        else if (value!=null){
            cmb.getEditor().setItem(value);
        }
        else {
            cmb.getEditor().setItem("");
        }
        cmb.setBackground(CellEditor.BG_COLOR);
        return cmb;
    }
}
