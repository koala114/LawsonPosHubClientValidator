package com.kargotest.alading;

import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
899438924020610070169000000000010005 -- 10.00
899776024020610060149000000000010004 -- 10.00

 */
public class ISVALaDingHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(ISVALaDingHandler.class);
    private static Map<String, AladingBarcode> mAladingBarcodes = new HashMap<>();
    private StringWriter sw = new StringWriter();

    private Map<String, String> msgHead = null;
    private Map<String, String> msgBody = null;

    private String OPType = "";

    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String result = "";
        String aladingBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        msgHead = parseXMLString(aladingBody, "//Head/*");
        msgBody = parseXMLString(aladingBody, "//Body/*");
        logger.info(aladingBody);
        OPType = msgBody.get("OpType");
        try{
            if(OPType.equals("1"))
                result = OPType1();
            else if(OPType.equals("2"))
                result = OPType2();
            else
                result = OPType0();

        }catch (Exception e){
            e.printStackTrace();
        }


        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);
        return 0;
    }


    public String OPType1() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = timeFormat.format(new Date());
        // Msg root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Msg");
        doc.appendChild(rootElement);

        // Head
        Element head = createHead(doc);
        rootElement.appendChild(head);

        // Body
        Element body = createBody4First(doc, msgBody);

        rootElement.appendChild(body);
        return toXMLString(doc);
    }

    public String OPType2() throws ParserConfigurationException {
        boolean properTransPrice = false;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        String code = "";
        String outTradeNo =  msgBody.get("POSOrderId");
        AladingBarcode aladingBarcode = null;

        // Msg root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Msg");
        doc.appendChild(rootElement);

        // Head
        Element head = createHead(doc);
        rootElement.appendChild(head);
        // Body
        Element body = doc.createElement("Body");
        Map<String, String> mBody= new HashMap<>();
        mBody.put("OpType", msgBody.get("OpType"));

        String transPrice = msgBody.get("TransPrice");

        if(!msgBody.containsKey("PayMethod")){
            // 没传券号，即撤销该券
            Element returnValue = doc.createElement("ReturnValue");
            returnValue.setTextContent("0000");
            body.appendChild(returnValue);
            Element returnMessage = doc.createElement("ReturnMessage");
            returnMessage.setTextContent("成功");
            body.appendChild(returnMessage);
            rootElement.appendChild(body);
            return toXMLString(doc);
        }

        if(transPrice.equals(msgBody.get("PayMethod")) && Double.valueOf(transPrice)%10 == 0)
            properTransPrice = true;

        if(mAladingBarcodes.containsKey(outTradeNo)){
            aladingBarcode = mAladingBarcodes.get(outTradeNo);
            code = aladingBarcode.getBarcode();
            if(code.substring(code.length() - 4).equals("0009") || !properTransPrice){
                // 验证TransPrice，即阿拉订券不是10元，表明券金额错误
                mBody.put("ReturnValue", "0009");
                mBody.put("ReturnMessage", "券号结尾是0009或者券金额不正确");
            }
            else {
                mBody.put("ReturnValue", "0000");
                mBody.put("ReturnMessage", "成功");
            }

            for (Map.Entry<String, String> e : mBody.entrySet()) {
                Element element = doc.createElement(e.getKey());
                element.setTextContent(e.getValue());
                body.appendChild(element);
            }
        }
        rootElement.appendChild(body);

        return toXMLString(doc);
    }

    public String OPType0() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Msg root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Msg");
        doc.appendChild(rootElement);

        // Head
        Element head = createHead(doc);
        rootElement.appendChild(head);
        // Body
        Element body = createBody4Query(doc);

        rootElement.appendChild(body);
        return toXMLString(doc);
    }

    public Element createHead(Document doc){
        // Head
        Element head = doc.createElement("Head");

        for (Map.Entry<String, String> entry : msgHead.entrySet()) {
            if(entry.getKey().equals("DllSubmitTime") || entry.getKey().equals("Platform"))
                continue;
            Element element = doc.createElement(entry.getKey());
            element.setTextContent(entry.getValue());
            head.appendChild(element);
        }
        return head;
    }

    public Element createBody4First(Document doc, Map<String, String> msgBody){
        String code =  msgBody.get("Barcode");
        String outTradeNo =  msgBody.get("POSOrderId");
        double amount = 10.0; //券金额

        mAladingBarcodes.put(outTradeNo, new AladingBarcode(code, amount));

        // Body
        Element body = doc.createElement("Body");
        Map<String, String> mBody= new HashMap<>();
        mBody.put("OpType", msgBody.get("OpType"));
        mBody.put("Barcode", code);
        mBody.put("BarCodeType", "0"); // 0代金券
        mBody.put("CashierStep", "2"); // 2次交互

        Map<String, String> mBarCodeDetail= new HashMap<>();
        Element barCodeDetail = doc.createElement("BarCodeDetail");

        if(code.substring(code.length() - 4).equals("8001")){
            mBarCodeDetail.put("name", "密码收银");
            mBarCodeDetail.put("Category", "0");
            for (Map.Entry<String, String> entry : mBarCodeDetail.entrySet()) {
                Element element = doc.createElement(entry.getKey());
                element.setTextContent(entry.getValue());
                barCodeDetail.appendChild(element);
            }

            mBody.put("ReturnValue", "8001");
            mBody.put("ReturnMessage", "请耐心等待用户输入>密码，兑换处理中...");
        }
        else {
            mBarCodeDetail.put("name", "代金券收银");
            mBarCodeDetail.put("Category", "0");
            mBarCodeDetail.put("Amount", String.valueOf(amount));
            mBarCodeDetail.put("Brokerage", "0");
            mBarCodeDetail.put("BrokerageRate", "0");
            mBarCodeDetail.put("ShippingFee", "0");
            mBarCodeDetail.put("ExtraFee", "0");
            for (Map.Entry<String, String> entry : mBarCodeDetail.entrySet()) {
                Element element = doc.createElement(entry.getKey());
                element.setTextContent(entry.getValue());
                barCodeDetail.appendChild(element);
            }
            body.appendChild(barCodeDetail);

            Element fixed = doc.createElement("Fixed");
            Element goodsSet = doc.createElement("GoodsSet");
            Element goodsGroup = doc.createElement("GoodsGroup");
            Element goods = doc.createElement("Goods");
            Element gBarcode = doc.createElement("Barcode");
            gBarcode.setTextContent("9999999999999999999");
            Element gName = doc.createElement("Name");
            gName.setTextContent("测试可兑换的商品");
            Element gQuantity = doc.createElement("Quantity");
            gQuantity.setTextContent("1");
            Element gAmount = doc.createElement("Amount");
            gAmount.setTextContent("9999.99");
            goods.appendChild(gAmount);
            goods.appendChild(gName);
            goods.appendChild(gQuantity);
            goodsGroup.appendChild(goods);
            goodsSet.appendChild(goodsGroup);
            fixed.appendChild(goodsSet);
            body.appendChild(fixed);
            mBody.put("ReturnValue", "0000"); // 2次交互
            mBody.put("ReturnMessage", "成功"); // 2次交互
        }

        for (Map.Entry<String, String> entry : mBody.entrySet()) {
            Element element = doc.createElement(entry.getKey());
            element.setTextContent(entry.getValue());
            body.appendChild(element);
        }


        return body;
    }

    public Element createBody4Query(Document doc){
        AladingBarcode aladingBarcode = null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = timeFormat.format(new Date());
        String code =  msgBody.get("Barcode");
        // Body
        Element body = doc.createElement("Body");
        Map<String, String> mBody= new HashMap<>();

        Iterator<Map.Entry<String, AladingBarcode>> iterator = mAladingBarcodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AladingBarcode> entry = iterator.next();
            if(entry.getValue().getBarcode().equals(code)){
                aladingBarcode = entry.getValue();
                break;
            }
            else {
                mBody.put("OpType", msgBody.get("OpType"));
                mBody.put("Barcode", code);
                //mBody.put("BarCodeStatus", "2"); // 0:未使用;1:已锁定;2:已使用
                mBody.put("BarCodeUseTime", date); // 使用时间
                mBody.put("BarCodeUseChannel", "");
                //mBody.put("BarCodeUseShopCode", msgHead.get("ShopCode")); // 使用时间
                mBody.put("ReturnValue", "0009");
                mBody.put("ReturnMessage", "没有找到第一次请求");

                for (Map.Entry<String, String> e : mBody.entrySet()) {
                    Element element = doc.createElement(e.getKey());
                    element.setTextContent(e.getValue());
                    body.appendChild(element);
                }
                return body;
            }
        }

        int queryLimit = aladingBarcode.getQueryLimit();

        if(queryLimit > 0){
            Map<String, String> mBarCodeDetail= new HashMap<>();
            Element barCodeDetail = doc.createElement("BarCodeDetail");
            mBarCodeDetail.put("name", "密码收银");
            mBarCodeDetail.put("Category", "0");
            for (Map.Entry<String, String> entry : mBarCodeDetail.entrySet()) {
                Element element = doc.createElement(entry.getKey());
                element.setTextContent(entry.getValue());
                barCodeDetail.appendChild(element);
            }
            mBody.put("ReturnValue", "8001");
            mBody.put("ReturnMessage", "请耐心等待用户输入>密码，兑换处理中...");
            body.appendChild(barCodeDetail);
        }
        else {
            mBody.put("OpType", msgBody.get("OpType"));
            mBody.put("Barcode", code);
            mBody.put("BarCodeStatus", "2"); // 0:未使用;1:已锁定;2:已使用
            mBody.put("BarCodeUseTime", date); // 使用时间
            mBody.put("BarCodeUseChannel", "");
            mBody.put("BarCodeUseShopCode", msgHead.get("ShopCode")); // 使用时间
            mBody.put("ReturnValue", "0000");
            mBody.put("ReturnMessage", "成功");
        }

        for (Map.Entry<String, String> entry : mBody.entrySet()) {
            Element element = doc.createElement(entry.getKey());
            element.setTextContent(entry.getValue());
            body.appendChild(element);
        }

        // 更新查询次数
        aladingBarcode.setQueryLimit(--queryLimit);
        mAladingBarcodes.put(code, aladingBarcode);

        return body;
    }


    public Map<String, String> parseXMLString(String xmlStr, String xpathExpress) {
        Map<String, String> xml2Map = new HashMap<>();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlStr));
            document = docBuilder.parse(is);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile(xpathExpress);
            Object result = expr.evaluate(document, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            for (int i = 0; i < nodes.getLength(); i++) {
                String value = "";
                String key = nodes.item(i).getNodeName();
                if(key.equals("PayMethod")){
                    // 累加每张券的金额
                    XPathExpression e = xpath.compile("sum(//Body/PayMethod/Payment/Amount)");
                    Object v = Double.valueOf(e.evaluate(nodes.item(i).getChildNodes()));
                    xml2Map.put(key, v.toString());
                    continue;
                }
                if(nodes.item(i).hasChildNodes()){
                    value = nodes.item(i).getFirstChild().getNodeValue();
                }
                else
                    continue;
                xml2Map.put(key, value);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return xml2Map;
    }

    public String toXMLString(Document document) {
        try {
            sw.getBuffer().setLength(0);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(sw);
            transformer.transform(domSource, streamResult);

        } catch (TransformerException tfe) {
            System.out.println(tfe.getStackTrace());
        }
        return sw.toString();
    }

    class AladingBarcode{
        private int queryLimit = 5;
        private String barcode = "";
        private String outTradeNo = "";
        private double amout = 0.00;

        public double getAmout() {
            return amout;
        }

        public AladingBarcode(String code, double amout){
            this.barcode = code;
            this.amout = amout;
        }
        public int getQueryLimit() {
            return queryLimit;
        }

        public String getBarcode() {
            return barcode;
        }

        public String getOutTradeNo() {
            return outTradeNo;
        }

        public void setQueryLimit(int queryLimit) {
            this.queryLimit = queryLimit;
        }
    }
}
