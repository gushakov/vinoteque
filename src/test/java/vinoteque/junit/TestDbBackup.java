package vinoteque.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
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
 *
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
        assertTrue(files != null && files.size() > 0);
        Pattern p = Pattern.compile("data-\\d+-(\\d+).script", Pattern.CASE_INSENSITIVE);
        Collections.shuffle(files);
        Collections.sort(files, new RegexGroupComparator(p));
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

//    @Test
    public void testBackup() throws Exception {
        //get the backup files
        List<File> files = Utils.getBackupFiles(true);
        assertNotNull(files);
    }

    @Test
    public void testGetBackupFileInfo() throws Exception {
        String info = "";
        File backupFile = new File("C:\\vinoteque\\hsqldb\\backup\\data-20120427142331.script");
        
        Pattern namePattern = Pattern.compile("data-(\\d+)\\.script", Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(backupFile.getName());
        if (nameMatcher.find()){
            SimpleDateFormat fmtIn = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd MMMMM yyyy, HH:mm:ss");
            info += fmtOut.format(fmtIn.parse(nameMatcher.group(1)));
        }
        
        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(backupFile))).useDelimiter("\\n");
            int counter = 0;
            while (scanner.hasNext()) {
                String line = scanner.next();
                Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+VINS", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()){
                    counter++;
                }
            }
            info += " (" + counter + " vins)";
        } catch (Exception e) {
            throw new RuntimeException("Error scanning backup file " + backupFile.getAbsolutePath(),e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        
        System.out.println(info);
        
    }
}
