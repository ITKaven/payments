package com.kaven.payments.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kaven.payments.dao.OrderItemMapper;
import com.kaven.payments.dao.OrderMapper;
import com.kaven.payments.dao.ProductMapper;
import com.kaven.payments.dao.ShippingMapper;
import com.kaven.payments.enums.OrderStatusEnum;
import com.kaven.payments.enums.PaymentTypeEnum;
import com.kaven.payments.enums.ProductStatusEnum;
import com.kaven.payments.enums.ResponseEnum;
import com.kaven.payments.pojo.*;
import com.kaven.payments.service.ICartService;
import com.kaven.payments.service.IOrderService;
import com.kaven.payments.vo.OrderItemVo;
import com.kaven.payments.vo.OrderVo;
import com.kaven.payments.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private ICartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public ResponseVo<OrderVo> create(Integer uid, Integer shippingId) {
        // 收货地址校验
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid , shippingId);
        if(shipping == null){
            return ResponseVo.error(ResponseEnum.SHIPPING_NOT_EXIST);
        }

        // 获取购物车，校验（是否有商品、库存）
        List<CartProduct> cartProductList = cartService.listForCart(uid)
                .stream()
                .filter(CartProduct::getProductSelected)
                .collect(Collectors.toList());
        if(cartProductList == null){
            return ResponseVo.error(ResponseEnum.CART_SELECTED_IS_EMPTY);
        }

        // 获取cartProductList里的productIds
        Set<Integer> productIdSet = cartProductList.stream()
                .map(CartProduct::getProductId)
                .collect(Collectors.toSet());
        List<Product> productList = productMapper.selectByProductIdSet(productIdSet);

        Map<Integer , Product> map = productList.stream()
                .collect(Collectors.toMap(Product::getId ,product -> product));

        List<OrderItem> orderItemList = new ArrayList<>();
        Long orderNo = generateOrderNo();
        for (CartProduct cartProduct : cartProductList){
            Product product = map.get(cartProduct.getProductId());
            // 是否有该商品
           if(product == null){
               return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST ,
                       "商品不存在. productId" + cartProduct.getProductId());
           }
            // 商品上下架状态
            if(!ProductStatusEnum.ON_SALE.getCode().equals(product.getStatus())){
                return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DETELE ,
                        "商品不是在售状态. productName" + product.getName());
            }
           // 库存是否充足
            if(product.getStock() < cartProduct.getQuantity()){
                return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR ,
                        "库存不正确. productName" + product.getName());
            }
            OrderItem orderItem = buildOrderItem(uid, orderNo,
                    cartProduct.getQuantity(), product);
            orderItemList.add(orderItem);

            // 减库存
            product.setStock(product.getStock() - cartProduct.getQuantity());
            int row = productMapper.updateByPrimaryKeySelective(product);
            if(row == 0){
                return ResponseVo.error(ResponseEnum.ERROR);
            }
        }
        // 计算总价 ， 只计算选中的商品
        // 生成订单 ， 入库 : order和order_item(使用事务)
        Order order = buildOrder(uid, orderNo, shippingId, orderItemList);

        int rowForOrder = orderMapper.insertSelective(order);
        if(rowForOrder == 0){
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        int rowForOrderItem = orderItemMapper.batchInsert(orderItemList);
        if(rowForOrderItem == 0){
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        // 更新购物车（删除选中的商品）
        // redis有事务（打包命令） ， 不能回滚
        for (CartProduct cartProduct : cartProductList) {
            cartService.delete(uid , cartProduct.getProductId());
        }
        // 构造orderVo
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);
        return ResponseVo.success(orderVo);
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum , pageSize);
        List<Order> orderList = orderMapper.selectByUid(uid);

        Set<Long> orderNoSet = orderList.stream()
                .map(Order::getOrderNo)
                .collect(Collectors.toSet());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        Map<Long , List<OrderItem>> orderItemMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));

        Set<Integer> shippingIdSet = orderList.stream()
                .map(Order::getShippingId)
                .collect(Collectors.toSet());
        List<Shipping> shippingList = shippingMapper.selectByShippingIdSet(shippingIdSet);
        Map<Integer , Shipping> shippingMap = shippingList.stream()
                .collect(Collectors.toMap(Shipping::getId , shipping -> shipping));
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList) {
            OrderVo orderVo = buildOrderVo(order, orderItemMap.get(order.getOrderNo()),
                    shippingMap.get(order.getShippingId()));
            orderVoList.add(orderVo);
        }

        // TODO
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<OrderVo> detail(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null || !order.getUserId().equals(uid)){
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);
        return ResponseVo.success(orderVo);
    }

    @Override
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null || !order.getUserId().equals(uid)){
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        if(!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())){
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setCloseTime(new Date());
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if(row == 0){
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        return ResponseVo.success();
    }

    private OrderVo buildOrderVo(Order order, List<OrderItem> orderItemList, Shipping shipping){
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(order , orderVo);

        List<OrderItemVo> orderItemVoList = orderItemList.stream()
                .map(orderItem -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    BeanUtils.copyProperties(orderItem , orderItemVo);
                    return orderItemVo;
                }).collect(Collectors.toList());
        orderVo.setOrderItemVoList(orderItemVoList);
        if(shipping != null){
            orderVo.setShippingId(shipping.getId());
            orderVo.setShippingVo(shipping);
        }
        return orderVo;
    }

    private Order buildOrder(Integer uid , Long orderNo , Integer shippingId ,
                             List<OrderItem> orderItemList) {
        BigDecimal payment = orderItemList.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = new Order();
        order.setUserId(uid);
        order.setOrderNo(orderNo);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(PaymentTypeEnum.PAY_ONLINE.getCode());
        order.setPostage(0);
        order.setStatus(OrderStatusEnum.NO_PAY.getCode());
        return order;
    }

    private Long generateOrderNo() {
        return System.currentTimeMillis() + new Random().nextInt(999);
    }

    private OrderItem buildOrderItem(Integer uid , Long orderNo , Integer quantity ,
                                     Product product){
        OrderItem item = new OrderItem();
        item.setUserId(uid);
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setCurrentUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
