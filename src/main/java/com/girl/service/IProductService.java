package com.girl.service;

import com.girl.common.ServerResponse;
import com.girl.pojo.Product;
import com.girl.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

/**
 * Created by girl on 2017/6/2.
 */
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse manageProductDetails(Integer productId);

    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);

    ServerResponse<PageInfo> productSearch(Integer productId, String productName, int pageNum, int pageSize);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize);

}
