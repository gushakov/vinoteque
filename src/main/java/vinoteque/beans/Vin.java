package vinoteque.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import vinoteque.utils.Utils;

/**
 * Bean representing a wine.
 *
 * @author George Ushakov
 */
public class Vin implements Serializable {

    public static enum Column {

        DATE("Date"),
        CASIER("Casier"),
        ANNEE("Année"),
        PAYS("Pays"),
        REGION("Région"),
        APPELLATION("Appellation"),
        VIGNERON("Vigneron"),
        QUALITE("Qualité"),
        STOCK("Stock"),
        PRIX_BTL("Prix par bouteille"),
        ANNEE_CONSOMMATION("Année de consommation");
        private String displayName;

        private Column(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int index() {
            return Arrays.asList(values()).indexOf(this);
        }
    };
    public static final Locale LOCALE = new Locale("fr", "CH");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", LOCALE);
    public static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(LOCALE);
    private long timestamp;
    private boolean modified;
    private long id;
    private Date date;
    private int casier;
    private int annee;
    private String pays;
    private String region;
    private String appellation;
    private String vigneron;
    private String qualite;
    private int stock;
    private BigDecimal prixBtl;
    private int anneeConsommation;

    public Vin() {
        timestamp = Utils.getTimestamp();
        modified = false;
        id = -1;
    }

    public Object getColumnValue(Column column) {
        Object value = null;
        switch (column) {
            case DATE:
                value = date;
                break;
            case CASIER:
                value = casier;
                break;
            case ANNEE:
                value = annee;
                break;
            case PAYS:
                value = pays;
                break;
            case REGION:
                value = region;
                break;
            case APPELLATION:
                value = appellation;
                break;
            case VIGNERON:
                value = vigneron;
                break;
            case QUALITE:
                value = qualite;
                break;
            case STOCK:
                value = stock;
                break;
            case PRIX_BTL:
                value = prixBtl;
                break;
            case ANNEE_CONSOMMATION:
                value = anneeConsommation;
                break;
            default:
                throw new UnsupportedOperationException("Unknown column " + column);
        }
        return value;
    }

    public void setColumnValue(Column column, Object value) {
        switch (column) {
            case DATE:
                setDate((Date) value);
                break;
            case CASIER:
                setCasier((Integer) value);
                break;
            case ANNEE:
                setAnnee((Integer) value);
                break;
            case PAYS:
                setPays((String) value);
                break;
            case REGION:
                setRegion((String) value);
                break;
            case APPELLATION:
                setAppellation((String) value);
                break;
            case VIGNERON:
                setVigneron((String) value);
                break;
            case QUALITE:
                setQualite((String) value);
                break;
            case STOCK:
                setStock((Integer) value);
                break;
            case PRIX_BTL:
                setPrixBtl((BigDecimal) value);
                break;
            case ANNEE_CONSOMMATION:
                setAnneeConsommation((Integer) value);
                break;
            default:
                throw new UnsupportedOperationException("Unknown column " + column);
        }
    }

    @Override
    public boolean equals(Object obj) {
        boolean answer = false;
        if (obj instanceof Vin) {
            Vin vin = (Vin) obj;
            //compare by ids if set
            if (id != -1 && vin.id != -1) {
                if (id == vin.id) {
                    answer = true;
                }
            } else {
                //otherwise compare by the timestamps
                if (timestamp == vin.timestamp) {
                    answer = true;
                }
            }
        }
        return answer;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "Vin{" + "timestamp=" + timestamp + ", modified=" + modified + ", id=" + id + ", date=" + date + ", casier=" + casier + ", annee=" + annee + ", pays=" + pays + ", region=" + region + ", appellation=" + appellation + ", vigneron=" + vigneron + ", qualite=" + qualite + ", stock=" + stock + ", prixBtl=" + prixBtl + ", anneeConsommation=" + anneeConsommation + '}';
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public String getAppellation() {
        return appellation;
    }

    public void setAppellation(String appellation) {
        this.appellation = appellation;
    }

    public int getCasier() {
        return casier;
    }

    public void setCasier(int casier) {
        this.casier = casier;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public BigDecimal getPrixBtl() {
        return prixBtl;
    }

    public void setPrixBtl(BigDecimal prixBtl) {
        this.prixBtl = prixBtl;
    }

    public String getQualite() {
        return qualite;
    }

    public void setQualite(String qualite) {
        this.qualite = qualite;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVigneron() {
        return vigneron;
    }

    public void setVigneron(String vigneron) {
        this.vigneron = vigneron;
    }

    public int getAnneeConsommation() {
        return anneeConsommation;
    }

    public void setAnneeConsommation(int anneeConsommation) {
        this.anneeConsommation = anneeConsommation;
    }
}
