/**
 * MIT License
 * Copyright (c) 2018 yadong.zhang
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ralap.blog.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ralap.blog.business.entity.Type;
import com.ralap.blog.business.enums.ResponseStatus;
import com.ralap.blog.business.service.BizTypeService;
import com.ralap.blog.business.vo.TypeConditionVO;
import com.ralap.blog.util.ResultUtil;
import com.ralap.blog.business.entity.Type;
import com.ralap.blog.business.enums.ResponseStatus;
import com.ralap.blog.business.service.BizTypeService;
import com.ralap.blog.business.vo.TypeConditionVO;
import com.ralap.blog.framework.object.PageResult;
import com.ralap.blog.framework.object.ResponseVO;
import com.ralap.blog.util.ResultUtil;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章类型管理
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @website https://www.zhyd.me
 * @date 2018/4/24 14:37
 * @since 1.0
 */
@RestController
@RequestMapping("/type")
public class RestTypeController {

    @Autowired
    private BizTypeService typeService;

    @RequiresPermissions("types")
    @PostMapping("/list")
    public PageResult list(TypeConditionVO vo) {
        PageInfo<Type> pageInfo = typeService.findPageBreakByCondition(vo);
        return ResultUtil.tablePage(pageInfo);
    }

    @RequiresPermissions("type:add")
    @PostMapping(value = "/add")
    public ResponseVO add(Type type) {
        typeService.insert(type);
        return ResultUtil.success("文章类型添加成功！新类型 - " + type.getName());
    }

    @RequiresPermissions(value = {"type:batchDelete", "type:delete"}, logical = Logical.OR)
    @PostMapping(value = "/remove")
    public ResponseVO remove(Long[] ids) {
        if (null == ids) {
            return ResultUtil.error(500, "请至少选择一条记录");
        }
        for (Long id : ids) {
            typeService.removeByPrimaryKey(id);
        }
        return ResultUtil.success("成功删除 [" + ids.length + "] 个文章类型");
    }

    @RequiresPermissions("type:get")
    @PostMapping("/get/{id}")
    public ResponseVO get(@PathVariable Long id) {
        return ResultUtil.success(null, this.typeService.getByPrimaryKey(id));
    }

    @RequiresPermissions("type:edit")
    @PostMapping("/edit")
    public ResponseVO edit(Type type) {
        try {
            typeService.updateSelective(type);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("文章类型修改失败！");
        }
        return ResultUtil.success(ResponseStatus.SUCCESS);
    }

    @PostMapping("/listAll")
    public ResponseVO listType() {
        return ResultUtil.success(null, typeService.listAll());
    }

    @PostMapping("/listParent")
    public ResponseVO listParent() {
        return ResultUtil.success(null, typeService.listParent());
    }

}
