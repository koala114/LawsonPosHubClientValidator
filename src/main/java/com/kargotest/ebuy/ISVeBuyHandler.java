package com.kargotest.ebuy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

public class ISVeBuyHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(ISVeBuyHandler.class);
    private static HashMap<String, String> transMap = new HashMap<>();
    ArrayList<String> eCouponsUsed = new ArrayList<>();
    private ObjectMapper mapper = JsonMapper.builder().nodeFactory(new SortingNodeFactory()).build();
    static SecureRandom rnd = new SecureRandom();

    private String result = "";
    String patternDate = "yyyyMMdd";
    String patternTime = "HHmmss";
    String date;
    String time;

    SimpleDateFormat DateFormat = new SimpleDateFormat(patternDate);
    SimpleDateFormat timeFormat = new SimpleDateFormat(patternTime);
    private static final String SESSIONKEY = "12BA02A11736A03D7580BB3B535A9A61"; //kargo:hub:cache:lawsonKCManagerData:KcIsvSessionkeyConfig:Map yibai-Lawson:yibai

    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String reqBodyStr = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        logger.info(reqBodyStr);
        ObjectMapper mapper = new ObjectMapper();
        String path = req.getPath().split("/")[3];
        logger.info("PATH:" + path);

        JsonNode reqObj = mapper.readTree(reqBodyStr);
        String body = reqObj.get("body").asText();

        if(!requiredParametersVerification(reqObj)){
            result = "Missing required parameters";
        }

        if(path.equals("ecouponsVerify")){
            JsonNode jsonNode = ecouponsVerify(body);
            result = jsonNode.toString();
        }
        else if(path.equals("batchCodeVerifyQuery")){
            JsonNode jsonNode = batchCodeVerifyQuery(body);
            result = jsonNode.toString();
        }

        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);

        return 0;
    }

    public JsonNode ecouponsVerify(String body){
        JsonNode root = mapper.createObjectNode();
        JsonNode eCouponsNode = null;
        String eCouponCode = "";
        String traceNo = "";
        JsonNode resp = null;

        String bodyStr = new String(Base64.getDecoder().decode(body));
        logger.info("ecouponsVerify BODY Base64 decode: " + bodyStr);

        try{
            JsonNode bodyObj = mapper.readTree(bodyStr);
            eCouponsNode = bodyObj.get("ecoupons"); // 获取券号
            eCouponCode = eCouponsNode.get(0).get("code").textValue();
            traceNo = bodyObj.get("traceNo").textValue();
            transMap.put(traceNo, eCouponCode);

            String timeout = eCouponCode.substring(eCouponCode.length() - 4);
            // 1000 -> 9000 结尾的券号等待时间
            if(timeout.substring(1).equals("000")&&!eCouponsUsed.contains(traceNo)){
                if(timeout.equals("9000")){
                    transMap.remove(traceNo);
                    eCouponsUsed.add(traceNo);
                    logger.info(traceNo + " add to eCouponsUsed list!");
                }
                Thread.sleep(Integer.parseInt(timeout) + 5000);
            }

            resp = mapper.readTree("{\"ptlVersion\":\"20170214\",\"mwVersion\":\"20170214\",\"posVersion\":\"20181028\",\"sign\":\"E888F0C9C9A2E21E1653C287073B836B\",\"action\":\"ecouponsVerify\",\"deviceNo\":\"20888801\",\"body\":\"eyJlY291cG9uc0RhdGEiOlt7ImFjdGl2aXR5UHJvZHVjdE5hbWUiOiLnvZfmo6425YWD5Luj6YeR5Yi4IiwiYWN0aXZpdHlQcm9kdWN0Tm8iOiIwMDAwMDAwMDYwMjg3MCIsImNvZGUiOiJMUzIxMDAzMjg0MDU2MDE5NTk5OTI5MSIsImRlc2MiOiLmk43kvZzmiJDlip8iLCJmdW5kQ2hhbm5lbCI6W3siY2hhbm5lbEFtb3VudCI6MC4wMCwiY2hhbm5lbE5hbWUiOiLnlKjmiLflrp7pmYXmlK/ku5giLCJjaGFubmVsTm8iOiJ1c2VyX3JlYWxfbW9uZXkifV0sImludm9pY2VBbW91bnQiOjYuMDAsInBhaWRBbW91bnQiOjYuMDAsInJlY2VpcHRBbW91bnQiOjYuMDAsInJldHVybkNvZGUiOiIwMCIsInN0YXR1cyI6IjAwIiwidG90YWxBbW91bnQiOjh9XSwiZXh0ZW5kc0RhdGEiOnt9LCJpbnZvaWNlQW1vdW50IjowLCJvcmRlck5vIjoiMTk3NDAwMzQ4MDkyIiwicGFpZEFtb3VudCI6MCwicmVjZWlwdEFtb3VudCI6MCwicmV0dXJuQ29kZSI6IjAwIiwicmV0dXJuRGVzYyI6Iuivt+axguaIkOWKnyIsInN0ZXAiOiIwMSIsInRyYWNlTm8iOiJMUzAxMTgxMDIwMTUwMDUxMDEiLCJ0cmFuc0RhdGUiOiIyMDI0MDExODEwMjAxNyIsInV1aWQiOiJmYzBmNmJmNjJkZjY0MGI4YmE5ZDZiMjQyZmRiZTUxMyJ9\",\"brand\":\"6343\",\"shopNo\":\"208888\",\"timestamp\":\"1705544415000\"}");
        } catch (JsonProcessingException jException){
            throw new RuntimeException(jException);
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
        }
        logger.info("ecouponsVerify RESP : " + resp.toString());
        return resp;
    }
    // 批量核销结果查询
    public JsonNode batchCodeVerifyQuery(String body){
        int randomInt = rnd.nextInt(1000000);
        date = DateFormat.format(new Date());
        time = timeFormat.format(new Date());

        ObjectNode root = mapper.createObjectNode();
        JsonNode bodyObj = mapper.createObjectNode();
        root.put("invoiceAmount", 0.00);
        root.put("orderNo", "197410" + time);
        root.put("receiptAmount", 0);
        root.put("paidAmount", 0);
        root.put("returnCode", "00");
        root.put("returnDesc", "请求成功");
        root.put("rpcContext", "10.10.54.31:43053-->10.10.54.32:30032");
        root.put("step", "01");
        root.put("traceNo", date + time + String.valueOf(randomInt));
        root.put("transDate", date + time);
        root.put("uuid", "c6f" + date + "d40ceacea1f" + time + "bf77");

        String bodyStr = new String(Base64.getDecoder().decode(body));
        logger.info("batchCodeVerifyQuery BODY Base64 decode: " + bodyStr);

        String originalTraceNo = "";
        JsonNode resp = null;

        try{
            bodyObj = mapper.readTree(bodyStr);
            originalTraceNo = bodyObj.get("originalTraceNo").textValue();
            if(!transMap.containsKey(originalTraceNo)){
                // 没有查询到原交易
                resp = mapper.readTree("{\"ptlVersion\":\"20170214\",\"mwVersion\":\"20170214\",\"posVersion\":\"20181028\",\"sign\":\"3FA849790B1D95CF8939A250891D6ECA\",\"action\":\"batchCodeVerifyQuery\",\"deviceNo\":\"20888801\",\"body\":\"eyJleHRlbmRzRGF0YSI6e30sImludm9pY2VBbW91bnQiOjAsInBhaWRBbW91bnQiOjAsInJlY2VpcHRBbW91bnQiOjAsInJldHVybkNvZGUiOiJFQl9PUkdJTkFMVFJBTlNfTk9UX0VYSVNUIiwicmV0dXJuRGVzYyI6IuaJvuS4jeWIsOWOn+Wni+S6pOaYkyIsInRyYWNlTm8iOiJMUzE3MDU1NTAwMTkzMDc3MyIsInRyYW5zRGF0ZSI6IjIwMjQwMTE4MTE1MzM5In0=\",\"brand\":\"6343\",\"shopNo\":\"208888\",\"timestamp\":\"1705550018000\"}");
                return resp;
            }
            JsonNode ecouponsData =  (ArrayNode)mapper.readTree("{\"ecouponsData\":[{\"activityProductName\":\"罗森6元代金券\",\"activityProductNo\":\"00000000602870\",\"code\":\"LS210032842595798528794\",\"desc\":\"操作成功\",\"fundChannel\":[{\"channelAmount\":0.00,\"channelName\":\"用户实际支付\",\"channelNo\":\"user_real_money\"}],\"invoiceAmount\":6.00,\"paidAmount\":6.00,\"receiptAmount\":6.00,\"returnCode\":\"00\",\"status\":\"00\",\"totalAmount\":10.00}]}").get("ecouponsData");
            JsonNode objNode = ecouponsData.get(0);
            ((ObjectNode) objNode).put("code", transMap.get(originalTraceNo));
            root.put("ecouponsData", ecouponsData);

            JsonNode extendsData = mapper.readTree("{\"allSuccess\":true}");
            root.put("extendsData", extendsData);

            JsonNode printDetail = mapper.readTree("{\"content\":\"00001F0E652D42757920B5E7D7D3C6BED6A42F11617574682D6261746368696E7175697279\"}");
            root.put("printDetail", printDetail);

            logger.info("batchCodeVerifyQuery RESP BODY: " + root.toString());
            String bodyBase64 = new String(Base64.getEncoder().encode(root.toString().getBytes(StandardCharsets.UTF_8)));

            resp = mapper.readTree("{\"ptlVersion\":\"20170214\",\"mwVersion\":\"20170214\",\"posVersion\":\"20181028\",\"sign\":\"67728B14E051B2B9271653566C0A76C7\",\"action\":\"batchCodeVerifyQuery\",\"deviceNo\":\"20888601\",\"body\":\"\",\"brand\":\"6343\",\"shopNo\":\"208886\",\"timestamp\":\"1703655834\"}\n");
            ((ObjectNode) resp).put("body", bodyBase64);
        } catch (JsonProcessingException jException){
            throw new RuntimeException(jException);
        }
        return resp;
    }

    public boolean requiredParametersVerification(JsonNode jsonNode){
        return true;
    }

    private String makeSign(JsonNode jsonNode){
        String checksum = "";
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            buffer.append(field.getKey()).append("=").append(field.getValue()).append("&");
        }
        buffer.append("KEY=").append(SESSIONKEY);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer.toString().getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            checksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return checksum;
    }

    static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<String, JsonNode>());
        }
    }
}
