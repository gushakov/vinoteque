package vinoteque.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.EventObject;
import java.util.logging.Level;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.table.DatePickerCellEditor;

/**
 *
 * @author George Ushakov
 */
public class DateEditor extends DatePickerCellEditor {
    private static final Logger logger = Logger.getLogger(DateEditor.class);
    private EditWithFocusListener focusListener;
    private char editChar;

    public DateEditor(DateFormat dateFormat, EditWithFocusListener focusListener) {
        super(dateFormat);
        setClickCountToStart(2);
        final JFormattedTextField fmt = datePicker.getEditor();
        final DateFormat format = dateFormat;
        fmt.addKeyListener(new KeyAdapter() {
        @Override
            public void keyPressed(KeyEvent evt) {
                logger.debug(evt.getKeyCode());
                if (evt.getKeyCode()==KeyEvent.VK_ENTER
                        || evt.getKeyCode()==KeyEvent.VK_TAB){
                    
                    try {
                        format.parse(fmt.getText());
                        datePicker.commitEdit();
                        stopCellEditing();
                    } catch (ParseException e) {
                        cancelCellEditing();
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
                focusListener.editWithFocus(datePicker);
                return false;
            }
        }
        return super.isCellEditable(e);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JXDatePicker datePickerComponent = (JXDatePicker)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (editChar!=(char)0){
            datePickerComponent.getEditor().setText(String.valueOf(editChar));
            editChar = (char)0;
        }
        return datePickerComponent;
    }
    
}
