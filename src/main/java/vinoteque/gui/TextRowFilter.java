package vinoteque.gui;

import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import vinoteque.beans.Vin.Column;


/**
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
        if (text.equals("")){
            answer = true;
        }
        else {
            for (Column column : columns) {
                String value = entry.getStringValue(column.index());
                if (value!=null && !value.matches("\\s*")
                        && value.toLowerCase().matches(".*"+text.toLowerCase()+".*")){
                        answer = true;
                        break;
                }
            }    
        }
        return answer;
    }
}
