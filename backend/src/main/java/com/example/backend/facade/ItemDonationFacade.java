package com.example.backend.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.service.UserService;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class ItemDonationFacade {

    private final ItemMapper itemMapper;
    private final DonationItemMapper donationItemMapper;
    private final DonationFileMapper donationFileMapper;
    private final DonationStatusUpdateMapper donationStatusUpdateMapper;
    private final CategoryMapper categoryMapper;
    private final StockMapper stockMapper;
    private final StockRecordMapper stockRecordMapper;
    private final UserService userService;

    public DonationResponse buildDonationResponse(Donation donation,
                                                  List<DonationItem> items,
                                                  List<DonationFile> files,
                                                  User user) {
        if (items == null) {
            items = donationItemMapper.queryByDonation(donation.getId()).list();
        }
        if (files == null) {
            files = donationFileMapper.queryByDonation(donation.getId()).list();
        }
        if (user == null || !user.is(donation.getUserId())) {
            user = userService.requireById(donation.getUserId(), User::getId, User::getUsername, User::getAvatar);
        }

        Map<Long, Item> itemMap = itemMapper.groupById(
                items.stream().map(DonationItem::getItemId).filter(Objects::nonNull),
                Item::getId, Item::getCategoryId, Item::getName, Item::getUnit);
        Map<Long, Category> categoryMap = categoryMapper.groupById(
                itemMap.values().stream().map(Item::getCategoryId),
                items.stream().map(DonationItem::getCategoryId),
                Category::getId, Category::getName);
        List<DonationStatusUpdateRecord> updateRecords = donationStatusUpdateMapper.queryByDonation(donation.getId()).list();
        Map<Long, User> users = userService.groupById(
                updateRecords.stream().map(DonationStatusUpdateRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return DonationResponse.create(donation, user,
                files.stream().map(DonationFileResponse::create).toList(),
                items.stream().map(item -> DonationItemResponse.createBatch(item, itemMap, categoryMap)).toList(),
                updateRecords.stream().map(record -> DonationStatusUpdateResponse.createBatch(record, users)).toList());
    }

    public Page<DonationResponse> buildDonationPage(Page<Donation> result) {
        Set<Long> donationIds = result.getRecords().stream()
                .map(Donation::getId)
                .collect(Collectors.toSet());
        Map<Long, List<DonationFileResponse>> files = donationFileMapper
                .queryByDonations(donationIds)
                .groupList(DonationFile::getDonationId, DonationFileResponse::create);
        List<DonationItem> itemList = donationItemMapper.queryByDonations(donationIds).list();
        Map<Long, Item> items = itemMapper.groupById(
                itemList.stream().map(DonationItem::getItemId).filter(Objects::nonNull),
                Item::getId, Item::getName, Item::getCategoryId, Item::getUnit);
        Map<Long, Category> categories = categoryMapper.groupById(
                itemList.stream().map(DonationItem::getCategoryId),
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, List<DonationItemResponse>> donationItems = itemList.stream()
                .map(item -> DonationItemResponse.createBatch(item, items, categories))
                .collect(Collectors.groupingBy(DonationItemResponse::getDonationId));
        List<DonationStatusUpdateRecord> updateRecordList = donationStatusUpdateMapper.queryByDonations(donationIds).list();
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(Donation::getUserId),
                updateRecordList.stream().map(DonationStatusUpdateRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<DonationStatusUpdateResponse>> updateRecords = updateRecordList.stream()
                .map(record -> DonationStatusUpdateResponse.createBatch(record, users))
                .collect(Collectors.groupingBy(DonationStatusUpdateResponse::getDonationId));
        return convert(result, donation -> DonationResponse.createBatch(donation, users, files, donationItems, updateRecords));
    }

    public ItemResponse buildItemResponse(Item item) {
        Category category = categoryMapper.selectById(item.getCategoryId());
        return ItemResponse.create(item, category);
    }

    public Page<ItemResponse> buildItemPage(Page<Item> result) {
        Set<Long> categoryIds = result.getRecords().stream()
                .map(Item::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Category> categories = categoryMapper.groupById(categoryIds);
        return convert(result, item -> ItemResponse.createBatch(item, categories));
    }

    public StockResponse buildStockResponse(Stock stock, User user, List<StockRecordItemResponse> records) {
        Item item = itemMapper.requireById(stock.getItemId(),
                Item::getId, Item::getCategoryId, Item::getName, Item::getCategoryId);
        Category category = categoryMapper.requireById(item.getCategoryId(),
                Category::getId, Category::getName);
        if (user == null || !user.is(stock.getUserId())) {
            user = userService.requireById(stock.getUserId(),
                    User::getId, User::getUsername, User::getAvatar);
        }
        if (records == null) {
            List<StockRecord> recordList = stockRecordMapper.queryByStock(stock.getId(), 5).list();
            Map<Long, User> users = userService.groupById(
                    recordList.stream().map(StockRecord::getUserId),
                    User::getId, User::getUsername, User::getAvatar);
            records = recordList.stream()
                    .map(record -> StockRecordItemResponse.createBatch(record, users))
                    .toList();
        }
        return StockResponse.create(stock, item, category, user, records);
    }

    public Page<StockResponse> buildStockPage(Page<Stock> result, Integer count) {
        Map<Long, Item> items = itemMapper.groupById(
                result.getRecords().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName, Item::getCategoryId);
        Map<Long, Category> categories = categoryMapper.groupById(
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Set<Long> stockIds = result.getRecords().stream()
                .map(Stock::getId)
                .collect(Collectors.toSet());
        List<StockRecord> recordList = stockRecordMapper.queryByStocks(stockIds, count);
        Map<Long, User> users = userService.groupById(
                Stream.concat(result.getRecords().stream().map(Stock::getUserId), recordList.stream().map(StockRecord::getUserId)),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<StockRecordItemResponse>> records = recordList.stream()
                .map(record -> StockRecordItemResponse.createBatch(record, users))
                .collect(Collectors.groupingBy(StockRecordItemResponse::getStockId));
        return convert(result, stock -> StockResponse.createBatch(stock, items, categories, users, records));
    }

    public Page<StockRecordResponse> buildStockRecordPage(Page<StockRecord> result) {
        Map<Long, Stock> stocks = stockMapper.groupById(
                result.getRecords().stream().map(StockRecord::getStockId),
                Stock::getId, Stock::getItemId, Stock::getSourceType, Stock::getExpireTime, Stock::getCreateTime);
        Map<Long, Item> items = itemMapper.groupById(
                stocks.values().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName);
        Map<Long, Category> categories = categoryMapper.groupById(
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(StockRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, record -> StockRecordResponse.createBatch(record, stocks, items, categories, users));
    }

    public SubscribeResponse buildSubscribeResponse(Subscribe subscribe, User user) {
        if (subscribe.getAction().bindItem()) {
            Item item = itemMapper.requireById(subscribe.getElementId(),
                    Item::getId, Item::getCategoryId, Item::getName);
            Category category = categoryMapper.requireById(item.getCategoryId(),
                    Category::getId, Category::getName);
            return SubscribeResponse.createItem(subscribe, item, category);
        }
        if (subscribe.getAction().bindStock()) {
            Stock stock = stockMapper.requireById(subscribe.getElementId(),
                    Stock::getId, Stock::getItemId, Stock::getUserId, Stock::getCreateTime);
            Item item = itemMapper.requireById(stock.getItemId(),
                    Item::getId, Item::getCategoryId, Item::getName);
            Category category = categoryMapper.requireById(item.getCategoryId(),
                    Category::getId, Category::getName);
            user = user != null && user.is(stock.getUserId()) ? user : userService.requireById(stock.getUserId(),
                    User::getId, User::getUsername, User::getAvatar);
            return SubscribeResponse.createStock(subscribe, stock, item, category, user);
        }
        if (subscribe.getAction().bindUser()) {
            user = user != null && user.is(subscribe.getElementId()) ? user : userService.requireById(subscribe.getElementId(),
                    User::getId, User::getUsername, User::getAvatar);
            return SubscribeResponse.createUser(subscribe, user);
        }
        throw ServiceException.system("exception.system.unreachable");
    }

    public Page<SubscribeResponse> buildSubscribePage(Page<Subscribe> result) {
        Map<Long, Stock> stocks = stockMapper.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindStock()).map(Subscribe::getElementId),
                Stock::getId, Stock::getItemId, Stock::getUserId, Stock::getCreateTime);
        Map<Long, Item> items = itemMapper.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindItem()).map(Subscribe::getElementId),
                stocks.values().stream().map(Stock::getItemId),
                Item::getId, Item::getCategoryId, Item::getName);
        Map<Long, Category> categories = categoryMapper.groupById(
                items.values().stream().map(Item::getCategoryId),
                Category::getId, Category::getName);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().filter(s -> s.getAction().bindUser()).map(Subscribe::getElementId),
                stocks.values().stream().map(Stock::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, subscribe -> SubscribeResponse.createBatch(subscribe, stocks, items, categories, users));
    }

    private <T, R> Page<R> convert(Page<T> source, Function<T, R> mapper) {
        Page<R> page = PageDTO.of(source.getCurrent(), source.getSize(), source.getTotal());
        page.setRecords(source.getRecords().stream().map(mapper).toList());
        return page;
    }
}
