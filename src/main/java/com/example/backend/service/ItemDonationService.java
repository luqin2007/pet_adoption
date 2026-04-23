package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockAction;
import com.example.backend.entity.property.UserRole;
import com.example.backend.event.DonationAddEvent;
import com.example.backend.event.DonationStatusEvent;
import com.example.backend.event.DonationUpdateEvent;
import com.example.backend.event.StockEvent;
import com.example.backend.facade.ItemDonationFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.example.backend.entity.property.ParentType.DONATION;

/**
 * 物资与捐赠管理
 */
@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class ItemDonationService extends BaseService<ItemMapper, Item> {
    private final DonationMapper donationMapper;
    private final DonationItemMapper donationItemMapper;
    private final DonationFileMapper donationFileMapper;
    private final DonationStatusUpdateMapper donationStatusUpdateMapper;
    private final CategoryMapper categoryMapper;
    private final StockMapper stockMapper;
    private final StockRecordMapper stockRecordMapper;
    private final SubscribeMapper subscribeMapper;
    private final ItemDonationFacade itemDonationFacade;

    private UserService userService;
    private FileService fileService;

    @Value("${key.donation.uuid}")
    private String donationTemplate;
    @Value("${key.donation.file}")
    private String donationFileTemplate;

    /**
     * 准备物资捐赠
     */
    public String beginDonation() {
        User user = requireLoginUser();
        return beginRedisUuid(donationTemplate, String.valueOf(user.getId()));
    }

    /**
     * 上传捐赠影像
     */
    public String uploadDonationFile(String uuid, MultipartFile file) {
        // 权限校验
        User login = requireLoginUser();
        String redisKey = requireRedisUuid(donationTemplate, uuid);
        Long userId = Long.valueOf(redisHelper.getString(redisKey));
        requirePermission(login.is(userId));

        // 上传材料
        return fileService
                .uploadTempMedia(file, null, uuid, donationFileTemplate, DONATION)
                .getFilename();
    }

    /**
     * 删除捐赠影像
     */
    public void deleteDonationFile(String uuid, String filename) {
        // 权限校验
        User login = requireLoginUser();
        String redisKey = requireRedisUuid(donationTemplate, uuid);
        Long userId = Long.valueOf(redisHelper.getString(redisKey));
        requirePermission(login.is(userId));

        // 删除文件
        fileService.deleteTempFile(donationFileTemplate, uuid, filename, DONATION);
    }

    /**
     * 确认物资捐赠
     */
    @Transactional
    public DonationResponse addDonation(DonationAddRequest request) {
        User login = requireLoginUser();
        String uuid = request.getUuid();
        String redisKey = requireRedisUuid(donationTemplate, uuid);
        Long userId = Long.valueOf(redisHelper.getString(redisKey));
        requirePermission(login.is(userId));

        // 保存数据
        Donation donation = request.create(login.getId());
        donationMapper.insert(donation);
        List<DonationItem> items = request.createItems(donation);
        donationItemMapper.insert(items);
        List<DonationFile> files = fileService.saveTempFiles(donationFileTemplate, uuid, donation, DONATION)
                .map(file -> file.createDonationFile(donation.getId()))
                .toList();
        donationFileMapper.insert(files);

        // 更新用户身份
        login.setRole(login.getRole() | UserRole.DONOR.getMask());
        userService.updateById(login);
        eventPublisher.publishEvent(new DonationAddEvent(donation, items, login));
        return itemDonationFacade.buildDonationResponse(donation, items, files, login);
    }

    /**
     * 修改捐赠内容
     */
    @Transactional
    public DonationResponse updateDonation(Long donationId, DonationUpdateRequest request) {
        // 权限校验
        User login = requireLoginUser();
        Donation donation = donationMapper.requireById(donationId);
        if (donation.getStatus() == DonationStatus.CREATED) // CREATED 状态下可由用户修改
            requirePermission(login.is(donation.getUserId()) || login.isWorker());
        else
            requirePermission(login.isWorker());

        // 保存数据
        request.apply(donation);
        donationMapper.updateById(donation);
        List<DonationItem> items = request.createItems(donation);
        donationItemMapper.queryByDonation(donationId).delete();
        donationItemMapper.insert(items);

        eventPublisher.publishEvent(new DonationUpdateEvent(donation, items, login));
        return itemDonationFacade.buildDonationResponse(donation, items, null, null);
    }

    /**
     * 修改捐赠状态
     */
    @Transactional
    public DonationResponse updateDonationStatus(Long donationId, DonationStatusUpdateRequest request) {
        // 权限校验
        User login = requireLoginUser();
        Donation donation = donationMapper.requireById(donationId);
        DonationStatus status = DonationStatus.get(request.getStatus());
        // 仅 BACKING -> CLOSED 可由用户修改
        if (donation.getStatus() == DonationStatus.BACKING && status == DonationStatus.CLOSED)
            requirePermission(login.is(donation.getUserId()) || login.isWorker());
        else
            requirePermission(login.isWorker());
        require(status.canChangeFrom(donation.getStatus()), "exception.invalidate.donation_status");

        // 保存数据
        DonationStatusUpdateRecord updateRecord = request.create(donation, login.getId());
        donationStatusUpdateMapper.insert(updateRecord);
        donationMapper.updateStatus(donationId, status).update();
        eventPublisher.publishEvent(new DonationStatusEvent(donation, updateRecord, login));
        return itemDonationFacade.buildDonationResponse(donation, null, null, null);
    }

    /**
     * 获取捐赠信息
     */
    public DonationResponse getDonation(Long donationId) {
        Donation donation = donationMapper.requireById(donationId);
        return itemDonationFacade.buildDonationResponse(donation, null, null, null);
    }

    /**
     * 查询捐赠信息
     */
    public Page<DonationResponse> getDonations(DonationQueryParams paramRequest, PageParams pageRequest) {
        Page<Donation> result = donationMapper.queryByRequest(paramRequest).page(pageRequest);
        return itemDonationFacade.buildDonationPage(result);
    }

    /**
     * 创建物资信息
     */
    @Transactional
    public ItemResponse addItem(ItemAddRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(request.getCategoryId());

        Item item = request.create();
        save(item);
        return itemDonationFacade.buildItemResponse(item);
    }

    /**
     * 修改物资信息
     */
    @Transactional
    public ItemResponse updateItem(Long itemId, ItemUpdateRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(request.getCategoryId());

        Item item = requireById(itemId);
        request.applyTo(item);
        updateById(item);
        return itemDonationFacade.buildItemResponse(item);
    }

    /**
     * 获取物资详情
     */
    public ItemResponse getItem(Long itemId) {
        Item item = requireById(itemId);
        return itemDonationFacade.buildItemResponse(item);
    }

    /**
     * 查询物资信息
     */
    public Page<ItemResponse> getItems(ItemQueryParams queryRequest, PageParams pageRequest) {
        Page<Item> result = getBaseMapper().queryByRequest(queryRequest).page(pageRequest);
        return itemDonationFacade.buildItemPage(result);
    }

    /**
     * 删除物资信息
     */
    @Transactional
    public void discardItem(Long itemId) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        baseMapper.discardItem(itemId).update();
    }

    /**
     * 创建物资分类
     */
    @Transactional
    public Category addCategory(CategoryAddRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());

        Category category = request.create();
        categoryMapper.insert(category);
        return category;
    }

    /**
     * 修改物资分类
     */
    @Transactional
    public Category updateCategory(Long categoryId, CategoryUpdateRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        Category category = categoryMapper.requireById(categoryId);

        request.applyTo(category);
        categoryMapper.updateById(category);
        return category;
    }

    /**
     * 删除物资分类
     */
    @Transactional
    public void discardCategory(Long categoryId) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(categoryId);

        boolean used = baseMapper.queryByCategory(categoryId).exists();
        if (used)
            throw ServiceException.conflict("exception.conflict.category.not_empty");
        categoryMapper.update(categoryMapper.discard(categoryId));
    }

    /**
     * 获取物资分类
     */
    public Category getCategory(Long categoryId) {
        return categoryMapper.requireById(categoryId);
    }

    /**
     * 查询物资分类
     */
    public Page<Category> getCategories(String name, PageParams pageRequest) {
        Page<Category> page = pageRequest.createPage();
        return categoryMapper.selectPage(page, categoryMapper.queryName(name));
    }

    /**
     * 入库出库登记
     */
    @Transactional
    public StockResponse addStockRecord(StockRequest request) {
        User login = requireLoginUser(); // 工作人员、兽医
        requirePermission(login.isWorker() || login.isDoctor());
        StockAction action = StockAction.get(request.getAction());

        // 创建库存
        Stock stock;
        if (action.mayCreateStock() && request.getId() == null) {
            stock = request.createEmptyStock(login.getId());
        } else {
            stock = stockMapper.requireById(request.getId(),
                    Stock::getId, Stock::getItemId, Stock::getSourceType, Stock::getExpireTime);
        }
        // 库存检查
        boolean firstRecord = action == StockAction.IN && stock.getId() == null;
        BigDecimal count = new BigDecimal(request.getCount());
        switch (action) {
            case IN: // 入库
                require(stock.getExpireTime().after(new Date()), "exception.invalidate.stock.expired");
                count = stock.getCount().add(count);
                break;
            case OUT: // 出库
                require(stock.getExpireTime().after(new Date()), "exception.invalidate.stock.expired");
                count = stock.getCount().subtract(count);
                require(count.compareTo(BigDecimal.ZERO) >= 0, "exception.invalidate.stock.insufficient");
                break;
            case DESTROY: // 销毁
                count = stock.getCount().subtract(count);
                require(count.compareTo(BigDecimal.ZERO) >= 0, "exception.invalidate.stock.insufficient");
                break;
        }
        // 更新库存记录
        stock.setCount(count);
        if (firstRecord)
            stockMapper.insert(stock);
        else
            stockMapper.updateCount(stock.getId(), stock.getCount()).update();
        StockRecord record = request.createRecord(stock, login.getId());
        stockRecordMapper.insert(record);
        // 通知
        eventPublisher.publishEvent(new StockEvent(stock, record));

        User user = login.is(stock.getUserId()) ? login : null;
        List<StockRecordItemResponse> records = firstRecord
                ? List.of(StockRecordItemResponse.create(record, login))
                : null;
        return itemDonationFacade.buildStockResponse(stock, user, records);
    }

    /**
     * 获取库存记录
     */
    public StockResponse getStock(Long stockId) {
        User login = requireLoginUser();
        Stock stock = stockMapper.requireById(stockId);
        User user = login.is(stock.getUserId()) ? login : null;
        return itemDonationFacade.buildStockResponse(stock, user, null);
    }

    /**
     * 查询库存物品
     */
    public Page<StockResponse> getStocks(StockQueryParams paramRequest, PageParams pageRequest) {
        User login = requireLoginUser();
        if (!login.isWorker()) { // 非工作人员只能查看捐赠物品
            paramRequest.setSource(Set.of(SourceType.DONATION.name()));
        }

        // 查询 Stock
        Page<Stock> result = stockMapper.queryByRequest(paramRequest).page(pageRequest);
        Integer count = paramRequest.getCount(5);
        return itemDonationFacade.buildStockPage(result, count);
    }

    /**
     * 查询库存物品
     */
    public Page<StockRecordResponse> getStockRecords(StockRecordQueryParams paramRequest, PageParams pageRequest) {
        User login = requireLoginUser();
        if (!login.isWorker()) { // 非工作人员只能查看捐赠物品
            paramRequest.setSource(Set.of(SourceType.DONATION.name()));
        }

        Page<StockRecord> result = stockRecordMapper.queryByRequest(paramRequest).page(pageRequest);
        return itemDonationFacade.buildStockRecordPage(result);
    }

    /**
     * 添加预警
     */
    public SubscribeResponse addSubscribe(SubscribeAddRequest request) {
        User login = requireLoginUser();
        Subscribe subscribe = request.create(login.getId());
        subscribeMapper.insert(subscribe);
        return itemDonationFacade.buildSubscribeResponse(subscribe, login);
    }

    /**
     * 获取预警
     */
    public SubscribeResponse getSubscribe(Long subscribeId) {
        return itemDonationFacade.buildSubscribeResponse(subscribeMapper.requireById(subscribeId), null);
    }

    /**
     * 获取预警
     */
    public Page<SubscribeResponse> getSubscribes(SubscribeQueryParams paramRequest, PageParams pageRequest) {
        Page<Subscribe> result = subscribeMapper.queryByRequest(paramRequest).page(pageRequest);
        return itemDonationFacade.buildSubscribePage(result);
    }

    /**
     * 取消预警
     */
    public void cancelSubscribe(Set<Long> subscribeIds) {
        User login = requireLoginUser();
        if (login.isWorker())
            subscribeMapper.deleteByIds(subscribeIds);
        else
            subscribeMapper.delete(subscribeMapper.deleteUserSubscribes(subscribeIds, login.getId()));
    }

    @Autowired
    public void setServices(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }
}
