package com.kaven.payments.collectinfo;

import com.kaven.payments.pojo.Product;
import com.kaven.payments.service.ICategoryService;
import com.kaven.payments.service.IProductService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Clear {

    private static final  String regex = ",|，|】|【|！|/|丨|》|《|\\s+";
    private static final String[] categories  = {
            "手机" , "电脑" , "毛巾" , "手表" , "空调" , "女装" ,
            "零食" , "男装" , "洗衣机" , "旅行箱" , "手提包" ,
            "书" , "键盘" , "鼠标" , "保温杯" , "剃须刀" , "电吹风" , "充电器" ,
            "纸巾" , "笔" , "水果" , "书包",
            "台灯" , "被子" , "鞋" , "口红"  , "风扇",
            "沙发" , "电池" , "袜子" , "洗面奶" , "牙膏" , "牙刷" ,
            "沐浴乳" , "肥皂" , "眼镜" , "洗发水" ,
            "围巾" , "PS4" , "高达" , "音响" , "显卡" ,
            "内存条" , "主机" , "显示屏"
    };

    public void clear(ICategoryService categoryService , IProductService productService){
        for (String category : categories){
            int categoryId = categoryService.selectByName(category);
            Set<Integer> categoryIdSet = new HashSet<>();
            categoryIdSet.add(categoryId);
            List<Product> products = productService.getProductsByCategoryIdSet(categoryIdSet);

            int cnt=0;
            for(Product product : products){
                if(cnt<=30){
                    cnt++;
                }
                else{
                    productService.delete(product);
                }
            }
        }
    }

    public static String nameClear(String name){
        String newName = "";
        String[] names = name.split(regex);
        for (int i = 0; i < Math.min(3 , names.length); i++) {
            newName= newName + names[i] + " ";
        }

        if(newName.length() >= 2 && newName.length() <= 24) return newName;

        return null;
    }

    public static String titleClear(String title){
        String newTitle = "";
        String[] titles = title.split(regex);
        for (int i = 0; i < Math.min(3 , titles.length); i++) {
           newTitle= newTitle + titles[i] + " ";
        }

        if(newTitle.length() >= 6 && newTitle.length() <= 30) return newTitle;

        return null;
    }
}
