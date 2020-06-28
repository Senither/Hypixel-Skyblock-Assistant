package com.senither.hypixel.hypixel.bazaar;

import net.hypixel.api.reply.AbstractReply;

import java.util.HashMap;

public class BazaarProductReply extends AbstractReply {

    private static final Product defaultProduct = new Product();

    static {
        defaultProduct.product_id = "UNKNOWN";
        defaultProduct.quick_status = new QuickStatus();
        defaultProduct.quick_status.productId = "UNKNOWN";

        defaultProduct.quick_status.sellPrice = 0D;
        defaultProduct.quick_status.sellVolume = 0D;
        defaultProduct.quick_status.sellMovingWeek = 0D;
        defaultProduct.quick_status.sellOrders = 0D;
        defaultProduct.quick_status.buyPrice = 0D;
        defaultProduct.quick_status.buyVolume = 0D;
        defaultProduct.quick_status.buyMovingWeek = 0D;
        defaultProduct.quick_status.buyOrders = 0D;
    }

    private long lastUpdated;
    private HashMap<String, Product> products;

    public long getLastUpdated() {
        return lastUpdated;
    }

    public HashMap<String, Product> getProducts() {
        return products;
    }

    public Product getProduct(BazaarProduct product) {
        return getProducts().getOrDefault(product.getKey(), defaultProduct);
    }

    public static class Product {

        private String product_id;
        private QuickStatus quick_status;

        public String getProductId() {
            return product_id;
        }

        public QuickStatus getQuickStatus() {
            return quick_status;
        }
    }

    public static class QuickStatus {

        private String productId;
        private double sellPrice;
        private double sellVolume;
        private double sellMovingWeek;
        private double sellOrders;
        private double buyPrice;
        private double buyVolume;
        private double buyMovingWeek;
        private double buyOrders;

        public String getProductId() {
            return productId;
        }

        public double getSellPrice() {
            return sellPrice;
        }

        public double getSellVolume() {
            return sellVolume;
        }

        public double getSellMovingWeek() {
            return sellMovingWeek;
        }

        public double getSellOrders() {
            return sellOrders;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public double getBuyVolume() {
            return buyVolume;
        }

        public double getBuyMovingWeek() {
            return buyMovingWeek;
        }

        public double getBuyOrders() {
            return buyOrders;
        }
    }
}
