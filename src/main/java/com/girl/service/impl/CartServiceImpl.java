package com.girl.service.impl;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.dao.CartMapper;
import com.girl.dao.ProductMapper;
import com.girl.pojo.Cart;
import com.girl.pojo.Product;
import com.girl.service.ICartService;
import com.girl.util.BigDecimalUtil;
import com.girl.util.PropertiesUtil;
import com.girl.vo.CartProductVo;
import com.girl.vo.CartVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by girl on 2017/6/5.
 */
@Service
public class CartServiceImpl implements ICartService {

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 购物车添加商品
     *
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        Cart cart = this.cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null) {// 如果为空，说明该商品不在购物车里，需要往购物车中添加该商品
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);// 默认购物车为选中状态
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);

            this.cartMapper.insert(cartItem);
        } else {// 表示产品已经在购物车中，需要更新商品的数量
            count = cart.getQuantity() + count;// 数量相加
            cart.setQuantity(count);

            this.cartMapper.updateByPrimaryKeySelective(cart);
        }

        return this.list(userId);
    }

    // 从数据库中重新获取当前登录用户的购物车列表
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();

        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");// 存放所有购物车商品的总价

        // 获取当前登录用户的所有购物车列表
        List<Cart> cartList = this.cartMapper.selectCartByUserId(userId);
        if (CollectionUtils.isNotEmpty(cartList)) {
            // 遍历购物车填充CartProductVo
            for (Cart cartItem: cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = this.productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductSubTitle(product.getSubtitle());
                    cartProductVo.setProductStock(product.getStock());
                    // 判断库存
                    int buyLimitCount = 0;// 存放购买商品的最大值
                    if (product.getStock() >= cartItem.getQuantity()) {// 库存大于购买商品的数量
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {// 库存小于购买商品的数量，需要限制购买的数量
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        // 购物车中更新有效商品数量
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);

                        this.cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);

                    // 计算当前购物车中商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                // 如果该购物车已经勾选，则增加到整个购物车总价中
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }

                cartProductVoList.add(cartProductVo);
            }
        }

        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    // 判断当前登录用户的购物车是否为全选状态
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }

        return this.cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

    /**
     * 更新购物车中商品的数量
     *
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        Cart cart = this.cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);

            this.cartMapper.updateByPrimaryKeySelective(cart);
        }

        return this.list(userId);
    }

    /**
     * 通过id删除购物车中的商品
     *
     * @param userId
     * @param productIds
     * @return
     */
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        // 通过","分割成List数组并添加到集合当中
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }

        this.cartMapper.deleteByUserIdAndProductIds(userId, productList);

        return this.list(userId);
    }

    /**
     * 获取购物车列表
     *
     * @param userId
     * @return
     */
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 选择或者反选
     *
     * @param userId
     * @param checked
     * @return
     */
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer checked, Integer productId) {
        this.cartMapper.checkedOrUnCheckedProduct(userId, checked, productId);

        return this.list(userId);
    }

    /**
     * 获取当前用户的所有购物车中所有商品的数量
     *
     * @param userId
     * @return
     */
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }

        return ServerResponse.createBySuccess(this.cartMapper.selectCartProductCount(userId));
    }

}
