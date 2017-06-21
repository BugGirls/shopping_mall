package com.girl.dao;

import com.girl.pojo.Order;
import com.girl.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    // 通过userId和orderNo验证该订单是否存在
    Order selectByUserIdAndOrderNo(@Param(value = "userId") Integer userId, @Param(value = "orderNo") Long orderNo);

    // 通过orderNo查询一个订单
    Order selectByOrderNo(Long orderNo);

    // 通过userId获取当前登录用户的订单列表信息
    List<Order> getOrderListByUserId(Integer userId);

    // 获取所有的订单列表信息
    List<Order> getOrderList();
}