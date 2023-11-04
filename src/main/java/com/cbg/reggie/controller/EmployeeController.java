package com.cbg.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cbg.common.domain.R;
import com.cbg.reggie.domain.entity.Employee;
import com.cbg.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
//设置请求路径
@RequestMapping("/employee")
public class EmployeeController {
    //@Autowired 错误的！当一个接口有多个实现类时会报错，详情看https://www.fengnayun.com/news/content/189945.html
    private final EmployeeService employeeService;

    //可以用@Qualifier("实现类的名字")来指定实现类
    public EmployeeController(@Qualifier("employeeServiceImpl") EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    //前端的Post请求 @RequestBody将JSON格式转化成Employee类 request存储post的session
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1,将密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2,根据用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //Employee::getUsername相当于创建Employee对象并调用getUsername方法
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        //getOne验证unique唯一类型数据
//        Employee emp = employeeService.getOne(queryWrapper.eq(Employee::getUsername, employee.getUsername()).last("limit 1"));
        Employee emp = employeeService.getOne(queryWrapper);

        //3,没有查到返回登录失败
        if (null == emp) {
            return R.error("登录失败");
        }

        //4,密码的比对，不一致返回登录失败
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5,查看员工状态status，如果为禁用状态，返回已禁用
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6,登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 员工退出按钮
     * 退出不需要返回数据，用string表示就行
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工 @RequestBody 是让JSON格式识别
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码123456，但是用md5加密处理,getBytes()把字符串转数组
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //创建和更新时间
//        LocalDateTime nowDate = LocalDateTime.now();
//        employee.setCreateTime(nowDate);
//        employee.setUpdateTime(nowDate);
        //创建的账号和更新账号
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * 因为是get通过url传递的参数，不是JSON类型，所以变量不加@RequestBody
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page=" + page + ",pageSize=" + pageSize + ",name=" + name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件name构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件，空值不执行
        //不用if(name==null)是避免语法错误，同时判断字符串长度
        //queryWrapper.like(Employee::getName, name);
        queryWrapper.like(StringUtils.hasText(name), Employee::getName, name);
        //添加排序条件，按更新时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询,IService提供的分页查询page方法
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为:" + id);
        //通过employee对象和Id修改
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * PathVariable从url路径上获取值
     * http://localhost:8080/getbyid/1可以直接获取id=1
     * RequestParam从request里取值
     * http://localhost:8080/getbyid?id=2可以直接获取id=2
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null)
            return R.success(employee);

        return R.error("没有查询到对应员工信息");
    }
}
