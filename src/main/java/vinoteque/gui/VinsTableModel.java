package vinoteque.gui;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import vinoteque.beans.Entry;
import vinoteque.beans.Vin;
import vinoteque.beans.Vin.Column;
import vinoteque.utils.Utils;

import static  vinoteque.beans.Vin.Column.*;

/**
 *
 * @author George Ushakov
 */
public class VinsTableModel extends DefaultTableModel {
    private static final Logger logger = Logger.getLogger(VinsTableModel.class);
    private Column[] columns;
    private List<Vin> allVins;
    private List<Vin> deletedVins;

    public VinsTableModel(Column[] columns, List<Vin> allVins) {
        this.columns = columns;
        this.allVins = allVins;
        deletedVins = new ArrayList<Vin>();
        Vector<String> columnNames = Utils.getColumnDisplayNames(columns);
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        for (Vin vin : allVins) {
            Vector<Object> rowData = new Vector<Object>();
            for (Column column : columns) {
                rowData.add(vin.getColumnValue(column));
            }
            data.add(rowData);
        }
        setDataVector(data, columnNames);
    }

    public Vin getVin(int rowIndex){
        return allVins.get(rowIndex);
    }

    public void reset(){
        deletedVins = new ArrayList<Vin>();
        for (Vin vin : allVins) {
            vin.setModified(false);
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        Vin vin = allVins.get(row);
        Object oldValue = null;
        Object newValue = null;
        if (column==DATE.index()){
            oldValue = vin.getDate();
            if (aValue instanceof Date){
                newValue = new Timestamp(((Date)aValue).getTime());
            }
            else {
                newValue = (Timestamp)aValue;
            }
        }
        else if (column==CASIER.index()){
            oldValue = vin.getCasier();
            newValue = (Integer)aValue;
        }
        else if (column==ANNEE.index()){
            oldValue = vin.getAnnee();
            newValue = (Integer)aValue;
        }
        else if (column==PAYS.index()){
            oldValue = vin.getPays();
            newValue = (String)aValue;
        }
        else if (column==REGION.index()){
            oldValue = vin.getRegion();
            if (aValue instanceof Entry){
                newValue = ((Entry)aValue).getName();
            }
            else {
                newValue = (String)aValue;
            }
        }
        else if (column==APPELLATION.index()){
            oldValue = vin.getAppellation();
            if (aValue instanceof Entry){
                newValue = ((Entry)aValue).getName();
            }
            else {
                newValue = (String)aValue;
            }
        }
        else if (column==VIGNERON.index()){
            oldValue = vin.getVigneron();
            if (aValue instanceof Entry){
                newValue = ((Entry)aValue).getName();
            }
            else {
                newValue = (String)aValue;
            }
        }
        else if (column==QUALITE.index()){
            oldValue = vin.getQualite();
            newValue = (String)aValue;
        }
        else if (column==STOCK.index()){
            oldValue = vin.getStock();
            newValue = (Integer)aValue;
        }
        else if (column==PRIX_BTL.index()){
            oldValue = vin.getPrixBtl();
            if (aValue instanceof Long){
                newValue = BigDecimal.valueOf(((Long)aValue).longValue());
            }
            else if (aValue instanceof Double){
                newValue = BigDecimal.valueOf(((Double)aValue).doubleValue());
            }
            else {
                newValue = (BigDecimal)aValue;
            }
        }
        else if (column==ANNEE_CONSOMMATION.index()){
            oldValue = vin.getAnneeConsommation();
            newValue = (Integer)aValue;
        }
        else {
            throw new UnsupportedOperationException("Unknown column " + column);
        }

        //check if the new data is different form the old data
        if (!(""+oldValue).equals(""+newValue)){
            //set the value in the data vector
            super.setValueAt(newValue, row, column);
            //update the corresponding vin object
            Column updateColumn = Column.values()[column];
            logger.debug("Updated vin with id " + vin.getId() + ", column " + updateColumn + ", new value: " + newValue);
            vin.setColumnValue(updateColumn, newValue);
            vin.setModified(true);
            fireTableDataChanged();
        }
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        Vin deleted = (Vin) allVins.remove(row);
        if (deleted.getId()!=-1){
            deletedVins.add(deleted);
        }
    }

    public int addNewVin(){
        int row = getDataVector().size();
        Vin vin = new Vin();
        vin.setDate(new Timestamp(new Date().getTime()));
        vin.setCasier(0);
        vin.setAnnee(0);
        vin.setPays("");
        vin.setRegion("");
        vin.setAppellation("");
        vin.setVigneron("");
        vin.setQualite("");
        vin.setStock(0);
        vin.setPrixBtl(new BigDecimal(0.0));

        allVins.add(vin);

        Vector<Object> rowData = new Vector<Object>();
        for (Column column : columns) {
            rowData.add(vin.getColumnValue(column));
        }
        addRow(rowData);
        return row;
    }

    public List<Vin> getAddedVins(){
        ArrayList<Vin> list = new ArrayList<Vin>();
        for (Vin vin : allVins) {
            if (vin.getId()==-1){
                list.add(vin);
            }
        }
        return list;
    }

     public List<Vin> getModifiedVins(){
        ArrayList<Vin> list = new ArrayList<Vin>();
        for (Vin vin : allVins) {
            if (vin.getId()!=-1 && vin.isModified()){
                list.add(vin);
            }
        }
        return list;
    }

    public List<Vin> getDeletedVins() {
        return deletedVins;
    }
}
