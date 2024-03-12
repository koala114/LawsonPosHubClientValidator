package com.kargotest.miya;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.freeutils.httpserver.HTTPServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

public class DuoDian implements HTTPServer.ContextHandler {
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
        String reqBodyStr = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));

        ObjectMapper mapper = new ObjectMapper();
        String path = req.getPath().split("/")[3];

        if(path.equals("hangupCalculate")){
            JsonNode couponReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = couponHangupCalculate(couponReqObj);
            result = jsonNode.toString();
            //result = "{\"success\":false,\"code\":100005,\"message\":\"未获取到对应的商品7位编码转换6位编码关系\",\"data\":null,\"trace_id\":null}";
        } else if (path.equals("unlockCoupon")) {
            JsonNode couponReqObj = mapper.readTree(reqBodyStr);
            Iterator<String> iterator = couponReqObj.fieldNames();
            iterator.forEachRemaining(e -> System.out.println(e));

            JsonNode jsonNode = couponUnlockCoupon(couponReqObj);
            result = jsonNode.toString();
        }else if (path.equals("useCoupon")) {
            JsonNode useCouponReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = useCouponCoupon(useCouponReqObj);
            result = jsonNode.toString();
        }else if (path.equals("recoverCoupon")) {
            JsonNode recoverCouponReqObj = mapper.readTree(reqBodyStr);
            JsonNode jsonNode = recoverCoupon(recoverCouponReqObj);
            result = jsonNode.toString();
        }

        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);

        return 0;
    }
    public JsonNode recoverCoupon(JsonNode jsonNode) throws JsonProcessingException {
        JsonNode recoverCouponObj = null;
        JsonNode arrNode = null;
        if(jsonNode.get("orderUseCoupsReqs").isNull()){
            recoverCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[回滚接口]失败:orderUseCoupsReqs不能为空\",\"data\":null,\"trace_id\":null}");

            return recoverCouponObj;
        }
        else
            arrNode = jsonNode.get("orderUseCoupsReqs");

        if (arrNode.isArray()) {
            if(arrNode.size()>0) {
                if (arrNode.get(0).get("couponCode").asText().equals("SJXJ000000000000000000000")) {
                    recoverCouponObj = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[回滚接口]失败:优惠券未使用\",\"data\":null,\"trace_id\":null}");
                } else
                    recoverCouponObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":null,\"trace_id\":null}");
            }
            else
                recoverCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[回滚接口]失败:orderUseCoupsReqs不能为空\",\"data\":null,\"trace_id\":null}");
        }
        else
            recoverCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[回滚接口]失败:orderUseCoupsReqs不能为空\",\"data\":null,\"trace_id\":null}");

        return recoverCouponObj;
    }
    public JsonNode useCouponCoupon(JsonNode jsonNode) throws JsonProcessingException {
        JsonNode useCouponObj = null;
        JsonNode arrNode = null;

        if (jsonNode.get("couponDtoList").isNull()) {
            useCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[优惠券解锁接口]失败:couponCodeList不能为空\",\"data\":null,\"trace_id\":null}");

            return useCouponObj;
        }
        else
            arrNode = jsonNode.get("couponDtoList");

        if (arrNode.isArray()) {
            if(arrNode.size()>0)
                useCouponObj  = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":null,\"trace_id\":null}");
            else
                useCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[优惠券解锁接口]失败:couponCodeList不能为空\",\"data\":null,\"trace_id\":null}");
        }

        return useCouponObj;
    }
    public JsonNode couponUnlockCoupon(JsonNode jsonNode) throws JsonProcessingException {
        JsonNode unlockCouponObj = null;
        JsonNode arrNode = null;
        if (jsonNode.get("couponCodeList").isNull()) {
            unlockCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[优惠券解锁接口]失败:couponCodeList不能为空\",\"data\":null,\"trace_id\":null}");

            return unlockCouponObj;
        }
        else
            arrNode = jsonNode.get("couponCodeList");

        if (arrNode.isArray()) {
            if(arrNode.size()>0)
                unlockCouponObj  = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":null,\"trace_id\":null}");
            else
                unlockCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[优惠券解锁接口]失败:couponCodeList不能为空\",\"data\":null,\"trace_id\":null}");
        }
        else
            unlockCouponObj  = mapper.readTree("{\"success\":false,\"code\":100002,\"message\":\"调取[优惠券解锁接口]失败:couponCodeList不能为空\",\"data\":null,\"trace_id\":null}");

        return unlockCouponObj;
    }

    public JsonNode couponHangupCalculate(JsonNode jsonNode) throws JsonProcessingException {
        JsonNode missOtpCode = otpCodeValidator(jsonNode);
        JsonNode missWareReqVOList = wareReqVOListValidator(jsonNode);

        if(!(otpCodeValidator(jsonNode)==null))
            return missOtpCode;
        if(!(otpCodeValidator(jsonNode)==null))
            return missWareReqVOList;

        // 默认多点优惠券返回
        JsonNode hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":1300,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[{\"uuid\":\"1\",\"matnr\":\"5009958\",\"sixMatnr\":\"409722\",\"couponShare\":1300,\"couponShareVOList\":[{\"code\":\"SJMJ100122303000000000009\",\"applyId\":75591,\"applyName\":\"全场通用卖变券（可叠加）\",\"shareValue\":500},{\"code\":\"SJXJ100122303000000000021\",\"applyId\":75586,\"applyName\":\"全场通用代金券（可叠加）\",\"shareValue\":800}]}],\"yList\":[{\"couponCode\":\"SJXJ100122303000000000021\",\"batchId\":75586,\"cusId\":12,\"typeUseCode\":\"1\",\"actualValue\":800,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"全场通用代金券（可叠加）仅限测试使用\",\"startDate\":\"2023-03-27T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"8元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"全场通用代金券（可叠加）现金券\",\"limitSuperimpose\":1,\"recommend\":true,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩2天\",\"value\":800,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1,\"invoiceLimit\":1,\"couonPayCode\":\"093\"},{\"couponCode\":\"SJMJ100122303000000000009\",\"batchId\":75591,\"cusId\":12,\"typeUseCode\":\"2\",\"actualValue\":500,\"quota\":2000,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"全场通用卖变券仅限测试用\",\"startDate\":\"2023-03-27T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"5元满减券\",\"logoLink\":\"\",\"frontDisplayName\":\"全场通用卖变券（可叠加）\",\"limitSuperimpose\":1,\"recommend\":false,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩2天\",\"value\":500,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":0,\"invoiceLimit\":1,\"couonPayCode\":\"093\"}],\"waste\":false,\"nList\":[],\"nListReason\":{}},\"trace_id\":null}");

        ObjectMapper mapper = new ObjectMapper();
        String otpCode = jsonNode.get("otpCode").textValue();
        if (otpCode.equals("L99999999999999999") || otpCode.isEmpty()){
            try{
                hangupCalculateObj = mapper.readTree("{\"success\":false,\"code\":100004,\"message\":\"未获取到会员ID转换关系\",\"data\":null,\"trace_id\":null}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        if (otpCode.equals("L00000000000000000")){
            try{
                // wareShareVOList.size() == 1
                // couponShareVOList.size() == 0
                // yList.size() == 0;
                hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":0,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[],\"yList\":[],\"waste\":false,\"nList\":[],\"nListReason\":{},\"couonPayCode\":\"024\"},\"trace_id\":null}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        if (otpCode.equals("L12522847241471238")){
            try{
                // wareShareVOList.size() == 1
                // couponShareVOList.size() == 2
                // yList.size() == 2;
                hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":1300,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[{\"uuid\":\"1\",\"matnr\":\"5009958\",\"sixMatnr\":\"409722\",\"couponShare\":1300,\"couponShareVOList\":[{\"code\":\"SJMJ100122303000000000009\",\"applyId\":75591,\"applyName\":\"全场通用卖变券（可叠加）\",\"shareValue\":500},{\"code\":\"SJXJ100122303000000000021\",\"applyId\":75586,\"applyName\":\"全场通用代金券（可叠加）\",\"shareValue\":800}]}],\"yList\":[{\"couponCode\":\"SJXJ100122303000000000021\",\"batchId\":75586,\"cusId\":12,\"typeUseCode\":\"1\",\"actualValue\":800,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"全场通用代金券（可叠加）仅限测试使用\",\"startDate\":\"2023-03-27T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"8元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"全场通用代金券（可叠加）现金券\",\"limitSuperimpose\":1,\"recommend\":true,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩2天\",\"value\":800,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1},{\"couponCode\":\"SJMJ100122303000000000009\",\"batchId\":75591,\"cusId\":12,\"typeUseCode\":\"2\",\"actualValue\":500,\"quota\":2000,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"全场通用卖变券仅限测试用\",\"startDate\":\"2023-03-27T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"5元满减券\",\"logoLink\":\"\",\"frontDisplayName\":\"全场通用卖变券（可叠加）\",\"limitSuperimpose\":1,\"recommend\":false,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩2天\",\"value\":500,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":0}],\"waste\":false,\"nList\":[],\"nListReason\":{},\"couonPayCode\":\"024\"},\"trace_id\":null}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        if (otpCode.equals("L10768928895470144")){
            try{
                // wareShareVOList.size() == 2
                // ----> couponShareVOList
                // ----> couponShareVOList
                // yList.size() == 1;
                hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":500,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[{\"uuid\":\"1\",\"matnr\":\"1320072\",\"sixMatnr\":\"320072\",\"couponShare\":7,\"couponShareVOList\":[{\"code\":\"SJMJ100122303000000000008\",\"applyId\":75581,\"applyName\":\"全场通用卖变券（不可叠加）\",\"shareValue\":7}]},{\"uuid\":\"2\",\"matnr\":\"5006090\",\"sixMatnr\":\"405856\",\"couponShare\":493,\"couponShareVOList\":[{\"code\":\"SJMJ100122303000000000008\",\"applyId\":75581,\"applyName\":\"全场通用卖变券（不可叠加）\",\"shareValue\":493}]}],\"yList\":[{\"couponCode\":\"SJMJ100122303000000000008\",\"batchId\":75581,\"cusId\":12,\"typeUseCode\":\"2\",\"actualValue\":500,\"quota\":1000,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"全场通用卖变券仅限测试用\",\"startDate\":\"2023-03-27T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"5元满减券\",\"logoLink\":\"\",\"frontDisplayName\":\"全场通用卖变券满减券\",\"limitSuperimpose\":0,\"recommend\":true,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩2天\",\"value\":500,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":0}],\"waste\":false,\"nList\":[],\"nListReason\":{},\"couonPayCode\":\"024\"},\"trace_id\":null}\n");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        if (otpCode.equals("L10768621806866915")){
            // 优惠券不适用
            try{
                // wareShareVOList.size() == 0
                // yList.size() == 0;
                // nList.size() == 5;nListReason.size() == 5
                hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":0,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[],\"yList\":[],\"waste\":false,\"nList\":[{\"couponCode\":\"SJXJ100952303000000000179\",\"batchId\":75611,\"cusId\":9895,\"typeUseCode\":\"1\",\"actualValue\":null,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"111\",\"startDate\":\"2023-03-20T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"2元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"现金券\",\"limitSuperimpose\":0,\"recommend\":false,\"canCheck\":null,\"checked\":false,\"invalidReasonVO\":{\"invalidTip\":\"不可用原因：\",\"invalidDesc\":\"来自POS用券场景需要激活\"},\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩4天\",\"value\":200,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1},{\"couponCode\":\"SJXJ100952303000000000184\",\"batchId\":75626,\"cusId\":9895,\"typeUseCode\":\"1\",\"actualValue\":null,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"3元现金券\",\"startDate\":\"2023-03-21T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"3元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"现金券\",\"limitSuperimpose\":0,\"recommend\":false,\"canCheck\":null,\"checked\":false,\"invalidReasonVO\":{\"invalidTip\":\"不可用原因：\",\"invalidDesc\":\"来自POS用券场景需要激活\"},\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩4天\",\"value\":300,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1},{\"couponCode\":\"SJXJ100952303000000000185\",\"batchId\":75631,\"cusId\":9895,\"typeUseCode\":\"1\",\"actualValue\":null,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"322POS场景验证\",\"startDate\":\"2023-03-21T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"6元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"现金券\",\"limitSuperimpose\":0,\"recommend\":false,\"canCheck\":null,\"checked\":false,\"invalidReasonVO\":{\"invalidTip\":\"不可用原因：\",\"invalidDesc\":\"来自POS用券场景需要激活\"},\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩4天\",\"value\":600,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1},{\"couponCode\":\"SJXJ100952303000000000186\",\"batchId\":75636,\"cusId\":9895,\"typeUseCode\":\"1\",\"actualValue\":null,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"322POS场景验证\",\"startDate\":\"2023-03-21T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"2元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"现金券\",\"limitSuperimpose\":1,\"recommend\":false,\"canCheck\":null,\"checked\":false,\"invalidReasonVO\":{\"invalidTip\":\"不可用原因：\",\"invalidDesc\":\"来自POS用券场景需要激活\"},\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩4天\",\"value\":200,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1},{\"couponCode\":\"SJXJ100952303000000000187\",\"batchId\":75641,\"cusId\":9895,\"typeUseCode\":\"1\",\"actualValue\":null,\"quota\":null,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"322POS场景验证\",\"startDate\":\"2023-03-21T16:00:00.000+00:00\",\"endDate\":\"2023-03-31T15:59:59.000+00:00\",\"label\":\"3元现金券\",\"logoLink\":\"\",\"frontDisplayName\":\"现金券\",\"limitSuperimpose\":1,\"recommend\":false,\"canCheck\":null,\"checked\":false,\"invalidReasonVO\":{\"invalidTip\":\"不可用原因：\",\"invalidDesc\":\"来自POS用券场景需要激活\"},\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效期至2023.03.31 23:59\",\"remainDayRemark\":\"仅剩4天\",\"value\":300,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":1}],\"nListReason\":{\"SJXJ100952303000000000187\":\"3102\",\"SJXJ100952303000000000179\":\"3102\",\"SJXJ100952303000000000184\":\"3102\",\"SJXJ100952303000000000186\":\"3102\",\"SJXJ100952303000000000185\":\"3102\"},\"couonPayCode\":\"024\"},\"trace_id\":null}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        if (otpCode.startsWith("D1")){
            try{
                // wareShareVOList.size() == 0
                // yList.size() == 0;
                // nList.size() == 5;nListReason.size() == 5
                hangupCalculateObj = mapper.readTree("{\"success\":true,\"code\":0,\"message\":\"成功\",\"data\":{\"couponAmount\":800,\"freightCouponAmount\":null,\"freightCouponVo\":null,\"wareShareVOList\":[{\"uuid\":\"1\",\"matnr\":\"5008423\",\"sixMatnr\":\"408189\",\"couponShare\":800,\"couponShareVOList\":[{\"code\":\"SJMJ100662308000000001133\",\"applyId\":76746,\"applyName\":\"抖音8元满减组合券双兼容\",\"shareValue\":800,\"marketAssumeInfoVO\":{\"applyId\":76746,\"applyName\":\"抖音8元满减组合券双兼容\",\"code\":\"SJMJ100662308000000001133\",\"maibianShareValue\":240,\"daijinShareValue\":560}}]}],\"yList\":[{\"couponCode\":\"SJMJ100662308000000001133\",\"batchId\":76746,\"cusId\":90366,\"typeUseCode\":\"2\",\"actualValue\":800,\"quota\":800,\"maxValue\":null,\"discount\":null,\"limitRemark\":\"抖音8元满减组合券双兼容\",\"startDate\":\"2023-08-16T07:20:00.000+00:00\",\"endDate\":\"2023-09-30T15:59:59.000+00:00\",\"label\":\"抖音8元满减组合券双兼容\",\"logoLink\":\"\",\"frontDisplayName\":\"抖音8元满减组合券双兼容\",\"limitSuperimpose\":0,\"recommend\":true,\"canCheck\":1,\"checked\":true,\"invalidReasonVO\":null,\"limitSceneRemark\":[\"到店可用\"],\"validDateRemark\":\"有效>期至2023.09.30 23:59\",\"remainDayRemark\":null,\"value\":800,\"singleUseNum\":1,\"waste\":false,\"couponMarketType\":3,\"excludePromotion\":2,\"excludeThirdVip\":2}],\"waste\":false,\"nList\":[],\"nListReason\":{},\"couonPayCode\":\"025\",\"marketAssumeInfoVOList\":[{\"applyId\":76746,\"applyName\":\"抖音8元满减组合券双兼容\",\"code\":\"SJMJ100662308000000001133\",\"maibianShareValue\":240,\"daijinShareValue\":560}]},\"trace_id\":null}");
            }catch (Exception e){
                System.out.println(e.getStackTrace());
            }
            return hangupCalculateObj;
        }
        return hangupCalculateObj;
    }

    public JsonNode otpCodeValidator(JsonNode jsonNode) throws JsonProcessingException {
        if (jsonNode.findValue("otpCode") == null || jsonNode.get("otpCode").isNull()){
            return mapper.readTree("{\"success\":false,\"code\":999999,\"message\":\"没有传 otpCode \",\"data\":null,\"trace_id\":null}");
        }
        else
            return null;
    }

    public JsonNode wareReqVOListValidator(JsonNode jsonNode) throws JsonProcessingException {
        if (jsonNode.findValue("wareReqVOList") == null || jsonNode.get("wareReqVOList").size()==0){
            return mapper.readTree("{\"success\":false,\"code\":999999,\"message\":\"没有传 wareReqVOList \",\"data\":null,\"trace_id\":null}");
        }
        else
            return null;
    }
}
