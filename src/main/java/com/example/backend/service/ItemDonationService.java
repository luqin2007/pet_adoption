package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.StockEvent;
import com.example.backend.mapper.*;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.ParentType.DONATION;
import static com.example.backend.util.C.KEY_DONATION;
import static com.example.backend.util.C.KEY_DONATION_FILE;

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

    private final UserService userService;

    /**
     * 准备物资捐赠
     */
    public String beginDonation() {
        User user = getLoginUser();

        String uuid = StringUtils.randomUUID(KEY_DONATION, redisHelper, 10);
        String redisKey = String.format(KEY_DONATION, uuid);
        redisHelper.putString(redisKey, String.valueOf(user.getId()), 30);
        return uuid;
    }

    /**
     * 上传捐赠影像
     */
    public String uploadDonationFile(String uuid, MultipartFile file) {
        // 权限校验
        User user = getLoginUser();
        String redisKey = String.format(KEY_DONATION, uuid);
        redisHelper.requireString(redisKey, "创建超时");
        Long userId = Long.valueOf(redisHelper.getString(redisKey));
        requireEqual(user.getId(), userId, "用户错误");

        // 上传材料
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        String name = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
        Date now = new Date();
        String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
        Path path = FileUtils.generateTempPath(DONATION, uuid);
        FileUtils.upload(file, filename, path);

        // 保存文件信息
        TempFileInfo fileInfo = new TempFileInfo(filename, filename, userId, extAndType.getSecond(), now);
        String fileKey = String.format(KEY_DONATION_FILE, uuid);
        redisHelper.putObjectToHash(fileKey, filename, fileInfo);
        redisHelper.expireObject(fileKey, 30);
        redisHelper.expireString(redisKey, 30);
        return filename;
    }

    /**
     * 删除捐赠影像
     */
    public void deleteDonationFile(String uuid, String filename) {
        // 权限校验
        User user = getLoginUser();
        String redisKey = String.format(KEY_DONATION, uuid);
        redisHelper.requireString(redisKey, "创建超时");
        Long userId = Long.valueOf(redisHelper.getString(redisKey));
        requireEqual(user.getId(), userId, "用户错误");

        // 删除文件
        List<TempFileInfo> files = redisHelper.getAndDeleteObjectsFromHash(redisKey, filename);
        require(!files.isEmpty(), "文件不存在");
        Path file = FileUtils.generateTempPath(DONATION, uuid, files.get(0).getFilename());
        FileUtils.tryDeleteFile(file);
    }

    /**
     * 确认物资捐赠
     */
    @Transactional
    public DonationResponse addDonation(DonationAddRequest request) {
        User login = getLoginUser();
        String uuid = request.getUuid();
        String redisKey = String.format(KEY_DONATION, uuid);
        redisHelper.requireString(redisKey, "创建超时");

        // 保存数据
        Donation donation = request.create(login.getId());
        donationMapper.insert(donation);
        List<DonationItem> items = request.createItems(donation);
        donationItemMapper.insert(items);

        // 转移临时文件
        String fileKey = String.format(KEY_DONATION_FILE, uuid);
        List<DonationFile> files = redisHelper.getObjectsFromHash(fileKey, TempFileInfo.class)
                .filter(file -> FileUtils.transferTempFile(file, uuid, donation.getId(), DONATION))
                .map(file -> file.createDonationFile(donation.getId()))
                .toList();
        donationFileMapper.insert(files);

        // 清理
        Path tempPath = FileUtils.generateTempPath(DONATION, uuid);
        FileUtils.tryDeleteDirectory(tempPath, false);
        return buildDonationResponse(donation, items, files, login);
    }

    /**
     * 修改捐赠内容
     */
    @Transactional
    public DonationResponse updateDonation(Long donationId, DonationUpdateRequest request) {
        // 权限校验
        User login = getLoginUser();
        Donation donation = donationMapper.requireById(donationId);
        if (donation.getStatus() == DonationStatus.CREATED) // CREATED 状态下可由用户修改
            requirePermission(login.is(donation.getUserId()) || login.isWorker());
        else
            requirePermission(login.isWorker());

        // 保存数据
        request.apply(donation);
        donationMapper.updateById(donation);
        List<DonationItem> items = request.createItems(donation);
        donationItemMapper.delete(donationItemMapper.queryByDonation(donationId));
        donationItemMapper.insert(items);

        return buildDonationResponse(donation, items, null, null);
    }

    /**
     * 修改捐赠状态
     */
    @Transactional
    public DonationResponse updateDonationStatus(Long donationId, DonationStatusUpdateRequest request) {
        // 权限校验
        User login = getLoginUser();
        Donation donation = donationMapper.requireById(donationId);
        DonationStatus status = DonationStatus.get(request.getStatus());
        // 仅 BACKING -> CLOSED 可由用户修改
        if (donation.getStatus() == DonationStatus.BACKING && status == DonationStatus.CLOSED)
            requirePermission(login.is(donation.getUserId()) || login.isWorker());
        else
            requirePermission(login.isWorker());

        // 保存数据
        DonationStatusUpdateRecord updateRecord = request.create(donation, login.getId());
        donationStatusUpdateMapper.insert(updateRecord);
        donationMapper.update(donationMapper.updateStatus(donationId, status));
        return buildDonationResponse(donation, null, null, null);
    }

    /**
     * 获取捐赠信息
     */
    public DonationResponse getDonation(Long donationId) {
        Donation donation = donationMapper.requireById(donationId);
        return buildDonationResponse(donation, null, null, null);
    }

    /**
     * 查询捐赠信息
     */
    public Page<DonationResponse> getDonations(DonationQueryParams paramRequest, PageParams pageRequest) {
        Page<Donation> page = pageRequest.createPage();
        LambdaQueryWrapper<Donation> query = donationMapper.queryByRequest(paramRequest);
        Page<Donation> result = donationMapper.selectPage(page, query);

        Set<Long> donationIds = result.getRecords().stream()
                .map(Donation::getId)
                .collect(Collectors.toSet());
        Map<Long, List<DonationFileResponse>> files = donationFileMapper.groupList(
                donationFileMapper.queryByDonations(donationIds),
                DonationFile::getDonationId,
                DonationFileResponse::create);
        List<DonationItem> itemList = donationItemMapper.selectList(donationItemMapper.queryByDonations(donationIds));
        Map<Long, Item> items = groupById(
                itemList.stream().map(DonationItem::getItemId).filter(Objects::nonNull),
                Item::getId, Item::getName, Item::getCategoryId, Item::getUnit);
        Map<Long, Category> categories = categoryMapper.groupById(
                itemList.stream().map(DonationItem::getCategoryId),
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, List<DonationItemResponse>> donationItems = itemList.stream()
                .map(item -> DonationItemResponse.createBatch(item, items, categories))
                .collect(Collectors.groupingBy(DonationItemResponse::getDonationId));
        List<DonationStatusUpdateRecord> updateRecordList = donationStatusUpdateMapper
                .selectList(donationStatusUpdateMapper.queryByDonations(donationIds));
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(Donation::getUserId),
                updateRecordList.stream().map(DonationStatusUpdateRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<DonationStatusUpdateResponse>> updateRecords = updateRecordList.stream()
                .map(record -> DonationStatusUpdateResponse.createBatch(record, users))
                .collect(Collectors.groupingBy(DonationStatusUpdateResponse::getDonationId));
        return convertDto(result,
                donation -> DonationResponse.createBatch(donation, users, files, donationItems, updateRecords));
    }

    private DonationResponse buildDonationResponse(Donation donation,
                                                   List<DonationItem> items,
                                                   List<DonationFile> files,
                                                   User user) {
        if (items == null)
            items = donationItemMapper.selectList(donationItemMapper.queryByDonation(donation.getId()));
        if (files == null)
            files = donationFileMapper.selectList(donationFileMapper.queryByDonation(donation.getId()));
        if (user == null || !user.is(donation.getUserId()))
            user = userService.requireById(donation.getUserId(), User::getId, User::getUsername, User::getAvatar);

        Map<Long, Item> itemMap = groupById(
                items.stream().map(DonationItem::getItemId).filter(Objects::nonNull),
                Item::getId, Item::getCategoryId, Item::getName, Item::getUnit);
        Map<Long, Category> categoryMap = categoryMapper.groupById(
                itemMap.values().stream().map(Item::getCategoryId),
                items.stream().map(DonationItem::getCategoryId),
                Category::getId, Category::getName);
        List<DonationStatusUpdateRecord> updateRecords = donationStatusUpdateMapper
                .selectList(donationStatusUpdateMapper.queryByDonation(donation.getId()));
        Map<Long, User> users = userService.groupById(
                updateRecords.stream().map(DonationStatusUpdateRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return DonationResponse.create(donation, user,
                files.stream()
                        .map(DonationFileResponse::create)
                        .toList(),
                items.stream()
                        .map(item -> DonationItemResponse.createBatch(item, itemMap, categoryMap))
                        .toList(),
                updateRecords.stream()
                        .map(record -> DonationStatusUpdateResponse.createBatch(record, users))
                        .toList());
    }

    /**
     * 创建物资信息
     */
    @Transactional
    public ItemResponse addItem(ItemAddRequest request) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(request.getCategoryId());

        Item item = request.create();
        save(item);
        return buildItemResponse(item);
    }

    /**
     * 修改物资信息
     */
    @Transactional
    public ItemResponse updateItem(Long itemId, ItemUpdateRequest request) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(request.getCategoryId());

        Item item = requireById(itemId);
        request.applyTo(item);
        updateById(item);
        return buildItemResponse(item);
    }

    /**
     * 获取物资详情
     */
    public ItemResponse getItem(Long itemId) {
        Item item = requireById(itemId);
        return buildItemResponse(item);
    }

    /**
     * 查询物资信息
     */
    public Page<ItemResponse> getItems(ItemQueryParams queryRequest, PageParams pageRequest) {
        Page<Item> page = pageRequest.createPage();
        LambdaQueryWrapper<Item> query = getBaseMapper().queryByRequest(queryRequest);
        Page<Item> result = page(page, query);

        Set<Long> categoryIds = result.getRecords().stream()
                .map(Item::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Category> categories = categoryMapper.groupById(categoryIds);
        return convertDto(result, item -> ItemResponse.createBatch(item, categories));
    }

    /**
     * 删除物资信息
     */
    @Transactional
    public void discardItem(Long itemId) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        update(baseMapper.discardItem(itemId));
    }

    private ItemResponse buildItemResponse(Item item) {
        Category category = categoryMapper.selectById(item.getCategoryId());
        return ItemResponse.create(item, category);
    }

    /**
     * 创建物资分类
     */
    @Transactional
    public Category addCategory(CategoryAddRequest request) {
        User login = getLoginUser();
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
        User login = getLoginUser();
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
        User login = getLoginUser();
        requirePermission(login.isWorker());
        categoryMapper.requireExist(categoryId);

        boolean used = exists(baseMapper.queryByCategory(categoryId));
        require(!used, "分类非空");
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
        User login = getLoginUser(); // 工作人员、兽医
        requirePermission(login.isWorker() || login.isDoctor());
        StockRecordAction action = StockRecordAction.get(request.getAction());

        // 创建库存
        Stock stock;
        if (action.mayCreateStock() && request.getId() == null) {
            stock = request.createEmptyStock(login.getId());
        } else {
            stock = stockMapper.requireById(request.getId(),
                    Stock::getId, Stock::getItemId, Stock::getSourceType, Stock::getExpireTime);
        }
        // 库存检查
        boolean firstRecord = action == StockRecordAction.IN && stock.getId() == null;
        BigDecimal count = new BigDecimal(request.getCount());
        switch (action) {
            case IN: // 入库
                require(stock.getExpireTime().after(new Date()), "库存已过期");
                count = stock.getCount().add(count);
                break;
            case OUT: // 出库
                require(stock.getExpireTime().after(new Date()), "库存已过期");
                count = stock.getCount().subtract(count);
                require(count.compareTo(BigDecimal.ZERO) >= 0, "库存不足");
                break;
            case DESTROY: // 销毁
                count = stock.getCount().subtract(count).min(BigDecimal.ZERO);
                break;
        }
        // 更新库存记录
        stock.setCount(count);
        stockMapper.update(stockMapper.updateCount(stock.getId(), stock.getCount()));
        StockRecord record = request.createRecord(stock, login.getId());
        stockRecordMapper.insert(record);
        // 通知
        eventPublisher.publishEvent(new StockEvent(record));

        User user = login.is(stock.getUserId()) ? login : null;
        List<StockRecordItemResponse> records = firstRecord
                ? List.of(StockRecordItemResponse.create(record, login))
                : null;
        return buildStockResponse(stock, user, records);
    }

    /**
     * 获取库存记录
     */
    public StockResponse getStock(Long stockId) {
        User login = getLoginUser();
        Stock stock = stockMapper.requireById(stockId);
        User user = login.is(stock.getUserId()) ? login : null;
        return buildStockResponse(stock, user, null);
    }

    /**
     * 查询库存物品
     */
    public Page<StockResponse> getStocks(StockQueryParams paramRequest, PageParams pageRequest) {
        User login = getLoginUser();
        if (!login.isWorker()) { // 非工作人员只能查看捐赠物品
            paramRequest.setSource(Set.of(SourceType.DONATION.name()));
        }

        // 查询 Stock
        Page<Stock> page = pageRequest.createPage();
        LambdaQueryWrapper<Stock> query = stockMapper.queryByRequest(paramRequest);
        Page<Stock> result = stockMapper.selectPage(page, query);

        // 查询其他数据
        Map<Long, Item> items = groupById(
                result.getRecords().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName, Item::getCategoryId);
        Map<Long, Category> categories = categoryMapper.groupById(
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Set<Long> stockIds = result.getRecords().stream()
                .map(Stock::getId)
                .collect(Collectors.toSet());
        Integer count = paramRequest.getCount(5);
        List<StockRecord> recordList = stockRecordMapper.queryByStocks(stockIds, count);
        Map<Long, User> users = userService.groupById(
                Stream.concat(
                        result.getRecords().stream().map(Stock::getUserId),
                        recordList.stream().map(StockRecord::getUserId)),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<StockRecordItemResponse>> records = recordList.stream()
                .map(record -> StockRecordItemResponse.createBatch(record, users))
                .collect(Collectors.groupingBy(StockRecordItemResponse::getStockId));
        return convertDto(result, stock ->
                StockResponse.createBatch(stock, items, categories, users, records));
    }

    /**
     * 查询库存物品
     */
    public Page<StockRecordResponse> getStockRecords(StockRecordQueryParams paramRequest, PageParams pageRequest) {
        User login = getLoginUser();
        if (!login.isWorker()) { // 非工作人员只能查看捐赠物品
            paramRequest.setSource(Set.of(SourceType.DONATION.name()));
        }

        Page<StockRecord> page = pageRequest.createPage();
        LambdaQueryWrapper<StockRecord> query = stockRecordMapper.queryByRequest(paramRequest);
        Page<StockRecord> result = stockRecordMapper.selectPage(page, query);

        Map<Long, Stock> stocks = stockMapper.groupById(
                result.getRecords().stream().map(StockRecord::getStockId),
                Stock::getId, Stock::getItemId, Stock::getSourceType, Stock::getExpireTime, Stock::getCreateTime);
        Map<Long, Item> items = groupById(
                stocks.values().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName);
        Map<Long, Category> categories = categoryMapper.groupById(
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(StockRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result,
                record -> StockRecordResponse.createBatch(record, stocks, items, categories, users));
    }

    private StockResponse buildStockResponse(Stock stock, User user, List<StockRecordItemResponse> records) {
        Item item = requireById(stock.getItemId(),
                Item::getId, Item::getCategoryId, Item::getName, Item::getCategoryId);
        Category category = categoryMapper.requireById(item.getCategoryId(),
                Category::getId, Category::getName);
        if (user == null || !user.is(stock.getUserId()))
            user = userService.requireById(stock.getUserId(),
                    User::getId, User::getUsername, User::getAvatar);
        if (records == null) {
            List<StockRecord> recordList = stockRecordMapper
                    .selectList(stockRecordMapper.queryByStock(stock.getId(), 5));
            Map<Long, User> users = userService.groupById(
                    recordList.stream().map(StockRecord::getUserId),
                    User::getId, User::getUsername, User::getAvatar);
            records = recordList.stream()
                    .map(record -> StockRecordItemResponse.createBatch(record, users))
                    .toList();
        }
        return StockResponse.create(stock, item, category, user, records);
    }

    /**
     * 添加预警
     */
    public SubscribeResponse addSubscribe(SubscribeAddRequest request) {
        User login = getLoginUser();
        Subscribe subscribe = request.create(login.getId());
        subscribeMapper.insert(subscribe);
        return buildSubscribeResponse(subscribe, login);
    }

    /**
     * 获取预警
     */
    public SubscribeResponse getSubscribe(Long subscribeId) {
        return buildSubscribeResponse(subscribeMapper.requireById(subscribeId), null);
    }

    private SubscribeResponse buildSubscribeResponse(Subscribe subscribe, User user) {
        SubscribeAction action = subscribe.getAction();
        if (action.bindItem()) { // ITEM_CHANGE, ITEM_COUNT
            Item item = requireById(subscribe.getElementId(),
                    Item::getId, Item::getCategoryId, Item::getName);
            Category category = categoryMapper.requireById(item.getCategoryId(),
                    Category::getId, Category::getName);
            return SubscribeResponse.createItem(subscribe, item, category);
        }
        if (action.bindCategory()) { // CATEGORY_COUNT
            Category category = categoryMapper.requireById(subscribe.getElementId(),
                    Category::getId, Category::getName);
            return SubscribeResponse.createCategory(subscribe, category);
        }
        if (action.bindStock()) { // IN_STOCK, OUT_STOCK
            Stock stock = stockMapper.requireById(subscribe.getElementId(),
                    Stock::getId, Stock::getItemId, Stock::getUserId, Stock::getCreateTime);
            Item item = requireById(stock.getItemId(),
                    Item::getId, Item::getCategoryId, Item::getName);
            Category category = categoryMapper.requireById(item.getCategoryId(),
                    Category::getId, Category::getName);
            user = user != null && user.is(stock.getUserId()) ? user : userService.requireById(stock.getUserId(),
                    User::getId, User::getUsername, User::getAvatar);
            return SubscribeResponse.createStock(subscribe, stock, item, category, user);
        }
        if (action.bindUser()) { // DONATE
            user = user != null && user.is(subscribe.getElementId()) ? user : userService.requireById(subscribe.getElementId(),
                    User::getId, User::getUsername, User::getAvatar);
            return SubscribeResponse.createUser(subscribe, user);
        }
        // Never here
        throw ServiceException.system("Never here");
    }

    /**
     * 获取预警
     */
    public Page<SubscribeResponse> getSubscribes(SubscribeQueryParams paramRequest, PageParams pageRequest) {
        Page<Subscribe> page = pageRequest.createPage();
        LambdaQueryWrapper<Subscribe> query = subscribeMapper.queryByRequest(paramRequest);
        Page<Subscribe> result = subscribeMapper.selectPage(page, query);

        Map<Long, Stock> stocks = stockMapper.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindStock()).map(Subscribe::getElementId),
                Stock::getId, Stock::getItemId, Stock::getUserId, Stock::getCreateTime);
        Map<Long, Item> items = groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindItem()).map(Subscribe::getElementId),
                stocks.values().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName);
        Map<Long, Category> categories = categoryMapper.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindCategory()).map(Subscribe::getElementId),
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindUser()).map(Subscribe::getElementId),
                stocks.values().stream().map(Stock::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result,
                subscribe -> SubscribeResponse.createBatch(subscribe, stocks, items, categories, users));
    }

    /**
     * 取消预警
     */
    public void cancelSubscribe(Set<Long> subscribeIds) {
        User login = getLoginUser();
        if (login.isWorker())
            subscribeMapper.deleteByIds(subscribeIds);
        else
            subscribeMapper.delete(subscribeMapper.deleteUserSubscribes(subscribeIds, login.getId()));
    }
}
