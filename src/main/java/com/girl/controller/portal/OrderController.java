package com.girl.controller.portal;


import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.User;
import com.girl.service.IOrderService;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by girl on 2017/6/9.
 */
@Controller
@RequestMapping(value = "/order/")
public class OrderController {

    @Resource
    private IOrderService iOrderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * 创建订单
     *
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "create_order.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse createOrder(HttpSession session, Integer shippingId) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        return this.iOrderService.createOrder(user.getId(), shippingId);
    }

    /**
     * 取消订单
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "cancel_order.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse cancelOrder(HttpSession session, Long orderNo) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        return this.iOrderService.cancelOrder(user.getId(), orderNo);
    }

    /**
     * 获取购物车中已经选中的商品信息
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "get_order_cart_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        return this.iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 获取订单详情
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "order_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderDetail(HttpSession session, Long orderNo) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        return this.iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    /**
     * 获取所有订单信息
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "order_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        return this.iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }

    /**
     * 支付宝支付
     *
     * @param session
     * @param orderNo：订单号
     * @param request
     * @return
     */
    @RequestMapping(value = "pay.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        // 需要将alipay生成的二维码上传到FTP服务器上
        String path = request.getSession().getServletContext().getRealPath("upload");

        return this.iOrderService.pay(user.getId(), path, orderNo);
    }

    /**
     * 支付宝回调函数
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "alipay_callback.do", method = RequestMethod.POST)
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String> params = Maps.newHashMap();

        // 支付宝将回调信息以map的形式放入request中
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        logger.info("支付宝回调，签名:{}，交易状态:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        // 验证回调的正确性是不是支付宝发的，并
        params.remove("sign_type");
        try {
            // 使用RSA2的验签方法
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());

            if (!alipayRSACheckedV2) {
               return ServerResponse.createBySuccessMessage("非法请求，验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.info("支付宝验证回调异常", e);
        }

        // 验证请求
        ServerResponse serverResponse = this.iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }

        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 查询支付状态
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "query_order_pay_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先进行登录");
        }

        ServerResponse response = this.iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }

        return ServerResponse.createBySuccess(false);
    }

}
