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
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DoubleBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;


public class PurchaseOrderPDF {
    private static final Logger logger = LogManager.getLogger(PurchaseOrderPDF.class.getName());
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

        //Vendor Lines
        doc.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.0f));
        vendorShipToLines3(doc, purchaseOrder);//Header
        doc.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.5f));


        //Purchase Order Table of items
        float[] columnWidths = {2, 20, 4, 4, 4};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();


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




    private void vendorShipToLines(Document doc, PurchaseOrder purchaseOrder) {
        Table table = new Table(3);
        table.useAllAvailableWidth();
        String vendorLabel = "Vendor: ";
        String vendorLeading = String.format("%" + vendorLabel.length() + "s", "");
        String shipToLabel = "Ship To: ";
        String shipToLeading = String.format("%" + shipToLabel.length() + "s", "");
        logger.trace("Vendor Label: " + vendorLabel.length() + "[" + vendorLeading + "]");
        logger.trace("Ship To Label: " + shipToLabel.length() + "[" + shipToLeading + "]");
        //Left: Vendor
        Text label = createHeaderLabel(vendorLabel);
        Text text = getText(purchaseOrder.getVendor().getName());
        Paragraph paragraph = createHeaderParagraph(label).add(text).add("\n").setMultipliedLeading(1);
        text = getText(vendorLeading + purchaseOrder.getVendor().getAddress());
        paragraph.add(text).add("\n");
        text = getText(vendorLeading + purchaseOrder.getVendor().getCity());
        paragraph.add(text).add(", ");
        text = getText(vendorLeading + purchaseOrder.getVendor().getState());
        paragraph.add(text).add("\n");
        text = getText(vendorLeading + purchaseOrder.getVendor().getZip());
        paragraph.add(text).add("\n");
        text = getText(vendorLeading + purchaseOrder.getVendor().getPhone());
        paragraph.add(text).add("\n");
        text = getText(vendorLeading + purchaseOrder.getVendor().getEmail());
        paragraph.add(text);
        table.addCell(new Cell().add(paragraph));
        //Middle: NOTHING
        //table.addCell(new Cell().add(new Paragraph("Middle")).setFontSize(Constants.LABEL_FONT_SIZE));
        //Right: Ship To

        label = createHeaderLabel(shipToLabel);
        text = getText(purchaseOrder.getShipTo().getName());
        paragraph = createHeaderParagraph(label).add(text).add("\n").setMultipliedLeading(1);
        text = getText(purchaseOrder.getShipTo().getAddress());
        paragraph.add(shipToLeading).add(text).add("\n");
        text = getText(purchaseOrder.getShipTo().getCity());
        paragraph.add(shipToLeading).add(text).add(", ");
        text = getText(purchaseOrder.getShipTo().getState());
        paragraph.add(shipToLeading).add(text).add("\n");
        text = getText(purchaseOrder.getShipTo().getZip());
        paragraph.add(shipToLeading).add(text).add("\n");
        text = getText(purchaseOrder.getShipTo().getPhone());
        paragraph.add(shipToLeading).add(text).add("\n");
        text = getText(purchaseOrder.getShipTo().getEmail());
        paragraph.add(shipToLeading).add(text);
        table.addCell(new Cell().add(paragraph));
        doc.add(table);
    }

    private Text getText(String name) {
        return new Text(name).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
    }

    private void vendorShipToLines2(Document doc, PurchaseOrder purchaseOrder) {
        Table table = new Table(3);
        table.useAllAvailableWidth();
        String vendorLabel = "Vendor: ";
        String vendorLeading = String.format("%" + vendorLabel.length() + "s", "");
        String shipToLabel = "Ship To: ";
        String shipToLeading = String.format("%" + shipToLabel.length() + "s", "");
        logger.trace("Vendor Label: " + vendorLabel.length() + "[" + vendorLeading + "]");
        logger.trace("Ship To Label: " + shipToLabel.length() + "[" + shipToLeading + "]");
        logger.trace(String.format("Document Leading: %f",doc.getProperty(Property.LEADING)));
        //Left: Vendor
        List list = new List();
        Text label = createHeaderLabel(vendorLabel);
        list.setListSymbol(label);
        Text text = getText(purchaseOrder.getVendor().getName());
        list.add(purchaseOrder.getVendor().getName()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.setListSymbol(vendorLeading);
        list.add(purchaseOrder.getVendor().getAddress()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getCity()).add(", ").add(purchaseOrder.getVendor().getState()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getZip()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getCountry()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getPhone()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getFax()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getVendor().getEmail()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        table.addCell(new Cell().add(list));
        //Middle: NOTHING
        //table.addCell(new Cell().add(new Paragraph("Middle")).setFontSize(Constants.LABEL_FONT_SIZE));
        //Right: Ship To
        list = new List();
        label = createHeaderLabel(shipToLabel);
        list.setListSymbol(label);
        list.add(purchaseOrder.getShipTo().getName()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.setListSymbol(shipToLeading);
        list.add(purchaseOrder.getShipTo().getAddress()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getCity()).add(", ").add(purchaseOrder.getVendor().getState()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getZip()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getCountry()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getPhone()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getFax()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        list.add(purchaseOrder.getShipTo().getEmail()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE);
        table.addCell(new Cell().add(list));
        doc.add(table);
    }

    private void vendorShipToLines3(Document doc, PurchaseOrder purchaseOrder) {
        float[] columnWidths = {1,3,3,1,3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();
        table.setBorder(Border.NO_BORDER);
//        table.setBorderBottom(new DoubleBorder(ColorConstants.BLACK, 1));
        String vendorLabel = "Vendor: ";
        String vendorLeading = String.format("%" + vendorLabel.length() + "s", "");
        String shipToLabel = "Ship To: ";
        String shipToLeading = String.format("%" + shipToLabel.length() + "s", "");
        logger.trace("Vendor Label: " + vendorLabel.length() + "[" + vendorLeading + "]");
        logger.trace("Ship To Label: " + shipToLabel.length() + "[" + shipToLeading + "]");
        //Row 0
        table.addCell(createHeaderCell(new Paragraph("")));//0,1
        table.addCell(createHeaderCell(new Paragraph("")));//0,2
        table.addCell(createHeaderCell(new Paragraph("")));//0,3
        table.addCell(createHeaderCell(new Paragraph("")));//0,4
        table.addCell(createHeaderCell(new Paragraph("")));//0,5
        //Row 1
        Text label = createHeaderLabel(vendorLabel);
        Paragraph paragraph;
        Cell cell = createHeaderCell(createHeaderParagraph(label));
        table.addCell(cell);//1,1
        Text text;
         paragraph = createHeaderParagraph(createHeaderText(purchaseOrder));
        table.addCell(createHeaderCell(paragraph));//1,2
        table.addCell(createHeaderCell(new Paragraph("")));//1,3 Middle
        paragraph = createHeaderParagraph(createHeaderLabel(shipToLabel));
        table.addCell(createHeaderCell(paragraph));//1,4
        text = getText(purchaseOrder.getShipTo().getName());
        table.addCell(createHeaderCell(createHeaderParagraph(text)));//1,5
        //Row 2
        table.addCell(createHeaderCell(new Paragraph("")));//2,1
        table.addCell(createHeaderCell(createHeaderParagraph(getText(purchaseOrder.getVendor().getAddress()))));//2,2
        table.addCell(createHeaderCell(new Paragraph("")));//2,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//2,4
        table.addCell(createHeaderCell(createHeaderParagraph(getText( purchaseOrder.getShipTo().getAddress()))));//2,5
        //Row 3
        table.addCell(createHeaderCell(new Paragraph("")));//3,1
        table.addCell(createHeaderCell(createHeaderParagraph(new Text(purchaseOrder.getVendor().getCity()+", "+purchaseOrder.getVendor().getState()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE))));//3,2
        table.addCell(createHeaderCell(new Paragraph("")));//3,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//3,4
        paragraph = createHeaderParagraph(new Text(purchaseOrder.getShipTo().getCity()+", "+purchaseOrder.getShipTo().getState()).setFontSize(Constants.TABLE_ITEM_FONT_SIZE));
        table.addCell(createHeaderCell(paragraph));//3,5
        //Row 4
        table.addCell(createHeaderCell(new Paragraph("")));//4,1
        paragraph = createHeaderParagraph(getText( purchaseOrder.getVendor().getCountry()));
        table.addCell(createHeaderCell(paragraph));//4,2
        table.addCell(createHeaderCell(new Paragraph("")));//4,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//4,4
        paragraph = createHeaderParagraph(getText( purchaseOrder.getShipTo().getCountry()));
        table.addCell(createHeaderCell(paragraph));//4,5
        // Row 5
        table.addCell(createHeaderCell(new Paragraph("")));//5,1
        paragraph = createHeaderParagraph(getText(purchaseOrder.getVendor().getZip()));
        table.addCell(createHeaderCell(paragraph));//5,2
        table.addCell(createHeaderCell(new Paragraph("")));//5,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//5,4
        paragraph = createHeaderParagraph(getText( purchaseOrder.getShipTo().getZip()));
        table.addCell(createHeaderCell(paragraph));//5,5
        //Row 6
        table.addCell(createHeaderCell(new Paragraph("")));//6,1
        paragraph = createHeaderParagraph(getText(purchaseOrder.getVendor().getPhone()));
        table.addCell(createHeaderCell(paragraph));//6,2
        table.addCell(createHeaderCell(new Paragraph("")));//6,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//6,4
        paragraph = createHeaderParagraph(getText(purchaseOrder.getShipTo().getPhone()));
        table.addCell(createHeaderCell(paragraph));//6,5
        //Row 7
        table.addCell(createHeaderCell(new Paragraph("")));//7,1
        paragraph = createHeaderParagraph(getText(purchaseOrder.getVendor().getEmail()));
        table.addCell(createHeaderCell(paragraph));//7,2
        table.addCell(createHeaderCell(new Paragraph("")));//7,3 Middle
        table.addCell(createHeaderCell(new Paragraph("")));//7,4
        paragraph = createHeaderParagraph(getText(purchaseOrder.getShipTo().getEmail()));
        table.addCell(createHeaderCell(paragraph));//7,5
        //Row 8 - Blank
        table.addCell(createHeaderCell(new Paragraph("")));//8,1
        doc.add(table);
    }

    private Paragraph createHeaderParagraph(Text label) {
        return new Paragraph().add(label);
    }

    private Text createHeaderText(PurchaseOrder purchaseOrder) {
        return getText(purchaseOrder.getVendor().getName());
    }

    private Cell createHeaderCell(Paragraph paragraph) {
        return new Cell().add(paragraph).setBorder(Border.NO_BORDER);
    }

    private Text createHeaderLabel(String vendorLabel) {
        return new Text(vendorLabel).setBold().setFontSize(Constants.LABEL_FONT_SIZE);
    }

    private void setBlankTable(Table table, int i) {
        logger.debug(String.format("Columns: %d",table.getNumberOfColumns()));
        logger.debug(String.format("Rows: %d",table.getNumberOfRows()));
        logger.debug(String.format("IsEmpty: %b",table.isEmpty()));
        for (int j = 0; j < i; j++) {
            table.addCell(new Cell().add(new Paragraph("")));
        }
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
            canvas.showTextAligned(new Paragraph("NOT A REAL PO!"), pageSize.getRight() / 2, pageSize.getTop() / 2, pdfDoc.getPageNumber(page),
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
            headerLogoImage.setFixedPosition(i, 45, pdfDoc.getPage(i).getPageSize().getHeight() - 33);
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
