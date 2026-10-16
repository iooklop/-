package com.resourceshare.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端使用 Vue Router 的 history 模式，刷新任意前端路由时回退到 index.html，
 * 交给前端路由处理。带后缀的静态资源（.js/.css/.png 等）不会被这里拦截。
 */
@Controller
public class SpaController {

    @GetMapping(value = {"/", "/{path:[^.]*}", "/**/{path:[^.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
