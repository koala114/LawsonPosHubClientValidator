package com.kargotest.miya;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiYa implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(MiYa.class);

    static Map<String, String> tradeNoMap = new HashMap<>();
    static Map<String, Integer> queryTimes = new HashMap<>();

    String patternDate = "yyyyMMdd";
    String patternTime = "HHmmss";

    SimpleDateFormat DateFormat = new SimpleDateFormat(patternDate);
    SimpleDateFormat timeFormat = new SimpleDateFormat(patternTime);
    String date;
    String time;
    private String result = "";

    private ObjectMapper mapper = new ObjectMapper();
    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String reqBodyStr = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));
        logger.info(reqBodyStr);
        ObjectMapper mapper = new ObjectMapper();
        String path = req.getPath().split("/")[4];
        logger.info("PATH:" + path);

        if(path.equals("querycardinfo")){
            JsonNode queryCardInfoReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = querycardinfo4MiYa(queryCardInfoReqObj);
            result = jsonNode.toString();
        } else if (path.equals("tradeconfirm")) {
            JsonNode tradeconfirmReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = tradeconfirm4MiYa(tradeconfirmReqObj);
            result = jsonNode.toString();
        }else if (path.equals("barcode")) {
            JsonNode barCodeReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = barCode4MiYa(barCodeReqObj);
            result = jsonNode.toString();
        }else if (path.equals("uploadgoodsdetail")) {
            JsonNode uploadgoodsdetailReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = uploadgoodsdetailReqObj4MiYa(uploadgoodsdetailReqObj);
            result = jsonNode.toString();
        }else if (path.equals("unfreeze")) {
            JsonNode unfreezeReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = unfreezeReqObj4MiYa(unfreezeReqObj);
            result = jsonNode.toString();
        }else if (path.equals("tradequery")) {
            JsonNode unfreezeReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = tradequeryReqObj4MiYa(unfreezeReqObj);
            result = jsonNode.toString();
        }else if (path.equals("traderefund")) {
            JsonNode unfreezeReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = traderefundReqObj4MiYa(unfreezeReqObj);
            result = jsonNode.toString();
        }else if (path.equals("exchangeconfirm")) {
            JsonNode unfreezeReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = exchangeconfirm4MiYa(unfreezeReqObj);
            result = jsonNode.toString();
        }else if (path.equals("createpayment")) {
            JsonNode createpaymentReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = createpaymentm4MiYa(createpaymentReqObj);
            result = jsonNode.toString();
        }



        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);

        return 0;
    }

    public JsonNode createpaymentm4MiYa(JsonNode jsonNode) {
        JsonNode root = mapper.createObjectNode();
        JsonNode createpaymenmBizObj = null;
        try{
            createpaymenmBizObj =  mapper.readTree("{\"ret_code\":\"00\",\"ret_msg\":\"请求成功[%s]\",\"status\":\"01\",\"sys_trade_no\":\"102315396332\"}");
            ((ObjectNode) createpaymenmBizObj).put("sys_trade_no", DateFormat.format(new Date()) + timeFormat.format(new Date()));
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ((ObjectNode) root).put("biz_content", createpaymenmBizObj);
        ((ObjectNode) root).put("sign", "BEE1D24E54D6D64EE6E7FFE04C5E12AC");
        return root;
    }

    public JsonNode exchangeconfirm4MiYa(JsonNode jsonNode) {
        JsonNode root = mapper.createObjectNode();
        String memberNo = "";
        JsonNode exchangeconfirmBizObj = null;
        if(jsonNode.has("member_no"))
            memberNo = jsonNode.get("member_no").textValue();
        String outTradeNo = jsonNode.get("out_trade_no").textValue();
        try {
                exchangeconfirmBizObj  = mapper.readTree("{\"out_trade_no\":\"20888823164522577\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功\"}");
                ((ObjectNode) exchangeconfirmBizObj).put("out_trade_no",outTradeNo );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ((ObjectNode) root).put("biz_content",exchangeconfirmBizObj );
        ((ObjectNode) root).put("sign", "004D7BFF3A9196248FD188E6942341B9");
        return root;
    }

    public JsonNode querycardinfo4MiYa(JsonNode jsonNode){
        JsonNode root = mapper.createObjectNode();
        ((ObjectNode) root).put("sign", "90BDB49FF42BDD6821BE9578AB6E9DF3");
        JsonNode biz_content = mapper.createObjectNode();
        String cardNo = jsonNode.get("dynamic_id").textValue();
        if (cardNo.startsWith("996016844010007348")) {
            ((ObjectNode) biz_content).put("balance", 68.77);
            ((ObjectNode) biz_content).put("biz_type", "03");
            ((ObjectNode) biz_content).put("out_trade_no", jsonNode.get("out_trade_no").textValue());
            ((ObjectNode) biz_content).put("outid", "001369070018");
            ((ObjectNode) biz_content).put("pay_code", "045");
            ((ObjectNode) biz_content).put("pay_name", "卡购卡");
            ((ObjectNode) biz_content).put("ret_code", "00");
            ((ObjectNode) biz_content).put("ret_msg", "Txn completed successfully");
            ((ObjectNode) biz_content).put("status", "1000");
            ((ObjectNode) biz_content).put("sys_trade_no", DateFormat.format(new Date()) + timeFormat.format(new Date()));
            ((ObjectNode) biz_content).put("trade_no", jsonNode.get("trade_no").textValue());

            ((ObjectNode) root).put("biz_content", biz_content);
        }

        return root;
    }

    public JsonNode tradeconfirm4MiYa(JsonNode jsonNode){
        JsonNode tradeconfirmObj = null;
        String memberNo = jsonNode.get("member_no").textValue();
        String outTradeNo = jsonNode.get("out_trade_no").textValue();

        int codes = jsonNode.get("coupon_code").size();
        try {
            if (codes == 0)
                tradeconfirmObj  = mapper.readTree("{\"biz_content\":{\"coupon_cnt\":0,\"gained_point\":0,\"ret_code\":\"00\",\"ret_msg\":\"请求成功[订单确认]\",\"status\":\"1000\",\"totalPoint\":0},\"sign\":\"94D96D915C6470E6ABC7106EFE3E5C4F\"}");
            else // 使用优惠券 "coupon_code":["362945552#44747#0#0#5.00#0#5.00"]
                tradeconfirmObj  = mapper.readTree("{\"biz_content\":{\"coupon_cnt\":0,\"gained_point\":52,\"out_trade_no\":\"2088880110131\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[订单确认]\",\"status\":\"1000\",\"totalPoint\":7628},\"sign\":\"0E863F9513AE20E66C0CCDD46EA6F58A\"}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        try {
            if (memberNo.equals("1900213189174")) {
                tradeconfirmObj  = mapper.readTree("{\"biz_content\":{\"campaignInfo\":[{\"campaignId\":\"2763\",\"campaignName\":\"集点满额礼券    \",\"stampNum\":\"1\"},{\"campaignId\":\"2764\",\"campaignName\":\"集点满额预约\",\"stampNum\":\"1\"},{\"campaignId\":\"2765\",\"campaignName\":\"集点满额寄送礼品\",\"stampNum\":\"1\"}],\"coupon_cnt\":0,\"gained_point\":0,\"out_trade_no\":\"2088880110062\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[订单确认]\",\"status\":\"1000\",\"totalPoint\":92100},\"sign\":\"13B189156CC5C57FE5B6C399B0E66671\"}");
                ((ObjectNode) tradeconfirmObj).put("out_trade_no", outTradeNo);
            }
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return tradeconfirmObj;
    }

    public JsonNode barCode4MiYa(JsonNode jsonNode){
        JsonNode barCodeObj = null;
        ObjectNode root =  mapper.createObjectNode();
        String cardNo = jsonNode.get("dynamic_id").textValue();
        String tradeNo = jsonNode.get("trade_no").textValue();
        String outTradeNo = jsonNode.get("out_trade_no").textValue();
        String feeType = jsonNode.get("fee_type").textValue();
        logger.info("dynamic_id: " + cardNo);

        try {
            if (feeType.equals("1")){ // 付费通
                logger.info("feeType is " + feeType);
                barCodeObj  = mapper.readTree("{\"biz_content\":{\"billBizInfos\":[{\"barcode\":\"573050063357885003819194\",\"bill_addr\":\"上海市嘉定区栖林路30****号(商铺)\",\"bill_amt\":3819.19,\"bill_id\":\"202306210364774693\",\"bill_month\":\"202305\",\"bill_no\":\"0063357885\",\"bill_org_id\":\"888880000502900\",\"bill_org_name\":\"上海市电力公司\",\"bill_owner\":\"******)\",\"bill_record_time\":\"40\",\"goods_id\":\"1311278\",\"is_insurance\":\"N\",\"overdue_fee\":0.00,\"status\":\"00\"}],\"biz_type\":\"04\",\"out_trade_no\":\"20291801271457\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[付费通账单查询]\"},\"sign\":\"78BE8DF0AD69B9AAEEDC060E027D4787\"}");
                JsonNode bizContent = ((ObjectNode) barCodeObj).get("biz_content");
                ((ObjectNode) bizContent).put("out_trade_no", outTradeNo);
                //JsonNode billBizInfos = ((ObjectNode) bizContent).get("billBizInfos");
                //((ObjectNode) billBizInfos).put("barcode", cardNo);

                return barCodeObj;
            }

            barCodeObj  = mapper.readTree("{\"biz_type\":\"00\",\"buyer_account\":\"2088122120375551\",\"buyer_pay_amount\":17.9,\"buyer_userid\":\"2088122120375551\",\"gmt_payment\":\"20230406092720\",\"merchant_discount\":0,\"other_dicount\":0,\"out_trade_no\":\"20311801279081\",\"outid\":\"438422885588\",\"pay_code\":\"051\",\"pay_name\":\"支付宝\",\"payment_user_id\":\"2088122120375551\",\"plat_discount\":0.0,\"ret_code\":\"00\",\"ret_msg\":\"Txn completed successfully\",\"status\":\"1000\",\"sys_trade_no\":\"2023040622001475551431288018\",\"total_fee\":17.9,\"trade_no\":\"2031180127908101\"}");
            ((ObjectNode) barCodeObj).put("out_trade_no", outTradeNo);
            ((ObjectNode) barCodeObj).put("trade_no", tradeNo);
            ((ObjectNode) barCodeObj).put("outid", "000" + tradeNo.substring(5, 14));


            if (cardNo.startsWith("3918") || cardNo.startsWith("19000")){ // 游人会员码或者罗森会员码
                barCodeObj  = mapper.readTree("{\"biz_content\":{\"biz_type\":\"02\",\"extraInfo\":\"{\\\"memberAmountFree\\\":0,\\\"memberAmountLocked\\\":0,\\\"memberAmount\\\":0,\\\"memberAmountFixed\\\":0,\\\"memberAmountFlg\\\":0,\\\"memberPromotionRec\\\":\\\"\\\",\\\"memberAmountLimited\\\":0,\\\"memberPromotion\\\":\\\"0\\\"}\",\"pay_code\":\"038\",\"pay_id\":\"038\",\"pay_name\":\"罗森点点\",\"ret_code\":\"00\",\"user_info\":{\"code\":\"1900002315401\",\"kbn\":\"1\",\"level\":\"01\",\"mobile\":\"18721460515\",\"name\":\"LAWSON测试\",\"status\":0,\"total_point\":7524,\"total_point_amount\":7.5,\"total_prepaid_amount\":1271.75}},\"sign\":\"8D936AC63B359F1C3EE35FE0D5DDAF27\"}");

            } else if (cardNo.startsWith("651680")){
                ((ObjectNode) barCodeObj).put("pay_name", "驾续多");
                ((ObjectNode) barCodeObj).put("pay_code", "026");
                if (cardNo.startsWith("6516801002")){
                    ((ObjectNode) barCodeObj).put("status", "1002");
                    ((ObjectNode) barCodeObj).put("ret_msg", "支付处理中10000请求成功");
                }
                else {
                    ((ObjectNode) barCodeObj).put("status", "1000");
                    ((ObjectNode) barCodeObj).put("ret_msg", "请求成功[PAYSUCCESS]");
                }
            }else if (cardNo.startsWith("99601841")){//卡购卡
                barCodeObj = mapper.readTree("{\"balance\":46.62,\"biz_type\":\"00\",\"out_trade_no\":\"20311801279927\",\"outid\":\"019008259411\",\"pay_code\":\"045\",\"pay_name\":\"e支付\",\"ret_code\":\"00\",\"ret_msg\":\"Txn completed successfully\",\"status\":\"1000\",\"sys_trade_no\":\"23040717091681\",\"trade_no\":\"2031180127992701\"}");
                ((ObjectNode) barCodeObj).put("out_trade_no", outTradeNo);
                ((ObjectNode) barCodeObj).put("trade_no", tradeNo);
            }else if (cardNo.startsWith("899")){
                ((ObjectNode) barCodeObj).put("biz_type", "01");
                ((ObjectNode) barCodeObj).put("pay_name", "阿拉订");
                ((ObjectNode) barCodeObj).put("pay_code", "032");
                ((ObjectNode) barCodeObj).put("pay_id", "11");
            }else if (cardNo.startsWith("77FF")){
                ((ObjectNode) barCodeObj).put("pay_name", "索迪斯");
                ((ObjectNode) barCodeObj).put("pay_code", "004");
            }else if (cardNo.startsWith("13")){
                logger.info("WeChat order");
                barCodeObj = mapper.readTree("{\"biz_type\":\"00\",\"out_trade_no\":\"35039101280601\",\"pay_code\":\"050\",\"pay_name\":\"微信支付\",\"ret_code\":\"00\",\"ret_msg\":\"支付处理中ORDERNOTEXIST订单不存在\",\"status\":\"1002\",\"trade_no\":\"3503910128060101\"}");
                queryTimes.put(tradeNo, 3);
                ((ObjectNode) barCodeObj).put("out_trade_no", outTradeNo);
                ((ObjectNode) barCodeObj).put("trade_no", tradeNo);
            } else if (cardNo.startsWith("62")){
                ((ObjectNode) barCodeObj).put("pay_name", "银联支付");
                ((ObjectNode) barCodeObj).put("pay_code", "057");
            }else if (cardNo.startsWith("28")){
                ((ObjectNode) barCodeObj).put("pay_name", "支付宝");
                ((ObjectNode) barCodeObj).put("pay_code", "051");
                ((ObjectNode) barCodeObj).put("buyer_userid", "2088002764241406");
                ((ObjectNode) barCodeObj).put("buyer_account", "2088002764241406");
                if(cardNo.startsWith("2835")){
                    ((ObjectNode) barCodeObj).put("status", "1002");
                    ((ObjectNode) barCodeObj).put("ret_msg", "支付处理中PAYWAIT[支付宝]交易创建，等待买家付款");
                }
            }else if (jsonNode.findValue("accessCode") != null ){
                ((ObjectNode) barCodeObj).put("pay_name", "华联OK实体卡");
                ((ObjectNode) barCodeObj).put("pay_code", "009");
                ((ObjectNode) barCodeObj).put("biz_type", "03");
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        tradeNoMap.put(tradeNo, barCodeObj.get("pay_code").textValue()); // 给tradeRefund接口用，根据tradeNo找pay_code

        ((ObjectNode) root).put("biz_content", barCodeObj);
        ((ObjectNode) root).put("sign", "E470852FFE8FFE393733E86F9C966724");

        return root;
    }

    public JsonNode uploadgoodsdetailReqObj4MiYa(JsonNode jsonNode){
        ObjectNode uploadObj = null;
        JsonNode biz_content = null;

        Map<String, String> m = new HashMap<>();
        m.put("391806021865243203", "{\"coupons\":[{\"barcode\":[],\"coupon_code\":\"362945552#44747#0#0#5.00#0#5.00\",\"coupon_name\":\"罗森测试满减代金10-6\",\"coupon_type\":\"20\",\"discount_price\":14.80,\"save_payment\":5.00,\"sell_kbn\":1,\"total_payment\":19.80}],\"extraInfo\":\"{\\\"memberAmountFree\\\":200.00,\\\"memberAmountLocked\\\":0.00,\\\"memberAmount\\\":0.00,\\\"memberAmountFixed\\\":0.00,\\\"memberAmountFlg\\\":0,\\\"memberPromotionRec\\\":\\\"\\\",\\\"memberAmountLimited\\\":200,\\\"memberPromotion\\\":\\\"0\\\"}\",\"out_trade_no\":\"2088880110116\",\"pay_code\":\"038\",\"pay_id\":\"038\",\"pay_name\":\"罗森点点\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[商品明细上传]\"}");
        m.put("391708071682027806", "{\"coupons\":[{\"barcode\":[\"6902827190343\"],\"coupon_code\":\"362945542#44746#0#0#1.75#0#1.75\",\"coupon_name\":\"罗森测试050733卖变5折\",\"coupon_type\":\"10\",\"discount_price\":5.25,\"save_payment\":1.75,\"sell_kbn\":0,\"total_payment\":7.00}],\"discount_commodity_list\":[{\"after_discount_price\":1.75,\"before_discount_price\":3.5,\"commodity_barcode\":\"6902827190343\",\"discount_payment\":1.75,\"discount_quantity\":1}],\"extraInfo\":\"{\\\"memberAmountFree\\\":200.00,\\\"memberAmountLocked\\\":0.00,\\\"memberAmount\\\":0.00,\\\"memberAmountFixed\\\":0.00,\\\"memberAmountFlg\\\":0,\\\"memberPromotionRec\\\":\\\"\\\",\\\"memberAmountLimited\\\":200,\\\"memberPromotion\\\":\\\"0\\\"}\",\"out_trade_no\":\"2088880110119\",\"pay_code\":\"038\",\"pay_id\":\"038\",\"pay_name\":\"罗森点点\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[商品明细上传]\"}");
        String out_trade_no = jsonNode.get("out_trade_no").textValue();
        String member_no = jsonNode.get("member_no").textValue();

        try {
            uploadObj = mapper.createObjectNode();
            if (jsonNode.findValue("member_no") != null ){
                if (member_no.equals("391806021865243203"))
                    biz_content = mapper.readTree(m.get("391806021865243203"));
                else if (member_no.equals("391708071682027806"))
                    biz_content = mapper.readTree(m.get("391708071682027806")); // 返回中包含折扣
            }
            else {
                biz_content =  mapper.readTree("{\"extraInfo\":\"{\\\"memberAmountFree\\\":200.00,\\\"memberAmountLocked\\\":0.00,\\\"memberAmount\\\":0.00,\\\"memberAmountFixed\\\":0.00,\\\"memberAmountFlg\\\":0,\\\"memberPromotionRec\\\":\\\"\\\",\\\"memberAmountLimited\\\":200,\\\"memberPromotion\\\":\\\"0\\\"}\",\"out_trade_no\":\"2088880110117\",\"pay_code\":\"038\",\"pay_id\":\"038\",\"pay_name\":\"罗森点点\",\"ret_code\":\"00\",\"ret_msg\":\"请求成功[商品明细上传]\"}");
            }

            //((ObjectNode) biz_content).remove("out_trade_no");
            ((ObjectNode) biz_content).put("out_trade_no", out_trade_no);
            uploadObj.set("biz_content", biz_content);
            uploadObj.put("sign", "8D936AC63B359F1C3EE35FE0D5DDAF27");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return uploadObj;
    }

    public JsonNode unfreezeReqObj4MiYa(JsonNode jsonNode){
        ObjectNode unfreezeObj = null;
        String out_trade_no = jsonNode.get("out_trade_no").textValue();

        try {
            unfreezeObj = mapper.createObjectNode();
            JsonNode biz_content =  mapper.readTree("{\"out_trade_no\":\"2088880110117\",\"ret_code\":\"00\",\"ret_msg\":\"撤销成功\"}");
            //((ObjectNode) biz_content).remove("out_trade_no");
            ((ObjectNode) biz_content).put("out_trade_no", out_trade_no);
            unfreezeObj.set("biz_content", biz_content);
            unfreezeObj.put("sign", "0F2AC47102B92EF8A89361C772007611");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return unfreezeObj;
    }
    public JsonNode tradequeryReqObj4MiYa(JsonNode jsonNode){
        ObjectNode tradequeryObj = null;
        JsonNode biz_content = null;
        String tradeNo = jsonNode.get("trade_no").textValue();
        String outTradeNo = jsonNode.get("out_trade_no").textValue();
        String dynamicId = jsonNode.get("dynamic_id").textValue();
        try {
            tradequeryObj = mapper.createObjectNode();
            int counter = Integer.valueOf(queryTimes.get(tradeNo));
            if(dynamicId.startsWith("133")||counter!=0){
                biz_content =  mapper.readTree("{\"biz_type\":\"00\",\"out_trade_no\":\"35039101280601\",\"pay_code\":\"050\",\"pay_name\":\"微信支付\",\"ret_code\":\"00\",\"ret_msg\":\"支付处理中ORDERNOTEXIST订单不存在\",\"status\":\"1002\",\"trade_no\":\"3503910128060101\"}");
                ((ObjectNode) biz_content).put("out_trade_no", outTradeNo);
                ((ObjectNode) biz_content).put("trade_no", tradeNo);
                queryTimes.put(tradeNo, --counter);
            }
            else {
                biz_content =  mapper.readTree("{\"ret_code\":\"00\",\"ret_msg\":\"请求成功[PAYSUCCESS]\",\"biz_type\":\"00\",\"out_trade_no\":\"35049502338772\",\"trade_no\":\"3504950233877201\",\"status\":\"1000\",\"pay_code\":\"050\",\"pay_name\":\"微信支付\",\"openid\":\"o1vUMt2B1QOMZrWUoPhTsCcTBd8w\",\"sub_openid\":\"omCzZw8NH-mZi5nse1Dei7sgg5c0\",\"gmt_payment\":\"20240125174703\",\"sys_trade_no\":\"4200066300202401254656344971\",\"total_fee\":4,\"buyer_pay_amount\":4,\"merchant_discount\":0,\"plat_discount\":0,\"other_dicount\":0,\"is_subscribe\":\"N\",\"outid\":\"3504950233877201\",\"payment_user_id\":\"omCzZw8NH-mZi5nse1Dei7sgg5c0\",\"payment_merchant_id\":\"wx5f57b93140cbea5c\"}");
                ((ObjectNode) biz_content).put("out_trade_no", outTradeNo);
                ((ObjectNode) biz_content).put("trade_no", tradeNo);
            }

            tradequeryObj.set("biz_content", biz_content);
            tradequeryObj.put("sign", "0F2AC47102B92EF8A89361C772007611");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return tradequeryObj;
    }

    public JsonNode traderefundReqObj4MiYa(JsonNode jsonNode){
        ObjectNode traderefundObj = null;
        String oldTradeNo = jsonNode.get("old_trade_no").textValue();
        try {
            String payCode = tradeNoMap.containsKey(oldTradeNo)?tradeNoMap.get(oldTradeNo):"000";
            traderefundObj = mapper.createObjectNode();
            JsonNode biz_content =  mapper.readTree("{\"biz_type\":\"00\",\"outid\":\"000893430133\",\"pay_code\":\"000\",\"pay_name\":\"没有找到支付方式\",\"ret_code\":\"00\",\"ret_msg\":\"Txn completed successfully\",\"status\":\"2000\",\"sys_trade_no\":\"932304131639209687778\"}");
            ((ObjectNode) biz_content).put("outid", "000" + oldTradeNo.substring(9));
            if (payCode.equals("051")){
                ((ObjectNode) biz_content).put("pay_code", "051");
                ((ObjectNode) biz_content).put("pay_name", "支付宝");
            }
            if (payCode.equals("050")){
                ((ObjectNode) biz_content).put("pay_code", "050");
                ((ObjectNode) biz_content).put("pay_name", "微信支付");
            }

            traderefundObj.set("biz_content", biz_content);
            traderefundObj.put("sign", "EE02255F5AC78A7B536C266AAD6C7029");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return traderefundObj;
    }
}
