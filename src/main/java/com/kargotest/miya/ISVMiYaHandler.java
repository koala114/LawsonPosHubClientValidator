package com.kargotest.miya;

import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ISVMiYaHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(ISVMiYaHandler.class);
    private static Map<String, MiYaReqDataField> trans = new HashMap<>();
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private StringWriter sw = new StringWriter();

    private Map<String, String> requestField;
    Map<String, String> dataField;
    private String result = "";

    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String miyaBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        requestField = parseXMLString(miyaBody, "//request/*");
        dataField = parseXMLString(miyaBody, "//data/*");

        logger.info(requestField);
        logger.info(dataField);

        String reqMethod = requestField.get("A6");
        result = miyaProcessor(reqMethod);

        resp.getHeaders().add("Content-Type", "application/xml");
        resp.send(200, result);
        return 0;
    }

    public String miyaProcessor(String reqMethod){
        try{
            switch (reqMethod){
                case "A":
                    return miyaEPayREDMP();
                case "B":
                    return miyaEPayQuery();
                case "C":
                    return miyaEPayREFUND();
                case "E":
                    return miyaEPayRESVAL();

            }
            result = miyaProcessor(reqMethod);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

    public String miyaEPayQuery() throws ParserConfigurationException{
        Document doc;
        Map<String, String> elements = new HashMap<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = timeFormat.format(new Date());
        String orignOrderId = dataField.get("B1");
        int counter = trans.get(orignOrderId).getQueryLimits();
        if( counter < 0){
            if(!trans.get(orignOrderId).getAmount().equals("999")){
                elements =  Stream.of(new String[][]{
                        {"C1", "SUCCESS"},
                        {"C2", "PAYSUCCESS"},
                        {"C3", "PAYSUCCESS"},
                        {"C4", "支付成功"},
                        {"C5",orignOrderId},
                        {"C6", "4200002057202401304875893631"},
                        {"C7", "1"},
                        {"C8", date},
                        {"C9", date},
                        {"C14", "支付成功"},
                        {"C15", "H"},
                }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
            }
            else {
                elements =  Stream.of(new String[][]{
                        {"C1", "SUCCESS"},
                        {"C2", "PAYFAIL"},
                        {"C3", "PAYFAIL"},
                        {"C4", "支付失败"},
                        {"C5",orignOrderId},
                        {"C6", "4200002057202401304875893631"},
                        {"C7", "1"},
                        {"C8", date},
                        {"C9", date},
                        {"C14", "支付失败"},
                        {"C15", "H"},
                }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
            }
        }
        else {
            elements =  Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "PAYWAIT"},
                    {"C3", "PAYWAIT"},
                    {"C4", "处理中"},
                    {"C5",orignOrderId},
                    {"C6", "4200002057202401304875893631"},
                    {"C7", "1"},
                    {"C8", date},
                    {"C9", date},
                    {"C14", "处理中"},
                    {"C15", "H"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }

        trans.get(orignOrderId).setQueryLimits(--counter);
        doc = map2XML(elements);
        return toXMLString(doc);
    }
    public String miyaEPayRESVAL() throws ParserConfigurationException {
        Document doc;
        String orignOrderId = "";
        Map<String, String> elements = new HashMap<>();
        orignOrderId = dataField.get("B1");
        if(!trans.containsKey(orignOrderId)) {
            elements = Stream.of(new String[][]{
                    {"C1", "FAIL"},
                    {"C2", "10000000803"},
                    {"C3", "10000000803"},
                    {"C4", "该订单不存在!"},
                    {"C5", orignOrderId}, // 原交易订单号
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
            doc = map2XML(elements);
        }
        else {
            switch (trans.get(orignOrderId).getBarcode().substring(0,2)){
                case "81":
                    elements = getAlipayResponse("E");
                    break;
                case "13":
                    elements = getWeChatResponse("E");
                    break;
                case "28":
                    elements = getCMPayResponse("E");
                    break;
            }
            doc = map2XML(elements);
        }
        return toXMLString(doc);
    }
    public String miyaEPayREFUND() throws ParserConfigurationException {
        Document doc;
        String orignOrderId = "";
        Map<String, String> elements = new HashMap<>();
        orignOrderId = dataField.get("B1");
        if(!trans.containsKey(orignOrderId)) {
            elements = Stream.of(new String[][]{
                    {"C1", "FAIL"},
                    {"C2", "10000000803"},
                    {"C3", "10000000803"},
                    {"C4", "该订单不存在!"},
                    {"C5", orignOrderId}, // 原交易订单号
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
            doc = map2XML(elements);
        }
        else {
            switch (trans.get(orignOrderId).getBarcode().substring(0,2)){
                case "81":
                    elements = getCMPayResponse("C");
                    break;
                case "13":
                    elements = getWeChatResponse("C");
                    elements.put("C5",orignOrderId);
                    break;
                case "28":
                    elements = getAlipayResponse("C");
                    break;
            }
            doc = map2XML(elements);
        }
        return toXMLString(doc);
    }

    public String miyaEPayREDMP() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("xml");
        document.appendChild(root);
        String pan = "";

        Map<String, String> elements = new HashMap<>();
        pan = dataField.get("B2");
        trans.put(dataField.get("B1"), new MiYaReqDataField(dataField));
        if(pan.startsWith("28")){
            elements = getAlipayResponse("A");
        }
        else if(pan.startsWith("13")){
            elements = getWeChatResponse("A");
        }
        else if(pan.startsWith("81")){
            elements = getCMPayResponse("A");
        }

        for (Map.Entry<String, String> entry : elements.entrySet()
        ) {
            String key = entry.getKey();
            String value = entry.getValue();
            Element subElement = document.createElement(key);
            subElement.setTextContent(value);
            root.appendChild(subElement);
        }
        return toXMLString(document);
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
                String key = nodes.item(i).getNodeName();
                String value = nodes.item(i).getFirstChild().getNodeValue();
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

    public Document map2XML(Map<String, String> elements) throws ParserConfigurationException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("xml");
        document.appendChild(root);
        for (Map.Entry<String, String> entry : elements.entrySet()
        ) {
            String key = entry.getKey();
            String value = entry.getValue();
            Element subElement = document.createElement(key);
            subElement.setTextContent(value);
            root.appendChild(subElement);
        }

        return document;
    }

    public Map<String, String> getAlipayResponse(String reqMethod) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = timeFormat.format(new Date());
        if (reqMethod.equals("A")) {
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "PAYSUCCESS"},
                    {"C3", "PAYSUCCESS"},
                    {"C4", "[支付宝]支付成功"},
                    {"C5", dataField.get("B1")},
                    {"C6", "2024012922001441401443289858"},
                    {"C7", dataField.get("B4")}, // 金额
                    {"C8", "3"}, //1-微信,3-支付宝
                    {"C9", "for***@126.com"},
                    {"C10", "[1|0|0|0|0|0]"},
                    {"C12", date},
                    {"C13", date},
                    {"C14", "[支付宝]支付成功"},
                    {"C19", "2088002764241406"},
                    {"C24", "支付宝"},
                    {"C25", "PCREDIT:0.01"},
                    {"C30", "2D5AFD567E2638EB1F7569EA62CCDCDB"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        } else if (reqMethod.equals("C")) {
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "REFUNDSUCCESS"},
                    {"C3", "REFUNDSUCCESS"},
                    {"C4", "[支付宝]退款成功!累计退款金额XX元"},
                    {"C5", dataField.get("B1")},
                    {"C6", "2024012922001441401443289858"},
                    {"C7", dataField.get("B2")},
                    {"C8", trans.get(dataField.get("B1")).getAmount()},
                    {"C9", dataField.get("B4")},
                    {"C10", "3"}, // 3-支付宝
                    {"C11", date},
                    {"C12", date},
                    {"C14", "[支付宝]退款成功!累计退款金额XX元"},
                    {"C16", "[1|0|0|0|0|0]"},
                    {"C24", "支付宝"},
                    {"C25", "PCREDIT:0.01"},
                    {"C30", "239B88DB344B35E6240F6471ED776D8D"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        return null;
    }

    public Map<String, String> getWeChatResponse(String reqMethod){
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = timeFormat.format(new Date());

        if(reqMethod.equals("A")) {
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "PAYWAIT"},
                    {"C3", "PAYWAIT"},
                    {"C4", "[微信]支付处理中！"},
                    {"C5", dataField.get("B1")},
                    {"C6", "4200002057202401304875893631"},
                    {"C7", dataField.get("B4")}, // 金额
                    {"C8", "1"}, //1-微信,3-支付宝
                    {"C9", "o1vUMt-1PRt0Le-s78kN5fr1jvnY"},
                    {"C10", "[1|0|0|0|0|0]"},
                    {"C12", date},
                    {"C13", date},
                    {"C14", "[微信]支付处理中！"},
                    {"C17", "wx273772ce26499402"},
                    {"C18", "owYXn536-c5nmFfeGLOrX_mxkyv0"},
                    {"C19", "wxc80ace02b7a6968d"},
                    {"C24", "微信"},
                    {"C29", "{}"},
                    {"C30", "BEA1052B5DE29322991C6276DB1B07D1"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        else if(reqMethod.equals("C")){
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "REFUNDSUCCESS"},
                    {"C3", "REFUNDSUCCESS"},
                    {"C4", "[微信]退款成功！"},
                    {"C5", dataField.get("B1")},
                    {"C6", "4200002057202401304875893631"},
                    {"C7", dataField.get("B2")},
                    {"C8", trans.get(dataField.get("B1")).getAmount()},
                    {"C9", dataField.get("B4")},
                    {"C10", "1"}, // 1-微信
                    {"C11", date},
                    {"C12", date},
                    {"C14", "[微信]退款成功！"},
                    {"C16", "[1|0|0|0|0|0]"},
                    {"C24", "微信"},
                    {"C30", "079879249D13C6EA7CE90D90736C91DF"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        else if(reqMethod.equals("E")){
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "CANCELSUCCESS"},
                    {"C3", "CANCELSUCCESS"},
                    {"C4", "[微信]撤销成功！"},
                    {"C5", dataField.get("B1")},
                    {"C7", "1"}, //1-微信,3-支付宝
                    {"C9", date},
                    {"C10", trans.get(dataField.get("B1")).getAmount()},
                    {"C14", "[微信]撤销成功！"},
                    {"C24", "微信"},
                    {"C30", "6BCB196B5522029BB3AE04D898DAA72D"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        return null;
    }

    public Map<String, String> getCMPayResponse(String reqMethod){
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = timeFormat.format(new Date());
        MiYaReqDataField orignData = trans.get(dataField.get("B1"));

        if(reqMethod.equals("A")){
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "PAYSUCCESS"},
                    {"C3", "000000"},
                    {"C4", "SUCCESS"},
                    {"C5", dataField.get("B1")},
                    {"C6", "820000004811372076"},
                    {"C7", dataField.get("B4")}, // 金额
                    {"C8", "U"},
                    {"C9", "13818595461"},
                    {"C10", "[1|0|0|0|0|0]"},
                    {"C12", date},
                    {"C13", date},
                    {"C14", "[和包]订单" + dataField.get("B1") + "支付成功!"},
                    {"C24", "移动和包"},
                    {"C30", "D679CC377919AD61BEE88932FB2C7C94"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        else if(reqMethod.equals("C")){
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "REFUNDSUCCESS"},
                    {"C3", "REFUNDSUCCESS"},
                    {"C4", "[和包]订单" + dataField.get("B1") + "退款成功!"},
                    {"C5", dataField.get("B1")}, // 原交易订单号
                    {"C7", dataField.get("B2")}, // 退款订单号
                    {"C8", orignData.getAmount()}, // 原订单金额
                    {"C9", dataField.get("B4")}, // 退款金额
                    {"C10", "U"},
                    {"C11", date},
                    {"C12", date},
                    {"C14", "[和包]订单" + dataField.get("B1") + "退款成功!"},
                    {"C24", "移动和包"},
                    {"C30", "CB757D6C09DBCB71B20A29B2C86A50D1"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        else if(reqMethod.equals("E")){
            return Stream.of(new String[][]{
                    {"C1", "SUCCESS"},
                    {"C2", "CANCELSUCCESS"},
                    {"C3", "CANCELSUCCESS"},
                    {"C4", "[和包]订单" + dataField.get("B1") + "撤销成功!"},
                    {"C5", dataField.get("B1")}, // 原交易订单号
                    {"C7", "U"},
                    {"C8", orignData.getAmount()}, // 原订单金额
                    {"C10", "U"},
                    {"C11", date},
                    {"C14", "[和包]订单" + dataField.get("B1") + "撤销成功!"},
                    {"C24", "移动和包"},
                    {"C30", "989A87F92A403784970538991BAB245E"},
            }).collect(Collectors.toMap(d -> d[0], d -> d[1]));
        }
        return null;
    }

    class MiYaReqDataField{
        int queryLimits = 5;
        String B4 = ""; // 金额
        String pan = "";

        MiYaReqDataField(Map<String, String> data){
            B4 = data.get("B4");

            pan = data.get("B2");
        }
        
        public int getQueryLimits(){
            return queryLimits;
        }

        public void setQueryLimits(int i){
            queryLimits = i;
        }

        public String getAmount(){
            return B4;
        }

        public String getBarcode(){
            return pan;
        }
    }
}

