package com.girl.controller.portal;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.Shipping;
import com.girl.pojo.User;
import com.girl.service.IShippingService;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * Created by girl on 2017/6/6.
 */
@Controller
@RequestMapping(value = "/shipping/")
public class ShippingController {

    @Resource
    private IShippingService iShippingService;


    /**
     * 添加一个收货地址
     *
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录。。。");
        }

        return this.iShippingService.addOrUpdate(user.getId(), shipping);
    }

    /**
     * 删除一个收货地址
     *
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "delete.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse delete(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录。。。");
        }

        return this.iShippingService.delete(user.getId(), shippingId);
    }

    /**
     * 修改收货地址
     *
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录。。。");
        }

        return this.iShippingService.addOrUpdate(user.getId(), shipping);
    }

    /**
     * 查询一个收货地址
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "select_one_shipping.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse selectOneShipping(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录。。。");
        }

        return this.iShippingService.selectOneShipping(user.getId(), shippingId);
    }

    /**
     * 查询收货地址列表
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，请先登录。。。");
        }

        return this.iShippingService.list(pageNum, pageSize, user.getId());
    }

}
