package com.girl.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by girl on 2017/6/12.
 */
public class OrderItemVo {

    private Long orderNo;// 订单编号

    private Integer productId;// 商品ID

    private String productName;// 商品名称

    private String productImage;// 商品主图

    private BigDecimal currentUnitPrice;// 生成订单时的商品单价

    private Integer quantity;// 商品数量

    private BigDecimal totalPrice;// 商品总价

    private String createTime;// 创建时间

    public Long getOrderNo(){
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage == null ? null : productImage.trim();
    }

    public BigDecimal getCurrentUnitPrice() {
        return currentUnitPrice;
    }

    public void setCurrentUnitPrice(BigDecimal currentUnitPrice) {
        this.currentUnitPrice = currentUnitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
