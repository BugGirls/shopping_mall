package com.girl.controller.portal;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.User;
import com.girl.service.ICartService;
import com.girl.vo.CartVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 购物车-前台管理
 *
 * Created by girl on 2017/6/5.
 */
@Controller
@RequestMapping(value = "/cart/")
public class CartController {

    @Resource
    private ICartService iCartService;

    /**
     * 购物车添加商品
     *
     * @param session
     * @param count：商品数量
     * @param productId：商品id
     * @return
     */
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.add(user.getId(), productId, count);
    }

    /**
     * 更新购物车中商品的数量
     *
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.update(user.getId(), productId, count);
    }

    /**
     * 通过id删除购物车中的商品
     *
     * @param session
     * @param productIds：删除多个产品的id值，以","分割
     * @return
     */
    @RequestMapping(value = "delete_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session, String productIds) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.delete(user.getId(), productIds);
    }

    /**
     * 获取当前登录用户的购物车信息
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.list(user.getId());
    }

    /**
     * 修改当前登录用户商品的选择状态：全选
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.selectOrUnSelect(user.getId(), Const.Cart.CHECKED, null);
    }

    /**
     * 修改当前登录用户商品的选择状态：全反选
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "un_select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.selectOrUnSelect(user.getId(), Const.Cart.UN_CHECKED, null);
    }

    /**
     * 修改当前登录用户商品的选择状态：单独选
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "select_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectProduct(HttpSession session, Integer productId) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.selectOrUnSelect(user.getId(), Const.Cart.CHECKED, productId);
    }

    /**
     * 修改当前登录用户商品的选择状态：单独反选
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "un_select_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectProduct(HttpSession session, Integer productId) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录");
        }

        return this.iCartService.selectOrUnSelect(user.getId(), Const.Cart.UN_CHECKED, productId);
    }

    /**
     * 获取当前用户的所有购物车中所有商品的数量
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "get_cart_product_count.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session) {
        // 判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createBySuccess(0);
        }

        return this.iCartService.getCartProductCount(user.getId());
    }


}
