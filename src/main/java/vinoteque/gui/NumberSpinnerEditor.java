package vinoteque.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.EventObject;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author George Ushakov
 */
public class NumberSpinnerEditor extends CellEditor {
    private JSpinner spinner;
    private EditWithFocusListener focusListener;
    private char editChar;

    public NumberSpinnerEditor(EditWithFocusListener focusListener) {
        spinner = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        final JFormattedTextField fmt = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        ((NumberFormat)((NumberFormatter)fmt.getFormatter()).getFormat()).setGroupingUsed(false);
        fmt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                    if (fmt.isEditValid()){
                        try {
                            spinner.commitEdit();
                        } catch (ParseException e) {
                            //do nothing
                        }
                    }
                    else {
                        fireEditingCanceled();
                    }
                }
            }
        });
        this.focusListener = focusListener;
        editChar = (char)0;
    }

    @Override
    public boolean isCellEditable(EventObject e) {        
        if (e instanceof KeyEvent){
            KeyEvent evt = (KeyEvent)e;
            boolean isEditable = super.isCellEditable(e);
            if (isEditable){                
                editChar = evt.getKeyChar();
                focusListener.editWithFocus(spinner.getEditor());
                return false;
            }
        }
        return super.isCellEditable(e);
    }

    @Override
    public Object getCellEditorValue() {
       return spinner.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (editChar != (char)0 && editChar >= '0' && editChar <= '9'){
            spinner.setValue(Integer.valueOf(String.valueOf(editChar)));
            editChar = (char)0;
        }
        else if (value != null){
            spinner.setValue(value);
        }
        else {
            spinner.setValue(0);
        }
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setBackground(CellEditor.BG_COLOR);
        return spinner;
    }

    @Override
    public boolean stopCellEditing() {
        JFormattedTextField fmt = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        if (!fmt.isEditValid()){
            boolean stopEdit = super.stopCellEditing();
            if (stopEdit){
                fireEditingCanceled();
            }
            return stopEdit;
        }
        else {
            try {
                fmt.commitEdit();
            } catch (ParseException ex) {
                //do nothing
            }
            return super.stopCellEditing();
        }
    }
}
