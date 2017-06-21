package com.girl.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.girl.common.Const;
import com.girl.common.ServerResponse;
import com.girl.dao.*;
import com.girl.pojo.*;
import com.girl.service.IOrderService;
import com.girl.util.BigDecimalUtil;
import com.girl.util.DateTimeUtil;
import com.girl.util.FTPUtil;
import com.girl.util.PropertiesUtil;
import com.girl.vo.OrderItemVo;
import com.girl.vo.OrderProductVo;
import com.girl.vo.OrderVo;
import com.girl.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by girl on 2017/6/9.
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private PayInfoMapper payInfoMapper;

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ShippingMapper shippingMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 创建订单
     *
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        // 从购物车中获取已经选中的数据
        List<Cart> cartList = this.cartMapper.selectCheckedCartByUserId(userId);

        // 获取订单明细信息
        ServerResponse response = this.getCartOrderItem(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }

        // 计算订单的总价
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        BigDecimal payment = this.getPayment(orderItemList);

        // 生成订单
        logger.info("开始创建订单");
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {
            logger.info("订单创建失败");
            return ServerResponse.createByErrorMessage("订单创建失败");
        }
        logger.info("订单创建成功");

        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        // 填充OrderItem中的订单编号
        for (OrderItem orderItem:orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        // mybatis批量插入OrderItem
        this.orderItemMapper.batchInsert(orderItemList);

        // 减少商品的库存
        this.reduceProductStock(orderItemList);

        // 清空购物车
        this.cleanCart(cartList);

        // 返回给前端数据
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    // 组装OrderVo
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = this.shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            // 组装ShippingVo
            ShippingVo shippingVo = this.assembleShippingVo(shipping);
            orderVo.setShippingVo(shippingVo);
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem:orderItemList) {
            OrderItemVo orderItemVo = this.assembleOrderItemVO(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }

    // 组装ShippingVo
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());

        return shippingVo;
    }

    // 组装OrderItemVo
    private OrderItemVo assembleOrderItemVO(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getUpdateTime()));
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        return orderItemVo;
    }

    // 获取订单明细信息
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();

        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        // 校验购物车的数据，包括商品的状态和数量
        for (Cart cart:cartList) {
            OrderItem orderItem = new OrderItem();

            Product product = this.productMapper.selectByPrimaryKey(cart.getProductId());
            // 校验商品状态
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("商品 " + product.getName() + " 不是在线售卖状态");
            }

            // 校验库存
            if (cart.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("商品 " + product.getName() + " 库存不足");
            }

            // 组装OrderItem
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity(), product.getPrice().doubleValue()));

            orderItemList.add(orderItem);
        }

        return ServerResponse.createBySuccess(orderItemList);
    }

    // 计算订单总价
    private BigDecimal getPayment(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");

        for (OrderItem orderItem:orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }

        return payment;
    }

    // 创建订单
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();

        long orderNo = this.generateOrderNo();
        logger.info("生成的订单编号：" + orderNo);
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);

        int insertCount = this.orderMapper.insert(order);
        if (insertCount > 0) {
            return order;
        }

        return null;
    }

    // 生成订单编号
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        System.out.println(currentTime);

        return currentTime + new Random().nextInt(100);
    }

    // 订单创建成功后减少商品库存
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = this.productMapper.selectByPrimaryKey(orderItem.getProductId());

            if (product.getStock() - orderItem.getQuantity() > 0) {
                product.setStock(product.getStock() - orderItem.getQuantity());
                this.productMapper.updateByPrimaryKeySelective(product);
            }
        }
    }

    // 订单创建成功后清空购物车
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            this.cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 取消订单
     *
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse cancelOrder(Integer userId, Long orderNo) {
        Order order = this.orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户订单不存在");
        }

        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("该订单已付款，无法取消订单");
        }

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int updateCount = this.orderMapper.updateByPrimaryKey(updateOrder);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("订单取消成功");
        }

        return ServerResponse.createByErrorMessage("订单取消失败");
    }

    /**
     * 获取购物车中已经选中的商品信息
     *
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();

        // 从购物车中获取数据
        List<Cart> cartList = this.cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse response = this.getCartOrderItem(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem:orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVO(orderItem));
        }

        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(payment);

        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取订单详情
     *
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = this.orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有找到该订单");
        }

        List<OrderItem> orderItemList = this.orderItemMapper.getOrderItemByOrderNoAndUserId(orderNo, userId);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获取所有订单信息
     *
     * @param userId
     * @return
     */
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = this.orderMapper.getOrderListByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    // 组装List<OrderVo>
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();

        for (Order order:orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {// 管理员
                // 管理员查询的时候，不需要传入userId
                orderItemList = this.orderItemMapper.getOrderItemByOrderNo(order.getOrderNo());
            } else {// 普通用户
                orderItemList = this.orderItemMapper.getOrderItemByOrderNoAndUserId(order.getOrderNo(), userId);
            }

            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }

        return orderVoList;
    }

    /**
     * 支付
     *
     * @param userId
     * @param path
     * @param orderNo
     * @return
     */
    public ServerResponse pay(Integer userId, String path, Long orderNo) {
        Map<String, String> resultMap = Maps.newHashMap();

        // 通过userId和orderNo验证该订单是否存在
        Order order = this.orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }

        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // 组装生成支付宝订单参数
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("shopping_mall扫码支付，订单号：").append(order.getOrderNo()).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品，共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        // 通过orderNo获取当前登录用户的商品明细列表
        List<OrderItem> orderItemList = this.orderItemMapper.getOrderItemByOrderNoAndUserId(orderNo, userId);
        for (OrderItem orderItem:orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        logger.info("初始化配置文件");
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        logger.info("加载订单信息");
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        logger.info("支付宝预下单");
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 生成二维码并上传到ftp服务器上，通过组装后的URL返回给前端
                // 创建图片上传到本地服务器上的路径
                File folder = new File(path);
                if (folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 获取上传到本地服务器的路径
                // 需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                // 获取上传二维码的名称
                String qrFileName = String.format("/qr-%s.png", response.getOutTradeNo());
                // 生成二维码
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                logger.info("二维码上传路径：" + qrPath);

                // 将名称为qrFileName的二维码图片上传到FTP服务器
                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("FTP服务器上传二维码异常：", e);
                }

                String quUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", quUrl);

                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        System.out.println("应答开始");
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
        System.out.println("应答结束");
    }

    /**
     * 支付宝回调
     *
     * @param params
     * @return
     */
    public ServerResponse aliCallback(Map<String, String> params) {
        // 获取回调的订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        // 获取交易号
        String tradeNo = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");

        // 通过订单号查询该订单是否存在
        Order order = this.orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("未知订单，回调忽略");
        }

        // 如果该订单的支付状态为“已支付”，则直接返回
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }

        // 判断交易状态，如果交易成功，则将订单中的交易状态设置为“已付款”
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));// 设置订单付款时间
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());// 设置订单状态

            this.orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        this.payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    /**
     * 查询支付状态
     *
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        // 通过订单编号和userId查询该订单信息是否存在
        Order order = this.orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }

        // 如果支付成功（PAID及PAID以上为支付成功，否则支付失败）
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    /**
     * 管理员获取所有订单信息
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageOrderList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = this.orderMapper.getOrderList();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员获取一个订单详情信息
     *
     * @param orderNo
     * @return
     */
    public ServerResponse manageOrderDetail(Long orderNo) {
        Order order = this.orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }

        List<OrderItem> orderItemList = this.orderItemMapper.getOrderItemByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 搜索订单信息
     *
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageOrderSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        Order order = this.orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }

        List<OrderItem> orderItemList = this.orderItemMapper.getOrderItemByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 发货管理
     *
     * @param orderNo
     * @return
     */
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = this.orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }

        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {// 已付款
            Order orderItem = new Order();
            orderItem.setId(order.getId());
            orderItem.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());// 设置成已发货
            orderItem.setSendTime(new Date());

            this.orderMapper.updateByPrimaryKeySelective(orderItem);

        }

        return ServerResponse.createBySuccessMessage("发货成功");
    }

}
