package com.cmr.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;

/**
 * https://kb.itextpdf.com/home/it7kb/examples/adding-a-background-to-a-table
 */
public class TableWithColoredBackground {

    public static final String DEST = "results/examples/colored_background.pdf";

    public static void main(String[] args) throws Exception {
        File file = new File(DEST);
        file.getParentFile().mkdirs();

        new TableWithColoredBackground().manipulatePdf(DEST);
    }

    protected void manipulatePdf(String dest) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
        Document doc = new Document(pdfDoc);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Table table = new Table(UnitValue.createPercentArray(16)).useAllAvailableWidth();

        for (int aw = 0; aw < 16; aw++) {
            Cell cell = new Cell().add(new Paragraph("hi this")
                    .setFont(font)
                    .setFontColor(ColorConstants.WHITE));
            cell.setBackgroundColor(ColorConstants.DARK_GRAY);
            cell.setBorder(new SolidBorder(1.0f));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }

        doc.add(table);

        doc.close();
    }
}
