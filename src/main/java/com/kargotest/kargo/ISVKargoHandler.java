package com.kargotest.kargo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kargotest.miya.MiYa;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ISVKargoHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(MiYa.class);
    private Map<String, String> errorCodes = new HashMap<String, String>() {{
        put("0000","Txn completed successfully");
        put("1002","Transaction Approved");
        put("1003","Transaction Approved");
        put("1004","Transaction Approved");
        put("7001","Pick Up Card");
        put("7002","Pick Up");
        put("7003","Pick Up");
        put("7004","Pick Up");
        put("7005","Pick Up");
        put("7006","Pick Up");
        put("8001","System Error");
        put("8002","Txn not supported at Termnl");
        put("8003","Txn not supported at Mrchnt");
        put("8004","Invalid Transaction Amount");
        put("8005","Invalid Pan Number");
        put("8007","Bad data in request");
        put("8008","Invalid Merchant");
        put("8010","Bad data in request");
        put("8011","Timeout");
        put("8013","Velocity check failed");
        put("8014","Invalid Transaction");
        put("8015","Data Error");
        put("8016","Unsupported Mrchnt currency");
        put("8017","No Termnl Mrchnt assocation");
        put("8018","Terminal not logged on");
        put("8021","Issuer Not Available");
        put("8023","Invalid Authentication");
        put("8024","Terminal Config Not Exist");
        put("8025","Invalid Config Download Req");
        put("8027","Invalid Provisioning Req");
        put("8028","Card not suported at Issuer");
        put("8029","PAN or TrackData Required");
        put("8030","Invalid merchantTransactionID");
        put("8031","Txn not supported at Store");
        put("8032","Invalid value for extendExpiryMonthBy");
        put("8033","No Router ID Found");
        put("8034","Server Busy");
        put("8040","Original Transaction Not Found");
        put("8041","Void Time Limit Exceeds");
        put("8042","Failed Original Transaction");
        put("8043","Amount mismatch for transaction");
        put("8044","Bad data in request");
        put("8045","Invalid UPC");
        put("8046","Refund Time Limit Exceeds");
        put("8047","Refund not supported for original txn");
        put("8048","Original txn used different card");
        put("8049","New card number is required");
        put("8050","CardHolder Profile Data missing");
        put("8051","Transasation only supported by dynamic code");
        put("9001","Refer to Card Issuer");
        put("9002","Invalid Merchant");
        put("9003","Do not Honor");
        put("9004","Transaction Declined");
        put("9005","Request in Process");
        put("9006","Transaction Declined");
        put("9007","Transaction Declined");
        put("9008","Invalid Card Number");
        put("9009","No Such Issuer");
        put("9010","Reenter Transaction");
        put("9011","Invalid Response");
        put("9012","Transaction Declined");
        put("9013","Suspected Malfunction");
        put("9014","Unacceptable Transaction Fee");
        put("9015","Unable to Locate Original Transaction");
        put("9016","Transaction Declined");
        put("9017","Bank Not Supported");
        put("9018","Expired Card");
        put("9019","Transaction Declined");
        put("9020","Transaction Declined");
        put("9021","Insufficient Funds");
        put("9022","Transaction Declined");
        put("9023","Transaction Not Permitted");
        put("9024","Suspected Fraud");
        put("9025","Transaction Declined");
        put("9026","Restricted Card");
        put("9027","Transaction Declined");
        put("9028","Transaction Declined");
        put("9029","Transaction Declined");
        put("9030","Cutoff is in Progress");
        put("9031","Transaction Declined");
        put("9032","Transaction Declined");
        put("9033","Transaction Declined");
        put("9035","Transaction Declined");
        put("9036","Transaction Declined");
        put("9037","Transaction Declined");
        put("9038","Transaction Declined");
        put("9039","Transaction Declined");
        put("9040","Transaction Declined");
        put("9051","Card Not Active");
        put("9052","Already Activated Card");
        put("9053","Activation in process");
        put("9054","This card cannot be activated. Please destroy the card and use a new one.");
        put("9055","Invalid currency");
        put("9056","Card can be used after 30 minutes");
        put("9057","Invalid Request");
        put("9058","Card Reissued. please activate new card");
        put("9059","Card Reissued. Please contact clerk for technical support");
        put("9060","Reversal is not allowed for already used card");
        put("9061","Card is already registered");
        put("9062","Activation Reversal Time Limit Exceeds");
        put("9063","Card is already reissued");
        put("9064","Card was closed");
        put("9065","Insufficient account funds");
    }};
    private ObjectMapper mapper = new ObjectMapper();
    private String result = "";
    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String reqBodyStr = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        logger.info(reqBodyStr);
        ObjectMapper mapper = new ObjectMapper();
        String path = req.getPath().split("/")[7];
        logger.info("PATH:" + path);

        if(path.equals("redeemprepaid")){
            JsonNode redeemprepaidReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = redeemprepaid(redeemprepaidReqObj);
            result = jsonNode.toString();
        }

        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);

        return 0;
    }

    public JsonNode redeemprepaid(JsonNode jsonNode){
        JsonNode root = mapper.createObjectNode();
        JsonNode resp = null;
        try{
            Thread.sleep(9000);
            resp = mapper.readTree("{\"merchantTransactionID\":\"208888241104539802\",\"kargoCardTransactionID\":\"001033916212\",\"hostTransactionID\":\"001033916212\",\"responseCode\":\"9000\",\"reasonCode\":\"9065\",\"responseMessage\":\"Insufficient account funds\",\"prepaidBalance\":0.0,\"accountStatus\":\"WALLET:WCTPAY\",\"amountRequested\":0.01,\"amountProcessed\":0.01,\"primaryAccountNumber\":\"134280065829410384\"}");
        }
        catch (JsonProcessingException jException){
            throw new RuntimeException(jException);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Double errorCode = jsonNode.get("transactionAmount").doubleValue();
        resp = convertErrorCode(errorCode, resp);
        return resp;
    }

    private JsonNode convertErrorCode(Double d, JsonNode resp){
        int amount = d.intValue();
        String key = "0000";
        if(amount > 1001)
            key = String.valueOf(amount);

        ((ObjectNode) resp).put("reasonCode", key);
        ((ObjectNode) resp).put("responseCode", key);

        ((ObjectNode) resp).put("responseMessage", errorCodes.get(key));

        return resp;
    }
}
