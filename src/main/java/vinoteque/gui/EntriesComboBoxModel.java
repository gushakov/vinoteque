package vinoteque.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.apache.log4j.Logger;
import vinoteque.beans.Entry;
import vinoteque.beans.Vin.Column;

/**
 *
 * @author George Ushakov
 */
public class EntriesComboBoxModel extends DefaultComboBoxModel {
    private static final Logger logger = Logger.getLogger(EntriesComboBoxModel.class);

    public List<Entry> deletedEntries;

    public EntriesComboBoxModel(List<Entry> entries) {
        super(entries.toArray(new Entry[]{}));
        deletedEntries = new ArrayList<Entry>();
    }

    public void reset(){
        deletedEntries = new ArrayList<Entry>();
        for (int i = 0; i < getSize(); i++) {
            Entry entry = (Entry)getElementAt(i);
            entry.setModified(false);
        }
    }

    public int addEntry(Column column, String name) {
        int index = -1;
        //check if an entry with the same name exists
        if (indexOfEntry(name)<0){
            logger.debug("Adding entry " + name + ", column " + column);
            Entry entry = new Entry();
            entry.setColumn(column.name());
            entry.setName(name);
            //insert element before the first alphabetically preceeding or equal entry,
            //this is needed to keep the entries sorted
            for (int i = 0; i < getSize(); i++) {
                Entry anotherEntry = (Entry) getElementAt(i);
                if (entry.getName().compareTo(anotherEntry.getName())<=0){
                    insertElementAt(entry, i);
                    index = i;
                    break;
                }
            }
            //just add element at the end
            if (index<0){
                addElement(entry);
            }
        }
        return index;
    }

    public int indexOfEntry(String name){
        int index = -1;
        for (int i = 0; i < getSize(); i++) {
            Entry next = (Entry)getElementAt(i);
            if (next.getName().equalsIgnoreCase(name)){
                index = i;
                break;
            }
        }
        return index;
    }

    public int findFirstSimilarEntry(String text){
        int index = -1;
        for (int i = 0; i < getSize(); i++) {
            Entry next = (Entry)getElementAt(i);
            if (next.getName().toLowerCase().matches("^"+text.toLowerCase()+".*$")){
                index = i;
                break;
            }
        }        
        return index;
    }

    public boolean modifyEntryAt(int index, String name){
        boolean modified = false;
        if (indexOfEntry(name)<0){
            Entry entry = (Entry) getElementAt(index);
            if (!entry.getName().equals(name)){
                logger.debug("Modifying entry " + entry.getName() + ", column " + entry.getColumn());
                entry.setName(name);
                entry.setModified(true);
                fireContentsChanged(this, index, index);
            }
        }
        return modified;
    }

    public void deleteEntryAt(int index){
        Entry entry = (Entry) getElementAt(index);
        logger.debug("Deleting entry " + entry.getName() + ", column " + entry.getColumn());
        removeElementAt(index);
        if (entry.getId()!=-1){
            deletedEntries.add(entry);
        }
    }

    public List<Entry> getAddedEntries(){
        List<Entry> list = new ArrayList<Entry>();
        for (int i = 0; i < getSize(); i++) {
            Entry entry = (Entry) getElementAt(i);
            if (entry.getId()==-1){
                list.add(entry);
            }
        }
        return list;
    }

    public List<Entry> getModifiedEntries(){
        List<Entry> list = new ArrayList<Entry>();
        for (int i = 0; i < getSize(); i++) {
            Entry entry = (Entry) getElementAt(i);
            if (entry.getId()!=-1 && entry.isModified()){
                list.add(entry);
            }
        }
        return list;
    }

    public List<Entry> getDeletedEntries() {
        return deletedEntries;
    }

    public void setDeletedEntries(List<Entry> deletedEntries) {
        this.deletedEntries = deletedEntries;
    }
}
