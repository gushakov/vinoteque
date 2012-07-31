package vinoteque.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;

/**
 *
 * @author George Ushakov
 */
public class NumberEditor extends CellEditor {
    private JFormattedTextField fmtTextField;

    public NumberEditor() {
        fmtTextField = new JFormattedTextField(new java.text.DecimalFormat("##0"));
        fmtTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                    if (fmtTextField.isEditValid()){
                        try {
                            fmtTextField.commitEdit();
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
    }

    @Override
    public Object getCellEditorValue() {
        Integer intValue = null;
        Object value = fmtTextField.getValue();
        if (value != null) {
            if (value instanceof Long){
                intValue = ((Long)value).intValue();
            }
            else if (value instanceof Integer) {
                intValue = (Integer)value;
            }
            else {
                intValue = 0;
            }
        }
        else {
            intValue = 0;
        }
        return intValue;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value!=null){
            fmtTextField.setValue(value);
        }
        else {
            fmtTextField.setValue(new Integer(0));
        }
        fmtTextField.selectAll();
        return fmtTextField;
    }

    @Override
    public boolean stopCellEditing() {
        if (!fmtTextField.isEditValid()){
            boolean stopEdit = super.stopCellEditing();
            if (stopEdit){
                fireEditingStopped();
            }
            return stopEdit;
        }
        else {
            try {
                fmtTextField.commitEdit();
            } catch (ParseException ex) {
                //do nothing
            }
            return super.stopCellEditing();
        }
    }
}
