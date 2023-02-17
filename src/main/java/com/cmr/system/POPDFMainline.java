package com.cmr.system;

import com.cmr.domain.PurchaseOrder;
import com.cmr.factory.PurchaseOrderFactory;
import com.cmr.pdf.PurchaseOrderPDF;

public class POPDFMainline {
    public static void main(String[] args) throws Exception {
        PurchaseOrder purchaseOrder = PurchaseOrderFactory.createTestPO("45430");
        new PurchaseOrderPDF().manipulatePdf(purchaseOrder);
    }
}
