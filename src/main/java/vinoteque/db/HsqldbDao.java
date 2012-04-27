package vinoteque.db;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import vinoteque.beans.Entry;

import vinoteque.beans.Vin;
import vinoteque.beans.Vin.Column;


/**
 * Manages all queries to the local HSQL database. For configuration see <code>app.properties</code>
 * file.
 * @author George Ushakov
 */
public class HsqldbDao {

    private static final Logger logger = Logger.getLogger(HsqldbDao.class);
    private SimpleJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertVin;
    private SimpleJdbcInsert insertEntries;

    public HsqldbDao(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        this.insertVin =
                new SimpleJdbcInsert(dataSource).withSchemaName("public").withTableName("vins").usingGeneratedKeyColumns("id");
        this.insertEntries =
                new SimpleJdbcInsert(dataSource).withSchemaName("public").withTableName("entries").usingGeneratedKeyColumns("id");        
    }

    public List<Vin> getAllVins() {
        return jdbcTemplate.getJdbcOperations().query("select * from public.vins", ParameterizedBeanPropertyRowMapper.newInstance(Vin.class));
    }

    public List<Entry> getEntries(Column column){
        return jdbcTemplate
                .getJdbcOperations()
                .query("select * from public.entries where column = ? order by lcase(ltrim(name))",
                    ParameterizedBeanPropertyRowMapper.newInstance(Entry.class),
                    column.name());
    }

    public List<Vin> getSomeVins(int limit) {
        return jdbcTemplate.getJdbcOperations()
                    .query("select * from public.vins limit ?",
                    ParameterizedBeanPropertyRowMapper.newInstance(Vin.class),
                    new Object[]{limit});
    }

    public long addVin(Vin vin) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(vin);
        Number newId = insertVin.executeAndReturnKey(parameters);
        long id = newId.longValue();
        vin.setId(id);
        return id;
    }

    public void addVins(List<Vin> vins){
        SqlParameterSource[] parameters = new BeanPropertySqlParameterSource[vins.size()];
        for (int i=0; i<parameters.length; i++) {
            parameters[i] = new BeanPropertySqlParameterSource(vins.get(i));

        }
        insertVin.executeBatch(parameters);
    }

    public void addEntries(List<Entry> entries){
        SqlParameterSource[] parameters = new BeanPropertySqlParameterSource[entries.size()];
        for (int i=0; i<parameters.length; i++) {
            parameters[i] = new BeanPropertySqlParameterSource(entries.get(i));
        }
        insertEntries.executeBatch(parameters);
    }

    public long addEntry(Entry entry){
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(entry);
        Number newId = insertEntries.executeAndReturnKey(parameters);
        long id = newId.longValue();
        entry.setId(id);
        return id;
    }

    public List<Entry> getDistinct(Column column){
        String sql = "select distinct " +
                column.name() +
                " as name, '" +
                column.name() +
                "' as column from public.vins where " +
                column.name() +
                " is not null order by lcase(ltrim(" +
                column.name() +
                ")) asc";
        return jdbcTemplate.getJdbcOperations()
            .query(sql, ParameterizedBeanPropertyRowMapper.newInstance(Entry.class));
    }


    public boolean addEntry(Entry entry, boolean withCheck){
        boolean added = false;
        if (withCheck){
            String existsSql = "select count(*) from public.entries where name = ? and column = ?";
            int count = jdbcTemplate.getJdbcOperations()
                        .queryForInt(existsSql, entry.getName(), entry.getColumn());
            if (count == 0){
                //does not exist, insert data
                addEntry(entry);
                added = true;
            }
        }
        else {
            addEntry(entry);
            added = true;
        }
        return added;
    }

    public int[] updateEntries(List<Entry> entries){
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(entries.toArray(new Entry[]{}));
        int[] updateCounts = jdbcTemplate.batchUpdate(
            "update public.entries set name = :name where id = :id",
            batch);
        return updateCounts;
    }

    public int[] deleteEntries(List<Entry> entries){
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(entries.toArray(new Entry[]{}));
        int[] deleteCounts = jdbcTemplate.batchUpdate(
            "delete from public.entries where id = :id",
            batch);
        return deleteCounts;
    }
    
    public int deleteAllEmpty(){
        
        int deleteCounts = jdbcTemplate.update("delete from public.vins "
                + "where casier = ? and "
                + "appellation = ? and "
                + "annee = ? and "
                + "pays = ? and "
                + "region = ? and "
                + "vigneron = ? and "
                + "qualite = ? and "
                + "stock = ? and "
                + "prix_btl = ? and "
                + "annee_consommation = ?",
                0,                  //casier
                "",                 //appellation
                0,                  //annee
                "",                 //pays
                "",                 //region
                "",                 //vigneron
                "",                 //qualite
                0,                  //stock
                new BigDecimal(0d), //prix_btl
                0);                 //annee_consommation
        
        return deleteCounts;
    }
    
    public int[] updateVins(List<Vin> vins){
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(vins.toArray(new Vin[]{}));
        int[] updateCounts = jdbcTemplate.batchUpdate(
            "update public.vins set date = :date, casier = :casier, annee = :annee, pays = :pays, region = :region, appellation = :appellation, vigneron = :vigneron, qualite = :qualite, stock = :stock, prix_btl = :prixBtl, annee_consommation = :anneeConsommation where id = :id",
            batch);
        return updateCounts;
    }

    public int[] deleteVins(List<Vin> vins){
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(vins.toArray(new Vin[]{}));
        int[] updateCounts = jdbcTemplate.batchUpdate(
            "delete from public.vins where id = :id",
            batch);
        return updateCounts;
    }
    
    public void shutdown(){
        jdbcTemplate.getJdbcOperations().execute("SHUTDOWN");
    }
    
    /**
     * Executes <code>ALTER TABLE</code> statement to add new column.
     * If <code>defaultValue</code> is specified, then executes <code>UPDATE</code>
     * statement to set all values in the column to the specified default value.
     * @param tableName table name without schema prefix
     * @param columnName name of the new column
     * @param columnType HSQL compatible type name
     * @param defaultValue default value to set in the column for all table entries
     * @see JdbcOperations#execute(java.lang.String)
     */
    public void addColumn(String tableName, String columnName, String columnType, String defaultValue){
        String sqlAlter = "alter table public." +
                tableName + " add column " + columnName + " " + columnType;
        jdbcTemplate.getJdbcOperations().execute(sqlAlter);
        if (defaultValue!=null){
            String sqlUpdate = "update public."
                    + tableName
                    + " set "
                    + columnName
                    + "="
                    + defaultValue
                    + " where "
                    + columnName
                    + " is null";
            jdbcTemplate.getJdbcOperations().execute(sqlUpdate);
        }
    }
}
