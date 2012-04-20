package vinoteque.utils;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import vinoteque.beans.Vin;
import vinoteque.beans.Vin.Column;
import static vinoteque.beans.Vin.Column.DATE;
import vinoteque.exceptions.CsvLineParseException;
import vinoteque.exceptions.IncorrectFileFormatException;

/**
 *
 * @author George Ushakov
 */
public class Utils {

    private static long offset = 0;
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static class DateComparator implements Comparator<Vin> {

        @Override
        public int compare(Vin vin1, Vin vin2) {
            int answer = 0;
            if (vin1.getDate() != null && vin2.getDate() != null) {
                answer = vin1.getDate().compareTo(vin2.getDate());
            }
            return answer;
        }
    }

    public static Vector<String> getColumnDisplayNames(Column[] columns) {
        Vector<String> names = new Vector<String>();
        for (Column column : columns) {
            names.add(column.getDisplayName());
        }
        return names;
    }

    public static List<Vin> importVinsFromCsvFile(File csvFile) throws FileNotFoundException, IncorrectFileFormatException {
        SimpleDateFormat dateFormat = null;
        if (Locale.getDefault().equals(Vin.LOCALE)) {
            dateFormat = Vin.dateFormat;
        } else {
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        }
        List<Vin> vins = null;
        if (csvFile.isFile() && csvFile.getName().toLowerCase().endsWith(".csv")) {
            logger.info("Importing from " + csvFile.getAbsoluteFile());
            vins = new ArrayList<Vin>();
            //assume default Windows character encoding
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
            String line = null;
            try {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    throw new CsvLineParseException(e);
                }
                while (line != null && !line.matches("\\s*")) {
                    logger.debug("Line: " + line);
                    StringTokenizer st = new StringTokenizer(line.trim(), ",\"", true);
                    String[] tokens = new String[st.countTokens()];
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        tokens[i] = st.nextToken();
                        i++;
                    }
                    Vin vin = new Vin();
                    int columnCounter = 0;
                    boolean isQuote = false;
                    String word = "";
                    for (String token : tokens) {
                        logger.debug("Token: " + token);
                        if (token.equals(",")) {
                            //see if this token is inside a quoted string
                            if (isQuote) {
                                //add token to the word
                                word += token.trim();
                            } else {
                                try {
                                    if (!word.matches("\\s*")) {
                                        logger.debug("Parsing: " + word);
                                        //parse the field value, based on the columnCounter index
                                        if (columnCounter == 0) {
                                            //DATE
                                            vin.setDate(dateFormat.parse(word));
                                        } else if (columnCounter == 1) {
                                            //CASIER
                                            vin.setCasier(Integer.parseInt(word));
                                        } else if (columnCounter == 2) {
                                            //ANNEE
                                            vin.setAnnee(Integer.parseInt(word));
                                        } else if (columnCounter == 3) {
                                            //PAYS
                                            vin.setPays(word);
                                        } else if (columnCounter == 4) {
                                            //REGION
                                            vin.setRegion(word);
                                        } else if (columnCounter == 5) {
                                            //APPELLATION
                                            vin.setAppellation(word);
                                        } else if (columnCounter == 6) {
                                            //VIGNERON
                                            vin.setVigneron(word);
                                        } else if (columnCounter == 7) {
                                            //Rg
                                            vin.setQualite("Rouge");
                                        } else if (columnCounter == 8) {
                                            //Bl
                                            vin.setQualite("Blanc");
                                        } else if (columnCounter == 9) {
                                            //re
                                            vin.setQualite("Rosé");
                                        } else if (columnCounter == 10) {
                                            //STOCK
                                            vin.setStock(Integer.parseInt(word));
                                        } else if (columnCounter == 11) {
                                            //PRIX_BTL
                                            vin.setPrixBtl(BigDecimal.valueOf(Vin.currencyFormat.parse(word).doubleValue()));
                                        } else {
                                            // do nothing after all the columns were processed
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    //skip the value for this column
                                    logger.error(e.getMessage());
                                } catch (ParseException e) {
                                    //skip the value for this column
                                    logger.error(e.getMessage());
                                } finally {
                                    //next column
                                    columnCounter++;
                                    //reset the word
                                    word = "";
                                }
                            }
                        } else if (token.equals("\"")) {
                            if (isQuote) {
                                //close the quote
                                isQuote = false;
                            } else {
                                //open quote
                                isQuote = true;
                            }
                        } else {
                            //add token to the word
                            word += token.trim();
                        }
                    }
                    //wine is parsed
                    vins.add(vin);
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        throw new CsvLineParseException(e);
                    }
                }
            } catch (CsvLineParseException e) {
                logger.error("Error parsing line " + line + ". " + e.getMessage());
            }
        } else {
            throw new IncorrectFileFormatException();
        }
        return vins;
    }

    public static void sortVins(Column column, List<Vin> vins) {
        switch (column) {
            case DATE:
                Collections.sort(vins, new DateComparator());
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = GregorianCalendar.getInstance(Vin.LOCALE);
        cal.set(year, month, day);
        return cal.getTime();
    }

    public static synchronized long getTimestamp() {
        long timestamp = System.currentTimeMillis() + offset;
        offset++;
        return timestamp;
    }

    /**
     * Reads vinoteque.properties file in the application directory and returns
     * the properties object.
     *
     * @return Properties for the application
     */
    public static Properties getProperties() {
        File file = new File("c:\\vinoteque\\vinoteque.properties");
        Properties props = new Properties();
        if (file.exists()) {
            try {
                props.load(new FileInputStream(file));
            } catch (IOException ex) {
                logger.error("Cannot read vinoteque.properties file", ex);
            }
        }
        return props;
    }

    /**
     * Writes properties to the file
     * <code>vinoteque.properties</code>
     *
     * @param props
     */
    public static void writeProperties(Properties props) {
        File file = new File("c:\\vinoteque\\vinoteque.properties");
        try {
            PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
            props.store(fw, null);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public static List<File> getBackupFiles(boolean oldestFirst) {
        List<File> files = new ArrayList<File>();
        File backupDir = new File("c:\\vinoteque\\hsqldb\\backup");
        if (backupDir.exists()) {
            Pattern pattern = Pattern.compile("data-(\\d+).script", Pattern.CASE_INSENSITIVE);
            files = new ArrayList<File>(FileUtils.listFiles(backupDir, new RegexFileFilter(pattern), null));
            if (oldestFirst){
                Collections.sort(files, new RegexGroupComparator(pattern));    
            }
            else {
                Collections.sort(files, new ReverseComparator(new RegexGroupComparator(pattern)));
            }
        }
        return files;
    }

    public static String backupDatabaseScript() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        String ts = fmt.format(now);
        File src = new File("c:\\vinoteque\\hsqldb\\data.script");
        File dst = new File("c:\\vinoteque\\hsqldb\\backup\\data-" + ts + ".script");
        try {
            FileUtils.forceMkdir(new File("c:\\vinoteque\\hsqldb"));
            FileUtils.copyFile(src, dst);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dst.getAbsolutePath();
    }
    
}
