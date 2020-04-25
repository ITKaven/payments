package com.kaven.payments.collectinfo;

import com.google.gson.Gson;
import com.kaven.payments.pojo.Category;
import com.kaven.payments.pojo.Product;
import com.kaven.payments.service.ICategoryService;
import com.kaven.payments.service.IProductService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;

public class JingDong {

    /**
     *  "手机" , "电脑" , "毛巾" , "手表" , "空调" , "女装" ,
     *   "零食" , "男装" , "洗衣机" , "旅行箱" , "手提包" ,
     *   "书" , "键盘" , "鼠标" , "保温杯" , "剃须刀" , "电吹风" , "充电器" ,
     *   "纸巾" , "笔" , "水果" , "书包",
     *   "台灯" , "被子" , "鞋" , "口红"  , "风扇",
     *   "沙发" , "电池" , "袜子" , "洗面奶" , "牙膏" , "牙刷" ,
     *   "沐浴乳" , "肥皂" , "眼镜" , "洗发水" ,
     *   "围巾" , "PS4" , "高达" , "音响" , "显卡" ,
     *   "内存条" , "主机" , "显示屏","电视"
     * */
    private static final String[] categories  = {
            "电视"
    };

    public void collect(ICategoryService categoryService , IProductService productService) throws IOException, InterruptedException {

        for (String category : categories){
            Integer categoryId = categoryService.selectByName(category);
            if(categoryId == null){
                Category category_ = new Category();
                category_.setName(category);
                category_.setParentId(0);
                category_.setSortOrder(1);
                category_.setStatus(true);
                categoryService.insert(category_);
            }
            categoryId = categoryService.selectByName(category);
            for(int page = 11 ; page < 100 ; page++){
                Thread.sleep(1000*30);
                Document doc = Jsoup.connect("https://search.jd.com/Search?keyword="+category+"&page="+page).get();
                Elements elementsByClass = doc.getElementsByClass("gl-i-wrap");
                for (int i = 0 ; i < elementsByClass.size() ; i++){
                    // 单个商品信息
                    Element element = elementsByClass.get(i);
                    // 商品图片信息
                    Elements img = element.getElementsByTag("img");
                    Element imgElement = img.get(0);
                    String img_ = imgElement.attr("src");

                    // 商品价格信息
                    Elements price = element.getElementsByTag("i");
                    Element priceElement = price.get(0);
                    String price_ = priceElement.text();


                    // 商品名称、标题信息
                    Elements nameAndTitle = element.getElementsByClass("p-name");
                    Element nameAndTitleElement = nameAndTitle.get(0);
                    Elements a = nameAndTitleElement.getElementsByTag("a");
                    Elements em = nameAndTitleElement.getElementsByTag("em");
                    String title_ = Clear.titleClear(a.get(0).attr("title"));
                    String name_ = Clear.nameClear(em.get(0).text());

                    // 名称或者标题不符合要求 ，或者商品已经存在
                    if(title_  == null || name_ == null || productService.getByName(name_) != null) continue;


                    Product product = new Product();
                    product.setCategoryId(categoryId);
                    product.setName(name_);
                    product.setSubtitle(title_);
                    product.setMainImage(img_);
                    product.setSubImages((new Gson().toJson(img_)));
                    product.setDetail(title_);
                    product.setPrice(new BigDecimal(price_));
                    product.setStock(100);
                    product.setStatus(1);

                    productService.insert(product);
                }
            }
        }
    }
}
