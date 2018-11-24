package com.len.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.len.entity.ArticleList;
import com.len.entity.BlogArticle;
import com.len.service.ArticleCategoryService;
import com.len.service.BlogArticleService;
import com.len.service.BlogCategoryService;
import com.len.util.IpUtil;
import com.len.util.JsonUtil;
import com.len.util.ReType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Condition;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章管理（后台）
 *
 * @author JamesZBL
 * @email 154040976@qq.com
 * @date 2018-05-05
 */
@RestController
@RequestMapping("/blog")
public class ArticleController {

    @Autowired
    private BlogArticleService articleService;

    @Autowired
    private ArticleCategoryService articleCategoryService;

    @Autowired
    private BlogCategoryService categoryService;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @GetMapping("/articleList")
    public String articleListPage() {
        return "articleList";
    }


    @GetMapping("/header")
    public List<String> showArticles() {
        List<String> list = new ArrayList<>(4);
        list.add("java");
        list.add("架构");
        list.add("Linux");
        list.add("其他");
        return list;
    }

    /**
     * 获取 文章列表
     *
     * @param code
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/article/list")
    public ReType getList(String code, Integer page, Integer limit) {
        limit = limit > 100 ? 100 : limit;
        Page<Object> startPage = PageHelper.startPage(page, limit);

        List<BlogArticle> articles = articleService.selectArticle(code);

        List<ArticleList> articleLists = new ArrayList<>();
        articles.forEach(s -> articleLists.add(
                new ArticleList(s.getId(), s.getCode(), s.getTitle(),
                        s.getTopNum(), s.getCreateBy(),
                        format.format(s.getCreateDate()), s.getContent())
                )
        );
        return new ReType(startPage.getTotal(), startPage.getPageNum(), articleLists);
    }

    /**
     * 根据code获取文章内容
     *
     * @param code
     * @return
     */
    @GetMapping("/article/getDetail/{code}")
    public JsonUtil detail(@PathVariable("code") String code, HttpServletRequest request) {
        String ip = IpUtil.getIp(request);
        return articleService.detail(code, ip);
    }

    /**
     * 根据标签code获取文章
     *
     * @param tagName
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/article/list/{tagName}")
    public ReType getArticleByTag(@PathVariable("tagName") String tagName, Integer page, Integer limit) {
        limit = limit > 100 ? 100 : limit;
        Page<Object> startPage = PageHelper.startPage(page, limit);

        List<BlogArticle> articles = articleService.selectArticleByTag(tagName);
        return new ReType(startPage.getTotal(), startPage.getPageNum(), articles);
    }

    @GetMapping("/article/list/order/read")
    public ReType getArticleByReadNumber() {
        Condition condition = new Condition(BlogArticle.class);
        PageHelper.startPage(1, 5);
        condition.createCriteria();
        condition.orderBy("readNumber").desc();
        List<BlogArticle> articles = articleService.selectByExample(condition);
        articles.forEach(s -> {
            s.setContent(null);
        });
        ReType reType = new ReType();
        reType.setData(articles);
        return reType;
    }
}
