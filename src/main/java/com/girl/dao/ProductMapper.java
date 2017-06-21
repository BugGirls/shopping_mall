package com.girl.dao;

import com.girl.pojo.Product;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    // 获取商品列表
    List<Product> selectProductList();

    // 通过查询条件获取商品列表
    List<Product> selectByNameAndProductId(@Param(value = "productName") String productName, @Param(value = "productId") Integer productId);

    // 通过关键字和
    List<Product> selectByKeywordAndCategoryIds(@Param(value = "keyword") String keyword, @Param(value = "categoryIdList") List<Integer> categoryIdList);

}