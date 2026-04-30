package com.example.backend.event;

import com.example.backend.entity.*;
import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockAction;
import com.example.backend.entity.property.SubscribeAction;
import com.example.backend.mapper.ItemMapper;
import com.example.backend.mapper.StockMapper;
import com.example.backend.mapper.SubscribeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.NoticeSource.DONATION;
import static com.example.backend.entity.property.NoticeSource.STOCK;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class DonationEventListener extends BaseEventListener {

    private final StockMapper stockMapper;
    private final ItemMapper itemMapper;
    private final SubscribeMapper subscribeMapper;

    @TransactionalEventListener
    public void onDonationAdd(DonationAddEvent event) {
        notifyWorkers(event.user().getId(), event);
    }

    @TransactionalEventListener
    public void onDonationUpdate(DonationUpdateEvent event) {
        Donation donation = event.data();
        if (event.user().is(donation.getUserId())) { // 捐赠者修改
            notifyWorkers(event.user().getId(), event);
        } else {
            notify(donation.getUserId(), event);
        }
    }

    @TransactionalEventListener
    public void onDonationStatus(DonationStatusEvent event) {
        Donation donation = event.data();
        boolean isReply = notifyReview(event);
        if (isReply) {
            sendEmail(donation.getUserId(), event);
        }
    }

    @TransactionalEventListener
    public void onStock(StockEvent event) {
        Stock stock = event.stock();
        StockRecord record = event.record();
        String itemName = itemMapper.requireById(stock.getItemId(), Item::getName).getName();

        // 监听：物品变更
        Set<Long> notifyUsers = Stream.of(
                        subscribeMapper.queryByActionAndElement(SubscribeAction.ITEM_CHANGE, stock.getItemId()).list(),
                        record.getAction() == StockAction.IN ? subscribeMapper.queryByActionAndElement(SubscribeAction.IN_STOCK, stock.getId()).list() : List.<Subscribe>of(),
                        record.getAction() == StockAction.OUT ? subscribeMapper.queryByActionAndElement(SubscribeAction.OUT_STOCK, stock.getId()).list() : List.<Subscribe>of())
                .flatMap(List::stream)
                .map(Subscribe::getUserId)
                .collect(Collectors.toSet());
        notify(null, notifyUsers, STOCK,
                event.buildStockChangeNotifyTitle(langHelper),
                event.buildStockChangeNotifyContent(langHelper, itemName));

        // 监听：物品数量
        BigDecimal currentItemCount = stockMapper.queryByItem(stock.getItemId())
                .list(Stock::getCount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        notifyUsers = subscribeMapper.queryByActionAndElement(SubscribeAction.ITEM_COUNT, stock.getItemId()).list().stream()
                .filter(subscribe -> currentItemCount.compareTo(subscribe.getCount()) <= 0)
                .map(Subscribe::getUserId)
                .collect(Collectors.toSet());
        notify(null, notifyUsers, STOCK,
                event.buildStockLowItemNotifyTitle(langHelper),
                event.buildStockLowItemNotifyContent(langHelper, itemName));
        sendEmail(notifyUsers,
                event.buildStockLowItemMailTitle(langHelper),
                event.buildStockLowItemMailContent(langHelper, itemName));

        // 监听：物资捐赠
        if (stock.getSourceType() == SourceType.DONATION) {
            notifyUsers = subscribeMapper.queryByActionAndElement(SubscribeAction.DONATE, stock.getUserId()).list().stream()
                    .map(Subscribe::getUserId)
                    .collect(Collectors.toSet());
            notify(null, notifyUsers, DONATION,
                    event.buildDonationNotifyTitle(langHelper),
                    event.buildDonationNotifyContent(langHelper, itemName));
            sendEmail(notifyUsers,
                    event.buildDonationMailTitle(langHelper),
                    event.buildDonationMailContent(langHelper, itemName));
        }
    }
}
