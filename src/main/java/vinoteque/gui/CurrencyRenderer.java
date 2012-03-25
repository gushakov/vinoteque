package vinoteque.gui;

import java.awt.Component;
import java.math.BigDecimal;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import vinoteque.beans.Vin;

/**
 *
 * @author George Ushakov
 */
public class CurrencyRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value!=null){
            if (value instanceof BigDecimal){
                BigDecimal currency = (BigDecimal)value;
                label.setText(Vin.currencyFormat.format(currency.doubleValue()));
            }
            else {
                label.setText(Vin.currencyFormat.format(new BigDecimal(value.toString())));
            }
        }
        return label;
    }

}
