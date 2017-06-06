package com.girl.controller.backand;

import com.girl.common.Const;
import com.girl.common.ResponseCode;
import com.girl.common.ServerResponse;
import com.girl.pojo.Product;
import com.girl.pojo.User;
import com.girl.service.IFileService;
import com.girl.service.IProductService;
import com.girl.service.IUserService;
import com.girl.util.PropertiesUtil;
import com.girl.vo.ProductDetailVo;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 后台-商品管理
 *
 * Created by girl on 2017/6/2.
 */
@Controller
@RequestMapping(value = "/manage/product/")
public class ProductManageController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IProductService iProductService;

    @Resource
    private IFileService iFileService;


    /**
     * 管理员添加一个商品
     *
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "product_save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        // 执行添加操作
        return this.iProductService.saveOrUpdateProduct(product);
    }

    /**
     * 设置商品上下架，即修改商品销售状态
     *
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        // 执行修改操作
        return this.iProductService.setSaleStatus(productId, status);
    }


    /**
     * 获取商品详情
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "get_product_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(HttpSession session, Integer productId) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iProductService.manageProductDetails(productId);
    }

    /**
     * 获取商品列表
     *
     * @param session
     * @param pageNum：当前页
     * @param pageSize：每页显示多少条数据
     * @return
     */
    @RequestMapping(value = "get_product_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getProductList(HttpSession session,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        return this.iProductService.getProductList(pageNum, pageSize);
    }

    /**
     * 商品搜索
     *
     * @param session
     * @param productId
     * @param productName
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "product_search.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, Integer productId, String productName,
                                        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        // 执行搜索操作
        return this.iProductService.productSearch(productId, productName, pageNum, pageSize);
    }

    /**
     * 商品图片上传
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpSession session, HttpServletRequest request,
                                 @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile) {
        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "您还没有登录，需要强制登录status=10");
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            return ServerResponse.createByErrorMessage("您不是管理员，没有权限");
        }

        // 获取上传路径
        String path = request.getSession().getServletContext().getRealPath("upload");

        // 执行上传
        String targetFileName = this.iFileService.upload(multipartFile, path);
        if (StringUtils.isBlank(targetFileName)) {
            return ServerResponse.createByErrorMessage("上传失败");
        }

        // 获取上传到ftp服务器上的路径
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

        // 将上传信息放入Map中
        Map fileMap = Maps.newHashMap();
        fileMap.put("uri", targetFileName);
        fileMap.put("url", url);

        return ServerResponse.createBySuccess(fileMap);
    }

    /**
     * 富文本上传图片
     *
     * @param session
     * @param request
     * @param multipartFile
     * @return
     */
    @RequestMapping(value = "rich_text_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextImgUpload(HttpSession session, HttpServletRequest request,
                                 @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile,
                                 HttpServletResponse response) {
        // 富文本中对于返回值有自己的要求，我们使用的是simditor，所以要按照simditor的要求进行返回
        // 返回格式：
        // {
        //      "success":true/false,
        //      "msg":"error message",
        //      "file_path":"{real file path}"
        // }

        Map resultMap = Maps.newHashMap();

        // 验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "管理员没有登录");

            return resultMap;
        }

        // 验证登录的是不是管理员
        if (!iUserService.checkAdminRole(user).isSuccess()) {
            resultMap.put("success", false);
            resultMap.put("msg", "您不是管理员，没有权限");

            return resultMap;
        }

        // 获取上传路径
        String path = request.getSession().getServletContext().getRealPath("upload");

        // 执行上传
        String targetFileName = this.iFileService.upload(multipartFile, path);
        if (StringUtils.isBlank(targetFileName)) {
            resultMap.put("success", false);
            resultMap.put("msg", "上传失败");

            return resultMap;
        }

        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

        resultMap.put("success", true);
        resultMap.put("msg", "上传成功");
        resultMap.put("file_path", url);

        response.addHeader("Access-Control-Allow-Headers", "X-File-Name");

        return resultMap;
    }


}
