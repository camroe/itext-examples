package com.cmr.pdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;

/**
 * https://kb.itextpdf.com/home/it7kb/examples/adding-a-background-to-a-table
 */
public class SimpleTable {
    public static final String DEST = "results/examples/simple_table.pdf";

    public static void main(String[] args) throws Exception {
        File file = new File(DEST);
        //This will make the directory structure if it does not exist.
        file.getParentFile().mkdirs();

        new SimpleTable().manipulatePdf(DEST);
    }

    protected void manipulatePdf(String dest) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);

        Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();

        for (int i = 0; i < 16; i++) {
            table.addCell("hi");
        }

        doc.add(table);

        doc.close();
    }
}
