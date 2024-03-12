package com.kargotest.fft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kargotest.ebuy.ISVeBuyHandler;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ISVFFTHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(ISVFFTHandler.class);
    private String result = "";
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd");

    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        //HashMap<String, String> fftBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        Map<String, String> body = req.getParams();
        String service = body.get("service");
        if(service.equals("synchronize.writeOff.service")){
            result = "{\"code\":\"1000\",\"message\":\"SUCCESS\",\"transDate\":" + timeFormat.format(new Date()) + ",\"transTxnId\":\"102438319671\"}";
        } else if (service.equals("bill.conf.get.service")) {
            result = "{\"code\":\"1000\",\"message\":null,\"billConfList\":[{\"searchType\":\"0\",\"searchTypeName\":\"用户代码\",\"validationExp\":\"^\\\\d{10}$\",\"remark\":null,\"needPwd\":\"N\"}]}";
        } else if (service.equals("query.by.billno.service")) {
            result = "{\"code\":\"1000\",\"message\":\"交易成功\",\"bills\":[{\"billId\":\"202403060377996995\",\"billStatus\":\"04\",\"billOrgId\":\"888880000502900\",\"billOrgName\":\"上海市电力公司\",\"billNo\":\"1264255625\",\"billMonth\":\"202402\",\"billAmt\":55.8,\"billRecordTimes\":\"89\",\"barcode\":\"544021264255625000055803\",\"billAddr\":\"上海市闸北区沪太路132****号701室\",\"billOwner\":\"**良\",\"overdueFee\":0.0,\"isInsurance\":\"N\"}]}";
        }


        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);
        return 0;
    }
}