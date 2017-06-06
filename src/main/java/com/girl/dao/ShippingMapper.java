package com.girl.dao;

import com.girl.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    // 通过id删除当前登录用户的收货地址
    int deleteShippingByUserIdAndShippingId(@Param(value = "userId") Integer userId, @Param(value = "shippingId") Integer shippingId);

    // 通过id更新当前登录用户的收货地址
    int updateShippingByUserIdAndShipping(Shipping shipping);

    // 通过id查询当前登录用户的收货地址
    Shipping selectShippingByUserIdAndShippingId(@Param(value = "userId") Integer userId, @Param(value = "shippingId") Integer shippingId);

    // 获取当前登录用户的收货地址列表
    List<Shipping> selectShippingListByUserId(Integer userId);
}