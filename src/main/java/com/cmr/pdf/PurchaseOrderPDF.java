package com.cmr.pdf;

import com.cmr.domain.Item;
import com.cmr.domain.PurchaseOrder;
import com.cmr.support.Utils;
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
import com.itextpdf.layout.borders.DoubleBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;


public class PurchaseOrderPDF {
    private static final Logger logger = LogManager.getLogger(PurchaseOrderPDF.class.getName());
    PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont cellFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
    PdfFont informationFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

    public PurchaseOrderPDF() throws IOException {
    }

    public void manipulatePdf(PurchaseOrder purchaseOrder) throws Exception {
        logger.debug(Utils.prettyJson(purchaseOrder));
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
        logger.trace("Left Margin: " + doc.getLeftMargin());
        logger.trace("Right Margin: " + doc.getRightMargin());
        logger.trace("Top Margin: " + doc.getTopMargin());
        logger.trace("Bottom Margin: " + doc.getBottomMargin());
        //Header
        float[] columnWidths = {2, 20, 4, 4, 4};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();

        vendorShipToLines(doc);
        table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();

        for (int i = 0; i < 2; i++) {
            Cell[] headerFooter = new Cell[]{
                    cellHeaderFactory("Quantity"),
                    cellHeaderFactory("Description"),
                    cellHeaderFactory("Price"),
                    cellHeaderFactory("Unit"),
                    cellHeaderFactory("Extended")
            };

            for (Cell hfCell : headerFooter) {
                if (i == 0) {
                    table.addHeaderCell(hfCell);
                } else {
//                    table.addFooterCell(hfCell);
                }
            }
        }


        for (Item item :
                purchaseOrder.getItems()) {
            tableDataFactory(table, String.valueOf(item.getQuantity()));
            tableDataFactory(table, item.getDescription());
            tableDataFactory(table, String.format("$%,.2f", item.getPrice()), true);
            tableDataFactory(table, item.getUnit(), true);
            tableDataFactory(table, String.format("$%,.2f", item.getPrice() * item.getQuantity()), true);

        }
        doc.add(table);
        doc.add(new Paragraph(calculateTotal(purchaseOrder))
                .setBorderBottom(new DoubleBorder(1))
                .setBold()
                .setFontSize(Constants.PO_TABLE_TOTAL_FONT_SIZE)
                .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
        addPageNumbersXofY(workingFileStr, finalFileStr, purchaseOrder);
    }

    private void vendorShipToLines(Document doc) {
        doc.add(new Paragraph("Insert Data Fields Here"));
    }

    private String calculateTotal(PurchaseOrder purchaseOrder) {
        Double total = 0d;
        for (Item item :
                purchaseOrder.getItems()) {
            total = total + (item.getPrice() * item.getQuantity());
        }

        return String.format("Total Purchase Order:   $%,.2f", total);
    }

    private Table tableDataFactory(Table table, String s, boolean right) {
        if (right)
            return table.addCell(new Cell().setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(Constants.TABLE_ITEM_FONT_SIZE)
                    .add(new Paragraph(s)));
        else
            return tableDataFactory(table, s);
    }

    private Table tableDataFactory(Table table, String s) {
        return table.addCell(new Cell().setTextAlignment(TextAlignment.LEFT)
                .setFontSize(Constants.TABLE_ITEM_FONT_SIZE)
                .add(new Paragraph(s)));
    }

    private Cell cellHeaderFactory(String s) {
        return new Cell().setBackgroundColor(new DeviceGray(0.75f)).add(new Paragraph(s)).setTextAlignment(TextAlignment.CENTER);
    }

    private String displayColumnWidthsOf(Table table) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < table.getNumberOfColumns(); i++) {
            sb.append("Column "
                    + i
                    + ": "
                    + table.getColumnWidth(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    protected class MyEventHandler implements IEventHandler {

        @SneakyThrows
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            pdfCanvas
                    .setStrokeColor(ColorConstants.BLACK)
                    .moveTo(pageSize.getLeft(), pageSize.getHeight() - 35)
                    .lineTo(pageSize.getRight(), pageSize.getHeight() - 35)
                    .closePathStroke();
            logger.trace("Page Size is : X(RIGHT) ::" + pageSize.getRight() + ", Y(TOP) ::" + pageSize.getTop());
            logger.trace("Page Size is : X(WIDTH) ::" + pageSize.getWidth() + ", Y(HEIGHT) ::" + pageSize.getHeight());

            //Add watermark
            Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
            canvas.setFontColor(ColorConstants.LIGHT_GRAY);
            canvas.setProperty(Property.FONT_SIZE, UnitValue.createPointValue(120));
            canvas.setProperty(Property.FONT, informationFont);
            canvas.showTextAligned(new Paragraph("Draft"), pageSize.getRight() / 2, pageSize.getTop() / 2, pdfDoc.getPageNumber(page),
                    TextAlignment.CENTER, VerticalAlignment.MIDDLE, 45);

            pdfCanvas.release();
        }
    }

    private void logImageData(Image headerLogoImage) {
        logger.trace("Image Height: " + headerLogoImage.getImageHeight());
        logger.trace("Image Scaled Height:: " + headerLogoImage.getImageScaledHeight());
        logger.trace("Image Width: " + headerLogoImage.getImageWidth());
        logger.trace("Image Scaled Width:: " + headerLogoImage.getImageScaledWidth());
    }

    protected void addPageNumbersXofY(String src, String dest, PurchaseOrder purchaseOrder) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
        Document doc = new Document(pdfDoc);
        ImageData logoImageData = ImageDataFactory.create(Constants.HEADER_LOGO);
        Image headerLogoImage = new Image(logoImageData);
        headerLogoImage.scaleToFit(Constants.LOGO_SCALE_TO_FIT_WIDTH, Constants.LOGO_SCALE_TO_FIT_HEIGHT);
        logImageData(headerLogoImage);

        int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {

            //put footer on each page
            doc.showTextAligned(new Paragraph(String.format("PO [%s] - page %s of %s", purchaseOrder.getPoNumber(), i, numberOfPages)).setFontSize(Constants.FOOTER_FONT_SIZE),
                            pdfDoc.getPage(i).getPageSize().getWidth() / 2,
                            Constants.FOOTER_HEIGHT_FROM_BOTTOM,
                            i,
                            TextAlignment.CENTER,
                            VerticalAlignment.TOP,
                            0)

            ;
            //put header on each page - including PO number
            headerLogoImage.setFixedPosition(i, 45, pdfDoc.getPage(i).getPageSize().getHeight() - 35);
            doc.add(headerLogoImage);
            doc.showTextAligned(new Paragraph(String.format("Purchase Order  %s", purchaseOrder.getPoNumber())).setFontSize(Constants.PO_TITLE_FONT_SIZE),
                    pdfDoc.getPage(i).getPageSize().getWidth() / 2,
                    pdfDoc.getPage(i).getPageSize().getHeight() - 5,
                    i,
                    TextAlignment.CENTER,
                    VerticalAlignment.TOP,
                    0).setFontSize(Constants.PO_TITLE_FONT_SIZE);

        }

        doc.close();

    }
}
