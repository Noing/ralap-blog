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

import com.ralap.blog.business.annotation.BussinessLog;
import com.ralap.blog.business.entity.UserPwd;
import com.ralap.blog.business.service.SysUserService;
import com.ralap.blog.util.ResultUtil;
import com.ralap.blog.util.SessionUtil;
import com.ralap.blog.business.annotation.BussinessLog;
import com.ralap.blog.business.entity.UserPwd;
import com.ralap.blog.business.service.SysUserService;
import com.ralap.blog.framework.object.ResponseVO;
import com.ralap.blog.framework.property.AppProperties;
import com.ralap.blog.util.ResultUtil;
import com.ralap.blog.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 登录相关
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @website https://www.zhyd.me
 * @date 2018/4/24 14:37
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping(value = "/passport")
public class PassportController {

    @Autowired
    private AppProperties config;
    @Autowired
    private SysUserService userService;

    @BussinessLog("进入登录页面")
    @GetMapping("/login")
    public ModelAndView login(Model model) {
        model.addAttribute("enableKaptcha", config.getEnableKaptcha());
        return ResultUtil.view("/login");
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/signin")
    @ResponseBody
    public ResponseVO submitLogin(String username, String password, boolean rememberMe, String kaptcha) {
        if (config.getEnableKaptcha()) {
            if (StringUtils.isEmpty(kaptcha) || !kaptcha.equals(SessionUtil.getKaptcha())) {
                return ResultUtil.error("验证码错误！");
            }
            SessionUtil.removeKaptcha();
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        //获取当前的Subject
        Subject currentUser = SecurityUtils.getSubject();
        try {
            // 在调用了login方法后,SecurityManager会收到AuthenticationToken,并将其发送给已配置的Realm执行必须的认证检查
            // 每个Realm都能在必要时对提交的AuthenticationTokens作出反应
            // 所以这一步在调用login(token)方法时,它会走到xxRealm.doGetAuthenticationInfo()方法中,具体验证方式详见此方法
            currentUser.login(token);
            return ResultUtil.success("登录成功！");
        } catch (Exception e) {
            log.error("登录失败，用户名[{}]", username, e);
            token.clear();
            return ResultUtil.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @return
     */
    @PostMapping("/updatePwd")
    @ResponseBody
    public ResponseVO updatePwd(@Validated UserPwd userPwd, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return ResultUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        boolean result = userService.updatePwd(userPwd);
        SessionUtil.removeAllSession();
        return ResultUtil.success(result ? "密码已修改成功，请重新登录" : "密码修改失败");
    }

    /**
     * 使用权限管理工具进行用户的退出，跳出登录，给出提示信息
     *
     * @param redirectAttributes
     * @return
     */
    @GetMapping("/logout")
    public ModelAndView logout(RedirectAttributes redirectAttributes) {
        // http://www.oschina.net/question/99751_91561
        // 此处有坑： 退出登录，其实不用实现任何东西，只需要保留这个接口即可，也不可能通过下方的代码进行退出
        // SecurityUtils.getSubject().logout();
        // 因为退出操作是由Shiro控制的
        redirectAttributes.addFlashAttribute("message", "您已安全退出");
        return ResultUtil.redirect("index");
    }
}
