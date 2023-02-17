package com.cmr.pdf;

import com.cmr.domain.PurchaseOrder;
import com.cmr.system.Constants;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;


public class PurchaseOrderPDF {

    PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont cellFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont informationFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

    public PurchaseOrderPDF() throws IOException {
    }

    public void manipulatePdf(PurchaseOrder purchaseOrder) throws Exception {


        //Build the tmp & final  file locations
        String workingFileStr = Constants.TMP_DIR + "tmpPO" + purchaseOrder.getPoNumber() + Constants.PDF_FILE_EXTENSION;
        File tmpFile = new File(workingFileStr);
        String finalFileStr = Constants.RESULTS_DIR + "PO" + purchaseOrder.getPoNumber() + Constants.PDF_FILE_EXTENSION;
        File finalFile = new File(finalFileStr);

        //The following will make the directory structure(s) if they do not exist.
        tmpFile.getParentFile().mkdirs();
        finalFile.getParentFile().mkdirs();

        //construct and initialize the document
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(tmpFile));
        //TODO: Do I need this event handler anymore?
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new MyEventHandler());
        Document doc = new Document(pdfDoc, PageSize.LETTER);

        //Header
        float[] columnWidths = {2, 5, 5};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();

        //TODO: The following call is debug info - remove when done.
        displayColumnWidthsOf(table);
        Table testTable = new Table(UnitValue.createPointArray(columnWidths));
        displayColumnWidthsOf(testTable);

        Image logoImage = new Image(ImageDataFactory.create(Constants.HEADER_LOGO)).setAutoScale(true)
                .setAutoScaleHeight(true)
                .setAutoScaleWidth(true);
        Cell logoCell = new Cell().add(logoImage);
        table.addHeaderCell(logoCell);
        com.itextpdf.layout.element.Text text = new Text("Purchase Order: " + purchaseOrder.getPoNumber())
                .setTextRise(2.0f)
                .setFontSize(13)
                .setFontColor(ColorConstants.BLACK)
                .setBackgroundColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.LEFT);
        com.itextpdf.layout.element.Text text2 = new Text("Purchase Order: " + purchaseOrder.getPoNumber())
                .setTextRise(2.0f)
                .setFontSize(13)
                .setFontColor(ColorConstants.BLACK)
                .setBackgroundColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.RIGHT);

        Cell cell = new Cell(1, 2)
                .add(new Paragraph(text).add(text2));

        table.addHeaderCell(cell);
        doc.add(table);
        doc.add(new Paragraph("Insert Data Fields Here"));
        table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();

        for (int i = 0; i < 2; i++) {
            Cell[] headerFooter = new Cell[]{
                    cellHeaderFactory("Item #\nSKU\nPart #"),
                    cellHeaderFactory("Description"),
                    cellHeaderFactory("QTY")
            };

            for (Cell hfCell : headerFooter) {
                if (i == 0) {
                    table.addHeaderCell(hfCell);
                } else {
//                    table.addFooterCell(hfCell);
                }
            }
        }


        for (int counter = 0; counter < 100; counter++) {
            if (counter == 0) {
                tableDataFactory(table, String.valueOf((counter + 1)));
                tableDataFactory(table, "Description - This is a very long description and  This is a very long description and  This is a very long description and  This is a very long description and  This is a very long description and  This is a very long description and  This is a very long description and  This is a very long description and  " + (counter + 1));
                tableDataFactory(table, "Quantity" + (counter + 1));
            } else {
                tableDataFactory(table, String.valueOf((counter + 1)));
                tableDataFactory(table, "Description  " + (counter + 1));
                tableDataFactory(table, "Quantity" + (counter + 1));
            }
        }
        doc.add(table);
        doc.close();
        addPageNumbersXofY(workingFileStr, finalFileStr);
    }

    private Table tableDataFactory(Table table, String s) {
        return table.addCell(new Cell().setTextAlignment(TextAlignment.LEFT)
                .setFontSize(10)
                .add(new Paragraph(s)));
    }

    private Cell cellHeaderFactory(String s) {
        return new Cell().setBackgroundColor(new DeviceGray(0.75f)).add(new Paragraph(s)).setTextAlignment(TextAlignment.CENTER);
    }

    private void displayColumnWidthsOf(Table table) {
        for (int i = 0; i < table.getNumberOfColumns(); i++)
            System.out.println("Column "
                    + i
                    + ": "
                    + table.getColumnWidth(i));
    }

    protected class MyEventHandler implements IEventHandler {

        @SneakyThrows
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            int lineX=45;

            pdfCanvas
                    .setStrokeColor(ColorConstants.BLACK)
                    .moveTo(page.getPageSize().getLeft(),page.getPageSize().getHeight()-35)
                    .lineTo(page.getPageSize().getRight(),page.getPageSize().getHeight()-35)
                    .closePathStroke();
            System.out.println("Letter Page Size is : X ::" + page.getPageSize().getX() + ", Y ::" + page.getPageSize().getY());

            //Add watermark
            Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
            canvas.setFontColor(ColorConstants.LIGHT_GRAY);
            canvas.setProperty(Property.FONT_SIZE, UnitValue.createPointValue(120));
            canvas.setProperty(Property.FONT, informationFont);
            canvas.showTextAligned(new Paragraph("Draft"), 298, 421, pdfDoc.getPageNumber(page),
                    TextAlignment.CENTER, VerticalAlignment.MIDDLE, 45);

            pdfCanvas.release();
        }
    }

    private void logImageData(Image headerLogoImage) {
        System.out.println("Image Height: " + headerLogoImage.getImageHeight());
        System.out.println("Image Scaled Height:: " + headerLogoImage.getImageScaledHeight());
        System.out.println("Image Width: " + headerLogoImage.getImageWidth());
        System.out.println("Image Scaled Width:: " + headerLogoImage.getImageScaledWidth());
    }

    protected void addPageNumbersXofY(String src, String dest) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
        Document doc = new Document(pdfDoc);
        ImageData logoImageData = ImageDataFactory.create(Constants.HEADER_LOGO);
        Image headerLogoImage = new Image(logoImageData);
        headerLogoImage.scaleToFit(65,65);
        logImageData(headerLogoImage);

        int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {

            // Write aligned text to the specified by parameters point
            doc.showTextAligned(new Paragraph(String.format("PO [%s] - page %s of %s", "23sdfdsfasdfdsf412", i, numberOfPages)),
                    306, 25, i, TextAlignment.CENTER, VerticalAlignment.TOP, 0);
            headerLogoImage.setFixedPosition(i,45,pdfDoc.getPage(i).getPageSize().getHeight()-35);
            doc.add(headerLogoImage);
        }

        doc.close();

    }
}
