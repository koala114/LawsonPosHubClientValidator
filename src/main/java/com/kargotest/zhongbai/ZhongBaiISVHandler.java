package com.kargotest.zhongbai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class ZhongBaiISVHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(com.kargotest.alading.ISVALaDingHandler.class);
    private ObjectMapper mapper = JsonMapper.builder().nodeFactory(new SortingNodeFactory()).build();
    private static List<String> codesOffset = new ArrayList<>();

    String patternDate = "yyyyMMdd";
    String patternTime = "HHmmss";
    String date;
    String time;

    SimpleDateFormat DateFormat = new SimpleDateFormat(patternDate);
    SimpleDateFormat timeFormat = new SimpleDateFormat(patternTime);


    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String result = "";
        String zhongBaiBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        logger.info(zhongBaiBody);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode reqObj = mapper.readTree(zhongBaiBody);
        String service = reqObj.get("service").asText();

        if(service.equals("101")){
            result = One0One(reqObj);
        }
        else if(service.equals("401")) {
            result = "{\"traceId\":\"4e478230-819e-401a-bf31-3b2dada02043\",\"mcId\":\"01\",\"retCode\":\"00\",\"resultCode\":\"SUCCESS\",\"payMsg\":\"中百优惠券 01100101012018031016195840\",\"data\":{\"gmt\":\"20180310162946\",\"orderNo\":\"01100101012018031016195840\",\"transAmt\":340,\"factAmt\":200,\"funds\":[{\"rowno\":1,\"code\":\"9010\",\"name\":\"现金券\",\"je\":100,\"payno\":\"5101-1204-3257-2401\",\"ye\":0,\"batch\":\"V79310313249877131\"},{\"rowno\":2,\"code\":\"9010\",\"name\":\"折扣券\",\"je\":100,\"payno\":\"6101-5431-1246-5689\",\"ye\":0,\"batch\":\"V79310313249877132\"}]}}";
        }
        else if(service.equals("102")) {
            result = "{\"traceId\":\"c345d460-e26d-443c-9f53-a4d8c9d1575d\",\"mcId\":\"01\",\"retCode\":\"00\",\"resultCode\":\"SUCCUESS\",\"payMsg\":\"中百优惠券 01100101012018031016195840\",\"data\":{\"gmt\":\"20180310162220\",\"orderNo\":\"01100101012018031016195840\",\"orderId\":\"95333011812374333108866\",\"transAmt\":340,\"factAmt\":200,\"funds\":[{\"rowno\":1,\"code\":\"9010\",\"name\":\"现金券\",\"je\":100,\"payno\":\"5101-1204-3257-2401\",\"ye\":0,\"batch\":\"V79310313249877131\"},{\"rowno\":2,\"code\":\"9010\",\"name\":\"折扣券\",\"je\":100,\"payno\":\"6101-5431-1246-5689\",\"ye\":0,\"batch\":\"V79310313249877132\"}]}}";
        }

        if(reqObj.get("data").get("bill").get("sgs").size() <= 0){
            result = "{\"traceId\":\"b50b4f2ac6c84845a30bf7f8115b4579\",\"mcId\":\"06\",\"retCode\":\"99\",\"resultCode\":\"FAIL\",\"errCode\":\"5420\",\"errMsg\":\"参数错误缺失GoodsDetail\",\"data\":{\"gmt\":\"20240313140922\",\"orderNo\":\"06350088012024031314092267\",\"transAmt\":1,\"useFlag\":1,\"nti\":0}}";
        }
        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);
        return 0;
    }

    public String One0One(JsonNode reqObj) {
        String payCode = reqObj.get("data").get("payCode").textValue();
        String orderNo = reqObj.get("data").get("orderNo").textValue();
        Integer transAmt = reqObj.get("data").get("transAmt").intValue();
        Integer useFlag = reqObj.get("data").get("useFlag").intValue();
        String payType = "";
        logger.info("---- 中百券先试算0，再核销1 ---- useFlag: " + useFlag);
        date = DateFormat.format(new Date());
        time = timeFormat.format(new Date());

        ObjectNode root = mapper.createObjectNode();
        root.put("traceId", reqObj.get("traceId").textValue());
        root.put("mcId", reqObj.get("mcId").textValue());
        root.put("retCode", "00");
        root.put("resultCode", "SUCCESS");
        ObjectNode data = mapper.createObjectNode();
        data.put("gmt", date + time);
        data.put("orderNo", orderNo);
        data.put("transAmt", transAmt);
        data.put("useFlag", useFlag);
        data.put("factAmt", 0);
        data.put("nti", 0);

        ArrayNode funds = mapper.createArrayNode();
        ObjectNode fund = mapper.createObjectNode();
        fund.put("rowno", 1);
        fund.put("je", 0);
        fund.put("ye", 0);
        fund.put("batch", orderNo);

        if(payCode.startsWith("6666") || payCode.startsWith("8")){
            // 如果是中百券需要先Barcode(useFlag=0)，再TradeConfirm(useFlag=1)
            payType = "中百券";
            root.put("payMsg", payType + orderNo);
            fund.put("name", payType);
            fund.put("code", "3304");
            if(useFlag == 0){
                if(!codesOffset.contains(orderNo))
                    codesOffset.add(orderNo);
                else
                    return "{\"traceId\":\"e9dd0070d1fd442d8d158be76bcc5a9e\",\"mcId\":\"06\",\"retCode\":\"99\",\"resultCode\":\"FAIL\",\"errCode\":\"5420\",\"errMsg\":\"PAY-5420:useFlag参数错误\",\"data\":{\"gmt\":\"20240313150533\",\"orderNo\":\"06350088022024031315053222\",\"transAmt\":1180,\"useFlag\":1,\"nti\":0}}";
            }
            else if(!codesOffset.contains(orderNo)){
                return "{\"traceId\":\"e9dd0070d1fd442d8d158be76bcc5a9e\",\"mcId\":\"06\",\"retCode\":\"99\",\"resultCode\":\"FAIL\",\"errCode\":\"5420\",\"errMsg\":\"PAY-5420:useFlag参数错误\",\"data\":{\"gmt\":\"20240313150533\",\"orderNo\":\"06350088022024031315053222\",\"transAmt\":1180,\"useFlag\":1,\"nti\":0}}";
            }
        }
        else if(payCode.startsWith("http")){
            payType = "中百抖音";
        }
        else {
            String payNo =  payCode.substring(3,16);
            payType = "中百钱包";
            if(useFlag != 1)
                return "{\"traceId\":\"e9dd0070d1fd442d8d158be76bcc5a9e\",\"mcId\":\"06\",\"retCode\":\"99\",\"resultCode\":\"FAIL\",\"errCode\":\"5420\",\"errMsg\":\"PAY-5420:useFlag参数错误\",\"data\":{\"gmt\":\"20240313150533\",\"orderNo\":\"06350088022024031315053222\",\"transAmt\":1180,\"useFlag\":1,\"nti\":0}}";

            root.put("payMsg", payType + payNo + "余额396.65");
            data.put("factAmt", transAmt);
            fund.put("code", "014");
            fund.put("je", transAmt);
            fund.put("ye", 39665);
            fund.put("payno", payNo);
        }

        funds.add(fund);
        data.put("funds", funds);
        root.put("data", data);

        return root.toString();
    }

    static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<String, JsonNode>());
        }
    }
}



