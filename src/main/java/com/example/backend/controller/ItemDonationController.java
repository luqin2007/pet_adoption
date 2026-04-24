package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.Category;
import com.example.backend.service.ItemDonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 物资与捐赠管理模块<br>
 * - 物资管理<br>
 * ---- 准备物资捐赠 beginDonation ( √ × )<br>
 * ---- 上传捐赠影像 uploadDonation ( √ × )<br>
 * ---- 删除捐赠影像 deleteDonation ( √ × )<br>
 * ---- 确认物资捐赠 addDonation ( √ × )<br>
 * ---- 修改捐赠信息 updateDonation ( √ × )<br>
 * ---- 修改捐赠状态 updateDonationStatus ( √ × )<br>
 * ---- 获取捐赠信息 getDonation ( √ × )<br>
 * ---- 查询捐赠信息 getDonations ( √ × )<br>
 * ---- 创建物资信息 addItem ( √ × )<br>
 * ---- 修改物资信息 updateItem ( √ × )<br>
 * ---- 获取物资信息 getItem ( √ × )<br>
 * ---- 查询物资信息 getItems ( √ × )<br>
 * ---- 删除物资信息 discardItem ( √ × )<br>
 * - 使用跟踪<br>
 * ---- 申请使用物资 addUsage ( √ × )<br>
 * ---- 修改使用记录 updateUsage ( √ × )<br>
 * ---- 获取使用记录 getUsage ( √ × )<br>
 * ---- 查询使用记录 getUsages ( √ × )<br>
 * - 物资分类<br>
 * ---- 创建物资分类 addCategory ( √ × )<br>
 * ---- 修改物资分类 updateCategory ( √ × )<br>
 * ---- 删除物资分类 discardCategory ( √ × )<br>
 * ---- 获取物资分类 getCategory ( √ × )<br>
 * ---- 查询物资分类 getCategories ( √ × )<br>
 * - 库存管理<br>
 * ---- 入库出库登记 addStockRecord ( √ × )<br>
 * ---- 获取库存记录 getStock ( √ × )<br>
 * ---- 查询库存物品 getStocks ( √ × )<br>
 * ---- 查询库存记录 getStockRecords ( √ × )<br>
 * - 库存预警<br>
 * ---- 添加预警 addSubscribe ( √ × )<br>
 * ---- 获取预警 getSubscribe ( √ × )<br>
 * ---- 查询预警 getSubscribes ( √ × )<br>
 * ---- 取消预警 cancelSubscribe ( √ × )<br>
 */
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemDonationController {

    private final ItemDonationService itemDonationService;

    /**
     * 准备物资捐赠
     */
    @PutMapping("/donations")
    public Result<String> addDonation() {
        return Result.success(itemDonationService.beginDonation());
    }

    /**
     * 上传捐赠影像
     */
    @PostMapping("/donations/upload/{_id}")
    public Result<String> uploadDonation(@PathVariable("_id") String uuid,
                                         @RequestParam("file") MultipartFile request) {
        return Result.success(itemDonationService.uploadDonationFile(uuid, request));
    }

    /**
     * 删除捐赠影像
     */
    @DeleteMapping("/donations/upload/{_id}/{name}")
    public Result<Void> deleteDonation(@PathVariable("_id") String uuid, @PathVariable("name") String filename) {
        itemDonationService.deleteDonationFile(uuid, filename);
        return Result.success();
    }

    /**
     * 确认物资捐赠
     */
    @PostMapping("/donations")
    public Result<DonationResponse> addDonation(@Valid @RequestBody DonationAddRequest request) {
        return Result.success(itemDonationService.addDonation(request));
    }

    /**
     * 修改捐赠信息
     */
    @PutMapping("/donations/{id}")
    public Result<DonationResponse> updateDonation(@PathVariable("id") Long donationId,
                                                   @Valid @RequestBody DonationUpdateRequest request) {
        return Result.success(itemDonationService.updateDonation(donationId, request));
    }

    /**
     * 修改捐赠状态
     */
    @PatchMapping("/donations/{id}/status")
    public Result<DonationResponse> updateDonationStatus(@PathVariable("id") Long donationId,
                                                         @Valid @RequestBody DonationStatusUpdateRequest status) {
        return Result.success(itemDonationService.updateDonationStatus(donationId, status));
    }

    /**
     * 获取捐赠信息
     */
    @GetMapping("/donations/{id}")
    public Result<DonationResponse> getDonation(@PathVariable("id") Long donationId) {
        return Result.success(itemDonationService.getDonation(donationId));
    }

    /**
     * 查询捐赠信息
     */
    @GetMapping("/donations")
    public Result<Page<DonationResponse>> getDonations(@Valid DonationQueryParams params, PageParams page) {
        return Result.success(itemDonationService.getDonations(params, page));
    }

    /**
     * 创建物资信息
     */
    @PostMapping("/items")
    public Result<ItemResponse> addItem(@Valid @RequestBody ItemAddRequest request) {
        return Result.success(itemDonationService.addItem(request));
    }

    /**
     * 修改物资信息
     */
    @PutMapping("/items/{id}")
    public Result<ItemResponse> updateItem(@PathVariable("id") Long itemId, @Valid @RequestBody ItemUpdateRequest request) {
        return Result.success(itemDonationService.updateItem(itemId, request));
    }

    /**
     * 获取物资详情
     */
    @GetMapping("/items/{id}")
    public Result<ItemResponse> getItem(@PathVariable("id") Long itemId) {
        return Result.success(itemDonationService.getItem(itemId));
    }

    /**
     * 查询物资信息
     */
    @GetMapping("/items")
    public Result<Page<ItemResponse>> getItems(@Valid ItemQueryParams params, PageParams page) {
        return Result.success(itemDonationService.getItems(params, page));
    }

    /**
     * 删除物资信息
     */
    @DeleteMapping("/items/{id}")
    public Result<Void> discardItems(@PathVariable("id") Long itemId) {
        itemDonationService.discardItem(itemId);
        return Result.success();
    }

    /**
     * 创建物资分类
     */
    @PostMapping("/categories")
    public Result<Category> addCategory(@Valid @RequestBody CategoryAddRequest request) {
        return Result.success(itemDonationService.addCategory(request));
    }

    /**
     * 修改物资分类
     */
    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable("id") Long categoryId,
                                           @Valid @RequestBody CategoryUpdateRequest request) {
        return Result.success(itemDonationService.updateCategory(categoryId, request));
    }

    /**
     * 删除物资分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> discardCategory(@PathVariable("id") Long categoryId) {
        itemDonationService.discardCategory(categoryId);
        return Result.success();
    }

    /**
     * 获取物资分类
     */
    @GetMapping("/categories/{id}")
    public Result<Category> getCategory(@PathVariable("id") Long categoryId) {
        return Result.success(itemDonationService.getCategory(categoryId));
    }

    /**
     * 查询物资分类
     */
    @GetMapping("/categories")
    public Result<Page<Category>> getCategories(String name, PageParams page) {
        Page<Category> categories = itemDonationService.getCategories(name, page);
        return Result.success(categories);
    }

    /**
     * 入库出库登记
     */
    @PostMapping("/stocks")
    public Result<StockResponse> addStockRecord(@Valid @RequestBody StockRequest request) {
        return Result.success(itemDonationService.addStockRecord(request));
    }

    /**
     * 获取库存记录
     */
    @GetMapping("/stocks/{id}")
    public Result<StockResponse> getStock(@PathVariable("id") Long stockId) {
        return Result.success(itemDonationService.getStock(stockId));
    }

    /**
     * 查询库存物品
     */
    @GetMapping("/stocks")
    public Result<Page<StockResponse>> getStocks(@Valid StockQueryParams params, PageParams page) {
        return Result.success(itemDonationService.getStocks(params, page));
    }

    /**
     * 查询库存记录
     */
    @GetMapping("/records")
    public Result<Page<StockRecordResponse>> getStockRecords(@Valid StockRecordQueryParams params, PageParams page) {
        return Result.success(itemDonationService.getStockRecords(params, page));
    }

    /**
     * 添加预警
     */
    @PostMapping("/subscribe")
    public Result<SubscribeResponse> addSubscribe(@Valid @RequestBody SubscribeAddRequest request) {
        return Result.success(itemDonationService.addSubscribe(request));
    }

    /**
     * 获取预警
     */
    @GetMapping("/subscribe/{id}")
    public Result<SubscribeResponse> getSubscribe(@PathVariable("id") Long subscribeId) {
        return Result.success(itemDonationService.getSubscribe(subscribeId));
    }

    /**
     * 查询预警
     */
    @GetMapping("/subscribe")
    public Result<Page<SubscribeResponse>> getSubscribes(@Valid SubscribeQueryParams params, PageParams page) {
        return Result.success(itemDonationService.getSubscribes(params, page));
    }

    /**
     * 取消预警
     */
    @DeleteMapping("/subscribe")
    public Result<Void> cancelSubscribe(@RequestParam("subscribeIds") Set<Long> subscribeIds) {
        itemDonationService.cancelSubscribe(subscribeIds);
        return Result.success();
    }
}
