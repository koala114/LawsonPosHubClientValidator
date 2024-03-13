package com.kargotest.zhongbai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kargotest.ebuy.ISVeBuyHandler;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeMap;

public class ZhongBaiISVHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(com.kargotest.alading.ISVALaDingHandler.class);
    private ObjectMapper mapper = JsonMapper.builder().nodeFactory(new SortingNodeFactory()).build();


    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String result = "";
        String zhongBaiBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        logger.info(zhongBaiBody);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode reqObj = mapper.readTree(zhongBaiBody);
        String service = reqObj.get("service").asText();
        if(service.equals("101")){
            // {"data":{"bill":{"sgs":[{"barcode":"6943870510689","catid":"05","code":"6943870510689","hjje":1000,"hjzk":0,"jg":500,"name":"地道肠黑椒味","ppid":"","rowno":1,"sj":500,"sl":2000,"xxtax":0}],"sysy":0,"tot":1,"type":"1","xsje":1000,"ysje":1000,"zsl":2000,"zzke":0},"cashier":"35008801","listNo":"3500880133590901","orderNo":"06350088012023120614454081","payCode":"666613297933851","posId":"01","storeCode":"350088","transAmt":1000,"transDate":"20231206144622","useFlag":1},"mcId":"06","service":"101","traceId":"56adace447e540a38bd02db2eb5de154","version":"1.0"}
            // {"traceId":"56adace447e540a38bd02db2eb5de154","mcId":"06","retCode":"00","resultCode":"SUCCESS","payMsg":"中百券06350088012023120614454081","data":{"gmt":"20231206144623","orderNo":"06350088012023120614454081","transAmt":1000,"useFlag":1,"factAmt":0,"nti":0,"funds":[{"rowno":1,"code":"3304","name":"中百券","je":0,"ye":0,"batch":"06350088012023120614454081"}]}}
            result = "{\"traceId\":\"0649aab8b81e48cba035451117277b52\",\"mcId\":\"06\",\"retCode\":\"00\",\"resultCode\":\"SUCCESS\",\"payMsg\":\"中百钱包310100003103076余额81.60\",\"data\":{\"gmt\":\"20240307154236\",\"orderNo\":\"06350088022024030715423887\",\"orderId\":\"154236\",\"transAmt\":1840,\"useFlag\":1,\"factAmt\":1840,\"nti\":0,\"funds\":[{\"rowno\":1,\"code\":\"014\",\"name\":\"中百钱包\",\"je\":1840,\"payno\":\"310100003103076\",\"ye\":8160,\"batch\":\"06350088022024030715423887\"}]}}";
        }
        else if(service.equals("401")) {
            result = "{\"traceId\":\"4e478230-819e-401a-bf31-3b2dada02043\",\"mcId\":\"01\",\"retCode\":\"00\",\"resultCode\":\"SUCCESS\",\"payMsg\":\"中百优惠券 01100101012018031016195840\",\"data\":{\"gmt\":\"20180310162946\",\"orderNo\":\"01100101012018031016195840\",\"transAmt\":340,\"factAmt\":200,\"funds\":[{\"rowno\":1,\"code\":\"9010\",\"name\":\"现金券\",\"je\":100,\"payno\":\"5101-1204-3257-2401\",\"ye\":0,\"batch\":\"V79310313249877131\"},{\"rowno\":2,\"code\":\"9010\",\"name\":\"折扣券\",\"je\":100,\"payno\":\"6101-5431-1246-5689\",\"ye\":0,\"batch\":\"V79310313249877132\"}]}}";
        }
        else if(service.equals("102")) {
            result = "{\"traceId\":\"c345d460-e26d-443c-9f53-a4d8c9d1575d\",\"mcId\":\"01\",\"retCode\":\"00\",\"resultCode\":\"SUCCUESS\",\"payMsg\":\"中百优惠券 01100101012018031016195840\",\"data\":{\"gmt\":\"20180310162220\",\"orderNo\":\"01100101012018031016195840\",\"orderId\":\"95333011812374333108866\",\"transAmt\":340,\"factAmt\":200,\"funds\":[{\"rowno\":1,\"code\":\"9010\",\"name\":\"现金券\",\"je\":100,\"payno\":\"5101-1204-3257-2401\",\"ye\":0,\"batch\":\"V79310313249877131\"},{\"rowno\":2,\"code\":\"9010\",\"name\":\"折扣券\",\"je\":100,\"payno\":\"6101-5431-1246-5689\",\"ye\":0,\"batch\":\"V79310313249877132\"}]}}";
        }
        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);
        return 0;
    }

    static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<String, JsonNode>());
        }
    }
}



