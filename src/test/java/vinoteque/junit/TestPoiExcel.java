/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vinoteque.junit;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.junit.Test;


/**
 *
 * @author George Ushakov
 */
public class TestPoiExcel {

    @Test
    public void exportExcel() throws Exception {
        //Creating Cells

    Workbook wb = new HSSFWorkbook();
    //Workbook wb = new XSSFWorkbook();
    CreationHelper createHelper = wb.getCreationHelper();
    Sheet sheet = wb.createSheet("new sheet");

    // Create a row and put some cells in it. Rows are 0 based.
    Row row = sheet.createRow((short)0);
    // Create a cell and put a value in it.
    Cell cell = row.createCell(0);
    cell.setCellValue(1);

    // Or do it on one line.
    row.createCell(1).setCellValue(1.2);
    row.createCell(2).setCellValue(
         createHelper.createRichTextString("This is a string"));
    row.createCell(3).setCellValue(true);
    row.createCell(4).setCellValue("Hello World");

    // Write the output to a file
    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
    wb.write(fileOut);
    fileOut.close();
    
    Desktop dt = Desktop.getDesktop();
    dt.open(new File("workbook.xls"));

                    
    }
}
