package com.girl.dao;

import com.girl.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    // 通过用户id和商品id查询一个购物车
    Cart selectCartByUserIdAndProductId(@Param(value = "userId") Integer userId, @Param(value = "productId") Integer productId);

    // 获取当前登录用户的所有购物车信息
    List<Cart> selectCartByUserId(Integer userId);

    // 查看当前登录用户是否存在未勾选的购物车
    int selectCartProductCheckedStatusByUserId(Integer userId);

    // 删除当前登录用户下的商品
    int deleteByUserIdAndProductIds(@Param(value = "userId") Integer userId, @Param(value = "productIdList") List<String> productIdList);

    // 修改当前登录用户商品的选择状态：全选或全反选
    int checkedOrUnCheckedProduct(@Param(value = "userId") Integer userId, @Param(value = "checked") Integer checked, @Param(value = "productId") Integer productId);

    // 获取当前登录用户所有购物车中的所有商品数量
    int selectCartProductCount(Integer userId);

    // 获取已勾选的商品
    List<Cart> selectCheckedCartByUserId(Integer userId);
}