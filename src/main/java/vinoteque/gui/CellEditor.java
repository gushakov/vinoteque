package vinoteque.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.apache.log4j.Logger;

/**
 * 
 * @author George Ushakov
 */
public class CellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final Logger logger = Logger.getLogger(CellEditor.class);
    public static final Color BG_COLOR = new Color(255, 248, 220);

    @Override
    public boolean isCellEditable(EventObject e) {
        //set edit on double click
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() >= 2;
        } else {
            return super.isCellEditable(e);
        }
    }

    @Override
    public Object getCellEditorValue() {
        //subclasses must override
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //subclasses must override
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}
