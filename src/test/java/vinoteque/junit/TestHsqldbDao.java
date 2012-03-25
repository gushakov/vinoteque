package vinoteque.junit;

import viniteque.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import vinoteque.beans.Entry;

import vinoteque.beans.Vin;
import vinoteque.db.HsqldbDao;
import vinoteque.utils.Utils;

import static vinoteque.beans.Vin.Column.*;

/**
 *
 * @author George Ushakov
 */
public class TestHsqldbDao {

    private static final Logger logger = Logger.getLogger(TestHsqldbDao.class);

    private HsqldbDao dao;
    
    @Before
    public void before() throws Exception{
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        dao = ctx.getBean(HsqldbDao.class);
    }
    
    
//    @Test
    public void test() throws Exception {
    }

//    @Test
    public void testGetAllVins() throws Exception {
        List<Vin> vins = dao.getAllVins();
        assertNotNull(vins);
        assertTrue(vins.size()>0);
        Vin v = vins.get(0);
        logger.debug(v.getAppellation());
        logger.debug(v.getDate().getClass().getCanonicalName());
    }

//    @Test
    public void testInsert() throws Exception {
        Vin v = new Vin();
        v.setAppellation("Masandra, Vins de Crimée");
        v.setDate(new Date());
        v.setCasier(2);
        v.setAnnee(2007);
        v.setPays("UA");
        v.setRegion("Crimée");
        v.setQualite("Rouge");
        v.setPrixBtl(BigDecimal.valueOf(20.0));
        v.setVigneron("Masandra Vinzavod");
        dao.addVin(v);
    }

    @Test
    public void testImportFromCsv() throws Exception {
        File csvFile = new File("c:\\vinoteque\\casiers.csv");
        List<Vin> vins = Utils.importVinsFromCsvFile(csvFile);
        assertNotNull(vins);
        dao.addVins(vins);
    }

    @Test
    public void testAddEntries() throws Exception {
        List<Entry> list = dao.getDistinct(REGION);
        list.addAll(dao.getDistinct(APPELLATION));
        list.addAll(dao.getDistinct(VIGNERON));
        dao.addEntries(list);
    }
    
    
//    @Test
    public void testGetEntries() throws Exception {
        List<Entry> list = dao.getEntries(APPELLATION);
        assertNotNull(list);
        logger.debug(list.size());
    }

//    @Test
    public void testGetDistinct() throws Exception {
        List<Entry> list = dao.getDistinct(APPELLATION);
        assertNotNull(list);
        logger.debug(list.size());
    }

//    @Test
    public void testAddEntryWithCheck() throws Exception {
        Entry entry = new Entry();
        entry.setColumn(APPELLATION.name());
        entry.setName("Bordeaux");
        boolean added = dao.addEntry(entry, true);
        assertFalse(added);
    }

//    @Test
    public void testUpdateEntries() throws Exception {
        Entry entry = new Entry();
        entry.setId(1220);
        entry.setColumn(REGION.name());
        entry.setName("Alba modified");
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        dao.updateEntries(entries);
    }

//    @Test
    public void testDeleteEntries() throws Exception {
        Entry entry = new Entry();
        entry.setId(1220);
        entry.setColumn(REGION.name());
        entry.setName("Alba modified");
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);
        dao.deleteEntries(entries);
    }

//    @Test
    public void testUpdateVins() throws Exception {
        Vin vin = new Vin();
        vin.setId(11);
        vin.setPays("Russia");
        vin.setPrixBtl(new BigDecimal("25.0"));
        ArrayList<Vin> vins = new ArrayList<Vin>();
        vins.add(vin);
        dao.updateVins(vins);
    }

//    @Test
    public void testDeleteVins() throws Exception {
        Vin vin = new Vin();
        vin.setId(11);
        ArrayList<Vin> vins = new ArrayList<Vin>();
        vins.add(vin);
        dao.deleteVins(vins);
    }

//    @Test
    public void testAddColumn() throws Exception {
        dao.addColumn("vins", "ANNEE_CONSOMMATION", "INTEGER", "0");
    }
}
