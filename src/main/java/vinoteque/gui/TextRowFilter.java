package vinoteque.gui;

import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import vinoteque.beans.Vin.Column;

/**
 * Table row filter. Used by {@linkplain javax.swing.table.TableRowSorter} to
 * filter only the rows where the row has matching string values in the
 * specified columns.
 *
 * @author George Ushakov
 */
public class TextRowFilter extends RowFilter<Object, Object> {

    private String text;
    private Column[] columns;

    public TextRowFilter(String text, Column[] columns) {
        this.text = text;
        this.columns = columns;
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean include(Entry<? extends Object, ? extends Object> entry) {
        boolean answer = false;
        if (text.equals("")) {
            answer = true;
        } else {
            for (int i = 0; i < columns.length; i++) {
                Column column = columns[i];
                String value = entry.getStringValue(column.index());
                if (value != null && !value.matches("\\s*")) {
                    //see if we are searching by a casier number                    
                    if (text.matches("\\d{1,2}")) {
                        if (value.equalsIgnoreCase(text)) {
                            answer = true;
                            break;
                        }
                    }
                    else{
                        if (value.toLowerCase().contains(text.toLowerCase())) {
                            answer = true;
                            break;
                        }                        
                    }
                }
            }
        }
        return answer;
    }
}