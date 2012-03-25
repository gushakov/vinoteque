package vinoteque.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import vinoteque.beans.Entry;

/**
 *
 * @author George Ushakov
 */
public class EntryEditor extends CellEditor {
    private static final Logger logger = Logger.getLogger(EntryEditor.class);

    private JComboBox cmb;
    private EditWithFocusListener focusListener;

    public EntryEditor(List<Entry> entries, EditWithFocusListener focusListener) {
        String[] list = new String[entries.size()];
        int i = 0;
        for (Entry entry : entries) {
            list[i] = entry.getName();
            i++;
        }
        init(new DefaultComboBoxModel(list), focusListener);
    }

    public EntryEditor(ComboBoxModel model, EditWithFocusListener focusListener) {
        init(model, focusListener);
    }

    private void init(ComboBoxModel model, EditWithFocusListener focusListener){
        cmb = new JComboBox(model);
        cmb.setEditable(true);
        AutoCompleteDecorator.decorate(cmb);
        cmb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
               if ("comboBoxEdited".equals(evt.getActionCommand())){
                   //stop editing on enter
                   fireEditingStopped();
               }
            }
        });
        this.focusListener = focusListener;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof KeyEvent){
            KeyEvent evt = (KeyEvent)e;
            if (evt.getID()==KeyEvent.KEY_PRESSED){
                boolean isEditable = super.isCellEditable(e);
                if (isEditable){
                    //ask the focus listener to edit and set the focus on this component editor
                    final JTextField textField = (JTextField)cmb.getEditor().getEditorComponent();
                    textField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            textField.setText(String.valueOf(e.getKeyChar()));
                            textField.removeKeyListener(this);
                        }
                    });
                    focusListener.editWithFocus(textField);
                    return false;
                }
            }
        }        
        return super.isCellEditable(e);
    }

    @Override
    public Object getCellEditorValue() {
        return cmb.getEditor().getItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value!=null){
            cmb.getEditor().setItem(value);
        }
        else {
            cmb.getEditor().setItem("");
        }
        return cmb;
    }
}
