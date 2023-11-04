package com.cbg.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbg.reggie.domain.dto.DishDto;
import com.cbg.reggie.domain.entity.Dish;
import com.cbg.reggie.domain.entity.DishFlavor;
import com.cbg.reggie.mapper.DishMapper;
import com.cbg.reggie.service.DishFlavorService;
import com.cbg.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishFlavorService dishFlavorService;

    public DishServiceImpl(DishFlavorService dishFlavorService) {
        this.dishFlavorService = dishFlavorService;
    }

    /**
     * 新增菜品，保存对应口味数据
     * Transactional声明式事务管理建立在AOP之上的。
     * 其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。
     * 简而言之，@Transactional注解在代码执行出错的时候能够进行事务的回滚。
     *
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表
        this.save(dishDto);

        //dish_flavor里的dishId菜品id在dishDto没有封装，dishId在雪花算法后自动赋予，所以在save后获取
        Long dishId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //可以使用flavors。forEach()循环便利。这里使用stream流
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品风味表 saveBatch批量保存集合
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        //拷贝风味到dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查
        //根据id 两张表联合查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 根据ids查询菜品信息和口味信息
     *
     * @param ids
     * @return
     */
    @Transactional
    public List<DishDto> getByIdsWithFlavor(List<Long> ids) {
        //查询菜品基本信息
        List<Dish> dishes = this.listByIds(ids);
        //拷贝风味到dishDtoList
        List<DishDto> dishDtoList = dishes.stream().map((dish) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        //查询当前菜品对应的口味信息，从dish_flavor表查
        //根据id 两张表联合查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //循环比对id赋值 因为dishDtoList是地址，访问地址直接修改数据
        dishDtoList.forEach((item) -> {
            item.setFlavors(
                    flavors.stream().filter(
                            flavor -> flavor.getDishId().equals(item.getId())
                    ).collect(Collectors.toList())
            );
        });

        return dishDtoList;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息 自动向下转型
        this.updateById(dishDto);

        //清理当前菜品口味数据--delete
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加提交过来的菜品口味数据--insert
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
