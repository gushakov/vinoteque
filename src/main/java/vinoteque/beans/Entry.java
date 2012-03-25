package vinoteque.beans;

import java.io.Serializable;
import vinoteque.utils.Utils;


/**
 *
 * @author George Ushakov
 */
public class Entry implements Serializable {

    private long timestamp;
    private boolean modified;
    private long id;
    private String name;
    private String column;

    public Entry() {
        timestamp =  Utils.getTimestamp();
        modified = false;
        id = -1;
    }

    @Override
    public boolean equals(Object obj) {
        boolean answer = false;
        if (obj instanceof Entry){
            Entry entry = (Entry) obj;
            //compare by ids if set
            if (id != -1 && entry.id != -1){
                if (id == entry.id){
                    answer = true;
                }
            }
            else {
                //otherwise compare by the timestamps
                if (timestamp == entry.timestamp){
                    answer = true;
                }
            }            
        }
        return answer;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
