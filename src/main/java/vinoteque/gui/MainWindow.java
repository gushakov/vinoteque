/*
 * MainWindow.java
 *
 * Created on 20 sept. 2010, 11:53:36
 */
package vinoteque.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import vinoteque.beans.Entry;
import vinoteque.beans.Vin;
import vinoteque.beans.Vin.Column;
import static vinoteque.beans.Vin.Column.*;
import vinoteque.config.AppConfig;
import vinoteque.db.HsqldbDao;
import vinoteque.utils.Utils;

/**
 * Main JFame for the vinoteque application.
 * @author George Ushakov
 */
public class MainWindow extends javax.swing.JFrame 
        implements ListSelectionListener, EditWithFocusListener, PropertyChangeListener,
                   PreferencesChangeListener {

    private static final Logger logger = Logger.getLogger(MainWindow.class);
    private Properties props;
    private Properties appProps;
    private ApplicationContext applicationContext;
    private String appVersion;
    private HsqldbDao dao;
    private VinsTableModel tableModel;
    private TableRowSorter tableRowSorter;
    private EntriesComboBoxModel regionsModel;
    private EntriesComboBoxModel appellationsModel;
    private EntriesComboBoxModel vigneronsModel;    
    
    class SaveTask extends SwingWorker<Void, Void> {
        private boolean onExit;
        public SaveTask(boolean onExit) {
            this.onExit = onExit;
        }
        @Override
        protected Void doInBackground() throws Exception {
            try {
                setProgress(5);
                //remove deleted entries
                List<Entry> deletedEntries = regionsModel.getDeletedEntries();
                deletedEntries.addAll(appellationsModel.getDeletedEntries());
                deletedEntries.addAll(vigneronsModel.getDeletedEntries());
                if (deletedEntries.size() > 0){
                    logger.debug("Deleting " + deletedEntries.size() + " entries from the database");
                    dao.deleteEntries(deletedEntries);
                }
                setProgress(15);
                //add new entries
                List<Entry> addedEntries = regionsModel.getAddedEntries();
                addedEntries.addAll(appellationsModel.getAddedEntries());
                addedEntries.addAll(vigneronsModel.getAddedEntries());
                if (addedEntries.size() > 0){
                    logger.debug("Adding " + addedEntries.size() + " entries to the database");
                    for (Entry entry : addedEntries) {
                        long id = dao.addEntry(entry);
                        entry.setId(id);
                        entry.setModified(false);
                    }
                }
                setProgress(30);
                //update modified entries
                List<Entry> modifiedEntries = regionsModel.getModifiedEntries();
                modifiedEntries.addAll(appellationsModel.getModifiedEntries());
                modifiedEntries.addAll(vigneronsModel.getModifiedEntries());
                if (modifiedEntries.size() > 0){
                    logger.debug("Modifying " + modifiedEntries.size() + " entries in the  database");
                    dao.updateEntries(modifiedEntries);
                }
               setProgress(50);
                regionsModel.reset();
                appellationsModel.reset();
                vigneronsModel.reset();
                //remove deleted vins
                List<Vin> deletedVins = tableModel.getDeletedVins();
                if (deletedVins.size()>0){
                    logger.debug("Deleting " + deletedVins.size() + " vins from the database");
                    dao.deleteVins(deletedVins);
                }
                setProgress(60);
                //add new vins
                List<Vin> addedVins = tableModel.getAddedVins();
                if (addedVins.size() > 0){
                    logger.debug("Adding " + addedVins.size() + " vins to the database");
                    for (Vin vin : addedVins) {
                        long id = dao.addVin(vin);
                        vin.setId(id);
                        vin.setModified(false);
                    }
                }
                setProgress(75);
                //update modified vins
                List<Vin> modifiedVins = tableModel.getModifiedVins();
                if (modifiedVins.size() > 0){
                    logger.debug("Modifying " + modifiedVins.size() + " vins in the  database");
                    dao.updateVins(modifiedVins);
                }
                setProgress(90);
                tableModel.reset();
                //save the properties
                //record table columns preferred widths
                for (Column column : Column.values()) {
                    props.put("column."+column+".width",
                        ""+jXTable1.getColumnModel().getColumn(column.index()).getPreferredWidth());
                }
                Utils.writeProperties(props);
                
                //TODO: delete empty lines if the properties have been set
                
            }
            catch (Exception e){
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(null, "Erreur est survenue pendant la sauvegarde des données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                throw e;
            }
            finally {
                setProgress(100);
                if (!onExit){
                    Thread.sleep(1000);
                    setProgress(0);
                    Toolkit.getDefaultToolkit().beep();
                }                
            }
            return null;
        }
    }

    class ExportExcelTask extends SwingWorker<Void, Void>{
        private File xlsFile;
        private  List<Vin> vins;
        private boolean withHeaders;

        public ExportExcelTask(File xlsFile, boolean withHeaders) {
            this.xlsFile = xlsFile;
            this.withHeaders = withHeaders;
            vins = new ArrayList<Vin>();
            for (int i = 0; i < jXTable1.getRowCount(); i++) {
                vins.add(tableModel.getVin(jXTable1.convertRowIndexToModel(i)));
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                Workbook wb = new HSSFWorkbook();
                CreationHelper createHelper = wb.getCreationHelper();
                Sheet sheet = wb.createSheet("Vins");
                CellStyle headingStyle = wb.createCellStyle();
                Font boldFont = wb.createFont();
                boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
                headingStyle.setFont(boldFont);
                Column[] columns = Column.values();
                int rowCounter = 0;
                if (withHeaders){
                    Row row = sheet.createRow(rowCounter);
                    rowCounter++;
                    for (int i = 0; i < columns.length; i++) {
                        Column column = columns[i];
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(headingStyle);
                        cell.setCellValue(createHelper.createRichTextString(column.getDisplayName()));                    
                    }
                }
                for (int i = 0; i < vins.size(); i++) {
                    Row row = sheet.createRow(rowCounter);
                    rowCounter++;
                    for (int j = 0; j < columns.length; j++) {
                        Column column = columns[j];
                        Cell cell = row.createCell(j);

                        Object val = vins.get(i).getColumnValue(column);
                        String text = "";
                        if (val != null){
                            if (column == Column.DATE){
                                text = Vin.dateFormat.format(val);
                            }
                            else if (column == Column.PRIX_BTL){
                                text = Vin.currencyFormat.format(val);
                            }
                            else {
                                text = val.toString();
                            }
                        }
                        //logger.debug("Exporting value " + text + " in vin number " + i + " and column " + column);
                        cell.setCellValue(createHelper.createRichTextString(text));                    
                    }
                    setProgress((int)(i * 100 / vins.size()));
                }
                FileOutputStream fileOut = new FileOutputStream(xlsFile);
                wb.write(fileOut);
                fileOut.close();
                Desktop dt = Desktop.getDesktop();
                dt.open(xlsFile);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(null, "Erreur est survenue pendant l'exportation des données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                throw e;
            }
            finally {
                setProgress(100);
                Thread.sleep(1000);
                setProgress(0);                
            }
            return null;
        }
    }

    ////////////////////
    //Private
    ///////////////////
    private void doBeforeInitComponents() {
        applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        props = Utils.getProperties();
        appProps = (Properties) applicationContext.getBean("appProps");
        appVersion = appProps.getProperty("app.version");
        dao = applicationContext.getBean(HsqldbDao.class);
        List<Vin> allVins = dao.getAllVins();

        //initialize table data
        Column[] columns = new Column[]{
            DATE,
            CASIER,
            ANNEE,
            PAYS,
            REGION,
            APPELLATION,
            VIGNERON,
            QUALITE,
            STOCK,
            PRIX_BTL,
            ANNEE_CONSOMMATION
        };

        //initialize table model
        tableModel = new VinsTableModel(columns, allVins);

        //initialize the row sorter
        tableRowSorter = new TableRowSorter(tableModel);

        tableRowSorter.setComparator(DATE.index(), new Comparator<Timestamp>(){
            @Override
            public int compare(Timestamp t1, Timestamp t2) {
                return t1.compareTo(t2);
            }
        });

        tableRowSorter.setComparator(CASIER.index(), new Comparator<Integer>() {

            @Override
            public int compare(Integer n1, Integer n2) {
                return n1.compareTo(n2);
            }


        });
        tableRowSorter.setComparator(ANNEE.index(), new Comparator<Integer>() {

            @Override
            public int compare(Integer n1, Integer n2) {
                return n1.compareTo(n2);
            }


        });
        tableRowSorter.setComparator(STOCK.index(), new Comparator<Integer>() {

            @Override
            public int compare(Integer n1, Integer n2) {
                return n1.compareTo(n2);
            }


        });

        tableRowSorter.setComparator(PRIX_BTL.index(), new Comparator<BigDecimal>(){

            @Override
            public int compare(BigDecimal n1, BigDecimal n2) {
                return n1.compareTo(n2);
            }

        });

        //setup the list models for the entries
        regionsModel = new EntriesComboBoxModel(dao.getEntries(REGION));
        appellationsModel = new EntriesComboBoxModel(dao.getEntries(APPELLATION));
        vigneronsModel = new EntriesComboBoxModel(dao.getEntries(VIGNERON));
    }

    private void doAfterInitComponents(){
        //enable the update program menu if needed
        if (! isUpToDate()){
            jMenuItem3.setEnabled(true);
        }

        //disable reordering of the columns
        jXTable1.getTableHeader().setReorderingAllowed(false);
         
        //initialize table cell renderers and editors
        jXTable1.getColumnModel().getColumn(DATE.index()).setCellRenderer(new DateRenderer());
        
        jXTable1.getColumnModel().getColumn(DATE.index()).setCellEditor(new DateEditor(Vin.dateFormat, this));
        jXTable1.getColumnModel().getColumn(CASIER.index()).setCellEditor(new NumberEditor());
        jXTable1.getColumnModel().getColumn(ANNEE.index()).setCellEditor(new NumberEditor());
        jXTable1.getColumnModel().getColumn(PAYS.index())
                .setCellEditor(new EntryEditor(dao.getDistinct(PAYS), this));
        jXTable1.getColumnModel().getColumn(REGION.index()).setCellEditor(new EntryEditor(regionsModel, this));
        jXTable1.getColumnModel().getColumn(APPELLATION.index()).setCellEditor(new EntryEditor(appellationsModel, this));
        jXTable1.getColumnModel().getColumn(VIGNERON.index()).setCellEditor(new EntryEditor(vigneronsModel, this));
        jXTable1.getColumnModel().getColumn(QUALITE.index())
                .setCellEditor(new ListEditor(new String[]{"Rouge", "Blanc", "Rosé", "Divers"}, this));
        jXTable1.getColumnModel().getColumn(STOCK.index()).setCellEditor(new NumberSpinnerEditor(this));
        jXTable1.getColumnModel().getColumn(PRIX_BTL.index()).setCellRenderer(new CurrencyRenderer());
        jXTable1.getColumnModel().getColumn(PRIX_BTL.index()).setCellEditor(new CurrencyEditor(this));
        jXTable1.getColumnModel().getColumn(ANNEE_CONSOMMATION.index()).setCellRenderer(new YearRenderer());
        jXTable1.getColumnModel().getColumn(ANNEE_CONSOMMATION.index()).setCellEditor(new NumberEditor());

        //set the selection listeners
        jXTable1.getSelectionModel().addListSelectionListener(this);

        //set the table rows height
        jXTable1.setRowHeight(20);
        
        for (Column column : Column.values()) {
            String width = props.getProperty("column."+column+".width");
            if (width!=null){
                jXTable1.getColumnModel().getColumn(column.index()).setPreferredWidth(Integer.parseInt(width));
            }
        }

        //disable delete rows button
        jButton1.setEnabled(false);

        //configure confirmation dialog look and feel
        JDialog.setDefaultLookAndFeelDecorated(true);

        //note, not using row sorters on the list models
        //since they are shared between lists and combo boxes,
        //so need to insert entries in the right order while
        //adding them, see EntriesComboBoxModel.addEntry()
        
        //configure regions list
        jXList3.setModel(regionsModel);

        //configure appellations list
        jXList1.setModel(appellationsModel);

        //configure vignerons list
        jXList2.setModel(vigneronsModel);

        //configure the file chooser
        jFileChooser1.setDialogTitle("Choisissez l'emplacement du fichier CSV à enregistrer");
        FileFilter fileFilter = new FileNameExtensionFilter("Fichiers Excel", "XLS");
        jFileChooser1.setFileFilter(fileFilter);
    }

    private void exit() {
        logger.debug("User ended the application normally.");
        dispose();
        System.exit(0);
    }

    /** Creates new form MainWindow */
    public MainWindow() {
        super("Vinotèque");
        doBeforeInitComponents();
        initComponents();
        doAfterInitComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXList1 = new org.jdesktop.swingx.JXList();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jXList2 = new org.jdesktop.swingx.JXList();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jTextField4 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jXList3 = new org.jdesktop.swingx.JXList();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jCheckBox1 = new javax.swing.JCheckBox();
        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                handleWindowClosing(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Mes appellations"));

        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField2KeyPressed(evt);
            }
        });

        jButton2.setText("Ajouter");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jXList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jXList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jXList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jXList1);

        jButton3.setText("Modifier");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Effacer");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addGap(4, 4, 4))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Mes vignerons"));

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField3KeyPressed(evt);
            }
        });

        jButton6.setText("Ajouter");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jXList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jXList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jXList2ValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jXList2);

        jButton7.setText("Modifier");
        jButton7.setEnabled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Effacer");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addGap(4, 4, 4))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton8)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Mes régions"));

        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField4KeyPressed(evt);
            }
        });

        jButton9.setText("Ajouter");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jXList3.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jXList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jXList3ValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(jXList3);

        jButton10.setText("Modifier");
        jButton10.setEnabled(false);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("Effacer");
        jButton11.setEnabled(false);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jButton9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton11)
                .addGap(4, 4, 4))
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton9)
                    .addComponent(jButton10)
                    .addComponent(jButton11)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(438, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel1);

        jXTable1.setAutoCreateRowSorter(false);
        jXTable1.setModel(tableModel);
        jXTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jXTable1.setHighlighters(HighlighterFactory.createSimpleStriping());
        jXTable1.setRowSorter(tableRowSorter);
        jScrollPane1.setViewportView(jXTable1);

        jLabel2.setText("SFr 0.00");

        jTextField1.setEnabled(false);
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });

        jButton1.setText("Effaçer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton5.setText("Nouveau");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Filtrer par la région, l'appellation, ou le vigneron ");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        jToggleButton1.setText("Sans Tri");
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButton1ItemStateChanged(evt);
            }
        });

        jLabel1.setText("Total de la sélection :");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(223, 223, 223)
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 386, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 769, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addGap(19, 19, 19))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1249, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton5)
                    .addComponent(jCheckBox1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton1))
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel1))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        jMenu1.setText("Fichier");

        jMenuItem2.setText("Sauvegarder");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Mettre à jour");
        jMenuItem3.setToolTipText("Mettre à jour le logiciel");
        jMenuItem3.setEnabled(false);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Exporter en Excel");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem5.setText("Préférences");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuItem1.setText("Quitter");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void handleWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_handleWindowClosing
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous sauvegarder les données modifiées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            save(true);
        }
        exit();
    }//GEN-LAST:event_handleWindowClosing

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous sauvegarder les données modifiées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            save(true);
        }
        exit();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode()== KeyEvent.VK_ENTER){
            filter();
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous vraiement effaçer les entrées sélectionnées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            //delete selected table rows
            int[] rows = jXTable1.getSelectedRows();
            for (int i=0; i < rows.length; i++) {
               tableModel.removeRow(jXTable1.convertRowIndexToModel(rows[0]));
            }
            tableModel.fireTableDataChanged();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int index = addEntry(appellationsModel, jTextField2, APPELLATION);
        if (index>=0){
            jXList1.setSelectedValue(appellationsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
            int index = findFirstSimilarEntry(appellationsModel, jTextField2);
            if (index>=0){
                jXList1.requestFocus();
                jXList1.clearSelection();
                jXList1.setSelectedValue(appellationsModel.getElementAt(index), true);
            }
        }
    }//GEN-LAST:event_jTextField2KeyPressed

    private void jXList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jXList1ValueChanged
        if (!evt.getValueIsAdjusting()){
            JXList list = (JXList)evt.getSource();
            Entry entry = (Entry)list.getSelectedValue();
            if (entry!=null){
                jTextField2.setText(entry.getName());
                if (list.getSelectedIndices().length==1){
                    jButton3.setEnabled(true);
                    jButton4.setEnabled(true);
                }
                else {
                    jButton3.setEnabled(false);
                    jButton4.setEnabled(true);
                }
            }
            else {
                jTextField2.setText("");
                
            }
        }
        else {
            jButton3.setEnabled(false);
            jButton4.setEnabled(false);
        }
    }//GEN-LAST:event_jXList1ValueChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int index = jXList1.convertIndexToModel(jXList1.getSelectedIndex());
        boolean modified = modifyEntry(appellationsModel, index, jTextField2);
        if (modified){
            jXList1.setSelectedValue(appellationsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous vraiement effaçer les entrées sélectionnées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            //delete selected list entries
            int[] rows = jXList1.getSelectedIndices();
            for (int i=0; i < rows.length; i++) {
               appellationsModel.deleteEntryAt(jXList1.convertIndexToModel(rows[0]));
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //disable filter
        if (jCheckBox1.isSelected()){
            jCheckBox1.setSelected(false);
        }
        else {
            //clear the filter, do not sort
            clearFilter(false);
        }

        //remove sorting
        unsort();

        //add new table entry for the new wine
        int row = tableModel.addNewVin();

        //select the first row, should be the one we just added
        int viewRow = jXTable1.convertRowIndexToView(row);
        jXTable1.setRowSelectionInterval(viewRow, viewRow);

        //scroll to the first row
        jXTable1.scrollRowToVisible(viewRow);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            jTextField1.setEnabled(true);
            filter();
        }
        else {
            jTextField1.setEnabled(false);
            clearFilter(true);
        }
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jToggleButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButton1ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            unsort();
            for (Column column : Column.values()) {
                tableRowSorter.setSortable(column.index(), false);
            }
        }
        else {
            for (Column column : Column.values()) {
                tableRowSorter.setSortable(column.index(), true);
            }
        }
    }//GEN-LAST:event_jToggleButton1ItemStateChanged

    private void jTextField3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField3KeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
            int index = findFirstSimilarEntry(vigneronsModel, jTextField3);
            if (index>=0){
                jXList2.requestFocus();
                jXList2.clearSelection();
                jXList2.setSelectedValue(vigneronsModel.getElementAt(index), true);
            }
        }
    }//GEN-LAST:event_jTextField3KeyPressed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        int index = addEntry(vigneronsModel, jTextField3, VIGNERON);
        if (index>=0){
            jXList2.setSelectedValue(vigneronsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jXList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jXList2ValueChanged
        if (!evt.getValueIsAdjusting()){
            JXList list = (JXList)evt.getSource();
            Entry entry = (Entry)list.getSelectedValue();
            if (entry!=null){
                jTextField3.setText(entry.getName());
                if (list.getSelectedIndices().length==1){
                    jButton7.setEnabled(true);
                    jButton8.setEnabled(true);
                }
                else {
                    jButton7.setEnabled(false);
                    jButton8.setEnabled(true);
                }
            }
            else {
                jTextField3.setText("");

            }
        }
        else {
            jButton7.setEnabled(false);
            jButton8.setEnabled(false);
        }
    }//GEN-LAST:event_jXList2ValueChanged

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int index = jXList2.convertIndexToModel(jXList2.getSelectedIndex());
        boolean modified = modifyEntry(vigneronsModel, index, jTextField3);
        if (modified){
            jXList2.setSelectedValue(vigneronsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous vraiement effaçer les entrées sélectionnées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            //delete selected list entries
            int[] rows = jXList2.getSelectedIndices();
            for (int i=0; i < rows.length; i++) {
               vigneronsModel.deleteEntryAt(jXList2.convertIndexToModel(rows[0]));
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jTextField4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField4KeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
            int index = findFirstSimilarEntry(regionsModel, jTextField4);
            if (index>=0){
                jXList3.requestFocus();
                jXList3.clearSelection();
                jXList3.setSelectedValue(regionsModel.getElementAt(index), true);
            }
        }

    }//GEN-LAST:event_jTextField4KeyPressed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        int index = addEntry(regionsModel, jTextField4, REGION);
        if (index>=0){
            jXList3.setSelectedValue(regionsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jXList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jXList3ValueChanged
        if (!evt.getValueIsAdjusting()){
            JXList list = (JXList)evt.getSource();
            Entry entry = (Entry)list.getSelectedValue();
            if (entry!=null){
                jTextField4.setText(entry.getName());
                if (list.getSelectedIndices().length==1){
                    jButton10.setEnabled(true);
                    jButton11.setEnabled(true);
                }
                else {
                    jButton10.setEnabled(false);
                    jButton11.setEnabled(true);
                }
            }
            else {
                jTextField4.setText("");

            }
        }
        else {
            jButton10.setEnabled(false);
            jButton11.setEnabled(false);
        }
    }//GEN-LAST:event_jXList3ValueChanged

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        int index = jXList3.convertIndexToModel(jXList3.getSelectedIndex());
        boolean modified = modifyEntry(regionsModel,
            index,
            jTextField4);
        if (modified){
            jXList3.setSelectedValue(regionsModel.getElementAt(index), true);
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        //show the confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null,
                "Voulez-vous vraiement effaçer les entrées sélectionnées ?",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer==JOptionPane.YES_OPTION){
            //delete selected list entries
            int[] rows = jXList3.getSelectedIndices();
            for (int i=0; i < rows.length; i++) {
               regionsModel.deleteEntryAt(jXList3.convertIndexToModel(rows[0]));
            }
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        save(false);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        logger.info("Upgrading to version " + appVersion);
        upgrade();
        jMenuItem3.setEnabled(false);
        JOptionPane.showMessageDialog(this,
                "L'application a été mise à jour."
                + " Version courante est " + appVersion + "."
                + " Vous devez à présent redémarrer l'application.");
        props.setProperty("version", appVersion);
        Utils.writeProperties(props);
        exit();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
        jFileChooser1.setSelectedFile(new File(
            jFileChooser1.getCurrentDirectory().getAbsolutePath() +
            "/vinoteque_" + format.format(new Date()) + ".xls"));

       int option = jFileChooser1.showSaveDialog(this);
       if (option == JFileChooser.APPROVE_OPTION){
            exportToExcelFile(jFileChooser1.getSelectedFile(), true);
       }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        //show preferences dialog
        logger.debug("Showing preferences dialog");        
        JDialog dialog = new JDialog(this, "Préférences", true);
        PreferencesPanel prefsPanel = new PreferencesPanel(this);
        dialog.getContentPane().add(prefsPanel);
        dialog.setSize(prefsPanel.getPreferredSize());
        dialog.setLocation(200, 200);
        dialog.setVisible(true);        
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            logger.error(ex.getMessage());
        } catch (InstantiationException ex) {
            logger.error(ex.getMessage());
        } catch (IllegalAccessException ex) {
            logger.error(ex.getMessage());
        } catch (UnsupportedLookAndFeelException ex) {
            logger.error(ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                MainWindow win = new MainWindow();
                win.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXList jXList1;
    private org.jdesktop.swingx.JXList jXList2;
    private org.jdesktop.swingx.JXList jXList3;
    private org.jdesktop.swingx.JXTable jXTable1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()){
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (!lsm.isSelectionEmpty()){
                //adjust the value of the selection total
                int[] rows = jXTable1.getSelectedRows();
                BigDecimal total = new BigDecimal(0);
                boolean outOfView = false;
                for (int row : rows) {
                    //see if the row is in view
                    if (row < tableRowSorter.getViewRowCount()){
                        Vin vin = tableModel.getVin(jXTable1.convertRowIndexToModel(row));
                        total = total.add(vin.getPrixBtl().multiply(new BigDecimal(vin.getStock())));
                    }
                    else {
                        outOfView = true;
                        break;
                    }
                }
                if (!outOfView){
                    jLabel2.setText(Vin.currencyFormat.format(total));
                    //enable the delete row button
                    jButton1.setEnabled(true);
                }
                else {
                    jLabel2.setText("");
                    //disable the delete row button
                    jButton1.setEnabled(false);
                }
            }
            else {
                jLabel2.setText("");
                //disable the delete row button
                jButton1.setEnabled(false);
            }
        }
        else {
            jLabel2.setText("");
            //disable the delete row button
            jButton1.setEnabled(false);
        }
    }

    private int addEntry(EntriesComboBoxModel model, JTextField textField, Column column){
        int index = -1;
        String text = textField.getText();
        if (text!=null && !text.matches("\\s*")){
            index = model.addEntry(column, text.trim());
        }
        return index;
    }

    private int findFirstSimilarEntry(EntriesComboBoxModel model, JTextField textField){
        int index = -1;
        String text = textField.getText();
        if (text!=null && !text.matches("\\s*")){
            index = model.findFirstSimilarEntry(text.trim());
        }
        return index;
    }

    private boolean modifyEntry(EntriesComboBoxModel model, int index, JTextField textField){
        boolean modified = false;
        String text = textField.getText();
        if (text!=null && !text.matches("\\s*")){
            modified = model.modifyEntryAt(index, textField.getText().trim());
        }
        return modified;
    }

    private void filter(){
        String text = jTextField1.getText();
        if (text.matches("\\s*")){
            //reset filter, show all the entries
            text = "";
        }
        logger.debug("Filtering by " + text);
        TextRowFilter rowFilter = (TextRowFilter)tableRowSorter.getRowFilter();
        if (rowFilter!=null){
            rowFilter.setText(text);
            tableRowSorter.sort();
        }
        else {
            rowFilter = new TextRowFilter(text, new Column[]{REGION, APPELLATION, VIGNERON});
            tableRowSorter.setRowFilter(rowFilter);
        }
    }

    private void clearFilter(boolean sort){
        TextRowFilter filter = (TextRowFilter) tableRowSorter.getRowFilter();
        if (filter!=null){
            filter.setText("");
            if (sort){
                tableRowSorter.sort();
            }
        }
    }

    private void unsort(){
        //remove sorting
        List<SortKey> sortKeys = tableRowSorter.getSortKeys();
        List<SortKey> newSortKeys = new ArrayList<SortKey>();
        for (SortKey sortKey : sortKeys) {
            SortKey newSortKey = new SortKey(sortKey.getColumn(), SortOrder.UNSORTED);
            newSortKeys.add(newSortKey);
        }
        tableRowSorter.setSortKeys(newSortKeys);
        tableRowSorter.sort();
    }

    @Override
    public void editWithFocus(Component component) {
        int row = jXTable1.getSelectedRow();
        int column = jXTable1.getSelectedColumn();
        jXTable1.editCellAt(row, column);
        component.requestFocusInWindow();

    }

    private void save(boolean onExit){
        SaveTask task = new SaveTask(onExit);
        task.addPropertyChangeListener(this);
        task.execute();
    }

    private void exportToExcelFile(File file, boolean withHeaders) {
        ExportExcelTask task = new ExportExcelTask(file, withHeaders);
        task.addPropertyChangeListener(this);
        task.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())){
            int progress = (Integer) evt.getNewValue();
            jProgressBar1.setValue(progress);
        }
    }

    private boolean isUpToDate(){
        boolean upToDate = true;
        if (! props.containsKey("version")
                || props.getProperty("version").compareTo(appVersion)<0){
            upToDate = false;
        }
        return upToDate;
    }

    /*
     * Performs the upgrade logic.
     */
    private void upgrade(){
        dao.addColumn("vins", "ANNEE_CONSOMMATION", "INTEGER", "0");
    }

    @Override
    public Properties getPreferences() {
        //copy the properties
        Properties copy = new Properties();
        copy.putAll(props);
        return copy;
    }

    @Override
    public void preferencesChanged(Properties preferences) {
        logger.debug("Preferences have changed");
        //invoked once the user has okeyed the preferences dialog
        //copy the preferences to the properties
        Iterator it = preferences.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            if (key.startsWith("prefs.")){
                props.put(key, preferences.getProperty(key));
            }
        }
    }

}
