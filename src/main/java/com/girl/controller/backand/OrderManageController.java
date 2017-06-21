package com.girl.controller.backand;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.User;
import com.girl.service.IOrderService;
import com.girl.service.IUserService;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * Created by girl on 2017/6/12.
 */
@Controller
@RequestMapping(value = "/manage/order/")
public class OrderManageController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IOrderService iOrderService;

    /**
     * 管理员获取所有订单列表信息
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，请先登录");
        }

        if (!this.iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iOrderService.manageOrderList(pageNum, pageSize);
    }

    /**
     * 管理员获取订单详情
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderDetail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，请先登录");
        }

        if (!this.iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iOrderService.manageOrderDetail(orderNo);
    }

    /**
     * 搜索订单信息
     *
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo,
                                                @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，请先登录");
        }

        if (!this.iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iOrderService.manageOrderSearch(orderNo, pageNum, pageSize);
    }

    /**
     * 发货管理
     *
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，请先登录");
        }

        if (!this.iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iOrderService.manageSendGoods(orderNo);
    }


}
