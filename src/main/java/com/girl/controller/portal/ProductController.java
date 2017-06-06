package com.girl.controller.portal;

import com.girl.common.ServerResponse;
import com.girl.service.IProductService;
import com.girl.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 前台-商品管理
 *
 * Created by girl on 2017/6/4.
 */
@Controller
@RequestMapping(value = "/product/")
public class ProductController {

    @Resource
    private IProductService iProductService;

    /**
     * 获取商品详情
     *
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return this.iProductService.getProductDetail(productId);
    }

    /**
     * 通过关键字、商品分类id搜索商品列表
     *
     * @param keyword
     * @param categoryId
     * @param orderBy
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                         @RequestParam(value = "orderBy", defaultValue = "") String orderBy,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        return this.iProductService.getProductByKeywordCategory(keyword, categoryId, orderBy, pageNum, pageSize);
    }

}
