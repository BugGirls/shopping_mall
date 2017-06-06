package com.girl.vo;

import java.math.BigDecimal;

/**
 * 结合了商品和购物车的一个抽象对象
 *
 * Created by girl on 2017/6/5.
 */
public class CartProductVo {

    private Integer id;// 购物车ID
    private Integer userId;// 用户ID
    private Integer productId;// 商品ID
    private Integer quantity;// 购物车中此商品的数量
    private String productName;// 商品的名称
    private String productSubTitle;// 商品的副标题
    private String productMainImage;// 商品的主图
    private BigDecimal productPrice;// 商品的价格
    private Integer productStatus;// 商品的状态
    private BigDecimal productTotalPrice;// 商品的总价
    private Integer productStock;// 商品的库存
    private Integer productChecked;// 此商品是否勾选

    private String limitQuantity;// 限制数量的返回结果：当前台传入的商品的数量大于该商品库存数量，则返回错误提示LIMIT_NUM_FAIL，并把商品数量设置为库存数量

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSubTitle() {
        return productSubTitle;
    }

    public void setProductSubTitle(String productSubTitle) {
        this.productSubTitle = productSubTitle;
    }

    public String getProductMainImage() {
        return productMainImage;
    }

    public void setProductMainImage(String productMainImage) {
        this.productMainImage = productMainImage;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(Integer productStatus) {
        this.productStatus = productStatus;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public Integer getProductStock() {
        return productStock;
    }

    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }

    public Integer getProductChecked() {
        return productChecked;
    }

    public void setProductChecked(Integer productChecked) {
        this.productChecked = productChecked;
    }

    public String getLimitQuantity() {
        return limitQuantity;
    }

    public void setLimitQuantity(String limitQuantity) {
        this.limitQuantity = limitQuantity;
    }
}
