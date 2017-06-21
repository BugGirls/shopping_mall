package com.girl.dao;

import com.girl.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    // 通过orderNo和userId获取订单明细列表
    List<OrderItem> getOrderItemByOrderNoAndUserId(@Param(value = "orderNo") Long orderNo, @Param(value = "userId") Integer userId);

    // 批量插入
    void batchInsert(@Param(value = "orderItemList") List<OrderItem> orderItemList);

    // 通过orderNo获取订单明细列表
    List<OrderItem> getOrderItemByOrderNo(Long orderNo);

}