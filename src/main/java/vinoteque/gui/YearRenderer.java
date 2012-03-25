package vinoteque.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Calendar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author George
 */
public class YearRenderer extends DefaultTableCellRenderer {
    private static final int now = Calendar.getInstance().get(Calendar.YEAR);
    private static final Color green = new Color(0, 153, 0);
    private static Color selectionForeground;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (selectionForeground == null){
            selectionForeground = table.getSelectionForeground();
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    @Override
    public void paint(Graphics g) {
        Color defaultForeground = getForeground();
        if (defaultForeground.getRGB() != selectionForeground.getRGB()){
            int annee = Integer.parseInt(getText());
            if (annee > 0){
                if (annee < now){
                    setForeground(Color.RED);
                }
                else if (annee == now){
                    setForeground(green);
                }
                else {
                    setForeground(Color.BLUE);
                }
            }            
        }
        super.paint(g);
        setForeground(defaultForeground);
    }

    

}
