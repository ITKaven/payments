package com.kaven.payments.service.impl;

import com.google.gson.Gson;
import com.kaven.payments.consts.MallConst;
import com.kaven.payments.dao.ProductMapper;
import com.kaven.payments.enums.ProductStatusEnum;
import com.kaven.payments.enums.ResponseEnum;
import com.kaven.payments.form.CartAddForm;
import com.kaven.payments.form.CartUpdateForm;
import com.kaven.payments.pojo.CartProduct;
import com.kaven.payments.pojo.Product;
import com.kaven.payments.service.ICartService;
import com.kaven.payments.vo.CartProductVo;
import com.kaven.payments.vo.CartVo;
import com.kaven.payments.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Gson gson = new Gson();

    @Override
    public ResponseVo<CartVo> add(Integer uid , CartAddForm form) {
        Integer quantity = 1;
        Product product = productMapper.selectByPrimaryKey(form.getProductId());
        // 商品是否存在
        if(product == null){
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }
        // 商品是否正常出售
        if(product.getStatus().equals(ProductStatusEnum.OFF_SALE)
                || product.getStatus().equals(ProductStatusEnum.DETELE)){
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DETELE);
        }
        // 商品库存是否充足
        if(product.getStock() <= 0){
            return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }

        // 写入redis
        // key: cart_uid productId
        // value : CartProduct
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);

        String redisValue = opsForHash.get(redisKey , String.valueOf(product.getId()));
        CartProduct cartProduct;
        if(StringUtils.isEmpty(redisValue)){
            // 没有该商品
            cartProduct = new CartProduct(product.getId() , quantity, form.getSelected());
        }
        else{
            // 已经有了，数量加1
            cartProduct = gson.fromJson(redisValue , CartProduct.class);
            cartProduct.setQuantity(cartProduct.getQuantity() + 1);
        }
        opsForHash.put(
                redisKey ,
                String.valueOf(product.getId()) ,
                gson.toJson(cartProduct));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);
        Map<String , String> entries = opsForHash.entries(redisKey);

        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        boolean selectAll = true;
        Integer cartTotalQuantity = 0;
        BigDecimal cartTotalPrice = new BigDecimal("0");
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            CartProduct cartProduct = gson.fromJson(entry.getValue() , CartProduct.class);

            // TODO 需要优化 ， 使用MySQL里面的in
            Product product = productMapper.selectByPrimaryKey(productId);
            if(product != null){
                CartProductVo cartProductVo = new CartProductVo(productId ,
                        cartProduct.getQuantity() ,
                        product.getName() ,
                        product.getSubtitle() ,
                        product.getMainImage() ,
                        product.getPrice() ,
                        product.getStatus() ,
                        product.getPrice().multiply(BigDecimal.valueOf(cartProduct.getQuantity())) ,
                        product.getStock() ,
                        cartProduct.getProductSelected()
                        );
                cartProductVoList.add(cartProductVo);

                if(!cartProduct.getProductSelected()){
                    selectAll = false;
                }
                else{
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }
            }
            cartTotalQuantity += cartProduct.getQuantity();
        }
        cartVo.setSelectedAll(selectAll);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId,
                                     CartUpdateForm form) {
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);

        String redisValue = opsForHash.get(redisKey , String.valueOf(productId));
        if(StringUtils.isEmpty(redisValue)){
            // 购物车中无此商品 , 报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        // 已经有了，修改内容
        CartProduct cartProduct = gson.fromJson(redisValue , CartProduct.class);
        if(form.getQuantity() != null
                && form.getQuantity() >= 0 && form.getSelected() != null){
            cartProduct.setQuantity(form.getQuantity());
            cartProduct.setProductSelected(form.getSelected());
        }
        opsForHash.put(redisKey , String.valueOf(productId) ,
                gson.toJson(cartProduct));
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);

        String redisValue = opsForHash.get(redisKey , String.valueOf(productId));
        if(StringUtils.isEmpty(redisValue)){
            // 购物车中无此商品 , 报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        // 已经有了，删除
        opsForHash.delete(redisKey , String.valueOf(productId));
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);

        for (CartProduct cartProduct : listForCart(uid)){
            cartProduct.setProductSelected(true);
            opsForHash.put(redisKey ,
                    String.valueOf(cartProduct.getProductId()) ,
                    gson.toJson(cartProduct));
        }
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);

        for (CartProduct cartProduct : listForCart(uid)){
            cartProduct.setProductSelected(false);
            opsForHash.put(redisKey ,
                    String.valueOf(cartProduct.getProductId()) ,
                    gson.toJson(cartProduct));
        }
        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        Integer sum = listForCart(uid)
                .stream()
                .map(CartProduct::getQuantity)
                .reduce( 0 , Integer::sum);
        return ResponseVo.success(sum);
    }

    private List<CartProduct> listForCart(Integer uid){
        HashOperations<String , String , String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(MallConst.CART_REDIS_KEY_TEMPLATE , uid);
        Map<String , String> entries = opsForHash.entries(redisKey);

        List<CartProduct> cartProductList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            cartProductList.add(gson.fromJson(entry.getValue() , CartProduct.class));
        }
        return cartProductList;
    }
}
