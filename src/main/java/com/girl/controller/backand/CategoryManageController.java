package com.girl.controller.backand;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.Category;
import com.girl.pojo.User;
import com.girl.service.ICategoryService;
import com.girl.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.net.ServerSocket;
import java.util.List;

/**
 * 商品类别
 *
 * Created by girl on 2017/6/1.
 */
@Controller
@RequestMapping(value = "/manage/category/")
public class CategoryManageController {

    @Resource
    private IUserService iUserService;

    @Resource
    private ICategoryService iCategoryService;

    /**
     * 管理员添加商品类别
     *
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }

        // 校验是否为管理员登录
        ServerResponse response = this.iUserService.checkAdminRole(user);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("登录的不是管理员，没有权限");
        }

        // 登录的是管理员，增加分类处理的逻辑
        return this.iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 更新商品类别名称
     *
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "update_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        System.out.println(categoryName + "//////////////////////////////");
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }

        // 校验是否为管理员登录
        ServerResponse response = this.iUserService.checkAdminRole(user);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("登录的不是管理员，没有权限");
        }

        // 修改商品类别名称
        return this.iCategoryService.updateCategoryName(categoryId, categoryName);
    }

    /**
     * 通过ID获取该节点的平级节点的信息
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_children_parallel_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }

        // 校验是否为管理员登录
        ServerResponse response = this.iUserService.checkAdminRole(user);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("登录的不是管理员，没有权限");
        }

        // 查询子节点的商品类别信息，并且不递归，保持平级
       return this.iCategoryService.getChildrenParallelCategory(categoryId);
    }

    /**
     * 通过id递归获取本节点和该节点下所有子节点信息
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_children_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // 判断是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }

        // 校验是否为管理员登录
        ServerResponse response = this.iUserService.checkAdminRole(user);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("登录的不是管理员，没有权限");
        }

        return this.iCategoryService.getCategoryAndDeepChildrenCategory(categoryId);
    }

}
