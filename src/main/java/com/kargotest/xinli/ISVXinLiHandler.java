package com.kargotest.xinli;

import net.freeutils.httpserver.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;

public class ISVXinLiHandler implements HTTPServer.ContextHandler {
    private static final Logger logger = LogManager.getLogger(ISVXinLiHandler.class);
    private static int queryLimit = 5;
    private String result = "";

    public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
        String xinLiBody = HTTPServer.convert(req.getBody(), Charset.forName("UTF-8"));

        logger.info(xinLiBody);

        String path = req.getPath().split("/")[1];

        if(path.equals("Payment")){
            result = "{\"print_info\":\"的侧就哦股份认购热天给他人\",\"actpayamt\":\"10\",\"amount\":\"11\",\"bank_biz_time\":\"20240204141537\",\"bank_order_no\":\"3310000069222927002324EFb\",\"channel_type\":\"3\",\"err_code\":\"000000\",\"mch_id\":\"105331000006922\",\"mch_name\":\"测试商户-20180911\",\"mch_order_no\":\"32218d72c20ff2000000WL\",\"pos_id\":\"0001\",\"refno\":\"710356677443\",\"result_code\":\"123\",\"shop_id\":\"208888\",\"shopaddr\":\"测试地址-20180911\",\"shopname\":\"测试门店-20180911\",\"sign\":\"4076C31E728B0058E6688C3A5F81FC0C\",\"sign_method\":\"A\",\"term_id\":\"00402927\",\"timestamp\":\"20240204141538\"}";
        }
        else if(path.equals("OrderQuery")){
            if (queryLimit<0)
                result = "{\"disamt\":\"10\",\"actpayamt\":\"1\",\"amount\":\"11\",\"bank_biz_time\":\"20240204141537\",\"bank_order_no\":\"3310000069222927002324EFb\",\"channel_type\":\"3\",\"err_code\":\"000000\",\"mch_id\":\"105331000006922\",\"mch_name\":\"测试商户-20180911\",\"mch_order_no\":\"32218d72c20ff2000000WL\",\"pos_id\":\"0001\",\"refno\":\"710356677443\",\"result_code\":\"0\",\"shop_id\":\"208888\",\"shopaddr\":\"测试地址-20180911\",\"shopname\":\"测试门店-20180911\",\"sign\":\"4076C31E728B0058E6688C3A5F81FC0C\",\"sign_method\":\"A\",\"term_id\":\"00402927\",\"timestamp\":\"20240204141538\"}";
            else
                result = "{\"disamt\":\"10\",\"actpayamt\":\"1\",\"amount\":\"11\",\"bank_biz_time\":\"20240204141537\",\"bank_order_no\":\"3310000069222927002324EFb\",\"channel_type\":\"3\",\"err_code\":\"000000\",\"mch_id\":\"105331000006922\",\"mch_name\":\"测试商户-20180911\",\"mch_order_no\":\"32218d72c20ff2000000WL\",\"pos_id\":\"0001\",\"refno\":\"710356677443\",\"result_code\":\"NNN2\",\"shop_id\":\"208888\",\"shopaddr\":\"测试地址-20180911\",\"shopname\":\"测试门店-20180911\",\"sign\":\"4076C31E728B0058E6688C3A5F81FC0C\",\"sign_method\":\"A\",\"term_id\":\"00402927\",\"timestamp\":\"20240204141538\"}";
        }
        else if(path.equals("Refund")){
            result = "{\"amount\":\"1\",\"bank_biz_time\":\"20240204141605\",\"bank_order_no\":\"3310000069222927002324EFb\",\"bank_refund_order_no\":\"020414160517\",\"channel_type\":\"3\",\"err_code\":\"000000\",\"err_msg\":\"交易成功\",\"mch_id\":\"105331000006922\",\"mch_name\":\"测试商户-20180911\",\"mch_order_no\":\"32218d72c27c36000000WL\",\"pos_id\":\"0001\",\"refno\":\"710356677779\",\"result_code\":\"0\",\"shop_id\":\"208888\",\"shopaddr\":\"测试地址-20180911\",\"shopname\":\"测试门店-20180911\",\"sign\":\"32981C467A422A22969B02C8C43EB1A0\",\"sign_method\":\"A\",\"term_id\":\"00402927\",\"timestamp\":\"20240204141605\"}";
        }

        queryLimit--;
        resp.getHeaders().add("Content-Type", "application/json");
        resp.send(200, result);
        return 0;
    }

}
