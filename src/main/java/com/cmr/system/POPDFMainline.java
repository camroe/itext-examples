package com.cmr.system;

import com.cmr.domain.PurchaseOrder;
import com.cmr.factory.PurchaseOrderFactory;
import com.cmr.pdf.PurchaseOrderPDF;
import com.cmr.support.Utils;

public class POPDFMainline {
    public static void main(String[] args) throws Exception {
        PurchaseOrder purchaseOrder = PurchaseOrderFactory
                .createTestPO(String.valueOf(Utils.rndBetween(30000, 30400)),Constants.NUMBER_OF_TEST_ITEMS_TO_PRODUCE);
        new PurchaseOrderPDF().manipulatePdf(purchaseOrder);
    }
}
