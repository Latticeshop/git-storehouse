package com.cbg.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cbg.common.domain.R;
import com.cbg.reggie.domain.dto.DishDto;
import com.cbg.reggie.domain.entity.Category;
import com.cbg.reggie.domain.entity.Dish;
import com.cbg.reggie.service.CategoryService;
import com.cbg.reggie.service.DishFlavorService;
import com.cbg.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    //自动装填set和get方法
    private final DishService dishService;

    private final DishFlavorService dishFlavorService;

    private final CategoryService categoryService;

    public DishController(DishService dishService, DishFlavorService dishFlavorService, CategoryService categoryService) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
        this.categoryService = categoryService;
    }

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝 ignoreProperties为忽视对应属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<DishDto> list = getDishDtos(pageInfo);

        //Category category = categoryService.listByIds(categoryIds);

        dishDtoPage.setRecords(list);


        return R.success(dishDtoPage);
//        return R.success(pageInfo);
    }

    private List<DishDto> getDishDtos(Page<Dish> pageInfo) {
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //拷贝到dishDto
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();

            //根据id查分类对象(黑马史山代码，有时间优化)
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * 根据id查询对应菜品信息和口味信息(回显)
     * PathVariable从url路径上获取值
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * PutMapping 更新请求
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 根据条件查询对应菜品数据
     *
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        //添加排序条件，查询状态为1(起售状态)菜品
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        queryWrapper.eq(Dish::getStatus, 1);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加排序条件，查询状态为1(起售状态)菜品
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        queryWrapper.eq(Dish::getStatus, 1);
        //封装进list
        List<Dish> list = dishService.list(queryWrapper);

        //通过stream流取得list也就是菜品id，并封装进ids
        List<Long> ids = list.stream().map(Dish::getId).collect(Collectors.toList());
//      同理如下：
//        List<Dish> list = dishService.list(queryWrapper);
//        List<Long> ids = list.stream().map((item) -> {
//            Long id = item.getId();
//            return id;
//        }).collect(Collectors.toList());

        //调用getByIdsWithFlavor设置口味
        List<DishDto> dishDtoList = dishService.getByIdsWithFlavor(ids);

        return R.success(dishDtoList);
    }
}
