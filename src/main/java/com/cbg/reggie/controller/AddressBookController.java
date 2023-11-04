package com.cbg.reggie.controller;

import com.cbg.common.domain.R;
import com.cbg.reggie.domain.entity.AddressBook;
import com.cbg.reggie.service.AddressBookService;
import com.cbg.web.config.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    /**
     * 新增
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        addressBookService.save(addressBook);

        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);

        addressBookService.setDefault(addressBook);

        return R.success(addressBook);
    }

    /**
     * 根据id修改地址
     * (自己写的)
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);

        //SQL:update address_book set user_id='..',phone='..' where id = ?
        addressBookService.updateById(addressBook);

        return R.success("修改成功");
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {

        AddressBook addressBook = addressBookService.getDefault();

        return null == addressBook ? R.error("没有找到该对象") : R.success(addressBook);

    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        List<AddressBook> list = addressBookService.byList(addressBook);

        return R.success(list);
    }

    /**
     * 删除地址
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("地址id：{}", ids);

        addressBookService.remove(ids);

        return R.success("地址删除成功");
    }
}
