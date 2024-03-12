package com.kargotest.YoRen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.freeutils.httpserver.HTTPServer;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class YoRen implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(YoRen.class);

    static SecureRandom rnd = new SecureRandom();
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> couponLocks = new HashMap<>();
    private String result = "";
    String patternDate = "yyyyMMdd";
    String patternTime = "HHmmss";

    SimpleDateFormat DateFormat = new SimpleDateFormat(patternDate);
    SimpleDateFormat timeFormat = new SimpleDateFormat(patternTime);
    String date;
    String time;


    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        logger.info("YoRen Service begin......");
        Iterator<HTTPServer.Header> headers = req.getHeaders().iterator();
        while(headers.hasNext()){
            HTTPServer.Header header = headers.next();
            logger.info("Request Headers: {} -> {}", header.getName(), header.getValue());
        }
        Map<String, String> params = new HashMap<>();
        List<String[]> paramsList = req.getParamsList();
        for (String[] s: paramsList
             ) {
            logger.info(s[0] + " --> " +s[1] );
            params.put(s[0],s[1] );
        }

        ObjectMapper mapper = new ObjectMapper();
        String path = req.getPath().split("/")[4];
        if (path.equals("redeemPrepaid")) {
            JsonNode jsonNode = YoRenRedeemPrepaid(params);
            result = jsonNode.toString();
        } else if (path.equals("getMemberDiscount")) {
            JsonNode jsonNode = YoRenGetMemberDiscount(params);
            result = jsonNode.toString();
        } else if (path.equals("settlementTransactions")) {
            JsonNode jsonNode = YoRenSettlementTransactions(params);
            result = jsonNode.toString();
        } else if (path.equals("returnInformation")) {
            JsonNode jsonNode = YoRenReturnInformation();
            result = jsonNode.toString();
        }
        else if (path.equals("dealDoneNotice")) {
            logger.info(params);
            // YoRen没有登录的情况下会调用dealDoneNotice，需要检查paymentInfoList里面的paymentMerchantID和paymentUserID 不能为空
            JsonNode jsonNode = YoRenDealDoneNotice(params);
            result = jsonNode.toString();
        }
        else if (path.equals("couponCorrection")) {
            result = "{\"resultCode\":400}";
        }

        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);

        return 0;
    }

    public JsonNode YoRenRedeemPrepaid(Map<String, String> params) throws JsonProcessingException {
        ObjectNode root = null;
        if (params.get("cardPaidCode").equals("999999999999999999")){
            JsonNode failed =  mapper.readTree("{\"message\":\"预付费卡支付码验证无效\",\"resultCode\":102}");
            root = (ObjectNode)failed;
            return root;
        }

        try{
            root = mapper.createObjectNode();
            JsonNode transactionInfo =  mapper.readTree("{\"responseCode\":\"0000\",\"merchantTransactionID\":\"2028420132400302\",\"kargoCardTransactionID\":\"019036901011\",\"hostTransactionID\":\"23042017155124\",\"responseMessage\":\"Txn completed successfully\",\"reasonCode\":\"0000\",\"prepaidBalance\":98.0,\"amountRequested\":2.0,\"amountProcessed\":2.0,\"primaryAccountNumber\":\"6018411010019124\"}");
            ((ObjectNode) transactionInfo).put("merchantTransactionID", params.get("merchantTransactionID"));
            ((ObjectNode) transactionInfo).put("kargoCardTransactionID", "01011" + params.get("merchantTransactionID").substring(7));
            ((ObjectNode) transactionInfo).put("hostTransactionID", "71551" + params.get("merchantTransactionID").substring(7));
            root.set("transactionInfo", transactionInfo);
            root.put("resultCode", 100);
            root.put("message", "成功");
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }

        return root;
    }

    public JsonNode YoRenGetMemberDiscount(Map<String, String> params) throws JsonProcessingException {
        ObjectNode root = mapper.createObjectNode();
        logger.info(params.values());
        String commodityStr = params.get("commodityList");
        String dynamicId = params.get("userCode");
        ArrayNode commodityList =  (ArrayNode)mapper.readTree(commodityStr);
        if(commodityList.size() == 0){
            try{
                root = (ObjectNode) mapper.readTree("{\"couponInfoList\":[],\"discountCommodityList\":[],\"extraInfo\":{\"memberAmount\":0,\"memberAmountFixed\":0,\"memberAmountFlg\":0,\"memberAmountFree\":0,\"memberAmountLimited\":0,\"memberAmountLocked\":0,\"memberPromotion\":\"0\",\"memberPromotionRec\":\"\",\"memberAmountMsg\": { \"msgForPOS\": \"今日可购买会员促销商品总额度1000元，已使用998元，已被限额~\", \"msgForUser\": \"今日可购买会员促销商品总额度1000元，已使用998元，已被限额~(额度计算以零售价为准)\" }},\"paymentAmount\":\"0.00\",\"resultCode\":100,\"userInfo\":[{\"totalPoint\":347858,\"totalPrePaidAmount\":0,\"totalPointAmount\":347.8,\"userStatus\":0,\"codeType\":1,\"level\":\"01\",\"mobile\":\"18616623768\",\"codeKbn\":1,\"userName\":\"测试\",\"userCode\":\"1900213189174\"}]}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
        }
        else {
            List<JsonNode> commodityBarcodeListNode = (List<JsonNode>) commodityList.findValues("commodityBarcode");
            logger.info("检查commodityList" + commodityBarcodeListNode.toString());
            for (JsonNode j:commodityBarcodeListNode) {
                if(j.textValue().equals("050733")){
                    try{
                        root = (ObjectNode) mapper.readTree("{\"couponInfoList\":[{\"commodityInfoList\":[{\"commodityBarcode\":\"050733\",\"commodityQuantity\":1,\"sharedDiscount\":0}],\"couponCode\":\"368643351#48822#0#0#1.75#0#1.75\",\"couponId\":48822,\"couponName\":\"罗森测试卖变5折券\",\"couponType\":\"10\",\"paidType\":\"0\",\"savePayment\":1.75,\"sellType\":\"0\"}],\"discountCommodityList\":[{\"afterDiscountPrice\":1.75,\"beforeDiscountPrice\":3.5,\"commodityBarcode\":\"050733\",\"discountPayment\":1.75,\"discountQuantity\":1}],\"extraInfo\":{\"memberAmount\":0.00,\"memberAmountFixed\":0.00,\"memberAmountFlg\":0,\"memberAmountFree\":200.00,\"memberAmountLimited\":200,\"memberAmountLocked\":0.00,\"memberPromotion\":\"0\",\"memberPromotionRec\":\"\"},\"paymentAmount\":\"3.50\",\"resultCode\":100,\"userInfo\":[{\"totalPoint\":347858,\"totalPrePaidAmount\":0,\"totalPointAmount\":347.8,\"userStatus\":0,\"codeType\":1,\"level\":\"01\",\"mobile\":\"18616623768\",\"codeKbn\":1,\"userName\":\"酒尘\",\"userCode\":\"1900213189174\"}]}");
                    }catch (Exception e){
                        System.out.println(e.getStackTrace());
                    }
                    break;
                }
                else
                    root = (ObjectNode) mapper.readTree("{\"couponInfoList\":[],\"discountCommodityList\":[],\"extraInfo\":{\"memberAmount\":0,\"memberAmountFixed\":0,\"memberAmountFlg\":0,\"memberAmountFree\":0,\"memberAmountLimited\":0,\"memberAmountLocked\":0,\"memberPromotion\":\"0\",\"memberPromotionRec\":\"\"},\"paymentAmount\":\"0.00\",\"resultCode\":100,\"userInfo\":[{\"totalPoint\":347858,\"totalPrePaidAmount\":0,\"totalPointAmount\":347.8,\"userStatus\":0,\"codeType\":1,\"level\":\"01\",\"mobile\":\"18616623768\",\"codeKbn\":1,\"userName\":\"测试\",\"userCode\":\"1900213189174\"}]}");
            }
        }

        if(dynamicId.equals("391615349392262981"))
            root = (ObjectNode) mapper.readTree("{\"couponInfoList\":[{\"couponCode\":\"386921013#51657#0#0#1.00#0#1.00\",\"couponName\":\"【统括代金测试】满2减1\",\"couponType\":\"20\",\"savePayment\":1.0,\"sellType\":1}],\"discountCommodityList\":[],\"extraInfo\":{\"memberAmount\":0,\"memberAmountFixed\":0,\"memberAmountFlg\":0,\"memberAmountFree\":0,\"memberAmountLimited\":0,\"memberAmountLocked\":0,\"memberPromotion\":\"0\",\"memberPromotionRec\":\"\"},\"paymentAmount\":\"3.00\",\"resultCode\":100,\"userInfo\":[{\"totalPoint\":347858,\"totalPrePaidAmount\":0,\"totalPointAmount\":347.8,\"userStatus\":0,\"codeType\":1,\"level\":\"01\",\"mobile\":\"18616623768\",\"codeKbn\":1,\"userName\":\"测试\",\"userCode\":\"1900213189174\"}]}");

        logger.info(root.toPrettyString());
        return root;
    }

    public JsonNode YoRenSettlementTransactions(Map<String, String> params) throws JsonProcessingException {
        ObjectNode root = mapper.createObjectNode();
        logger.info(params.values());
        try{
            if (params.get("userCode").equals("399999999999999999"))
                root = (ObjectNode) mapper.readTree("{\"resultCode\":301}");
            else
                root = (ObjectNode) mapper.readTree("{\"resultCode\":300,\"userInfo\":[{\"usedPoint\":0,\"totalPoint\":347875,\"gainedPoint\":17}],\"campaignInfo\":[{\"campaignId\":\"2873\",\"campaignName\":\"天地劫：幽城再临\",\"stampNum\":\"1\"}]}");
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        logger.info(root.toPrettyString());
        return root;
    }

    public JsonNode YoRenReturnInformation() throws JsonProcessingException{
        ObjectNode root = mapper.createObjectNode();
        try{
            root = (ObjectNode) mapper.readTree("{\"resultCode\":200}");
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        logger.info(root.toPrettyString());
        return root;
    }

    public JsonNode YoRenDealDoneNotice(Map<String, String> params) throws JsonProcessingException{
        ObjectNode root = mapper.createObjectNode();
        try{
            if(checkPaymentInfoList(params.get("paymentInfoList"))){
                root = (ObjectNode) mapper.readTree("{\"resultCode\":300}");
            }
            else
                root = (ObjectNode) mapper.readTree("{\"resultCode\":301}");

        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        logger.info(root.toPrettyString());
        return root;
    }

    private boolean checkPaymentInfoList(String paymentInfoList) {
        boolean result = false;
        ObjectNode root = mapper.createObjectNode();
        try{
            PaymentInfoList[] p = mapper.readValue(paymentInfoList, PaymentInfoList[].class);
            if(p[0].paymentMerchantID.equals("") && p[0].paymentUserID.equals(""))
                result = false;
            else
                result = true;
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }

        return result;

    }
}
