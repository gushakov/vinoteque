package vinoteque.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import vinoteque.beans.Vin;


/**
 *
 * @author George Ushakov
 */
public class DateRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value!=null){
            if (value instanceof Timestamp){
                Timestamp date = (Timestamp)value;
                label.setText(Vin.dateFormat.format(date));
            }
            else if (value instanceof Date){
                Date date = (Date)value;
                label.setText(Vin.dateFormat.format(date));
            }
            else {
                label.setText(value.toString());
            }
        }
        else {
            label.setText("");
        }
        return label;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }


}
