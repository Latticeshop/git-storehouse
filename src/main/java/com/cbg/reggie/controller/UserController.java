package com.cbg.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cbg.common.domain.R;
import com.cbg.common.utils.SMSUtils;
import com.cbg.common.utils.ValidateCodeUtils;
import com.cbg.reggie.domain.entity.User;
import com.cbg.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final SMSUtils smsUtils;

    public UserController(UserService userService, SMSUtils smsUtils) {
        this.userService = userService;
        this.smsUtils = smsUtils;
    }

    /**
     * 发送短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();

        if (!StringUtils.isEmpty(phone)) {
            //生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code:{}", code);
            //调用阿里云短信服务API完成发送短信
            smsUtils.sendMessage("瑞吉外卖", "13588882222", phone, code);

            //将生成的验证码保存到Session
            session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * map以"phone":13XXXXXXXXX和"code":XXXX来获取
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //验证码比对(页面提交的验证码:session保存的验证码)
        if (codeInSession.equals(code)) {
            //如果能比对成功，登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断当前手机号是否为新用户，是则自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);

                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }

    /**
     * 移动端退出
     *
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        //清理Session中保存的用户id
        request.getSession().removeAttribute("user");

        return R.success("退出成功");
    }
}
