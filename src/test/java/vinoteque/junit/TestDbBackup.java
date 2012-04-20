package vinoteque.junit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hsqldb.lib.tar.DbBackup;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import vinoteque.utils.RegexGroupComparator;
import vinoteque.utils.Utils;

/**
 * Testing database backup
 * @author gushakov
 */
public class TestDbBackup {
    
//    @Test
    public void testBackupOnline() throws Exception {
        DbBackup backup = new DbBackup(new File("c:\\tmp\\backup\\data.1234.tar"), "/vinoteque/hsqldb/data");
        assertNotNull(backup);
        backup.setAbortUponModify(false);
        backup.write();
    }

//    @Test
    public void testBackupScriptFile() throws Exception {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        String ts = fmt.format(now);
        File src = new File("c:\\vinoteque\\hsqldb\\data.script");
        File dst = new File("c:\\vinoteque\\hsqldb\\backup\\data-" + ts + "-3.script");
        FileUtils.copyFile(src, dst);
        assertTrue(dst.exists());
    }
    
//  @Test
    public void testListBackupFiles() throws Exception {
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("c:\\vinoteque\\hsqldb\\backup"),
                new RegexFileFilter("data-\\d+-\\d+.script"), null));
        assertTrue(files!=null && files.size()>0);
        Pattern p = Pattern.compile("data-\\d+-(\\d+).script", Pattern.CASE_INSENSITIVE);
        Collections.shuffle(files);
        Collections.sort(files, new RegexGroupComparator(p));
        for (File file : files) {
            System.out.println(file.getName());
        }
    }
    
    @Test
    public void testBackup() throws Exception {
        //get the backup files
        List<File> files  = Utils.getBackupFiles(true);
        assertNotNull(files);    
    }

}
